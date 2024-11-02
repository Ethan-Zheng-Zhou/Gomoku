package Chess;
import java.io.*; 
import java.net.*; 
import java.awt.*; 
import java.util.*; 
import java.awt.event.*; 
  
import javax.swing.JButton; 

public class Server extends Frame implements ActionListener{ 
 JButton clearMsgButton = new JButton("清空列表"); 
 JButton serverStatusButton = new JButton("服务器状态"); 
 JButton closeServerButton = new JButton("关闭服务器"); 
 Panel buttonPanel = new Panel(); 
 ServerMsgPanel serverMsgPanel = new ServerMsgPanel(); 
 ServerSocket serverSocket; 
 Hashtable clientDataHash = new Hashtable(50); //将客户端套接口和输出流绑定 
 Hashtable clientNameHash = new Hashtable(50); //将客户端套接口和客户名绑定 
 Hashtable chessPeerHash = new Hashtable(50); //将游戏创建者和游戏加入者绑定 
  
 public Server() 
 { 
 super("Java 五子棋服务器"); 
 setBackground(Color.LIGHT_GRAY); 
 buttonPanel.setLayout(new FlowLayout()); 
 clearMsgButton.setSize(60, 25); 
 buttonPanel.add(clearMsgButton); 
 clearMsgButton.addActionListener(this); 
 serverStatusButton.setSize(75, 25); 
 buttonPanel.add(serverStatusButton); 
 serverStatusButton.addActionListener(this); 
 closeServerButton.setSize(75, 25); 
 buttonPanel.add(closeServerButton); 
 closeServerButton.addActionListener(this); 
 add(serverMsgPanel, BorderLayout.CENTER); 
 add(buttonPanel, BorderLayout.SOUTH); 
  
 addWindowListener(new WindowAdapter() 
 { 
  public void windowClosing(WindowEvent e) 
  { 
  System.exit(0); 
  } 
 }); 
 pack(); 
 setVisible(true); 
 setSize(400, 300); 
 setResizable(false); 
 validate(); 
  
 try
 { 
  createServer(8888, serverMsgPanel); 
 } 
 catch (Exception e) 
 { 
  e.printStackTrace(); 
 } 
 } 
  
 // 用指定端口和面板创建服务器 
 public void createServer(int port, ServerMsgPanel serverMsgPanel) throws IOException 
 { 
 Socket clientSocket; // 客户端套接口 
 long clientAccessNumber = 1; // 连接到主机的客户数量 
 this.serverMsgPanel = serverMsgPanel; // 设定当前主机 
 try
 { 
  serverSocket = new ServerSocket(port); 
  serverMsgPanel.msgTextArea.setText("服务器启动于:"
   + InetAddress.getLocalHost() + ":" //djr 
   + serverSocket.getLocalPort() + "\n"); 
  while (true) 
  { 
  // 监听客户端套接口的信息 
  clientSocket = serverSocket.accept(); 
  serverMsgPanel.msgTextArea.append("已连接:" + clientSocket + "\n"); 
  // 建立客户端输出流 
  String userName = "user";
   
  DataOutputStream outputData = new DataOutputStream(clientSocket 
   .getOutputStream()); 
  // 将客户端套接口和输出流绑定 
  clientDataHash.put(clientSocket, outputData); 
  // 将客户端套接口和客户名绑定 
  clientNameHash .put(clientSocket, (userName + clientAccessNumber++)); 
  // 创建并运行服务器端线程 
  ServerThread thread = new ServerThread(clientSocket, 
   clientDataHash, clientNameHash, chessPeerHash, serverMsgPanel); 
  thread.start(); 
  } 
 } 
 catch (IOException ex) 
 { 
  ex.printStackTrace(); 
 } 
 } 
  
 public void actionPerformed(ActionEvent e) 
 { 
 if (e.getSource() == clearMsgButton) 
 { // 清空服务器信息 
  serverMsgPanel.msgTextArea.setText(""); 
 } 
 if (e.getSource() == serverStatusButton) 
 { // 显示服务器信息 
  try
  { 
  serverMsgPanel.msgTextArea.append("服务器信息:"
   + InetAddress.getLocalHost() + ":"
   + serverSocket.getLocalPort() + "\n"); 
  } 
  catch (Exception ee) 
  { 
  ee.printStackTrace(); 
  } 
 } 
 if (e.getSource() == closeServerButton) 
 { // 关闭服务器 
  System.exit(0); 
 } 
 } 
  
 public static void main(String args[]) 
 { 
 Server firServer = new Server(); 
 } 
} 