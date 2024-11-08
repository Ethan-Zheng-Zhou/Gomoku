import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 游戏数据库类，负责游戏记录的保存、加载和删除
// 提供静态方法来操作数据库
// 使用DatabaseConfig类来获取数据库连接
public class GameDatabase {
    public static int saveGame(String gameType, String winner, int duration) {
        String sql = "INSERT INTO game_records (game_type, winner, duration) VALUES (?, ?, ?)";

        // 使用try-with-resources语句来确保Connection和PreparedStatement自动关闭
        // 使用Statement.RETURN_GENERATED_KEYS选项来获取自动生成的键值，即游戏ID
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, gameType);
            pstmt.setString(2, winner);
            pstmt.setInt(3, duration);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                System.err.println("创建游戏记录失败，没有行被插入。");
                return -1;
            }

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int gameId = rs.getInt(1);
                System.out.println("成功创建游戏记录，ID: " + gameId);
                return gameId;
            } else {
                System.err.println("创建游戏记录失败，无法获取ID。");
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("保存游戏记录时发生SQL错误: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public static void saveMoves(int gameId, List<Move> moves) {
        String sql = "INSERT INTO moves (game_id, move_number, x, y, is_black) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                pstmt.setInt(1, gameId);
                pstmt.setInt(2, i + 1);
                pstmt.setInt(3, move.getX());
                pstmt.setInt(4, move.getY());
                pstmt.setBoolean(5, move.isBlack());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<GameRecord> getGameList() {
        List<GameRecord> games = new ArrayList<>();
        String sql = "SELECT * FROM game_records ORDER BY start_time DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int count = 0;
            while (rs.next()) {
                GameRecord game = new GameRecord(
                    rs.getInt("game_id"),
                    rs.getTimestamp("start_time"),
                    rs.getString("game_type"),
                    rs.getString("winner"),
                    rs.getInt("duration")
                );
                games.add(game);
                count++;
            }
            System.out.println("从数据库加载了 " + count + " 条游戏记录");
        } catch (SQLException e) {
            System.err.println("获取游戏列表时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return games;
    }

    public static List<Move> loadMoves(int gameId) {
        List<Move> moves = new ArrayList<>();
        String sql = "SELECT * FROM moves WHERE game_id = ? ORDER BY move_number";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, gameId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Move move = new Move(
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getBoolean("is_black")
                );
                moves.add(move);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moves;
    }

    public static boolean deleteGame(int gameId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 开始事务
            conn.setAutoCommit(false);
            try {
                // 先删除移动记录
                String deleteMovesSql = "DELETE FROM moves WHERE game_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteMovesSql)) {
                    pstmt.setInt(1, gameId);
                    pstmt.executeUpdate();
                }
                
                // 再删除游戏记录
                String deleteGameSql = "DELETE FROM game_records WHERE game_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteGameSql)) {
                    pstmt.setInt(1, gameId);
                    pstmt.executeUpdate();
                }
                
                // 提交事务
                conn.commit();
                return true;
            } catch (SQLException e) {
                // 发生错误时回滚事务
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
