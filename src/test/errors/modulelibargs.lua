package.path = "?.lua;src/test/errors/?.lua"
require 'args'

-- arg type tests for module library functions

-- require
banner('require')
checkallpass('require',{{'math'}})
checkallerrors('require',{{anumber}},'not found')
checkallerrors('require',{{anil,aboolean,afunction,atable}},'bad argument')

-- package.loadlib
banner('package.loadlib')
checkallpass('package.loadlib',{{'foo'},{'bar'}})
checkallerrors('package.loadlib',{notastring},'bad argument')

-- package.seeall
banner('package.seeall')
checkallpass('package.seeall',{sometable})
checkallerrors('package.seeall',{notatable},'bad argument')


-- module (last because it pretty much breaks this chunk)
print( pcall( function() 
	banner('module')
	checkallpass('module',{somenumber,{package.seeall},{nil,afunction}})
	checkallerrors('module',{{aboolean,atable,afunction}},'bad argument')
	checkallerrors('module',{{aboolean,atable,afunction},{package.seeall}},'bad argument')
	checkallerrors('module',{somestring,{astring,anumber,aboolean,atable}},'attempt to call a')
end ) )
