package org.luaj.vm2.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.luaj.vm2.LuaValue;

public class NameScope {

	private static final Set<String> LUA_KEYWORDS = new HashSet<String>();
	
	static { 
		String[] k = new String[] { 
	        "and", "break", "do", "else", "elseif", "end", 
	        "false", "for", "function", "if", "in", "local",
	        "nil", "not", "or", "repeat", "return", 
	        "then", "true", "until", "while" };
		for ( int i=0; i<k.length; i++ )
			LUA_KEYWORDS.add( k[i] );
	}
	
	public final Map<String,NamedVariable> namedVariables = new HashMap<String,NamedVariable>();

	public final NameScope outerScope;

	public int functionNestingCount;
	
	/** Construct default names scope */
	public NameScope() {
		this.outerScope = null;
		this.functionNestingCount = 0;
	}
	
	/** Construct name scope within another scope*/
	public NameScope(NameScope outerScope) {
		this.outerScope = outerScope;
		this.functionNestingCount = outerScope!=null? outerScope.functionNestingCount: 0;
	}
	
	/** Look up a name.  If it is a global name, then throw IllegalArgumentException. */
	public NamedVariable find( String name ) throws IllegalArgumentException {
		validateIsNotKeyword(name);
		for ( NameScope n = this; n!=null; n=n.outerScope )
			if ( n.namedVariables.containsKey(name) )
				return n.namedVariables.get(name);
		NamedVariable value = new NamedVariable(name);
		this.namedVariables.put(name, value);
		return value;
	}
	
	/** Define a name in this scope.  If it is a global name, then throw IllegalArgumentException. */
	public NamedVariable define( String name ) throws IllegalStateException, IllegalArgumentException {
		validateIsNotKeyword(name);
		NamedVariable value = new NamedVariable(name, this);
		this.namedVariables.put(name, value);
		return value;
	}
	
	private void validateIsNotKeyword(String name) {
		if ( LUA_KEYWORDS.contains(name) )
			throw new IllegalArgumentException("name is a keyword: '"+name+"'");
	}

	/** Named variable is load, global, or upvalue.  */
	public static class NamedVariable {
		public final String name;
		public final NameScope definingScope;
		public boolean isupvalue;
		public boolean hasassignments;
		public LuaValue initialValue;
		/** Global is named variable not associated with a defining scope */
		public NamedVariable(String name) {
			this.name = name;
			this.definingScope = null;
		}
		public NamedVariable(String name, NameScope definingScope) {
		/** Local variable is defined in a particular scope.  */
			this.name = name;
			this.definingScope = definingScope;
		}
		public boolean isLocal() {
			return this.definingScope != null;
		}
		public boolean isConstant() {
			return ! hasassignments && initialValue != null;
		}
	}
}
