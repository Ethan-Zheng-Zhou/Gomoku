import javax.swing.*;
import java.awt.*;

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
            GomokuGame game = new GomokuGame("HOST");
            if (game.getNetworkManager() != null) {
                dispose();
                GameBoard gameBoard = new GameBoard(game);
                gameBoard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "创建房间失败");
            }
        });
        
        joinButton.addActionListener(e -> {
            String ip = ipField.getText().trim();
            if (ip.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入IP地址");
                return;
            }
            
            GomokuGame game = new GomokuGame("CLIENT");
            NetworkManager networkManager = game.getNetworkManager();
            
            if (networkManager != null && networkManager.connectToServer(ip)) {
                dispose();
                GameBoard gameBoard = new GameBoard(game);
                gameBoard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "连接失败");
            }
        });
        
        add(panel);
    }
} 