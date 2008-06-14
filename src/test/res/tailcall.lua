
function a() 
	return pcall( function() end )
end

function b() 
	return pcall( function() print 'b' end )
end

function c() 
	return pcall( function() return 'c' end )
end

print( pcall( a )  )
print( pcall( b )  )
print( pcall( c )  )

local function sum(...)
	local s = 0
	for i,v in ipairs({...}) do
		s = s + v
	end
	return s
end

local function f1(n,a,b,c)
	local a = a or 0
	local b = b or 0
	local c = c or 0
	if n <= 0 then 
		return a,a+b,a+b+c
	end
	return f1(n-1,a,a+b,a+b+c)
end

local function f2(n,...)
	if n <= 0 then 
		return sum(...)
	end
	return f2(n-1,n,...)
end

local function f3(n,...)
	if n <= 0 then 
		return sum(...)
	end
	return pcall(f3,n-1,n,...)
end

local function all(f)
	for n=0,3 do
		t = {}
		for m=1,5 do 
			print( pcall( f, n, unpack(t)) )
			t[m] = m
		end
	end
end

all(f1)
all(f2)
all(f3)
