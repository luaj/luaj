package.path = "?.lua;src/test/errors/?.lua"
require 'args'

-- arg type tests for string library functions

-- string.byte
banner('string.byte')
checkallpass('string.byte',{somestring,{nil,ainteger},{nil,ainteger}})
checkallerrors('string.byte',{somestring,{astring,afunction,atable}},'bad argument')
checkallerrors('string.byte',{notastring,{nil,111}},'bad argument')

-- string.char
banner('string.char')
checkallpass('string.char',{{nil,0,1,40,127,128,255,'0','1','255','1.2',1.2}})
checkallpass('string.char',{{0,127,255},{0,127,255}})
checkallpass('string.char',{})
checkallerrors('string.char',{{-1,256}},'bad argument #1')
checkallerrors('string.char',{notanumber,{23,'45',6.7}},'bad argument #1')
checkallerrors('string.char',{{23,'45',6.7},nonnumber},'bad argument #2')

-- string.dump
banner('string.dump')
checkallpass('string.dump',{{afunction}})
checkallerrors('string.dump',{notafunction},'bad argument')

-- string.find
banner('string.find')
checkallpass('string.find',{somestring,somestring})
checkallpass('string.find',{somestring,somestring,{nil,-3,3}})
checkallpass('string.find',{somestring,somestring,somenumber,anylua})
checkallerrors('string.find',{notastring,somestring},'bad argument #1')
checkallerrors('string.find',{somestring,notastring},'bad argument #2')
checkallerrors('string.find',{somestring,somestring,nonnumber},'bad argument #3')

-- string.format
local numfmts = {'%c','%d','%E','%e','%f','%g','%G','%i','%o','%u','%X','%x'}
local strfmts = {'%q','%s'}
banner('string.format')
checkallpass('string.format',{somestring,anylua})
checkallpass('string.format',{numfmts,somenumber})
checkallpass('string.format',{strfmts,somestring})
checkallerrors('string.format',{numfmts,notanumber},'bad argument #2')
checkallerrors('string.format',{strfmts,notastring},'bad argument #2')

-- string.gmatch
banner('string.gmatch')
checkallpass('string.gmatch',{somestring,somestring})
checkallerrors('string.gmatch',{notastring,somestring},'bad argument #1')
checkallerrors('string.gmatch',{somestring,notastring},'bad argument #2')

-- string.gsub
local somerepl = {astring,atable,afunction}
local notarepl = {nil,aboolean}
banner('string.gsub')
checkallpass('string.gsub',{somestring,somestring,somerepl,{nil,-1}})
checkallerrors('string.gsub',{notastring,somestring,somerepl},'bad argument #1')
checkallerrors('string.gsub',{somestring,notastring,somerepl},'bad argument #2')
checkallerrors('string.gsub',{{astring},{astring},notarepl},'bad argument #3')
checkallerrors('string.gsub',{{astring},{astring},somerepl,nonnumber},'bad argument #4')

-- string.len
banner('string.len')
checkallpass('string.len',{somestring})
checkallerrors('string.len',{notastring},'bad argument #1')

-- string.lower
banner('string.lower')
checkallpass('string.lower',{somestring})
checkallerrors('string.lower',{notastring},'bad argument #1')

-- string.match
banner('string.match')
checkallpass('string.match',{somestring,somestring})
checkallpass('string.match',{somestring,somestring,{nil,-3,3}})
checkallerrors('string.match',{notastring,somestring},'bad argument #1')
checkallerrors('string.match',{somestring,notastring},'bad argument #2')
checkallerrors('string.match',{somestring,somestring,notanumber},'bad argument #3')

-- string.reverse
banner('string.reverse')
checkallpass('string.reverse',{somestring})
checkallerrors('string.reverse',{notastring},'bad argument #1')

-- string.rep
banner('string.rep')
checkallpass('string.rep',{somestring,somenumber})
checkallerrors('string.rep',{notastring,somenumber},'bad argument #1')
checkallerrors('string.rep',{somestring,notanumber},'bad argument #2')

-- string.sub
banner('string.sub')
checkallpass('string.sub',{somestring,somenumber,somenumber})
checkallerrors('string.sub',{notastring,somenumber,somenumber},'bad argument #1')
checkallerrors('string.sub',{somestring,notanumber,somenumber},'bad argument #2')
checkallerrors('string.sub',{somestring,somenumber,nonnumber},'bad argument #3')

-- string.upper
banner('string.upper')
checkallpass('string.upper',{somestring})
checkallerrors('string.upper',{notastring},'bad argument #1')

