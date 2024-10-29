import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class NetworkManager {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private boolean isHost;
    private static final int PORT = 5000;
    
    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            isHost = true;
            System.out.println("等待对手连接...");
            clientSocket = serverSocket.accept();
            initializeStreams();
            return true;
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
            return false;
        }
    }
    
    public boolean connectToServer(String hostAddress) {
        try {
            clientSocket = new Socket(hostAddress, PORT);
            isHost = false;
            initializeStreams();
            return true;
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            return false;
        }
    }
    
    private void initializeStreams() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        startMessageListener();
    }
    
    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    messageQueue.put(message);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("接收消息错误: " + e.getMessage());
            }
        }).start();
    }
    
    public void sendMove(int x, int y) {
        out.println("MOVE " + x + " " + y);
    }
    
    public String receiveMove() throws InterruptedException {
        return messageQueue.take();
    }
    
    public boolean isHost() {
        return isHost;
    }
    
    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("关闭连接错误: " + e.getMessage());
        }
    }
} 