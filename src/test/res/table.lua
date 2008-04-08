-- concat
print( '-- concat tests' )
function tryconcat(t)
	print( table.concat(t) )
	print( table.concat(t,'--') )
	print( table.concat(t,',',2) )
	print( table.concat(t,',',2,2) )
	print( table.concat(t,',',5,2) )
end
tryconcat( { "one", "two", "three", a='aaa', b='bbb', c='ccc' } )
tryconcat( { "one", "two", "three", "four", "five" } )
function tryconcat(t)
	print( table.concat(t) )
	print( table.concat(t,'--') )
	print( table.concat(t,',',2) )
end
tryconcat( { a='aaa', b='bbb', c='ccc', d='ddd', e='eee' } )
tryconcat( { [501]="one", [502]="two", [503]="three", [504]="four", [505]="five" } )
tryconcat( {} )

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
local t = { "one", "two", "three", a='aaa', b='bbb', c='ccc' }
print( eles(t) )
table.insert(t,'six'); print( eles(t) )
table.insert(t,1,'seven'); print( eles(t) )
table.insert(t,4,'eight'); print( eles(t) )
table.insert(t,7,'nine');  print( eles(t) )
table.insert(t,10,'ten');  print( eles(t) )

-- remove
print( '-- remove tests' )
t = { "one", "two", "three", "four", "five", "six", "seven", [10]="ten", a='aaa', b='bbb', c='ccc' }
print( eles(t) )
print( 'table.remove(t)', table.remove(t) ); print( eles(t) )
print( 'table.remove(t,1)', table.remove(t,1) ); print( eles(t) )
print( 'table.remove(t,3)', table.remove(t,3) ); print( eles(t) )
print( 'table.remove(t,5)', table.remove(t,5) ); print( eles(t) )
print( 'table.remove(t,10)', table.remove(t,10) ); print( eles(t) )
print( 'table.remove(t,-1)', table.remove(t,-1) ); print( eles(t) )
print( 'table.remove(t,-1)', table.remove(t,-1) ) ; print( eles(t) )

-- sort
print( '-- sort tests' )
function sorttest(t,f)
	t = (t)
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
t0 = {}
t1 = { 'one', 'two', 'three' }
t2 = { a='aa', b='bb', c='cc' }
t3 = { 'one', 'two', 'three', a='aa', b='bb', c='cc' }
print( 'getn('..eles(t0)..')', pcall( table.getn, t0 ) ) 
print( 'getn('..eles(t1)..')', pcall( table.getn, t1 ) ) 
print( 'getn('..eles(t2)..')', pcall( table.getn, t2 ) ) 
print( 'getn('..eles(t3)..')', pcall( table.getn, t3 ) ) 

-- foreach
function test( f, t, result, name ) 
	status, value = pcall( f, t, function(...) 
		print('  -- ',...)
		print('  next',next(t,(...)))
		return result 
	end )
	print( name, 's,v', status, value )
end
function testall( f, t, name ) 
	test( f, t, nil, name..'nil' )
	test( f, t, false, name..'fls' )
	test( f, t, 100, name..'100' )
end
testall( table.foreach, t0, 'table.foreach('..eles(t0)..')' )
testall( table.foreach, t1, 'table.foreach('..eles(t1)..')' )
testall( table.foreach, t2, 'table.foreach('..eles(t2)..')' )
testall( table.foreach, t3, 'table.foreach('..eles(t3)..')' )
testall( table.foreachi, t0, 'table.foreachi('..eles(t0)..')' )
testall( table.foreachi, t1, 'table.foreachi('..eles(t1)..')' )
testall( table.foreachi, t2, 'table.foreachi('..eles(t2)..')' )
testall( table.foreachi, t3, 'table.foreachi('..eles(t3)..')' )

-- pairs, ipairs
function testpairs(f, t, name)
	print( name )
	for a,b in f(t) do
		print( ' ', a, b )
	end
end
function testbothpairs(t)
	testpairs( pairs, t, 'pairs( '..eles(t)..' )' )
	testpairs( ipairs, t, 'ipairs( '..eles(t)..' )' )
end
for i,t in ipairs({t0,t1,t2,t3}) do
	testbothpairs(t)
end
t = { 'one', 'two', 'three', 'four', 'five' }
testbothpairs(t)
t[6] = 'six'
testbothpairs(t)
t[4] = nil
testbothpairs(t)
