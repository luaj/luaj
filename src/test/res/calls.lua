local function f(x)
	-- tailcall to a builtin
	return math.sin(x)
end

local function factorial(i)
	local function helper(sum, n)
		if n <= 0 then
			return sum
		else
			-- tail call to a nested Lua function
			return helper(n + sum, n - 1)
		end
	end
	return helper(0, i)
end

local result1 = factorial(5)
print(result1)
print(factorial(5))

local result2 = f(math.pi)
print(result2)
print(f(math.pi))
