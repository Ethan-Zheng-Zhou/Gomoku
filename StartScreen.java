import javax.swing.*;
import java.awt.*;

public class StartScreen extends JFrame {
    public StartScreen() {
        // 基础设置
        setTitle("五子棋");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // 添加标题
        JLabel titleLabel = new JLabel("五子棋", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        
        // 创建按钮
        JButton pvpButton = new JButton("玩家对战");
        JButton aiButton = new JButton("AI对战");
        
        // 添加按钮事件
        pvpButton.addActionListener(e -> {
            new GameBoard(false);
            dispose();
        });
        
        aiButton.addActionListener(e -> {
            new GameBoard(true);
            dispose();
        });
        
        // 添加组件到面板
        panel.add(titleLabel);
        panel.add(pvpButton);
        panel.add(aiButton);
        
        add(panel);
    }
}
