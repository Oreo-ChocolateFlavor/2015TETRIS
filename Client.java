import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;



public class Client {

	private static room_info[] data; //방정보 데이터를 가지고 있는 클래스변수를 선언 해 놓는다.
	public static Socket sock = null;//소켓변수도 미리 선언 해 놓는다.
	private static String host_ip; //To store host_ip
	
	public static room_info[] getData() { //클래스 변수의 값을 가져오는 get함수이다.
		return data;
	}

	public static void setData(room_info[] data) { //클래스 변수의 값을 설정하는 set함수이다.
		Client.data = data;
	}

	public static String getip() //ip를 가져오는 함수이다.
	{
		return host_ip;
	}
	
	public static Socket connect_gameserver(Socket sock, int port, String ip) //게임서버에 클라이언트를 연결하는 함수이다.
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
	
	public static Socket read_line(DataInputStream din, Socket new_sock) //시그널을 만날때 까지 계속해서 읽고 시그널을 만나면 시그널에 따라 행동을 취한다.
	{
		
		byte[] buf = new byte[CreateRoom.BUF_SIZE]; //전체를 저장할 버퍼.
		byte tmpbuf;								//한개씩 저장할 버퍼.
		int index=0;
		try {
			for(int i=0; i<CreateRoom.BUF_SIZE; i++)//버퍼사이즈를 초과하지 않을때까지 계속해서 읽는다.
			{
				tmpbuf = din.readByte();	//한바이트씩 읽으면서 조건에 맞는지 판단한다.
		
				if(tmpbuf < 0)	//바이트의 값이 0보다 작다는 것은 우리가 사전에 정의한 시그널만 들어온다고 판단 할 수 있다. 따라서 각 시그널에 맞는 행동을 취한다.
				{
					System.out.println("Get signal "+(int)tmpbuf);
					if((int)tmpbuf == CreateRoom.PORT_SIG)//포트번호에 대한 시그널을 읽었을 경우 앞에 있는 데이터가 포트이기 때문에int로 변환하여 게임서버에 연결한다.
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
					else if((int)tmpbuf == CreateRoom.ROOMINFOSEND_SIGNAL) //방정보에 관한 시그널을 읽었을 경우 앞에 있는 데이터가 방정보이기 때문에 사전에 정의된 만큼씩 바이트로 읽어서 값을 설정한다.
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
					index = i;
					break;
				}
				else //시그널이 아닌 경우 시그널 앞에 존재하는 데이터이므로 데이터를 버퍼에 저장한다.
				{
					buf[i] = tmpbuf;
				}
				
				
			}
		
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] rtn = new byte[++index];
		return new_sock; //연결을 하는 부분일 경우 연결 된 소켓을 리턴한다.
		
	}

	public static int byteToint(byte[] arr) //byte로 받은 데이터를 int형으로 변환하는 함수이다.
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
             각각 DataInputStream과 DataOutputStream 형태로 변환시킴

             ******************************************************************/

        	host_ip = new String(args[1]);
            sock = new Socket(args[1], 1818);
            dout = new DataOutputStream(sock.getOutputStream());
            din = new DataInputStream(sock.getInputStream());


             //make WaitingRoom
            dout.writeByte(CreateRoom.ROOMINFOSEND_SIGNAL);
            dout.flush(); 
            Client.read_line(din,dummy_sock);
            WaitingRoom waitingRoom = new WaitingRoom(sock);
			waitingRoom.setRoom_info(getData());

			//쓰레드로 변경
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    waitingRoom.setVisible(true);
                }
            });

            while(true) //게임이 진행되고 있는동안은 무한루프를 돌고있는다.
            {
                if(!waitingRoom.isrunning) {
                    System.exit(0);
                }
                Thread.sleep(2000);
            }


        } catch(Exception ex){
            if(!endflag)
                System.out.println(ex);

        } finally{
            
            try{
                if(sock != null)
                    sock.close();
            }catch(Exception ex){}

        }
    }


}
