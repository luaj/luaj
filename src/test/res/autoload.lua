local function autoload(table, key)
	local chunk = loadfile("/"..key..".luac")
	table[key] = chunk()
end

autoload_mt = { __index = autoload }

setmetatable(_G, autoload_mt)

print("square root of 9.0 is ", math.sqrt(9.0))
print("math.pi=", math.pi);
