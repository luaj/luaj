print( math.sin( 0.0 ) )
print( math.cos( math.pi ) )
print( math.sqrt( 9.0 ) )
print( math.modf( 5.25 ) )

local aliases = {
	['nan']='<nan>',
	['inf']='<pos-inf>',
	['-inf']='<neg-inf>',
	['1.#INF']='<pos-inf>',
	['-1.#INF']='<neg-inf>',
	['1.#IND']='<nan>',
	['-1.#IND']='<nan>',
}

local function normalized(x)
	local s = tostring(x)
	return aliases[s] or s
end

-- binary ops
function binops( a, b )
	local sa = tostring(a)
	local sb = tostring(b) 
	print( sa..'+'..sb..'='..normalized(a+b) )
	print( sa..'-'..sb..'='..normalized(a-b) )
	print( sa..'*'..sb..'='..normalized(a*b) )
	print( sa..'^'..sb..'='..normalized(a^b) )
	print( sa..'/'..sb..'='..normalized(a/b) ) 
	print( sa..'%'..sb..'='..normalized(a%b) )
	return '--' 
end
print( pcall( binops, 2, 0 ) )
print( pcall( binops, 2.5, 0 ) )
print( pcall( binops, -2.5, 0 ) )
print( pcall( binops, 2, 1 ) )
print( pcall( binops, 5, 2 ) )
print( pcall( binops, -5, 2 ) )
print( pcall( binops, 16, -2 ) )
print( pcall( binops, -16, -2 ) )
print( pcall( binops, 256, 0.5 ) )
print( pcall( binops, 256, 0.25 ) )
print( pcall( binops, 256, 0.625 ) )
print( pcall( binops, 256, -0.5 ) )
print( pcall( binops, 256, -0.25 ) )
print( pcall( binops, 256, -0.625 ) )
print( pcall( binops, -256, 0.5 ) )
print( pcall( binops, 0.5, 0 ) )
print( pcall( binops, 0.5, 1 ) )
print( pcall( binops, 0.5, 2 ) )
print( pcall( binops, 0.5, -1 ) )
print( pcall( binops, 0.5, -2 ) )
print( pcall( binops, 2.25, 0 ) )
print( pcall( binops, 2.25, 2 ) )
print( pcall( binops, 2.25, 0.5 ) )
print( pcall( binops, 2.25, 2.5 ) )
print( pcall( binops, -2, 0 ) )

-- random tests
print("Random tests")
local function testrandom(string,lo,hi)
	local c,e = loadstring('return '..string)
	for i=1,5 do 
		local s,e = pcall(c) 
		print( string, s and type(e) or e, (e>=lo) and (e<=hi) )
	end
end
testrandom('math.random()',0,1)
testrandom('math.random(5,10)',5,10)
testrandom('math.random(30)',0,30)
testrandom('math.random(-4,-2)',-4,-2)
local t = {} 
print( math.randomseed(20) )
for i=1,20 do
	t[i] = math.random()
end
print( '-- comparing new numbers')
for i=1,20 do
	print( t[i] == math.random(), t[i] == t[0] )
end
print( '-- resetting seed')
print( math.randomseed(20) )
for i=1,20 do
	print( t[i] == math.random() )
end
