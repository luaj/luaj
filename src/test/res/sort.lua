-- concat
print( '-- sort tests' )
local function tryall(cmp)
	local function try(t)
		print( table.concat(t,'-') )
		if pcall( table.sort, t, cmp ) then
			print( table.concat(t,'-') )
		else
			print( 'sort failed' )
		end
	end
	try{ 2, 4, 6, 8, 1, 3, 5, 7 }
	try{ 333, 222, 111 }
	try{ "www", "xxx", "yyy", "aaa", "bbb", "ccc" }
	try{ 21, 23, "25", 27, 22, "24", 26, 28 }
end
local function comparator(a,b)
	return tonumber(a)<tonumber(b) 
end
tryall()
tryall(comparator)






