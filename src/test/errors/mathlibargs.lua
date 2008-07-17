package.path = "?.lua;src/test/errors/?.lua"
require 'args'

-- arg type tests for math library functions
local somenumber = {23,45.67,'-12','-345.678'}
local notanumber = {nil,astring,aboolean,afunction,atable,athread}
local nonnumber = {astring,aboolean,afunction,atable}

local singleargfunctions = { 
	'abs', 'acos', 'asin', 'atan', 'ceil', 'cos', 'cosh', 'deg', 'exp', 'floor', 'frexp', 
	'log', 'log10', 'rad', 'randomseed', 'sin', 'sinh', 'sqrt', 'tan', 'tanh', 
}
	
local twoargfunctions = { 
	'atan2', 'fmod', 'pow', 
}

-- single argument tests
for i,v in ipairs(singleargfunctions) do
	local funcname = 'math.'..v
	banner(funcname)
	checkallpass(funcname,{somenumber})
	checkallerrors(funcname,{notanumber},'bad argument #1')
end	

-- two-argument tests
for i,v in ipairs(twoargfunctions) do
	local funcname = 'math.'..v
	banner(funcname)
	checkallpass(funcname,{somenumber,somenumber})
	checkallerrors(funcname,{},'bad argument #1')
	checkallerrors(funcname,{notanumber},'bad argument #1')
	checkallerrors(funcname,{notanumber,somenumber},'bad argument #1')
	checkallerrors(funcname,{somenumber},'bad argument #2')
	checkallerrors(funcname,{somenumber,notanumber},'bad argument #2')
end

-- math.max
banner('math.max')
checkallpass('math.max',{somenumber})
checkallpass('math.max',{somenumber,somenumber})
checkallerrors('math.max',{},'bad argument #1')
checkallerrors('math.max',{nonnumber},'bad argument #1')
checkallerrors('math.max',{somenumber,nonnumber},'bad argument #2')

-- math.min
banner('math.min')
checkallpass('math.min',{somenumber})
checkallpass('math.min',{somenumber,somenumber})
checkallerrors('math.min',{},'bad argument #1')
checkallerrors('math.min',{nonnumber},'bad argument #1')
checkallerrors('math.min',{somenumber,nonnumber},'bad argument #2')

-- math.random
local somem = {3,4.5,'6.7'}
local somen = {8,9.10,'12.34'}
local notamn = {astring,aboolean,atable,afunction}
banner('math.random')
checkallpass('math.random',{})
checkallpass('math.random',{somem})
checkallpass('math.random',{somem,somen})
checkallpass('math.random',{{8},{7.8}})
checkallpass('math.random',{{-4,-5.6,'-7','-8.9'},{-1,100,23.45,'-1.23'}})
checkallerrors('math.random',{{-4,-5.6,'-7','-8.9'}},'interval is empty')
checkallerrors('math.random',{somen,somem},'interval is empty')
checkallerrors('math.random',{notamn,somen},'bad argument #1')
checkallerrors('math.random',{somem,notamn},'bad argument #2')

-- math.ldexp
local somee = {-3,0,3,9.10,'12.34'}
local notae = {nil,astring,aboolean,atable,afunction}
banner('math.ldexp')
checkallpass('math.ldexp',{somenumber,somee})
checkallerrors('math.ldexp',{},'bad argument #2')
checkallerrors('math.ldexp',{notanumber},'bad argument #2')
checkallerrors('math.ldexp',{notanumber,somee},'bad argument #1')
checkallerrors('math.ldexp',{somenumber},'bad argument #2')
checkallerrors('math.ldexp',{somenumber,notae},'bad argument #2')