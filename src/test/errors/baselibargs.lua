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
checkallerrors('getmetatable',{},'bad argument')

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
-- checkallpass('next', {{{aa=11}},{nil,'aa'}})
checkallpass('next', {sometable,somekey})
checkallerrors('next', {notatable,{nil,1}}, 'bad argument')
checkallerrors('next', {sometable,notakey}, 'invalid key')

-- pairs
banner('pairs')
checkallpass('pairs', {sometable})
checkallerrors('pairs', {notatable}, 'bad argument')

-- pcall
banner('pcall')
checkallpass('pcall', {notanil,anylua})
checkallerrors('pcall',{},'bad argument')

-- print
banner('print')
checkallpass('print', {}) 
checkallpass('print', {{anil,astring,anumber,aboolean}}) 

-- rawequal
banner('rawequal')
checkallpass('rawequal', {notanil,notanil})
checkallerrors('rawequal', {notanil}, 'bad argument')
checkallerrors('rawequal', {}, 'bad argument')

-- rawget
banner('rawget')
checkallpass('rawget', {sometable,somekey})
checkallpass('rawget', {sometable,notakey})
checkallerrors('rawget', {notatable,notakey}, 'bad argument')
checkallerrors('rawget', {}, 'bad argument')

-- rawset
banner('rawset')
checkallpass('rawset', {sometable,somekey,notanil})
checkallpass('rawset', {sometable,notakey,notanil})
checkallerrors('rawset', {sometable,somekey}, 'bad argument')
checkallerrors('rawset', {notatable,somestring,somestring}, 'bad argument')
checkallerrors('rawset', {}, 'bad argument')

-- select
banner('select')
checkallpass('select', {{anumber,'#'},anylua})
checkallerrors('select', {notanumber}, 'bad argument')

-- setfenv
banner('setfenv')
checkallpass('setfenv', {{function()end},sometable})
checkallerrors('setfenv', {{function()end}}, 'bad argument')
checkallerrors('setfenv', {{function()end},notatable}, 'bad argument')

-- setmetatable
banner('setmetatable')
checkallpass('setmetatable', {sometable,sometable})
checkallpass('setmetatable', {sometable,{anil,atable},{'anchor'}})
checkallerrors('setmetatable',{notatable,sometable},'bad argument')
checkallerrors('setmetatable',{sometable,notatable},'bad argument')

-- tonumber
banner('tonumber')
checkallpass('tonumber',{somestrnum,{nil,2,10,36}})
checkallpass('tonumber',{notastrnum,{nil,10}})
checkallerrors('tonumber',{notastrnum,{2,9,11,36}},'bad argument')
checkallerrors('tonumber',{somestrnum,{1,37,atable,afunction,aboolean}},'bad argument')

-- tostring
banner('tostring')
checkallpass('tostring',{notanil})
checkallpass('tostring',{anylua,{'anchor'}})
checkallerrors('tostring',{},'bad argument')

-- type
banner('type')
checkallpass('type',{notanil})
checkallpass('type',{anylua,{'anchor'}})
checkallerrors('type',{},'bad argument')

-- unpack
banner('unpack')
checkallpass('unpack',{sometable,{nil,anumber,astrnum},{nil,anumber,astrnum}})
checkallerrors('unpack',{notatable,{nil,anumber,astrnum},{nil,anumber,astrnum}},'bad argument')

-- xpcall
banner('xpcall')
checkallpass('xpcall', {notanil,notanil})
checkallerrors('xpcall',{},'bad argument')


