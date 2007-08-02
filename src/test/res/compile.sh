#!/bin/bash
TESTS=`echo *.lua`
TESTS="test3.lua"
for x in $TESTS
do
    echo compiling $x
	luac -l -o ${x}c ${x}
	lua ${x}c
done
