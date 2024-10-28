import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.border.EmptyBorder;
import java.io.File;

public class StartScreen extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color TITLE_COLOR = new Color(47, 79, 79);
    private static final Color COPYRIGHT_COLOR = new Color(128, 128, 128);
    private static final Color LINK_COLOR = new Color(51, 122, 183);
    
    public StartScreen() {
        setTitle("基于Java和Swing实现的五子棋小游戏");
        setSize(800, 600);  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        
        // 修改添加校徽的部分
        // 添加校徽
        try {
            // 使用类路径加载图片
            java.net.URL imageUrl = getClass().getResource("/njupt_logo.jpg");
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(400, 80, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                JLabel logoLabel = new JLabel(scaledIcon);
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                mainPanel.add(logoLabel);
                mainPanel.add(Box.createVerticalStrut(20));
            } else {
                System.err.println("无法找到校徽图片文件");
            }
        } catch (Exception e) {
            System.err.println("加载校徽失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 创建标题
        JLabel titleLabel = new JLabel("基于Java和Swing实现的五子棋小游戏");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 创建按钮
        JButton pvpButton = createStyledButton("玩家对战");
        JButton aiButton = createStyledButton("AI对战");
        JButton replayButton = createStyledButton("棋谱回放");
        
        // 创建版权信息面板
        JPanel copyrightPanel = new JPanel();
        copyrightPanel.setLayout(new BoxLayout(copyrightPanel, BoxLayout.Y_AXIS));
        copyrightPanel.setBackground(BACKGROUND_COLOR);
        
        // 创建版权信息标签
        JLabel copyrightLabel1 = new JLabel("版权所有 © 2024");
        JLabel copyrightLabel2 = new JLabel("周正（UI、人机对战）");
        JLabel copyrightLabel3 = new JLabel("周陇（联网对战）");
        
        // 设置版权信息样式
        Font copyrightFont = new Font("微软雅黑", Font.PLAIN, 12);
        copyrightLabel1.setFont(copyrightFont);
        copyrightLabel2.setFont(copyrightFont);
        copyrightLabel3.setFont(copyrightFont);
        
        copyrightLabel1.setForeground(COPYRIGHT_COLOR);
        copyrightLabel2.setForeground(COPYRIGHT_COLOR);
        copyrightLabel3.setForeground(COPYRIGHT_COLOR);
        
        copyrightLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        copyrightLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        copyrightLabel3.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 添加版权信息到版权面板
        copyrightPanel.add(copyrightLabel1);
        copyrightPanel.add(Box.createVerticalStrut(5));
        copyrightPanel.add(copyrightLabel2);
        copyrightPanel.add(Box.createVerticalStrut(5));
        copyrightPanel.add(copyrightLabel3);
        
        // 添加按钮事件
        pvpButton.addActionListener(e -> {
            new GameBoard(false);
            dispose();
        });
        
        aiButton.addActionListener(e -> {
            new GameBoard(true);
            dispose();
        });
        
        replayButton.addActionListener(e -> {
            new ReplayScreen();
            dispose();
        });
        
        // 创建GitHub链接标签
        JLabel githubLabel = new JLabel("GitHub主页: https://github.com/Ethan-Zheng-Zhou/Gomoku");
        githubLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        githubLabel.setForeground(LINK_COLOR);
        githubLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        githubLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加点击事件
        githubLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/Ethan-Zheng-Zhou/Gomoku"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                githubLabel.setText("<html><u>GitHub: https://github.com/Ethan-Zheng-Zhou/Gomoku</u></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                githubLabel.setText("GitHub: https://github.com/Ethan-Zheng-Zhou/Gomoku");
            }
        });
        
        // 添加组件到主面板
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(pvpButton);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(aiButton);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(replayButton);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(copyrightPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(githubLabel);  // 添加GitHub链接
        
        add(mainPanel);
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(BUTTON_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(BUTTON_COLOR.brighter());
                } else {
                    g2.setColor(BUTTON_COLOR);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        return button;
    }
}
