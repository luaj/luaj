-- This file attemps to test that the setlist instruction works

local list = { 1, 2, 3 }

-- for now, can't just do:
--   for x, y in pairs( list ) do
-- since our tables don't iterate over keys in the same order
-- as regular Lua.

print( #list )
for i = 1, 3 do
	print("list[", i, "]=", list[i])
end

local function printList( l )
	for i = 1, #l do
		print(i, "->", l[i] )
	end
end

printList( { "a", "b", "c" } )

local function foo()
	return "d", "e", "f", "g"
end

printList( { foo() } )
