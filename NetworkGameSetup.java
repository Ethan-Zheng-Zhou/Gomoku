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
        
        ipField = new JTextField();
        hostButton = new JButton("创建房间");
        joinButton = new JButton("加入房间");
        
        panel.add(new JLabel("对方IP地址:"));
        panel.add(ipField);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(hostButton);
        buttonPanel.add(joinButton);
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
                
                if (game.getNetworkManager() != null) {
                    started.set(game.getNetworkManager().startServer());
                }
                
                // 在 EDT 中更新 UI
                SwingUtilities.invokeLater(() -> {
                    if (started.get()) {
                        dispose();
                        new GameBoard(game).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "创建房间失败，请检查端口是否被占用",
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
} 