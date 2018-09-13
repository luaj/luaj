# This is a fork!
<div style="border: 1px dotted red; margin: 1.em 0.5em; font-weight: bold; color: red;">
This repository has been forked from the original CVS sources of Luaj.
The commit history has been converted to make sure that the original work of
James Roseborough and Ian Farmer is not lost.
Unfortunately, I was not able to contact either James or Ian to hand over
ownership of the Github organization/repo as I have originally intended to.
The community however seems interested enough to continue work on the original
sources and therefore I have decided to make sure that any useful pull requests
that may add some value to the original code base shall be merged in from now
on.<br>
-- Benjamin P. Jung, Jan. 26th 2018
</div>

<h1>Getting Started with LuaJ</h1>


<h1>Getting Started with LuaJ</h1>
James Roseborough, Ian Farmer, Version 3.0.2
<p>
<small>
Copyright &copy; 2009-2014 Luaj.org.
Freely available under the terms of the
<a href="http://sourceforge.net/dbimage.php?id=196142">Luaj license</a>.
</small>
<hr>
<p>

<a href="#1">introduction</a>
&middot;
<a href="#2">examples</a>
&middot;
<a href="#3">concepts</a>
&middot;
<a href="#4">libraries</a>
&middot;
<a href="#5">luaj api</a>
&middot;
<a href="#6">parser</a>
&middot;
<a href="#7">building</a>
&middot;
<a href="#8">downloads</a>
&middot;
<a href="#9">release notes</a>

<!-- ====================================================================== -->
<p>

<h1>1 - <a name="1">Introduction</a></h1>
<h2>Goals of Luaj</h2>
Luaj is a lua interpreter based on the 5.2.x version of lua with the following goals in mind:
<ul>
<li>Java-centric implementation of lua vm built to leverage standard Java features.
<li>Lightweight, high performance execution of lua. 
<li>Multi-platform to be able to run on JME, JSE, or JEE environments. 
<li>Complete set of libraries and tools for integration into real-world projects.
<li>Dependable due to sufficient unit testing of vm and library features. 
</ul>

<h2>Luaj version and Lua Versions</h2>
<h3>Luaj 3.0.x</h3>
Support for lua 5.2.x features:
<ul>
<li>_ENV environments model.
<li>yield from pcall or metatags.
<li>Bitwise operator library.
</ul>
It also includes miscellaneous improvements over luaj 2.0.x:
<ul>
<li>Better thread safety.
<li>More compatible table behavior.
<li>Better coroutine-related garbage collection. 
<li>Maven integration.
<li>Better debug reporting when using closures.
<li>Line numbers in parse syntax tree.
</ul>
<h3>Luaj 2.0.x</h3>
Support for lua 5.1.x features, plus:
<ul>
<li>Support for compiling lua source code into Java source code.
<li>Support for compiling lua bytecode directly into Java bytecode.
<li>Stackless vm design centered around dynamically typed objects.
<li>Good alignment with C API (see <a href="names.csv">names.csv</a> for details)
<li>Implementation of weak keys and values, and all metatags. 
</ul>
<h3>Luaj 1.0.x</h3>
Support for most lua 5.1.x features.

<h2>Performance</h2>
Good performance is a major goal of luaj.  
The following table provides measured execution times on a subset of benchmarks from 
<a href="http://shootout.alioth.debian.org/">the computer language benchmarks game</a>  
in comparison with the standard C distribution. 
<table cellspacing="10"><tr><td><table>
<tr valign="top">
	<td><u>Project</td>
	<td><u>Version</td>
	<td><u>Mode</td>
	<td rowspan="9">&nbsp;&nbsp;</td>
	<td colspan="4" align="center"><u>Benchmark&nbsp;execution&nbsp;time&nbsp;(sec)</td>
	<td rowspan="9">&nbsp;&nbsp;</td>
	<td><u>Language</td>
	<td><u>Sample&nbsp;command</td>
	</tr>
<tr valign="top">
	<td colspan="2"></td>
	<td></td>
	<td><em>binarytrees 15</em></td>
	<td><em>fannkuch 10</em></td>
	<td><em>nbody 1e6</em></td>
	<td><em>nsieve 9</em></td>
	</tr>
<tr valign="top">
	<td>luaj</td>
	<td>3.0</td>
	<td>-b (luajc)</td>
	<td>2.980</td>
	<td>5.073</td>
	<td>16.794</td>
	<td>11.274</td>
	<td>Java</td>
	<td>java -cp luaj-jse-3.0.2.jar;bcel-5.2.jar lua <b>-b</b> fannkuch.lua 10</td></tr>
<tr valign="top">
	<td></td>
	<td></td>
	<td>-n (interpreted)</td>
	<td>12.838</td>
	<td>23.290</td>
	<td>36.894</td>
	<td>15.163</td>
	<td></td>
	<td>java -cp luaj-jse-3.0.2.jar lua -n fannkuch.lua 10</td></tr>
<tr valign="top">
	<td>lua</td>
	<td>5.1.4</td>
	<td></td>
	<td>17.637</td>
	<td>16.044</td>
	<td>15.201</td>
	<td>5.477</td>
	<td>C</td>
	<td>lua fannkuch.lua 10</td></tr>
<tr valign="top">
	<td>jill</td>
	<td>1.0.1</td>
	<td></td>
	<td>44.512</td>
	<td>54.630</td>
	<td>72.172</td>
	<td>20.779</td>
	<td>Java</td>
	<td></td></tr>
