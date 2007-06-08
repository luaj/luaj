#!/bin/bash
LUA_HOME=/cygdrive/c/programs/lua5.1
for x in test1 test2 test3 test4
do
    echo compiling $x
	${LUA_HOME}/luac5.1.exe -l -o ${x}.luac ${x}.lua
done
