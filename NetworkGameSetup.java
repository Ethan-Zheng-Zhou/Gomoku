import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkGameSetup extends JFrame {
    private JTextField ipField;
    private JButton hostButton;
    private JButton joinButton;
    
    public NetworkGameSetup() {
        setTitle("网络对战设置");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建IP地址面板
        JPanel ipPanel = new JPanel(new BorderLayout(5, 5));
        ipPanel.add(new JLabel("对方IP地址:"), BorderLayout.WEST);
        
        // 添加帮助按钮
        JButton helpButton = new JButton("?");
        helpButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        helpButton.setPreferredSize(new Dimension(25, 25));
        helpButton.addActionListener(e -> showIPHelp());
        ipPanel.add(helpButton, BorderLayout.EAST);
        
        // 创建输入框和按钮
        ipField = new JTextField();
        hostButton = new JButton("创建房间");
        joinButton = new JButton("加入房间");
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(hostButton);
        buttonPanel.add(joinButton);
        
        // 添加组件到主面板
        panel.add(ipPanel);
        panel.add(ipField);
        panel.add(buttonPanel);
        
        hostButton.addActionListener(e -> {
            // 禁用按钮，显示等待状态
            hostButton.setEnabled(false);
            joinButton.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // 在新线程中启动服务器
            new Thread(() -> {
                final GomokuGame game = new GomokuGame("HOST");
                final AtomicBoolean started = new AtomicBoolean(false);
                final NetworkManager networkManager = game.getNetworkManager();
                
                if (networkManager != null) {
                    started.set(networkManager.startServer());
                }
                
                // 在 EDT 中更新 UI
                SwingUtilities.invokeLater(() -> {
                    if (started.get()) {
                        // 显示成功信息和端口号
                        JOptionPane.showMessageDialog(this,
                            "房间创建成功，使用端口: " + networkManager.getCurrentPort(),
                            "创建成功",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        new GameBoard(game).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "创建房间失败，所有端口(5000-5010)都被占用\n请稍后重试或联系管理员",
                            "创建失败",
                            JOptionPane.ERROR_MESSAGE);
                            
                        // 重新启用按钮
                        hostButton.setEnabled(true);
                        joinButton.setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                });
            }).start();
        });
        
        joinButton.addActionListener(e -> {
            String ip = ipField.getText().trim();
            if (ip.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入IP地址");
                return;
            }
            
            // 禁用按钮，显示连接中状态
            joinButton.setEnabled(false);
            hostButton.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // 在新线程中进行连接
            new Thread(() -> {
                final GomokuGame game = new GomokuGame("CLIENT");
                final NetworkManager networkManager = game.getNetworkManager();
                final AtomicBoolean connected = new AtomicBoolean(false);
                
                if (networkManager != null) {
                    connected.set(networkManager.connectToServer(ip));
                }
                
                // 在 EDT 中更新 UI
                SwingUtilities.invokeLater(() -> {
                    if (connected.get()) {
                        dispose();
                        new GameBoard(game).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "连接失败，请检查IP地址是否正确，以及对方是否已创建房间",
                            "连接失败",
                            JOptionPane.ERROR_MESSAGE);
                            
                        // 重新启用按钮
                        joinButton.setEnabled(true);
                        hostButton.setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                });
            }).start();
        });
        
        add(panel);
    }
    
    private void showIPHelp() {
        String helpMessage = 
            "连接指南：\n\n" +
            "1. 在同一个局域网内（比如同一个WiFi下）：\n" +
            "   - 使用局域网IP（在命令行输入ipconfig查看）\n" +
            "   - 通常是192.168.x.x格式\n\n" +
            "2. 使用内网穿透工具连接：\n" +
            "   a) 创建房间的玩家：\n" +
            "      - 下载并安装ngrok (ngrok.com)\n" +
            "      - 运行命令：ngrok tcp 5000\n" +
            "      - 将显示的域名和端口告诉对方\n" +
            "      例如：若显示 tcp://0.tcp.ngrok.io:12345\n" +
            "      对方应输入：0.tcp.ngrok.io\n\n" +
            "   b) 加入房间的玩家：\n" +
            "      - 直接输入对方提供的域名\n" +
            "      - 不需要输入端口号\n\n" +
            "使用ngrok的步骤：\n" +
            "1. 访问 ngrok.com 注册账号\n" +
            "2. 下载并安装ngrok\n" +
            "3. 在命令行运行：ngrok tcp 5000\n" +
            "4. 将显示的域名告诉对方\n\n" +
            "注意事项：\n" +
            "- 确保防火墙允许连接\n" +
            "- 免费版ngrok每次启动域名会改变\n" +
            "- 连接成功前请保持ngrok运行";

        JTextArea textArea = new JTextArea(helpMessage);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        textArea.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "连接帮助", JOptionPane.INFORMATION_MESSAGE);
    }
} 