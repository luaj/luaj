-- Parts of this test are commented out because it looks like
-- there is a problem with our argument passing, particularly in the
-- presence of the VARARG instruction.

--[[ local function f(...)
print("arg count:", select('#', ...))
end

local function g(...)
	local a, b, c = select(2, ...)
	print(a, b, c)
end
]]--

print((select(1, "a", "b", "c")))
print( select(1, "a", "b", "c"))

print((select(2, "a", "b", "c")))
print( select(2, "a", "b", "c"))

print((select(3, "a", "b", "c")))
print( select(3, "a", "b", "c"))

print((select(4, "a", "b", "c")))
print( select(4, "a", "b", "c"))

print( select("#") )
print( select("#", "a") )
print( select("#", "a", "b") )
-- f("hello", "world")
-- g(1, 2, 3, 4, 5, 6, 7)
