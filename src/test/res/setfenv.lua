-- object ids
package.path = "?.lua;src/test/res/?.lua"
require 'ids'

local id = id
local setfenv = setfenv
local print = print
local pcall = pcall
local create = coroutine.create
local resume = coroutine.resume
local seeall = package.seeall

-- unit tests for getfenv, setfenv
local function f3(level,value)
	if value then
		local t = {abc=value}
		seeall(t)
		setfenv( level, t )
	end
	return abc         
end
local function f2(...)
	local r = f3(...)
	return abc,r 
end
local function f1(...)
	local r,s = f2(...)
	print( ' ....... f1 returning ',abc,r,s )
	return abc,r,s
end
local function survey(msg)
	print('-------',msg)
	print( '   _G,f1,f2,f3', id(_G),id(f1),id(f2),id(f3) )
 	print( '   envs._G,f1,f2,f3', id(getfenv()),id(getfenv(f1)),id(getfenv(f2)),id(getfenv(f3)) )
	print( '   vals._G,f1,f2,f3', abc,getfenv(f1).abc, getfenv(f2).abc, getfenv(f3).abc )
	print( '   pcall(f1)', pcall(f1) )
end

survey( 'before')
setfenv(f1,{abc='ghi'})
setfenv(f2,{abc='jkl'})
setfenv(f3,{abc='mno'})
survey( 'after')
print( 'abc,f1(1,"pqr")', abc,f1(1,"pqr") )
print( 'abc,f1(2,"stu")', abc,f1(2,"stu") )
print( 'abc,f1(3,"vwx")', abc,f1(3,"vwx") )
survey( 'via f1()' )

local c = create( function()
	survey( 'coroutine-thread-before')
	print( 'f1(3,"abc")', f1(3,"567") )
	survey( 'coroutine-thread-1')
	print( 'f1(2,"abc")', f1(2,"456") )
	survey( 'coroutine-thread-2')
	print( 'f1(1,"abc")', f1(1,"345") )
	survey( 'coroutine-thread-3')
	print( 'f1(0,"abc")', f1(0,"234") )
	survey( 'coroutine-thread-after')
end )
print( 'resume', resume(c) )
survey( 'post-resume')
