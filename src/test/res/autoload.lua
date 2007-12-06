-- Clear out builtin math package
math = nil

local function autoload(table, key)
	local pkg = require(key)
	table[key] = pkg
	return pkg
end

setmetatable(_G, { __index = autoload } )

-- local result = math.sqrt(9.0)
-- print("x=", result)
print("square root of 9.0 is ", math.sqrt(9.0))
print("square root of 4.0 is ", math.sqrt(4.0))
