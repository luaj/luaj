
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

print( myfunc(0.1) )

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

