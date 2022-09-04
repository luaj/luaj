for l in *.lua
do
    echo $l
    result=${l/\.lua/\.out}
    lua $l       > jse/$result
    lua $l 'JME' > jme/$result
    luajit $l    > luajit/$result
done

# TODO Test is currently disabled
rm luajit/debuglib.out
