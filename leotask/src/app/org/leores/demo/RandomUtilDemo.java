package org.leores.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.leores.plot.JGnuplot;
import org.leores.plot.JGnuplot.Plot;
import org.leores.util.RandomUtil;
import org.leores.util.TestUtil;
import org.leores.util.U;
import org.leores.util.data.DataTableSet;
import org.leores.util.data.Distribution;

public class RandomUtilDemo extends Demo {
	public void genN() {
		RandomUtil ru = new RandomUtil(null);
		int[] rtn = ru.genN(5, 0, 9);
		for (int i = 0; i < rtn.length; i++) {
			log(rtn[i]);
		}
		log("---------");
		rtn = ru.genN(5, 1, 7);
		for (int i = 0; i < rtn.length; i++) {
			log(rtn[i]);
		}
	}

	public void getNElementSpeed() {
		final RandomUtil ru = new RandomUtil(null);
		final int max = 100000;
		final List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < max; i++) {
			list.add(i);
		}
		final int n = max * 9 / 10;

		Runnable r0 = new Runnable() {
			public void run() {
				ru.getNElementsKnuthFisherYatesShuffle(list, n);
			}
		};

		Runnable r1 = new Runnable() {
			public void run() {
				ru.getNElementsReservoirSampling(list, n);
			}
		};

		Runnable r2 = new Runnable() {
			public void run() {
				List<Integer> rtn = new ArrayList<Integer>();
				int[] ids = ru.genN(n, 0, max);
				for (int i = 0; i < ids.length; i++) {
					rtn.add(list.get(ids[i]));
				}
			}
		};

		Runnable rcombined = new Runnable() {
			public void run() {
				ru.getNElements(list, n);
			}
		};

		TestUtil.compareRunTime(100, 3, r0, r1, r2, rcombined);
	}

	public void getNElementRandomQuality() {
		RandomUtil ru = new RandomUtil(null);
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= 5; i++) {
			list.add(i);
		}
		Distribution dist = new Distribution("NElementRandomQuality");
		for (int i = 0; i < 100000; i++) {
			List<Integer> lSel = ru.getNElements(list, 2);
			Collections.sort(lSel);
			int val = 0;
			for (int j = 0, mj = lSel.size(); j < mj; j++) {
				int valj = lSel.get(j);
				val += valj * Math.pow(10, mj - 1 - j);
			}
			dist.statDiscrete((double) val);
		}
		dist.sPatNumOut = ".####";
		dist.calDist();
		dist.print();
	}

	public void getNSpeed() {
		final RandomUtil ru = new RandomUtil(null);
		final int min = 0, max = 100000, n = max / 100;
		Runnable r1 = new Runnable() {
			public void run() {
				ru.genNBitSet(n, min, max);
			}
		};
		Runnable r2 = new Runnable() {
			public void run() {
				ru.genNReservoirSampling(n, min, max);
			}
		};
		Runnable r3 = new Runnable() {
			public void run() {
				ru.genN(n, min, max);
			}
		};
		TestUtil.compareRunTime(100, 3, r1, r2, r3);
	}

	public void getNRandomQuality() {
		RandomUtil ru = new RandomUtil(null);
		Distribution dist = new Distribution("NElementRandomQuality");
		for (int i = 0; i < 100000; i++) {
			int[] iSel = ru.genN(2, 1, 10);
			List<Integer> lSel = U.parseList(iSel);
			Collections.sort(lSel);
			int val = 0;
			for (int j = 0, mj = lSel.size(); j < mj; j++) {
				int valj = lSel.get(j);
				val += valj * Math.pow(10, mj - 1 - j);
			}
			dist.statDiscrete((double) val);
		}
		dist.sPatNumOut = ".####";
		dist.calDist();
		dist.print();
	}

	public static void demo() {
		RandomUtilDemo demo = new RandomUtilDemo();
		demo.genN();
		demo.getNElementSpeed();
		demo.getNElementRandomQuality();
		demo.getNSpeed();
		demo.getNRandomQuality();
	}
}
