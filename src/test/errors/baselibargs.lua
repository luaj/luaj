package.path = "?.lua;src/test/errors/?.lua"
require 'args'


-- arg types for basic library functions
local somestrnumnil={anil,astring,anumber}
local notastrnumnil={aboolean,atable,afunction}

-- assert
banner('assert')
checkallpass('assert',{{true,123},anylua})
checkallfail('assert',{{nil,false},{nil,'message'}})
checkallerrors('assert',{{nil,false},{nil}},'assertion failed')
checkallerrors('assert',{{nil,false},{'message'}},'message')

-- collectgarbage
banner('collectgarbage')
checkallpass('collectgarbage',{{'collect','count'}})
checkallerrors('collectgarbage',{notanil},'bad argument')

-- dofila
banner('dofile')
checkallpass('dofile', {{nil,'src/test/errors/args.lua','args.lua'}})
checkallerrors('dofile', {notastrnumnil}, 'bad argument')

-- error
banner('error')
checkallfail('error', {anylua,{nil,0,1,2}})
checkallerrors('dofile', {{'message'},{nil,0,1,2}}, 'message')
checkallerrors('dofile', {{123},{nil,1,2}}, 123)

-- getfenv
banner('getfenv')
checkallpass('getfenv', {{nil,print,function()end,0,1,2}})
checkallerrors('getfenv', {{true,{},'abc'}}, 'bad argument')

-- getmetatable
banner('getmetatable')
checkallpass('getmetatable', {notanil})
checkfail('getmetatable',nil)

-- ipairs
banner('ipairs')
checkallpass('ipairs', {sometable})
checkallerrors('ipairs', {notatable}, 'bad argument')

-- load
banner('load')
checkallpass('load', {somefunction,{anil,astring}})
checkallerrors('load', {notafunction,{anil,astring,anumber}}, 'bad argument')
checkallerrors('load', {somefunction,{afunction,atable}}, 'bad argument')

-- loadfile
banner('loadfile')
checkallerrors('loadfile', {notastring}, 'bad argument')

-- loadstring
banner('loadstring')
checkallpass('loadstring', {{'return'},{anil,astring}})
checkallerrors('loadstring', {notastring,{anil,astring,anumber}}, 'bad argument')
checkallerrors('loadstring', {{'return'},{afunction,atable}}, 'bad argument')

-- next
banner('next')
checkallpass('next', {{{aa=11}},{nil,'aa'}})
checkallerrors('next', {notatable,{nil,1}}, 'bad argument')
checkallerrors('next', {sometable,{astring,afunction,atable}}, 'invalid key')

-- pairs
banner('pairs')
checkallpass('pairs', {sometable})
checkallerrors('pairs', {notatable}, 'bad argument')

