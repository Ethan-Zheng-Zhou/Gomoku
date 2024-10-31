package Chess;
import java.awt.*;

import User.FIRPad; 

public class FIRPointWhite extends Canvas { 
    FIRPad padBelonged;    // 白棋所属的棋盘 

    public FIRPointWhite(FIRPad padBelonged) { 
        setSize(20, 20); 
        this.padBelonged = padBelonged; 
    } 

    public void paint(Graphics g) { 
        // 画棋子 
    } 
}
