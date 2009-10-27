
-- tostring replacement that assigns ids
local ts,id,nid,types = tostring,{},0,{table='tbl',thread='thr',userdata='uda',['function']='func'}
tostring = function(x)
	if not x or not types[type(x)] then return ts(x) end
	if not id[x] then nid=nid+1; id[x]=types[type(x)]..'.'..nid end
	return id[x]
end

	
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
			print( " --f2, n<=0, returning sum(...)", ... )
		return sum(...)
	end
	print( " --f2, n>0, returning f2(n-1,n,...)", n-1,n,... )
	return f2(n-1,n,...)
end

local function f3(n,...)
	if n <= 0 then 
		return sum(...)
	end
	print( "    f3,n-1,n,...", f3,n-1,n,... )
	return pcall(f3,n-1,n,...)
end

local function all(f)
	for n=0,3 do
		t = {}
		for m=1,5 do 
			print( "--f, n, unpack(t)", f, n, unpack(t) )
			print( pcall( f, n, unpack(t)) )
			t[m] = m
		end
	end
end

all(f1)
all(f2)
all(f3)


local function f(x)
	-- tailcall to a builtin
	return math.abs(x)
end

local function factorial(i)
	local function helper(product, n)
		if n <= 0 then
			return product
		else
			-- tail call to a nested Lua function
			return helper(n * product, n - 1)
		end
	end
	return helper(1, i)
end

local result1 = factorial(5)
print(result1)
print(factorial(5))

local result2 = f(-1234)
print( result2 )
