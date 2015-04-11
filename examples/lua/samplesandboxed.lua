-- Illustration of simple sandboxing techniques that can be used in luaj.
--
-- This sandboxing is done in lua.  These same techniques are all
-- possible directly from Java, as shown in /examples/jse/SampleSandboxed.java.
-- 
-- The main goals of this sandbox are:
-- *  lightweight sandbox controlled by single lua script
-- *  use new globals per-script and leave out dangerous libraries
-- *  use debug hook functions with yield to limit lua scripts
-- *  use read-only tables to protect shared metatables
 
-- Replace the string metatable with a read-only version.
debug.setmetatable('', {
   __index = string,
   __newindex = function() error('table is read only') end,
   __metatable = false,
})

-- Duplicate contents of a table.
local function dup(table)
	local t = {}
	for k,v in pairs(table) do t[k] = v end
	return t
end

-- Produce a new user environment.   
-- Only a subset of functionality is exposed.
-- Must not expose debug, luajava, or other easily abused functions.
local function new_user_globals() 
    local g = {
		print = print,
		pcall = pcall,
		xpcall = xpcall,   
		pairs = pairs,
		ipairs = ipairs,     
		getmetatable = getmetatable,
		setmetatable = setmetatable,
		load = load,
		package = { preload = {}, loaded = {}, },
		table = dup(table),
		string = dup(string),
		math = dup(math),
		bit32 = dup(bit32),
		-- functions can also be customized here
	}
	g._G = g
	return g
end

-- Run a script in it's own user environment,
-- and limit it to a certain number of cycles before abandoning it.
local function run_user_script_in_sandbox(script)
	do 
		-- load the chunk using the main globals for the environment 
		-- initially so debug hooks will be usable in these threads.
		local chunk, err = _G.load(script, 'main', 't')
		if not chunk then 
			print('error loading', err, script)
			return
		end
		
		-- set the user environment to user-specific globals.
		-- these must not contain debug, luajava, coroutines, or other
		-- dangerous functionality.
		local user_globals = new_user_globals()
		debug.setupvalue(chunk, 1, user_globals)
	
		-- run the thread for a specific number of cycles.
		-- when it yields out, abandon it.  
		local thread = coroutine.create(chunk)
		local hook = function() coroutine.yield('resource used too many cycles') end
		debug.sethook(thread, hook, '', 40)
		local errhook = function(msg) print("in error hook", msg); return msg; end
		print(script, xpcall(coroutine.resume, errhook, thread))
	end
	
	-- run garbage collection to clean up orphaned threads
	collectgarbage()
end

-- Tun various test scripts that should succeed.
run_user_script_in_sandbox( "return 'foo'" )
run_user_script_in_sandbox( "return ('abc'):len()" )
run_user_script_in_sandbox( "return getmetatable('abc')" )
run_user_script_in_sandbox( "return getmetatable('abc').len" )
run_user_script_in_sandbox( "return getmetatable('abc').__index" )

-- Example user scripts that attempt rogue operations, and will fail. 
run_user_script_in_sandbox( "return setmetatable('abc', {})" )
run_user_script_in_sandbox( "getmetatable('abc').len = function() end" )
run_user_script_in_sandbox( "getmetatable('abc').__index = {}" )
run_user_script_in_sandbox( "getmetatable('abc').__index.x = 1" )
run_user_script_in_sandbox( "while true do print('loop') end" )

-- Example use of other shared metatables, which should also be made read-only.
-- this toy example allows booleans to be added to numbers.

-- Normally boolean cannot participate in arithmetic.
local number_script =  "return 2 + 7, 2 + true, false + 7"
run_user_script_in_sandbox(number_script)

-- Create a shared metatable that includes addition for booleans.
-- This would only be set up by the server, not by client scripts.
debug.setmetatable(true, {
   __newindex = function() error('table is read only') end,
   __metatable = false,
   __add = function(a, b)
   	  return (a == true and 1 or a == false and 0 or a) +
   	         (b == true and 1 or b == false and 0 or b) 
   end
})

-- All user scripts will now get addition involving booleans.
run_user_script_in_sandbox(number_script)
