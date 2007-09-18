-- Clear out builtin math package
math = nil

local function autoload(table, key)
	local chunk = loadfile(key..".luac")
	local pkg = chunk()
	table[key] = pkg
	return pkg
end

setmetatable(_G, { __index = autoload } )

-- local result = math.sqrt(9.0)
-- print("x=", result)
print("square root of 9.0 is ", math.sqrt(9.0))
print("math.pi=", math.pi);
