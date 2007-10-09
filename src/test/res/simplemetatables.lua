-- The purpose of this test case is to check
-- that certain simple metatable operations work properly
t = { b='bbb' }
t.__index = t
u = {}
setmetatable( u, t )
u.c = 'ccc'

print( 't.a', t.a )
print( 'u.a', u.a )
print( 't.b', t.b )
print( 'u.b', u.b )
print( 't.c', t.c )
print( 'u.c', u.c )
