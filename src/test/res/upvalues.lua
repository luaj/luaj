function test()
	local x = 5
	function f()
		x = x + 1
		return x
	end
	function g()
		x = x - 1
		return x
	end
	print(f())
	print(g())
	return f, g
end

f1, g1 = test()
print("f1()=", f1())
print("g1()=", g1())

f2, g2 = test()
print("f2()=", f2())
print("g2()=", g2())

print("g1()=", g1())
print("f1()=", f1())
