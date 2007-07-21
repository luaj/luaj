local mathClass = luajava.bindClass("java.lang.Math")

local function sqrt(x)
	return mathClass:sqrt(x)
end

return { sqrt = sqrt; pi = mathClass.PI }
