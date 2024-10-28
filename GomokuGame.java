import javax.swing.SwingUtilities;

public class GomokuGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartScreen screen = new StartScreen();
            screen.setVisible(true);
        });
    }
}
