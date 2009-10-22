package org.luaj.sample;
import org.luaj.lib.j2se.CoerceJavaToLua;
import org.luaj.platform.*;
import org.luaj.vm.*;

/**
 * Program that illustrates how userdata is mapped into lua using 
 * LuaJava's automated coercion  
 */
public class SampleUserdataMain {

	public static class MyData {
		public int x = 7;
		public String y = "seven";
		public int xx[] = new int[] { 11, 22, 33, };
		public String yy[] = new String[] { "aa", "bb" };
		public int xxx[][] = new int[][] { {444, 555}, {666, 777} } ;
		public String yyy[][] = new String[][] { { "ccc", "ddd" }, { "eee", "fff" } };
		public void initScalars( int newx, String newy ) {
			x = newx;
			y = newy;
		}
		public void initArrays( int[] newxx, String[] newyy ) {
			xx = newxx;
			yy = newyy;
		}
		public void initMatrices( int[][] newxxx, String[][] newyyy ) {
			xxx = newxxx;
			yyy = newyyy;
		}
		public int getx() { return x; }
		public String gety() { return y; }
		public int[] getxx() { return xx; }
		public String[] getyy() { return yy; }
		public int[][] getxxx() { return xxx; }
		public String[][] getyyy() { return yyy; }
	}
	
	public static void main(String[] args) {
		Platform.setInstance( new J2sePlatform() );
		LuaState vm = Platform.newLuaState();
		org.luaj.compiler.LuaC.install();
		
		// test script
		vm.getglobal( "loadstring" );
		vm.pushstring( "local mydata = ...\n" +
				"print( 'mydata', mydata )\n" +
				"print( 'mydata.x, mydata.y', mydata.x, mydata.y )\n" +
				"print( 'mydata:getx()', mydata:getx() )\n" +
				"print( 'mydata:getxx()', mydata:getxx()[1], mydata:getxx()[2] )\n" +
				"print( 'mydata:getxxx()', mydata:getxxx()[1][1], mydata:getxxx()[1][2] )\n" +
				"print( 'mydata:getyyy()', mydata:getyyy()[1][1], mydata:getyyy()[1][2] )\n" +
				"mydata:initScalars(3,'pqr')\n" +
				"mydata:initArrays({55,66},{'abc','def'})\n" +
				"mydata:initMatrices({{44,55},{66}},{{'qq','rr'},{'ss','tt'}})\n" + 
				"print( 'mydata:getx()', mydata:getx() )\n" +
				"print( 'mydata:getxx()', mydata:getxx()[1], mydata:getxx()[2] )\n" +
				"print( 'mydata:getxxx()', mydata:getxxx()[1][1], mydata:getxxx()[1][2] )\n" +
				"print( 'mydata:getyyy()', mydata:getyyy()[1][1], mydata:getyyy()[1][2] )\n" +
				"");
		vm.call( 1, 2 );
		System.out.println("load result: "+vm.tostring(-2)+", "+vm.tostring(-1));
		vm.settop(1);
		
		// load argument to test script
		vm.pushlvalue( CoerceJavaToLua.coerce(new MyData()) );
		vm.call( 1, 0 );
	}
}
