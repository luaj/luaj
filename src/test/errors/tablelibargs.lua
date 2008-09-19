package.path = "?.lua;src/test/errors/?.lua"
require 'args'

-- arg type tests for table library functions

-- table.concat
local somestringstable = {{8,7,6,5,4,3,2,1,}}
local somenonstringtable = {{true,true,true,true,true,true,true,true,}}
local somesep = {',',1.23}
local notasep = {aboolean,atable,afunction}
local somei = {2,'2','2.2'}
local somej = {4,'4','4.4'}
local notij = {astring,aboolean,atable,afunction}
banner('table.concat')
checkallpass('table.concat',{somestringstable})
checkallpass('table.concat',{somestringstable,somesep})
checkallpass('table.concat',{somestringstable,{'-'},somei})
checkallpass('table.concat',{somestringstable,{'-'},{2},somej})
checkallerrors('table.concat',{notatable},'bad argument #1')
checkallerrors('table.concat',{somenonstringtable},'table contains non-strings')
checkallerrors('table.concat',{somestringstable,notasep},'bad argument #2')
checkallerrors('table.concat',{somestringstable,{'-'},notij},'bad argument #3')
checkallerrors('table.concat',{somestringstable,{'-'},{2},notij},'bad argument #4')

-- table.insert
banner('table.insert')
checkallpass('table.insert',{sometable,notanil})
checkallpass('table.insert',{sometable,somei,notanil})
checkallerrors('table.insert',{notatable,somestring},'bad argument #1')
checkallerrors('table.insert',{sometable,notij,notanil},'bad argument #2')

-- table.maxn
banner('table.maxn')
checkallpass('table.maxn',{sometable})
checkallerrors('table.maxn',{notatable},'bad argument #1')

-- table.remove
banner('table.remove')
checkallpass('table.remove',{sometable})
checkallpass('table.remove',{sometable,somei})
checkallerrors('table.remove',{notatable},'bad argument #1')
checkallerrors('table.remove',{notatable,somei},'bad argument #1')
checkallerrors('table.remove',{sometable,notij},'bad argument #2')

-- table.sort
local somecomp = {nil,afunction}
local notacomp = {astring,anumber,aboolean,atable}
banner('table.sort')
checkallpass('table.sort',{somestringstable,somecomp})
checkallerrors('table.sort',{sometable},'attempt to compare')
checkallerrors('table.sort',{notatable,somecomp},'bad argument #1')
checkallerrors('table.sort',{sometable,notacomp},'bad argument #2')

-- table get
banner('table_get - tbl[key]')
function table_get(tbl,key) return tbl[key] end
checkallpass('table_get',{sometable,anylua})

-- table set
banner('table_set - tbl[key]=val')
function table_set(tbl,key,val) tbl[key]=val end
function table_set_nil_key(tbl,val) tbl[nil]=val end
checkallpass('table_set',{sometable,notanil,anylua})
checkallerrors('table_set_nil_key',{sometable,anylua},'table index is nil')


