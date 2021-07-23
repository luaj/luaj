function p(a,...)
	print("a",a)
	print("...",...)
	print("...,a",...,a)
	print("a,...",a,...)
end
function q(a,...)
	print("a,arg[1],arg[2],arg[3]",a,arg[1],arg[2],arg[3])
end
function r(a,...)
	print("a,arg[1],arg[2],arg[3]",a,arg[1],arg[2],arg[3])
	print("a",a)
	print("...",...)
	print("...,a",...,a)
	print("a,...",a,...)
end
function s(a)
	local arg = { '1', '2', '3' }	
	print("a,arg[1],arg[2],arg[3]",a,arg[1],arg[2],arg[3])
	print("a",a)
end
function t(a,...)
	local arg = { '1', '2', '3' }	
	print("a,arg[1],arg[2],arg[3]",a,arg[1],arg[2],arg[3])
	print("a",a)
	print("...",...)
	print("...,a",...,a)
	print("a,...",a,...)
end
