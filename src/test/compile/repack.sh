#!/bin/bash
LUA_HOME=/cygdrive/c/programs/lua5.1
#DIRS="lua5.1-tests regressions"
DIRS="regressions"
for d in $DIRS; do

	# clean out the old
	rm -f $d/*.luac
	
	# compile the tests
	TESTS=`echo $d/*.lua`
	for x in $TESTS; do
		echo compiling $x
		luac -o ${x}c ${x}
	done
	
	# rebuild the directory
	rm -f ${d}.zip
	jar -cvf ${d}.zip ${d}
done