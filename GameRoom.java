import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * Created by JaeSeung on 2016. 6. 3..
 */
public class GameRoom extends javax.swing.JFrame{

    //join
    boolean join;

    private javax.swing.JPanel main_jPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;

    //dummy
    private javax.swing.JButton jButton_exit;
    private javax.swing.JButton jButton_start;

    String roomname;

    Socket sock;
    PrintWriter pw;
    BufferedReader br;
    DataOutputStream dout;
    DataInputStream din;
    DataOutputStream dout_listen;
    DataInputStream din_listen;

    public GameRoom(Socket sock, boolean join) {
        //check join
        this.join = join;
        //sock, read, write
        this.sock = sock;

        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            dout = new DataOutputStream(sock.getOutputStream());
            din = new DataInputStream(sock.getInputStream());
            dout_listen = new DataOutputStream(Client.sock.getOutputStream());
            din_listen = new DataInputStream(Client.sock.getInputStream());
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "IOException");
        }

        main_jPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        jLabel1.setText("Player1");
        jLabel2.setText("Player2");
        jLabel3.setText("Player3");
        jLabel4.setText("Player4");
        jLabel5.setText("Player5");

        roomname = new String();

        //join 값 줘서 변경해야함
        //시작은 방장만 할 수 있도록

        Tetris tetris = new Tetris();
        tetris.init();
        tetris.start();

        Tetris_display tetris2 = new Tetris_display();
        tetris2.init();
        int array[][] = new int[18][10];
        for(int i=0; i<9; i++)
        {
            for(int j=0; j<10; j++)
            {
                array[i][j] = -1;
            }
        }
        for(int i=9; i<18; i++)
        {
            for(int j=0; j<10; j++)
            {
                array[i][j] = 3;
            }
        }
        tetris2.setGrid(array);

        jButton_exit = new javax.swing.JButton();
        jButton_exit.setText("Exit");
        jButton_exit
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        for(int i=0; i<9; i++)
                            {
                                for(int j=0; j<10; j++)
                                {
                                    array[i][j] = 0;
                                }
                            }
                            for(int i=9; i<18; i++)
                            {
                                for(int j=0; j<10; j++)
                                {
                                    array[i][j] = 0;
                                }
                            }
                        update_table(tetris2, array);
                        if(join == true)
                        {
                        	try {
								dout.writeByte(CreateRoom.LEAVE_GAMEROOM_SIG);
								dout.flush();

								dout_listen.writeByte(CreateRoom.LEAVE_GAMEROOM_SIG);
								dout_listen.writeByte(sock.getPort());
								System.out.println("client_port = "+sock.getPort());
								dout_listen.flush();
                        	} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            
                        }
                        else if(join == false)
                        {
                        	try {
								dout.writeByte(CreateRoom.DESTORY_ROOM_SIG);
								dout.flush();

								dout_listen.writeByte(CreateRoom.DESTORY_ROOM_SIG);
								dout_listen.writeByte(sock.getPort());
								dout_listen.flush();
                        	} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            
                        }
                        else
                        {
                        	System.out.println("Exit room error");
                        }
                        dispose();
                    }
                });

        jButton_start = new javax.swing.JButton();
        jButton_start.setText("Start");
        jButton_start
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {

                        if(!join) {
                            //tetris.newGame();
                            //tetris.start();

                            /*try {
                                dout.writeByte(CreateRoom.LEAVE_GAMEROOM_SIG);
                                dout.flush();

                                dout_listen.writeByte(CreateRoom.LEAVE_GAMEROOM_SIG);
                                dout_listen.writeByte(sock.getPort());
                                System.out.println("client_port = "+sock.getPort());
                                dout_listen.flush();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }*/
                        }

                    }
                });

        Tetris_display tetris3 = new Tetris_display();
        tetris3.init();
        Tetris_display tetris4 = new Tetris_display();
        tetris4.init();
        Tetris_display tetris5 = new Tetris_display();
        tetris5.init();


        javax.swing.GroupLayout main_jPanelLayout = new javax.swing.GroupLayout(main_jPanel);
        main_jPanel.setLayout(main_jPanelLayout);
        main_jPanelLayout
                .setVerticalGroup(main_jPanelLayout
                        .createParallelGroup(
                                GroupLayout.Alignment.LEADING)
                        .addGap(18, 18, 18)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(tetris,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                500,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel1)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup()
                                                        .addComponent(jButton_exit)
                                                        .addComponent(jButton_start)
                                        )
                        )
                        .addGap(18, 18, 18)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(tetris2,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                200,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel2)
                                        .addGap(18, 18, 18)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(tetris4,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                200,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jLabel4)
                                        )
                        )
                        .addGap(18, 18, 18)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(tetris3,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                200,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel3)
                                        .addGap(18, 18, 18)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(tetris5,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                200,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jLabel5)
                                        )

                        )


                );
        main_jPanelLayout
                .setHorizontalGroup(main_jPanelLayout
                        .createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                main_jPanelLayout
                                        .createSequentialGroup()
                                        .addGap(30, 30, 30)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup()
                                                        .addGap(30, 30, 30)
                                                        .addComponent(tetris,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                250,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jLabel1)
                                                        .addGroup(
                                                                main_jPanelLayout
                                                                        .createSequentialGroup()
                                                                        .addComponent(jButton_exit)
                                                                        .addComponent(jButton_start)
                                                        )
                                        )
                                        .addGap(30, 30, 30)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup()
                                                        .addComponent(tetris2,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel2)
                                                        .addGap(30, 30, 30)
                                                        .addGroup(
                                                                main_jPanelLayout
                                                                        .createParallelGroup()
                                                                        .addComponent(tetris4,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                100,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(jLabel4)
                                                        )
                                        )
                                        .addGap(30, 30, 30)
                                        .addGroup(
                                                main_jPanelLayout
                                                        .createParallelGroup()
                                                        .addComponent(tetris3,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel3)
                                                        .addGroup(
                                                                main_jPanelLayout
                                                                        .createParallelGroup()
                                                                        .addComponent(tetris5,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                100,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(18, 18, 18)
                                                                        .addComponent(jLabel5)
                                                        )
                                        )


                                        .addContainerGap()
                        )
                );

        setBounds(0, javax.swing.GroupLayout.PREFERRED_SIZE, 1000, 700);

        //this.setTitle("Game Room");
        this.setTitle(roomname);
        this.add(main_jPanel);
        setResizable(false);// 창 크기 못바꾸게

    }


    public void update_table(Tetris_display tetris, int[][] array) {
        tetris.init();
        tetris.setGrid(array);
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
        this.setTitle(roomname);
        }
}