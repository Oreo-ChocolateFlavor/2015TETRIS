#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdbool.h>
#include <pthread.h>

#define CREATEROOM_SIGNAL -100
#define ROOMINFOSEND_SIGNAL -101
#define ADDROOM_SIGNAL -102
#define JOINROOM_SIGNAL -103
#define CLOSE_MAINROOM_SIGNAL -104

int inputsignal;
#define SIG inputsignal
int nfd,fdname;

struct PIPE
{
  int fd[2];
}p[1000];

int pipe_count;

struct room_info
{
  char name[200];
  int port;
  int maxperson;
  int nowperson;
}room[100];

void SendRoomList();
void AddRoomList();
void Gameserver();
void ReadMessage(int sock,char* buf);
void ConnectedServer(int connectedsock,int* pip);
void CreateRoom();
void JoinRoom();

void* ReadFromChildProcess(void* arg);

int main(int argc,char* argv[])
{
  int server_sock,client_sock;
  int len;
  pid_t pid;
  fd_set oldset,newset;

  struct sockaddr_in serveraddr,clientaddr;

  if(argc != 2)
  {
    printf("Usage: %s <Port>",argv[0]);
    exit(1);
  }

  if((server_sock = socket(PF_INET,SOCK_STREAM,0)) < 0)
  {
    perror("sock()");
    exit(1);
  }

  nfd = server_sock +1;
  FD_ZERO(&oldset);
  FD_SET(serv_sock,&oldset);

  struct timeval tim;

  memset(&serveraddr,0,sizeof(serveraddr));
  serveraddr.sin_family = AF_INET;
  serveraddr.sin_addr.s_addr = htonl(INADDR_ANY);
  serveraddr.sin_port = htons(atoi(argv[1]));

  if((bind(server_sock,(struct sockaddr*)&serveraddr,sizeof(serveraddr))) < 0)
  {
    perror("bind()");
    exit(1);
  }

  if((listen(server_sock,5)) < 0)
  {
    perror("listen()");
    exit(1);
  }

  signal(SIGCHLD,SIG_IGN);
  len = sizeof(clientaddr);
  tim.tv_sec =1;
  tim.tv_usec =0;

  while(1)
  {
    newset = oldset;

    if((fdname =select(nfd,&newset,0,0,&tim)) < 0)
    {
      perror("select() err");
      continue;
    }

    if(FD_ISSET(server_sock,&newset))
    {

      client_sock = accept(server_sock,(struct sockaddr*)&clientaddr,(socklen_t*)&len);

      int pipe_sock[2];
      pipe(pipe_sock);

      p[pipe_count].fd[0] = pipe_sock[0];
      p[pipe_count].fd[1] = pipe_sock[1];
      pipe_count++;

      if(client_sock == -1)
      {
        perror("accept()");
        exit(1);
      }

      pid = fork();
      if(pid == 0)  // 자식이면! 포크후 커넥티트 서버라는 함수를 실행하는 프로세스를 만듬!
      {
        close(server_sock);
        ConnectedServer(client_sock,pipe_sock);
      }
      else if(pid > 0) //
      {
        close(client_sock);


      }
      else if(pid == -1)
      {
        perror("fork()");
        exit(1);
      }
    }
    else{

      for(int i=0; i<pipe_count; i++)
      {
        if(FD_ISSET(pipe_sock[0],&newset))
        {


        }
      }

    }


  }

  return 0;
}


void SendRoomList()
{
  printf("%d in the SendRoomList()\n",getpid());
}

void AddRoomList()
{
  printf("%d in the AddRoomList()\n",getpid());
}

void Gameserver()
{
    printf("%d in the Gameserver()\n",getpid());
}

void ReadMessage(int sock,char* buf)
{
  printf("%d in the ReadMessage()\n",getpid());
  int len=sizeof(buf);
  int recvlen=0;

  while(len!=0 && (recvlen = read(sock,buf,len)))
  {
    len -= recvlen;
    buf += recvlen;

    if(*(buf-1) < 0)
      break;
  }

  SIG = *(buf-1);
  *(buf-1) = 0;
}

void JoinRoom()
{
  printf("%d in the JoinRoom()\n",getpid());
}

void CreateRoom()
{
  printf("%d in the CreateRoom()\n",getpid());
}

void ConnectedServer(int connectedsock) //커넥트 된후 실행되는 놈.
{
  char buf[1024];
  while(1)
  {
    memset(buf,0,sizeof(buf));
    ReadMessage(connectedsock,buf);

    if(SIG == ROOMINFOSEND_SIGNAL) SendRoomList();
    else if(SIG == ADDROOM_SIGNAL)  AddRoomList();
    else if(SIG == CREATEROOM_SIGNAL) CreateRoom();
    else if(SIG == JOINROOM_SIGNAL) JoinRoom();
    else if(SIG ==  CLOSE_MAINROOM_SIGNAL)
    {
      exit(1);
    }
  }
}
