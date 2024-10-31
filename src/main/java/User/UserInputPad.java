package User;
import javax.swing.*; 
import java.awt.*; 
  

public class UserInputPad extends JPanel{ 
 public JTextField contentInputted = new JTextField("",26); 
 public JComboBox userChoice = new JComboBox(); 
  
 public UserInputPad(){ 
 setLayout(new FlowLayout(FlowLayout.LEFT)); 
 for(int i=0;i<50;i++){ 
  userChoice.addItem(i+"."+"无用户"); 
 } 
 userChoice.setSize(60,24); 
 add(userChoice); 
 add(contentInputted); 
 } 
} 