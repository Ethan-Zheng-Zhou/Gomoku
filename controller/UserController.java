package Gomoku.controller;

import Gomoku.model.User;
import java.sql.*;

public class UserController {
    
    public boolean login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); // 注意：实际应用中应该使用密码加密
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // 如果有结果返回true
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password, nickname) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateStats(String username, boolean won) {
        String sql = won ? 
            "UPDATE users SET wins = wins + 1 WHERE username = ?" :
            "UPDATE users SET losses = losses + 1 WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private Connection getConnection() throws SQLException {
        // TODO: 实现数据库连接
        return null;
    }
} 