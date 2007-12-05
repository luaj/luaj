-- helper file for require() tests
module( 'req.subsample', package.seeall )
function h()
	print 'in subsample.h' 
end
print 'loading subsample.lua'
return 'return value from subsample', 'second return value from subsample'