<tr valign="top">
	<td>kahlua</td>
	<td>1.0</td>
	<td>jse</td>
	<td>22.963</td>
	<td>63.277</td>
	<td>68.223</td>
	<td>21.529</td>
	<td>Java</td>
	<td></td></tr>
<tr valign="top">
	<td>mochalua</td>
	<td>1.0</td>
	<td></td>
	<td>50.457</td>
	<td>70.368</td>
	<td>82.868</td>
	<td>41.262</td>
	<td>Java</td>
	<td></td></tr>
</table></td></tr></table>

Luaj in interpreted mode performs well for the benchmarks, and even better when 
the lua-to-java-bytecode (luajc) compiler is used, 
and actually executes <em>faster</em> than C-based lua in some cases.  
It is also faster than Java-lua implementations Jill, Kahlua, and Mochalua for all benchmarks tested.  

<h1>2 - <a name="2">Examples</a></h1>

<h2>Run a lua script in Java SE</h2>

<p>
From the main distribution directory line type:

<pre>
	java -cp lib/luaj-jse-3.0.2.jar lua examples/lua/hello.lua
</pre>

<p>
You should see the following output:
<pre>
	hello, world
</pre>

To see how luaj can be used to acccess most Java API's including swing, try:

<pre>
	java -cp lib/luaj-jse-3.0.2.jar lua examples/lua/swingapp.lua
</pre>

<p>
Links to sources:<pre>
	<a href="examples/lua/hello.lua">examples/lua/hello.lua</a>
	<a href="examples/lua/swingapp.lua">examples/lua/swingapp.lua</a>
</pre>

<h2>Compile lua source to lua bytecode</h2>

<p>
From the main distribution directory line type:

<pre>
	java -cp lib/luaj-jse-3.0.2.jar luac examples/lua/hello.lua
	java -cp lib/luaj-jse-3.0.2.jar lua luac.out
</pre>

<p>
The compiled output "luac.out" is lua bytecode and should run and produce the same result.


<h2>Compile lua source or bytecode to java bytecode</h2>

<p>
Luaj can compile lua sources or binaries directly to java bytecode if the bcel library is on the class path. From the main distribution directory line type:

<pre>
	ant bcel-lib
	java -cp &quot;lib/luaj-jse-3.0.2.jar;lib/bcel-5.2.jar&quot; luajc -s examples/lua -d . hello.lua
	java -cp &quot;lib/luaj-jse-3.0.2.jar;.&quot; lua -l hello
</pre>

<p>
The output <em>hello.class</em> is Java bytecode, should run and produce the same result.
There is no runtime dependency on the bcel library, 
but the compiled classes must be in the class path at runtime, unless runtime jit-compiling via luajc and bcel are desired (see later sections).

<p>
Lua scripts can also be run directly in this mode without precompiling using the <em>lua</em> command with the <b><em>-b</em></b> option and providing the <em>bcel</em> library in the class path:
<pre>
	java -cp &quot;lib/luaj-jse-3.0.2.jar;lib/bcel-5.2.jar&quot; lua -b examples/lua/hello.lua
</pre>


<h2>Run a script in a Java Application</h2>

<p>
A simple hello, world example in luaj is:

<pre>
	import org.luaj.vm2.*;
	import org.luaj.vm2.lib.jse.*;

	Globals globals = JsePlatform.standardGlobals();
	LuaValue chunk = globals.load("print 'hello, world'");
	chunk.call();
	
</pre>

Loading from a file is done via Globals.loadFile():

<pre>
	LuaValue chunk = globals.loadfile("examples/lua/hello.lua");
</pre>

Chunks can also be loaded from a <code>Reader</code> as text source

<pre>
	chunk = globals.load(new StringReader("print 'hello, world'"), "main.lua");
</pre>

or an InputStream to be loaded as text source "t", or binary lua file "b":

<pre>
	chunk = globals.load(new FileInputSStream("examples/lua/hello.lua"), "main.lua", "bt"));
</pre>

<p>
A simple example may be found in
<pre>
	<a href="examples/jse/SampleJseMain.java">examples/jse/SampleJseMain.java</a>
</pre>

<p>
You must include the library <b>lib/luaj-jse-3.0.2.jar</b> in your class path.

<h2>Run a script in a MIDlet</h2>

<p>
For MIDlets the <em>JmePlatform</em> is used instead:

<pre>
	import org.luaj.vm2.*;
	import org.luaj.vm2.lib.jme.*;

	Globals globals = JmePlatform.standardGlobals();
	LuaValue chunk = globals.loadfile("examples/lua/hello.lua");
	chunk.call();
</pre>

<p>
The file must be a resource within within the midlet jar for the loader to find it.
Any files included via <em>require()</em> must also be part of the midlet resources.

<p>
A simple example may be found in
<pre>
	<a href="examples/jme/SampleMIDlet.java">examples/jme/SampleMIDlet.java</a>
</pre>

<p>
You must include the library <b>lib/luaj-jme-3.0.2.jar</b> in your midlet jar.

<p>
An ant script to build and run the midlet is in
<pre>
	<a href="build-midlet.xml">build-midlet.xml</a>
</pre>

<p>
You must install the wireless toolkit and define <em>WTK_HOME</em> for this script to work. 

<h2>Run a script using JSR-223 Dynamic Scripting</h2>

<p>
The standard use of JSR-223 scripting engines may be used:

