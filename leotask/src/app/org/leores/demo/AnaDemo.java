package org.leores.demo;

import org.leores.net.ana.Metric;

public class AnaDemo extends Demo {

	public void batchMetric(String sFNet, String sExt, int iFrom, int iTo) {
		for (int i = iFrom; i <= iTo; i++) {
			String sFNeti = sFNet + i + sExt;
			log(sFNeti);
			Metric metric = new Metric(sFNeti);
			Double cf = metric.correlationFunction();
			Double ad = metric.avgDegree();
			log("CorrelationFunction: " + cf + " AverageDegree:" + ad);
		}
	}

	public void netMetric() {
		Metric metric;
		batchMetric("net-er-gne-(1000,2000)-", ".dat", 0, 2);
	}
	
	public void printMetric(String sFile){
		Metric metric = new Metric(sFile);
		metric.printMetrics();
	}

	public static void demo() {
		AnaDemo anaDemo = new AnaDemo();
		anaDemo.printMetric("net-er-gne-(1000,2000).dat");
		anaDemo.netMetric();
	}

}
