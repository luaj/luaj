
function p(a,...)
	print("a",a)
	print("...",...)
	print("...,a",...,a)
	print("a,...",a,...)
end
function q(a,...)
	print("a,arg[1],arg[2],arg[3]",a,arg.n,arg[1],arg[2],arg[3])
end
function r(a,...)
	print("a,arg",a,arg)
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
function u(arg)
	print( 'arg', arg )
end
arg = { "global-1", "global-2", "global-3" }
function tryall(f,name)
	print( '---- function '..name..'()' )
	print( '--'..name..'():' )
	print( ' ->', pcall( f ) )
	print( '--'..name..'("q"):' )
	print( ' ->', pcall( f, "q" ) )
	print( '--'..name..'("q","r"):' )
	print( ' ->', pcall( f, "q", "r" ) )
	print( '--'..name..'("q","r","s"):' )
	print( ' ->', pcall( f, "q", "r", "s" ) )
end
tryall(p,'p')
tryall(q,'q')
tryall(r,'r')
tryall(s,'s')
tryall(t,'t')
tryall(u,'u')
