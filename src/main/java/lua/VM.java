package lua;

import java.io.InputStream;

import lua.io.Closure;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;
/**
 * <hr>
 * <h3><a name="VM"><code>VM</code></a></h3>
 * 
 * <pre>
 * typedef struct VM;
 * </pre>
 * 
 * <p>
 * Opaque structure that keeps the whole state of a Lua interpreter. The Lua
 * library is fully reentrant: it has no global variables. All information about
 * a state is kept in this structure.
 * 
 * 
 * <p>
 * Here we list all functions and types from the C&nbsp;API in alphabetical
 * order. Each function has an indicator like this: <span class="apii">[-o, +p,
 * <em>x</em>]</span>
 * 
 * <p>
 * The first field, <code>o</code>, is how many elements the function pops
 * from the stack. The second field, <code>p</code>, is how many elements the
 * function pushes onto the stack. (Any function always pushes its results after
 * popping its arguments.) A field in the form <code>x|y</code> means the
 * function may push (or pop) <code>x</code> or <code>y</code> elements,
 * depending on the situation; an interrogation mark '<code>?</code>' means
 * that we cannot know how many elements the function pops/pushes by looking
 * only at its arguments (e.g., they may depend on what is on the stack). The
 * third field, <code>x</code>, tells whether the function may throw errors: '<code>-</code>'
 * means the function never throws any error; '<code>m</code>' means the
 * function may throw an error only due to not enough memory; '<code>e</code>'
 * means the function may throw other kinds of errors; '<code>v</code>'
 * means the function may throw an error on purpose.
 * 
 * 
 */
public interface VM {

	// ================ interfaces for performing calls
	
	/**
	 * Create a call frame for a call that has been set up on
	 * the stack. The first value on the stack must be a Closure,
	 * and subsequent values are arguments to the closure.
	 */ 
	public void prepStackCall();
	
	/** 
	 * Invoke a JavaFunction being called via prepStackCall()
	 * @param javaFunction
	 */
	public void invokeJavaFunction(JavaFunction javaFunction);
	
	/**
	 * Execute bytecodes until the current call completes
	 * or the vm yields.
	 */
	public void execute();

	/**
	 * Put the closure on the stack with arguments, 
	 * then perform the call.   Leave return values 
	 * on the stack for later querying. 
	 * 
	 * @param c
	 * @param values
	 */
	public void doCall(Closure c, LValue[] values);	

	
	// ===============================================================
	//                    Lua Java API 
	// ===============================================================

	/**
	 * Sets a new panic function and returns the old one. <span
	 * class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * If an error happens outside any protected environment, Lua calls a
	 * <em>panic function</em> and then calls <code>exit(EXIT_FAILURE)</code>,
	 * thus exiting the host application. Your panic function may avoid this
	 * exit by never returning (e.g., doing a long jump).
	 * 
	 * 
	 * <p>
	 * The panic function can access the error message at the top of the stack.
	 */
	public JavaFunction atpanic(JavaFunction panicf);

	/**
	 * Calls a function. <span class="apii">[-(nargs + 1), +nresults, <em>e</em>]</span>
	 * 
	 * 
	 * <p>
	 * To call a function you must use the following protocol: first, the
	 * function to be called is pushed onto the stack; then, the arguments to
	 * the function are pushed in direct order; that is, the first argument is
	 * pushed first. Finally you call <a href="#lua_call"><code>lua_call</code></a>;
	 * <code>nargs</code> is the number of arguments that you pushed onto the
	 * stack. All arguments and the function value are popped from the stack
	 * when the function is called. The function results are pushed onto the
	 * stack when the function returns. The number of results is adjusted to
	 * <code>nresults</code>, unless <code>nresults</code> is <a
	 * name="pdf-LUA_MULTRET"><code>LUA_MULTRET</code></a>. In this case,
	 * <em>all</em> results from the function are pushed. Lua takes care that
	 * the returned values fit into the stack space. The function results are
	 * pushed onto the stack in direct order (the first result is pushed first),
	 * so that after the call the last result is on the top of the stack.
	 * 
	 * 
	 * <p>
	 * Any error inside the called function is propagated upwards (with a
	 * <code>longjmp</code>).
	 * 
	 * 
	 * <p>
	 * The following example shows how the host program may do the equivalent to
	 * this Lua code:
	 * 
	 * <pre>
	 * a = f(&quot;how&quot;, t.x, 14)
	 * </pre>
	 * 
	 * <p>
	 * Here it is in&nbsp;C:
	 * 
	 * <pre>
	 * lua_getfield(L, LUA_GLOBALSINDEX, &quot;f&quot;); // function to be called 
	 * lua_pushstring(L, &quot;how&quot;); // 1st argument 
	 * lua_getfield(L, LUA_GLOBALSINDEX, &quot;t&quot;); // table to be indexed 
	 * lua_getfield(L, -1, &quot;x&quot;); // push result of t.x (2nd arg) 
	 * lua_remove(L, -2); // remove 't' from the stack 
	 * lua_pushinteger(L, 14); // 3rd argument 
	 * lua_call(L, 3, 1); // call 'f' with 3 arguments and 1 result 
	 * lua_setfield(L, LUA_GLOBALSINDEX, &quot;a&quot;); // set global 'a' 
	 * </pre>
	 * 
	 * <p>
	 * Note that the code above is "balanced": at its end, the stack is back to
	 * its original configuration. This is considered good programming practice.
	 */
	public void call(int nargs, int nresults);

	/**
	 * 
	 * Ensures that there are at least <code>extra</code> free stack slots in
	 * the stack. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * It returns false if it cannot grow the stack to that size. This function
	 * never shrinks the stack; if the stack is already larger than the new
	 * size, it is left unchanged.
	 * 
	 */
	public void checkstack(int extra);

