package top.mygld.zhihuiwen_server.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.dto.QuestionDTO;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.pojo.QuestionnaireQuestion;
import top.mygld.zhihuiwen_server.utils.AIUtil;
import top.mygld.zhihuiwen_server.utils.JWTUtil;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static top.mygld.zhihuiwen_server.prompt.AIPrompt.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

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






}