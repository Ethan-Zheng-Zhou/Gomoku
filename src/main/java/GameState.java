import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public final int[][] board;
    public final boolean isBlackTurn;
    public final int blackTime;
    public final int whiteTime;
    
    public GameState(int[][] board, boolean isBlackTurn, int blackTime, int whiteTime) {
        this.board = board;
        this.isBlackTurn = isBlackTurn;
        this.blackTime = blackTime;
        this.whiteTime = whiteTime;
    }
}
