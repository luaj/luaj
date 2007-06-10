i = 777
while i<780 do
   print(i)
   i = i+1
end

a,b = 0,1
while true do               -- infinite loop
   print( b )
  a,b = b,a+b
  if a>10 then break end   -- exit the loop if the condition is true
end

for count = 336,330,-2 do print(count) end  -- numerical iteration

for key,value in pairs({a=10, 3.14159265358, c="banana" }) do print(key, value) end 
