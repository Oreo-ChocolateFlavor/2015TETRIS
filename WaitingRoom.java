import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by JaeSeung on 2016. 6. 3..
 */
public class WaitingRoom extends javax.swing.JFrame{
    public static WaitingRoom waitingRoom;  // to setvisibility
    public boolean isrunning;
    //room_info
    private room_info[] data;

    private javax.swing.JPanel main_jPanel;
    private javax.swing.JButton jButton_create_room;
    private javax.swing.JButton jButton_join_room;
    private javax.swing.JButton jButton_search;
    private javax.swing.JButton jButton_exit;
    private JTable table;
    private DefaultTableModel model;

    Socket sock;
    Socket new_sock;
    PrintWriter pw;
    BufferedReader br;
    DataOutputStream dout;
    DataInputStream din;

    public WaitingRoom(Socket sock) {
        this.waitingRoom = this;
        isrunning = true;

        //sock, read, write
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
        jButton_create_room = new javax.swing.JButton();
        jButton_join_room = new javax.swing.JButton();
        jButton_search = new javax.swing.JButton();
        jButton_exit = new javax.swing.JButton();

        //TABLE
        String[] field = {"Room Number", "Label", "Port number", "Number of User"};
        model = new DefaultTableModel(field, 0);
        table = new JTable(model);
        JScrollPane pane = new JScrollPane(table);

        //BUTTON Listener
        jButton_create_room.setText("Create Room");
        jButton_create_room
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jButton_create_roomActionPerformed(evt);
                    }
                });

        jButton_join_room.setText("Join Room");
        jButton_join_room
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jButton_join_roomActionPerformed(evt);
                    }
                });

        jButton_search.setText("Refresh Table");
        jButton_search
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jButton_searchActionPerformed(evt);
                    }
                });

        jButton_exit.setText("Exit");
        jButton_exit
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jButton_exitActionPerformed(evt);
                    }
                });

        //Layout Setting
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
                                        .addGap(18, 18, 18)
                                        .addComponent(pane,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                300,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(
                                                                jButton_create_room,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(
                                                                jButton_join_room,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(
                                                                jButton_search,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                        .addComponent(pane,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                500,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap()
                        )
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(30, 30, 30)
                                        .addComponent(
                                                jButton_create_room)
                                        .addPreferredGap(
                                                LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(
                                                jButton_join_room)
                                        .addPreferredGap(
                                                LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGap(30, 30, 30)
                                        .addComponent(
                                                jButton_search)
                                        .addPreferredGap(
                                                LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGap(30, 30, 30)
                                        //.addGap(180, 180, 180)
                                        .addComponent(
                                                jButton_exit))
                );
        setBounds(590, javax.swing.GroupLayout.PREFERRED_SIZE, 591, 428);   // set size of window

        this.setTitle("Waiting Room");
        this.add(main_jPanel);
        setResizable(false);// 창 크기 못바꾸게

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                try {
                    dout.writeByte(CreateRoom.CLOSE_MAINROOM_SIGNAL);
                    dout.flush();

                    isrunning = false;
                } catch (Exception err) {
                    // TODO Auto-generated catch block
                    JOptionPane.showMessageDialog(null, err.getMessage());
                }
            }
        });
    }

    // When "Create Room" button is clicked
    private void jButton_create_roomActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:jButton_create_roomActionPerformed
        // TODO add your handling code here:
        try {
            CreateRoom createRoom = new CreateRoom(sock);
            createRoom.setVisible(true);
        } catch (Exception err) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, err.getMessage());
        }

    }// GEN-LAST:jButton_create_roomActionPerformed

    // When "Join Room" button is clicked
    private void jButton_join_roomActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:jButton_join_roomActionPerformed
        // TODO add your handling code here:
        try {
            int row = table.getSelectedRow();   // Get number of row that you selected
            // Refresh Table to get new data
            dout.writeByte(CreateRoom.ROOMINFOSEND_SIGNAL);
            dout.flush();
            Client.read_line(din, new_sock);
            setRoom_info(Client.getData());
            //테이블에서 방정보 받아오기
            //키값 갖고오려면 아래 숫자 바꾸면 됨
            final String now_max = (String) model.getValueAt(row, 3);
            final String room_port = (String) model.getValueAt(row, 2);
            final String room_name = (String) model.getValueAt(row, 1);

            dout.writeByte(CreateRoom.JOINROOM_SIGNAL);
            dout.writeInt(Integer.parseInt(room_port));
            dout.flush();
            byte tmpbuf;
            tmpbuf = din.readByte();

            if(tmpbuf == CreateRoom.FULLL_ROOM_SIG)  // When Room is full
            {
                JOptionPane.showMessageDialog(null, room_name + "is Full!",
                        "참가 불가",JOptionPane.PLAIN_MESSAGE);
            }
            else if(tmpbuf ==CreateRoom.NO_EXIST_ROOM ) // When there is no selected Room now
            {
                JOptionPane.showMessageDialog(null, room_name + "There is no room you choose!",
                        "참가 불가",JOptionPane.PLAIN_MESSAGE);
            }
            else if(tmpbuf == CreateRoom.AVAIL_ROOM_SIG)
            {
                new_sock = Client.connect_gameserver(new_sock, Integer.parseInt(room_port), Client.getip());
                boolean join = true;
                GameRoom gameRoom = new GameRoom(new_sock, join);
                gameRoom.setRoomname(room_name);
                byte temp;
                temp = (byte)(Integer.parseInt(""+now_max.charAt(0)));
                gameRoom.setId(temp);

                //쓰레드로 실행
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        gameRoom.setVisible(true);
                    }
                });
            }
            else if(tmpbuf == CreateRoom.IS_NOW_PLAYING_SIG) // When selected Room is playing
            {
                JOptionPane.showMessageDialog(null, room_name + "is now playing!",
                        "참가 불가",JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception err) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, err.toString());
        }
    }// GEN-LAST:jButton_join_roomActionPerformed

    // When "Refresh Table" button is clicked
    private void jButton_searchActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:jButton_searchActionPerformed
        // TODO add your handling code here:
        try {
            dout.writeByte(CreateRoom.ROOMINFOSEND_SIGNAL);
            dout.flush();

            Client.read_line(din, new_sock);
            setRoom_info(Client.getData());
        } catch (Exception err) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, err.getMessage());
        }

    }// GEN-LAST:jButton_searchActionPerformed

    // When "Exit" button is clicked
    private void jButton_exitActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:jButton_exitActionPerformed
        // TODO add your handling code here:
        try {
            dout.writeByte(CreateRoom.CLOSE_MAINROOM_SIGNAL);
            dout.flush();

            isrunning = false;
        } catch (Exception err) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, err.getMessage());
        }
    }// GEN-LAST:jButton_exitActionPerformed

    // Refresh Table data
    public void setRoom_info(room_info[] data) {
        this.data = data;
        model.setRowCount(0);
        //TABLE initiate
        ArrayList<String[]> tmplist = new ArrayList<String[]>();
        for (int i = 0; i < data.length; i++) {
            String[] item = {"No. " + (i+1), data[i].name,
                    ""+ data[i].port, data[i].nowperson + "/" + data[i].maxperson};
            tmplist.add(item);
        }
        int size2 = tmplist.size();
        for (int i = 0; i < size2; i++) {
            model.addRow(tmplist.get(i));
        }
    }
}
