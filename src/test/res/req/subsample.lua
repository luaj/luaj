-- helper file for require() tests
require 'ids'
print( 'in subsample (before module statement)' )
print( 'env', id(getfenv()), '_G', id(_G), '_NAME', _NAME, '_M', id(_M), '_PACKAGE', _PACKAGE )
module( 'req.subsample', package.seeall )

print( 'in subsample (after module statement)' )
print( 'env', id(getfenv()), '_G', id(_G), '_NAME', _NAME, '_M', id(_M), '_PACKAGE', _PACKAGE )
function h()
	print 'in subsample.h' 
	print( 'env', id(getfenv()), '_G', id(_G), '_NAME', _NAME, '_M', id(_M), '_PACKAGE', _PACKAGE )
end
print 'loading subsample.lua'
h()
return 'return value from subsample', 'second return value from subsample'

