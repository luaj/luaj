print( string.find( "", "" ) )
print( string.find( "ababaabbaba", "abb" ) )
print( string.find( "ababaabbaba", "abb", 7 ) )

print( string.match( "aabaa", "a*" ) )
print( string.match( "aabaa", "a*", 3 ) )
print( string.match( "aabaa", "a*b" ) )
print( string.match( "aabaa", "a*b", 3 ) )

print( string.match( "abbaaababaabaaabaa", "b(a*)b" ) )

print( string.match( "abbaaababaabaaabaa", "b(a*)()b" ) )
print( string.match( "abbaaababaabaaabaa", "b(a*)()b", 3 ) )
print( string.match( "abbaaababaabaaabaa", "b(a*)()b", 8 ) )
print( string.match( "abbaaababaabaaabaa", "b(a*)()b", 12 ) )

print( string.byte("hi", -3) )

print( tostring(1234567890123) )
print( tostring(1234567890124) )
print( tostring(1234567890125) )
