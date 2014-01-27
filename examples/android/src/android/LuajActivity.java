package org.luaj.vm2.android;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class LuajActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LuajView view = new LuajView(this);
		setContentView(view);
		try {
			LuaValue activity = CoerceJavaToLua.coerce(this);
			LuaValue viewobj = CoerceJavaToLua.coerce(view);
			view.globals.loadfile("activity.lua").call(activity, viewobj);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.luaj, menu);
		return true;
	}

}
