#define CREATEROOM_SIGNAL -100   // 클라이언트에서 방을 만들때!
#define ROOMINFOSEND_SIGNAL -101 // 방정보를 보내달라!
#define ADDROOM_SIGNAL -102  // 이건 클라가 보낼일이 없음!
#define JOINROOM_SIGNAL -103    // 방을 조인할때
#define CLOSE_MAINROOM_SIGNAL -104 // 대기방을 close 할떄
#define PORT_SIG -105;

struct PIPE
{
  int child[2];
  int parent[2];
};

struct room_info
{
  char name[50];
  int port;
  int maxperson;
  int nowperson;
};


struct person
{
  char id[20];
  int client_sock;
};