	/**
	 * Closes the given Lua state. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Destroys all objects in the given Lua state (calling the corresponding
	 * garbage-collection metamethods, if any) and frees all dynamic memory used
	 * by this state. On several platforms, you may not need to call this
	 * function, because all resources are naturally released when the host
	 * program ends. On the other hand, long-running programs, such as a daemon
	 * or a web server, might need to release states as soon as they are not
	 * needed, to avoid growing too large.
	 */
	public void close();

	/**
	 * Concatenates the <code>n</code> values at the top of the stack. <span
	 * class="apii">[-n, +1, <em>e</em>]</span>
	 * 
	 * <p>
	 * Concatenates the <code>n</code> values at the top of the stack, pops
	 * them, and leaves the result at the top. If <code>n</code>&nbsp;is&nbsp;1,
	 * the result is the single value on the stack (that is, the function does
	 * nothing); if <code>n</code> is 0, the result is the empty string.
	 * Concatenation is performed following the usual semantics of Lua (see <a
	 * href="#2.5.4">&sect;2.5.4</a>).
	 */
	public void concat(int n);

	/**
	 * Calls the C&nbsp;function <code>func</code> in protected mode. <span
	 * class="apii">[-0, +(0|1), <em>-</em>]</span>
	 * 
	 * <p>
	 * <code>func</code> starts with only one element in its stack, a light
	 * userdata containing <code>ud</code>. In case of errors, <a
	 * href="#lua_cpcall"><code>lua_cpcall</code></a> returns the same error
	 * codes as <a href="#lua_pcall"><code>lua_pcall</code></a>, plus the
	 * error object on the top of the stack; otherwise, it returns zero, and
	 * does not change the stack. All values returned by <code>func</code> are
	 * discarded.
	 */
	public int javapcall(JavaFunction func, Object ud);

	/**
	 * Creates a new empty table and pushes it onto the stack. <span
	 * class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * The new table has space pre-allocated for <code>narr</code> array
	 * elements and <code>nrec</code> non-array elements. This pre-allocation
	 * is useful when you know exactly how many elements the table will have.
	 * Otherwise you can use the function <a href="#lua_newtable"><code>lua_newtable</code></a>.
	 */
	public void createtable(int narr, int nrec);

	/**
	 * Dumps a function as a binary chunk. <span class="apii">[-0, +0,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * Receives a Lua function on the top of the stack and produces a binary
	 * chunk that, if loaded again, results in a function equivalent to the one
	 * dumped. As it produces parts of the chunk, <a href="#lua_dump"><code>lua_dump</code></a>
	 * calls function <code>writer</code> (see <a href="#lua_Writer"><code>lua_Writer</code></a>)
	 * with the given <code>data</code> to write them.
	 * <p>
	 * The value returned is the error code returned by the last call to the
	 * writer; 0&nbsp;means no errors.
	 * 
	 * 
	 * <p>
	 * This function does not pop the Lua function from the stack.
	 * 
	 */
	public void dump();

	/**
	 * Tests if two items on the stack are equal. <span class="apii">[-0, +0,
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the two values in acceptable indices <code>index1</code>
	 * and <code>index2</code> are equal, following the semantics of the Lua
	 * <code>==</code> operator (that is, may call metamethods). Otherwise
	 * returns&nbsp;0. Also returns&nbsp;0 if any of the indices is non valid.
	 * 
	 * 
	 */
	public boolean equal(int index1, int index2);

	/**
	 * Generates a Lua error. <span class="apii">[-1, +0, <em>v</em>]</span>
	 * 
	 * <p>
	 * The error message (which can actually be a Lua value of any type) must be
	 * on the stack top. This function does a long jump, and therefore never
	 * returns. (see <a href="#luaL_error"><code>luaL_error</code></a>).
	 * 
	 */
	public void error();

	/**
	 * Raises an error with the default level.   
	 * 
	 * In the java implementation this throws a RuntimeException, possibly filling 
	 * line number information first.
	 */
	public void error(String message);
	
	/**
	 * Raises an error.   The message is pushed onto the stack and used as the error message.  
	 * It also adds at the beginning of the message the file name and the line number where 
	 * the error occurred, if this information is available.
	 * 
	 * In the java implementation this throws a RuntimeException, possibly filling 
	 * line number information first.
	 */
	public void error(String message, int level);
	
	/**
	 * Controls the garbage collector. <span class="apii">[-0, +0, <em>e</em>]</span>
	 * 
	 * <p>
	 * This function performs several tasks, according to the value of the
	 * parameter <code>what</code>:
	 * 
	 * <ul>
	 * 
	 * <li><b><code>LUA_GCSTOP</code>:</b> stops the garbage collector.
	 * </li>
	 * 
	 * <li><b><code>LUA_GCRESTART</code>:</b> restarts the garbage
	 * collector. </li>
	 * 
	 * <li><b><code>LUA_GCCOLLECT</code>:</b> performs a full
	 * garbage-collection cycle. </li>
	 * 
	 * <li><b><code>LUA_GCCOUNT</code>:</b> returns the current amount of
	 * memory (in Kbytes) in use by Lua. </li>
	 * 
	 * <li><b><code>LUA_GCCOUNTB</code>:</b> returns the remainder of
	 * dividing the current amount of bytes of memory in use by Lua by 1024.
	 * </li>
	 * 
	 * <li><b><code>LUA_GCSTEP</code>:</b> performs an incremental step of
	 * garbage collection. The step "size" is controlled by <code>data</code>
	 * (larger values mean more steps) in a non-specified way. If you want to
	 * control the step size you must experimentally tune the value of
	 * <code>data</code>. The function returns 1 if the step finished a
	 * garbage-collection cycle. </li>
	 * 
	 * <li><b><code>LUA_GCSETPAUSE</code>:</b> sets <code>data</code>/100
	 * as the new value for the <em>pause</em> of the collector (see <a
	 * href="#2.10">&sect;2.10</a>). The function returns the previous value of
	 * the pause. </li>
	 * 
	 * <li><b><code>LUA_GCSETSTEPMUL</code>:</b> sets <code>data</code>/100
	 * as the new value for the <em>step multiplier</em> of the collector (see
	 * <a href="#2.10">&sect;2.10</a>). The function returns the previous value
	 * of the step multiplier. </li>
	 * 
	 * </ul>
	 */
	public void gc(int what, int data);

