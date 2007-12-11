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

-- insert, maxn
print( '-- insert, maxn tests' )
local t = { "one", "two", "three", a='aaa', b='bbb', c='ccc' }
print( table.concat(t,'-'), table.maxn(t), #t, table.getn(t) )
table.insert(t,'six') 
print( table.concat(t,'-'), table.maxn(t), #t, table.getn(t) )
table.insert(t,1,'seven') 
print( table.concat(t,'-'), table.maxn(t), #t, table.getn(t) )
table.insert(t,4,'eight') 
print( table.concat(t,'-'), table.maxn(t), #t, table.getn(t) )
table.insert(t,7,'nine') 
print( table.concat(t,'-'), table.maxn(t), #t, table.getn(t) )
table.insert(t,10,'ten') 
print( table.concat(t,'-'), table.maxn(t), #t, table.getn(t) )
print( t[10] )
print( table.maxn({}), #{} )

-- remove
print( '-- remove tests' )
t = { "one", "two", "three", "four", "five", "six", "seven", [10]="ten", a='aaa', b='bbb', c='ccc' }
print( table.concat(t,'-'), table.maxn(t), #t )
print( table.remove(t) )
print( table.concat(t,'-'), table.maxn(t) )
print( table.remove(t,1) )
print( table.concat(t,'-'), table.maxn(t) )
print( table.remove(t,3) )
print( table.concat(t,'-'), table.maxn(t) )
print( table.remove(t,5) )
print( table.concat(t,'-'), table.maxn(t), t[10] )
print( table.remove(t,10) )
print( table.concat(t,'-'), table.maxn(t), t[10] )
print( table.remove(t,-1) )
print( table.concat(t,'-'), table.maxn(t), t[10] )
print( table.remove(t,-1) ) 
print( table.concat(t,'-'), table.maxn(t), t[10] )

-- sort
print( '-- sort tests' )
t = { "one", "two", "three", a='aaa', b='bbb', c='ccc' }
print( table.concat(t,'-'), table.maxn(t), #t )
table.sort(t)
print( table.concat(t,'-'), table.maxn(t), #t )
t = { "zzz", "yyy", "xxx", "www", "vvv", "uuu", "ttt", "sss" }
print( table.concat(t,'-'), table.maxn(t), #t )
table.sort(t)
print( table.concat(t,'-'), table.maxn(t), #t )
table.sort(t,function(a,b) return b<a end)
print( table.concat(t,'-'), table.maxn(t), #t )

-- getn
t0 = {}
t1 = { 'one', 'two', 'three' }
t2 = { a='aa', b='bb', c='cc' }
t3 = { 'one', 'two', 'three', a='aa', b='bb', c='cc' }
print( 'getn(t0)', pcall( table.getn, t0 ) ) 
print( 'getn(t0)', pcall( table.getn, t1 ) ) 
print( 'getn(t0)', pcall( table.getn, t2 ) ) 
print( 'getn(t0)', pcall( table.getn, t3 ) ) 

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

