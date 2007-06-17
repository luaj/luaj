t = { 11, 22, 33, you='one', me='two' }

for a,b in pairs(t) do
	print( a, b )
end
print( "t[2]", t[2] )
print( "t.me", t.me )
print( "t.fred", t.fred )
print( "t['me']", t["me"] )
print( "t['fred']", t["fred"] )
print( "me", me )
print( "fred", fred )
print( "t[me]", t[me] )
print( "t[fred]", t[fred] )

-- basic metatable setting 
t = { 11, 22, 33, you='one', me='two' }
mt = { __index = print, __newindex = print }
setmetatable(t,mt)
a = t[11]
b = t.one
c = t[1]
d = t.you
t[4] = 'rat'
t[1] = 'pipe'
print( a, b, c, d )
for a,b in pairs(t) do
	print( a, b )
end

-- delegate to actual metatable
s = { }
mt = { __newindex = s, __index = _G }
setmetatable(t, mt)
print( t.you )
x = 'wow'
print( t.x )
me = 'here'
print( t.me )
t[5] = 99
for a,b in pairs(s) do
	print( a, b )
end
