package User;

import java.awt.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*; 
  
import javax.swing.*;

import Chess.FIRPointBlack;
import Chess.FIRPointWhite;
import Chess.FIRThread; 
  
public class FIRPad extends JPanel implements MouseListener, ActionListener { 
    // 保持原有的成员变量
    public boolean isMouseEnabled = false; 
    public boolean isWinned = false; 
    public boolean isGaming = false; 
    public int chessX_POS = -1; 
    public int chessY_POS = -1; 
    public int chessColor = 1; 
    public int chessBlack_XPOS[] = new int[200]; 
    public int chessBlack_YPOS[] = new int[200]; 
    public int chessWhite_XPOS[] = new int[200]; 
    public int chessWhite_YPOS[] = new int[200]; 
    public int chessBlackCount = 0; 
    public int chessWhiteCount = 0; 
    public int chessBlackVicTimes = 0; 
    public int chessWhiteVicTimes = 0; 
    public Socket chessSocket; 
    public DataInputStream inputData; 
    public DataOutputStream outputData; 
    public String chessSelfName = null; 
    public String chessPeerName = null; 
    public String host = null; 
    public int port = 8888; 
    public JTextField statusText;
    public FIRThread firThread;
    
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 35;
    private static final int MARGIN = 40;
    
    public FIRPad() { 
        setLayout(null);
        setBackground(new Color(222, 184, 135));
        
        // 创建状态文本框
        statusText = new JTextField("请连接服务器！");
        statusText.setEditable(false);
        statusText.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statusText.setHorizontalAlignment(JTextField.CENTER);
        statusText.setBounds(MARGIN, 5, (BOARD_SIZE + 1) * CELL_SIZE - 2 * MARGIN, 24);
        add(statusText);
        
        // 设置面板大小
        int totalWidth = (BOARD_SIZE + 2) * CELL_SIZE;
        int totalHeight = (BOARD_SIZE + 3) * CELL_SIZE;
        setPreferredSize(new Dimension(totalWidth, totalHeight));
        setMinimumSize(new Dimension(totalWidth, totalHeight));
        
        addMouseListener(this);
        firThread = new FIRThread(this);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);
    }
    
    private void drawBoard(Graphics g) {
        // 使用抗锯齿
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制棋盘背景
        g.setColor(new Color(222, 184, 135));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // 绘制棋盘线
        g.setColor(Color.BLACK);
        for (int i = 0; i <= BOARD_SIZE; i++) {
            // 横线
            g.drawLine(MARGIN, i * CELL_SIZE + MARGIN, 
                      BOARD_SIZE * CELL_SIZE + MARGIN, i * CELL_SIZE + MARGIN);
            // 竖线
            g.drawLine(i * CELL_SIZE + MARGIN, MARGIN,
                      i * CELL_SIZE + MARGIN, BOARD_SIZE * CELL_SIZE + MARGIN);
        }
        
        // 绘制天元和星位
        int dotSize = 6;
        g.fillOval(8 * CELL_SIZE + MARGIN - dotSize/2, 
                   8 * CELL_SIZE + MARGIN - dotSize/2, 
                   dotSize, dotSize);
        int[] starPoints = {4, 4, 4, 12, 12, 4, 12, 12, 8, 4, 4, 8, 12, 8, 8, 12};
        for (int i = 0; i < starPoints.length; i += 2) {
            g.fillOval(starPoints[i] * CELL_SIZE + MARGIN - dotSize/2,
                      starPoints[i+1] * CELL_SIZE + MARGIN - dotSize/2, 
                      dotSize, dotSize);
        }
    }
    
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int pieceSize = (int)(CELL_SIZE * 0.8);
        
        // 绘制黑棋
        g.setColor(Color.BLACK);
        for (int i = 0; i < chessBlackCount; i++) {
            int x = chessBlack_XPOS[i];
            int y = chessBlack_YPOS[i];
            g.fillOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize);
        }
        
        // 绘制白棋
        for (int i = 0; i < chessWhiteCount; i++) {
            int x = chessWhite_XPOS[i];
            int y = chessWhite_YPOS[i];
            // 先绘制白色填充
            g.setColor(Color.WHITE);
            g.fillOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize);
            // 再绘制黑色边框
            g.setColor(Color.BLACK);
            g.drawOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize);
        }
    }

    // 保持原有的其他方法，但修改坐标计算方式
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON1_MASK && isMouseEnabled) {
            int x = Math.round((float)(e.getX() - MARGIN) / CELL_SIZE);
            int y = Math.round((float)(e.getY() - MARGIN) / CELL_SIZE);
            
            if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
                paintFirPoint(x, y, chessColor);
            }
        }
    }
    
    // 修改绘制棋子的方法
    public void paintFirPoint(int xPos, int yPos, int chessColor) {
        if (chessColor == 1 && isMouseEnabled) {
            setLocation(xPos, yPos, chessColor);
            isWinned = checkVicStatus(xPos, yPos, chessColor);
            
            if (!isWinned) {
                firThread.sendMessage("/" + chessPeerName + " /chess " + xPos + " " + yPos + " " + chessColor);
                statusText.setText("黑(第" + chessBlackCount + "步)" + xPos + " " + yPos + ",轮到白方.");
                isMouseEnabled = false;
            } else {
                firThread.sendMessage("/" + chessPeerName + " /chess " + xPos + " " + yPos + " " + chessColor);
                setVicStatus(1);
                isMouseEnabled = false;
            }
            repaint();
        } else if (chessColor == -1 && isMouseEnabled) {
            setLocation(xPos, yPos, chessColor);
            isWinned = checkVicStatus(xPos, yPos, chessColor);
            
            if (!isWinned) {
                firThread.sendMessage("/" + chessPeerName + " /chess " + xPos + " " + yPos + " " + chessColor);
                statusText.setText("白(第" + chessWhiteCount + "步)" + xPos + " " + yPos + ",轮到黑方.");
                isMouseEnabled = false;
            } else {
                firThread.sendMessage("/" + chessPeerName + " /chess " + xPos + " " + yPos + " " + chessColor);
                setVicStatus(-1);
                isMouseEnabled = false;
            }
            repaint();
        }
    }
    
    // 修改网络棋子绘制方法
    public void paintNetFirPoint(int xPos, int yPos, int chessColor) {
        setLocation(xPos, yPos, chessColor);
        isWinned = checkVicStatus(xPos, yPos, chessColor);
        
        if (chessColor == 1) {
            if (!isWinned) {
                statusText.setText("黑(第" + chessBlackCount + "步)" + xPos + " " + yPos + ",轮到白方.");
                isMouseEnabled = true;
            } else {
                firThread.sendMessage("/" + chessPeerName + " /victory " + chessColor);
                setVicStatus(1);
                isMouseEnabled = true;
            }
        } else {
            if (!isWinned) {
                statusText.setText("白(第" + chessWhiteCount + "步)" + xPos + " " + yPos + ",轮到黑方.");
                isMouseEnabled = true;
            } else {
                firThread.sendMessage("/" + chessPeerName + " /victory " + chessColor);
                setVicStatus(-1);
                isMouseEnabled = true;
            }
        }
        repaint();
    }

    // 保持其他方法不变...
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void actionPerformed(ActionEvent e) {}

    public boolean connectServer(String ServerIP, int ServerPort) throws Exception {
        try {
            // 取得主机端口 
            chessSocket = new Socket(ServerIP, ServerPort); 
            // 取得输入流 
            inputData = new DataInputStream(chessSocket.getInputStream()); 
            // 取得输出流 
            outputData = new DataOutputStream(chessSocket.getOutputStream()); 
            firThread.start();
            return true;
        } catch (IOException ex) {
            statusText.setText("连接失败!");
        }
        return false;
    }

    public void setVicStatus(int vicChessColor) {
        // 清空棋盘
        removeAll();
        // 将黑棋的位置设置到零点
        for (int i = 0; i <= chessBlackCount; i++) {
            chessBlack_XPOS[i] = 0;
            chessBlack_YPOS[i] = 0;
        }
        // 将白棋的位置设置到零点
        for (int i = 0; i <= chessWhiteCount; i++) {
            chessWhite_XPOS[i] = 0;
            chessWhite_YPOS[i] = 0;
        }
        // 清空棋盘上的黑棋数
        chessBlackCount = 0;
        // 清空棋盘上的白棋数
        chessWhiteCount = 0;
        add(statusText);
        statusText.setBounds(40, 5, 360, 24);
        if (vicChessColor == 1) { // 黑棋胜
            chessBlackVicTimes++;
            statusText.setText("黑方胜,黑:白 " + chessBlackVicTimes + ":" + chessWhiteVicTimes
                    + ",游戏重启,等待白方...");
        } else if (vicChessColor == -1) { // 白棋胜
            chessWhiteVicTimes++;
            statusText.setText("白方胜,黑:白 " + chessBlackVicTimes + ":" + chessWhiteVicTimes
                    + ",游戏重启,等待黑方...");
        }
        repaint();
    }

    public void setLocation(int xPos, int yPos, int chessColor) {
        if (chessColor == 1) { // 棋子为黑棋时
            chessBlack_XPOS[chessBlackCount] = xPos * CELL_SIZE + MARGIN;
            chessBlack_YPOS[chessBlackCount] = yPos * CELL_SIZE + MARGIN;
            chessBlackCount++;
        } else if (chessColor == -1) { // 棋子为白棋时
            chessWhite_XPOS[chessWhiteCount] = xPos * CELL_SIZE + MARGIN;
            chessWhite_YPOS[chessWhiteCount] = yPos * CELL_SIZE + MARGIN;
            chessWhiteCount++;
        }
    }

    public boolean checkVicStatus(int xPos, int yPos, int chessColor) {
        int count;
        int[][] directions = {
            {1, 0},  // 水平
            {0, 1},  // 垂直
            {1, 1},  // 主对角线
            {1, -1}  // 副对角线
        };
        
        for (int[] dir : directions) {
            count = 1;
            // 正向检查
            for (int i = 1; i <= 4; i++) {
                int newX = xPos + dir[0] * i;
                int newY = yPos + dir[1] * i;
                if (!isValidPosition(newX, newY)) break;
                if (!hasSameColorPiece(newX, newY, chessColor)) break;
                count++;
            }
            // 反向检查
            for (int i = 1; i <= 4; i++) {
                int newX = xPos - dir[0] * i;
                int newY = yPos - dir[1] * i;
                if (!isValidPosition(newX, newY)) break;
                if (!hasSameColorPiece(newX, newY, chessColor)) break;
                count++;
            }
            if (count >= 5) return true;
        }
        return false;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    private boolean hasSameColorPiece(int x, int y, int targetColor) {
        if (targetColor == 1) {
            for (int i = 0; i < chessBlackCount; i++) {
                if (chessBlack_XPOS[i] == (x + 1) * CELL_SIZE && 
                    chessBlack_YPOS[i] == (y + 1) * CELL_SIZE) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < chessWhiteCount; i++) {
                if (chessWhite_XPOS[i] == (x + 1) * CELL_SIZE && 
                    chessWhite_YPOS[i] == (y + 1) * CELL_SIZE) {
                    return true;
                }
            }
        }
        return false;
    }
} 