	/**
	 * Pushes a value's environment table. <span class="apii">[-0, +1,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the environment table of the value at the given
	 * index.
	 */
	public void getfenv(int index);

	/**
	 * Dereference a tables field. <span class="apii">[-0, +1, <em>e</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value <code>t[k]</code>, where
	 * <code>t</code> is the value at the given valid index. As in Lua, this
	 * function may trigger a metamethod for the "index" event (see <a
	 * href="#2.8">&sect;2.8</a>).
	 * 
	 */
	public void getfield(int index, String k);

	/**
	 * Look up a global value. <span class="apii">[-0, +1, <em>e</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value of the global <code>name</code>. It is
	 * defined as a macro:
	 * 
	 * <pre>
	 * 	 #define lua_getglobal(L,s)  lua_getfield(L, LUA_GLOBALSINDEX, s)
	 * 	
	 * </pre>
	 */
	public void getglobal(String s);

	/**
	 * Get a value's metatable. <span class="apii">[-0, +(0|1), <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the metatable of the value at the given acceptable
	 * index. If the index is not valid, or if the value does not have a
	 * metatable, the function returns&nbsp;0 and pushes nothing on the stack.
	 */
	public int getmetatable(int index);

	/**
	 * Dereference a table's list element. <span class="apii">[-1, +1,
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value <code>t[k]</code>, where
	 * <code>t</code> is the value at the given valid index and <code>k</code>
	 * is the value at the top of the stack.
	 * 
	 * <p>
	 * This function pops the key from the stack (putting the resulting value in
	 * its place). As in Lua, this function may trigger a metamethod for the
	 * "index" event (see <a href="#2.8">&sect;2.8</a>).
	 */
	public void gettable(int index);

	/**
	 * Returns the index of the top element in the stack. <span
	 * class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Because indices start at&nbsp;1, this result is equal to the number of
	 * elements in the stack (and so 0&nbsp;means an empty stack).
	 */
	public int gettop();

	/**
	 * Insert the top item somewhere in the stack. <span class="apii">[-1, +1,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Moves the top element into the given valid index, shifting up the
	 * elements above this index to open space. Cannot be called with a
	 * pseudo-index, because a pseudo-index is not an actual stack position.
	 * 
	 */
	public void insert(int index);

	/**
	 * Test if a value is boolean. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index has type boolean,
	 * and 0&nbsp;otherwise.
	 * 
	 */
	public boolean isboolean(int index);

	/**
	 * Test if a value is a JavaFunction. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a
	 * C&nbsp;function, and 0&nbsp;otherwise.
	 * 
	 */
	public boolean isjavafunction(int index);

	/**
	 * Test if a value is a function. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns true if the value at the given acceptable index is a function
	 * (either C or Lua), and false&nbsp;otherwise.
	 * 
	 */
	public boolean isfunction(int index);

	/**
	 * Test if a value is light user data <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a light userdata,
	 * and 0&nbsp;otherwise.
	 */
	public boolean islightuserdata(int index);

	/**
	 * Test if a value is nil <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is <b>nil</b>, and
	 * 0&nbsp;otherwise.
	 */
	public boolean isnil(int index);

	/**
	 * Test if a value is not valid <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the the given acceptable index is not valid (that is, it
	 * refers to an element outside the current stack), and 0&nbsp;otherwise.
	 */
	public boolean isnone(int index);

	/**
	 * Test if a value is nil or not valid <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the the given acceptable index is not valid (that is, it
	 * refers to an element outside the current stack) or if the value at this
	 * index is <b>nil</b>, and 0&nbsp;otherwise.
	 */
	public boolean isnoneornil(int index);

	/**
	 * Test if a value is a number <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a number or a
	 * string convertible to a number, and 0&nbsp;otherwise.
	 */
	public boolean isnumber(int index);

	/**
	 * Test if a value is a string <span class="apii">[-0, +0, <em>m</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a string or a
	 * number (which is always convertible to a string), and 0&nbsp;otherwise.
	 */
	public boolean isstring(int index);

	/**
	 * Test if a value is a table <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a table, and
	 * 0&nbsp;otherwise.
	 */
	public boolean istable(int index);

	/**
	 * Test if a value is a thread <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a thread, and
	 * 0&nbsp;otherwise.
	 */
	public boolean isthread(int index);

	/**
	 * Test if a value is a userdata <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a userdata
	 * (either full or light), and 0&nbsp;otherwise.
	 */
	public boolean isuserdata(int index);

	/**
	 * Compare two values <span class="apii">[-0, +0, <em>e</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at acceptable index <code>index1</code> is
	 * smaller than the value at acceptable index <code>index2</code>,
	 * following the semantics of the Lua <code>&lt;</code> operator (that is,
	 * may call metamethods). Otherwise returns&nbsp;0. Also returns&nbsp;0 if
	 * any of the indices is non valid.
	 */
	public boolean lessthan(int index1, int index2);

