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


struct person per[5];
int persontop = 0;
char recvgamesig;

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
    else{
      fprintf(stderr,"멀티플렉싱 진행중");
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

      printf("ACCEPT %d %d\n",client_sock,nfd);
    }
    else
    {

      for(int i=0; i<persontop; i++)
      {
        if(FD_ISSET(per[i].client_sock,&newset))
        {
          printf("%d 소켓이 변함\n",per[i].client_sock);

          char buf[1024] = "";
          ReadGameserver(per[i].client_sock,buf);

          if(recvgamesig == (char)LEAVE_GAMEROOM_SIG)
          {
            printf("%c[1;33m\n",27);
            printf("IN THE GAMEROOM\n");
            printf("야! 방떠나장!!\n");
            printf("%c[0m\n",27);
            fflush(stdout);
          }
          else if(recvgamesig == (char)DESTROY_ROOM_SIG)
          {
            printf("%c[1;33m\n",27);
            printf("IN THE GAMEROOM\n");
            printf("방파괴하자!\n");
            printf("%c[0m\n",27);
            fflush(stdout);
          }
        }
      }
    }



  }




  return;
}


void ReadGameserver(int sock,char* buf) // 버그의 소지가 있음.. 고치는 것은 생각을좀 해보자.
{
  memset(buf,0,1024);
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

  recvgamesig = *(t-1);
  *(t-1) = 0;
  printf("gameserver <%d> is read  SIG is  = %d  \"%s\"\n",getpid(),recvgamesig,buf);
}
