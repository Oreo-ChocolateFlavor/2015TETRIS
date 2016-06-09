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



char map[6][18][10] = {0};
struct person per[5];
int persontop = 0;
char recvgamesig;

int nowstack = 0;
int score[5] = {0};

void ReadGameserver(int sock,char* buf);

void Gameserver(struct PIPE pip,int serverport)
{
  int owner;

  printf("GAMESERVER is Created serverport : %d\n",serverport);

  int gameserver_sock, client_sock;
  struct sockaddr_in  server_addr,client_addr;
  fd_set oldset,newset;

  int fdname,nfd;

  if((gameserver_sock = socket(PF_INET,SOCK_STREAM,0)) < 0)
  {
    perror("game sock() err");
    exit(1);
  }

  memset(&server_addr,0,sizeof(server_addr));
  server_addr.sin_family = AF_INET;
  server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
  server_addr.sin_port = htons(serverport);


  int enable = 1;

  if(setsockopt(gameserver_sock,SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < -1)
  {
    perror("setsockopt()");
    exit(1);
  }

  if(bind(gameserver_sock,(struct sockaddr*)&server_addr,sizeof(server_addr)) < 0)
  {
    perror("game bind() err");
    exit(1);
  }

  if(listen(gameserver_sock,5) < 0)
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

  while(1)
  {
    newset = oldset;
    if((fdname = select(nfd,&newset,0,0,&tim)) < 0)
    {
      perror("gameserver select()");
      continue;
    }


    if(FD_ISSET(gameserver_sock,&newset)) // join 요청이 들어올때
    {
      client_sock = accept(gameserver_sock,(struct sockaddr*)&client_addr,(socklen_t*)&len);
      per[persontop].client_sock = client_sock;

      FD_SET(client_sock,&oldset);

      if(nfd <= client_sock)
      {
        nfd = client_sock +1;
      }

      if(persontop == 0) owner = client_sock;
      persontop++;

    }
    else
    {

      for(int i=0; i<persontop; i++)
      {
        if(FD_ISSET(per[i].client_sock,&newset))
        {

          char buf[1024] = "";
          ReadGameserver(per[i].client_sock,buf);

          if(recvgamesig == (char)LEAVE_GAMEROOM_SIG)
          {

            FD_CLR(per[i].client_sock,&oldset);
            close(per[i].client_sock);
            per[i] = per[persontop -1];
            persontop--;

            printf("%c[1;33m\n",27);
            printf("IN THE GAMEROOM\n");
            printf("GUEST is LEAVE THE ROOM!\n");
            printf("%c[0m\n",27);
            fflush(stdout);
          }
          else if(recvgamesig == (char)DESTROY_ROOM_SIG)
          {

            for(int j=0; j<persontop; j++)
              close(per[j].client_sock);

            printf("%c[1;33m\n",27);
            printf("IN THE GAMEROOM\n");
            printf("HOST IS DESTROY THE GAMEROOM!!\n");
            printf("%c[0m\n",27);
            fflush(stdout);
            exit(0);
          }
          else if(recvgamesig == (char)HOST_GAMESTART_SIG)
          {

              printf("%c[1;33m\n",27);
              printf("HOST 가 게임스타트 버튼을 누름\n");
              printf("%c[0m\n",27);
              fflush(stdout);

              for(int j=0 ; j<persontop; j++)
                  write(per[j].client_sock,&recvgamesig,1);

              printf("GAMESTART!\n");
          }
          else if(recvgamesig == GAMEBOARD_UPDATE_SIG)
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
              char sendid = -(j+1);
              write(per[i].client_sock,map[j],10*18*sizeof(char));
              write(per[i].client_sock,&sendid,1);

            }
          }
          else if(recvgamesig == GAME_OVER_SIG)
          {
            char id = 0;
            read(per[i].client_sock,&id,1);
            score[nowstack] = id;
            nowstack++;

            //for(int j=0; j<persontop; j++)
            //{
            //  printf("%d플레이어의  게임보드 상태\n",j);
            //  for(int k=0; k<18; k++)
            //  {
            //    for(int l=0; l<10; l++)
            //    {
            //      printf("%d ",map[j][k][l]);
            //    }

            //    printf("\n");
            //  }

            //  printf("\n\n");
            //}

            printf("%d 플레어가 뒤짐\n",id);
          }
        }
      }
    }

    if(nowstack == persontop)
    {
      printf("모든 놈들이 다주금!\n");
    }

  }

  return;
}


void ReadGameserver(int sock,char* buf) // 버그의 소지가 있음.. 고치는 것은 생각을좀 해보자.
{
  int len=1024;
  int recvlen=0;
  char *t = buf;

  printf("리드에서 블락걸림\n");

  while(len!=0  && (recvlen = read(sock,t,1)))
  {
    len -= recvlen;
    t += recvlen;
    if(*(t-1) < 0)
      break;
  }

  recvgamesig = *(t-1);
  *(t-1) = 0;

  printf("블락 풀림\n");
  printf("gameserver <%d> is read  SIG is  = %d  \"%s\"\n",getpid(),recvgamesig,buf);
}
