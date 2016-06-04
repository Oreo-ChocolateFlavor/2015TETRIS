essential: listenserver.o gameserver.o
	gcc -o server listenserver.o gameserver.o

listenserver.o: listenserver.c
	gcc -c listenserver.c

gameserver.o: gameserver.c
	gcc -c gameserver.c
