import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameBoard extends JFrame {
    private GomokuGame game;
    private JPanel boardPanel;
    private JPanel infoPanel;
    private boolean isAIGame;
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private JLabel blackTimeLabel;
    private JLabel whiteTimeLabel;
    private JLabel moveCountLabel;
    private JLabel currentPlayerLabel;
    private Timer timer;
    private int blackSeconds = 0;
    private int whiteSeconds = 0;
    
    public GameBoard(GomokuGame game) {
        this.game = game;
        initializeUI();
        startTimer();
    }
    
    private void initializeUI() {
        setTitle("五子棋");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        
        // 创建棋盘面板
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
            }
        };
        
        boardPanel.setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE + CELL_SIZE * 2,
                                                 BOARD_SIZE * CELL_SIZE + CELL_SIZE * 2));
        
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
        
        // 修改信息面板
        infoPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(200, getHeight()));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建一个容器面板来包含所有标签和按钮
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 设置标签字体和样式
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 16);
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 14);
        Dimension buttonSize = new Dimension(160, 30);
        
        // 创建并设置标签
        JLabel[] labels = {
            blackTimeLabel = new JLabel("黑方用时: 0:00"),
            whiteTimeLabel = new JLabel("白方用时: 0:00"),
            moveCountLabel = new JLabel("手数: 0"),
            currentPlayerLabel = new JLabel("当前: 黑方")
        };
        
        // 设置标签样式
        for (JLabel label : labels) {
            label.setFont(labelFont);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        
        // 创建按钮
        JButton[] buttons = {
            new JButton("悔棋"),
            new JButton("重新开始"),
            new JButton("认输"),
            new JButton("返回主菜单"),
            new JButton("退出游戏")
        };
        
        // 设置按钮样式
        for (JButton button : buttons) {
            button.setFont(buttonFont);
            button.setPreferredSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        
        // 添加按钮事件
        buttons[0].addActionListener(e -> undoMove());
        buttons[1].addActionListener(e -> restartGame());
        buttons[2].addActionListener(e -> surrender());
        buttons[3].addActionListener(e -> {
            dispose();
            new StartScreen().setVisible(true);
        });
        buttons[4].addActionListener(e -> System.exit(0));
        
        // 如果是AI对战或网络对战，禁用悔棋按钮
        if (game.isAIGame() || game.isNetworkGame()) {
            buttons[0].setEnabled(false);
        }
        
        // 添加组件到内容面板，使用固定间距
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(blackTimeLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(whiteTimeLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(moveCountLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(currentPlayerLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        
        // 添加按钮，每个按钮之间有固定间距
        for (JButton button : buttons) {
            contentPanel.add(button);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        
        // 添加一个弹性空间，使内容居中
        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(contentPanel);
        infoPanel.add(Box.createVerticalGlue());
        
        // 添加面板到主窗口
        add(boardPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void startTimer() {
        timer = new Timer(1000, e -> {
            if (game.getCurrentPlayer() == GomokuGame.BLACK) {
                blackSeconds++;
            } else {
                whiteSeconds++;
            }
            updateTimeLabels();
        });
        timer.start();
    }
    
    private void updateTimeLabels() {
        blackTimeLabel.setText(String.format("黑方用时: %d:%02d", blackSeconds / 60, blackSeconds % 60));
        whiteTimeLabel.setText(String.format("白方用时: %d:%02d", whiteSeconds / 60, whiteSeconds % 60));
    }
    
    private void undoMove() {
        if (game.undoMove()) {
            boardPanel.repaint();
            updateInfoPanel();
        } else {
            JOptionPane.showMessageDialog(this, "无法悔棋！");
        }
    }
    
    private void restartGame() {
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要重新开始吗？", "重新开始", 
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            new GameBoard(game).setVisible(true);
        }
    }
    
    private void surrender() {
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要认输吗？", "认输", 
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            timer.stop();
            String winner = (game.getCurrentPlayer() == GomokuGame.BLACK) ? "白方" : "黑方";
            game.gameOver(winner);  // 先保存游戏
            JOptionPane.showMessageDialog(this, winner + "获胜！");
            dispose();
            new StartScreen().setVisible(true);
        }
    }
    
    private void updateInfoPanel() {
        moveCountLabel.setText("手数: " + game.getMoveCount());
        currentPlayerLabel.setText("当前: " + (game.getCurrentPlayer() == GomokuGame.BLACK ? "黑方" : "白方"));
    }
    
    private void handleMouseClick(MouseEvent e) {
        int x = Math.round((float)(e.getX() - CELL_SIZE) / CELL_SIZE);
        int y = Math.round((float)(e.getY() - CELL_SIZE) / CELL_SIZE);
        
        if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
            if (game.makeMove(x, y)) {
                boardPanel.repaint();
                updateInfoPanel();
                
                // 检查获胜条件
                if (game.checkWin(x, y)) {
                    timer.stop();
                    String winner = (game.getCurrentPlayer() == GomokuGame.BLACK) ? "黑方" : "白方";
                    game.gameOver(winner);
                    JOptionPane.showMessageDialog(this, winner + "获胜！");
                    dispose();
                    new StartScreen().setVisible(true);
                    return;
                }
                
                // 检查平局
                if (game.isBoardFull()) {
                    timer.stop();
                    game.gameOver("平局");
                    JOptionPane.showMessageDialog(this, "平局！");
                    dispose();
                    new StartScreen().setVisible(true);
                    return;
                }
                
                // 更新当前玩家
                if (!game.isNetworkGame() && game.isAIGame() && game.getCurrentPlayer() == GomokuGame.WHITE) {
                    makeAIMove();
                }
            }
        }
    }
    
    private void makeAIMove() {
        // AI移动逻辑
        // ... 
    }
    
    private void drawBoard(Graphics g) {
        // 绘制棋盘背景
        g.setColor(new Color(222, 184, 135));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // 绘制棋盘线
        g.setColor(Color.BLACK);
        for (int i = 0; i < BOARD_SIZE; i++) {
            // 修改线条绘制位置，使其与格点对齐
            g.drawLine(CELL_SIZE, CELL_SIZE * (i + 1),
                      CELL_SIZE * BOARD_SIZE, CELL_SIZE * (i + 1));
            g.drawLine(CELL_SIZE * (i + 1), CELL_SIZE,
                      CELL_SIZE * (i + 1), CELL_SIZE * BOARD_SIZE);
        }

        // 绘制天元和星位
        g.fillOval(CELL_SIZE * 8 - 3, CELL_SIZE * 8 - 3, 6, 6);  // 天元
        int[] starPoints = {4, 4, 4, 12, 12, 4, 12, 12, 8, 4, 4, 8, 12, 8, 8, 12};
        for (int i = 0; i < starPoints.length; i += 2) {
            g.fillOval(CELL_SIZE * starPoints[i] - 3, 
                      CELL_SIZE * starPoints[i + 1] - 3, 6, 6);
        }
    }
    
    private void drawPieces(Graphics g) {
        int[][] board = game.getBoard();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != GomokuGame.EMPTY) {
                    // 修改棋子绘制位置计算
                    int x = (i + 1) * CELL_SIZE;
                    int y = (j + 1) * CELL_SIZE;
                    
                    // 使用抗锯齿
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                       RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    if (board[i][j] == GomokuGame.BLACK) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    
                    // 调整棋子大小
                    int pieceSize = (int)(CELL_SIZE * 0.8);
                    g.fillOval(x - pieceSize/2, y - pieceSize/2,
                              pieceSize, pieceSize);
                    
                    if (board[i][j] == GomokuGame.WHITE) {
                        g.setColor(Color.BLACK);
                        g.drawOval(x - pieceSize/2, y - pieceSize/2,
                                 pieceSize, pieceSize);
                    }
                }
            }
        }
    }
}
