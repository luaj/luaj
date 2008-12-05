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

local h = io.open("abc.txt", "a")
print( 'h', io.type(h) )
print( 'write', h:write('\nmore text\neven more text\n') )
print( 'close', h:close() )

local j = io.open( "abc.txt", "r" )
print( 'j', io.type(j) )
print( 'seek', j:seek("set", 3) )
print( 'read', j:read(4), j:read(3) )
print( 'seek', j:seek("set", 2) )
print( 'read', j:read(4), j:read(3) )
print( 'seek', j:seek("cur", -8 ) )
print( 'read', j:read(4), j:read(3) )
print( 'seek(cur,0)', j:seek("cur",0) )
print( 'seek(cur,20)', j:seek("cur",20) )
print( 'seek(end,-5)', j:seek("end", -5) )
print( 'read(4)', string.format("%q", tostring(j:read(4))) )
print( 'read(4)', string.format("%q", tostring(j:read(4))) )
print( 'read(4)', string.format("%q", tostring(j:read(4))) )

for l in io.lines("abc.txt") do
	print( string.format('%q',l) )
end
io.input("abc.txt")
for l in io.lines() do
	print( string.format('%q',l) )
end
io.input(io.open("abc.txt","r"))
for l in io.lines() do
	print( string.format('%q',l) )
end
io.input("abc.txt")
io.input(io.input())
for l in io.lines() do
	print( string.format('%q',l) )
end

