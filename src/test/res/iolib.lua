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

local h,s = io.open("abc.txt", "a")
print( 'h', io.type(h), s )
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

local count = 0
io.tmpfile = function()
	count = count + 1 
	return io.open("tmp"..count..".out","w")
end

local a = io.tmpfile()
local b = io.tmpfile()
print( io.type(a) )
print( io.type(b) )
print( "a:write", a:write('aaaaaaa') )
print( "b:write", b:write('bbbbbbb') )
print( "a:setvbuf", a:setvbuf("no") )
print( "a:setvbuf", a:setvbuf("full",1024) )
print( "a:setvbuf", a:setvbuf("line") )
print( "a:write", a:write('ccccc') )
print( "b:write", b:write('ddddd') )
print( "a:flush", a:flush() )
print( "b:flush", b:flush() )
--[[
print( "a:read", a:read(7) )
print( "b:read", b:read(7) )
print( "a:seek", a:seek("cur",-4) )
print( "b:seek", b:seek("cur",-4) )
print( "a:write", a:read(7) )
print( "b:write", b:read(7) )
--]]

local pcall = function(...)	return ( pcall(...) )end

print( 'a:close', pcall( a.close, a ) )
print( 'a:write', pcall( a.write, a, 'eee') )
print( 'a:flush', pcall( a.flush, a) )
print( 'a:read', pcall( a.read, a, 5) )
print( 'a:lines', pcall( a.lines, a) )
print( 'a:seek', pcall( a.seek, a, "cur", -2) )
print( 'a:setvbuf', pcall( a.setvbuf, a, "no") )
print( 'a:close', pcall( a.close, a ) )
print( 'io.type(a)', pcall( io.type, a ) )

print( 'io.close()', pcall( io.close ) ) 
print( 'io.close(io.output())', pcall( io.close, io.output() ) ) 

io.output('abc.txt')
print( 'io.close()', pcall( io.close ) ) 
print( 'io.write', pcall( io.write, 'eee') )
print( 'io.flush', pcall( io.flush) )
print( 'io.close', pcall( io.close ) )
io.input('abc.txt'):close()
print( 'io.read', pcall( io.read, 5) )
print( 'io.lines', pcall( io.lines) )

