package User;

import java.awt.*;

public class UserListPad extends Panel { 
    public List userList = new List(10);

    public UserListPad() { 
        setLayout(new BorderLayout());
        
        // �����б�ı���ɫ������
        userList.setBackground(Color.WHITE); // ���ñ���ɫ
        userList.setFont(new Font("΢���ź�", Font.PLAIN, 14)); // ��������

        // Ԥ���һЩʾ���û�
        for (int i = 1; i < 11; i++) { 
            userList.add(i + ". "); 
        }

        // ����б������
        add(userList, BorderLayout.CENTER);
    }
}
