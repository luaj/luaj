local f

do
	local x = 10
	function g()
		print(x, f())
	end
end

function f()
	return 20
end

g()
