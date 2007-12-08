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
