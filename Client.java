import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;



public class Client {

	private static room_info[] data;
	public static Socket sock = null;
	private static String host_ip; //To store host_ip
	
	public static room_info[] getData() {
		return data;
	}

	public static void setData(room_info[] data) {
		Client.data = data;
	}

	public static String getip()
	{
		return host_ip;
	}
	
	public static Socket connect_gameserver(Socket sock, int port, String ip) //connecting client to game_server
	{
		try {
			System.out.println("IP: "+ip +" port: "+port);
			sock = new Socket(ip, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sock;
	}
	
	public static Socket read_line(DataInputStream din, Socket new_sock) //read and check by the signal
	{
		
		byte[] buf = new byte[CreateRoom.BUF_SIZE];
		byte tmpbuf;
		int index=0;
		try {
			for(int i=0; i<CreateRoom.BUF_SIZE; i++)
			{
				tmpbuf = din.readByte();
		
				if(tmpbuf < 0)
				{
					System.out.println("Get signal "+(int)tmpbuf);
					if((int)tmpbuf == CreateRoom.PORT_SIG)
					{
						System.out.println("Test point");
						byte[] tmp4 = new byte[4];
						tmp4[0] = buf[0];
						tmp4[1] = buf[1];
						tmp4[2] = buf[2];
						tmp4[3] = buf[3];
						int tmp = byteToint(tmp4);
						System.out.println("goTTA port: " + tmp);
						new_sock = connect_gameserver(new_sock, tmp, host_ip);
						System.out.println("connect success! to IP: "+host_ip+"port: "+tmp);
					}
					else if((int)tmpbuf == CreateRoom.ROOMINFOSEND_SIGNAL)
					{
						index = 0;
						for(int q=0; q<CreateRoom.BUF_SIZE; q++)
						{
							
							tmpbuf = din.readByte();
							if(tmpbuf  == CreateRoom.ROOMINFOSEND_SIGNAL)
							{
								System.out.println("test");
								
								int j=(index+1)/64;
								setData(new room_info[j]);
								String tmp_name = "";
								int tmp_port = 0;
								int tmp_maxperson =0;
								int tmp_nowperson =0;
								byte[] tmp_byte = new byte[4];
								for(int k=0; k<j; k++)
								{
									tmp_name ="";
									for(int l=0; l<52; l++)
									{
										tmp_name = tmp_name + Character.toString((char)buf[l+k*68]); 
									}
									
									System.out.println("JAVA " + tmp_name);
									
									for(int l=0; l<4; l++)
									{
										tmp_byte[l] = buf[l+52+k*68];
										System.out.println("l="+l);
									}
									

									System.out.println("you2 " + tmp_byte[0] + "  "+tmp_byte[1] + "  "+tmp_byte[2] + "  "+tmp_byte[3]);
									tmp_port = byteToint(tmp_byte);
									System.out.println("you");
									
									for(int l=0; l<4; l++)
									{
										tmp_byte[l] = buf[l+56+k*68];
									}
									tmp_maxperson = byteToint(tmp_byte);
									System.out.println("FUCK");
									
									for(int l=0; l<4; l++)
									{
										tmp_byte[l] = buf[l+60+k*68];
									}
									tmp_nowperson = byteToint(tmp_byte);
									System.out.println("HEEDONG");
									for(int l=0; l<4; l++)
									{
										tmp_byte[l] = buf[l+64+k*68];
									}
									getData()[k] = new room_info(tmp_name, tmp_port, tmp_maxperson, tmp_nowperson);
									System.out.println(k+"th name:"+tmp_name+"port"+tmp_port+"max"+tmp_maxperson+"now"+tmp_nowperson);
								}	
								
								break;
							}
							else
							{
								
								System.out.println(index);
								index = q+1;
								buf[q] = tmpbuf;
							}
						}
					}
					else if((int)tmpbuf == CreateRoom.CHANGE_OWNER_SIG)
					{
						
					}
					else if((int)tmpbuf == CreateRoom.HOST_GAMESTART_SIG)
					{
						
					}
					index = i;
					break;
				}
				else
				{
					buf[i] = tmpbuf;
				}
				
				
			}
		
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] rtn = new byte[++index];
		return new_sock;
		
	}

	public static int byteToint(byte[] arr)
	{
		ByteBuffer buff =  ByteBuffer.allocate(Integer.SIZE/8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		
		buff.put(arr);
		buff.flip();
		
		return buff.getInt();
		
		
	}
    public static void main(String[] args){

        if(args.length != 2){
            System.out.println("사용법 : java ChatClient id 접속할 서버 ip");
            System.exit(1);
        }

       
        Socket dummy_sock = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        DataOutputStream dout;
        DataInputStream din;
        boolean endflag = false;

        try{
            /******************************************************************

             입력받은 ip로 10001번 포트에 접속( args[0] : id, args[1] : 서버 ip)
             1. 서버에 접속하기 위해 Socket 생성하고,
             Socket으로부터 InputStream과 OutputStream을  얻어와서
             각각 Buffered와 PrintWriter 형태로 변환시킴

             ******************************************************************/

        	host_ip = new String(args[1]);
            sock = new Socket(args[1], 1818);
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            dout = new DataOutputStream(sock.getOutputStream());
            din = new DataInputStream(sock.getInputStream());
            /******************************************************************

             2. 키보드로부터 입력받기 위한 BufferedReader를 생성한 후,
             서버로부터 전달된 문자열을 모니터에 출력하는 InputThread 객체를 생성

             ******************************************************************/


             //make WaitingRoom
            dout.writeByte(CreateRoom.ROOMINFOSEND_SIGNAL);
            dout.flush(); 
            Client.read_line(din,dummy_sock);
            WaitingRoom waitingRoom = new WaitingRoom(sock);
			waitingRoom.setRoom_info(getData());
            //waitingRoom.setVisible(true);

			//쓰레드로 변경
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    waitingRoom.setVisible(true);
                }
            });

			/*
            Thread thread = new Thread() {
                public void run() {
                    waitingRoom.setVisible(true);
                }
            };
            thread.start();

            System.out.println("정상적으로 ");
            thread.join();
            System.out.println("정상적으로 joinsignal보냄");
*/

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

            // 사용자의 id를 서버로 전송한다
//            pw.println(args[0]);
//            pw.flush();

            while(true)
            {
                if(!waitingRoom.isrunning) {
                  //  System.out.println("클라이언트의 접속을 종료합니다.");
                    System.exit(0);
                }
                //System.out.println("정상적으로 joinsignal보냄");
                Thread.sleep(2000);
            }
//
//                    InputThread it = new InputThread(sock, br);
//
//                    it.start();
//

            /******************************************************************

             3. 키보드로부터 한 줄씩 입력받아 서버에 전송(/quit를 입력받기 전까지)

             ******************************************************************/

           /* String line = null;

            while((line = keyboard.readLine()) != null){
                pw.println(line);
                pw.flush();

                if(line.equals("/quit")){
                    endflag = true;
                    break;
                }
            }
            System.out.println("클라이언트의 접속을 종료합니다.");
*/

        } catch(Exception ex){
            if(!endflag)
                System.out.println(ex);

        } finally{
            try{
                if(pw != null)
                    pw.close();
            }catch(Exception ex){}

            try{
                if(sock != null)
                    sock.close();
            }catch(Exception ex){}

        }
    }


}
