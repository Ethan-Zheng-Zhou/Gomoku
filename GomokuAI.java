import java.util.ArrayList;
import java.awt.Point;
import java.util.List;

public class GomokuAI {
    private static final int EMPTY = 0;
    private static final int BLACK = 1;
    private static final int WHITE = 2;
    private static final int SEARCH_DEPTH = 4;
    private static final int WIN_SCORE = 100000;
    private static final int MAX_DEPTH = 8; // 将最大搜索深度限制在8层
    private static final long TIME_LIMIT = 5000; // 10秒时间限制
    private long startTime;
    
    // 方向数组，用于检查八个方向
    private static final int[][] DIRECTIONS = {
        {1, 0}, {0, 1}, {1, 1}, {1, -1},
        {-1, 0}, {0, -1}, {-1, -1}, {-1, 1}
    };
    
    public static class Move {
        int x, y;
        int score;
        
        public Move(int x, int y, int score) {
            this.x = x;
            this.y = y;
            this.score = score;
        }
    }
    
    public Move findBestMove(int[][] board) {
        startTime = System.currentTimeMillis();
        return minimax(board, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
    }
    
    private Move minimax(int[][] board, int depth, int alpha, int beta, boolean isMaximizing) {
        // 检查时间限制
        if (System.currentTimeMillis() - startTime > TIME_LIMIT) {
            return new Move(-1, -1, evaluate(board));
        }
        
        // 基本情况：到达最大深度或游戏结束
        if (depth == 0 || isGameOver(board)) {
            return new Move(-1, -1, evaluate(board));
        }
        
        List<Point> moves = generateMoves(board);
        if (moves.isEmpty()) {
            return new Move(-1, -1, evaluate(board));
        }
        
        // 启发式排序可能的移动
        moves.sort((a, b) -> evaluateMove(board, b) - evaluateMove(board, a));
        
        Move bestMove = null;
        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Point move : moves) {
                board[move.x][move.y] = BLACK;
                int eval = minimax(board, depth - 1, alpha, beta, false).score;
                board[move.x][move.y] = EMPTY;
                
                if (eval > maxEval) {
                    maxEval = eval;
                    bestMove = new Move(move.x, move.y, maxEval);
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Point move : moves) {
                board[move.x][move.y] = WHITE;
                int eval = minimax(board, depth - 1, alpha, beta, true).score;
                board[move.x][move.y] = EMPTY;
                
                if (eval < minEval) {
                    minEval = eval;
                    bestMove = new Move(move.x, move.y, minEval);
                }
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
        }
        return bestMove;
    }
    
    private List<Point> generateMoves(int[][] board) {
        List<Point> moves = new ArrayList<>();
        int size = board.length;
        
        // 只考虑已有棋子周围的空位
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == EMPTY && hasNeighbor(board, i, j)) {
                    moves.add(new Point(i, j));
                }
            }
        }
        return moves;
    }
    
    private boolean hasNeighbor(int[][] board, int x, int y) {
        int size = board.length;
        int range = 2; // 考虑2格范围内的邻居
        
        for (int i = Math.max(0, x - range); i <= Math.min(size - 1, x + range); i++) {
            for (int j = Math.max(0, y - range); j <= Math.min(size - 1, y + range); j++) {
                if (board[i][j] != EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private int evaluateMove(int[][] board, Point move) {
        // 简单评估函数，计算周围的连子情况
        int score = 0;
        int[][] directions = {{1,0}, {0,1}, {1,1}, {1,-1}};
        
        for (int[] dir : directions) {
            score += evaluateDirection(board, move.x, move.y, dir[0], dir[1]);
        }
        return score;
    }
    
    private int evaluateDirection(int[][] board, int x, int y, int dx, int dy) {
        // 评估某个方向的连子价值
        int score = 0;
        int size = board.length;
        
        // 检查连续5个位置
        int count = 1;
        int empty = 0;
        
        // 向正方向检查
        for (int i = 1; i < 5; i++) {
            int newX = x + dx * i;
            int newY = y + dy * i;
            if (newX < 0 || newX >= size || newY < 0 || newY >= size) break;
            if (board[newX][newY] == EMPTY) empty++;
            else if (board[newX][newY] == board[x][y]) count++;
            else break;
        }
        
        // 向反方向检查
        for (int i = 1; i < 5; i++) {
            int newX = x - dx * i;
            int newY = y - dy * i;
            if (newX < 0 || newX >= size || newY < 0 || newY >= size) break;
            if (board[newX][newY] == EMPTY) empty++;
            else if (board[newX][newY] == board[x][y]) count++;
            else break;
        }
        
        // 根据连子数和空位数评分
        if (count >= 5) score += 100000;
        else if (count == 4 && empty >= 1) score += 10000;
        else if (count == 3 && empty >= 2) score += 1000;
        else if (count == 2 && empty >= 3) score += 100;
        
        return score;
    }
    
    private int evaluate(int[][] board) {
        int score = 0;
        int size = board.length;
        
        // 横向、纵向和对角线方向评估
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != EMPTY) {
                    // 对每个方向进行评估
                    for (int[] direction : DIRECTIONS) {
                        score += evaluateLineFromPoint(board, i, j, direction[0], direction[1]);
                    }
                }
            }
        }
        
        return score;
    }
    
    private int evaluateLineFromPoint(int[][] board, int startX, int startY, int dx, int dy) {
        int size = board.length;
        int player = board[startX][startY];
        int count = 1;
        int blocked = 0;
        
        // 向一个方向检查
        for (int i = 1; i < 5; i++) {
            int x = startX + dx * i;
            int y = startY + dy * i;
            
            if (x < 0 || x >= size || y < 0 || y >= size) {
                blocked++;
                break;
            }
            
            if (board[x][y] == player) {
                count++;
            } else if (board[x][y] == EMPTY) {
                break;
            } else {
                blocked++;
                break;
            }
        }
        
        // 向相反方向检查
        for (int i = 1; i < 5; i++) {
            int x = startX - dx * i;
            int y = startY - dy * i;
            
            if (x < 0 || x >= size || y < 0 || y >= size) {
                blocked++;
                break;
            }
            
            if (board[x][y] == player) {
                count++;
            } else if (board[x][y] == EMPTY) {
                break;
            } else {
                blocked++;
                break;
            }
        }
        
        // 根据连子数量和被封堵情况评分
        if (count >= 5) return player == BLACK ? WIN_SCORE : -WIN_SCORE;
        
        int score = 0;
        if (blocked == 0) {
            switch (count) {
                case 4: score = 10000; break;
                case 3: score = 1000; break;
                case 2: score = 100; break;
                case 1: score = 10; break;
            }
        } else if (blocked == 1) {
            switch (count) {
                case 4: score = 1000; break;
                case 3: score = 100; break;
                case 2: score = 10; break;
                case 1: score = 1; break;
            }
        }
        
        return player == BLACK ? score : -score;
    }
    
    private boolean isGameOver(int[][] board) {
        // 检查是否有玩家获胜
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] != EMPTY) {
                    if (checkWin(board, i, j)) {
                        return true;
                    }
                }
            }
        }
        
        // 检查是否平局（棋盘已满）
        boolean isFull = true;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == EMPTY) {
                    isFull = false;
                    break;
                }
            }
        }
        
        return isFull;
    }
    
    private boolean checkWin(int[][] board, int x, int y) {
        int player = board[x][y];
        
        // 检查所有方向
        for (int[] direction : DIRECTIONS) {
            int count = 1;
            
            // 向一个方向检查
            for (int i = 1; i < 5; i++) {
                int newX = x + direction[0] * i;
                int newY = y + direction[1] * i;
                
                if (newX < 0 || newX >= board.length || newY < 0 || newY >= board.length) {
                    break;
                }
                
                if (board[newX][newY] == player) {
                    count++;
                } else {
                    break;
                }
            }
            
            // 向相反方向检查
            for (int i = 1; i < 5; i++) {
                int newX = x - direction[0] * i;
                int newY = y - direction[1] * i;
                
                if (newX < 0 || newX >= board.length || newY < 0 || newY >= board.length) {
                    break;
                }
                
                if (board[newX][newY] == player) {
                    count++;
                } else {
                    break;
                }
            }
            
            if (count >= 5) {
                return true;
            }
        }
        
        return false;
    }
}
