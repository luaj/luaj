/*******************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2;

import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;

/** 
 * Test compilation of various fragments that have
 * caused problems for jit compiling during development. 
 *
 */
public class FragmentsTest extends TestSuite {

	static final int TEST_TYPE_LUAC     = 0;
	static final int TEST_TYPE_LUAJC    = 1;

	public static class JseFragmentsTest extends FragmentsTestCase {
		public JseFragmentsTest() { super(  TEST_TYPE_LUAC ); }
	}
	public static class LuaJCFragmentsTest extends FragmentsTestCase {
		public LuaJCFragmentsTest() { super(  TEST_TYPE_LUAJC ); }
	}
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Compiler Fragments Tests");
		suite.addTest( new TestSuite( JseFragmentsTest.class,      "JSE Fragments Tests" ) );
		suite.addTest( new TestSuite( LuaJCFragmentsTest.class,    "LuaJC Fragments Tests" ) );
		return suite;
	}
	
	abstract protected static class  FragmentsTestCase extends TestCase {
	
		final int TEST_TYPE;

		protected FragmentsTestCase(int testType) {
			this.TEST_TYPE = testType;
		}
	
		public void runFragment( Varargs expected, String script ) {
			try {
				String name = getName();
				Globals globals = JsePlatform.debugGlobals();
				Reader reader = new StringReader(script);
				LuaValue chunk ;
				switch ( TEST_TYPE ) {
				case TEST_TYPE_LUAJC:
					LuaJC.install(globals);
					chunk = globals.load(reader, name);
					break;
				default:
					Prototype p = globals.compilePrototype(reader, name);
					chunk = new LuaClosure(p, globals);
					Print.print(p);
					break;
				}
				Varargs actual = chunk.invoke();
				assertEquals( expected.narg(), actual.narg() );
				for ( int i=1; i<=actual.narg(); i++ )
					assertEquals( expected.arg(i), actual.arg(i) );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.toString());
			}		
		}
		
		public void testFirstArgNilExtended() {
			runFragment( LuaValue.NIL, 
				"function f1(a) print( 'f1:', a ) return a end\n" +
				"b = f1()\n" + 
				"return b" );
		}

		public void testSimpleForloop() {
			runFragment( LuaValue.valueOf(77),
				"for n,p in ipairs({77}) do\n"+
				"	print('n,p',n,p)\n"+
				"   return p\n"+
				"end\n");
			
		}
			
		public void testForloopParamUpvalues() {
			runFragment( LuaValue.varargsOf(new LuaValue[] { 
					LuaValue.valueOf(77), 
					LuaValue.valueOf(1) } ),
				"for n,p in ipairs({77}) do\n"+
				"	print('n,p',n,p)\n"+
				"	foo = function()\n"+
				"		return p,n\n"+
				"	end\n"+
				"	return foo()\n"+
				"end\n");
			
		}
			
		public void testArgVarargsUseBoth() {
			runFragment( LuaValue.varargsOf( new LuaValue[] { 
					LuaValue.valueOf("a"), 
					LuaValue.valueOf("b"), 
					LuaValue.valueOf("c")}),
				"function v(arg,...)\n" +
				"	return arg,...\n" +
				"end\n" +
				"return v('a','b','c')\n" );
		}
		
		public void testArgParamUseNone() {
			runFragment( LuaValue.valueOf("string"), 
				"function v(arg,...)\n" +
				"	return type(arg)\n" +
				"end\n" +
				"return v('abc')\n" );		
		}

		public void testSetlistVarargs() {
			runFragment( LuaValue.valueOf("abc"),
				"local f = function() return 'abc' end\n" +
				"local g = { f() }\n" +
				"return g[1]\n" );
		}
		
		public void testSelfOp() {
			runFragment( LuaValue.valueOf("bcd"), 
				"local s = 'abcde'\n"+
				"return s:sub(2,4)\n" );		
		}
		
		public void testSetListWithOffsetAndVarargs() {
			runFragment( LuaValue.valueOf(1003), 
				"local bar = {1000, math.sqrt(9)}\n"+
				"return bar[1]+bar[2]\n" );
		}
		
		public void testMultiAssign() {
			// arargs evaluations are all done before assignments 
			runFragment( LuaValue.varargsOf(new LuaValue[]{
					LuaValue.valueOf(111),
					LuaValue.valueOf(111),
					LuaValue.valueOf(111)}), 
				"a,b,c = 1,10,100\n" +
				"a,b,c = a+b+c, a+b+c, a+b+c\n" +
				"return a,b,c\n" );		
		}
	
		public void testUpvalues() {
			runFragment( LuaValue.valueOf(999), 
				"local a = function(x)\n" +
				"  return function(y)\n" +
				"    return x + y\n" +
				"  end\n" +
				"end\n" +
				"local b = a(222)\n" +
				"local c = b(777)\n" +
				"print( 'c=', c )\n" +
				"return c\n" );		
		}
				
		public void testNonAsciiStringLiterals() {
			runFragment( LuaValue.valueOf("7,8,12,10,9,11,133,222"), 
				"local a='\\a\\b\\f\\n\\t\\v\\133\\222'\n"+
				"local t={string.byte(a,1,#a)}\n"+
				"return table.concat(t,',')\n" );
		}
	
		public void testControlCharStringLiterals() {
			runFragment( LuaValue.valueOf("97,0,98,18,99,18,100,18,48,101"), 
				"local a='a\\0b\\18c\\018d\\0180e'\n"+
				"local t={string.byte(a,1,#a)}\n"+
				"return table.concat(t,',')\n" );		
		}
		
		public void testLoopVarNames() {
			runFragment( LuaValue.valueOf(" 234,1,aa 234,2,bb"), 
				"local w = ''\n"+
				"function t()\n"+
				"	for f,var in ipairs({'aa','bb'}) do\n"+
				"		local s = 234\n"+
				"		w = w..' '..s..','..f..','..var\n"+
				"	end\n"+
				"end\n" +
				"t()\n" +
				"return w\n" );
			
		}
		
		public void testForLoops() {
			runFragment( LuaValue.valueOf("12345 357 963"), 
				"local s,t,u = '','',''\n"+
				"for m=1,5 do\n"+
				"	s = s..m\n"+
				"end\n"+
				"for m=3,7,2 do\n"+
				"	t = t..m\n"+
				"end\n"+
				"for m=9,3,-3 do\n"+
				"	u = u..m\n"+
				"end\n"+
				"return s..' '..t..' '..u\n" );
		}
		
		public void testLocalFunctionDeclarations() {
			runFragment( LuaValue.varargsOf(LuaValue.valueOf("function"),LuaValue.valueOf("nil")), 
				"local function aaa()\n"+
				"	return type(aaa)\n"+
				"end\n"+
				"local bbb = function()\n"+
				"	return type(bbb)\n"+
				"end\n"+
				"return aaa(),bbb()\n" );
		}
		
		public void testNilsInTableConstructor() {
			runFragment( LuaValue.valueOf("1=111 2=222 3=333 "), 
				"local t = { 111, 222, 333, nil, nil }\n"+
				"local s = ''\n"+
				"for i,v in ipairs(t) do \n" +
				"	s=s..tostring(i)..'='..tostring(v)..' '\n" +
				"end\n"+
				"return s\n" );
			
		}
		
		public void testUnreachableCode() {
			runFragment( LuaValue.valueOf(66), 
				"local function foo(x) return x * 2 end\n" + 
				"local function bar(x, y)\n" + 
				"	if x==y then\n" + 
				"		return y\n" + 
				"	else\n" + 
				"		return foo(x)\n" + 
				"	end\n" + 
				"end\n" + 
				"return bar(33,44)\n" ); 
			
		}
		public void testVarargsWithParameters() {
			runFragment( LuaValue.valueOf(222), 
				"local func = function(t,...)\n"+ 
				"	return (...)\n"+
				"end\n"+
				"return func(111,222,333)\n" );
		}
		
		public void testNoReturnValuesPlainCall() {
			runFragment( LuaValue.TRUE, 
				"local testtable = {}\n"+
				"return pcall( function() testtable[1]=2 end )\n" );
		}
			
		public void testVarargsInTableConstructor() {
			runFragment( LuaValue.valueOf(222), 
				"local function foo() return 111,222,333 end\n"+
				"local t = {'a','b',c='c',foo()}\n"+
				"return t[4]\n" );
		}
	
		public void testVarargsInFirstArg() {
			runFragment( LuaValue.valueOf(123), 
					"function aaa(x) return x end\n" +
					"function bbb(y) return y end\n" +
					"function ccc(z) return z end\n" +
					"return ccc( aaa(bbb(123)), aaa(456) )\n" );
		}
		
		public void testSetUpvalueTableInitializer() {
			runFragment( LuaValue.valueOf("b"), 
					"local aliases = {a='b'}\n" +
					"local foo = function()\n" +
					"	return aliases\n" +
					"end\n" +
					"return foo().a\n"  );
		}
		
		
		public void testLoadNilUpvalue() {
			runFragment( LuaValue.NIL, 
					"tostring = function() end\n" +
					"local pc \n" +
					"local pcall = function(...)\n" +
					"	pc(...)\n" +
					"end\n" +
					"return NIL\n" );
		}
		
		public void testUpvalueClosure() {
			runFragment( LuaValue.NIL, 
					"print()\n"+
					"local function f2() end\n"+
					"local function f3()\n"+
					"	return f3\n"+
					"end\n" +
					"return NIL\n" );
		}
	
		public void testUninitializedUpvalue() {
			runFragment( LuaValue.NIL, 
					"local f\n"+	
					"do\n"+
					"	function g()\n"+
					"		print(f())\n"+
					"	end\n"+
					"end\n" +
					"return NIL\n" );
		}
	
		public void testTestOpUpvalues() {
			runFragment( LuaValue.varargsOf(LuaValue.valueOf(1),LuaValue.valueOf(2),LuaValue.valueOf(3)), 
					"print( nil and 'T' or 'F' )\n"+
					"local a,b,c = 1,2,3\n"+
					"function foo()\n"+
					"	return a,b,c\n"+
					"end\n" +
					"return foo()\n" );				
		}
		public void testTestSimpleBinops() {
			runFragment( LuaValue.varargsOf(new LuaValue[] {
					LuaValue.FALSE, LuaValue.FALSE, LuaValue.TRUE, LuaValue.TRUE, LuaValue.FALSE }),
					"local a,b,c = 2,-2.5,0\n" +
					"return (a==c), (b==c), (a==a), (a>c), (b>0)\n" );
		}
		
		public void testNumericForUpvalues() {
			runFragment( LuaValue.valueOf(8), 
					"for i = 3,4 do\n"+
					"	i = i + 5\n"+
					"	local a = function()\n"+
					"		return i\n"+
					"	end\n" +
					"	return a()\n"+
					"end\n");
		}
		
		public void testNumericForUpvalues2() {
			runFragment( LuaValue.valueOf("222 222"), 
					"local t = {}\n"+
					"local template = [[123 456]]\n"+
					"for i = 1,2 do\n"+
					"	t[i] = template:gsub('%d', function(s)\n"+
					"		return i\n"+
					"	end)\n"+
					"end\n" +
					"return t[2]\n");
		}
		
		public void testReturnUpvalue() {
			runFragment( LuaValue.varargsOf(new LuaValue[] { LuaValue.ONE, LuaValue.valueOf(5), }),
					"local a = 1\n"+
					"local b\n"+
					"function c()\n"+
					"	b=5\n" +
					"	return a\n"+
					"end\n"+
					"return c(),b\n" );			
		}
		
		public void testUninitializedAroundBranch() {
			runFragment( LuaValue.valueOf(333), 
					"local state\n"+
					"if _G then\n"+
					"    state = 333\n"+
					"end\n"+
					"return state\n" );
		}
		
		public void testLoadedNilUpvalue() {
			runFragment( LuaValue.NIL, 
					"local a = print()\n"+   
					"local b = c and { d = e }\n"+
					"local f\n"+
					"local function g()\n"+
					"	return f\n"+
					"end\n" +
					"return g()\n" );
		}
		
		public void testUpvalueInFirstSlot() {
			runFragment( LuaValue.valueOf("foo"), 
					"local p = {'foo'}\n"+
					"bar = function()\n"+
					"	return p \n"+
					"end\n"+
					"for i,key in ipairs(p) do\n"+
					"	print()\n"+
					"end\n" +
					"return bar()[1]");
		}
		
		public void testReadOnlyAndReadWriteUpvalues() {
			runFragment( LuaValue.varargsOf( new LuaValue[] { LuaValue.valueOf(333), LuaValue.valueOf(222) } ),
					"local a = 111\n" +
					"local b = 222\n" +
					"local c = function()\n"+
					"	a = a + b\n" +
					"	return a,b\n"+
					"end\n" +
					"return c()\n" );
		}
		
		public void testNestedUpvalues() {
			runFragment( LuaValue.varargsOf( new LuaValue[] { LuaValue.valueOf(5), LuaValue.valueOf(8), LuaValue.valueOf(9) } ),
					"local x = 3\n"+
					"local y = 5\n"+	   
					"local function f()\n"+
					"   return y\n"+
					"end\n"+
					"local function g(x1, y1)\n"+
					"   x = x1\n"+
					"   y = y1\n" +
					"	return x,y\n"+
					"end\n"+
					"return f(), g(8,9)\n"+
					"\n" );
		}
		
		public void testLoadBool() {
			runFragment( LuaValue.NONE, 
					"print( type(foo)=='string' )\n"+
					"local a,b\n"+
					"if print() then\n"+
					"	b = function()\n"+
					"		return a\n"+
					"	end\n"+
					"end\n" );
		}
		
		public void testBasicForLoop() {
			runFragment( LuaValue.valueOf(2), 
					"local data\n"+
					"for i = 1, 2 do\n"+
					"     data = i\n"+
					"end\n"+
					"local bar = function()\n"+
					"	return data\n"+
					"end\n" +
					"return bar()\n" );
		}
		
		public void testGenericForMultipleValues() {
			runFragment( LuaValue.varargsOf(LuaValue.valueOf(3),LuaValue.valueOf(2),LuaValue.valueOf(1)), 
					"local iter = function() return 1,2,3,4 end\n" +
					"local foo  = function() return iter,5 end\n" +
			        "for a,b,c in foo() do\n" +
			        "    return c,b,a\n" +
			        "end\n" );
		}
		
		public void testPhiUpvalue() {
			runFragment( LuaValue.valueOf(6), 
					"local a = foo or 0\n"+
					"local function b(c)\n"+
					"	if c > a then a = c end\n" +
					"	return a\n"+
					"end\n" +
					"b(6)\n" +
					"return a\n" );
		}
		
		public void testAssignReferUpvalues() {
			runFragment( LuaValue.valueOf(123), 
					"local entity = 234\n" +
				    "local function c()\n" +
					"    return entity\n" +
					"end\n" +
				    "entity = (a == b) and 123\n" +
				    "if entity then\n" +
				    "    return entity\n" +
				    "end\n" );			
		}
		
		public void testSimpleRepeatUntil() {
			runFragment( LuaValue.valueOf(5), 
					"local a\n"+
					"local w\n"+
					"repeat\n"+
					"	a = w\n"+
					"until not a\n" +
					"return 5\n" );
		}
		
		public void testLoopVarUpvalues() {
			runFragment( LuaValue.valueOf("b"), 
					"local env = {}\n" +
					"for a,b in pairs(_G) do\n" +
					"	c = function()\n" +
					"		return b\n" +
					"	end\n" +
					"end\n" +
					"local e = env\n" +
					"local f = {a='b'}\n" +
					"for k,v in pairs(f) do\n" +
					"	return env[k] or v\n" +
					"end\n");
		}
		
		public void testPhiVarUpvalue() {
			runFragment( LuaValue.valueOf(2), 
					"local a = 1\n"+    
				    "local function b()\n"+
				    "    a = a + 1\n"+
				    "    return function() end\n"+
				    "end\n"+
				    "for i in b() do\n"+
					"	a = 3\n"+
				    "end\n" +
				    "return a\n");			
		}
		
		public void testUpvaluesInElseClauses() {
			runFragment( LuaValue.valueOf(111), 
					"if a then\n" +
			        "   foo(bar)\n" +
			        "elseif _G then\n" +
			        "    local x = 111\n" +
			        "    if d then\n" +
			        "        foo(bar)\n" +
			        "    else\n" +
			        "    	local y = function()\n" +
			        "    		return x\n" +
			        "        end\n" +
			        "    	return y()\n" +
			        "    end\n" +
			        "end\n");
		}
		
		public void testUpvalueInDoBlock() {
			runFragment( LuaValue.NONE, "do\n"+
					"	local x = 10\n"+
					"	function g()\n"+
					"		return x\n"+
					"	end\n"+
					"end\n"+
					"g()\n");
		}
		
		public void testNullError() {
			runFragment( LuaValue.varargsOf(LuaValue.FALSE, LuaValue.NIL),
					"return pcall(error)\n");
		}

		public void testFindWithOffset() {
			runFragment(LuaValue.varargsOf(LuaValue.valueOf(8), LuaValue.valueOf(5)),
					"string = \"abcdef:ghi\"\n" +
					"substring = string:sub(3)\n" +
					"idx = substring:find(\":\")\n" +
					"return #substring, idx\n");
		}

		public void testErrorArgIsString() {
			runFragment(LuaValue.varargsOf(LuaValue.valueOf("string"), LuaValue.valueOf("c")),
					"a,b = pcall(error, 'c'); return type(b), b\n");
		}
		public void testErrorArgIsNil() {
			runFragment(LuaValue.varargsOf(LuaValue.valueOf("nil"), LuaValue.NIL),
					"a,b = pcall(error); return type(b), b\n");
		}
		public void testErrorArgIsTable() {
			runFragment(LuaValue.varargsOf(LuaValue.valueOf("table"), LuaValue.valueOf("d")),
					"a,b = pcall(error, {c='d'}); return type(b), b.c\n");
		}
		public void testErrorArgIsNumber() {
			runFragment(LuaValue.varargsOf(LuaValue.valueOf("string"), LuaValue.valueOf("1")),
					"a,b = pcall(error, 1); return type(b), b\n");
		}
		public void testErrorArgIsBool() {
			runFragment(LuaValue.varargsOf(LuaValue.valueOf("boolean"), LuaValue.TRUE),
					"a,b = pcall(error, true); return type(b), b\n");
		}
		public void testBalancedMatchOnEmptyString() {
			runFragment(LuaValue.NIL, "return (\"\"):match(\"%b''\")\n");
		}
		public void testReturnValueForTableRemove() {
			runFragment(LuaValue.NONE, "return table.remove({ })");
		}
		public void testTypeOfTableRemoveReturnValue() {
			runFragment(LuaValue.valueOf("nil"), "local k = table.remove({ }) return type(k)");
		}
	}
}
