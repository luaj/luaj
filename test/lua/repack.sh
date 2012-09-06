#!/bin/bash
#unzip -o luaj3.0-tests.zip
for DIR in "errors" "perf" "."; do
	cd ${DIR}
	FILES=`ls -1 *.lua | awk 'BEGIN { FS="." } ; { print $1 }'`
	echo "FILES" $FILES
	for FILE in $FILES ; do
		echo "executing ${FILE}.lua"
   		luac ${FILE}.lua 
   		mv luac.out ${FILE}.lc
   		lua ${FILE}.lua > ${FILE}.out
	done
	rm abc.txt
	cd ..
done
cd lua
rm -f luaj3.0-tests.zip
zip -9 luaj3.0-tests.zip *.lua *.lc *.out */*.lua */*.lc */*.out
