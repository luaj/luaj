local function f(x)
	-- tailcall to a builtin
	return math.sin(x)
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

local function truncate(x)
	local s = tostring(x)
	return (#s<6 and s) or string.sub(s,1,6)..'...'
end
local result2 = f(math.pi)
print(truncate(result2))
print(truncate(f(math.pi)))
