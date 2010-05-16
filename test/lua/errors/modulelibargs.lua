package.path = "?.lua;test/lua/errors/?.lua"
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
checkallerrors('module',{{20001},{nil,package.seeall,n=2},{nil,function()end,n=2}},"'module' not called from a Lua function")
checkallerrors('module',{{'testmodule1'},{nil,'pqrs',aboolean,athread,atable}},"'module' not called from a Lua function")
checkallerrors('module',{{aboolean,atable,function() end}},'bad argument')
checkallerrors('module',{{aboolean,atable,function() end},{package.seeall}},'bad argument')

-- enclose each invokation in its own function
function invoke( name, arglist )
	assert( name=='module', 'module rig used for '..name )
	local func = function()
		module( unpack(arglist,1,arglist.n or #arglist) )
	end
	return pcall( func )
end
checkallpass('module',{{'foo1',20001}})
checkallpass('module',{{'foo2',20002},{package.seeall}})
checkallpass('module',{{'foo3',20003},{package.seeall},{function() end}})
checkallerrors('module',{{aboolean,atable,function() end}},'bad argument')
checkallerrors('module',{{aboolean,atable,function() end},{package.seeall}},'bad argument')
checkallerrors('module',{{'testmodule2'},{'pqrs'}},'attempt to call')
checkallerrors('module',{{'testmodule3'},{aboolean}},'attempt to call')
checkallerrors('module',{{'testmodule4'},{athread}},'attempt to call')
checkallerrors('module',{{'testmodule5'},{atable}},'attempt to call')
