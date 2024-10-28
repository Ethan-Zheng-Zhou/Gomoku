import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReplayScreen extends JFrame {
    private List<Move> moves;
    private int currentMove = 0;
    private JPanel boardPanel;
    private final int BOARD_SIZE = 15;
    private final int CELL_SIZE = 40;
    private JSlider moveSlider;
    private Timer autoPlayTimer;
    private JList<GameRecord> gameList;
    
    public ReplayScreen() {
        setTitle("棋谱回放");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        showGameSelector();
    }
    
    private void showGameSelector() {
        List<GameRecord> games = GameDatabase.getGameList();
        gameList = new JList<>(games.toArray(new GameRecord[0]));
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 添加右键菜单
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("删除记录");
        deleteItem.addActionListener(e -> deleteSelectedGame());
        popupMenu.add(deleteItem);
        
        // 添加鼠标监听器
        gameList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    handlePopup(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    handlePopup(e);
                }
            }
            
            private void handlePopup(MouseEvent e) {
                int index = gameList.locationToIndex(e.getPoint());
                if (index != -1) {
                    gameList.setSelectedIndex(index);
                    popupMenu.show(gameList, e.getX(), e.getY());
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(gameList);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        int result = JOptionPane.showConfirmDialog(this, scrollPane, 
            "选择要回放的棋局", JOptionPane.OK_CANCEL_OPTION);
            
        if (result == JOptionPane.OK_OPTION && gameList.getSelectedValue() != null) {
            GameRecord selected = gameList.getSelectedValue();
            moves = GameDatabase.loadMoves(selected.getGameId());
            initializeUI();
            setVisible(true);
        } else {
            dispose();
            new StartScreen().setVisible(true);
        }
    }
    
    private void deleteSelectedGame() {
        GameRecord selected = gameList.getSelectedValue();
        if (selected != null) {
            int result = JOptionPane.showConfirmDialog(this,
                "确定要删除这条记录吗？", "删除确认",
                JOptionPane.YES_NO_OPTION);
                
            if (result == JOptionPane.YES_OPTION) {
                if (GameDatabase.deleteGame(selected.getGameId())) {
                    // 刷新列表
                    List<GameRecord> games = GameDatabase.getGameList();
                    gameList.setListData(games.toArray(new GameRecord[0]));
                    JOptionPane.showMessageDialog(this, "删除成功！");
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败！",
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // 棋盘面板
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawMoves(g);
            }
        };
        boardPanel.setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
        
        // 控制面板
        JPanel controlPanel = new JPanel();
        moveSlider = new JSlider(0, moves.size(), 0);
        moveSlider.addChangeListener(e -> {
            currentMove = moveSlider.getValue();
            boardPanel.repaint();
        });
        
        JButton prevButton = new JButton("上一步");
        JButton nextButton = new JButton("下一步");
        JButton autoPlayButton = new JButton("自动播放");
        JButton returnButton = new JButton("返回主菜单");
        
        prevButton.addActionListener(e -> showPreviousMove());
        nextButton.addActionListener(e -> showNextMove());
        autoPlayButton.addActionListener(e -> toggleAutoPlay());
        
        // 修改返回主菜单按钮的事件处理
        returnButton.addActionListener(e -> {
            if (autoPlayTimer != null) {
                autoPlayTimer.stop();  // 停止自动播放定时器
            }
            dispose();  // 关闭当前窗口
            new StartScreen().setVisible(true);  // 打开新的主菜单窗口
        });
        
        controlPanel.add(prevButton);
        controlPanel.add(moveSlider);
        controlPanel.add(nextButton);
        controlPanel.add(autoPlayButton);
        controlPanel.add(returnButton);
        
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void drawBoard(Graphics g) {
        // 绘制棋盘网格
        g.setColor(Color.BLACK);
        for (int i = 0; i < BOARD_SIZE; i++) {
            g.drawLine(CELL_SIZE, CELL_SIZE * (i + 1), 
                      CELL_SIZE * BOARD_SIZE, CELL_SIZE * (i + 1));
            g.drawLine(CELL_SIZE * (i + 1), CELL_SIZE, 
                      CELL_SIZE * (i + 1), CELL_SIZE * BOARD_SIZE);
        }
    }
    
    private void drawMoves(Graphics g) {
        for (int i = 0; i < currentMove; i++) {
            Move move = moves.get(i);
            int x = (move.getX() + 1) * CELL_SIZE;
            int y = (move.getY() + 1) * CELL_SIZE;
            
            g.setColor(move.isBlack() ? Color.BLACK : Color.WHITE);
            g.fillOval(x - CELL_SIZE/2, y - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
            if (!move.isBlack()) {
                g.setColor(Color.BLACK);
                g.drawOval(x - CELL_SIZE/2, y - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
            }
        }
    }
    
    private void showPreviousMove() {
        if (currentMove > 0) {
            currentMove--;
            moveSlider.setValue(currentMove);
            boardPanel.repaint();
        }
    }
    
    private void showNextMove() {
        if (currentMove < moves.size()) {
            currentMove++;
            moveSlider.setValue(currentMove);
            boardPanel.repaint();
        }
    }
    
    private void toggleAutoPlay() {
        if (autoPlayTimer != null && autoPlayTimer.isRunning()) {
            autoPlayTimer.stop();
        } else {
            autoPlayTimer = new Timer(1000, e -> {
                if (currentMove < moves.size()) {
                    showNextMove();
                } else {
                    autoPlayTimer.stop();
                }
            });
            autoPlayTimer.start();
        }
    }
    
    private List<Move> loadMoves() {
        List<Move> loadedMoves = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("game_records.dat"))) {
            loadedMoves = (List<Move>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loadedMoves;
    }
}
