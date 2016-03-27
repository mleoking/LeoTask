package org.leores.demo;

import org.leores.util.LogUtil;
import org.leores.util.U;

public class Demo extends LogUtil {

	public static void demo() {
	}

	/**
	 * Set the working directory to be ${workspace_loc:leotask/demo} in Eclipse.
	 * In other IDEs set the working directory to the "demo" folder.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		U.tLog("Set the working directory to be ${workspace_loc:leotask/demo} in Eclipse. In other IDEs set the working directory to the \"demo\" folder.");
		U.tLog("Some demos require Gnuplot installed and its gnuplot command folder included in the system environment variable PATH.");
		U.tLog("------------------");
		//AnaDemo.demo();
		//DataDemo.demo();
		//DelimitedReaderDemo.demo();
		//EpidemicDemo.demo();
		//FileUtilDemo.demo();
		JGnuplotDemo.demo();
		//MathDemo.demo();
		//ModDemo.demo();
		//NetDemo.demo();
		//ObjUtilDemo.demo();
		//ProcessableDemo.demo();
		//RandomUtilDemo.demo();
		//TaskDemo.demo();
		//UtilDemo.demo();
	}
}
