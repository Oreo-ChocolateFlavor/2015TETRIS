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
#include "datastruct.h"



char map[6][18][10] = {0};  // 게임 맵을 저장하는 배열
struct person per[5]; //플레이어 정보저장
int persontop = 0;
char recvgamesig;
char isdie[7];

int nowstack = 0; // 죽은 순서를 저장하기 위한 STACK
int score[5] = {0};

void ReadGameserver(int sock,char* buf);

void Gameserver(struct PIPE pip,int serverport)
{
  char filename[100];
  sprintf(filename,"%d",serverport);

  FILE* gameserverlog = fopen(filename,"a+");

  for(int i=0; i<7; i++)
    isdie[i] = -1;

  int owner;

  fprintf(gameserverlog,"GAMESERVER is Created serverport : %d\n",serverport);

  int gameserver_sock, client_sock;
  struct sockaddr_in  server_addr,client_addr;
  fd_set oldset,newset;

  int fdname,nfd;

  if((gameserver_sock = socket(PF_INET,SOCK_STREAM,0)) < 0)  //  소켓을 만들고
  {
    perror("game sock() err");
    exit(1);
  }

  memset(&server_addr,0,sizeof(server_addr));
  server_addr.sin_family = AF_INET;
  server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
  server_addr.sin_port = htons(serverport);


  int enable = 1;

  if(setsockopt(gameserver_sock,SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < -1)  // SO_REUSEADDR 를 설정한후
  {
    perror("setsockopt()");
    exit(1);
  }

  if(bind(gameserver_sock,(struct sockaddr*)&server_addr,sizeof(server_addr)) < 0)  // 바인딩하고
  {
    perror("game bind() err");
    exit(1);
  }

  if(listen(gameserver_sock,5) < 0) // 리슨상태로 만듦
  {
    perror("listen error\n");
  }

  FD_ZERO(&oldset);
  FD_SET(gameserver_sock,&oldset);

  struct timeval tim;
  int len = sizeof(client_addr);
  tim.tv_sec =1;
  tim.tv_usec =0;

  nfd = gameserver_sock+1;

  while(1)  // 루프를 계속도는데
  {
    newset = oldset;
    if((fdname = select(nfd,&newset,0,0,&tim)) < 0)
    {
      perror("gameserver select()");
      continue;
    }


    if(FD_ISSET(gameserver_sock,&newset)) // connect 요청이 들어오면
    {
      client_sock = accept(gameserver_sock,(struct sockaddr*)&client_addr,(socklen_t*)&len); //엑셉하고
      per[persontop].client_sock = client_sock; // per에 소켓을 추가한후

      fprintf(gameserverlog,"플레이어 접속!\n");

      FD_SET(client_sock,&oldset); // 멀티플렉싱을위해 등록

      if(nfd <= client_sock)
      {
        nfd = client_sock +1;
      }

      if(persontop == 0) owner = client_sock;
      isdie[persontop] = 0;
      persontop++;

    }
    else
    {

      for(int i=0; i<persontop; i++)
      {
        if(FD_ISSET(per[i].client_sock,&newset))  //i번쨰 플레이어의 소켓이 변했으면
        {

          char buf[1024] = "";
          ReadGameserver(per[i].client_sock,buf);

          if(recvgamesig == (char)LEAVE_GAMEROOM_SIG) // join한 사람이 방을떠날떄
          {

            FD_CLR(per[i].client_sock,&oldset);
            close(per[i].client_sock);
            per[i] = per[persontop -1];
            persontop--;
            isdie[persontop] = -1;

            fprintf(gameserverlog,"%c[1;33m\n",27);
            fprintf(gameserverlog,"IN THE GAMEROOM\n");
            fprintf(gameserverlog,"GUEST is LEAVE THE ROOM!\n");
            fprintf(gameserverlog,"%c[0m\n",27);
            fflush(gameserverlog);
          }
          else if(recvgamesig == (char)DESTROY_ROOM_SIG)  // 방이 없어질 때
          {
            for(int j=0; j<persontop; j++)
              write(per[j].client_sock,&recvgamesig,1);

            for(int j=0; j<persontop; j++)
              close(per[j].client_sock);

            fprintf(gameserverlog,"%c[1;33m\n",27);
            fprintf(gameserverlog,"IN THE GAMEROOM\n");
            fprintf(gameserverlog,"HOST IS DESTROY THE GAMEROOM!!\n");
            fprintf(gameserverlog,"%c[0m\n",27);
            fflush(gameserverlog);
            exit(0);
          }
          else if(recvgamesig == (char)HOST_GAMESTART_SIG)  // 호스트가 게임 시작을할떄
          {

              for(int j=0; j<7; j++)
                isdie[j] = -1;
              for(int j=0; j<persontop; j++)
                isdie[j] = 0;

              fprintf(gameserverlog,"%c[1;33m\n",27);
              fprintf(gameserverlog,"HOST 가 게임스타트 버튼을 누름\n");
              fprintf(gameserverlog,"%c[0m\n",27);
              fflush(gameserverlog);

              for(int j=0 ; j<persontop; j++)
                  write(per[j].client_sock,&recvgamesig,1);

              fprintf(gameserverlog,"GAMESTART!\n");
          }
          else if(recvgamesig == GAMEBOARD_UPDATE_SIG)  // 게임보드 업데이트 시그널을 받았을때
          {
            char id;
            read(per[i].client_sock,&id,1);

            for(int j=0; j<18; j++)
            {
              for(int k=0; k<10; k++)
              {
                map[id][j][k] = *(buf+ j*10 + k);
              }
            }

            for(int j=0; j<5; j++)
            {
              char isdiesig = isdie[j];
              write(per[i].client_sock,map[j],10*18*sizeof(char));
              write(per[i].client_sock,&isdiesig,1);

            }
          }
          else if(recvgamesig == GAME_OVER_SIG)  //게임오버 시그널을 받았을때
          {
            char id = 0;
            read(per[i].client_sock,&id,1);
            score[nowstack] = id;
            isdie[i] = persontop - nowstack;
            nowstack++;

            fprintf(gameserverlog,"%d 플레어가 죽음 등수 : %d\n",id,isdie[i]);
          }
          else if(recvgamesig == 0)  //비정상적으로 종료를 하였을때
          {

            FD_CLR(per[i].client_sock,&oldset);
            fprintf(gameserverlog,"%d 유저가 비정상종료를 하였습니다.\n",i);
            fclose(gameserverlog);
            exit(1);
          }
        }
      }
    }

    if(nowstack == persontop)
    {
      fprintf(gameserverlog,"게임이 끝남!\n");
    }

    fflush(gameserverlog);
  }

  return;
}


void ReadGameserver(int sock,char* buf) // 메세지 읽어드리는 함수
{
  int len=1024;
  int recvlen=0;
  char *t = buf;

  while(len!=0  && (recvlen = read(sock,t,1)))
  {
    len -= recvlen;
    t += recvlen;
    if(*(t-1) < 0)
      break;
  }

  recvgamesig = *(t-1);
  *(t-1) = 0;

  printf("gameserver <%d> is read  SIG is  = %d  \"%s\"\n",getpid(),recvgamesig,buf);
}