<pre>
	ScriptEngineManager mgr = new ScriptEngineManager();
	ScriptEngine e = mgr.getEngineByName("luaj");
	e.put("x", 25);
	e.eval("y = math.sqrt(x)");
	System.out.println( "y="+e.get("y") );
</pre>

You can also look up the engine by language "lua" or mimetypes "text/lua" or "application/lua".

<p>
All standard aspects of script engines including compiled statements are supported.

<p>
You must include the library <b>lib/luaj-jse-3.0.2.jar</b> in your class path.

<p>
A working example may be found in
<pre>
	<a href="examples/jse/ScriptEngineSample.java">examples/jse/ScriptEngineSample.java</a>
</pre>

To compile and run it using Java 1.6 or higher:

<pre>
	javac -cp lib/luaj-jse-3.0.2.jar examples/jse/ScriptEngineSample.java
	java -cp &quot;lib/luaj-jse-3.0.2.jar;examples/jse&quot; ScriptEngineSample
</pre>

<h2>Excluding the lua bytecode compiler</h2>

By default, the compiler is included whenever <em>standardGlobals()</em> or <em>debugGlobals()</em> are called.  
Without a compiler, files can still be executed, but they must be compiled elsewhere beforehand.
The "luac" utility is provided in the jse jar for this purpose, or a standard lua compiler can be used.  

<p>
To exclude the lua-to-lua-bytecode compiler, do not call 
<em>standardGlobals()</em> or <em>debugGlobals()</em> 
but instead initialize globals with including only those libraries 
that are needed and omitting the line:
<pre>
	org.luaj.vm2.compiler.LuaC.install(globals);
</pre>


<h2>Including the LuaJC lua-bytecode-to-Java-bytecode compiler</h2>

<p>
To compile from lua to Java bytecode for all lua loaded at runtime, 
install the LuaJC compiler into a <em>globals</em> object use:

<pre>
	org.luaj.vm2.jse.luajc.LuaJC.install(globals);
</pre>

<p>
This will compile all lua bytecode into Java bytecode, regardless of if they are loaded as
lua source or lua binary files.

<p>
The requires <em>bcel</em> to be on the class path, and the ClassLoader of JSE or CDC.  

<h1>3 - <a name="3">Concepts</a></h1>

<h2>Globals</h2>
The old notion of platform has been replaced with creation of globals.  
The  <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/Globals.html">Globals</a>
class holds global state needed for executing closures as well as providing 
convenience functions for compiling and loading scripts.

<h2>Platform</h2>
To simplify construction of Globals, and encapsulate differences needed to support 
the diverse family of Java runtimes, luaj uses a Platform notion.  
Typically, a platform is used to construct a Globals, which is then provided as a global 
environment for client scripts.

<h3>JsePlatform</h3>
The <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/lib/jse/JsePlatform.html">JsePlatform</a> 
class can be used as a factory for globals in a typical Java SE application. 
All standard libraries are included, as well as the luajava library. 
The default search path is the current directory,
and the math operations include all those supported by Java SE.

<h4>Android</h4>

Android applications should use the JsePlatform, and can include the <a href="#luajava">Luajava</a> library 
to simplify access to underlying Android APIs.  
A specialized Globals.finder should be provided to find scripts and data for loading.
See <a href="examples/android/src/android/LuajView">examples/android/src/android/LuajView</a>
for an example that loads from the "res" Android project directory.
The ant build script is <a href="examples/android/build.xml">examples/android/build.xml</a>.  

<h4>Applet</h4>

Applets in browsers should use the JsePlatform.  The permissions model in applets is 
highly restrictive, so a specialization of the <a href="#luajava">Luajava</a> library must be used that 
uses default class loading.  This is illustrated in the sample Applet 
<a href="examples/jse/SampleApplet.java">examples/jse/SampleApplet.java</a>, 
which can be built using <a href="build-applet.xml">build-applet.xml</a>.


<h3>JmePlatform</h3>
The <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/lib/jme/JmePlatform.html">JmePlatform</a> 
class can be used to set up the basic environment for a Java ME application.
The default search path is limited to the jar resources,
and the math operations are limited to those supported by Java ME.
All libraries are included except luajava, and the os, io, and math libraries are 
limited to those functions that can be supported on that platform.

<h4>MIDlet</h4>

MIDlets require the JmePlatform.  
The JME platform has several limitations which carry over to luaj.
In particular Globals.finder is overridden to load as resources, so scripts should be 
colocated with class files in the MIDlet jar file.  <a href="#luajava">Luajava</a> cannot be used.
Camples code is in  
<a href="examples/jme/SampleMIDlet.java">examples/jme/SampleMIDlet.java</a>, 
which can be built using <a href="build-midlet.xml">build-midlet.xml</a>.


<h2>Thread Safety</h2>

Luaj 3.0 can be run in multiple threads, with the following restrictions:
<ul>
<li>Each thread created by client code must be given its own, distinct Globals instance
<li>Each thread must not be allowed to access Globals from other threads
<li>Metatables for Number, String, Thread, Function, Boolean, and and Nil 
 are shared and therefore should not be mutated once lua code is running in any thread.
</ul>

For an example of loading allocating per-thread Globals and invoking scripts in 
multiple threads see <a href="examples/jse/SampleMultiThreaded.java">examples/jse/SampleMultiThreaded.java</a>

<p>
As an alternative, the JSR-223 scripting interface can be used, and should always provide a separate Globals instance 
per script engine instance by using a ThreadLocal internally.  

