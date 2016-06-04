essential: listenserver.o gameserver.o
	gcc -o server listenserver.o gameserver.o

listenserver.o: datastruct.h listenserver.c
	gcc -c listenserver.c

gameserver.o: datastruct.h gameserver.c
	gcc -c gameserver.c
