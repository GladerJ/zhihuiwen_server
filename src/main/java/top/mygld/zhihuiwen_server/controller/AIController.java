package top.mygld.zhihuiwen_server.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.dto.QuestionDTO;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireQuestion;
import top.mygld.zhihuiwen_server.pojo.Report;
import top.mygld.zhihuiwen_server.pojo.Response;
import top.mygld.zhihuiwen_server.service.ReportService;
import top.mygld.zhihuiwen_server.service.ResponseService;
import top.mygld.zhihuiwen_server.service.QuestionnaireService;
import top.mygld.zhihuiwen_server.utils.AIUtil;
import top.mygld.zhihuiwen_server.utils.JWTUtil;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionException;

import static top.mygld.zhihuiwen_server.prompt.AIPrompt.*;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private ReportService reportService;

    // 存储活跃的 SseEmitter 实例，用于管理和取消生成任务
    private final ConcurrentHashMap<Long, SseEmitter> activeEmitters = new ConcurrentHashMap<>();

    @PostMapping("/modifyQuestion")
    public Result<QuestionnaireQuestion> modifyQuestion(@RequestBody QuestionDTO questionDTO) {
        String result = AIUtil.generate(prompt3, JSON.toJSONString(questionDTO), false);
        QuestionnaireQuestion questionnaireQuestion = JSON.parseObject(result, QuestionnaireQuestion.class);
        return Result.success(questionnaireQuestion);
    }


    @GetMapping("/generateQuestionnaire")
    public Result<Questionnaire> generateQuestionnaire(@RequestParam String content) {
        String result = AIUtil.generate(prompt2, content, false);
        Questionnaire questionnaire = JSON.parseObject(result, Questionnaire.class);
        return Result.success(questionnaire);
    }

    /**
     * 取消生成过程的端点
     * @param questionnaireId 问卷ID
     * @param token 认证令牌
     * @return 操作结果
     */
    @PostMapping("/cancelGeneration")
    public Result<String> cancelGeneration(@RequestParam Long questionnaireId, @RequestParam String token) {
        // 验证token
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("未授权");
        }

        // 检查是否有权限访问该问卷
        boolean hasPermission = questionnaireService.checkQuestionnaireForUserId(userId, questionnaireId);
        if (!hasPermission) {
            return Result.error("无权限访问该问卷");
        }

        // 获取并关闭 emitter
        SseEmitter emitter = activeEmitters.remove(questionnaireId);
        if (emitter != null) {
            try {
                emitter.complete();
                return Result.success("生成已取消");
            } catch (Exception e) {
                // 忽略关闭时的错误
                return Result.success("生成已取消，但处理过程中发生错误");
            }
        }

        return Result.success("没有找到进行中的生成任务");
    }

    /**
     * 基于 SSE 的流式输出示例
     *
     * @param content 用户输入内容
     * @param token   用户登录令牌
     */
    @GetMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String content, @RequestParam String token) {
        // 简单校验 Token
        if (JWTUtil.getUserIdFromToken(token) == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token is missing or invalid");
        }

        // timeout = 0L 表示无限等待
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            try {
                // 调用流式生成方法
                AIUtil.generate(prompt1, content, true, partialText -> {
                    try {
                        // 每获取一段文本，发送到前端
                        Result<String> partialResult = new Result<>(200, "success", partialText);
                        emitter.send(SseEmitter.event().data(partialResult));
                    } catch (IOException e) {
                        // 出错后结束 SSE，并抛出异常来中断后续回调
                        emitter.completeWithError(e);
                        throw new RuntimeException(e);
                    }
                });

                // 全部生成完后发送结束标记
                Result<String> doneResult = new Result<>(200, "success", "[DONE]");
                emitter.send(SseEmitter.event().data(doneResult));
                emitter.complete();

            } catch (Exception e) {
                // 出现其他异常时，也需要结束 SSE
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping(value = "/streamGenerateQuestionnaire", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // SSE类型: text/event-stream
    public SseEmitter streamGenerateQuestionnaire(@RequestParam String content, @RequestParam String token) {
        // 校验 Token
        if (JWTUtil.getUserIdFromToken(token) == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token is missing or invalid");
        }
        // timeout = 0L 表示无限等待
        SseEmitter emitter = new SseEmitter(0L);

        // 如果 content 为空，直接返回结束标志
        if (content == null || content.trim().isEmpty()) {
            try {
                emitter.send(SseEmitter.event().data(new Result<>(200, "success", "[DONE]")));
            } catch (IOException e) {
                emitter.completeWithError(e);
                return emitter;
            }
            emitter.complete();
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                StringBuilder buffer = new StringBuilder();
                // 调用流式生成方法，传入 prompt4 和用户输入内容
                AIUtil.generate(prompt4, content, true, partialText -> {
                    try {
                        // 将每次回调的文本追加到缓冲区
                        buffer.append(partialText);
                        // 持续检查是否能提取出完整的 token 格式数据
                        // 格式示例：#{问卷标题#}
                        while (true) {
                            int startIdx = buffer.indexOf("#{");
                            int endIdx = buffer.indexOf("#}");
                            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                                // 提取完整内容（包括分隔符）
                                String tokenContent = buffer.substring(startIdx, endIdx + 2);
                                // 删除已发送的内容
                                buffer.delete(startIdx, endIdx + 2);
                                // 去除开头的 "#{", 结尾的 "#}" 只保留中间内容
                                String extractedContent = tokenContent.substring(2, tokenContent.length() - 2);
                                // 每获取一段完整数据，包装成结果对象后流式发送给前端
                                Result<String> partialResult = new Result<>(200, "success", extractedContent);
                                emitter.send(SseEmitter.event().data(partialResult));
                            } else {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                        throw new RuntimeException(e);
                    }
                });
                // 生成完后，若缓冲区中还有未被拆解的内容，也发送出去
                if (buffer.length() > 0) {
                    String remaining = buffer.toString().trim();
                    if (!remaining.isEmpty()) {
                        emitter.send(SseEmitter.event().data(new Result<>(200, "success", remaining)));
                    }
                }
                // 发送结束标记
                emitter.send(SseEmitter.event().data(new Result<>(200, "success", "[DONE]")));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }


    @GetMapping(value = "/streamAnalyze", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnalyze(@RequestParam Long questionnaireId, @RequestParam String token) {
        // 简单校验 Token
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token is missing or invalid");
        }

        // 检查用户是否有权限访问该问卷
        boolean hasPermission = questionnaireService.checkQuestionnaireForUserId(userId, questionnaireId);
        if (!hasPermission) {
            // 对于SSE请求不能直接返回Result对象，需要创建一个emitter并立即完成它
            SseEmitter emitter = new SseEmitter(0L);
            try {
                Result<String> errorResult = new Result<>(403, "无权限访问该问卷", null);
                emitter.send(SseEmitter.event().data(errorResult));
                emitter.complete();
            } catch (Exception e) {
                // 忽略发送错误
            }
            return emitter;
        }

        // 设置超时时间为3分钟
        SseEmitter emitter = new SseEmitter(180000L);

        // 保存到活跃emitters映射中
        activeEmitters.put(questionnaireId, emitter);

        // 设置完成、超时和错误时的回调，确保清理资源
        emitter.onCompletion(() -> {
            activeEmitters.remove(questionnaireId);
            System.out.println("SSE连接完成，问卷ID: " + questionnaireId);
        });

        emitter.onTimeout(() -> {
            activeEmitters.remove(questionnaireId);
            System.out.println("SSE连接超时，问卷ID: " + questionnaireId);
        });

        emitter.onError(e -> {
            activeEmitters.remove(questionnaireId);
            // 静默处理客户端断开连接错误
            if (isClientAbortException(e)) {
                System.out.println("客户端断开连接，问卷ID: " + questionnaireId);
            } else {
                System.out.println("SSE连接错误，问卷ID: " + questionnaireId + ", 错误: " + e.getMessage());
            }
        });

        // 不使用CompletableFuture.runAsync的exceptionally方法，而是在内部捕获所有异常
        CompletableFuture.runAsync(() -> {
            // 用于保存完整的生成内容
            StringBuilder fullContent = new StringBuilder();

            // 创建一个标记，用于追踪生成是否已被取消
            boolean[] cancelled = new boolean[1]; // 使用数组实现可变布尔值

            try {
                // 获取问卷和回答数据
                Questionnaire questionnaire = questionnaireService.selectQuestionnaireByIdDetail(questionnaireId, userId);
                if (questionnaire == null) {
                    throw new RuntimeException("问卷不存在");
                }

                List<Response> responses = responseService.selectAllResponsesByQuestionnaireId(questionnaireId);
                String content = questionnaire.toString() + '\n' + responses.toString();

                // 确保AI生成过程中的任何异常都在此处捕获并处理
                try {
                    // 调用流式生成方法
                    AIUtil.generate(prompt5, content, true, partialText -> {
                        // 如果已经取消或emitter不再活跃，直接返回
                        if (cancelled[0] || !activeEmitters.containsKey(questionnaireId)) {
                            return;
                        }

                        try {
                            // 累积完整内容
                            fullContent.append(partialText);

                            // 每获取一段文本，发送到前端
                            Result<String> partialResult = new Result<>(200, "success", partialText);
                            emitter.send(SseEmitter.event().data(partialResult));
                        } catch (Exception e) {
                            // 发送出错，标记为已取消，防止后续回调
                            cancelled[0] = true;
                            activeEmitters.remove(questionnaireId);

                            // 静默处理客户端断开连接错误
                            if (isClientAbortException(e)) {
                                System.out.println("客户端断开连接，停止生成，问卷ID: " + questionnaireId);
                            } else {
                                System.out.println("发送数据时出错，停止生成，问卷ID: " + questionnaireId + ", 错误: " + e.getMessage());
                            }

                            // 保存已生成的内容
                            if (fullContent.length() > 0) {
                                try {
                                    saveReportToDatabase(questionnaireId, fullContent.toString());
                                } catch (Exception saveEx) {
                                    // 忽略保存过程中的错误
                                    System.out.println("保存已生成内容时出错: " + saveEx.getMessage());
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    // 生成过程中出现异常，但不重新抛出
                    System.out.println("AI生成过程中发生异常，问卷ID: " + questionnaireId + ", 错误: " + e.getMessage());

                    // 尝试保存已生成的内容
                    if (fullContent.length() > 0) {
                        try {
                            saveReportToDatabase(questionnaireId, fullContent.toString());
                        } catch (Exception saveEx) {
                            // 忽略保存过程中的错误
                        }
                    }

                    // 标记为已取消，避免继续处理
                    cancelled[0] = true;
                }

                // 如果生成未被取消且emitter仍然活跃，完成正常流程
                if (!cancelled[0] && activeEmitters.containsKey(questionnaireId)) {
                    try {
                        // 保存报告到数据库
                        saveReportToDatabase(questionnaireId, fullContent.toString());

                        // 发送完成标记
                        Result<String> doneResult = new Result<>(200, "success", "[DONE]");
                        emitter.send(SseEmitter.event().data(doneResult));
                        emitter.complete();
                    } catch (Exception e) {
                        // 忽略发送完成标记时可能出现的异常
                        if (isClientAbortException(e)) {
                            System.out.println("发送完成标记时客户端已断开连接，问卷ID: " + questionnaireId);
                        } else {
                            System.out.println("发送完成标记时出错，问卷ID: " + questionnaireId + ", 错误: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                // 捕获并处理整个过程中的所有异常
                System.out.println("处理请求过程中发生异常，问卷ID: " + questionnaireId + ", 错误: " + e.getMessage());

                // 尝试发送错误消息，但忽略可能出现的异常
                if (activeEmitters.containsKey(questionnaireId) && !cancelled[0]) {
                    try {
                        Result<String> errorResult = new Result<>(500, "服务器错误: " + e.getMessage(), null);
                        emitter.send(SseEmitter.event().data(errorResult));
                        emitter.complete();
                    } catch (Exception sendEx) {
                        // 忽略发送错误时的异常
                    }
                }

                // 尝试保存已生成的内容
                if (fullContent.length() > 0) {
                    try {
                        saveReportToDatabase(questionnaireId, fullContent.toString());
                    } catch (Exception saveEx) {
                        // 忽略保存过程中的错误
                    }
                }
            } finally {
                // 确保从活跃映射中移除，释放资源
                activeEmitters.remove(questionnaireId);
            }
        });

        return emitter;
    }

    /**
     * 检查异常是否是客户端断开连接导致的
     */
    private boolean isClientAbortException(Throwable e) {
        if (e == null) return false;

        // 检查异常类型
        if (e instanceof org.apache.catalina.connector.ClientAbortException) return true;
        if (e instanceof java.net.SocketException) return true;

        // 检查异常消息
        String message = e.getMessage();
        if (message != null) {
            String lowerMsg = message.toLowerCase();
            if (lowerMsg.contains("connection abort") ||
                    lowerMsg.contains("broken pipe") ||
                    lowerMsg.contains("connection reset") ||
                    lowerMsg.contains("connection was aborted")) {
                return true;
            }
        }

        // 递归检查cause
        return isClientAbortException(e.getCause());
    }

    /**
     * 保存报告到数据库
     */
    private void saveReportToDatabase(Long questionnaireId, String content) {
        try {
            // 创建报告对象
            Report report = new Report();
            report.setQuestionnaireId(questionnaireId);
            report.setContent(content);
            report.setCreatedAt(new Date()); // 设置创建时间

            // 检查是否已存在此问卷的报告
            Report existingReport = reportService.selectReportByQuestionnaireId(questionnaireId);

            if (existingReport != null) {
                // 已存在，更新报告
                report.setId(existingReport.getId());
                reportService.updateReport(report);
            } else {
                // 不存在，插入新报告
                reportService.insertReport(report);
            }
        } catch (Exception e) {
            // 记录错误但不抛出异常
            e.printStackTrace();
        }
    }
}