<h2>Sandboxing</h2>
Lua and luaj are allow for easy sandboxing of scripts in a server environment.
<P>
Considerations include
<ul>
<li>The <em>debug</em> and <em>luajava</em> library give unfettered access to the luaj vm and java vm so can be abused
<li>Portions of the <em>os</em>, <em>io</em>, and <em>coroutine</em> libraries are prone to abuse 
<li>Rogue scripts may need to be throttled or killed
<li>Shared metatables (string, booleans, etc.) need to be made read-only or isolated via class loaders
such as <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/server/LuajClassLoader.html">LuajClassLoader</a>
</ul>

Luaj provides sample code covering various approaches:
<ul>
<li><a href="examples/jse/SampleSandboxed.java">examples/jse/SampleSandboxed.java</a>
A java sandbox that limits libraries, limits bytecodes per script, and makes shared tables read-only
<li><a href="examples/lua/samplesandboxed.lua">examples/jse/samplesandboxed.lua</a>
A lua sandbox that limits librares,limits bytecodes per script, and makes shared tables read-only
<li><a href="examples/jse/SampleUsingClassLoader.java">examples/jse/SampleUsingClassLoader.java</a>
A heavier but strong sandbox where each script gets its own class loader and a full private luaj implementation
</ul>

<h1>4 - <a name="4">Libraries</a></h1>

<h2>Standard Libraries</h2>

Libraries are coded to closely match the behavior specified in 
See <a href="http://www.lua.org/manual/5.1/">standard lua documentation</a> for details on the library API's

<p>
The following libraries are loaded by both <em>JsePlatform.standardGlobals()</em> and <em>JmePlatform.standardGlobals()</em>:
<pre>	base
	bit32
	coroutine
	io
	math
	os
	package
	string
	table
</pre>

<p>
The <em>JsePlatform.standardGlobals()</em> globals also include:
<pre>	luajava 
</pre>

<p>
The <em>JsePlatform.debugGlobals()</em> and <em>JsePlatform.debugGlobals()</em> functions produce globals that include:
<pre>	debug
</pre>

<h3>I/O Library</h3>
The implementation of the <em>io</em> library differs by platform owing to platform limitations.

<p>
The <em>JmePlatform.standardGlobals()</em> instantiated the io library <em>io</em> in 
<pre>
	src/jme/org/luaj/vm2/lib/jme/JmeIoLib.java
</pre>

The <em>JsePlatform.standardGlobals()</em> includes support for random access and is in 
<pre>
	src/jse/org/luaj/vm2/lib/jse/JseIoLib.java
</pre>

<h3>OS Library</h3>
The implementation of the <em>os</em> library also differs per platform.

<p>
The basic <em>os</em> library implementation us used by <em>JmePlatform</em> and is in:
<pre>
	src/core/org/luaj/lib/OsLib.java
</pre>

A richer version for use by <em>JsePlatform</em> is : 
<pre>
	src/jse/org/luaj/vm2/lib/jse/JseOsLib.java
</pre>

Time is a represented as number of seconds since the epoch, 
and locales are not implemented.

<h3>Coroutine Library</h3>
The <em>coroutine</em> library is implemented using one JavaThread per coroutine.   
This allows <em>coroutine.yield()</em> can be called from anywhere, 
as with the yield-from-anywhere patch in C-based lua. 

<p>
Luaj uses WeakReferences and the OrphanedThread error to ensure that coroutines that are no longer referenced 
are properly garbage collected.  For thread safety, OrphanedThread should not be caught by Java code.  
See <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/LuaThread.html">LuaThread</a>
and <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/OrphanedThread.html">OrphanedThread</a>
javadoc for details.  The sample code in <a href="examples/jse/CollectingOrphanedCoroutines.java">examples/jse/CollectingOrphanedCoroutines.java</a>
provides working examples.

<h3>Debug Library</h3>
The <em>debug</em> library is not included by default by 
<em>JmePlatform.standardGlobals()</em> or <em>JsePlatform.standardGlobsls()</em> .

The functions <em>JmePlatform.debugGlobals()</em> and <em>JsePlatform.debugGlobsls()</em> 
create globals that contain the debug library in addition to the other standard libraries. 

To install dynamically from lua use java-class-based require:</em>:
<pre>
	require 'org.luaj.vm2.lib.DebugLib'
</pre>

The <em>lua</em> command line utility includes the <em>debug</em> library by default.


<h3><a name="luajava">The Luajava Library</a></h3>
The <em>JsePlatform.standardGlobals()</em> includes the <em>luajava</em> library, which simplifies binding to Java classes and methods.  
It is patterned after the original <a href="http://www.keplerproject.org/luajava/">luajava project</a>.

<p>
The following lua script will open a swing frame on Java SE:
<pre>
	jframe = luajava.bindClass( "javax.swing.JFrame" )
	frame = luajava.newInstance( "javax.swing.JFrame", "Texts" );
	frame:setDefaultCloseOperation(jframe.EXIT_ON_CLOSE)
	frame:setSize(300,400)
	frame:setVisible(true)
</pre>

<p>
See a longer sample in <em>examples/lua/swingapp.lua</em> for details, including a simple animation loop, rendering graphics, mouse and key handling, and image loading. 
Or try running it using: 
<pre>
	java -cp lib/luaj-jse-3.0.2.jar lua examples/lua/swingapp.lua
</pre>

<p>
The Java ME platform does not include this library, and it cannot be made to work because of the lack of a reflection API in Java ME. 

