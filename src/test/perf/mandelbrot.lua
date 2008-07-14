-- The Computer Language Shootout
-- http://shootout.alioth.debian.org/
-- contributed by Mike Pall
-- modified for luaj to remove dependency on io

local arg = {...}
local width = tonumber(arg and arg[1]) or 100
local height, wscale = width, 2/width
local m, limit2 = 50, 4.0
local write, char = print, string.char

write("P4\n", width, " ", height, "\n")

for y=0,height-1 do
  local Ci = 2*y / height - 1
  for xb=0,width-1,8 do
    local bits = 0
    local xbb = xb+7
    for x=xb,xbb < width and xbb or width-1 do
      bits = bits + bits
      local Zr, Zi, Zrq, Ziq = 0.0, 0.0, 0.0, 0.0
      local Cr = x * wscale - 1.5
      for i=1,m do
        local Zri = Zr*Zi
        Zr = Zrq - Ziq + Cr
        Zi = Zri + Zri + Ci
        Zrq = Zr*Zr
        Ziq = Zi*Zi
        if Zrq + Ziq > limit2 then
          bits = bits + 1
          break
        end
      end
    end
    if xbb >= width then
      for x=width,xbb do bits = bits + bits + 1 end
    end
    write(char(255-bits))
  end
end
