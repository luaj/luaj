function printrunning()
	if coroutine.running() == nil then 
		print("running is nil");
	else
		print("running is not nil")
	end
end

function foo (a)
	print("foo", a)
    return coroutine.yield(2*a)
end
     
co = coroutine.create(function (a,b)
	print("co-body", a, b)
	local r = foo(a+1)
	print("co-body", r)
	local r, s = coroutine.yield(a+b, a-b)
	print("co-body", r, s)

	printrunning()
	print("co.status.inside",coroutine.status(co));
	local co2 = coroutine.create(function()
		print("co.status.inside2",coroutine.status(co));
	end)
	print("co.status.inside",coroutine.status(co));
	coroutine.resume(co2);
		
	return b, "end"
end)

function exercise() 
	printrunning()
	print("co.status",coroutine.status(co));
	print("main", coroutine.resume(co, 1, 10))
	print("co.status",coroutine.status(co));
	print("main", coroutine.resume(co, "r"))
	print("co.status",coroutine.status(co));
	print("main", coroutine.resume(co, "x", "y"))
	print("co.status",coroutine.status(co));
	print("main", coroutine.resume(co, "x", "y"))
	print("co.status",coroutine.status(co));
end

exercise();

co = coroutine.create(function (a,b)
	print("co-body", a, b)
	local statis,r = pcall( foo, a+1 )
	print("co-body", status,r)
	local r, s = coroutine.yield(a+b, a-b)
	print("co-body", r, s)
	return b, "end"
end)

exercise();

-- wrap test 
local g = coroutine.wrap(function (a,b)
	print("co-body", a, b)
	local r = foo(a+1)
	print("co-body", r)
	local r, s = coroutine.yield(a+b, a-b)
	print("co-body", r, s)
	return b, "end"
end )

print("g", g(1, 10))
print("g", g("r"))
print("g", g("x", "y"))
print("g", pcall( g, "x", "y" ))

