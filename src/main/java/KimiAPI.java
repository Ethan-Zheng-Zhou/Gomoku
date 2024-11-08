import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.List;
import java.time.Duration;
import java.util.function.Consumer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class KimiAPI {
    private static final String API_KEY = "输入您自己的api-key";
    private static final String BASE_URL = "https://api.moonshot.cn/v1/chat/completions";
    private static final String MODEL = "moonshot-v1-8k";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    
    public static void getGameAnalysisStream(List<Move> moves, Consumer<String> onResponse, Runnable onComplete) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT)
                .writeTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .build();
            
            // 构建请求体
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("model", MODEL);
            jsonBody.addProperty("temperature", 0.3);
            jsonBody.addProperty("stream", true); // 启用流式输出
            
            JsonArray messages = new JsonArray();
            
            // 添加系统消息
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", 
                "你是一个专业的五子棋分析师，请对以下棋局进行分析。分析内容包括：整体局势评估、关键转折点、双方优劣势、以及可以改进的地方。另外，回答的内容不要用Markdown格式,使用纯文本格式。");
            messages.add(systemMessage);
            
            // 构建棋局描述
            StringBuilder gameDescription = new StringBuilder("这是一局五子棋对局，按照顺序记录每一步：\n");
            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                gameDescription.append(String.format("第%d手：%s在(%d,%d)\n", 
                    i + 1, 
                    move.isBlack() ? "黑棋" : "白棋",
                    move.getX() + 1, 
                    move.getY() + 1));
            }
            
            // 添加用户消息
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", gameDescription.toString());
            messages.add(userMessage);
            
            jsonBody.add("messages", messages);
            
            // 构建请求
            Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
                ))
                .build();
            
            // 发送请求并处理流式响应
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    e.printStackTrace();
                    String errorMsg = "AI分析失败：" + e.getMessage();
                    onResponse.accept(errorMsg);
                    onComplete.run();
                }
                
                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            throw new java.io.IOException("API请求失败: " + response.code());
                        }
                        
                        BufferedReader reader = new BufferedReader(
                            new java.io.InputStreamReader(responseBody.byteStream())
                        );
                        
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String jsonData = line.substring(6).trim();
                                if ("[DONE]".equals(jsonData)) {
                                    break;
                                }
                                
                                try {
                                    JsonObject jsonResponse = new Gson().fromJson(jsonData, JsonObject.class);
                                    JsonObject deltaObject = jsonResponse
                                        .getAsJsonArray("choices")
                                        .get(0)
                                        .getAsJsonObject()
                                        .getAsJsonObject("delta");
                                    
                                    // 检查delta对象中是否包含content字段
                                    if (deltaObject != null && deltaObject.has("content")) {
                                        String content = deltaObject.get("content").getAsString();
                                        onResponse.accept(content);
                                    }
                                } catch (Exception e) {
                                    System.err.println("解析JSON响应时出错: " + e.getMessage());
                                    continue; // 跳过这一条错误的数据，继续处理下一条
                                }
                            }
                        }
                        onComplete.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                        String errorMsg = "处理响应时出错：" + e.getMessage();
                        onResponse.accept(errorMsg);
                        onComplete.run();
                    }
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "AI分析失败：\n" +
                "错误类型：" + e.getClass().getSimpleName() + "\n" +
                "错误信息：" + e.getMessage() + "\n\n" +
                "可能的解决方法：\n" +
                "1. 检查网络连接\n" +
                "2. 确认API密钥是否正确\n" +
                "3. 检查代理设置（如果使用）\n" +
                "4. 尝试使用VPN或其他网络环境\n";
            onResponse.accept(errorMsg);
            onComplete.run();
        }
    }
    
    public static String getGameAnalysis(List<Move> moves) {
        StringBuilder result = new StringBuilder();
        Object lock = new Object();
        
        getGameAnalysisStream(
            moves,
            content -> result.append(content),
            () -> {
                synchronized (lock) {
                    lock.notify();
                }
            }
        );
        
        // 等待分析完成
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return result.toString();
    }
} 