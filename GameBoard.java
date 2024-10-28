import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class GameBoard extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int MARGIN = 20;
    
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE]; // 0:空, 1:黑, 2:白
    private boolean isBlackTurn = true;
    private Stack<Move> moveHistory = new Stack<>();
    private javax.swing.Timer gameTimer; // 明确指定使用 javax.swing.Timer
    private int blackTime = 0;
    private int whiteTime = 0;
    private JLabel timeLabel;
    private BoardPanel boardPanel; // 添加成员变量
    private boolean isAIMode;
    private GomokuAI ai;
    
    public GameBoard(boolean isAIMode) {
        this.isAIMode = isAIMode;
        if (isAIMode) {
            this.ai = new GomokuAI();
            setTitle("五子棋 - AI对战模式");
        } else {
            setTitle("五子棋 - 玩家对战模式");
        }
        
        setSize(BOARD_SIZE * CELL_SIZE + 2 * MARGIN + 200, BOARD_SIZE * CELL_SIZE + 2 * MARGIN);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建菜单栏
        createMenuBar();
        
        // 创建右侧控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        // 添加计时器标签
        timeLabel = new JLabel("黑方用时: 0:00  白方用时: 0:00");
        controlPanel.add(timeLabel);
        
        // 添加悔棋按钮
        JButton undoButton = new JButton("悔棋");
        undoButton.addActionListener(e -> undoMove());
        controlPanel.add(undoButton);
        
        // 添加重新开始按钮
        JButton restartButton = new JButton("重新开始");
        restartButton.addActionListener(e -> resetGame());
        controlPanel.add(restartButton);
        
        // 初始化棋盘面板
        boardPanel = new BoardPanel();
        
        // 设置布局
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        
        // 初始化计时器
        initializeTimer();
        
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 修正坐标计算，四舍五入到最近的交叉点
                int x = Math.round((float)(e.getX() - MARGIN) / CELL_SIZE);
                int y = Math.round((float)(e.getY() - MARGIN) / CELL_SIZE);
                
                if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && board[x][y] == 0) {
                    makeMove(x, y);
                }
            }
        });
        
        setVisible(true);
    }
    
    // 添加内部类 BoardPanel
    class BoardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
            drawPieces(g);
        }
        
        private void drawBoard(Graphics g) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                g.drawLine(MARGIN, MARGIN + i * CELL_SIZE, 
                          MARGIN + (BOARD_SIZE-1) * CELL_SIZE, MARGIN + i * CELL_SIZE);
                g.drawLine(MARGIN + i * CELL_SIZE, MARGIN, 
                          MARGIN + i * CELL_SIZE, MARGIN + (BOARD_SIZE-1) * CELL_SIZE);
            }
        }
        
        private void drawPieces(Graphics g) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j] == 1) {  // 黑子
                        g.setColor(Color.BLACK);
                        g.fillOval(MARGIN + i * CELL_SIZE - CELL_SIZE/3,
                                  MARGIN + j * CELL_SIZE - CELL_SIZE/3,
                                  CELL_SIZE*2/3, CELL_SIZE*2/3);
                    } else if (board[i][j] == 2) {  // 白子
                        g.setColor(Color.WHITE);
                        g.fillOval(MARGIN + i * CELL_SIZE - CELL_SIZE/3,
                                  MARGIN + j * CELL_SIZE - CELL_SIZE/3,
                                  CELL_SIZE*2/3, CELL_SIZE*2/3);
                        g.setColor(Color.BLACK);
                        g.drawOval(MARGIN + i * CELL_SIZE - CELL_SIZE/3,
                                  MARGIN + j * CELL_SIZE - CELL_SIZE/3,
                                  CELL_SIZE*2/3, CELL_SIZE*2/3);
                    }
                }
            }
        }
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGame = new JMenuItem("新游戏");
        JMenuItem saveGame = new JMenuItem("保存游戏");
        JMenuItem loadGame = new JMenuItem("加载游戏");
        JMenuItem exit = new JMenuItem("退出");
        
        newGame.addActionListener(e -> resetGame());
        saveGame.addActionListener(e -> saveGame());
        loadGame.addActionListener(e -> loadGame());
        exit.addActionListener(e -> System.exit(0));
        
        gameMenu.add(newGame);
        gameMenu.add(saveGame);
        gameMenu.add(loadGame);
        gameMenu.addSeparator();
        gameMenu.add(exit);
        
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }
    
    private void initializeTimer() {
        gameTimer = new javax.swing.Timer(1000, e -> {
            if (isBlackTurn) {
                blackTime++;
            } else {
                whiteTime++;
            }
            updateTimeLabel();
        });
        gameTimer.start();
    }
    
    private void updateTimeLabel() {
        timeLabel.setText(String.format("黑方用时: %d:%02d  白方用时: %d:%02d",
            blackTime / 60, blackTime % 60, whiteTime / 60, whiteTime % 60));
    }
    
    private boolean checkWin(int x, int y) {
        int player = board[x][y];
        // 检查横向
        int count = 1;
        for (int i = x - 1; i >= 0 && board[i][y] == player; i--) count++;
        for (int i = x + 1; i < BOARD_SIZE && board[i][y] == player; i++) count++;
        if (count >= 5) return true;
        
        // 检查纵向
        count = 1;
        for (int i = y - 1; i >= 0 && board[x][i] == player; i--) count++;
        for (int i = y + 1; i < BOARD_SIZE && board[x][i] == player; i++) count++;
        if (count >= 5) return true;
        
        // 检查左上-右下对角线
        count = 1;
        for (int i = 1; x - i >= 0 && y - i >= 0 && board[x-i][y-i] == player; i++) count++;
        for (int i = 1; x + i < BOARD_SIZE && y + i < BOARD_SIZE && board[x+i][y+i] == player; i++) count++;
        if (count >= 5) return true;
        
        // 检查右上-左下对角线
        count = 1;
        for (int i = 1; x + i < BOARD_SIZE && y - i >= 0 && board[x+i][y-i] == player; i++) count++;
        for (int i = 1; x - i >= 0 && y + i < BOARD_SIZE && board[x-i][y+i] == player; i++) count++;
        return count >= 5;
    }
    
    private void makeMove(int x, int y) {
        board[x][y] = isBlackTurn ? 1 : 2;
        moveHistory.push(new Move(x, y, isBlackTurn));
        
        if (checkWin(x, y)) {
            gameTimer.stop();
            JOptionPane.showMessageDialog(null, 
                (isBlackTurn ? "黑棋" : "白棋") + "获胜！");
            resetGame();
            return;
        }
        
        isBlackTurn = !isBlackTurn;
        repaint();
        
        // 如果是AI模式且轮到AI（白子）
        if (isAIMode && !isBlackTurn) {
            SwingUtilities.invokeLater(() -> {
                GomokuAI.Move bestMove = ai.findBestMove(board, false);
                if (bestMove.x != -1 && bestMove.y != -1) {
                    makeMove(bestMove.x, bestMove.y);
                }
            });
        }
    }
    
    private void undoMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.pop();
            board[lastMove.x][lastMove.y] = 0;
            isBlackTurn = lastMove.isBlack;
            repaint();
        }
    }
    
    public void resetGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        isBlackTurn = true;
        blackTime = 0;
        whiteTime = 0;
        moveHistory.clear();
        updateTimeLabel();
        gameTimer.restart();
        repaint();
    }
    
    // 内部类用于存储移动记录
    private static class Move {
        int x, y;
        boolean isBlack;
        
        Move(int x, int y, boolean isBlack) {
            this.x = x;
            this.y = y;
            this.isBlack = isBlack;
        }
    }
    
    private void saveGame() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("game.sav"))) {
            // 创建要保存的游戏状态
            GameState state = new GameState(
                board, 
                isBlackTurn, 
                blackTime, 
                whiteTime
            );
            oos.writeObject(state);
            JOptionPane.showMessageDialog(this, "游戏已保存");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage());
        }
    }

    private void loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("game.sav"))) {
            GameState state = (GameState) ois.readObject();
            // 恢复游戏状态
            board = state.board;
            isBlackTurn = state.isBlackTurn;
            blackTime = state.blackTime;
            whiteTime = state.whiteTime;
            moveHistory.clear();
            repaint();
            updateTimeLabel();
            JOptionPane.showMessageDialog(this, "游戏已加载");
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "加载失败: " + e.getMessage());
        }
    }
}
