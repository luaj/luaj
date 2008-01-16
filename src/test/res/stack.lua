-- This test case computes and outputs the sums of the first n
-- integers for n from 1 to 100 in the most ridiculous way possible.

-- The point is to exercise code related to varargs & argument passing
-- in the hopes of detecting bug(s) in stack resizing code.

local function upto(n)
   local t = {}
   for i = 1, n do
      t[i] = i
   end
   return unpack(t)
end

local function map(f, ...)
   local args = { ... }
   local result = {}
   local i = 1
   while args[i] do
      result[i] = f(args[i])
      i = i + 1
   end
   return unpack(result)
end

local function join(sep, ...)
   return table.concat({ ... }, sep)
end

local i = 1
local j = 1
while i < 200 do
   local params = join(", ", map(function(x) return("x"..x) end, upto(i)))
   local body = join(" + ", map(function(x) return("x"..x) end, upto(i)))
   local func = "function f(" .. params .. ")\n  return(" .. body .. ")\nend"
   
   local args = join(", ", map(function(x) return tostring(x) end, upto(i)))
   
   local chunk = func .. "\nreturn f( " .. args .. " )"

   local co = coroutine.create(assert(loadstring(chunk, "test #"..i)))
   
   print(coroutine.resume(co))
   i = i + j
   j = j + 1
end
