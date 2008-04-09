
-- call with arg 'true' to turn on error messages
local messages = select(1,...)
function mpcall(...)
	if messages then return pcall(...)
	else return (pcall(...)) end
end

-- unit tests for the next() function
function checkvalues(tag, tbl)
	local values = {}
	local index,value = next(tbl,nil)
	while index do
		table.insert( values, tostring(index).."="..tostring(value) )
		index,value = next(tbl,index)
	end
	table.sort( values )
	print( tag, "values: {"..table.concat(values,",").."}" )		
end
function donexts(tag,tbl,count)
	checkvalues(tag,tbl)
	print( tag, '--- -1', 'pcall( next, tbl,-1 )', mpcall( next, tbl,-1 ) )
	print( tag, '---  0', 'pcall( next, tbl,0 )', mpcall( next, tbl,0 ) )
	print( tag, '---"a"', 'pcall( next, tbl,"a" )', mpcall( next, tbl,"a" ) )
	print( tag, '--- 10', 'pcall( next, tbl, 10 )', mpcall( next, tbl, 10 ) )
end
donexts( 'next1', {}, 2 )
donexts( 'next2', {'one', 'two', 'three' }, 5 )
donexts( 'next3', { aa='aaa', bb='bbb', cc='ccc', [20]='20', [30]='30'}, 7 )
donexts( 'next4', {'one', 'two', 'three', aa='aaa', bb='bbb', cc='ccc', [20]='20', [30]='30'}, 9 )
donexts( 'next5', {'one', 'two', 'three', [-1]='minus-one', [0]='zero' }, 7 )

print( 'pcall(next)', mpcall(next) )
print( 'pcall(next,nil)', mpcall(next,nil) )
print( 'pcall(next,"a")', mpcall(next,"a") )
print( 'pcall(next,1)', mpcall(next,1) )

