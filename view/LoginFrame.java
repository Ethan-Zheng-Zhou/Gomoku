package Gomoku.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import Gomoku.controller.UserController;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private UserController userController;
    
    public LoginFrame() {
        userController = new UserController();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("五子棋登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 用户名
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("用户名:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);
        
        // 密码
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("密码:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        loginButton = new JButton("登录");
        registerButton = new JButton("注册");
        
        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        add(panel);
    }
    
    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (userController.login(username, password)) {
            JOptionPane.showMessageDialog(this, "登录成功！");
            // TODO: 打开主游戏界面
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "用户名或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void register() {
        // TODO: 打开注册界面
        // 可以创建新的RegisterFrame或显示注册对话框
    }
} 