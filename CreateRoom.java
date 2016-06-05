import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by JaeSeung on 2016. 6. 4..
 */
public class CreateRoom extends javax.swing.JFrame{

    public static final int CREATEROOM_SIGNAL = -100;
    public static final int ROOMINFOSEND_SIGNAL = -101;
    public static final int ADDROOM_SIGNAL = -102;
    public static final int JOINROOM_SIGNAL = -103;
    public static final int CLOSE_MAINROOM_SIGNAL = -104;
    public static final int PORT_SIG = -105;
    public static final int CHANGE_OWNER_SIG = -106;
    
    public static final int BUF_SIZE = 1024;
    
    private javax.swing.JPanel main_jPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField_name;

    private javax.swing.JButton jButton_exit;

    Socket sock;
    Socket new_sock;
    PrintWriter pw;
    BufferedReader br;
    DataOutputStream dout;
    DataInputStream din;
    public CreateRoom(Socket sock) {
        this.sock = sock;

        this.sock = sock;
        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            dout = new DataOutputStream(sock.getOutputStream());
            din = new DataInputStream(sock.getInputStream());
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "IOException");
        }

        //GUI
        main_jPanel = new javax.swing.JPanel();
        jButton_exit = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        jTextField_name = new JTextField();
        jTextField_name.setText("");

        jLabel1.setText("방 제목 : ");

        jButton_exit = new javax.swing.JButton();
        jButton_exit.setText("방 만들기!");
        jButton_exit
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        String room_name = new String();
                        room_name = jTextField_name.getText();
                        
                        
                        try {
                            if(room_name.length() == 0) {
                                throw new Exception("방 제목을 입력해 주세요!");
                            }
                            else if(room_name.length() >= 20) {
                                throw new Exception("방 제목을 줄여주세요!");
                            }
                            for (char c : room_name.toCharArray()) {
                                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {}
                                else
                                    throw new Exception("영어로만 입력해주세요!");
                            }

/*
                            JOptionPane.showMessageDialog(null, "방 제목 : "
                                            + room_name + "\r\n만들기 성공!", "",
                                    JOptionPane.PLAIN_MESSAGE);
*/

                            jTextField_name.setEditable(false);

//                          pw.write(room_name.toCharArray(), 0, room_name.length());
                            
//                          pw.write((char)ROOMINFOSEND_SIGNAL);
                            dout.write(room_name.getBytes(StandardCharsets.US_ASCII), 0, room_name.getBytes().length);
                            dout.writeByte(ADDROOM_SIGNAL);
                            dout.flush();
                            
                            
                            //방이름 보내주어야 함
                            boolean join = false;
                            GameRoom gameRoom = new GameRoom(sock, join);
                            gameRoom.setRoomname(room_name);
                            //gameRoom.setVisible(true);

                            //쓰레드로 변경
                            java.awt.EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    gameRoom.setVisible(true);
                                }
                            });


                            //이 창 닫아야 함
                            dispose();
                            Client.read_line(din,new_sock);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "방 제목 : "
                                            + room_name + "\r\n등록실패!!\r\n" + e.getMessage(),
                                    " 등록실패", JOptionPane.PLAIN_MESSAGE);
                        }



                    }
                });

        //Layout
        javax.swing.GroupLayout main_jPanelLayout = new javax.swing.GroupLayout(
                main_jPanel);
        main_jPanel.setLayout(main_jPanelLayout);
        main_jPanelLayout
                .setVerticalGroup(main_jPanelLayout
                        .createParallelGroup(
                                GroupLayout.Alignment.LEADING)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(30, 30, 30)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel1,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(
                                                                jTextField_name,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        )
                                        .addGap(18, 18, 18)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(
                                                                jButton_exit,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                        ));
        main_jPanelLayout
                .setHorizontalGroup(main_jPanelLayout
                        .createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(30, 30, 30)
                                        .addComponent(jLabel1)
                                        .addContainerGap()
                                        .addComponent(jTextField_name,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap()
                        )
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(50, 50, 50)
                                        .addComponent(jButton_exit))
                );

        setBounds(500, javax.swing.GroupLayout.PREFERRED_SIZE, 200, 150);

        this.setTitle("Create Room");
        this.add(main_jPanel);
        setResizable(false);// 창 크기 못바꾸게
    }
}