<p>
The <em>lua</em> connand line tool includes <em>luajava</em>. 

<h1>5 - <a name="5">LuaJ API</a></h1>

<h2>API Javadoc</h2>
The javadoc for the main classes in the LuaJ API are on line at
<pre>
	 <a href="http://luaj.org/luaj/3.0/api/index.html">http://luaj.org/luaj/3.0/api</a>
</pre>

You can also build a local version from sources using 
<pre>
	 ant doc
</pre>

<h2>LuaValue and Varargs</h2>
All lua value manipulation is now organized around 
<a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/LuaValue.html">LuaValue</a>
which exposes the majority of interfaces used for lua computation.  
<pre>
	 <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/LuaValue.html">org.luaj.vm2.LuaValue</a>
</pre>

<h3>Common Functions</h3>
<em>LuaValue</em> exposes functions for each of the operations in LuaJ.  
Some commonly used functions and constants include:
<pre>
	call();               // invoke the function with no arguments
	call(LuaValue arg1);  // call the function with 1 argument
	invoke(Varargs arg);  // call the function with variable arguments, variable return values
	get(int index);       // get a table entry using an integer key
	get(LuaValue key);    // get a table entry using an arbitrary key, may be a LuaInteger
	rawget(int index);    // raw get without metatable calls
	valueOf(int i);       // return LuaValue corresponding to an integer
	valueOf(String s);    // return LuaValue corresponding to a String
	toint();              // return value as a Java int
	tojstring();          // return value as a Java String
	isnil();              // is the value nil
	NIL;                  // the value nil
	NONE;                 // a Varargs instance with no values	 
</pre>

<h2>Varargs</h2>
The interface <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/Varargs.html">Varargs</a> provides an abstraction for 
both a variable argument list and multiple return values.  
For convenience, <em>LuaValue</em> implements <em>Varargs</em> so a single value can be supplied anywhere 
variable arguments are expected.      
<pre>
	 <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/Varargs.html">org.luaj.vm2.Varargs</a>
</pre>

<h3>Common Functions</h3>
<em>Varargs</em> exposes functions for accessing elements, and coercing them to specific types:
<pre>
	narg();                 // return number of arguments
	arg1();                 // return the first argument
	arg(int n);             // return the nth argument
	isnil(int n);           // true if the nth argument is nil
	checktable(int n);      // return table or throw error
	optlong(int n,long d);  // return n if a long, d if no argument, or error if not a long
</pre>
 
See the <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/Varargs.html">Varargs</a> API for a complete list.
 
<h2>LibFunction</h2>
The simplest way to implement a function is to choose a base class based on the number of arguments to the function.
LuaJ provides 5 base classes for this purpose, depending if the function has 0, 1, 2, 3 or variable arguments, 
and if it provide multiple return values.   
<pre>
	 <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/lib/ZeroArgFunction.html">org.luaj.vm2.lib.ZeroArgFunction</a>
	 <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/lib/OneArgFunction.html">org.luaj.vm2.lib.OneArgFunction</a>
	 <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/lib/TwoArgFunction.html">org.luaj.vm2.lib.TwoArgFunction</a>
	 <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/lib/ThreeArgFunction.html">org.luaj.vm2.lib.ThreeArgFunction</a>
	 <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/lib/VarArgFunction.html">org.luaj.vm2.lib.VarArgFunction</a>
</pre>

Each of these functions has an abstract method that must be implemented, 
and argument fixup is done automatically by the classes as each Java function is invoked.

<p>
An example of a function with no arguments but a useful return value might be:
<pre>
	pubic class hostname extends ZeroArgFunction {
		public LuaValue call() {
			return valueOf(java.net.InetAddress.getLocalHost().getHostName());
		}
	}
</pre>

The value <em>env</em> is the environment of the function, and is normally supplied 
by the instantiating object whenever default loading is used. 

<p>
Calling this function from lua could be done by:
<pre> 
	local hostname = require( 'hostname' )
</pre>

while calling this function from Java would look like:
<pre> 
	new hostname().call();
</pre>

Note that in both the lua and Java case, extra arguments will be ignored, and the function will be called.  
Also, no virtual machine instance is necessary to call the function. 
To allow for arguments, or return multiple values, extend one of the other base classes. 

<h2>Libraries of Java Functions</h2>
When require() is called, it will first attempt to load the module as a Java class that implements LuaFunction.  
To succeed, the following requirements must be met:
<ul>
<li>The class must be on the class path with name, <em>modname</em>.</li>
<li>The class must have a public default constructor.</li>
<li>The class must inherit from LuaFunction.</li>
</ul>

<p>
If luaj can find a class that meets these critera, it will instantiate it, cast it to <em>LuaFunction</em> 
then call() the instance with two arguments: 
the <em>modname</em> used in the call to require(), and the environment for that function.  
The Java may use these values however it wishes. A typical case is to create named functions 
in the environment that can be called from lua. 

<p>
A complete example of Java code for a simple toy library is in <a href="examples/jse/hyperbolic.java">examples/jse/hyperbolic.java</a> 
<pre>
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;

public class hyperbolic extends TwoArgFunction {

	public hyperbolic() {}

	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue library = tableOf();
		library.set( "sinh", new sinh() );
		library.set( "cosh", new cosh() );
		env.set( "hyperbolic", library );
		return library;
	}

	static class sinh extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(Math.sinh(x.checkdouble()));
		}
	}
	
	static class cosh extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(Math.cosh(x.checkdouble()));
		}
	}
}
</pre>

