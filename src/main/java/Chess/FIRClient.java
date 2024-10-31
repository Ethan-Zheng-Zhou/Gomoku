package Chess;
import java.awt.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*; 
  
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Chess.FIRClientThread;
import User.FIRPad;
import User.UserChatPad;
import User.UserControlPad;
import User.UserInputPad;
import User.UserListPad;


public class FIRClient extends JFrame implements ActionListener, KeyListener { 
 // 客户端套接口 
 Socket clientSocket; 
 // 数据输入流 
 DataInputStream inputStream; 
 // 数据输出流 
 DataOutputStream outputStream; 
 // 用户名 
 String chessClientName = null; 
 // 主机地址 
 String host = null; 
 // 主机端口 
 int port = 8888; 
 // 是否在聊天 
 boolean isOnChat = false; 
 // 是否在下棋 
 boolean isOnChess = false; 
 // 游戏是否进行中 
 boolean isGameConnected = false; 
 // 是否为游戏创建者 
 boolean isCreator = false; 
 // 是否为游戏加入者 
 boolean isParticipant = false; 
 // 用户列表区 
 UserListPad userListPad = new UserListPad(); 
 // 用户聊天区 
 UserChatPad userChatPad = new UserChatPad(); 
 // 用户操作区 
 UserControlPad userControlPad = new UserControlPad(); 
 // 用户输入区 
 UserInputPad userInputPad = new UserInputPad(); 
 // 下棋区 
 FIRPad firPad = new FIRPad(); 
 // 面板区 
 Panel southPanel = new Panel(); 
 Panel northPanel = new Panel(); 
 Panel centerPanel = new Panel(); 
 Panel eastPanel = new Panel(); 
  
 // 构造方法，创建界面 
 public FIRClient() 
 { 
 super("五子棋 - 联网对战"); 
 setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
  
 // 设置整体背景色和布局 
 getContentPane().setBackground(new Color(245, 245, 245)); 
 setLayout(new BorderLayout(10, 10)); 
  
 // 创建主面板，添加内边距 
 JPanel mainPanel = new JPanel(new BorderLayout(15, 15)); 
 mainPanel.setBackground(new Color(245, 245, 245)); 
 mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); 
  
 // 右侧面板（用户列表和聊天区） 
 JPanel rightPanel = new JPanel(new BorderLayout(0, 10)); 
 rightPanel.setBackground(new Color(245, 245, 245)); 
 rightPanel.setPreferredSize(new Dimension(250, 0)); 
  
 // 美化用户列表 
 userListPad.setBackground(new Color(245, 245, 245)); 
 userListPad.userList.setFont(new Font("微软雅黑", Font.PLAIN, 14)); 
 userListPad.userList.setBackground(Color.WHITE); 
  
