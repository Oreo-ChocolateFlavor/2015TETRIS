import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by JaeSeung on 2016. 6. 3..
 */
public class WaitingRoom extends javax.swing.JFrame{

    public static final int CREATEROOM_SIGNAL = -100;
    public static final int ROOMINFOSEND_SIGNAL = -101;
    public static final int JOINROOM_SIGNAL = -103;
    public static final int CLOSE_MAINROOM_SIGNAL = -104;

    public boolean isrunning;

    private javax.swing.JPanel main_jPanel;
    private javax.swing.JButton jButton_create_room;
    private javax.swing.JButton jButton_join_room;
    private javax.swing.JButton jButton_exit;
    private JTable table;
    private DefaultTableModel model;

    Socket sock;
    PrintWriter pw;
    BufferedReader br;
    DataOutputStream dout;
    DataInputStream din;

    public WaitingRoom(Socket sock) {

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

        jButton_exit.setText("Exit");
        jButton_exit
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jButton_exitActionPerformed(evt);
                    }
                });

        //TABLE initiate
        ArrayList<String[]> tmplist = new ArrayList<String[]>();
        for (int i = 0; i < 30; i++) {
            String[] item = {"Room " + i,
                    "Name " + i, "Port " + i, "User " + i};
            tmplist.add(item);
        }
        int size2 = tmplist.size();
        for (int i = 0; i < size2; i++) {
            model.addRow(tmplist.get(i));
        }

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
                                        .addGap(180, 180, 180)
                                        .addComponent(
                                                jButton_exit))
                );

        setBounds(590, javax.swing.GroupLayout.PREFERRED_SIZE, 591, 428);

        this.setTitle("Waiting Room");
        this.add(main_jPanel);
        setResizable(false);// 창 크기 못바꾸게

    }

    private void jButton_create_roomActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:jButton_create_roomActionPerformed
        // TODO add your handling code here:
        try {
        	dout.writeByte(CREATEROOM_SIGNAL);
            dout.flush();

            CreateRoom createRoom = new CreateRoom(sock);
            
            
            createRoom.setVisible(true);
          //  Client.read_line(din);
            
            

            /*java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    gameRoom.setVisible(true);
                }
            });
            */
        } catch (Exception err) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, err.getMessage());
        }

    }// GEN-LAST:jButton_create_roomActionPerformed

    private void jButton_join_roomActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:jButton_join_roomActionPerformed
        // TODO add your handling code here:
        try {
        	dout.writeByte(JOINROOM_SIGNAL);
            dout.flush();

            boolean join = true;
            GameRoom gameRoom = new GameRoom(sock, join);
            gameRoom.setVisible(true);

            /*java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    gameRoom.setVisible(true);
                }
            });
            */
        } catch (Exception err) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, err.getMessage());
        }

    }// GEN-LAST:jButton_join_roomActionPerformed

    private void jButton_exitActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:jButton_exitActionPerformed
        // TODO add your handling code here:
        try {
        	dout.writeByte(CLOSE_MAINROOM_SIGNAL);
            dout.flush();

            isrunning = false;

        } catch (Exception err) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, err.getMessage());

        }

    }// GEN-LAST:jButton_exitActionPerformed

/*

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new WaitingRoom().setVisible(true);
            }
        });

    }
*/

}
