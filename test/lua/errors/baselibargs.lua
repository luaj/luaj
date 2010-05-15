package.path = "?.lua;test/lua/errors/?.lua"
require 'args'

-- arg types for basic library functions

-- assert
banner('assert')
checkallpass('assert',{{true,123},anylua})
checkallerrors('assert',{{nil,false,n=2},{nil,n=1}},'assertion failed')
checkallerrors('assert',{{nil,false,n=2},{'message'}},'message')

-- collectgarbage
banner('collectgarbage')
checkallpass('collectgarbage',{{'collect','count'}},true)
checkallerrors('collectgarbage',{{astring, anumber}},'bad argument')
checkallerrors('collectgarbage',{{aboolean, atable, afunction, athread}},'string expected')

-- dofile
banner('dofile')
checkallpass('dofile', {})
checkallpass('dofile', {{'test/lua/errors/args.lua'}})
checkallerrors('dofile', {{'foo.bar'}}, 'cannot open foo.bar')
checkallerrors('dofile', {nonstring}, 'bad argument')

-- error
banner('error')
checkallerrors('error', {{'message'},{nil,0,1,2,n=4}}, 'message')
checkallerrors('error', {{123},{nil,1,2,n=3}}, 123)

-- getfenv
banner('getfenv')
checkallpass('getfenv', {{nil,print,function()end,0,1,2,n=5}})
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
checkallpass('load', {somefunction,{nil,astring,n=2}})
checkallerrors('load', {notafunction,{nil,astring,anumber,n=3}}, 'bad argument')
checkallerrors('load', {somefunction,{afunction,atable}}, 'bad argument')

-- loadfile
banner('loadfile')
checkallpass('loadfile', {})
checkallpass('loadfile', {{'bogus'}})
checkallpass('loadfile', {{'test/lua/errors/args.lua'}})
checkallpass('loadfile', {{'args.lua'}})
checkallerrors('loadfile', {nonstring}, 'bad argument')

-- loadstring
banner('loadstring')
checkallpass('loadstring', {{'return'}})
checkallpass('loadstring', {{'return'},{'mychunk'}})
checkallpass('loadstring', {{'return a ... b'},{'mychunk'}},true)
checkallerrors('loadstring', {{'return a ... b'},{'mychunk'}},'hello')
checkallerrors('loadstring', {notastring,{nil,astring,anumber,n=3}}, 'bad argument')
checkallerrors('loadstring', {{'return'},{afunction,atable}}, 'bad argument')

-- next
banner('next')
checkallpass('next', {sometable,somekey})
checkallerrors('next', {notatable,{nil,1,n=2}}, 'bad argument')
checkallerrors('next', {sometable,nonkey}, 'invalid key')

-- pairs
banner('pairs')
checkallpass('pairs', {sometable})
checkallerrors('pairs', {notatable}, 'bad argument')

-- pcall
banner('pcall')
checkallpass('pcall', {notanil,anylua}, true)
checkallerrors('pcall',{},'bad argument')

-- print
banner('print')
checkallpass('print', {}) 
checkallpass('print', {{nil,astring,anumber,aboolean,n=4}}) 

-- rawequal
banner('rawequal')
checkallpass('rawequal', {notanil,notanil})
checkallerrors('rawequal', {}, 'bad argument')
checkallerrors('rawequal', {notanil}, 'bad argument')

-- rawget
banner('rawget')
checkallpass('rawget', {sometable,somekey})
checkallpass('rawget', {sometable,nonkey})
checkallerrors('rawget', {sometable,somenil},'bad argument')
checkallerrors('rawget', {notatable,notakey}, 'bad argument')
checkallerrors('rawget', {}, 'bad argument')

-- rawset
banner('rawset')
checkallpass('rawset', {sometable,somekey,notanil})
checkallpass('rawset', {sometable,nonkey,notanil})
checkallerrors('rawset', {sometable,somenil},'table index is nil')
checkallerrors('rawset', {}, 'bad argument')
checkallerrors('rawset', {notatable,somestring,somestring}, 'bad argument')
checkallerrors('rawset', {sometable,somekey}, 'bad argument')

-- select
banner('select')
checkallpass('select', {{anumber,'#'},anylua})
checkallerrors('select', {notanumber}, 'bad argument')

-- setfenv
banner('setfenv')
local g = _G
checkallpass('setfenv', {{function()end},sometable})
checkallerrors('setfenv', {{-1, '-2'},{g}}, 'level must be non-negative')
checkallerrors('setfenv', {{10, '11'},{g}}, 'invalid level')
checkallerrors('setfenv', {{rawset},{g}}, 'cannot change environment of given object')
checkallerrors('setfenv', {{atable,athread,aboolean,astring},{g}}, 'bad argument')
checkallerrors('setfenv', {notafunction}, 'bad argument')
checkallerrors('setfenv', {anylua}, 'bad argument')
checkallerrors('setfenv', {{function()end},notatable}, 'bad argument')

-- setmetatable
banner('setmetatable')
checkallpass('setmetatable', {sometable,sometable})
checkallpass('setmetatable', {sometable,{}})
checkallerrors('setmetatable',{notatable,sometable},'bad argument')
checkallerrors('setmetatable',{sometable,notatable},'bad argument')

-- tonumber
banner('tonumber')
checkallpass('tonumber',{somenumber,{nil,2,10,36,n=4}})
checkallpass('tonumber',{notanil,{nil,10,n=2}})
checkallerrors('tonumber',{{nil,afunction,atable,n=3},{2,9,11,36}},'bad argument')
checkallerrors('tonumber',{somenumber,{1,37,atable,afunction,aboolean}},'bad argument')

-- tostring
banner('tostring')
checkallpass('tostring',{{astring,anumber,aboolean}})
checkallpass('tostring',{{atable,afunction,athread}},true)
checkallpass('tostring',{{astring,anumber,aboolean},{'anchor'}})
checkallpass('tostring',{{atable,afunction,athread},{'anchor'}},true)
checkallerrors('tostring',{},'bad argument')

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
checkallerrors('unpack',{notatable,somenumber,somenumber},'bad argument')
checkallerrors('unpack',{sometable,nonnumber,somenumber},'bad argument')
checkallerrors('unpack',{sometable,somenumber,nonnumber},'bad argument')

-- xpcall
banner('xpcall')
checkallpass('xpcall', {notanil,nonfunction})
checkallpass('xpcall', {notanil,{function(...)return 'aaa', 'bbb', #{...} end}})
checkallerrors('xpcall',{anylua},'bad argument')