In this case the call to require invokes the library itself to initialize it.  The library implementation 
puts entries into a table, and stores this table in the environment.

<p>
The lua script used to load and test it is in <a href="examples/lua/hyperbolicapp.lua">examples/lua/hyperbolicapp.lua</a>
<pre>
	require 'hyperbolic'

	print('hyperbolic', hyperbolic)
	print('hyperbolic.sinh', hyperbolic.sinh)
	print('hyperbolic.cosh', hyperbolic.cosh)

	print('sinh(0.5)', hyperbolic.sinh(0.5))
	print('cosh(0.5)', hyperbolic.cosh(0.5))
</pre>

For this example to work the code in <em>hyperbolic.java</em> must be compiled and put on the class path.
 
<h2>Closures</h2>
Closures still exist in this framework, but are optional, and are only used to implement lua bytecode execution, 
and is generally not directly manipulated by the user of luaj.
<p>
See the <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/LuaClosure.html">org.luaj.vm2.LuaClosure</a> 
javadoc for details on using that class directly. 

<h1>6 - <a name="6">Parser</a></h1>

<h2>Javacc Grammar</h2>
A Javacc grammar was developed to simplify the creation of Java-based parsers for the lua language.
The grammar is specified for <a href="https://javacc.dev.java.net/">javacc version 5.0</a> because that tool generates standalone 
parsers that do not require a separate runtime.     

<p>
A plain undecorated grammer that can be used for validation is available in 
<a href="http://luaj.org/luaj/3.0/grammar/Lua52.jj">grammar/Lua52.jj</a>
while a grammar that generates a typed parse tree is in 
<a href="http://luaj.org/luaj/3.0/grammar/LuaParser.jj">grammar/LuaParser.jj</a>

<h2>Creating a Parse Tree from Lua Source</h2>
The default lu compiler does a single-pass compile of lua source to lua bytecode, so no explicit parse tree is produced.  

<p>
To simplify the creation of abstract syntax trees from lua sources, the LuaParser class is generated as part of the JME build.  
To use it, provide an input stream, and invoke the root generator, which will return a Chunk if the file is valid, 
or throw a ParseException if there is a syntax error.

<p>
For example, to parse a file and print all variable names, use code like:
<pre>
	try {
		String file = "main.lua";
		LuaParser parser = new LuaParser(new FileInputStream(file));
		Chunk chunk = parser.Chunk();
		chunk.accept( new Visitor() {
			public void visit(Exp.NameExp exp) {
				System.out.println("Name in use: "+exp.name.name
					+" line "+exp.beginLine
					+" col "+exp.beginColumn);
			}
		} );
	} catch ( ParseException e ) {
		System.out.println("parse failed: " + e.getMessage() + "\n"
			+ "Token Image: '" + e.currentToken.image + "'\n"
			+ "Location: " + e.currentToken.beginLine + ":" + e.currentToken.beginColumn 
			         + "-" + e.currentToken.endLine + "," + e.currentToken.endColumn);
	}
</pre> 

An example that prints locations of all function definitions in a file may be found in
<pre>
	<a href="examples/jse/SampleParser.java">examples/jse/SampleParser.java</a>
</pre>

<p>
See the <a href="http://luaj.org/luaj/3.0/api/org/luaj/vm2/ast/package-summary.html">org.luaj.vm2.ast package</a> javadoc for the API relating to the syntax tree that is produced. 

<h1>7 - <a name="7">Building and Testing</a></h1>

<h2><a name="maven">Maven integration</a></h2>
The main jar files are now deployed in the maven central repository.  To use them in your maven-based project, list them as a dependency:

<p>
For JSE projects, add this dependency for the luaj-jse jar:
<pre>
   &lt;dependency&gt;
      &lt;groupId&gt;org.luaj&lt;/groupId&gt;
      &lt;artifactId&gt;luaj-jse&lt;/artifactId&gt;
      &lt;version&gt;3.0.2&lt;/version&gt;
   &lt;/dependency&gt;	
</pre>
while for JME projects, use the luaj-jme jar:
<pre>
   &lt;dependency&gt;
      &lt;groupId&gt;org.luaj&lt;/groupId&gt;
      &lt;artifactId&gt;luaj-jme&lt;/artifactId&gt;
      &lt;version&gt;3.0.2&lt;/version&gt;
   &lt;/dependency&gt;	
</pre>

An example skelton maven pom file for a skeleton project is in 
<pre>
	<a href="examples/maven/pom.xml">examples/maven/pom.xml</a>
</pre>


<h2>Building the jars</h2>
An ant file is included in the root directory which builds the libraries by default.

<p>
Other targets exist for creating distribution file an measuring code coverage of unit tests.

<h2>Unit tests</h2>

<p>
The main luaj JUnit tests are organized into a JUnit 3 suite:
<pre>
	test/junit/org/luaj/vm2/AllTests.lua
</pre>

