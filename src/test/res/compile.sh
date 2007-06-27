#!/bin/bash
LUA_HOME=/cygdrive/c/programs/lua5.1
TESTS="test1 test2 test3 test4 test5 swingapp"
TESTS="test2"
for x in $TESTS
do
    echo compiling $x
	${LUA_HOME}/luac5.1.exe -l -o ${x}.luac ${x}.lua
	${LUA_HOME}/lua5.1.exe ${x}.luac
done
