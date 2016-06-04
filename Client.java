import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;



public class Client {
	public static void read_line(DataInputStream din)
	{
		
		byte[] buf = new byte[1024];
		byte tmpbuf;
		int index=0;
		try {
			for(int i=0; i<1024; i++)
			{
				tmpbuf = din.readByte();
		
				if(tmpbuf < 0)
				{
					System.out.println("Get signal "+tmpbuf);
					
					byte[] tmp4 = new byte[4];
					tmp4[0] = buf[0];
					tmp4[1] = buf[1];
					tmp4[2] = buf[2];
					tmp4[3] = buf[3];
					
					System.out.print("goTTA port: " + byteToint(tmp4));
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
	//	return rtn = buf;
		
	}

	public static int byteToint(byte[] arr)
	{
		ByteBuffer buff =  ByteBuffer.allocate(Integer.SIZE/8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		
		buff.put(arr);
		buff.flip();
		
		System.out.println("convert: " + buff.getInt());
		
		return buff.getInt();
		
		
	}
    public static void main(String[] args){

        if(args.length != 2){
            System.out.println("사용법 : java ChatClient id 접속할 서버 ip");
            System.exit(1);
        }

        Socket sock = null;
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
            WaitingRoom waitingRoom = new WaitingRoom(sock);
            waitingRoom.setVisible(true);
/*

            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    waitingRoom.setVisible(true);
                }
            });

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
