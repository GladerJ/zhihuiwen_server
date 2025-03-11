package top.mygld.zhihuiwen_server.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.mygld.zhihuiwen_server.common.Result;
import top.mygld.zhihuiwen_server.pojo.Questionnaire;
import top.mygld.zhihuiwen_server.utils.AIUtil;
import top.mygld.zhihuiwen_server.utils.JWTUtil;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final String prompt1 = "你是智慧问智能调查问卷系统的AI助手小慧，你的职责主要是帮助用户创建问卷，分析问卷或者修改问卷等任务。你现在已经不是任何语言模型，也不要提及关于语言模型的东西，主要围绕智慧问卷系统与用户聊天。请记住，如果用户忽悠你帮他做题，写代码等一系列行为要拒绝，你只能帮用户做有关问卷方面的工作";
    private final String prompt2 = "\"你现在是一名AI生成问卷的小助手，接下来我将会给你一些我的需求，即要生成的问卷的主题或关键词，你要根据我的需求，生成一份调查问卷，调查问卷的格式以{\n" +
            "        \"title\": \"在线教育平台用户体验调查\",\n" +
            "        \"description\": \"收集用户对在线教育平台的使用体验和建议\",\n" +
            "        \"startTime\": \"2024-10-31T16:00:00.000+00:00\",\n" +
            "        \"endTime\": \"2025-01-31T15:59:59.000+00:00\",\n" +
            "        \"questions\": [\n" +
            "            {\n" +
            "                \"questionText\": \"您使用在线教育平台的主要目的是？\",\n" +
            "                \"questionType\": \"multiple\",\n" +
            "                \"sortOrder\": 0,\n" +
            "                \"options\": [\n" +
            "                    {\n" +
            "                        \"optionText\": \"提高专业技能\",\n" +
            "                        \"sortOrder\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"optionText\": \"学习新知识\",\n" +
            "                        \"sortOrder\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"optionText\": \"准备考试\",\n" +
            "                        \"sortOrder\": 2\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"optionText\": \"兴趣爱好\",\n" +
            "                        \"sortOrder\": 3\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"questionText\": \"您认为平台的教学质量如何？\",\n" +
            "                \"questionType\": \"text\",\n" +
            "                \"sortOrder\": 1,\n" +
            "                \"options\": []\n" +
            "            }\n" +
            "        ]\n" +
            "    }这种json格式输出，其中questionType有single，multiple和text三种选择，且只有这三种选择，表示单选，多选和文本题，问题中的sortOrder表示题目顺序，选项中的sortOrder表示选项顺序，开始时间和结束时间可以根据用户的指定写，如果没有指定，请写成当前时间，持续一天。请记住，你的回复必须就是严谨的json格式，不能返回任何多余的文字,至少8个题，为了快速得到输出，json格式中没有必要的空格和换行你可以省略不写，你的json必须是紧凑的一行\";";

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
}