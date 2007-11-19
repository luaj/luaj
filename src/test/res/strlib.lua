print( string.find( 'alo alx 123 b\0o b\0o', '(..*) %1' ) )
print( string.find( 'aloALO', '%l*' ) )
print( string.find( ' \n isto � assim', '%S%S*' ) )

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

function f1(s, p)
  print(p)
  p = string.gsub(p, "%%([0-9])", function (s) return "%" .. (s+1) end)
  print(p)
  p = string.gsub(p, "^(^?)", "%1()", 1)
  print(p)
  p = string.gsub(p, "($?)$", "()%1", 1)
  print(p)
  local t = {string.match(s, p)}
  return string.sub(s, t[1], t[#t] - 1)
end

print( f1('alo alx 123 b\0o b\0o', '(..*) %1') )

local function badpat()
	print( string.gsub( "alo", "(.)", "%2" ) )
end

print( pcall( badpat ) )

for k, v in string.gmatch("w=200&h=150", "(%w+)=(%w+)") do
    print(k, v)
end
