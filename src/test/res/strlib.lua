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

print( string.gsub("ABC", "@(%x+)", function(s) return "|abcd" end) )
print( string.gsub("@123", "@(%x+)", function(s) return "|abcd" end) )
print( string.gsub("ABC@123", "@(%x+)", function(s) return "|abcd" end) )
print( string.gsub("ABC@123@def", "@(%x+)", function(s) return "|abcd" end) )
print( string.gsub("ABC@123@qrs@def@tuv", "@(%x+)", function(s) return "|abcd" end) )
print( string.gsub("ABC@123@qrs@def@tuv", "@(%x+)", function(s) return "@ab" end) )

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

-- string.sub
function t(str)
	local i = { 0, 1, 2, 8, -1 }
	for ki,vi in ipairs(i) do
		local s,v = pcall( string.sub, str, vi )
		print( 'string.sub("'..str..'",'..tostring(vi)..')='..tostring(s)..',"'..tostring(v)..'"' )
		local j = { 0, 1, 2, 4, 8, -1 }
		for kj,vj in ipairs(j) do
			local s,v = pcall( string.sub, str, vi, vj )
			print( 'string.sub("'..str..'",'..tostring(vi)..','..tostring(vj)..')='..tostring(s)..',"'..tostring(v)..'"' )
		end
	end
end
t( 'abcdefghijklmn' )
t( 'abcdefg' )
t( 'abcd' )
t( 'abc' )
t( 'ab' )
t( 'a' )
t( '' )

print(string.len("Hello, world"))
print(#"Hello, world")
print(string.len("\0\0\0"))
print(#"\0\0\0")
print(string.len("\0\1\2\3"))
print(#"\0\1\2\3")
local s = "My JaCk-O-lAnTeRn CaSe TeXt"
print(s, string.len(s), #s)

local function pc(...)
	local s,e = pcall(...)
	return s and e or 'false-'..type(e)
end
local function strtests(name,func,...)
	print(name, 'good', pc( func, ... ) )
	print(name, 'empty', pc( func ) )
	print(name, 'table', pc( func, {} ) )
	print(name, 'nil', pc( func, nil ) )
end

strtests('lower', string.lower, s )
strtests('upper', string.upper, s )
strtests('reverse', string.reverse, s )
strtests('char', string.char, 92, 60, 61, 93 )
strtests('dump', string.dump, function() print('hello, world') end )

