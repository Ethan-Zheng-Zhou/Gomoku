import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReplayScreen extends JFrame {
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    
    private List<GameRecord> gameRecords;
    private List<Move> currentMoves;
    private int currentMoveIndex = -1;
    private int[][] board;
    private JPanel boardPanel;
    private JTable gameTable;
    private JLabel statusLabel;
    private JButton prevButton;
    private JButton nextButton;
    private JButton autoPlayButton;
    private Timer autoPlayTimer;
    private boolean isAutoPlaying = false;
    
    public ReplayScreen() {
        setTitle("对局复盘");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 初始化棋盘
        board = new int[BOARD_SIZE][BOARD_SIZE];
        
        // 创建主面板，使用BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建左侧面板（对局列表）
        JPanel leftPanel = createLeftPanel();
        
        // 创建右侧面板（棋盘和控制按钮）
        JPanel rightPanel = createRightPanel();
        
        // 添加分隔面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.4);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);
        
        // 加载对局记录
        loadGameRecords();
        
        // 初始化自动播放计时器
        autoPlayTimer = new Timer(1000, e -> showNextMove());
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "对局列表",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14)
        ));
        
        // 创建表格模型
        String[] columnNames = {"日期", "类型", "获胜方", "时长"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        gameTable = new JTable(model);
        gameTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gameTable.setRowHeight(25);
        gameTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 设置表格列宽
        TableColumnModel columnModel = gameTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(100);
        
        // 添加表格选择监听器
        gameTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = gameTable.getSelectedRow();
                if (selectedRow >= 0) {
                    loadGameMoves(gameRecords.get(selectedRow).getGameId());
                }
            }
        });
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(gameTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("删除记录");
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        deleteButton.addActionListener(e -> deleteSelectedGame());
        buttonPanel.add(deleteButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "棋局回放",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14)
        ));
        
        // 创建棋盘面板
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPieces(g);
            }
        };
        
        // 修改棋盘面板大小计算
        int boardWidth = BOARD_SIZE * CELL_SIZE + CELL_SIZE * 2;
        int boardHeight = BOARD_SIZE * CELL_SIZE + CELL_SIZE * 2;
        boardPanel.setPreferredSize(new Dimension(boardWidth, boardHeight));
        boardPanel.setMinimumSize(new Dimension(boardWidth, boardHeight));
        
        // 创建一个包装面板来居中显示棋盘
        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.add(boardPanel);
        
        // 创建控制面板
        JPanel controlPanel = new JPanel(new BorderLayout(10, 0));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建进度条面板
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        JSlider progressSlider = new JSlider(0, 0, 0);
        progressSlider.setPreferredSize(new Dimension(300, 20));
        progressSlider.addChangeListener(e -> {
            if (!progressSlider.getValueIsAdjusting()) {
                int targetMove = progressSlider.getValue();
                jumpToMove(targetMove);
            }
        });
        progressPanel.add(progressSlider, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        prevButton = new JButton("上一步");
        nextButton = new JButton("下一步");
        autoPlayButton = new JButton("自动播放");
        JButton returnButton = new JButton("返回主菜单");
        
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 12);
        prevButton.setFont(buttonFont);
        nextButton.setFont(buttonFont);
        autoPlayButton.setFont(buttonFont);
        returnButton.setFont(buttonFont);
        
        prevButton.addActionListener(e -> showPreviousMove());
        nextButton.addActionListener(e -> showNextMove());
        autoPlayButton.addActionListener(e -> toggleAutoPlay());
        returnButton.addActionListener(e -> {
            dispose();
        });
        
        buttonPanel.add(prevButton);
        buttonPanel.add(autoPlayButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(Box.createHorizontalStrut(20));  // 添加间隔
        buttonPanel.add(returnButton);
        
        // 创建状态标签
        statusLabel = new JLabel("请选择一局游戏", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        // 组装控制面板
        JPanel controlsContainer = new JPanel();
        controlsContainer.setLayout(new BoxLayout(controlsContainer, BoxLayout.Y_AXIS));
        controlsContainer.add(progressPanel);
        controlsContainer.add(Box.createVerticalStrut(10));
        controlsContainer.add(buttonPanel);
        controlsContainer.add(Box.createVerticalStrut(5));
        controlsContainer.add(statusLabel);
        
        controlPanel.add(controlsContainer, BorderLayout.CENTER);
        
        panel.add(boardWrapper, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void drawBoard(Graphics g) {
        // 获取实际的绘制区域大小
        int width = boardPanel.getWidth();
        int height = boardPanel.getHeight();
        
        // 绘制棋盘背景
        g.setColor(new Color(222, 184, 135));
        g.fillRect(0, 0, width, height);
        
        // 绘制棋盘线
        g.setColor(Color.BLACK);
        for (int i = 0; i < BOARD_SIZE; i++) {
            g.drawLine(CELL_SIZE, CELL_SIZE * (i + 1),
                      CELL_SIZE * BOARD_SIZE, CELL_SIZE * (i + 1));
            g.drawLine(CELL_SIZE * (i + 1), CELL_SIZE,
                      CELL_SIZE * (i + 1), CELL_SIZE * BOARD_SIZE);
        }
        
        // 绘制天元和星位
        g.fillOval(CELL_SIZE * 8 - 3, CELL_SIZE * 8 - 3, 6, 6);
        int[] starPoints = {4, 4, 4, 12, 12, 4, 12, 12, 8, 4, 4, 8, 12, 8, 8, 12};
        for (int i = 0; i < starPoints.length; i += 2) {
            g.fillOval(CELL_SIZE * starPoints[i] - 3,
                      CELL_SIZE * starPoints[i + 1] - 3, 6, 6);
        }
    }
    
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 设置字体
        Font moveNumberFont = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(moveNumberFont);
        FontMetrics metrics = g2d.getFontMetrics(moveNumberFont);
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != 0) {
                    int x = (i + 1) * CELL_SIZE;
                    int y = (j + 1) * CELL_SIZE;
                    int pieceSize = (int)(CELL_SIZE * 0.8);
                    
                    // 绘制棋子
                    if (board[i][j] == 1) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    
                    g.fillOval(x - pieceSize/2, y - pieceSize/2,
                              pieceSize, pieceSize);
                    
                    if (board[i][j] == 2) {
                        g.setColor(Color.BLACK);
                        g.drawOval(x - pieceSize/2, y - pieceSize/2,
                                 pieceSize, pieceSize);
                    }
                    
                    // 标记最后一手
                    if (i == lastMoveX && j == lastMoveY) {
                        g.setColor(Color.RED);
                        g.drawRect(x - pieceSize/2, y - pieceSize/2,
                                 pieceSize, pieceSize);
                    }
                    
                    // 查找当前位置的手数
                    int moveNumber = findMoveNumber(i, j);
                    if (moveNumber != -1) {
                        // 设置手数文字颜色（黑子用白色，白子用黑色）
                        g.setColor(board[i][j] == 1 ? Color.WHITE : Color.BLACK);
                        
                        // 计算文字位置使其居中
                        String numberStr = String.valueOf(moveNumber + 1);
                        int textWidth = metrics.stringWidth(numberStr);
                        int textHeight = metrics.getHeight();
                        int textX = x - textWidth / 2;
                        int textY = y + textHeight / 3;  // 稍微上移以视觉居中
                        
                        g.drawString(numberStr, textX, textY);
                    }
                }
            }
        }
    }
    
    // 添加查找手数的辅助方法
    private int findMoveNumber(int x, int y) {
        if (currentMoves == null) return -1;
        
        for (int i = 0; i <= currentMoveIndex; i++) {
            Move move = currentMoves.get(i);
            if (move.getX() == x && move.getY() == y) {
                return i;
            }
        }
        return -1;
    }
    
    private int lastMoveX = -1;
    private int lastMoveY = -1;
    
    private void loadGameRecords() {
        gameRecords = GameDatabase.getGameList();
        DefaultTableModel model = (DefaultTableModel) gameTable.getModel();
        model.setRowCount(0);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (GameRecord record : gameRecords) {
            model.addRow(new Object[]{
                sdf.format(record.getStartTime()),
                record.getGameType(),
                record.getWinner(),
                formatDuration(record.getDuration())
            });
        }
    }
    
    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private void loadGameMoves(int gameId) {
        currentMoves = GameDatabase.loadMoves(gameId);
        currentMoveIndex = -1;
        resetBoard();
        
        // 更新进度条的范围
        JSlider progressSlider = findProgressSlider();
        if (progressSlider != null && currentMoves != null) {
            progressSlider.setMinimum(0);
            progressSlider.setMaximum(currentMoves.size());
            progressSlider.setValue(0);
        }
        
        updateControlButtons();
        updateStatusLabel();
    }
    
    private void resetBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = 0;
            }
        }
        lastMoveX = -1;
        lastMoveY = -1;
        boardPanel.repaint();
    }
    
    private void showNextMove() {
        if (currentMoves != null && currentMoveIndex < currentMoves.size() - 1) {
            currentMoveIndex++;
            Move move = currentMoves.get(currentMoveIndex);
            board[move.getX()][move.getY()] = move.isBlack() ? 1 : 2;
            lastMoveX = move.getX();
            lastMoveY = move.getY();
            
            // 更新进度条
            JSlider progressSlider = findProgressSlider();
            if (progressSlider != null) {
                progressSlider.setValue(currentMoveIndex + 1);
            }
            
            boardPanel.repaint();
            updateControlButtons();
            updateStatusLabel();
        } else if (isAutoPlaying) {
            stopAutoPlay();
        }
    }
    
    private void showPreviousMove() {
        if (currentMoveIndex >= 0) {
            Move move = currentMoves.get(currentMoveIndex);
            board[move.getX()][move.getY()] = 0;
            currentMoveIndex--;
            
            if (currentMoveIndex >= 0) {
                Move lastMove = currentMoves.get(currentMoveIndex);
                lastMoveX = lastMove.getX();
                lastMoveY = lastMove.getY();
            } else {
                lastMoveX = -1;
                lastMoveY = -1;
            }
            
            // 更新进度条
            JSlider progressSlider = findProgressSlider();
            if (progressSlider != null) {
                progressSlider.setValue(currentMoveIndex + 1);
            }
            
            boardPanel.repaint();
            updateControlButtons();
            updateStatusLabel();
        }
    }
    
    private void toggleAutoPlay() {
        if (isAutoPlaying) {
            stopAutoPlay();
        } else {
            startAutoPlay();
        }
    }
    
    private void startAutoPlay() {
        if (currentMoves != null && currentMoveIndex < currentMoves.size() - 1) {
            isAutoPlaying = true;
            autoPlayButton.setText("停止播放");
            autoPlayTimer.start();
        }
    }
    
    private void stopAutoPlay() {
        isAutoPlaying = false;
        autoPlayButton.setText("自动播放");
        autoPlayTimer.stop();
    }
    
    private void updateControlButtons() {
        if (currentMoves == null) {
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            autoPlayButton.setEnabled(false);
        } else {
            prevButton.setEnabled(currentMoveIndex >= 0);
            nextButton.setEnabled(currentMoveIndex < currentMoves.size() - 1);
            autoPlayButton.setEnabled(currentMoveIndex < currentMoves.size() - 1);
        }
    }
    
    private void updateStatusLabel() {
        if (currentMoves == null) {
            statusLabel.setText("请选择一局游戏");
        } else {
            statusLabel.setText(String.format("第 %d/%d 手",
                currentMoveIndex + 1, currentMoves.size()));
        }
    }
    
    private void deleteSelectedGame() {
        int selectedRow = gameTable.getSelectedRow();
        if (selectedRow >= 0) {
            int result = JOptionPane.showConfirmDialog(this,
                "确定要删除这条记录吗？", "删除确认",
                JOptionPane.YES_NO_OPTION);
                
            if (result == JOptionPane.YES_OPTION) {
                int gameId = gameRecords.get(selectedRow).getGameId();
                if (GameDatabase.deleteGame(gameId)) {
                    loadGameRecords();
                    resetBoard();
                    currentMoves = null;
                    updateControlButtons();
                    updateStatusLabel();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "删除失败", "错误",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // 添加跳转到指定步数的方法
    private void jumpToMove(int targetMove) {
        if (currentMoves == null || targetMove < 0 || targetMove > currentMoves.size()) {
            return;
        }
        
        // 重置棋盘
        resetBoard();
        
        // 重新播放到目标步数
        for (int i = 0; i <= targetMove - 1; i++) {
            Move move = currentMoves.get(i);
            board[move.getX()][move.getY()] = move.isBlack() ? 1 : 2;
            lastMoveX = move.getX();
            lastMoveY = move.getY();
        }
        
        currentMoveIndex = targetMove - 1;
        boardPanel.repaint();
        updateControlButtons();
        updateStatusLabel();
    }
    
    // 添加查找进度条的辅助方法
    private JSlider findProgressSlider() {
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                return findSliderInContainer((Container) comp);
            }
        }
        return null;
    }
    
    private JSlider findSliderInContainer(Container container) {
        Component[] components = container.getComponents();
        for (Component comp : components) {
            if (comp instanceof JSlider) {
                return (JSlider) comp;
            } else if (comp instanceof Container) {
                JSlider slider = findSliderInContainer((Container) comp);
                if (slider != null) {
                    return slider;
                }
            }
        }
        return null;
    }
}
