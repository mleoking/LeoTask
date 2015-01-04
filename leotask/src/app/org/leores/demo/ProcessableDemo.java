package org.leores.demo;

import java.util.Arrays;
import java.util.List;

import org.leores.util.U;
import org.leores.util.able.Processable1;

public class ProcessableDemo extends Demo {

	public void expression() {
		List<String> lData1 = Arrays.asList(new String[] { "1.234E5", "7.54E-9", "1.1E+5" });
		List<Double> lData2 = Arrays.asList(new Double[] { 1E5, 1E-2, 1E+10 });
		Processable1 pa1 = new Processable1.Expression<String>("%/0.1");
		Processable1 pa2 = new Processable1.Expression<Double>("%/0.01");

		log("data1:" + lData1);
		log("data2:" + lData2);
		
		U.processElements(lData1,pa1);
		U.processElements(lData2,pa2);
		
		log("After processing:");
		log("data1:" + lData1);
		log("data2:" + lData2);
	}

	public static void demo() {
		ProcessableDemo demo = new ProcessableDemo();
		demo.expression();
	}

}
