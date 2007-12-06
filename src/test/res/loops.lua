-- This script tests the "generic" for loop with a script iterator.

local function stuff()
	local function i(o)
		if o.counter > 3 then
			return nil
		else
			local v = o.counter
			o.counter = v + 1
			return v
		end
	end
	return i, { counter=1 }
end

local function testfor()
	for x in stuff() do
		print(x)
	end
end

testfor()
