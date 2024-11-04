package User;
import javax.swing.*; 
import java.awt.*; 
  
public class UserControlPad extends JPanel { 
    public JLabel ipLabel = new JLabel("服务器IP:", JLabel.LEFT); 
    public JTextField ipInputted = new JTextField("localhost", 15);
    // public JTextField nameInputted = new JTextField("房间名", 15); 
    public JButton connectButton = new JButton("连接服务器"); 
    public JButton createButton = new JButton("创建游戏"); 
    public JButton joinButton = new JButton("加入游戏"); 
    public JButton cancelButton = new JButton("放弃游戏"); 
    public JButton exitButton = new JButton("返回主菜单");
    
    public UserControlPad() { 
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5)); 
        setBackground(new Color(245, 245, 245)); 
        
        // 设置字体
        Font font = new Font("微软雅黑", Font.PLAIN, 14);
        ipLabel.setFont(font);
        ipInputted.setFont(font);
        
        // IP输入面板
        JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ipPanel.setBackground(new Color(245, 245, 245));
        ipPanel.add(ipLabel);
        ipPanel.add(ipInputted);
        
        // 添加组件
        add(ipPanel);
        // add(nameInputted);
        add(connectButton);
        add(createButton);
        add(joinButton);
        add(cancelButton);
        add(exitButton);
    }
} 