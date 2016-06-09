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
#define IS_NOW_PLAYING -114
#define GAME_OVER_SIG -115
#define GMAE_END_SIG -116

struct PIPE
{
  int child[2];
  int parent[2];
};

struct room_info
{
  char name[52];
  int port;
  int maxperson;
  int nowperson;
  bool isplay;
};

struct person
{
  char id[20];
  int client_sock;
};
