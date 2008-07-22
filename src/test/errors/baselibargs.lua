package.path = "?.lua;src/test/errors/?.lua"
require 'args'

-- arg types for basic library functions

-- assert
banner('assert')
checkallpass('assert',{{true,123},anylua})
checkallerrors('assert',{{nil,false},{nil}},'assertion failed')
checkallerrors('assert',{{nil,false},{'message'}},'message')

-- collectgarbage
banner('collectgarbage')
checkallpass('collectgarbage',{{'collect','count'}},true)
checkallerrors('collectgarbage',{notanil},'bad argument #1')

-- dofile
banner('dofile')
checkallpass('dofile', {})
checkallpass('dofile', {{'src/test/errors/args.lua'}})
checkallerrors('dofile', {{'args.lua'}}, 'cannot open args.lua')
checkallerrors('dofile', {nonstring}, 'bad argument #1')

-- error
banner('error')
checkallerrors('error', {{'message'},{nil,0,1,2}}, 'message')
checkallerrors('error', {{123},{nil,1,2}}, 123)

-- getfenv
banner('getfenv')
checkallpass('getfenv', {{nil,print,function()end,0,1,2}})
checkallerrors('getfenv', {{true,{},'abc'}}, 'bad argument #1')

-- getmetatable
banner('getmetatable')
checkallpass('getmetatable', {notanil})
checkallerrors('getmetatable',{},'bad argument #1')

-- ipairs
banner('ipairs')
checkallpass('ipairs', {sometable})
checkallerrors('ipairs', {notatable}, 'bad argument #1')

-- load
banner('load')
checkallpass('load', {somefunction,{nil,astring}})
checkallerrors('load', {notafunction,{nil,astring,anumber}}, 'bad argument #1')
checkallerrors('load', {somefunction,{afunction,atable}}, 'bad argument #2')

-- loadfile
banner('loadfile')
checkallpass('loadfile', {})
checkallpass('loadfile', {{'bogus'}})
checkallpass('loadfile', {{'src/test/errors/args.lua'}})
checkallpass('loadfile', {{'args.lua'}})
checkallerrors('loadfile', {nonstring}, 'bad argument #1')

-- loadstring
banner('loadstring')
checkallpass('loadstring', {{'return'}})
checkallpass('loadstring', {{'return'},{'mychunk'}})
checkallpass('loadstring', {{'return a ... b'},{'mychunk'}})
checkallerrors('loadstring', {{'return a ... b'},{'mychunk'}},'hello')
checkallerrors('loadstring', {notastring,{nil,astring,anumber}}, 'bad argument #1')
checkallerrors('loadstring', {{'return'},{afunction,atable}}, 'bad argument #2')

-- next
banner('next')
checkallpass('next', {sometable,somekey})
checkallerrors('next', {notatable,{nil,1}}, 'bad argument #1')
checkallerrors('next', {sometable,nonkey}, 'invalid key')

-- pairs
banner('pairs')
checkallpass('pairs', {sometable})
checkallerrors('pairs', {notatable}, 'bad argument #1')

-- pcall
banner('pcall')
checkallpass('pcall', {notanil,anylua}, true)
checkallerrors('pcall',{},'bad argument #1')

-- print
banner('print')
checkallpass('print', {}) 
checkallpass('print', {{nil,astring,anumber,aboolean}}) 

-- rawequal
banner('rawequal')
checkallpass('rawequal', {notanil,notanil})
checkallerrors('rawequal', {}, 'bad argument #1')
checkallerrors('rawequal', {notanil}, 'bad argument #2')

-- rawget
banner('rawget')
checkallpass('rawget', {sometable,somekey})
checkallpass('rawget', {sometable,nonkey})
checkallerrors('rawget', {sometable,somenil},'bad argument #2')
checkallerrors('rawget', {notatable,notakey}, 'bad argument #1')
checkallerrors('rawget', {}, 'bad argument #1')

-- rawset
banner('rawset')
checkallpass('rawset', {sometable,somekey,notanil})
checkallpass('rawset', {sometable,nonkey,notanil})
checkallerrors('rawset', {sometable,somenil},'table index is nil')
checkallerrors('rawset', {}, 'bad argument #1')
checkallerrors('rawset', {notatable,somestring,somestring}, 'bad argument #1')
checkallerrors('rawset', {sometable,somekey}, 'bad argument #3')

-- select
banner('select')
checkallpass('select', {{anumber,'#'},anylua})
checkallerrors('select', {notanumber}, 'bad argument #1')

-- setfenv
banner('setfenv')
local g = _G
checkallpass('setfenv', {{function()end},sometable})
checkallerrors('setfenv', {{-1, '-2'},{g}}, 'level must be non-negative')
checkallerrors('setfenv', {{10, '11'},{g}}, 'invalid level')
checkallerrors('setfenv', {{rawset},{g}}, 'cannot change environment of given object')
checkallerrors('setfenv', {{atable,athread,aboolean,astring},{g}}, 'bad argument #1')
checkallerrors('setfenv', {notafunction}, 'bad argument #2')
checkallerrors('setfenv', {anylua}, 'bad argument #2')
checkallerrors('setfenv', {{function()end},notatable}, 'bad argument #2')

-- setmetatable
banner('setmetatable')
checkallpass('setmetatable', {sometable,sometable})
checkallpass('setmetatable', {sometable,{}})
checkallerrors('setmetatable',{notatable,sometable},'bad argument #1')
checkallerrors('setmetatable',{sometable,notatable},'bad argument #2')

-- tonumber
banner('tonumber')
checkallpass('tonumber',{somenumber,{nil,2,10,36}})
checkallpass('tonumber',{notanil,{nil,10}})
checkallerrors('tonumber',{{nil,afunction,atable},{2,9,11,36}},'bad argument #1')
checkallerrors('tonumber',{somenumber,{1,37,atable,afunction,aboolean}},'bad argument #2')

-- tostring
banner('tostring')
checkallpass('tostring',{{astring,anumber,aboolean}})
checkallpass('tostring',{{atable,afunction,athread}},true)
checkallpass('tostring',{{astring,anumber,aboolean},{'anchor'}})
checkallpass('tostring',{{atable,afunction,athread},{'anchor'}},true)
checkallerrors('tostring',{},'bad argument #1')

-- type
banner('type')
checkallpass('type',{notanil})
checkallpass('type',{anylua,{'anchor'}})
checkallerrors('type',{},'bad argument')

-- unpack
banner('unpack')
checkallpass('unpack',{sometable})
checkallpass('unpack',{sometable,somenumber})
checkallpass('unpack',{sometable,somenumber,somenumber})
checkallerrors('unpack',{notatable,somenumber,somenumber},'bad argument #1')
checkallerrors('unpack',{sometable,nonnumber,somenumber},'bad argument #2')
checkallerrors('unpack',{sometable,somenumber,nonnumber},'bad argument #3')

-- xpcall
banner('xpcall')
checkallpass('xpcall', {notanil,nonfunction})
checkallpass('xpcall', {notanil,{function(...)return 'aaa', 'bbb', #{...} end}})
checkallerrors('xpcall',{anylua},'bad argument #2')


