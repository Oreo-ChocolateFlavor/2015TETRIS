public class room_info{
		
		public String name; //이름, 포트, 최대수용인원, 현재인원을 가지고 있는 클래스이다.
		public int port;
		public int maxperson;
		public int nowperson;
		

		public room_info(String tmp_name, int tmp_port, int tmp_maxperson, int tmp_nowperson) {
			// TODO Auto-generated constructor stub

			this.name = tmp_name;
			this.port = tmp_port;
			this.maxperson = tmp_maxperson;
			this.nowperson = tmp_nowperson;
		}
		
	}