for l in *.lua
do
	if [ "$l" != "args.lua" ];
	then
		echo $l
		result=${l/\.lua/\.out}
		lua $l > jse/$result
	fi
done

grep -rHnae "^\(needcheck\|fail\|badmsg\)" jse/*.out
