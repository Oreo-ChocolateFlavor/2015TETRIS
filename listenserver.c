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
#include <errno.h>

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
  int status;
  pid_t id = waitpid(-1,&status,WNOHANG);

  if(WIFEXITED(status))
  {
    printf("fork()된 것이 죽음\n");
  }

}
void SendRoomList(struct PIPE pip,int connectedsock);
void AddRoomList(struct PIPE pip,char* buf,int childport);
void Gameserver(struct PIPE pip,int serverport);
void ReadMessage(int sock,char* buf);
void ConnectedServer(int connectedsock,struct PIPE pip);
void CreateRoom(struct PIPE pip,int connectedsock,int* childport);
void JoinRoom(struct PIPE pip,int connectedsock);

int ByteToInt(const char* byte)
{
  int s1 = byte[0] & 0xFF;
  int s2 = byte[1] & 0xFF;
  int s3 = byte[2] & 0xFF;
  int s4 = byte[3] & 0xFF;

  return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
}

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

      if(nfd <= p[pipe_count].parent[0])
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
            printf("Mainserver: JOINROOM_SIGNAL\n");
            int requsetport;

            read(p[i].parent[0],&requsetport,sizeof(int));

            int find_flag = false;
            char sendsignal = -1;

            printf("포트 번호: %d\n",requsetport);

            for(int rc = 0; rc< room_count; rc++)
            {
              if(room[rc].port == requsetport)
              {
                printf("FIND ROOM\n");
                find_flag = true;

                if(room[rc].isplay)
                {
                  printf("게임중이야 임마\n");
                  sendsignal = IS_NOW_PLAYING;
                  write(p[i].child[1],&sendsignal,1);

                }else if(room[rc].nowperson + 1 > room[rc].maxperson)
                {
                  printf("방꽉찼어 임마\n");
                  sendsignal = FULL_ROOM_SIG;
                  write(p[i].child[1],&sendsignal,1);

                }
                else{
                  printf("이럇샤이마세!\n");
                  sendsignal = AVAIL_ROOM_SIG;
                  room[rc].nowperson++;
                  write(p[i].child[1],&sendsignal,1);
                }

                break;
              }
            }

            if(!find_flag) // 방이 없으면
            {
              printf("방 없어 임마\n");
              sendsignal = NO_EXIST_ROOM;
              write(p[i].child[1],&sendsignal,1);
            }

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
          else if(SIG == LEAVE_GAMEROOM_SIG)
          {
            int recvport;
            read(p[i].parent[0],&recvport,sizeof(int));
            printf("Mainserver: LEAVE_GAMEROOM_SIG recv %d\n",recvport);

            for(int rc=0; rc <= room_count; rc++)
            {
              if(room[rc].port == recvport)
              {
                room[rc].nowperson--;
                break;
              }
            }
          }
          else if(SIG == DESTROY_ROOM_SIG)
          {
            int recvport;
            read(p[i].parent[0],&recvport,sizeof(int));
            printf("Mainserver: DESTROY_ROOM_SIG recv %d\n",recvport);

            for(int rc=0; rc <= room_count; rc++)
            {
              if(room[rc].port == recvport)
              {

                room[rc] = room[room_count -1];
                strcpy(room[room_count-1].name,"");
                room_count--;
                break;
              }
            }
          }
          else if(SIG == HOST_GAMESTART_SIG)
          {
            int recvport;

            read(p[i].parent[0],&recvport,sizeof(int));
            printf("Mainserver: HOST_GAMESTART_SIG recv %d\n",recvport);

            for(int rc=0; rc <= room_count; rc++)
            {
              if(room[rc].port == recvport)
              {
                room[rc].isplay = true;
                break;
              }
            }

          }else{
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

}

void AddRoomList(struct PIPE pip,char* buf,int childport)
{
  char sig = ADDROOM_SIGNAL;

  struct room_info temproom;

  write(pip.parent[1],&sig,1);

  strncpy(temproom.name,buf,sizeof(temproom.name));
  temproom.port = childport;
  temproom.maxperson = 5;
  temproom.nowperson = 1;

  printf("%c[1;35m",27);
  printf("%d in the AddRoomList() ROOMNAME is \"%s\" \"%s\"\n",getpid(),buf,temproom.name);
  printf("%c[0m\n",27);
  write(pip.parent[1],(char*)&temproom,sizeof(temproom));

}


