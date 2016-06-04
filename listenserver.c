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
#include <stdlib.h>
#include "datastruct.h"

int portnumber = 7000;
int inputsignal;
#define SIG inputsignal
int nfd,fdname;
struct PIPE p[1000];
int pipe_count;

struct room_info room[20];
int room_count;

static void sighandler(int sig)
{
  printf("killed!\n");
  int status;
  pid_t id = waitpid(-1,&status,WNOHANG);

  if(WIFEXITED(status))
  {
    printf("ZOMBIE is killed\n");
  }

}
void SendRoomList(struct PIPE pip,int connectedsock);
void AddRoomList(struct PIPE pip,char* buf,int childport);
void Gameserver(struct PIPE pip,int serverport);
void ReadMessage(int sock,char* buf);
void ConnectedServer(int connectedsock,struct PIPE pip);
void CreateRoom(struct PIPE pip,int connectedsock,int* childport);
void JoinRoom(struct PIPE pip);

int main(int argc,char* argv[])
{

  room_count = 0;
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
      int child[2],parent[2];
      if(pipe(child) == -1)
      {
        perror("pipe");
      }
      if(pipe(parent)==-1)
      {
        perror("pipe");
      }

      p[pipe_count].child[0] = child[0]; p[pipe_count].child[1] = child[1];
      p[pipe_count].parent[0] = parent[0]; p[pipe_count].parent[1] = parent[1];

      FD_SET(p[pipe_count].parent[0],&oldset);

      if(nfd < p[pipe_count].parent[0])
      {
        nfd = p[pipe_count].parent[0]+1;
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
        ConnectedServer(client_sock,p[pipe_count]);
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

      pipe_count++;
    }
    else{
      for(int i=0; i<pipe_count; i++)
      {
        if(FD_ISSET(p[i].parent[0],&newset)) // 자식 프로세스로 부터 통신 요청이 들어오면!
        {
          char pipemessagebuf[100] = "";
          ReadMessage(p[i].parent[0],pipemessagebuf);
          printf("In the Mainserver recv SIG : %d\n",SIG);

          if(SIG == CLOSE_MAINROOM_SIGNAL) // 자식이 EXIT했다는 거임.
          {
            printf("Mainserver: CLOSE MAINROOM_SIGNAL\n");
            FD_CLR(p[i].parent[0],&oldset);
            p[i] = p[pipe_count];
            pipe_count--;
          }
          else if(SIG == JOINROOM_SIGNAL)
          {

          }
          else if(SIG == CREATEROOM_SIGNAL) // 여기서 포트넘버를 넘겨주고 포트를 증가시킨다.
          {
            printf("Mainserver: CREATEROOM_SIGNAL recv\n");
            write(p[i].child[1],&portnumber,sizeof(int));
            portnumber++;
          }
          else if(SIG == ADDROOM_SIGNAL)
          {
            printf("Mainserver: ADDROOM_SIGNAL recv\n");

            read(p[i].parent[0],&room[room_count],sizeof(struct room_info));

            printf("ADDROOM_SIGNAL name: %s port: %d\n",room[room_count].name,room[room_count].port);
            room_count++;

          }
          else if(SIG == ROOMINFOSEND_SIGNAL){
            printf("Mainserver: ROOMINFOSEND_SIGNAL recv\n");
            write(p[i].child[1],&room,sizeof(room));
          }
          else{
            printf("Nah... error\n");
          }
        }
      }
    }
  }

  return 0;
}


void SendRoomList(struct PIPE pip,int connectedsock)
{
   struct room_info roomarr[30];

   int count =0;

  int sig = ROOMINFOSEND_SIGNAL;

  write(pip.parent[1],&sig,1); //부모 서버로 요청을 보냄
  read(pip.child[0],&roomarr,sizeof(roomarr)); // 자식은 정보를 읽어드린후

  for(count=0; count<30; count++)
  {
    if(strlen(roomarr[count].name) == 0)
      break;

    printf("count : %d information: name: %s , port: %d\n",count,roomarr[count].name,roomarr[count].port);
  }
  write(connectedsock,&sig,1);
  if(count != 0)
    write(connectedsock,&roomarr,sizeof(struct room_info)* count);
  write(connectedsock,&sig,1);

  printf("%d in the SendRoomList()\n",getpid());
}

void AddRoomList(struct PIPE pip,char* buf,int childport)
{
  char sig = ADDROOM_SIGNAL;

  struct room_info temproom;

  write(pip.parent[1],&sig,1);

  memcpy(temproom.name,buf,sizeof(temproom.name));
  temproom.port = childport;
  temproom.maxperson = 5;
  temproom.nowperson = 1;

  printf("%d in the AddRoomList() ROOMNAME is \"%s\"\n",getpid(),buf);
  write(pip.parent[1],(char*)&temproom,sizeof(temproom));

}


void ReadMessage(int sock,char* buf) // 버그의 소지가 있음.. 고치는 것은 생각을좀 해보자.
{
  int len=sizeof(buf);
  int recvlen=0;
  char *t = buf;

  while(len!=0  && (recvlen = read(sock,t,1)))
  {
    len -= recvlen;
    t += recvlen;
    if(*(t-1) < 0)
      break;
  }

  SIG = *(t-1);
  *(t-1) = 0;
  printf("<%d> is read  SIG is  = %d  \"%s\"\n",getpid(),SIG,buf);
}

void JoinRoom(struct PIPE pip)
{
//  printf("%d in the JoinRoom()\n",getpid());
}

void CreateRoom(struct PIPE pip,int connectedsock,int* childport) // 여기서 게임방 fork를 해준후에  서버랑 포트넘버를 넘겨줌;
{
  char sig = CREATEROOM_SIGNAL;
  int gameserverport;

  pid_t gameserver_pid;


  // 여기 방정보를 서버를 보내는 코드를 넣는다.
  write(pip.parent[1],&sig,1);

  sig = PORT_SIG;
  //서버 포트를 받고고
  read(pip.child[0],&gameserverport,sizeof(int));
  write(connectedsock,(char*)&gameserverport,sizeof(int));
  write(connectedsock,&sig,1);

  *childport = gameserverport;

  if((gameserver_pid = fork()) < 0)
  {
    perror("gameserver fork()");
  }else if(gameserver_pid == 0) // 여기가 게임서버!
  {
    Gameserver(pip,gameserverport);
  }
  else if(gameserver_pid > 0) //  여기는 부모!
  {

  }

  printf("%d in the CreateRoom() %d\n",getpid(),gameserverport);

}

void ConnectedServer(int connectedsock,struct PIPE pip) //커넥트 된후 실행되는 놈.
{
  char buf[1024];
  int nowport;
  while(1)
  {
    memset(buf,0,sizeof(buf));
    ReadMessage(connectedsock,buf);


    if(SIG == ROOMINFOSEND_SIGNAL) SendRoomList(pip,connectedsock);
    else if(SIG == CREATEROOM_SIGNAL) CreateRoom(pip,connectedsock,&nowport);
    else if(SIG == JOINROOM_SIGNAL){printf("TEST JOINROOM_SIGNAL\n"); JoinRoom(pip); }
    else if(SIG == CLOSE_MAINROOM_SIGNAL)
    {
      printf("<PID: %d>READ END SIGNAL %d\n",getpid(),pip.parent[1]);
      char endsignal = CLOSE_MAINROOM_SIGNAL;
      write(pip.parent[1],&endsignal,1);
      close(connectedsock);
      exit(1);
    }
    else if(SIG == ADDROOM_SIGNAL) AddRoomList(pip,buf,nowport);
  }
  printf("in the child : this is never display\n");
}
