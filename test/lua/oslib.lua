-- simple os-library tests
--
-- because the nature of the "os" library is to provide os-specific behavior,
-- the compatibility tests must be extremely loose, and can really only 
-- compare things like return value type to be meaningful.
-- 
-- actual os behavior needs to go in an oslib function test
-- 
local pcall = function(...)
	local s,e,f = pcall(...)
	return s,type(e)
end 
print( 'os', type(os) )
print( 'os.clock()', pcall( os.clock ) )
print( 'os.date()', pcall( os.date ) )
print( 'os.difftime(123000, 21500)', pcall( os.difftime, 123000, 21250 ) )
print( 'os.execute("bogus")', pcall( os.execute, '' ) )
print( 'os.getenv()', pcall( os.getenv ) )
print( 'os.getenv("bogus.key")', pcall( os.getenv, 'bogus.key' ) )
local s,p = pcall( os.tmpname )
local s,q = pcall( os.tmpname )
print( 'os.tmpname()', s, p )
print( 'os.tmpname()', s, q )
-- permission denied on windows
--print( 'os.remove(p)', pcall( os.remove, p ) )
--print( 'os.rename(p,q)', pcall( os.rename, p, q ) )
local s,f = pcall( io.open, p,"w" )
print( 'io.open', s, f )
print( 'write', pcall( f.write, f, "abcdef 12345" ) )
print( 'close', pcall( f.close, f ) )
print( 'os.rename(p,q)', pcall( os.rename, p, q ) )
print( 'os.remove(q)', pcall( os.remove, q ) )
print( 'os.remove(q)', pcall( os.remove, q ) )
-- setlocale not supported on jse yet
-- print( 'os.setlocale()', pcall( os.setlocale ) )
-- print( 'os.setlocale("jp")', pcall( os.setlocale, "jp" ) )
-- print( 'os.setlocale("us","monetary")', pcall( os.setlocale, "us", "monetary" ) )
-- print( 'os.setlocale(nil,"all")', pcall( os.setlocale, nil, "all" ) )
print( 'os.setlocale("C")', pcall( os.setlocale, "C" ) )
print( 'os.exit', type(os.exit) )
