import javax.swing.SwingUtilities;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class GomokuGame {
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    public static final int EMPTY = 0;
    
    private int[][] board;
    private int currentPlayer;
    private List<Move> moves;
    private static final int BOARD_SIZE = 15;
    private final String gameType;
    private final long startTime;
    private boolean isGameEnded = false;
    
    public GomokuGame(boolean isAIGame) {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = BLACK;
        moves = new ArrayList<>();
        initializeBoard();
        gameType = isAIGame ? "AI对战" : "玩家对战";
        startTime = System.currentTimeMillis();
        
        // 测试数据库连接
        try {
            Connection conn = DatabaseConfig.getConnection();
            System.out.println("数据库连接成功！");
            conn.close();
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartScreen screen = new StartScreen();
            screen.setVisible(true);
        });
    }

    public boolean makeMove(int x, int y) {
        if (isValidMove(x, y)) {
            board[x][y] = currentPlayer;
            moves.add(new Move(x, y, currentPlayer == BLACK));
            
            // 检查是否获胜或平局
            if (checkWin(x, y) || isBoardFull()) {
                return true;
            }
            
            // 如果游戏未结束，切换玩家
            currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
            return true;
        }
        return false;
    }
    
    public boolean isValidMove(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && board[x][y] == EMPTY;
    }
    
    public boolean isGameOver() {
        // 检查是否有玩家获胜
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != EMPTY) {
                    if (checkWin(i, j)) {
                        return true;
                    }
                }
            }
        }
        
        // 检查是否平局（棋盘已满）
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean checkWin(int x, int y) {
        int player = board[x][y];
        
        // 检查水平方向
        int count = 1;
        for (int i = 1; i < 5; i++) {
            if (x + i < BOARD_SIZE && board[x + i][y] == player) count++;
            else break;
        }
        for (int i = 1; i < 5; i++) {
            if (x - i >= 0 && board[x - i][y] == player) count++;
            else break;
        }
        if (count >= 5) return true;
        
        // 检查垂直方向
        count = 1;
        for (int i = 1; i < 5; i++) {
            if (y + i < BOARD_SIZE && board[x][y + i] == player) count++;
            else break;
        }
        for (int i = 1; i < 5; i++) {
            if (y - i >= 0 && board[x][y - i] == player) count++;
            else break;
        }
        if (count >= 5) return true;
        
        // 检查主对角线
        count = 1;
        for (int i = 1; i < 5; i++) {
            if (x + i < BOARD_SIZE && y + i < BOARD_SIZE && board[x + i][y + i] == player) count++;
            else break;
        }
        for (int i = 1; i < 5; i++) {
            if (x - i >= 0 && y - i >= 0 && board[x - i][y - i] == player) count++;
            else break;
        }
        if (count >= 5) return true;
        
        // 检查副对角线
        count = 1;
        for (int i = 1; i < 5; i++) {
            if (x + i < BOARD_SIZE && y - i >= 0 && board[x + i][y - i] == player) count++;
            else break;
        }
        for (int i = 1; i < 5; i++) {
            if (x - i >= 0 && y + i < BOARD_SIZE && board[x - i][y + i] == player) count++;
            else break;
        }
        return count >= 5;
    }

    public boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private void saveGameToDatabase(String winner) {
        try {
            // 计算游戏时长（秒）
            int duration = (int)((System.currentTimeMillis() - startTime) / 1000);
            
            // 保存游戏记录并获取生成的游戏ID
            int gameId = GameDatabase.saveGame(gameType, winner, duration);
            
            // 保存所有移动记录
            if (gameId != -1) {
                GameDatabase.saveMoves(gameId, moves);
                System.out.println("棋局已保存到数据库，游戏ID: " + gameId);
                System.out.println("总计保存了 " + moves.size() + " 步棋");
            } else {
                System.err.println("保存游戏记录失败：无法获取游戏ID");
            }
        } catch (Exception e) {
            System.err.println("保存棋局失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getter methods
    public int[][] getBoard() {
        return board;
    }
    
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void gameOver(String winner) {
        if (!isGameEnded) {  // 只有在游戏还没有保存时才保存
            System.out.println("游戏结束，获胜者：" + winner);
            saveGameToDatabase(winner);
            System.out.println("尝试保存游戏记录...");
            isGameEnded = true;
        }
    }

    public int getMoveCount() {
        return moves.size();
    }

    public boolean undoMove() {
        if (!moves.isEmpty()) {
            Move lastMove = moves.remove(moves.size() - 1);
            board[lastMove.getX()][lastMove.getY()] = EMPTY;
            currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
            return true;
        }
        return false;
    }
}
