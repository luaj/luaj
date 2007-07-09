function sum(a,b,c,d)          -- "sum" method
	local d = d or 0
	return a+b+c+d   -- return sum
end
print( sum( 1, 2, 3, 4 ) )
print( sum( 5, 6, 7 ) )
print( sum( 9, 10, 11, 12, 13, 14 ) )
print( sum( sum(1,2,3,4), sum(5,6,7), sum(9,10,11,12,13,14), 15 ) )

function myfunc(x)
	return x*x;
end

print( myfunc(0.25) )

do
  local oldMyfunc = myfunc
  local k = 55
  myfunc = function (x)
    local a = k + oldMyfunc(x)
    k = k + 5
    return a
  end
end

print( myfunc(0.1) )
print( myfunc(0.1) )

i = 1
table = { "west", "south", "east", "north" }
function next()
	if ( i >= 4 ) then
		i = 0
	end
	i = i + 1
	return table[i]
end

print( next() )
print( next() )
print( next() )
print( next() )
print( next() )

function room1 ()
  local move = next()
  print( "room1 moving", move )
  if move == "south" then return room3()
  elseif move == "east" then return room2()
  else print("invalid move")
       return room1()   -- stay in the same room
  end
end

function room2 ()
  local move = next()
  print( "room2 moving", move )
  if move == "south" then return room4()
  elseif move == "west" then return room1()
  else print("invalid move")
       return room2()
  end
end

function room3 ()
  local move = next()
  print( "room3 moving", move )
  if move == "north" then return room1()
  elseif move == "east" then return room4()
  else print("invalid move")
       return room3()
  end
end

function room4 ()
  print("congratulations!")
end

room1()
	