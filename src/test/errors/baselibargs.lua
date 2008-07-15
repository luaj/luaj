package.path = "?.lua;src/test/errors/?.lua"
require 'args'


-- arg types for basic library functions
local notastrnumnil={true,{},print}
checkallpass('dofile', {{nil,'src/test/errors/args.lua','args.lua'}})
checkallfail('dofile', {notastrnumnil})
checkallerrors('dofile', {notastrnumnil}, 'bad argument')