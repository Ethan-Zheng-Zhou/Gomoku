import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.List;
import java.time.Duration;

public class KimiAPI {
    private static final String API_KEY = "sk-kUdsaxDPwsnCMMH3jsaFfQSXU99VXHXSeWp6CVrKKmt8H0hl";
    private static final String BASE_URL = "https://api.moonshot.cn/v1/chat/completions";
    private static final String MODEL = "moonshot-v1-8k";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    
    public static String getGameAnalysis(List<Move> moves) {
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
            
            JsonArray messages = new JsonArray();
            
            // 添加系统消息
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", 
                "你是一个专业的五子棋分析师，请对以下棋局进行分析。分析内容包括：整体局势评估、关键转折点、双方优劣势、以及可以改进的地方。另外，回答的内容不要用Markdown格式。");
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
            RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();
            
            // 发送请求
            Response response = client.newCall(request).execute();
            
            if (!response.isSuccessful()) {
                throw new RuntimeException("API请求失败: " + response.code() + " " + response.message());
            }
            
            // 解析响应
            String responseBody = response.body().string();
            JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
            
            return jsonResponse
                .getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
                
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "AI分析失败：\n";
            errorMsg += "错误类型：" + e.getClass().getSimpleName() + "\n";
            errorMsg += "错误信息：" + e.getMessage() + "\n\n";
            errorMsg += "可能的解决方法：\n";
            errorMsg += "1. 检查网络连接\n";
            errorMsg += "2. 确认API密钥是否正确\n";
            errorMsg += "3. 检查代理设置（如果使用）\n";
            errorMsg += "4. 尝试使用VPN或其他网络环境\n";
            return errorMsg;
        }
    }
} 