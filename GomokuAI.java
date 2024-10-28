import java.util.ArrayList;
import java.awt.Point;

public class GomokuAI {
    private static final int EMPTY = 0;
    private static final int BLACK = 1;
    private static final int WHITE = 2;
    private static final int SEARCH_DEPTH = 4;
    private static final int WIN_SCORE = 100000;
    
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
    
    public Move findBestMove(int[][] board, boolean isBlack) {
        int player = isBlack ? BLACK : WHITE;
        return alphaBeta(board, SEARCH_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, player);
    }
    
    private Move alphaBeta(int[][] board, int depth, int alpha, int beta, boolean isMax, int player) {
        if (depth == 0) {
            return new Move(-1, -1, evaluateBoard(board, player));
        }
        
        ArrayList<Point> availableMoves = generateMoves(board);
        if (availableMoves.isEmpty()) {
            return new Move(-1, -1, evaluateBoard(board, player));
        }
        
        Move bestMove = new Move(-1, -1, isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE);
        
        for (Point move : availableMoves) {
            board[move.x][move.y] = player;
            
            Move currentMove;
            if (isMax) {
                currentMove = alphaBeta(board, depth - 1, alpha, beta, false, player == BLACK ? WHITE : BLACK);
                if (currentMove.score > bestMove.score) {
                    bestMove = new Move(move.x, move.y, currentMove.score);
                }
                alpha = Math.max(alpha, bestMove.score);
            } else {
                currentMove = alphaBeta(board, depth - 1, alpha, beta, true, player == BLACK ? WHITE : BLACK);
                if (currentMove.score < bestMove.score) {
                    bestMove = new Move(move.x, move.y, currentMove.score);
                }
                beta = Math.min(beta, bestMove.score);
            }
            
            board[move.x][move.y] = EMPTY;
            
            if (beta <= alpha) {
                break;
            }
        }
        
        return bestMove;
    }
    
    private ArrayList<Point> generateMoves(int[][] board) {
        ArrayList<Point> moves = new ArrayList<>();
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
        int range = 2; // 检查2格范围内是否有棋子
        
        for (int i = Math.max(0, x - range); i <= Math.min(size - 1, x + range); i++) {
            for (int j = Math.max(0, y - range); j <= Math.min(size - 1, y + range); j++) {
                if (board[i][j] != EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private int evaluateBoard(int[][] board, int player) {
        int score = 0;
        int size = board.length;
        
        // 评估所有可能的五元组
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int[] dir : DIRECTIONS) {
                    int[] lineScore = evaluateLine(board, i, j, dir[0], dir[1], player);
                    score += lineScore[0]; // 己方得分
                    score -= lineScore[1]; // 对方得分
                }
            }
        }
        
        return score;
    }
    
    private int[] evaluateLine(int[][] board, int startX, int startY, int dx, int dy, int player) {
        int[] score = new int[2]; // [己方得分, 对方得分]
        int opponent = (player == BLACK) ? WHITE : BLACK;
        int size = board.length;
        
        int count = 0;
        int block = 0;
        int piece = EMPTY;
        
        // 检查五个连续位置
        for (int i = 0; i < 5; i++) {
            int x = startX + i * dx;
            int y = startY + i * dy;
            
            if (x < 0 || x >= size || y < 0 || y >= size) {
                return new int[]{0, 0};
            }
            
            if (board[x][y] == player) {
                count++;
            } else if (board[x][y] == opponent) {
                block++;
                piece = opponent;
            }
        }
        
        // 评分规则
        if (count == 5) return new int[]{WIN_SCORE, 0};
        if (count == 4 && block == 0) return new int[]{8000, 0};
        if (count == 3 && block == 0) return new int[]{2000, 0};
        if (count == 2 && block == 0) return new int[]{400, 0};
        if (count == 1 && block == 0) return new int[]{100, 0};
        
        // 对方得分
        if (piece == opponent) {
            if (count == 0 && block == 5) return new int[]{0, WIN_SCORE};
            if (count == 0 && block == 4) return new int[]{0, 8000};
            if (count == 0 && block == 3) return new int[]{0, 2000};
            if (count == 0 && block == 2) return new int[]{0, 400};
            if (count == 0 && block == 1) return new int[]{0, 100};
        }
        
        return new int[]{0, 0};
    }
}
