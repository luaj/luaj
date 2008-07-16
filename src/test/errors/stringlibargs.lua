package.path = "?.lua;src/test/errors/?.lua"
require 'args'

-- arg type tests for string library functions
local somestring = {astring,anumber}
local notastring = {nil,aboolean,afunction,atable}
local somenumber = {ainteger,adouble,tostring(ainteger),tostring(adouble)}
local notanumber = {nil,astring,aboolean,afunction,atable}

-- string.byte
banner('string.byte')
checkallpass('string.byte',{somestring,{nil,ainteger},{nil,ainteger}})
checkallerrors('string.byte',{somestring,{astring,afunction,atable}},'bad argument')
checkallerrors('string.byte',{notastring,{nil,111}},'bad argument')

-- string.char
banner('string.char')
checkallpass('string.char',{{nil,0,1,40,127,128,255,'0','1','255','1.2',1.2}})
checkallpass('string.char',{{0,127,255},{0,127,255}})
checkallerrors('string.char',{{-1,256}},'bad argument')
checkallerrors('string.char',{notanumber,notanumber},'bad argument')

-- string.dump
banner('string.dump')
checkallpass('string.dump',{{afunction}})
checkallerrors('string.dump',{notafunction},'bad argument')

-- string.find
banner('string.find')
checkallpass('string.find',{somestring,somestring})
checkallpass('string.find',{somestring,somestring,{nil,-3,3}})
checkallpass('string.find',{somestring,somestring,somenumber,anylua})
checkallerrors('string.find',{somestring,notastring},'bad argument')
checkallerrors('string.find',{notastring,somestring},'bad argument')
checkallerrors('string.find',{somestring,somestring,notanumber},'bad argument')

-- string.format
local numfmts = {'%c','%d','%E','%e','%f','%g','%G','%i','%o','%u','%X','%x'}
local strfmts = {'%q','%s'}
banner('string.format')
checkallpass('string.format',{somestring,anylua})
checkallpass('string.format',{numfmts,{ainteger,adouble,astrnum}})
checkallpass('string.format',{strfmts,{astring,ainteger,adouble,astrnum}})
checkallerrors('string.format',{numfmts,notastring},'bad argument')
checkallerrors('string.format',{strfmts,notastring},'bad argument')

-- string.gmatch
banner('string.gmatch')
checkallpass('string.gmatch',{somestring,somestring})
checkallerrors('string.gmatch',{somestring,notastring},'bad argument')
checkallerrors('string.gmatch',{notastring,somestring},'bad argument')

-- string.gsub
local somerepl = {astring,atable,afunction}
local notarepl = {nil,aboolean}
banner('string.gsub')
checkallpass('string.gsub',{somestring,somestring,somerepl,{nil,-1}})
checkallerrors('string.gsub',{notastring,somestring,somerepl},'bad argument')
checkallerrors('string.gsub',{somestring,notastring,somerepl},'bad argument')
checkallerrors('string.gsub',{{astring},{astring},notarepl},'bad argument')
checkallerrors('string.gsub',{{astring},{astring},somerepl,notanumber},'bad argument')

-- string.len
banner('string.len')
checkallpass('string.len',{somestring})
checkallerrors('string.len',{notastring},'bad argument')

-- string.lower
banner('string.lower')
checkallpass('string.lower',{somestring})
checkallerrors('string.lower',{notastring},'bad argument')

-- string.match
banner('string.match')
checkallpass('string.match',{somestring,somestring})
checkallpass('string.match',{somestring,somestring,{nil,-3,3}})
checkallerrors('string.match',{somestring,notastring},'bad argument')
checkallerrors('string.match',{notastring,somestring},'bad argument')
checkallerrors('string.match',{somestring,somestring,notanumber},'bad argument')

-- string.reverse
banner('string.reverse')
checkallpass('string.reverse',{somestring})
checkallerrors('string.reverse',{notastring},'bad argument')

-- string.rep
banner('string.rep')
checkallpass('string.rep',{somestring,somenumber})
checkallerrors('string.rep',{notastring,somenumber},'bad argument')
checkallerrors('string.rep',{somestring,notanumber},'bad argument')

-- string.sub
banner('string.sub')
checkallpass('string.sub',{somestring,somenumber,somenumber})
checkallerrors('string.sub',{notastring,somenumber,somenumber},'bad argument')
checkallerrors('string.sub',{somestring,notanumber,somenumber},'bad argument')
checkallerrors('string.sub',{somestring,somenumber,notanumber},'bad argument')

-- string.upper
banner('string.upper')
checkallpass('string.upper',{somestring})
checkallerrors('string.upper',{notastring},'bad argument')

