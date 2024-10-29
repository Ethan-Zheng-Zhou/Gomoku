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
    private static final int CONNECTION_TIMEOUT = 10000; // 10秒连接超时
    
    public boolean startServer() {
        try {
            // 关闭可能存在的旧连接
            closeConnections();
            
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(CONNECTION_TIMEOUT);  // 设置接受连接的超时时间
            isHost = true;
            System.out.println("等待对手连接...");
            
            try {
                clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(CONNECTION_TIMEOUT);  // 设置数据读取的超时时间
                initializeStreams();
                System.out.println("对手已连接！");
                return true;
            } catch (SocketTimeoutException e) {
                System.err.println("等待连接超时");
                closeConnections();
                return false;
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
            closeConnections();
            return false;
        }
    }
    
    public boolean connectToServer(String hostAddress) {
        try {
            // 关闭可能存在的旧连接
            closeConnections();
            
            // 创建新的Socket连接，设置连接超时
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(hostAddress, PORT), CONNECTION_TIMEOUT);
            clientSocket.setSoTimeout(CONNECTION_TIMEOUT);  // 设置数据读取的超时时间
            
            isHost = false;
            initializeStreams();
            System.out.println("成功连接到服务器！");
            return true;
        } catch (UnknownHostException e) {
            System.err.println("无法解析主机地址: " + e.getMessage());
            return false;
        } catch (SocketTimeoutException e) {
            System.err.println("连接超时");
            return false;
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            return false;
        }
    }
    
    private void initializeStreams() throws IOException {
        try {
            out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8")), true);
            in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            startMessageListener();
        } catch (IOException e) {
            throw new IOException("初始化流失败: " + e.getMessage());
        }
    }
    
    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    messageQueue.put(message);
                }
            } catch (SocketTimeoutException e) {
                System.err.println("读取数据超时");
            } catch (IOException | InterruptedException e) {
                System.err.println("接收消息错误: " + e.getMessage());
            } finally {
                // 确保在连接断开时关闭所有资源
                close();
            }
        }).start();
    }
    
    public void sendMove(int x, int y) {
        if (out != null) {
            out.println("MOVE " + x + " " + y);
            out.flush(); // 确保数据被发送
        }
    }
    
    public String receiveMove() throws InterruptedException {
        return messageQueue.poll(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    
    public boolean isHost() {
        return isHost;
    }
    
    public void close() {
        closeConnections();
    }
    
    private void closeConnections() {
        try {
            if (out != null) {
                out.close();
                out = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            System.err.println("关闭连接错误: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }
} 