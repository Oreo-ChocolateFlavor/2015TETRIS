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

#define CREATEROOM_SIGNAL -100   // 클라이언트에서 방을 만들때!
#define ROOMINFOSEND_SIGNAL -101 // 방정보를 보내달라!
#define ADDROOM_SIGNAL -102  // 이건 클라가 보낼일이 없음!
#define JOINROOM_SIGNAL -103    // 방을 조인할때
#define CLOSE_MAINROOM_SIGNAL -104 // 대기방을 close 할떄

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

static void sighandler(int sig)
{
  printf("killed!\n");
}
void SendRoomList(int* pip);
void AddRoomList(int* pip);
void Gameserver(int* pip);
void ReadMessage(int sock,char* buf);
void ConnectedServer(int connectedsock,int* pip);
void CreateRoom(int* pip);
void JoinRoom(int* pip);

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

  int enable = 1;

  if(setsockopt(server_sock,SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < -1)
  {
    perror("setsockopt()");
    exit(1);
  }             // Option length


  nfd = server_sock +1;
  FD_ZERO(&oldset);
  FD_SET(server_sock,&oldset);

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

  signal(SIGCHLD,sighandler);
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
      FD_SET(pipe_sock[0],&oldset);

      if(nfd < pipe_sock[0])
      {
        nfd = pipe_sock[0] +1;
      }

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
      else if(pid > 0) // 부모면!
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
        if(FD_ISSET(p[0].fd[0],&newset)) // 자식 프로세스로 부터 통신 요청이 들어오면!
        {
          char pipemessagebuf[1024];
          ReadMessage(p[i].fd[0],pipemessagebuf);

          if(SIG == -110) // 자식이 EXIT했다는 거임.
          {
            FD_CLR(p[i].fd[0],&oldset);
            p[i].fd[0] = p[pipe_count-1].fd[0];
            p[i].fd[1] = p[pipe_count-1].fd[1];
            pipe_count--;
          }
        }
      }

    }


  }

  return 0;
}


void SendRoomList(int* pip)
{
  printf("%d in the SendRoomList()\n",getpid());
}

void AddRoomList(int* pip)
{
  printf("%d in the AddRoomList()\n",getpid());
}

void Gameserver(int* pip)
{
    printf("%d in the Gameserver()\n",getpid());
}

void ReadMessage(int sock,char* buf)
{
  int len=sizeof(buf);
  int recvlen=0;
  char *t = buf;

  while(len!=0  && (recvlen = read(sock,t,len)))
  {
    len -= recvlen;
    t += recvlen;
    if(*(t-1) < 0)
      break;
  }

  SIG = *(t-1);
  printf("<%d> is read  SIG is  = %d  \"%s\"\n",getpid(),SIG,buf);

  *(t-1) = 0;
}

void JoinRoom(int* pip)
{
  printf("%d in the JoinRoom()\n",getpid());
}

void CreateRoom(int* pip)
{
  printf("%d in the CreateRoom()\n",getpid());
}

void ConnectedServer(int connectedsock,int* pip) //커넥트 된후 실행되는 놈.
{
  char buf[1024];
  while(1)
  {
    memset(buf,0,sizeof(buf));
    ReadMessage(connectedsock,buf);
    if(SIG == ROOMINFOSEND_SIGNAL){ SendRoomList(pip); }
    else if(SIG == ADDROOM_SIGNAL){ AddRoomList(pip); }
    else if(SIG == CREATEROOM_SIGNAL){ CreateRoom(pip); }
    else if(SIG == JOINROOM_SIGNAL){ JoinRoom(pip); }
    else if(SIG ==  CLOSE_MAINROOM_SIGNAL)
    {
      printf("READ END SIGNAL\n");
      char endsignal = -110;
      write(pip[1],&endsignal,1);
      exit(1);
    }
  }
  printf("in the child : this is never display\n");
}
