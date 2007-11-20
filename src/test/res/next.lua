-- unit tests for the next() function
function donexts(tag,table,count)
	local index = nil
	for i = 1,count do
		index,value = next(table,index)
		print( tag, index, value )
	end
	print( tag, '--- -1', 'pcall( next, table,-1 )', pcall( next, table,-1 ) )
	print( tag, '---  0', 'pcall( next, table,0 )', pcall( next, table,0 ) )
	print( tag, '---"a"', 'pcall( next, table,"a" )', pcall( next, table,"a" ) )
	print( tag, '--- 10', 'pcall( next, table, 10 )', pcall( next, table, 10 ) )
end
donexts( 'next1', {}, 2 )
donexts( 'next2', {'one', 'two', 'three' }, 5 )
donexts( 'next3', { aa='aaa', bb='bbb', cc='ccc', [20]='20', [30]='30'}, 7 )
donexts( 'next4', {'one', 'two', 'three', aa='aaa', bb='bbb', cc='ccc', [20]='20', [30]='30'}, 9 )
donexts( 'next5', {'one', 'two', 'three', [-1]='minus-one', [0]='zero' }, 7 )

print( 'pcall(next)', pcall(next) )
print( 'pcall(next,nil)', pcall(next,nil) )
print( 'pcall(next,"a")', pcall(next,"a") )
print( 'pcall(next,1)', pcall(next,1) )