	/**
	 * Loads a Lua chunk. <span class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 * <p>
	 * If there are no errors, <a href="#lua_load"><code>lua_load</code></a>
	 * pushes the compiled chunk as a Lua function on top of the stack.
	 * Otherwise, it pushes an error message. The return values of <a
	 * href="#lua_load"><code>lua_load</code></a> are:
	 * 
	 * <ul>
	 * 
	 * <li><b>0:</b> no errors;</li>
	 * 
	 * <li><b><a name="pdf-LUA_ERRSYNTAX"><code>LUA_ERRSYNTAX</code></a>:</b>
	 * syntax error during pre-compilation;</li>
	 * 
	 * <li><b><a href="#pdf-LUA_ERRMEM"><code>LUA_ERRMEM</code></a>:</b>
	 * memory allocation error.</li>
	 * 
	 * </ul>
	 * 
	 * <p>
	 * This function only loads a chunk; it does not run it.
	 * 
	 * 
	 * <p>
	 * <a href="#lua_load"><code>lua_load</code></a> automatically detects
	 * whether the chunk is text or binary, and loads it accordingly (see
	 * program <code>luac</code>).
	 * 
	 * 
	 * <p>
	 * The <a href="#lua_load"><code>lua_load</code></a> function uses a
	 * user-supplied <code>reader</code> function to read the chunk (see <a
	 * href="#lua_Reader"><code>lua_Reader</code></a>). The
	 * <code>data</code> argument is an opaque value passed to the reader
	 * function.
	 * 
	 * 
	 * <p>
	 * The <code>chunkname</code> argument gives a name to the chunk, which is
	 * used for error messages and in debug information (see <a
	 * href="#3.8">&sect;3.8</a>).
	 */
	public int load(InputStream is, String chunkname);

	/**
	 * Create a table <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Creates a new empty table and pushes it onto the stack. It is equivalent
	 * to <code>lua_createtable(L, 0, 0)</code>.
	 */
	public void newtable();

	/**
	 * Create a thread <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Creates a new thread, pushes it on the stack, and returns a pointer to a
	 * <a href="#lua_State"><code>lua_State</code></a> that represents this
	 * new thread. The new state returned by this function shares with the
	 * original state all global objects (such as tables), but has an
	 * independent execution stack.
	 * 
	 * 
	 * <p>
	 * There is no explicit function to close or to destroy a thread. Threads
	 * are subject to garbage collection, like any Lua object.
	 */
	public void newthread();

	/**
	 * Create a userdata <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * This function allocates a new block of memory with the given size, pushes
	 * onto the stack a new full userdata with the block address, and returns
	 * this address.
	 * 
	 * 
	 * <p>
	 * Userdata represent C&nbsp;values in Lua. A <em>full userdata</em>
	 * represents a block of memory. It is an object (like a table): you must
	 * create it, it can have its own metatable, and you can detect when it is
	 * being collected. A full userdata is only equal to itself (under raw
	 * equality).
	 * 
	 * 
	 * <p>
	 * When Lua collects a full userdata with a <code>gc</code> metamethod,
	 * Lua calls the metamethod and marks the userdata as finalized. When this
	 * userdata is collected again then Lua frees its corresponding memory.
	 */
	public void newuserdata(Object o);

	/**
	 * Traverse to the next table item. <span class="apii">[-1, +(2|0),
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Pops a key from the stack, and pushes a key-value pair from the table at
	 * the given index (the "next" pair after the given key). If there are no
	 * more elements in the table, then <a href="#lua_next"><code>lua_next</code></a>
	 * returns 0 (and pushes nothing).
	 * 
	 * 
	 * <p>
	 * A typical traversal looks like this:
	 * 
	 * <pre>
	 * // table is in the stack at index 't' 
	 * lua_pushnil(L); // first key 
	 * while (lua_next(L, t) != 0) {
	 * 	// uses 'key' (at index -2) and 'value' (at index -1) 
	 * 	printf(&quot;%s - %s\n&quot;, lua_typename(L, lua_type(L, -2)), lua_typename(L,
	 * 			lua_type(L, -1)));
	 * 	// removes 'value'; keeps 'key' for next iteration 
	 * 	lua_pop(L, 1);
	 * }
	 * </pre>
	 * 
	 * <p>
	 * While traversing a table, do not call <a href="#lua_tolstring"><code>lua_tolstring</code></a>
	 * directly on a key, unless you know that the key is actually a string.
	 * Recall that <a href="#lua_tolstring"><code>lua_tolstring</code></a>
	 * <em>changes</em> the value at the given index; this confuses the next
	 * call to <a href="#lua_next"><code>lua_next</code></a>.
	 */
	public int next(int index);

	/**
	 * Get the length of an object <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns the "length" of the value at the given acceptable index: for
	 * strings, this is the string length; for tables, this is the result of the
	 * length operator ('<code>#</code>'); for userdata, this is the size of
	 * the block of memory allocated for the userdata; for other values, it
	 * is&nbsp;0.
	 */
	public int objlen(int index);

	/**
	 * Calls a function in protected mode. <span class="apii">[-(nargs + 1),
	 * +(nresults|1), <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * Both <code>nargs</code> and <code>nresults</code> have the same
	 * meaning as in <a href="#lua_call"><code>lua_call</code></a>. If there
	 * are no errors during the call, <a href="#lua_pcall"><code>lua_pcall</code></a>
	 * behaves exactly like <a href="#lua_call"><code>lua_call</code></a>.
	 * However, if there is any error, <a href="#lua_pcall"><code>lua_pcall</code></a>
	 * catches it, pushes a single value on the stack (the error message), and
	 * returns an error code. Like <a href="#lua_call"><code>lua_call</code></a>,
	 * <a href="#lua_pcall"><code>lua_pcall</code></a> always removes the
	 * function and its arguments from the stack.
	 * 
	 * 
	 * <p>
	 * If <code>errfunc</code> is 0, then the error message returned on the
	 * stack is exactly the original error message. Otherwise,
	 * <code>errfunc</code> is the stack index of an
	 * <em>error handler function</em>. (In the current implementation, this
	 * index cannot be a pseudo-index.) In case of runtime errors, this function
	 * will be called with the error message and its return value will be the
	 * message returned on the stack by <a href="#lua_pcall"><code>lua_pcall</code></a>.
	 * 
	 * 
	 * <p>
	 * Typically, the error handler function is used to add more debug
	 * information to the error message, such as a stack traceback. Such
	 * information cannot be gathered after the return of <a href="#lua_pcall"><code>lua_pcall</code></a>,
	 * since by then the stack has unwound.
	 * 
	 * 
	 * <p>
	 * The <a href="#lua_pcall"><code>lua_pcall</code></a> function returns
	 * 0 in case of success or one of the following error codes (defined in
	 * <code>lua.h</code>):
	 * 
	 * <ul>
	 * 
	 * <li><b><a name="pdf-LUA_ERRRUN"><code>LUA_ERRRUN</code></a>:</b> a
	 * runtime error. </li>
	 * 
	 * <li><b><a name="pdf-LUA_ERRMEM"><code>LUA_ERRMEM</code></a>:</b>
	 * memory allocation error. For such errors, Lua does not call the error
	 * handler function. </li>
	 * 
	 * <li><b><a name="pdf-LUA_ERRERR"><code>LUA_ERRERR</code></a>:</b>
	 * error while running the error handler function. </li>
	 * 
	 * </ul>
	 */
	public int pcall(int nargs, int nresults, int errfunc);

