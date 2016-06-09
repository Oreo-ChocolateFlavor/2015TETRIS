import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by JaeSeung on 2016. 6. 3..
 */
public class GameRoom extends javax.swing.JFrame{

    //join
    boolean join;
    public static boolean start_game =false;
    private javax.swing.JPanel main_jPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    public Tetris tetris;
    public Tetris_display tetris2;
    public Tetris_display tetris3;
    public Tetris_display tetris4;
    public Tetris_display tetris5;
    public ArrayList<Tetris_display> array_tetris;
    //dummy
    private javax.swing.JButton jButton_exit;
    private javax.swing.JButton jButton_start;

    String roomname;

    Socket new_sock;
    PrintWriter pw;
    BufferedReader br;
    DataOutputStream dout;
    DataInputStream din;
    DataOutputStream dout_listen;
    DataInputStream din_listen;

    public byte id;
    public byte[][][] player_board;
    public GameRoom(Socket new_sock, boolean join) {
        //check join
        this.join = join;
        //new_sock, read, write
        this.new_sock = new_sock;

        array_tetris = new ArrayList<Tetris_display>();
        try {
            pw = new PrintWriter(new OutputStreamWriter(new_sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(new_sock.getInputStream()));
            dout = new DataOutputStream(new_sock.getOutputStream());
            din = new DataInputStream(new_sock.getInputStream());
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

        tetris = new Tetris();
        tetris.init();
        tetris.start();

        tetris2 = new Tetris_display();
        tetris2.init();
        tetris3 = new Tetris_display();
        tetris3.init();
        tetris4 = new Tetris_display();
        tetris4.init();
        tetris5 = new Tetris_display();
        tetris5.init();
        
        
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
								dout_listen.writeInt(new_sock.getPort());
								System.out.println("client_port = "+new_sock.getPort());
								dout_listen.flush();
								new_sock.close();
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
								dout_listen.writeInt(new_sock.getPort());
								dout_listen.flush();
								new_sock.close();
                        	} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            
                        }
                        else
                        {
                        	System.out.println("Exit room error");
                        }
                        try {
							new_sock.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        dispose();
                    }
                });

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                if(join == true)
                {
                    try {
                        dout.writeByte(CreateRoom.LEAVE_GAMEROOM_SIG);
                        dout.flush();

                        dout_listen.writeByte(CreateRoom.LEAVE_GAMEROOM_SIG);
                        dout_listen.writeInt(new_sock.getPort());
                        System.out.println("client_port = "+new_sock.getPort());
                        dout_listen.flush();
                    } catch (IOException err) {
                        // TODO Auto-generated catch block
                        err.printStackTrace();
                    }

                }
                else if(join == false)
                {
                    try {
                        dout.writeByte(CreateRoom.DESTORY_ROOM_SIG);
                        dout.flush();

                        dout_listen.writeByte(CreateRoom.DESTORY_ROOM_SIG);
                        dout_listen.writeInt(new_sock.getPort());
                        dout_listen.flush();
                    } catch (IOException err) {
                        // TODO Auto-generated catch block
                        err.printStackTrace();
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
        if(join==true)
        {
        	jButton_start.setEnabled(false);
        }
        jButton_start
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                    	byte start_tmp = 0;
                    	start_game = true;
                    	
                    	if(!join) {
                        	
                        	System.out.println("point in not join");
                            try {
                            	dout.writeByte(CreateRoom.HOST_GAMESTART_SIG);
                            	dout.flush();
                            	dout_listen.writeByte(CreateRoom.HOST_GAMESTART_SIG);
                            	dout_listen.writeInt(new_sock.getPort());
                            	dout_listen.flush();
                            	
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            
                            try {
								start_tmp = din.readByte();
								if(start_tmp == CreateRoom.HOST_GAMESTART_SIG)
								{
									TimerTask mytask = new SendTimer();
							        java.util.Timer t = new java.util.Timer(true);
							        t.schedule(mytask, 1000);
									tetris.newGame();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            
                            jButton_start.addKeyListener(tetris.getKey_listener());

                        }
                        else
                        {
                        	System.out.println("point in join");
                        	try {
								start_tmp = din.readByte();
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(start_tmp == CreateRoom.HOST_GAMESTART_SIG)
							{
								TimerTask mytask = new SendTimer();
						        java.util.Timer t = new java.util.Timer(true);
						        t.schedule(mytask, 1000);
								tetris.newGame();
								jButton_start.addKeyListener(tetris.getKey_listener());
							}
							
                        }
                        

                    }
                });

        


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
        
        
        boolean test = GameRoom.start_game;
        
//        TimerTask mytask = new SendTimer();
//        java.util.Timer t = new java.util.Timer(true);
//        t.schedule(mytask, 1000);
        if(join)
        {
        	guest_gamestart g_game = new guest_gamestart();
            g_game.start();	
        }
        
    }

    public class guest_gamestart extends Thread
    {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			byte temp = 0;
        	try {
        			temp = din.readByte();		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if(temp == CreateRoom.HOST_GAMESTART_SIG&& join)
        	{
        		TimerTask mytask = new SendTimer();
		        java.util.Timer t = new java.util.Timer(true);
		        t.schedule(mytask, 500);
				tetris.newGame();
        	}
		}
    	
    }

    public void update_table(Tetris_display tetris, int[][] array) {
        tetris.init();
        tetris.setGrid(array);
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
        this.setTitle(roomname);
    }

    public void setId(byte id) {
        this.id = id;
    }
    
    private class RecvTimer extends TimerTask
    {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
					
		}
    	
    }
    private class SendTimer extends TimerTask
    {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[][] temp_array = new byte[18][10];
            int[][] temp_int_array = new int[18][10];
            
            System.out.println("point101");
            while(true)
            {
            	if(tetris.game_flag == true)
            	{
            		try {
            			dout.writeByte(CreateRoom.GAME_OVER_SIG);
            			dout.write(id);
            			dout.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		break;
            	}
            	try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	 temp_int_array = tetris.getgrid();
            	 for(int i=0; i<18; i++)
            	 {
            		 for(int j=0; j<10; j++)
            		 {
            			 if(temp_int_array[i][j]>=0)
            			 {
            				 temp_array[i][j] = (byte)1;
            			 }
            			 else
            			 {
            				 temp_array[i][j] = (byte)0;
            			 }
            		 }
            
            	 }
            	 //System.out.println("copy success! 0 0 :"+temp_array[0][0]);
            	 for(int i=0; i<18; i++)
            	 {
            		 try {
						dout.write(temp_array[i]);
						dout.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	 
            	 }
            	 try {
					dout.writeByte(CreateRoom.GAMEBOARD_UPDATE_SIG);
					dout.writeByte(id);
					System.out.println("id = "+id);
					dout.flush();
					array_tetris.add(0, tetris2);
					array_tetris.add(1, tetris3);
					array_tetris.add(2, tetris4);
					array_tetris.add(3, tetris5);
					
					player_board = new byte[5][18][10];
					int[][][] player_board_int = new int[5][18][10];
					
					byte id1,id2,id3,id4,id5;
					
					byte[][] temp_board = new byte[5][180];
					
					byte temp_buf = (byte)0;
					
					din.read(temp_board[0], 0, 180);
					id1 = din.readByte();
					
					din.read(temp_board[1], 0, 180);
					id2 = din.readByte();
					
					din.read(temp_board[2], 0, 180);
					id3 = din.readByte();
					
					din.read(temp_board[3], 0, 180);
					id4 = din.readByte();
					
					din.read(temp_board[4], 0, 180);
					id5 = din.readByte();
					
					
					for(int i=0; i<5; i++)
					{
					
					for(int j=0; j<18; j++)
					{
						for(int k=0; k<10; k++)
						{
							if(temp_board[i][j*10 + k]==1)
								player_board_int[i][j][k]=1;
							else
								player_board_int[i][j][k]=-1;
							
						}
					}
					}
					
					
//					for(int i=0; i<5; i++)
//					{
//						for(int j=0; j<18; j++)
//						{
//							for(int k=0; k<10; k++)
//							{
//								if(player_board[i][j][k]==1)
//								{
//									player_board_int[i][j][k]=1;
//								}
//								else
//								{
//									player_board_int[i][j][k]=-1;
//								}
//							}
//						}
//					}
					
					
//					for(int i=0; i<18; i++)
//					{
//						for(int j=0; j<10; j++)
//						{
//							System.out.print(player_board_int[0][i][j] + " ");
//						}
//						
//						System.out.println("");
//					}
//
					
					
					
					
					int index=0;
					for(int i=0; i<5; i++)
					{
						if(i==id)
						{
							continue;
						}
						else
						{
							array_tetris.get(index).init();
							array_tetris.get(index).setGrid(player_board_int[i]);
							index++;
						}
						
					}
			
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }	
		}
    	
    }

}
