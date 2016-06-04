#define CREATEROOM_SIGNAL -100   // 클라이언트에서 방을 만들때!
#define ROOMINFOSEND_SIGNAL -101 // 방정보를 보내달라!
#define ADDROOM_SIGNAL -102  // 부모 서버와 자식서버간의 ROOM동기화를 할때 쓰이는 시그날.
#define JOINROOM_SIGNAL -103    // 방을 조인할때
#define CLOSE_MAINROOM_SIGNAL -104 // 대기방을 close 할떄
#define PORT_SIG -105; //포트를 보낼때 쓰이는  SIGNAL
#define CHANGE_OWNER_SIG -106 // 방장이 바뀌었을때 보내는 시그널.

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
};

struct person
{
  char id[20];
  int client_sock;
};
