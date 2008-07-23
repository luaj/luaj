-- print uses tostring under-the-hood!

local function f()
	print()
	print('abc')
	print(123)
	print(true)
	print('abc',123,true)
end

local function g()	
	local fenv = {tostring=function(x)
		return '*'..type(x)..'*'
	end}
	package.seeall(fenv)	
	f()
	print('setfenv', pcall(setfenv, 0, fenv), {}, f )
	f()
end

local s,c = pcall( coroutine.create, g )
print('create', s, s and type(c) or c)
print('resume', pcall( coroutine.resume, c ) )
f()