	/**
	 * Pops <code>n</code> elements from the stack. <span class="apii">[-n,
	 * +0, <em>-</em>]</span>
	 */
	public void pop(int n);

	/**
	 * Pushes a boolean value with value <code>b</code> onto the stack. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 */
	public void pushboolean(boolean b);

	/**
	 * Pushes a new C&nbsp;closure onto the stack. <span class="apii">[-n, +1,
	 * <em>m</em>]</span>
	 * 
	 * 
	 * <p>
	 * When a Java&nbsp;function is created, it is possible to associate some
	 * values with it, thus creating a C&nbsp;closure (see <a
	 * href="#3.4">&sect;3.4</a>); these values are then accessible to the
	 * function whenever it is called. To associate values with a
	 * C&nbsp;function, first these values should be pushed onto the stack (when
	 * there are multiple values, the first value is pushed first). Then <a
	 * href="#lua_pushcclosure"><code>lua_pushcclosure</code></a> is called
	 * to create and push the C&nbsp;function onto the stack, with the argument
	 * <code>n</code> telling how many values should be associated with the
	 * function. <a href="#lua_pushcclosure"><code>lua_pushcclosure</code></a>
	 * also pops these values from the stack.
	 */
	public void pushclosure(JavaFunction fn, int n);

	/**
	 * Pushes a Java&nbsp;function onto the stack. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * This function receives a pointer to a C function and pushes onto the
	 * stack a Lua value of type <code>function</code> that, when called,
	 * invokes the corresponding C&nbsp;function.
	 * 
	 * 
	 * <p>
	 * Any function to be registered in Lua must follow the correct protocol to
	 * receive its parameters and return its results (see <a
	 * href="#lua_CFunction"><code>lua_CFunction</code></a>).
	 * 
	 */
	public void pushjavafunction(JavaFunction f);

	/**
	 * Format and push a string. <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack a formatted string and returns a pointer to this
	 * string. It is similar to the C&nbsp;function <code>sprintf</code>, but
	 * has some important differences:
	 * 
	 * <ul>
	 * 
	 * <li> You do not have to allocate space for the result: the result is a
	 * Lua string and Lua takes care of memory allocation (and deallocation,
	 * through garbage collection). </li>
	 * 
	 * <li> The conversion specifiers are quite restricted. There are no flags,
	 * widths, or precisions. The conversion specifiers can only be '<code>%%</code>'
	 * (inserts a '<code>%</code>' in the string), '<code>%s</code>'
	 * (inserts a zero-terminated string, with no size restrictions), '<code>%f</code>'
	 * (inserts a <a href="#lua_Number"><code>lua_Number</code></a>), '<code>%p</code>'
	 * (inserts a pointer as a hexadecimal numeral), '<code>%d</code>'
	 * (inserts an <code>int</code>), and '<code>%c</code>' (inserts an
	 * <code>int</code> as a character). </li>
	 * 
	 * </ul>
	 */
	public String pushfstring(String fmt, Object[] args);

	/**
	 * Pushes a number with value <code>n</code> onto the stack. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 */
	public void pushinteger(int n);

	/**
	 * Pushes a light userdata onto the stack. <span class="apii">[-0, +1,
	 * <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * Userdata represent C&nbsp;values in Lua. A <em>light userdata</em>
	 * represents a pointer. It is a value (like a number): you do not create
	 * it, it has no individual metatable, and it is not collected (as it was
	 * never created). A light userdata is equal to "any" light userdata with
	 * the same C&nbsp;address.
	 */
	public void pushlightuserdata(Object p);

	/**
	 * Push string bytes onto the stack as a string. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 * 
	 * Pushes the string pointed to by <code>s</code> with size
	 * <code>len</code> onto the stack. Lua makes (or reuses) an internal copy
	 * of the given string, so the memory at <code>s</code> can be freed or
	 * reused immediately after the function returns. The string can contain
	 * embedded zeros.
	 */
	public void pushlstring(byte[] bytes, int offset, int length);

	/**
	 * Push string bytes onto the stack as a string. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 * 
	 * Pushes the bytes in byteArray onto the stack as a lua string. 
	 */
	public void pushlstring(byte[] byteArray);

	/** 
	 * Push an LValue onto the stack. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 */
	public void pushlvalue(LValue v);

	/** 
	 * Push an LString onto the stack. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 */
	public void pushlstring(LString luaGetTypeName);
	
	/**
	 * Pushes a nil value onto the stack. <span class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 */
	public void pushnil();

	/**
	 * Pushes a number with value <code>d</code> onto the stack. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 */
	public void pushnumber(double d);

	/**
	 * Push a String onto the stack. <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Pushes the String <code>s</code> onto the stack. Lua makes (or reuses)
	 * an internal copy of the given string, so the memory at <code>s</code>
	 * can be freed or reused immediately after the function returns. The string
	 * cannot contain embedded zeros; it is assumed to end at the first zero.
	 */
	public void pushstring(String s);

	/**
	 * Push a thread onto the stack. <span class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 * Pushes the thread represented by <code>L</code> onto the stack. Returns
	 * 1 if this thread is the main thread of its state.
	 */
	public void pushthread();

