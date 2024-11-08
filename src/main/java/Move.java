import java.io.Serializable;


// 移动类，用于记录棋局中的每一步移动
// 包含移动坐标、落子颜色和时间戳
// 实现Serializable接口，支持序列化
public class Move implements Serializable {
    private static final long serialVersionUID = 1L;// 序列化机制使用的版本标识符
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
