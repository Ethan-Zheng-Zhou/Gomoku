import java.sql.Timestamp;

public class GameRecord {
    private int gameId;
    private Timestamp startTime;
    private String gameType;
    private String winner;
    private int duration;

    public GameRecord(int gameId, Timestamp startTime, String gameType, String winner, int duration) {
        this.gameId = gameId;
        this.startTime = startTime;
        this.gameType = gameType;
        this.winner = winner;
        this.duration = duration;
    }

    // Getters
    public int getGameId() { return gameId; }
    public Timestamp getStartTime() { return startTime; }
    public String getGameType() { return gameType; }
    public String getWinner() { return winner; }
    public int getDuration() { return duration; }

    @Override
    public String toString() {
        return String.format("%s - %s (%s获胜) - 用时：%d分钟", 
            startTime.toString(), 
            gameType, 
            winner, 
            duration / 60);
    }
}
