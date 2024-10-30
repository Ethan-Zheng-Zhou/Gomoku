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
    private static final int START_PORT = 5000;  // 起始端口
    private static final int MAX_PORT = 5010;    // 最大尝试端口
    private static final int CONNECTION_TIMEOUT = 120000; // 10秒连接超时
    private int currentPort = START_PORT;
    
    public boolean startServer() {
        try {
            // 关闭可能存在的旧连接
            closeConnections();
            
            // 尝试不同的端口
            for (int port = START_PORT; port <= MAX_PORT; port++) {
                try {
                    serverSocket = new ServerSocket(port);
                    currentPort = port;
                    System.out.println("成功在端口 " + port + " 创建服务器");
                    break;
                } catch (BindException e) {
                    if (port == MAX_PORT) {
                        throw new IOException("所有端口都被占用");
                    }
                    continue;
                }
            }
            
            serverSocket.setSoTimeout(CONNECTION_TIMEOUT);
            isHost = true;
            System.out.println("等待对手连接...");
            
            try {
                clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
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
            
            // 解析主机地址（支持域名和IP）
            InetAddress inetAddress = InetAddress.getByName(hostAddress);
            System.out.println("正在连接到地址: " + inetAddress.getHostAddress());
            
            // 尝试不同的端口
            for (int port = START_PORT; port <= MAX_PORT; port++) {
                try {
                    clientSocket = new Socket();
                    // 使用解析后的地址
                    clientSocket.connect(new InetSocketAddress(inetAddress, port), CONNECTION_TIMEOUT);
                    currentPort = port;
                    System.out.println("成功连接到端口 " + port);
                    break;
                } catch (ConnectException | SocketTimeoutException e) {
                    if (port == MAX_PORT) {
                        throw new IOException("无法连接到任何可用端口");
                    }
                    continue;
                }
            }
            
            clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
            isHost = false;
            initializeStreams();
            System.out.println("成功连接到服务器！");
            return true;
        } catch (UnknownHostException e) {
            System.err.println("无法解析主机地址: " + e.getMessage());
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
    
    public int getCurrentPort() {
        return currentPort;
    }
} 