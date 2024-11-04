package User;

import java.awt.*;

public class UserListPad extends Panel { 
    public List userList = new List(10);

    public UserListPad() { 
        setLayout(new BorderLayout());
        
        // 设置列表的背景色和字体
        userList.setBackground(Color.WHITE); // 设置背景色
        userList.setFont(new Font("微软雅黑", Font.PLAIN, 14)); // 设置字体

        // 预添加一些示例用户
        for (int i = 1; i < 11; i++) { 
            userList.add(i + ". "); 
        }

        // 添加列表到面板中
        add(userList, BorderLayout.CENTER);
    }
}
