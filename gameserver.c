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

void ReadMessage(int sock,char* buf);

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

      if(persontop == 0) owner = client_sock;
      // id 를 받아오는 과정.

      persontop++;
    }
    else
    {
      for(int i=0; i<persontop; i++)
      {

      }

    }



  }




  return;
}
