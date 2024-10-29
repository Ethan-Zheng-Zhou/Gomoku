import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class StartScreen extends JFrame {
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;
    
    public StartScreen() {
        setTitle("五子棋");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 设置窗口背景色
        getContentPane().setBackground(new Color(245, 245, 245));
        
        // 创建主面板，使用BorderLayout布局
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        
        // 创建顶部标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(245, 245, 245));
        
        // 添加校徽
        ImageIcon logoIcon = new ImageIcon("resources/njupt_logo.jpg");
        Image scaledImage = logoIcon.getImage().getScaledInstance(400, 80, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 添加标题
        JLabel titleLabel = new JLabel("五子棋");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 将校徽和标题添加到顶部面板
        titlePanel.add(logoLabel);
        titlePanel.add(Box.createVerticalStrut(20));
        titlePanel.add(titleLabel);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
        // 创建按钮
        String[] buttonTexts = {"AI对战", "双人对战", "网络对战", "对局复盘", "退出游戏"};
        Dimension buttonSize = new Dimension(250, 45);
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 18);
        
        for (String text : buttonTexts) {
            JButton button = new JButton(text);
            button.setMaximumSize(buttonSize);
            button.setPreferredSize(buttonSize);
            button.setFont(buttonFont);
            button.setFocusPainted(false);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // 添加鼠标悬停效果
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(100, 149, 237));
                }
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(70, 130, 180));
                }
            });
            
            buttonPanel.add(button);
            buttonPanel.add(Box.createVerticalStrut(15));
        }
        
        // 设置按钮事件
        ((JButton)buttonPanel.getComponent(0)).addActionListener(e -> {
            dispose();
            new GameBoard(new GomokuGame("AI")).setVisible(true);
        });
        
        ((JButton)buttonPanel.getComponent(2)).addActionListener(e -> {
            dispose();
            new GameBoard(new GomokuGame("LOCAL")).setVisible(true);
        });
        
        ((JButton)buttonPanel.getComponent(4)).addActionListener(e -> {
            new NetworkGameSetup().setVisible(true);
        });
        
        ((JButton)buttonPanel.getComponent(6)).addActionListener(e -> {
            new ReplayScreen().setVisible(true);
        });
        
        ((JButton)buttonPanel.getComponent(8)).addActionListener(e -> System.exit(0));
        
        // 创建底部版权面板
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(new Color(245, 245, 245));
        
        // 添加分隔线
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(600, 1));
        separator.setForeground(new Color(200, 200, 200));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 创建版权信息
        JLabel copyrightLabel = new JLabel("© 2024 南京邮电大学计算机学院");
        copyrightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        copyrightLabel.setForeground(new Color(102, 102, 102));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 创建作者信息
        JLabel authorsLabel = new JLabel("作者：周正（UI/人机/数据库） | 周陇（联网对战）");
        authorsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        authorsLabel.setForeground(new Color(102, 102, 102));
        authorsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 创建GitHub链接面板
        JPanel githubPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        githubPanel.setBackground(new Color(245, 245, 245));
        githubPanel.setMaximumSize(new Dimension(600, 30));  // 限制面板最大宽度
        githubPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 创建GitHub链接标签
        JLabel githubLabel = new JLabel("<html><u>GitHub项目主页</u></html>");
        githubLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        githubLabel.setForeground(new Color(51, 122, 183));
        githubLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        githubLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/Ethan-Zheng-Zhou/Gomoku"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        // 将GitHub链接添加到其面板中
        githubPanel.add(githubLabel);
        
        // 将组件添加到底部面板
        bottomPanel.add(separator);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(copyrightLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(authorsLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(githubPanel);  // 使用githubPanel代替直接添加githubLabel
        bottomPanel.add(Box.createVerticalStrut(10));
        
        // 将所有面板添加到主面板
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // 将主面板添加到窗口
        add(mainPanel);
    }
}
