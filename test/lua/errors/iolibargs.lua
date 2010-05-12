package.path = "?.lua;test/lua/errors/?.lua"
require 'args'

-- arg type tests for io library functions
local f

-- io.close ([file])
banner('io.close')
f = io.open("abc.txt","w")
checkallpass('io.close',{{f}})
checkallerrors('io.close',{notanil},'bad argument')

-- io.input ([file])
f = io.open("abc.txt","r")
checkallpass('io.input',{{nil,f,"abc.txt",n=3}})
checkallerrors('io.input',{nonstring},'bad argument')

-- io.lines ([filename])
io.input("abc.txt")
checkallpass('io.lines',{{nil,"abc.txt",n=2}})
checkallerrors('io.lines',{{f}},'bad argument')
checkallerrors('io.lines',{nonstring},'bad argument')

-- io.open (filename [, mode])
checkallpass('io.open',{{"abc.txt"},{nil,"r","w","a","r+","w+","a+"}})
checkallerrors('io.open',{notastring},'bad argument')
checkallerrors('io.open',{{"abc.txt"},{nonstring}},'bad argument')

-- io.output ([file])
checkallpass('io.output',{{nil,f,"abc.txt",n=3}})
checkallerrors('io.output',{nonstring},'bad argument')

-- io.popen (prog [, mode])
checkallpass('io.popen',{{"hostname"},{nil,"w",n=2}})
checkallerrors('io.popen',{notastring},'bad argument')
checkallerrors('io.popen',{{"hostname"},{nonstring}},'bad argument')

-- io.read (···)
local areadfmt = {2,"*n","*a","*l","3"}
checkallpass('io.read',{})
checkallpass('io.read',{areadfmt})
checkallpass('io.read',{areadfmt,areadfmt})
checkallerrors('io.read',{{aboolean,afunction,atable}},'bad argument')

-- io.read (···)
checkallpass('io.write',{})
checkallpass('io.write',{somestring})
checkallpass('io.write',{somestring,somestring})
checkallerrors('io.write',{nonstring},'bad argument')
checkallerrors('io.write',{somestring,nonstring},'bad argument')

-- file:write ()
file = io.open("seektest.txt","w")
checkallpass('file.write',{{file},somestring})
checkallpass('file.write',{{file},somestring,somestring})
checkallerrors('file.write',{},'bad argument')
checkallerrors('file.write',{{file},nonstring},'bad argument')
checkallerrors('file.write',{{file},somestring,nonstring},'bad argument')
pcall( file.close, file )

-- file:seek ([whence] [, offset])
file = io.open("seektest.txt","r")
checkallpass('file.seek',{{file}})
checkallpass('file.seek',{{file},{"set","cur","end"}})
checkallpass('file.seek',{{file},{"set","cur","end"},{2,"3"}})
checkallerrors('file.seek',{},'bad argument')
checkallerrors('file.seek',{{file},nonstring},'bad argument')
checkallerrors('file.seek',{{file},{"set","cur","end"},nonnumber},'bad argument')

-- file:setvbuf (mode [, size])
checkallpass('file.setvbuf',{{file},{"no","full","line"}})
checkallpass('file.setvbuf',{{file},{"full"},{1024,"512"}})
checkallerrors('file.setvbuf',{},'bad argument')
checkallerrors('file.setvbuf',{{file},notastring},'bad argument')
checkallerrors('file.setvbuf',{{file},{"full"},nonnumber},'bad argument')