	/**
	 * Push a value from the stack onto the stack. <span class="apii">[-0, +1,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes a copy of the element at the given valid index onto the stack.
	 */
	public void pushvalue(int index);

	/**
	 * Format and push a string. <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Equivalent to <a href="#lua_pushfstring"><code>lua_pushfstring</code></a>,
	 * except that it receives a <code>va_list</code> instead of a variable
	 * number of arguments.
	 */
	public void pushvfstring(String format, Object[] args);

	/**
	 * Test if two objects are the same object. <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the two values in acceptable indices <code>index1</code>
	 * and <code>index2</code> are primitively equal (that is, without calling
	 * metamethods). Otherwise returns&nbsp;0. Also returns&nbsp;0 if any of the
	 * indices are non valid.
	 */
	public void rawequal(int index1, int index2);

	/**
	 * Do a table get without metadata calls. <span class="apii">[-1, +1,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Similar to <a href="#lua_gettable"><code>lua_gettable</code></a>, but
	 * does a raw access (i.e., without metamethods).
	 */
	public void rawget(int index);

	/**
	 * Do a integer-key table get without metadata calls. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value <code>t[n]</code>, where
	 * <code>t</code> is the value at the given valid index. The access is
	 * raw; that is, it does not invoke metamethods.
	 */
	public void rawgeti(int index, int n);

	/**
	 * Do a table set without metadata calls. <span class="apii">[-2, +0,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * Similar to <a href="#lua_settable"><code>lua_settable</code></a>, but
	 * does a raw assignment (i.e., without metamethods).
	 */
	public void rawset(int index);

	/**
	 * Do a integer-key table set without metadata calls. <span
	 * class="apii">[-1, +0, <em>m</em>]</span>
	 * 
	 * <p>
	 * Does the equivalent of <code>t[n] = v</code>, where <code>t</code>
	 * is the value at the given valid index and <code>v</code> is the value
	 * at the top of the stack.
	 * 
	 * 
	 * <p>
	 * This function pops the value from the stack. The assignment is raw; that
	 * is, it does not invoke metamethods.
	 */
	public void rawseti(int index, int n);

	/**
	 * Register a JavaFunction with a specific name. <span class="apii">[-0, +0,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * Sets the C function <code>f</code> as the new value of global
	 * <code>name</code>. It is defined as a macro:
	 * 
	 * <pre>
	 * 	 #define lua_register(L,n,f) \
	 * 	 (lua_pushcfunction(L, f), lua_setglobal(L, n))
	 * 	
	 * </pre>
	 */
	public void register(String name, JavaFunction f);

	/**
	 * Remove an element from the stack. <span class="apii">[-1, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Removes the element at the given valid index, shifting down the elements
	 * above this index to fill the gap. Cannot be called with a pseudo-index,
	 * because a pseudo-index is not an actual stack position.
	 */
	public void remove(int index);

	/**
	 * Replace an element on the stack. <span class="apii">[-1, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Moves the top element into the given position (and pops it), without
	 * shifting any element (therefore replacing the value at the given
	 * position).
	 */
	public void replace(int index);

	/**
	 * Starts and resumes a coroutine in a given thread. <span class="apii">[-?,
	 * +?, <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * To start a coroutine, you first create a new thread (see <a
	 * href="#lua_newthread"><code>lua_newthread</code></a>); then you push
	 * onto its stack the main function plus any arguments; then you call <a
	 * href="#lua_resume"><code>lua_resume</code></a>, with
	 * <code>narg</code> being the number of arguments. This call returns when
	 * the coroutine suspends or finishes its execution. When it returns, the
	 * stack contains all values passed to <a href="#lua_yield"><code>lua_yield</code></a>,
	 * or all values returned by the body function. <a href="#lua_resume"><code>lua_resume</code></a>
	 * returns <a href="#pdf-LUA_YIELD"><code>LUA_YIELD</code></a> if the
	 * coroutine yields, 0 if the coroutine finishes its execution without
	 * errors, or an error code in case of errors (see <a href="#lua_pcall"><code>lua_pcall</code></a>).
	 * In case of errors, the stack is not unwound, so you can use the debug API
	 * over it. The error message is on the top of the stack. To restart a
	 * coroutine, you put on its stack only the values to be passed as results
	 * from <code>yield</code>, and then call <a href="#lua_resume"><code>lua_resume</code></a>.
	 */
	public void resume(int narg);

	/**
	 * Set the environment for a value. <span class="apii">[-1, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Pops a table from the stack and sets it as the new environment for the
	 * value at the given index. If the value at the given index is neither a
	 * function nor a thread nor a userdata, <a href="#lua_setfenv"><code>lua_setfenv</code></a>
	 * returns 0. Otherwise it returns 1.
	 */
	public int setfenv(int index);

	/**
	 * Set the value of a table field. <span class="apii">[-1, +0, <em>e</em>]</span>
	 * 
	 * <p>
	 * Does the equivalent to <code>t[k] = v</code>, where <code>t</code>
	 * is the value at the given valid index and <code>v</code> is the value
	 * at the top of the stack.
	 * 
	 * 
	 * <p>
	 * This function pops the value from the stack. As in Lua, this function may
	 * trigger a metamethod for the "newindex" event (see <a
	 * href="#2.8">&sect;2.8</a>).
	 */
	public void setfield(int index, String k);

	/**
	 * Set the value of a global variable. <span class="apii">[-1, +0,
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Pops a value from the stack and sets it as the new value of global
	 * <code>name</code>. It is defined as a macro:
	 * 
	 * <pre>
	 * 	 #define lua_setglobal(L,s)   lua_setfield(L, LUA_GLOBALSINDEX, s)
	 * 	
	 * </pre>
	 */
	public void setglobal(String name);

	/**
	 * Set the metatable of a value. <span class="apii">[-1, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Pops a table from the stack and sets it as the new metatable for the
	 * value at the given acceptable index.
	 */
	public void setmetatable(int index);

