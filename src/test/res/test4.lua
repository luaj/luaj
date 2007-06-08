function Point(x, y)          -- "Point" object constructor
  return { x = x, y = y }   -- Creates and returns a new object (table)
end
array = { Point(10, 20), Point(30, 40), Point(50, 60) }   -- Creates array of points
print(array[2].y)                                         -- Prints 40



