-- unit tests for getfenv, setfenv
local function f3(level,value)
	if value then
		setfenv( level, {setfenv=setfenv, abc=value} )
	end
	return abc         
end
local function f2(...)
	local r = f3(...)
	return abc,r 
end
local function f1(...)
	local r,s = f2(...)
	return abc,r,s
end
print( 'getfenv,setfenv - before')
print( 'getfenv(f1)["abc"]', getfenv(f1)["abc"] ) 
print( 'getfenv(f2)["abc"]', getfenv(f2)["abc"] ) 
print( 'getfenv(f3)["abc"]', getfenv(f3)["abc"] ) 
print( 'getfenv()["abc"]', getfenv()["abc"] ) 
print( 'abc,f1()', abc,f1() )
setfenv(f1,{setfenv=setfenv, abc='ghi'})
setfenv(f2,{setfenv=setfenv, abc='jkl'})
setfenv(f3,{setfenv=setfenv, abc='mno'})
print( 'getfenv,setfenv - after')
print( 'getfenv(f1)["abc"]', getfenv(f1)["abc"] ) 
print( 'getfenv(f2)["abc"]', getfenv(f2)["abc"] ) 
print( 'getfenv(f3)["abc"]', getfenv(f3)["abc"] ) 
print( 'getfenv()["abc"]', getfenv()["abc"] ) 
print( 'abc,f1()', abc,f1() )
print( 'abc,f1(1,"pqr")', abc,f1(1,"pqr") )
print( 'abc,f1(2,"stu")', abc,f1(2,"stu") )
print( 'abc,f1(3,"vwx")', abc,f1(3,"vwx") )
print( 'abc', abc )
local c = coroutine.create( function()
	print( 'pcall(f1,0,"abc")', pcall(f1,0,"234") )
end )
print( 'resume', coroutine.resume(c) )
print( 'abc,pcall(f1)', abc,pcall(f1) )
print( 'abc (out)', abc )
