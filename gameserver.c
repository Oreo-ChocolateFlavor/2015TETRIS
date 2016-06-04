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

void Gameserver(struct PIPE pip,int serverport)
{
  int owner;

  printf("GAMESERVER is Created\n");

  int gameserver_sock, client_sock;
  struct sockaddr_in  server_addr,client_addr;
  fd_set oldset,newset;

  int fdname,nfd;

  if((gameserver_sock = socket(PF_INET,SOCK_STREAM,0)) < 0)
  {
    perror("game sock() err");
    exit(1);
  }

  memset(&server_addr,0,sizeof(struct sockaddr_in));

  server_addr.sin_family = AF_INET;
  server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
  server_addr.sin_port = htonl(serverport);

  if(bind(gameserver_sock,(struct sockaddr*)&server_addr,sizeof(server_addr)) < 0)
  {
    perror("game bind() err");
    exit(1);
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
        if(FD_ISSET(per[i].client_sock,&newset)) // 클라이언트들로부터 요청이오면
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
          else{
            printf("Nah... error\n");
          }
        }
      }

    }



  }




  return;
}