<p>
Unit test scripts can be found in these locations
<pre>
	test/lua/*.lua
	test/lua/errors/*.lua
	test/lua/perf/*.lua
	test/lua/luaj3.0.2-tests.zip
</pre>

<h2>Code coverage</h2>

<p>
A build script for running unit tests and producing code coverage statistics is in 
<pre>
	<a href="build-coverage.xml">build-coverage.xml</a>
</pre>

It relies on the cobertura code coverage library.

<h1>8 - <a name="8">Downloads</a></h1>

<h2>Downloads and Project Pages</h2>
Downloads for all version available on SourceForge or LuaForge.  
Sources are hosted on SourceForge and available via sourceforge.net    
<br/>
<pre>
	<a href="http://luaj.sourceforge.net/">SourceForge Luaj Project Page</a>
	<a href="http://sourceforge.net/project/platformdownload.php?group_id=197627">SourceForge Luaj Download Area</a>
</pre>
<p/>
The jar files may also be downloaded from the maven central repository, see <a href="#maven">Maven Integration</a>.
<p/>
Files are no longer hosted at LuaForge.

<h1>9 - <a name="9">Release Notes</a></h1>

<h2>Main Changes by Version</h2>
<table cellspacing="10"><tr><td><table cellspacing="4">
<tr valign="top"><td>&nbsp;&nbsp;<b>2.0</b></td><td><ul>
<li>Initial release of 2.0 version </li>  
</ul></td></tr>

<tr valign="top"><td>&nbsp;&nbsp;<b>2.0.1</b></td><td><ul>
<li>Improve correctness of singleton construction related to static initialization </li>
<li>Fix nan-related error in constant folding logic that was failing on some JVMs </li>
<li>JSR-223 fixes: add META-INF/services entry in jse jar, improve bindings implementation </li>
</ul></td></tr>

<tr valign="top"><td>&nbsp;&nbsp;<b>2.0.2</b></td><td><ul>
<li>JSR-223 bindings change: non Java-primitives will now be passed as LuaValue </li> 
<li>JSR-223 enhancement: allow both ".lua" and "lua" as extensions in getScriptEngine() </li>
<li>JSR-223 fix: use system class loader to support using luaj as JRE extension </li>
<li>Improve selection logic when binding to overloaded functions using luajava</li>
<li>Enhance javadoc, put it <a href="docs/api/index.html">in distribution</a> 
and at <a href="http://luaj.sourceforge.net/api/2.0/index.html">http://luaj.sourceforge.net/api/2.0/</a></li>
<li>Major refactor of luajava type coercion logic, improve method selection.</li> 
<li>Add lib/luaj-sources-2.0.2.jar for easier integration into an IDE such as Netbeans </li>

<tr valign="top"><td>&nbsp;&nbsp;<b>2.0.3</b></td><td><ul>
<li>Improve coroutine state logic including let unreferenced coroutines be garbage collected </li>
<li>Fix lua command vararg values passed into main script to match what is in global arg table </li>
<li>Add arithmetic metatag processing when left hand side is a number and right hand side has metatable </li>
<li>Fix load(func) when mutiple string fragments are supplied by calls to func </li>
<li>Allow access to public members of private inner classes where possible </li>
<li>Turn on error reporting in LuaParser so line numbers ar available in ParseException </li>
<li>Improve compatibility of table.remove() </li>
<li>Disallow base library setfenv() calls on Java functions </li>

<tr valign="top"><td>&nbsp;&nbsp;<b>3.0</b></td><td><ul>
<li>Convert internal and external API's to match lua 5.2.x environment changes </li>
<li>Add bit32 library </li>
<li>Add explicit Globals object to manage global state, especially to imrpove thread safety </li>
<li>Drop support for lua source to java surce (lua2java) in favor of direct java bytecode output (luajc) </li>
<li>Remove compatibility functions like table.getn(), table.maxn(), table.foreach(), and math.log10() </li>
<li>Add ability to create runnable jar file from lua script with sample build file build-app.xml </li>
<li>Supply environment as second argument to LibFunction when loading via require() </li>
<li>Fix bug 3597515 memory leak due to string caching by simplifying caching logic.</li>
<li>Fix bug 3565008 so that short substrings are backed by short arrays.</li>
<li>Fix bug 3495802 to return correct offset of substrings from string.find().</li>
<li>Add artifacts to Maven central repository.</li>
<li>Limit pluggable scripting to use compatible bindings and contexts, implement redirection.</li>
<li>Fix bug that didn't read package.path from environment.</li>
<li>Fix pluggable scripting engine lookup, simplify implementation, and add unit tests.</li>
<li>Coerce script engine eval() return values to Java.</li>
<li>Fix Lua to Java coercion directly on Java classes.</li>
<li>Fix Globals.load() to call the library with an empty modname and the globals as the environment.</li>
<li>Fix hash codes of double.</li>
<li>Fix bug in luajava overload resolution.</li>
<li>Fix luastring bug where parsing did not check for overflow.</li>
<li>Fix luastring bug where circular dependency randomly caused NullPointerException.</li>
<li>Major refactor of table implementation.</li>
<li>Improved behavior of next() (fixes issue #7).</li>
<li>Existing tables can now be made weak (fixes issue #16).</li>
<li>More compatible allocation of table entries in array vs. hash (fixes issue #8).</li>
<li>Fix os.time() to return a number of seconds instead of milliseconds.</li>
<li>Implement formatting with os.date(), and table argument for os.time().</li>
<li>LuaValue.checkfunction() now returns LuaFunction.</li>
<li>Refactor APIs related to compiling and loading scripts to provide methods on Globals.</li>
<li>Add API to compile from Readers as well as InputStreams.</li>
<li>Add optional -c encoding flag to lua, luac, and luajc tools to control source encoding.</li>
<li>Let errors thrown in debug hooks bubble up to the running coroutine.</li>
<li>Make error message handler function in xpcall per-thread instead of per-globals.</li>
<li>Establish "org.luaj.debug" and "org.luaj.luajc" system properties to configure scripting engine.</li>
<li>Add sample code for Android Application that uses luaj.</li>
<li>Add sample code for Applet that uses luaj.</li>
<li>Fix balanced match for empty string (fixes issue #23).</li>
<li>Pass user-supplied ScriptContext to script engine evaluation (fixes issue #21).</li>
<li>Autoflush and encode written bytes in script contexts (fixes issue #20).</li>
<li>Rename Globals.FINDER to Globals.finder.</li>
<li>Fix bug in Globals.UTF8Stream affecting loading from Readers (fixes issue #24).</li>
<li>Add buffered input for compiling and loading of scripts.</li>
<li>In CoerceJavaToLua.coerse(), coerce byte[] to LuaString (fixes issue #31).</li>
<li>In CoerceJavaToLua.coerse(), coerce LuaValue to same value (fixes issue #29).</li>
<li>Fix line number reporting in debug stack traces (fixes issue #30).</li>

<tr valign="top"><td>&nbsp;&nbsp;<b>3.0.1</b></td><td><ul>
<li>Fix __len metatag processing for tables.</li>
<li>Add fallback to __lt when pocessing __le metatag.</li>
<li>Convert anonymous classes to inner classes (gradle build support).</li>
<li>Allow error() function to pass any lua object including non-strings.</li>
<li>Fix string backing ownership issue when compiling many scripts.</li>
<li>Make LuaC compile state explicit and improve factoring.</li>
<li>Add sample build.gradle file for Android example.</li>
<li>collectgarbage() now behaves same as collectgarbage("collect") (fixes issue #41).</li>
<li>Allow access to Java inner classes using lua field syntax (fixes issue #40).</li>
<li>List keyeq() and keyindex() methods as abstract on LuaTable.Entry (issue #37).</li>
<li>Fix return value for table.remove() and table.insert() (fixes issue #39)</li>
<li>Fix aliasing issue for some multiple assignments from varargs return values (fixes issue #38)</li>
<li>Let os.getenv() return System.getenv() values first for JSE, then fall back to properties (fixes issue #25)</li>
<li>Improve garbage collection of orphaned coroutines when yielding from debug hook functions (fixes issue #32).</li>
<li>LuaScriptEngineFactory.getScriptEngine() now returns new instance of LuaScriptEngine for each call.</li>
<li>Fix os.date("*t") to return hour in 24 hour format (fixes issue #45)</li>
<li>Add SampleSandboxed.java example code to illustrate sandboxing techniques in Java.</li>
<li>Add samplesandboxed.lua example code to illustrate sandboxing techniques in lua.</li>
<li>Add CollectingOrphanedCoroutines.java example code to show how to deal with orphaned lua threads.</li>
<li>Add LuajClassLoader.java and Launcher.java to simplify loading via custom class loader.</li>
<li>Add SampleUsingClassLoader.java example code to demonstrate loading using custom class loader.</li>
<li>Make shared string metatable an actual metatable.</li>
<li>Add sample code that illustrates techniques in creating sandboxed environments.</li>
<li>Add convenience methods to Global to load string scripts with custom environment.</li>
<li>Move online docs to <a href="http://luaj.org/luaj/3.0/api/index.html">http://luaj.org/luaj/3.0/api/</a></li>
<li>Fix os.time() conversions for pm times.</li>

<tr valign="top"><td>&nbsp;&nbsp;<b>3.0.2</b></td><td><ul>
<li>Fix JsePlatform.luaMain() to provide an "arg" table in the chunk's environment.</li>
<li>Let JsePlatform.luaMain() return values returned by the main chunk.</li>
<li>Add synchronization to CoerceJavaToLua.COERCIONS map.</li>

</ul></td></tr>
</table></td></tr></table>

<h2>Known Issues</h2>
<h3>Limitations</h3>
<ul>
<li>debug code may not be completely removed by some obfuscators
<li>tail calls are not tracked in debug information
<li>mixing different versions of luaj in the same java vm is not supported
<li>values associated with weak keys may linger longer than expected
<li>behavior of luaj when a SecurityManager is used has not been fully characterized
<li>negative zero is treated as identical to integer value zero throughout luaj
<li>lua compiled into java bytecode using luajc cannot use string.dump() or xpcall()
<li>number formatting with string.format() is not supported
<li>shared metatables for string, bool, etc are shared across Globals instances in the same class loader
<li>orphaned threads will not be collected unless garbage collection is run and sufficient time elapses
</ul>
<h3>File Character Encoding</h3>
Source files can be considered encoded in UTF-8 or ISO-8859-1 and results should be as expected, 
with literal string contianing quoted characters compiling to the same byte sequences as the input. 

For a non ASCII-compatible encoding such as EBSDIC, however, there are restrictions:
<ul>
<li>supplying a Reader to Globals.load() is preferred over InputStream variants
<li>using FileReader or InputStreamReader to get the default OS encoding should work in most cases
<li>string literals with quoted characters may not produce the expected values in generated code
<li>command-line tools lua, luac, and luajc will require <em>-c Cp037</em> to specify the encoding
</ul>
These restrictions are mainly a side effect of how the language is defined as allowing byte literals
within literal strings in source files.

Code that is generated on the fly within lua and compiled with lua's <em>load()</em> function 
should work as expected, since these strings will never be represented with the 
host's native character encoding.