	/**
	 * Set the value of a table for a key. <span class="apii">[-2, +0,
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Does the equivalent to <code>t[k] = v</code>, where <code>t</code>
	 * is the value at the given valid index, <code>v</code> is the value at
	 * the top of the stack, and <code>k</code> is the value just below the
	 * top.
	 * 
	 * 
	 * <p>
	 * This function pops both the key and the value from the stack. As in Lua,
	 * this function may trigger a metamethod for the "newindex" event (see <a
	 * href="#2.8">&sect;2.8</a>).
	 */
	public void settable(int index);

	/**
	 * Set the top of the stack. <span class="apii">[-?, +?, <em>-</em>]</span>
	 * 
	 * <p>
	 * Accepts any acceptable index, or&nbsp;0, and sets the stack top to this
	 * index. If the new top is larger than the old one, then the new elements
	 * are filled with <b>nil</b>. If <code>index</code> is&nbsp;0, then all
	 * stack elements are removed.
	 */
	public void settop(int index);

	/**
	 * Returns the status of the thread <code>L</code>. <span
	 * class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * 
	 * 
	 * <p>
	 * The status can be 0 for a normal thread, an error code if the thread
	 * finished its execution with an error, or <a name="pdf-LUA_YIELD"><code>LUA_YIELD</code></a>
	 * if the thread is suspended.
	 * 
	 */
	public void status();

	/**
	 * Get a value as a boolean. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the Lua value at the given acceptable index to a C&nbsp;boolean
	 * value (0&nbsp;or&nbsp;1). Like all tests in Lua, <a
	 * href="#lua_toboolean"><code>lua_toboolean</code></a> returns 1 for
	 * any Lua value different from <b>false</b> and <b>nil</b>; otherwise it
	 * returns 0. It also returns 0 when called with a non-valid index. (If you
	 * want to accept only actual boolean values, use <a href="#lua_isboolean"><code>lua_isboolean</code></a>
	 * to test the value's type.)
	 * 
	 */
	public boolean toboolean(int index);

	/**
	 * Get a value as a JavaFunction.
	 * <hr>
	 * <h3><a name="lua_tocfunction"><code>lua_tocfunction</code></a></h3>
	 * <p>
	 * <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <pre>
	 * lua_CFunction lua_tocfunction (lua_State *L, int index);
	 * </pre>
	 * 
	 * <p>
	 * Converts a value at the given acceptable index to a C&nbsp;function. That
	 * value must be a C&nbsp;function; otherwise, returns <code>NULL</code>.
	 */
	public JavaFunction tojavafunction(int index);

	/**
	 * Get a value as an int. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the Lua value at the given acceptable index to the signed
	 * integral type <a href="#lua_Integer"><code>lua_Integer</code></a>.
	 * The Lua value must be a number or a string convertible to a number (see
	 * <a href="#2.2.1">&sect;2.2.1</a>); otherwise, <a href="#lua_tointeger"><code>lua_tointeger</code></a>
	 * returns&nbsp;0.
	 * 
	 * 
	 * <p>
	 * If the number is not an integer, it is truncated in some non-specified
	 * way.
	 */
	public int tointeger(int index);

	/**
	 * Gets the value of a string as byte array. <span class="apii">[-0, +0,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * Converts the Lua value at the given acceptable index to a C&nbsp;string.
	 * If <code>len</code> is not <code>NULL</code>, it also sets
	 * <code>*len</code> with the string length. The Lua value must be a
	 * string or a number; otherwise, the function returns <code>NULL</code>.
	 * If the value is a number, then <a href="#lua_tolstring"><code>lua_tolstring</code></a>
	 * also <em>changes the actual value in the stack to a string</em>. (This
	 * change confuses <a href="#lua_next"><code>lua_next</code></a> when <a
	 * href="#lua_tolstring"><code>lua_tolstring</code></a> is applied to
	 * keys during a table traversal.)
	 * 
	 * 
	 * <p>
	 * <a href="#lua_tolstring"><code>lua_tolstring</code></a> returns a
	 * fully aligned pointer to a string inside the Lua state. This string
	 * always has a zero ('<code>\0</code>') after its last character (as
	 * in&nbsp;C), but may contain other zeros in its body. Because Lua has
	 * garbage collection, there is no guarantee that the pointer returned by <a
	 * href="#lua_tolstring"><code>lua_tolstring</code></a> will be valid
	 * after the corresponding value is removed from the stack.
	 */
	public LString tolstring(int index);

	/**
	 * Convert a value to a double. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the Lua value at the given acceptable index to the C&nbsp;type
	 * <a href="#lua_Number"><code>lua_Number</code></a> (see <a
	 * href="#lua_Number"><code>lua_Number</code></a>). The Lua value must
	 * be a number or a string convertible to a number (see <a
	 * href="#2.2.1">&sect;2.2.1</a>); otherwise, <a href="#lua_tonumber"><code>lua_tonumber</code></a>
	 * returns&nbsp;0.
	 * 
	 */
	public double tonumber(int index);

	/**
	 * Get the raw Object at a stack location. <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the value at the given acceptable index to a generic
	 * C&nbsp;pointer (<code>void*</code>). The value may be a userdata, a
	 * table, a thread, or a function; otherwise, <a href="#lua_topointer"><code>lua_topointer</code></a>
	 * returns <code>NULL</code>. Different objects will give different
	 * pointers. There is no way to convert the pointer back to its original
	 * value.
	 * 
	 * 
	 * <p>
	 * Typically this function is used only for debug information.
	 */
	public LValue topointer(int index);

	/**
	 * Get a stack value as a String. <span class="apii">[-0, +0, <em>m</em>]</span>
	 * 
	 * <p>
	 * Equivalent to <a href="#lua_tolstring"><code>lua_tolstring</code></a>
	 * with <code>len</code> equal to <code>NULL</code>.
	 */
	public String tostring(int index);

