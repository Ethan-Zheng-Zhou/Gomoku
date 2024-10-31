package User;
import javax.swing.*; 
import java.awt.*; 
  
 
public class UserChatPad extends JPanel{ 
 public JTextArea chatTextArea=new JTextArea("命令区域",18,20); 
 public UserChatPad(){ 
 setLayout(new BorderLayout()); 
 chatTextArea.setAutoscrolls(true); 
 chatTextArea.setLineWrap(true); 
 add(chatTextArea,BorderLayout.CENTER); 
 }}