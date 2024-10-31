import java.io.Serializable;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int x;
    private final int y;
    private final boolean isBlack;
    private final long timestamp;

    public Move(int x, int y, boolean isBlack) {
        this.x = x;
        this.y = y;
        this.isBlack = isBlack;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isBlack() { return isBlack; }
    public long getTimestamp() { return timestamp; }
}
