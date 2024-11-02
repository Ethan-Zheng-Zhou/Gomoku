package Chess;
import java.io.IOException; 
import java.util.StringTokenizer; 
 
  


public class ClientThread extends Thread{ 
 public Client firClient; 
  
 public ClientThread(Client firClient) 
 { 
 this.firClient = firClient; 
 } 
  
 public void dealWithMsg(String msgReceived) 
 { 
 if (msgReceived.startsWith("/userlist ")) 
 { // 若取得的信息为用户列表 
  StringTokenizer userToken = new StringTokenizer(msgReceived, " "); 
  int userNumber = 0; 
  // 清空客户端用户列表 
  firClient.userListPad.userList.removeAll(); 
while (userToken.hasMoreTokens()) { 
    String user = userToken.nextToken(" "); // 取得用户信息 
    if (userNumber > 0 && !user.startsWith("[inchess]")) { 
        firClient.userListPad.userList.add(user);//对战中用户则不会显示在列表中 
        // 将用户信息添加到房间列表中 
       } 
    userNumber++; 
} 
 } 
 else if (msgReceived.startsWith("/yourname ")) 
 { // 收到的信息为用户名时 
  firClient.chessClientName = msgReceived.substring(10); // 取得用户本名 
  firClient.setTitle("Java 五子棋客户端 " + "用户名:"
   + firClient.chessClientName);  
 } 
 else if (msgReceived.equals("/reject")) 
 { // 收到的信息为拒绝用户时 
  try
  { 
  firClient.firPad.statusText.setText("不能加入游戏!"); 
  firClient.userControlPad.cancelButton.setEnabled(false); 
  firClient.userControlPad.joinButton.setEnabled(true); 
  firClient.userControlPad.createButton.setEnabled(true); 
  } 
  catch (Exception ef) 
  { 
  firClient.userChatPad.chatTextArea 
   .setText("Cannot close!"); 
  } 
  firClient.userControlPad.joinButton.setEnabled(true); 
 } 
 else if (msgReceived.startsWith("/peer ")) 
 { // 收到信息为游戏中的等待时 
  firClient.firPad.chessPeerName = msgReceived.substring(6); 
  if (firClient.isCreator) 
  { // 若用户为游戏建立者 
  firClient.firPad.chessColor = 1; // 设定其为黑棋先行 
  firClient.firPad.isMouseEnabled = true; 
  firClient.firPad.statusText.setText("黑方下..."); 
  } 
  else if (firClient.isParticipant) 
  { // 若用户为游戏加入者 
  firClient.firPad.chessColor = -1; // 设定其为白棋后性 
  firClient.firPad.statusText.setText("游戏加入，等待对手."); 
  } 
 } 
 else if (msgReceived.equals("/youwin")) 
 { // 收到信息为胜利信息 
  firClient.isOnChess = false; 
  firClient.firPad.setVicStatus(firClient.firPad.chessColor); 
  firClient.firPad.statusText.setText("对手退出"); 
  firClient.firPad.isMouseEnabled = false; 
 } 
 else if (msgReceived.equals("/OK")) 
 { // 收到信息为成功创建游戏 
  firClient.firPad.statusText.setText("游戏创建等待对手"); 
 } 
  
 else if (msgReceived.equals("/error")) 
 { // 收到信息错误 
  firClient.userChatPad.chatTextArea.append("错误，退出程序.\n"); 
 } 
 else
 { 
  firClient.userChatPad.chatTextArea.append(msgReceived + "\n"); 
  firClient.userChatPad.chatTextArea.setCaretPosition( 
   firClient.userChatPad.chatTextArea.getText().length()); 
 } 
 } 
  
 public void run() 
 { 
 String message = ""; 
 try
 { 
  while (true) 
  { 
  message = firClient.inputStream.readUTF(); 
  dealWithMsg(message); 
  } 
 } 
 catch (IOException es){} 
 } 
} 