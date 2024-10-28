import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class GameBoard extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 45;  // 增加格子大小
    private static final int MARGIN = 30;     // 增加边距
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BOARD_COLOR = new Color(210, 180, 140);
    private static final Color GRID_COLOR = new Color(101, 67, 33);
    private static final Color CONTROL_PANEL_COLOR = new Color(232, 232, 232);
    
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
    private int moveCount = 0;
    private JLabel moveCountLabel;
    
    public GameBoard(boolean isAIMode) {
        this.isAIMode = isAIMode;
        if (isAIMode) {
            this.ai = new GomokuAI();
            setTitle("五子棋 - AI对战模式");
        } else {
            setTitle("五子棋 - 玩家对战模式");
        }
        
        // 调整窗口大小，确保能完整显示棋盘
        int windowWidth = BOARD_SIZE * CELL_SIZE + 2 * MARGIN + 300;  // 增加宽度以容纳右侧面板
        int windowHeight = BOARD_SIZE * CELL_SIZE + 2 * MARGIN + 150; // 增加高度以容纳工具栏和边距
        setSize(windowWidth, windowHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout(20, 20));  // 增加组件之间的间距
        
        // 创建工具栏
        createMenuBar();
        
        // 创建右侧控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(CONTROL_PANEL_COLOR);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 添加手数显示标签
        moveCountLabel = new JLabel("当前手数: 0");
        moveCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        moveCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 设置计时器标签样式
        timeLabel = new JLabel("黑方用时: 0:00  白方用时: 0:00");
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 创建按钮
        JButton undoButton = createStyledButton("悔棋");
        JButton resetButton = createStyledButton("重新开始");
        
        // 添加按钮事件
        undoButton.addActionListener(e -> undoMove());
        resetButton.addActionListener(e -> resetGame());
        
        // 添加组件到控制面板
        controlPanel.add(moveCountLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(timeLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(undoButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(resetButton);
        
        // 创建一个包装棋盘的面板，用于居中
        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setBackground(BACKGROUND_COLOR);
        
        // 创建棋盘面板
        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(
            BOARD_SIZE * CELL_SIZE + 2 * MARGIN,
            BOARD_SIZE * CELL_SIZE + 2 * MARGIN
        ));
        boardPanel.setBackground(BOARD_COLOR);
        
        // 将棋盘添加到包装面板中以实现居中
        boardWrapper.add(boardPanel);
        
        // 添加所有组件到窗口
        add(boardWrapper, BorderLayout.CENTER);
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
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 绘制棋盘网格
            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(1.0f));
            
            // 画横线
            for (int i = 0; i < BOARD_SIZE; i++) {
                g2.drawLine(
                    MARGIN, 
                    MARGIN + i * CELL_SIZE, 
                    MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, 
                    MARGIN + i * CELL_SIZE
                );
            }
            
            // 画竖线
            for (int i = 0; i < BOARD_SIZE; i++) {
                g2.drawLine(
                    MARGIN + i * CELL_SIZE, 
                    MARGIN, 
                    MARGIN + i * CELL_SIZE, 
                    MARGIN + (BOARD_SIZE - 1) * CELL_SIZE
                );
            }
            
            // 绘制棋子
            int pieceSize = (int)(CELL_SIZE * 0.8);
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j] == 1) {  // 黑子
                        g2.setColor(Color.BLACK);
                        g2.fillOval(
                            MARGIN + i * CELL_SIZE - pieceSize/2,
                            MARGIN + j * CELL_SIZE - pieceSize/2,
                            pieceSize,
                            pieceSize
                        );
                    } else if (board[i][j] == 2) {  // 白子
                        g2.setColor(Color.WHITE);
                        g2.fillOval(
                            MARGIN + i * CELL_SIZE - pieceSize/2,
                            MARGIN + j * CELL_SIZE - pieceSize/2,
                            pieceSize,
                            pieceSize
                        );
                        g2.setColor(Color.BLACK);
                        g2.drawOval(
                            MARGIN + i * CELL_SIZE - pieceSize/2,
                            MARGIN + j * CELL_SIZE - pieceSize/2,
                            pieceSize,
                            pieceSize
                        );
                    }
                }
            }
        }
    }
    
    private void createMenuBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(BACKGROUND_COLOR);
        toolBar.setBorderPainted(false);
        
        // 添加返回按钮
        JButton backBtn = createToolBarButton("返回主菜单");
        JButton newGameBtn = createToolBarButton("新游戏");
        JButton saveGameBtn = createToolBarButton("保存游戏");
        JButton loadGameBtn = createToolBarButton("加载游戏");
        
        // 返回按钮事件
        backBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要返回主菜单吗？当前游戏进度将丢失。",
                "返回确认",
                JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                dispose(); // 关闭当前游戏窗口
                new StartScreen().setVisible(true); // 打开开始界面
            }
        });
        
        newGameBtn.addActionListener(e -> resetGame());
        saveGameBtn.addActionListener(e -> saveGame());
        loadGameBtn.addActionListener(e -> loadGame());
        
        // 添加按钮到工具栏
        toolBar.add(backBtn);
        toolBar.addSeparator(new Dimension(20, 0));  // 加大与其他按钮的间距
        toolBar.add(newGameBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(saveGameBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(loadGameBtn);
        
        // 确保工具栏添加到 NORTH 位置
        add(toolBar, BorderLayout.NORTH);
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
        moveCount++;
        moveCountLabel.setText("当前手数: " + moveCount);
        
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
            moveCount--;
            moveCountLabel.setText("当前手数: " + moveCount);
            repaint();
        }
    }
    
    public void resetGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        isBlackTurn = true;
        blackTime = 0;
        whiteTime = 0;
        moveCount = 0;
        moveHistory.clear();
        moveCountLabel.setText("当前手数: 0");
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
    
    // 添加创建样式化按钮的方法
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(120, 35));
        button.setMaximumSize(new Dimension(120, 35));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }
    
    // 添加创���工具栏按钮的辅助方法
    private JButton createToolBarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(BACKGROUND_COLOR);
        button.setForeground(new Color(70, 130, 180));
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BACKGROUND_COLOR);
            }
        });
        
        return button;
    }
}
