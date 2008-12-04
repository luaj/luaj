-- simple io-library tests
print( io ~= nil )
print( io.open ~= nil )
print( io.stdin ~= nil )
print( io.stdout ~= nil )
print( io.stderr ~= nil )
print( 'write', io.write() )
print( 'write', io.write("This") )
print( 'write', io.write(" is a pen.\n") )
print( 'flush', io.flush() )

local f = io.open("abc.txt","w")
print( 'f', type(f) )
print( io.type(f) )
print( 'write', f:write("abcdef 12345 \t\r\n\t 678910 more\naaaaaa\rbbb\nthe rest") )
print( 'type(f)', io.type(f) )
print( 'close', f:close() )
print( 'type(f)', io.type(f) )
print( 'type("f")', io.type("f") )

local g = io.open("abc.txt","r")
local t = { g:read(3, 3, "*n", "*n", "*l", "*l", "*a") }
for i,v in ipairs(t) do
	print( string.format("%q",tostring(v)), type(v))
end

