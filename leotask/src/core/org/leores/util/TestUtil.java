package org.leores.util;

import org.leores.util.able.Processable0;

public class TestUtil extends LogUtil {
	public static double[] compareRunTime(int nRept, int nComp, Runnable... runs) {
		double[] rtn = new double[runs.length];
		Timer timer = new Timer();
		tLog("TestUtil.compareRunTime : ", U.toStr(runs));
		for (int i = 0; i < nComp; i++) {
			tLog("----- " + i + " -----");
			for (int k = 0; k < runs.length; k++) {
				long tTotal = 0;
				for (int j = 0; j < nRept; j++) {
					timer.start();
					runs[k].run();
					tTotal += timer.stop();
				}
				double tAverage = (double) tTotal / nRept;
				rtn[k] = tAverage;
				tLog("r" + k + " [average, total]ms : ", tAverage, tTotal);
			}
		}
		tLog("----- End -----");
		return rtn;
	}

	public static double[] compareRunTime(int nRept, int nComp, Processable0<Double>... pros) {
		double[] rtn = new double[pros.length];
		Timer timer = new Timer();
		long tDifference = 0;
		Double dummyRtn = 0.0;//do dummy operation on p1 and p2's return results to avoid java's optimisation from cancelling each run.

		tLog("TestUtil.compareRunTime : ", U.toStr(pros));
		for (int i = 0; i < nComp; i++) {
			tLog("----- " + i + " -----");
			for (int k = 0; k < pros.length; k++) {
				long tTotal = 0;
				for (int j = 0; j < nRept; j++) {
					timer.start();
					dummyRtn += pros[k].process();
					tTotal += timer.stop();
				}
				double tAverage = (double) tTotal / nRept;
				rtn[k] = tAverage;
				tLog("p" + k + " [average, total]ms : ", tAverage, tTotal);
			}
		}
		tLog("----- End -----");
		tLog("dummySum : " + dummyRtn);
		return rtn;
	}
}
