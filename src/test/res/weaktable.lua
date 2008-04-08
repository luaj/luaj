-- concat
print( '-- weak table tests' )

-- construct new weak table
function newweak(t) 
	return setmetatable(t,{__mode="v"})	
end

-- print the elements of a table in a platform-independent way
function eles(t,f)
	f = f or pairs
	all = {}
	for k,v in f(t) do
		table.insert( all, "["..tostring(k).."]="..tostring(v) )
	end
	table.sort( all )
	return "{"..table.concat(all,',').."}"
end

-- insert, maxn
print( '-- insert, maxn tests' )
local t = newweak{ "one", "two", "three", a='aaa', b='bbb', c='ccc' }
print( eles(t) )
table.insert(t,'six'); print( eles(t) )
table.insert(t,1,'seven'); print( eles(t) )
table.insert(t,4,'eight'); print( eles(t) )
table.insert(t,7,'nine');  print( eles(t) )
table.insert(t,10,'ten');  print( eles(t) )

-- remove
print( '-- remove tests' )
t = newweak{ "one", "two", "three", "four", "five", "six", "seven", [10]="ten", a='aaa', b='bbb', c='ccc' }
print( eles(t) )
print( table.remove(t) ); print( eles(t) )
print( table.remove(t,1) ); print( eles(t) )
print( table.remove(t,3) ); print( eles(t) )
print( table.remove(t,5) ); print( eles(t) )
print( table.remove(t,10) ); print( eles(t) )
print( table.remove(t,-1) ); print( eles(t) )
print( table.remove(t,-1) ) ; print( eles(t) )

-- sort
print( '-- sort tests' )
function sorttest(t,f)
	t = newweak(t)
	print( table.concat(t,'-') )
	if f then
		table.sort(t,f)
	else	
		table.sort(t)
	end
	print( table.concat(t,'-') )
end	
sorttest{ "one", "two", "three" }
sorttest{  "www", "vvv", "uuu", "ttt", "sss", "zzz", "yyy", "xxx" }
sorttest( {  "www", "vvv", "uuu", "ttt", "sss", "zzz", "yyy", "xxx" }, function(a,b) return b<a end)

-- getn
t0 = newweak{}
t1 = newweak{ 'one', 'two', 'three' }
t2 = newweak{ a='aa', b='bb', c='cc' }
t3 = newweak{ 'one', 'two', 'three', a='aa', b='bb', c='cc' }
print( 'getn(t0)', pcall( table.getn, t0 ) ) 
print( 'getn(t1)', pcall( table.getn, t1 ) ) 
print( 'getn(t2)', pcall( table.getn, t2 ) ) 
print( 'getn(t3)', pcall( table.getn, t3 ) ) 

-- foreach
function test( f, t, result, name ) 
	status, value = pcall( f, t, function(...) 
		print(name,...)
		return result 
	end )
	print( name, 's,v', status, value )
end
function testall( f, t, name ) 
	test( f, t, nil, name..'nil' )
	test( f, t, false, name..'fls' )
	test( f, t, 100, name..'100' )
end
testall( table.foreach, t0, 'table.foreach(t0)' )
testall( table.foreach, t1, 'table.foreach(t1)' )
testall( table.foreach, t2, 'table.foreach(t2)' )
testall( table.foreach, t3, 'table.foreach(t3)' )
testall( table.foreachi, t0, 'table.foreachi(t0)' )
testall( table.foreachi, t1, 'table.foreachi(t1)' )
testall( table.foreachi, t2, 'table.foreachi(t2)' )
testall( table.foreachi, t3, 'table.foreachi(t3)' )

-- pairs, ipairs
function testpairs(f, t, name)
	print( name, unpack(t) )
	for a,b in f(t) do
		print( a, b )
	end
end
testpairs( pairs, t0, 'pairs(t0)' )
testpairs( pairs, t1, 'pairs(t1)' )
testpairs( pairs, t2, 'pairs(t2)' )
testpairs( pairs, t3, 'pairs(t3)' )
testpairs( ipairs, t0, 'ipairs(t0)' )
testpairs( ipairs, t1, 'ipairs(t1)' )
testpairs( ipairs, t2, 'ipairs(t2)' )
testpairs( ipairs, t3, 'ipairs(t3)' )