 // 美化聊天区 
 userChatPad.setBackground(new Color(245, 245, 245)); 
 userChatPad.chatTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 14)); 
 userChatPad.chatTextArea.setBackground(Color.WHITE); 
 JScrollPane chatScroll = new JScrollPane(userChatPad.chatTextArea); 
 chatScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); 
  
 // 美化输入区 
 userInputPad.setBackground(new Color(245, 245, 245)); 
 userInputPad.contentInputted.setFont(new Font("微软雅黑", Font.PLAIN, 14)); 
 userInputPad.userChoice.setFont(new Font("微软雅黑", Font.PLAIN, 14)); 
  
 // 美化控制面板 
 userControlPad.setBackground(new Color(245, 245, 245)); 
 styleButton(userControlPad.connectButton); 
 styleButton(userControlPad.createButton); 
 styleButton(userControlPad.joinButton); 
 styleButton(userControlPad.cancelButton); 
 styleButton(userControlPad.exitButton); 
  
 // 美化棋盘区域 
 firPad.setBackground(new Color(222, 184, 135)); 
 firPad.statusText.setFont(new Font("微软雅黑", Font.PLAIN, 14)); 
  
 // 组装右侧面板 
 rightPanel.add(createTitledPanel("在线用户", userListPad), BorderLayout.NORTH); 
 rightPanel.add(createTitledPanel("聊天区域", chatScroll), BorderLayout.CENTER); 
 rightPanel.add(createTitledPanel("发送消息", userInputPad), BorderLayout.SOUTH); 
  
 // 组装中央面板 
 JPanel centerPanel = new JPanel(new GridBagLayout());
 centerPanel.setBackground(new Color(245, 245, 245));
 GridBagConstraints gbc = new GridBagConstraints();
 gbc.gridx = 0;
 gbc.gridy = 0;
 gbc.weightx = 1;
 gbc.weighty = 1;
 gbc.anchor = GridBagConstraints.CENTER;
 
 // 创建一个包装面板来容纳棋盘
 JPanel boardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
 boardWrapper.setBackground(new Color(245, 245, 245));
 boardWrapper.add(firPad);
 
 centerPanel.add(boardWrapper, gbc);
  
 // 组装底部控制面板 
 JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); 
 controlPanel.setBackground(new Color(245, 245, 245)); 
 controlPanel.add(userControlPad); 
  
 // 组装主面板 
 mainPanel.add(centerPanel, BorderLayout.CENTER); 
 mainPanel.add(rightPanel, BorderLayout.EAST); 
 mainPanel.add(controlPanel, BorderLayout.SOUTH); 
  
 // 添加主面板到窗口 
 add(mainPanel); 
  
 // 设置窗口属性 
 setSize(1000, 800); 
 setLocationRelativeTo(null); 
 setResizable(false); 
  
 // 添加窗口关闭事件 
 addWindowListener(new WindowAdapter() 
 { 
  public void windowClosing(WindowEvent e) 
  { 
  if (isOnChat) 
  { // 聊天中 
   try
   { // 关闭客户端套接口 
   clientSocket.close(); 
   } 
   catch (Exception ed){} 
  } 
  if (isOnChess || isGameConnected) 
  { // 下棋中 
   try
   { // 关闭下棋端口 
   firPad.chessSocket.close(); 
   } 
   catch (Exception ee){} 
  } 
  dispose(); 
  } 
 }); 
  
 // 初始化按钮状态 
 userControlPad.createButton.setEnabled(false); 
 userControlPad.joinButton.setEnabled(false); 
 userControlPad.cancelButton.setEnabled(false); 
  
 // 添加事件监听 
 userInputPad.contentInputted.addKeyListener(this); 
 userControlPad.connectButton.addActionListener(this); 
 userControlPad.createButton.addActionListener(this); 
 userControlPad.joinButton.addActionListener(this); 
 userControlPad.cancelButton.addActionListener(this); 
 userControlPad.exitButton.addActionListener(this); 
 } 
  
 // 创建带标题的面板 
 private JPanel createTitledPanel(String title, Component component) 
 { 
  JPanel panel = new JPanel(new BorderLayout()); 
  panel.setBackground(new Color(245, 245, 245)); 
  panel.setBorder(BorderFactory.createTitledBorder( 
   BorderFactory.createLineBorder(new Color(200, 200, 200)), 
   title, 
   javax.swing.border.TitledBorder.LEFT, 
   javax.swing.border.TitledBorder.TOP, 
   new Font("微软雅黑", Font.BOLD, 14) 
  )); 
  panel.add(component, BorderLayout.CENTER); 
  return panel; 
 } 
  
 // 统一按钮样式 
 private void styleButton(JButton button) 
 { 
  button.setFont(new Font("微软雅黑", Font.PLAIN, 14)); 
  button.setBackground(new Color(70, 130, 180)); 
  button.setForeground(Color.WHITE); 
  button.setFocusPainted(false); 
  button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); 
  
  // 添加鼠标悬停效果 
  button.addMouseListener(new MouseAdapter() 
  { 
   public void mouseEntered(MouseEvent e) 
   { 
    button.setBackground(new Color(100, 149, 237)); 
   } 
   public void mouseExited(MouseEvent e) 
   { 
    button.setBackground(new Color(70, 130, 180)); 
   } 
  }); 
 } 
  
 // 按指定的IP地址和端口连接到服务器 
 public boolean connectToServer(String serverIP, int serverPort) throws Exception 
 { 
 try
 { 
  // 创建客户端套接口 
  clientSocket = new Socket(serverIP, serverPort); 
  // 创建输入流 
  inputStream = new DataInputStream(clientSocket.getInputStream()); 
  // 创建输出流 
  outputStream = new DataOutputStream(clientSocket.getOutputStream()); 
  // 创建客户端线程 
  FIRClientThread clientthread = new FIRClientThread(this); 
  // 启动线程，等待聊天信息 
  clientthread.start(); 
  isOnChat = true; 
  return true; 
 } 
 catch (IOException ex) 
 { 
  userChatPad.chatTextArea 
   .setText("不能连接!\n"); 
 } 
 return false; 
 } 
  
 // 客户端事件处理 
 public void actionPerformed(ActionEvent e) 
 { 
 if (e.getSource() == userControlPad.connectButton) 
 { // 连接到主机按钮单击事件 
  host = firPad.host = userControlPad.ipInputted.getText(); // 取得主机地址 
  try
  { 
  if (connectToServer(host, port)) 
  { // 成功连接到主机时，设置客户端相应的界面状态 
   userChatPad.chatTextArea.setText(""); 
   userControlPad.connectButton.setEnabled(false); 
   userControlPad.createButton.setEnabled(true); 
   userControlPad.joinButton.setEnabled(true); 
   firPad.statusText.setText("连接成功，请等待!"); 
  } 
  } 
  catch (Exception ei) 
  { 
  userChatPad.chatTextArea 
   .setText("不能连接!\n"); 
  } 
 } 
 if (e.getSource() == userControlPad.exitButton) 
 { // 离开游戏按钮单击事件 
  if (isOnChat) 
  { // 若用户处于聊天状态中 
  try
  { // 关闭客户端套接口 
   clientSocket.close(); 
  } 
  catch (Exception ed){} 
  } 
  if (isOnChess || isGameConnected) 
  { // 若用户处于游戏状态中 
  try
  { // 关闭游戏端口 
   firPad.chessSocket.close(); 
  } 
  catch (Exception ee){} 
  }
  dispose();
  try {
      Class<?> startScreenClass = Class.forName("StartScreen");
      JFrame startScreen = (JFrame) startScreenClass.getDeclaredConstructor().newInstance();
      startScreen.setVisible(true);
  } catch (Exception ex) {
      ex.printStackTrace();
  }
 } 
 if (e.getSource() == userControlPad.joinButton) 
 { // 加入游戏按钮单击事件 
  String selectedUser = (String)userListPad.userList.getSelectedItem(); // 取得要加入的游戏 
  if (selectedUser == null || selectedUser.startsWith("[inchess]") || 
   selectedUser.equals(chessClientName)) 
  { // 若未选中要加入的用户，或选中的用户已经在游戏，则给出提示信息 
  firPad.statusText.setText("必须选择一个用户!"); 
  } 
  else
  { // 执行加入游戏的操作 
  try
  { 
   if (!isGameConnected) 
   { // 若游戏套接口未连接 
   if (firPad.connectServer(firPad.host, firPad.port)) 
   { // 若连接到主机成功 
    isGameConnected = true; 
    isOnChess = true; 
    isParticipant = true; 
    userControlPad.createButton.setEnabled(false); 
    userControlPad.joinButton.setEnabled(false); 
    userControlPad.cancelButton.setEnabled(true); 
    firPad.firThread.sendMessage("/joingame "
     + (String)userListPad.userList.getSelectedItem() + " "
     + chessClientName); 
   } 
   } 
   else
   { // 若游戏端口连接中 
   isOnChess = true; 
   isParticipant = true; 
   userControlPad.createButton.setEnabled(false); 
   userControlPad.joinButton.setEnabled(false); 
   userControlPad.cancelButton.setEnabled(true); 
   firPad.firThread.sendMessage("/joingame "
    + (String)userListPad.userList.getSelectedItem() + " "
    + chessClientName); 
   } 
  } 
  catch (Exception ee) 
  { 
   isGameConnected = false; 
   isOnChess = false; 
   isParticipant = false; 
   userControlPad.createButton.setEnabled(true); 
   userControlPad.joinButton.setEnabled(true); 
   userControlPad.cancelButton.setEnabled(false); 
   userChatPad.chatTextArea 
    .setText("不能连接: \n" + ee); 
  } 
  } 
 } 
 if (e.getSource() == userControlPad.createButton) 
 { // 创建游戏按钮单击事件 
  try
  { 
  if (!isGameConnected) 
  { // 若游戏端口未连接 
   if (firPad.connectServer(firPad.host, firPad.port)) 
   { // 若连接到主机成功 
   isGameConnected = true; 
   isOnChess = true; 
   isCreator = true; 
   userControlPad.createButton.setEnabled(false); 
   userControlPad.joinButton.setEnabled(false); 
   userControlPad.cancelButton.setEnabled(true); 
   firPad.firThread.sendMessage("/creatgame "
    + "[inchess]" + chessClientName); 
   } 
  } 
  else
  { // 若游戏端口连接中 
   isOnChess = true; 
   isCreator = true; 
   userControlPad.createButton.setEnabled(false); 
   userControlPad.joinButton.setEnabled(false); 
   userControlPad.cancelButton.setEnabled(true); 
   firPad.firThread.sendMessage("/creatgame "
    + "[inchess]" + chessClientName); 
  } 
  } 
  catch (Exception ec) 
  { 
  isGameConnected = false; 
  isOnChess = false; 
  isCreator = false; 
  userControlPad.createButton.setEnabled(true); 
  userControlPad.joinButton.setEnabled(true); 
  userControlPad.cancelButton.setEnabled(false); 
  ec.printStackTrace(); 
  userChatPad.chatTextArea.setText("不能连接: \n"
   + ec); 
  } 
 } 
 if (e.getSource() == userControlPad.cancelButton) 
 { // 退出游戏按钮单击事件 
  if (isOnChess) 
  { // 游戏中 
  firPad.firThread.sendMessage("/giveup " + chessClientName); 
  firPad.setVicStatus(-1 * firPad.chessColor); 
  userControlPad.createButton.setEnabled(true); 
  userControlPad.joinButton.setEnabled(true); 
  userControlPad.cancelButton.setEnabled(false); 
  firPad.statusText.setText("请创建或加入游戏!"); 
  } 
  if (!isOnChess) 
  { // 非游戏中 
  userControlPad.createButton.setEnabled(true); 
  userControlPad.joinButton.setEnabled(true); 
  userControlPad.cancelButton.setEnabled(false); 
  firPad.statusText.setText("请创建或加入游戏!"); 
  } 
  isParticipant = isCreator = false; 
 } 
 } 
  
 public void keyPressed(KeyEvent e) 
 { 
 TextField inputwords = (TextField) e.getSource(); 
 if (e.getKeyCode() == KeyEvent.VK_ENTER) 
 { // 处理车按键事件 
  if (userInputPad.userChoice.getSelectedItem().equals("所有用户")) 
  { // 给所有人发信息 
  try
  { 
   // 发送信息 
   outputStream.writeUTF(inputwords.getText()); 
   inputwords.setText(""); 
  } 
  catch (Exception ea) 
  { 
   userChatPad.chatTextArea 
    .setText("不能连接到服务器!\n"); 
   userListPad.userList.removeAll(); 
   userInputPad.userChoice.removeAll(); 
   inputwords.setText(""); 
   userControlPad.connectButton.setEnabled(true); 
  } 
  } 
  else
  { // 给指定人发信息 
  try
  { 
   outputStream.writeUTF("/" + userInputPad.userChoice.getSelectedItem() 
    + " " + inputwords.getText()); 
   inputwords.setText(""); 
  } 
  catch (Exception ea) 
  { 
   userChatPad.chatTextArea 
    .setText("不能连接到服务器!\n"); 
   userListPad.userList.removeAll(); 
   userInputPad.userChoice.removeAll(); 
   inputwords.setText(""); 
   userControlPad.connectButton.setEnabled(true); 
  } 
  } 
 } 
 } 
  
 public void keyTyped(KeyEvent e) {} 
 public void keyReleased(KeyEvent e) {} 
  
 public static void main(String args[]) 
 { 
 FIRClient chessClient = new FIRClient(); 
 } 
} 