	/**
	 * Get a thread value from the stack. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the value at the given acceptable index to a Lua thread
	 * (represented as <code>lua_State*</code>). This value must be a thread;
	 * otherwise, the function returns <code>NULL</code>.
	 */
	public StackState tothread(int index);

	/**
	 * Get a value from the stack as a lua table. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the value at the given acceptable index to a Lua table
	 * This value must be a table otherwise, the function returns <code>NIL</code>.
	 */
	public LTable totable(int index);

	/**
	 * Get the Object from a userdata value. <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * If the value at the given acceptable index is a full userdata, returns
	 * its block address. If the value is a light userdata, returns its pointer.
	 * Otherwise, returns <code>NULL</code>.
	 * 
	 */
	public Object touserdata(int index);

	/**
	 * Get the type of a value. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns the type of the value in the given acceptable index, or
	 * <code>LUA_TNONE</code> for a non-valid index (that is, an index to an
	 * "empty" stack position). The types returned by <a href="#lua_type"><code>lua_type</code></a>
	 * are coded by the following constants defined in <code>lua.h</code>:
	 * <code>LUA_TNIL</code>, <code>LUA_TNUMBER</code>,
	 * <code>LUA_TBOOLEAN</code>, <code>LUA_TSTRING</code>,
	 * <code>LUA_TTABLE</code>, <code>LUA_TFUNCTION</code>,
	 * <code>LUA_TUSERDATA</code>, <code>LUA_TTHREAD</code>, and
	 * <code>LUA_TLIGHTUSERDATA</code>.
	 */
	public int type(int index);

	/**
	 * Get the type name for a value. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns the name of the type encoded by the value <code>tp</code>,
	 * which must be one the values returned by <a href="#lua_type"><code>lua_type</code></a>.
	 */
	public String typename(int tp);

	/**
	 * Exchange values between threads. <span class="apii">[-?, +?, <em>-</em>]</span>
	 * 
	 * <p>
	 * Exchange values between different threads of the <em>same</em> global
	 * state.
	 * 
	 * 
	 * <p>
	 * This function pops <code>n</code> values from the stack
	 * <code>from</code>, and pushes them onto the stack <code>to</code>.
	 */
	public void xmove(VM to, int n);

	/**
	 * Yields a coroutine. <span class="apii">[-?, +?, <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * This function should only be called as the return expression of a
	 * C&nbsp;function, as follows:
	 * 
	 * <pre>
	 * return lua_yield(L, nresults);
	 * </pre>
	 * 
	 * <p>
	 * When a C&nbsp;function calls <a href="#lua_yield"><code>lua_yield</code></a>
	 * in that way, the running coroutine suspends its execution, and the call
	 * to <a href="#lua_resume"><code>lua_resume</code></a> that started
	 * this coroutine returns. The parameter <code>nresults</code> is the
	 * number of values from the stack that are passed as results to <a
	 * href="#lua_resume"><code>lua_resume</code></a>.
	 * 
	 */
	public void yield(int nresults);

	// ============================= conversion to and from Java boxed types ====================
	
	/**
	 * Push a Java Boolean value, or nil if the value is null. 
	 * @param b Boolean value to convert, or null to to nil.
	 */
	public void pushboolean( Boolean b );
	
	/**
	 * Push a Java Byte value, or nil if the value is null. 
	 * @param b Byte value to convert, or null to to nil.
	 */
	public void pushinteger( Byte b );
	
	/**
	 * Push a Java Character value, or nil if the value is null. 
	 * @param c Character value to convert, or null to to nil.
	 */
	public void pushinteger( Character c );
	
	/**
	 * Push a Java Double as a double, or nil if the value is null. 
	 * @param d Double value to convert, or null to to nil.
	 */
	public void pushnumber( Double d );
	
	/**
	 * Push a Java Float value, or nil if the value is null. 
	 * @param f Float value to convert, or null to to nil.
	 */
	public void pushnumber( Float f );
	
	/**
	 * Push a Java Integer value, or nil if the value is null. 
	 * @param i Integer value to convert, or null to to nil.
	 */
	public void pushinteger( Integer i );
	
	/**
	 * Push a Java Short value, or nil if the value is null. 
	 * @param s Short value to convert, or null to to nil.
	 */
	public void pushinteger( Short s );
	
	/**
	 * Push a Java Long value, or nil if the value is null. 
	 * @param l Long value to convert, or null to to nil.
	 */
	public void pushnumber( Long l );
	
	/**
	 * Push a Java Object as userdata, or nil if the value is null. 
	 * @param o Object value to push, or null to to nil.
	 */
	public void pushuserdata( Object o );


	/**
	 * Convert a value to a Java Boolean value, or null if the value is nil.
	 * @param index index of the parameter to convert. 
	 * @return Boolean value at the index, or null if the value was not a boolean.
	 */
	public Boolean toboxedboolean(int index);
	
	/**
	 * Convert a value to a Java Byte value, or null if the value is not a number.
	 * @param index index of the parameter to convert. 
	 * @return Byte value at the index, or null if the value was not a number.
	 */
	public Byte toboxedbyte(int index);
	
	/**
	 * Convert a value to a Java Double value, or null if the value is not a number.
	 * @param index index of the parameter to convert. 
	 * @return Double value at the index, or null if the value was not a number.
	 */
	public Double toboxeddouble(int index);
	
	/**
	 * Convert a value to a Java Float value, or null if the value is not a number.
	 * @param index index of the parameter to convert. 
	 * @return Float value at the index, or null if the value was not a boolean.
	 */
	public Float toboxedfloat(int index);
	
	/**
	 * Convert a value to a Java Integer value, or null if the value is not a number.
	 * @param index index of the parameter to convert. 
	 * @return Integer value at the index, or null if the value was not a number.
	 */
	public Integer toboxedinteger(int index);
	
	/**
	 * Convert a value to a Java Long value, or null if the value is nil.
	 * @param index index of the parameter to convert. 
	 * @return Long value at the index, or null if the value was not a number.
	 */
	public Long toboxedlong(int index);

}
