package top.mygld.zhihuiwen_server.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Component
public class AIUtil {
    private static String key;
    private static String url;
//    private static String model;

    @Value("${ai.key}")
    public void setKey(String key) {
        AIUtil.key = key;
    }

    @Value("${ai.url}")
    public void setUrl(String url) {
        AIUtil.url = url;
    }

//    @Value("${ai.model}")
//    public void setModel(String model) {
//        AIUtil.model = model;
//    }

    /**
     * 生成AI响应（支持流式输出，不带回调）
     */
    public static String generate(String prompt, String content,String model,boolean stream) {
        return generate(prompt, content, stream,model, null);
    }

    /**
     * 生成AI响应（支持流式输出 + 回调）
     *
     * @param prompt         提示词（作为system消息）
     * @param content        用户输入（作为user消息）
     * @param stream         是否流式输出
     * @param streamCallback 每获得一段文本时的回调（仅在 stream = true 时生效）
     * @return 若非流式，返回完整结果；若是流式，返回最终拼接的完整字符串（同时会实时调用回调）
     */
    public static String generate(String prompt,
                                  String content,
                                  boolean stream,
                                  String model,
                                  Consumer<String> streamCallback) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            JSONObject requestBody = new JSONObject();
            if(model == null){
                requestBody.put("model", "Qwen/Qwen2.5-Coder-32B-Instruct");
            }
            else requestBody.put("model", model);
            JSONArray messages = new JSONArray();
            // 创建系统消息
            if (prompt != null && !prompt.isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", prompt);
                messages.add(systemMessage);
            }
            // 创建用户消息
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", content);
            messages.add(userMessage);
            requestBody.put("messages", messages);
            requestBody.put("stream", stream);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + key)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            StringBuilder finalResult = new StringBuilder();
            if (stream) {
                // 添加中断检查机制
                AtomicBoolean cancelled = new AtomicBoolean(false);
                // 流式处理 - 使用InputStream直接处理
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 检查是否被请求取消
                        if (Thread.currentThread().isInterrupted() || cancelled.get()) {
                            break;
                        }

                        if (line.startsWith("data: ")) {
                            String jsonStr = line.substring(6).trim();
                            if ("[DONE]".equals(jsonStr)) {
                                continue;
                            }
                            try {
                                JSONObject jsonObject = JSON.parseObject(jsonStr);
                                JSONArray choices = jsonObject.getJSONArray("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    JSONObject choice = choices.getJSONObject(0);
                                    JSONObject delta = choice.getJSONObject("delta");
                                    String text = delta.getString("content");
                                    if (text != null && !text.isEmpty()) {
                                        finalResult.append(text);
                                        // 实时调用回调
                                        if (streamCallback != null) {
                                            try {
                                                streamCallback.accept(text);
                                            } catch (RuntimeException e) {
                                                // 如果回调抛出特定异常，标记为已取消并退出
                                                if (e instanceof CompletionException &&
                                                        e.getCause() instanceof RuntimeException &&
                                                        "GENERATION_CANCELLED".equals(e.getCause().getMessage())) {
                                                    cancelled.set(true);
                                                    break;
                                                }
                                                throw e; // 重新抛出其他异常
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 处理异常但不重复打印已知的取消异常
                                if (!(e instanceof CompletionException &&
                                        e.getCause() instanceof RuntimeException &&
                                        "GENERATION_CANCELLED".equals(e.getCause().getMessage()))) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else {
                // 非流式
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject responseObj = JSON.parseObject(response.body());
                JSONArray choices = responseObj.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject msgObj = choice.getJSONObject("message");
                    String text = msgObj.getString("content");
                    finalResult.append(text);
                }
            }

            return finalResult.toString();

        } catch (Exception e) {
            // 处理异常但不重复打印已知的取消异常
            if (!(e instanceof CompletionException &&
                    e.getCause() instanceof RuntimeException &&
                    "GENERATION_CANCELLED".equals(e.getCause().getMessage()))) {
                e.printStackTrace();
            }
            return "Error generating response: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
//        key = "sk-ctpycdhnzamkasvwwfxfposlgxcyrwltebtfnroshzkxwgtx";
//        url = "https://api.siliconflow.cn/v1/chat/completions";
//        model = "Qwen/QwQ-32B";
//
//        // 测试调用
//        String systemPrompt = "你是一个智能助手，请用简洁的语言回答问题";
//        String userContent = "什么是Python？";
//        System.out.println("开始流式输出:");
//        AIUtil.generate(systemPrompt, userContent, true, System.out::println);
    }
}