void ReadMessage(int sock,char* buf) // 버그의 소지가 있음.. 고치는 것은 생각을좀 해보자.
{
  int len= 1024;
  int recvlen=0;
  char *t = buf;

  while(len > 0 && (recvlen = read(sock,t,1)))
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

void JoinRoom(struct PIPE pip,int connectedsock)
{
  char ret = 0;
  read(pip.child[0],&ret,1);
  write(connectedsock,&ret,1);
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
    else if(SIG == JOINROOM_SIGNAL)
    {
      char joinsignal = JOINROOM_SIGNAL;
      read(connectedsock,buf,sizeof(int));

      int recvport = ByteToInt(buf);

      write(pip.parent[1],&joinsignal,1);
      write(pip.parent[1],&recvport,sizeof(int));
      JoinRoom(pip,connectedsock);
    }
    else if(SIG == CLOSE_MAINROOM_SIGNAL)
    {
      printf("%c[1;35m",27);
      printf("<PID: %d>READ END SIGNAL %d\n",getpid(),pip.parent[1]);
      printf("%c[1;0m",27);
      char endsignal = CLOSE_MAINROOM_SIGNAL;
      write(pip.parent[1],&endsignal,1);
      close(connectedsock);
      exit(1);
    }
    else if(SIG == ADDROOM_SIGNAL)
    {
       printf("ADDROOM_SIGNAL : %s\n",buf);
       AddRoomList(pip,buf,nowport);

    }else if(SIG == LEAVE_GAMEROOM_SIG)  //  게스트가 방을 떠났을떄 해야할 동작 -> 부모 서버에 사람이 줄었다고 통보.
    {
      char endsignal = LEAVE_GAMEROOM_SIG;

      memset(buf,0,sizeof(int) * 4);
      read(connectedsock,buf,sizeof(int));
      int recvport = ByteToInt(buf);

      write(pip.parent[1],&endsignal,1);
      write(pip.parent[1],&recvport,sizeof(int));

      printf("%c[1;35m",27);
      printf("방떠남 get PORT: %d\n",recvport);
      printf("%c[1;0m",27);
      fflush(stdout);
    }
    else if(SIG == DESTROY_ROOM_SIG) // 호스트가 방을 파괴해야할때 해야 할 동작  -> 부모 서버에 방을 없애주는 동작을 해야함.
    {
      char endsignal = DESTROY_ROOM_SIG;

      memset(buf,0,sizeof(int) * 4);
      read(connectedsock,buf,4);
      int recvport = 0;

      recvport = ByteToInt(buf);

      printf("%c[1;35m",27);
      printf("호스트가 방파괴! port: %d",recvport);
      printf("%c[0m\n",27);
      fflush(stdout);

      write(pip.parent[1],&endsignal,1);
      write(pip.parent[1],&recvport,sizeof(int));

      int status;
      waitpid(-1,&status,WNOHANG);
      printf("%c[1;35m",27);
      printf("방파괴후 Wait() 완료\n");
      printf("%c[0m\n",27);
    }
    else if(SIG == HOST_GAMESTART_SIG)
    {
      char endsignal = HOST_GAMESTART_SIG;
      memset(buf,0,sizeof(int) * 4);

      read(connectedsock,buf,4);
      int recvport = ByteToInt(buf);

      write(pip.parent[1],&endsignal,1);
      write(pip.parent[1],&recvport,sizeof(int));
      printf("%c[1;35m",27);
      printf("게임스타트 시그널! port: %d",recvport);
      printf("%c[0m\n",27);
      fflush(stdout);
    }
    else if(SIG == 0)
    {
      printf("%c[1;35m",27);
      printf("비정상적인 종료!\n");
      printf("%c[0m\n",27);
      char endsignal = CLOSE_MAINROOM_SIGNAL;
      write(pip.parent[1],&endsignal,1);
      close(connectedsock);
      fflush(stdout);
      exit(1);
    }
  }

}
