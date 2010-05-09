package.path = "?.lua;src/test/errors/?.lua"
require 'args'

-- arg type tests for module library functions

-- require
banner('require')
checkallpass('require',{{'math','coroutine','package','string','table'}},true)
checkallerrors('require',{{anumber}},'not found')
checkallerrors('require',{{anil,aboolean,afunction,atable}},'bad argument')

-- package.loadlib
banner('package.loadlib')
checkallpass('package.loadlib',{{'foo'},{'bar'}},true)
checkallerrors('package.loadlib',{notastring},'bad argument')

-- package.seeall
banner('package.seeall')
checkallpass('package.seeall',{sometable})
checkallerrors('package.seeall',{notatable},'bad argument')


-- module tests - require special rigging
banner('module')
print( pcall( function() 
	checkallpass('module',{{20001}})
end ) )
print( pcall( function() 
	checkallpass('module',{{20002},{package.seeall}})
end ) )
print( pcall( function() 
	checkallpass('module',{{20003},{package.seeall},{function() end}})
end ) )
print( pcall( function() 
	checkallerrors('module',{{aboolean,atable,function() end}},'bad argument')
	checkallerrors('module',{{aboolean,atable,function() end},{package.seeall}},'bad argument')
end ) )
print( pcall( function() 
	checkallerrors('module',{{'testmodule1'},{'pqrs'}},'attempt to call')
end ) )
print( pcall( function() 
	checkallerrors('module',{{'testmodule2'},{aboolean}},'attempt to call')
end ) )
print( pcall( function() 
	checkallerrors('module',{{'testmodule3'},{athread}},'attempt to call')
end ) )
print( pcall( function() 
	checkallerrors('module',{{'testmodule4'},{atable}},'attempt to call')
end ) )
