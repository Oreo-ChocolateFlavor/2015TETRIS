import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by JaeSeung on 2016. 6. 4..
 */
public class CreateRoom extends javax.swing.JFrame{

    public static final byte CREATEROOM_SIGNAL = -100;
    public static final byte ROOMINFOSEND_SIGNAL = -101;
    public static final byte ADDROOM_SIGNAL = -102;
    public static final byte JOINROOM_SIGNAL = -103;
    public static final byte CLOSE_MAINROOM_SIGNAL = -104;
    public static final byte PORT_SIG = -105;
    public static final byte CHANGE_OWNER_SIG = -106;
    public static final byte DESTORY_ROOM_SIG = -107;
    public static final byte FULLL_ROOM_SIG = -108;
    public static final byte NO_EXIST_ROOM = -109;
    public static final byte AVAIL_ROOM_SIG = -110;
    public static final byte LEAVE_GAMEROOM_SIG = -111;
    public static final byte HOST_GAMESTART_SIG = -112;
    public static final byte GAMEBOARD_UPDATE_SIG = -113;
    public static final byte IS_NOW_PLAYING_SIG = -114;
    public static final byte GAME_OVER_SIG = -115;

    //public static final byte 
    public static final int BUF_SIZE = 1024;

    private javax.swing.JPanel main_jPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField_name;

    private javax.swing.JButton jButton_exit;

    Socket sock;
    Socket new_sock;
    DataOutputStream dout;
    DataInputStream din;
    public CreateRoom(Socket sock) {
        this.sock = sock;

        try {
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

                            //방정보 시그널을 보낸 후 방정보에 관한 정보를 보낸다.
                            dout.writeByte(CREATEROOM_SIGNAL);
                            dout.flush();
                            jTextField_name.setEditable(false);

                            dout.write(room_name.getBytes(StandardCharsets.US_ASCII), 0, room_name.getBytes().length);
                            dout.writeByte(ADDROOM_SIGNAL);
                            dout.flush();

                            //game_server소켓에 연결하는 부분.
                            new_sock = Client.read_line(din,new_sock);
                            boolean join = false;
                            if(new_sock==null)
                                System.out.println("new_sock is null");
                            GameRoom gameRoom = new GameRoom(new_sock, join);
                            gameRoom.setRoomname(room_name);
                            gameRoom.setId((byte)0);

                            //쓰레드로 실행
                            java.awt.EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    gameRoom.setVisible(true);
                                }
                            });

                            // close this CreateRoom after make GameRoom
                            dispose();
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

        setBounds(500, javax.swing.GroupLayout.PREFERRED_SIZE, 200, 150);   // Set CreateRoom's window size

        this.setTitle("Create Room");
        this.add(main_jPanel);
        setResizable(false);// 창 크기 못바꾸게
    }
}