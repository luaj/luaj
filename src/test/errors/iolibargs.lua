package.path = "?.lua;src/test/errors/?.lua"
require 'args'

-- arg type tests for io library functions
local f

-- io.close ([file])
banner('io.close')
f = io.open("abc.txt","w")
checkallpass('io.close',{{f}})
checkallerrors('io.close',{notanil},'bad argument #1')

-- io.input ([file])
f = io.open("abc.txt","r")
checkallpass('io.input',{{nil,f,"abc.txt"}})
checkallerrors('io.input',{nonstring},'bad argument #1')

-- io.lines ([filename])
io.input("abc.txt")
checkallpass('io.lines',{{nil,"abc.txt"}})
checkallerrors('io.lines',{{f}},'bad argument #1')
checkallerrors('io.lines',{nonstring},'bad argument #1')

-- io.open (filename [, mode])
checkallpass('io.open',{{"abc.txt"},{nil,"r","w","a","r+","w+","a+"}})
checkallerrors('io.open',{notastring},'bad argument #1')
checkallerrors('io.open',{{"abc.txt"},{nonstring}},'bad argument #2')

-- io.output ([file])
checkallpass('io.output',{{nil,f,"abc.txt"}})
checkallerrors('io.output',{nonstring},'bad argument #1')

-- io.popen (prog [, mode])
checkallpass('io.popen',{{"hostname"},{nil,"r","w","a","r+","w+","a+"}})
checkallerrors('io.popen',{notastring},'bad argument #1')
checkallerrors('io.popen',{{"hostname"},{nonstring}},'bad argument #2')

-- io.read (···)
local areadfmt = {2,"*n","*a","*l","3"}
checkallpass('io.read',{})
checkallpass('io.read',{areadfmt})
checkallpass('io.read',{areadfmt,areadfmt})
checkallerrors('io.read',{{aboolean,afunction,atable}},'bad argument #1')

-- io.read (···)
checkallpass('io.write',{})
checkallpass('io.write',{somestring})
checkallpass('io.write',{somestring,somestring})
checkallerrors('io.write',{nonstring},'bad argument #1')
checkallerrors('io.write',{somestring,nonstring},'bad argument #2')

-- file:write ()
file = io.open("seektest.txt","w")
checkallpass('file.write',{{file},somestring})
checkallpass('file.write',{{file},somestring,somestring})
checkallerrors('file.write',{},'bad argument #1')
checkallerrors('file.write',{{file},nonstring},'bad argument #1')
checkallerrors('file.write',{{file},somestring,nonstring},'bad argument #2')
pcall( file.close, file )

-- file:seek ([whence] [, offset])
file = io.open("seektest.txt","r")
checkallpass('file.seek',{{file}})
checkallpass('file.seek',{{file},{"set","cur","end"}})
checkallpass('file.seek',{{file},{"set","cur","end"},{2,"3"}})
checkallerrors('file.seek',{},'bad argument #1')
checkallerrors('file.seek',{{file},nonstring},'bad argument #1')
checkallerrors('file.seek',{{file},{"set","cur","end"},nonnumber},'bad argument #2')

-- file:setvbuf (mode [, size])
checkallpass('file.setvbuf',{{file},{"no","full","line"}})
checkallpass('file.setvbuf',{{file},{"full"},{1024,"512"}})
checkallerrors('file.setvbuf',{},'bad argument #1')
checkallerrors('file.setvbuf',{{file},notastring},'bad argument #1')
checkallerrors('file.setvbuf',{{file},{"full"},nonnumber},'bad argument #2')

