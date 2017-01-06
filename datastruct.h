#define CREATEROOM_SIGNAL -100   // 클라이언트에서 방을 만들때!
#define ROOMINFOSEND_SIGNAL -101 // 방정보를 보내달라!
#define ADDROOM_SIGNAL -102  // 부모 서버와 자식서버간의 ROOM동기화를 할때 쓰이는 시그날.
#define JOINROOM_SIGNAL -103    // 방을 조인할때
#define CLOSE_MAINROOM_SIGNAL -104 // 대기방을 close 할떄
#define PORT_SIG -105; //포트를 보낼때 쓰이는  SIGNAL
#define CHANGE_OWNER_SIG -106 // 방장이 바뀌었을때 보내는 시그널.
#define DESTROY_ROOM_SIG -107 //방이 없어질때 들어오는 시그널.
#define FULL_ROOM_SIG -108 // 방 인원이 다찼을때
#define NO_EXIST_ROOM -109 // 방 이 없을때.
#define AVAIL_ROOM_SIG -110 // 방이 이용가능 할떄
#define LEAVE_GAMEROOM_SIG  -111 // 게임룸에서 사용자가 나갈때
#define HOST_GAMESTART_SIG -112 // 게임룸에서 호스트가 스타트 버튼을 누를때
#define GAMEBOARD_UPDATE_SIG -113 // 게임보드를 업데이트 해줄떄.
#define IS_NOW_PLAYING -114 // 해당방이 게임 플레이중이면
#define GAME_OVER_SIG -115 // 특정 유저가 죽었을 때
#define GMAE_END_SIG -116 // 모든 유저가 죽었을 때

struct PIPE  // 부모서버와 자식서버 사이의 통신을위해 정의하는 구조체
{
  int child[2]; // 0 == 자식 stdin  1 == 자식 stdout
  int parent[2]; // 0 == 부모 stdin  1 == 부모 stdout
};

struct room_info  // 게임방 정보를 담아두는 구조체
{
  char name[52]; // 방이름
  int port; // 해당 방 포트
  int maxperson; // 맥스 인원수
  int nowperson; // 현재 방인원수
  bool isplay; // 지금 현재 게임중인지.
};

struct person // 게임서버에서 방에 들어온 플레이어를 관리하는 구조체
{
  char id[20]; //플레이어 아이디 저장 구조체
  int client_sock; // 해당 클라와 통신하는 소켓번호를 저장;
};
