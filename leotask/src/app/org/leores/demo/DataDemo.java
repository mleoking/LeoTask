package org.leores.demo;

import java.util.Arrays;

import org.leores.util.U;
import org.leores.util.data.Distribution;
import org.leores.util.data.Statistic;
import org.leores.util.data.Statistics;

public class DataDemo extends Demo {
	public static class Data implements Comparable {
		public static int count = 0;
		public Integer id;
		public Integer i1;
		protected Double d1;
		private String s1;

		public Data() {
		}

		public Data(Integer i1, Double d1, String s1) {
			id = count;
			this.i1 = i1;
			this.d1 = d1;
			this.s1 = s1;
			count++;
		}

		public int compareTo(Object o) {
			Data d2 = (Data) o;
			return id - d2.id;
		}
	}

	public String fStatConfig = "stat.xml";
	public String fStatsConfig = "stats.xml";
	public String fStatOut = "stat.csv";
	public String fStatsOut = "stats.csv";
	public String fStatsOutAggregate = "statsAggregate.csv";
	public String fDistOut = "dist.csv";

	public DataDemo() {
		U.deleteFile(fStatOut);
		U.deleteFile(fStatsOut);
		U.deleteFile(fStatsOutAggregate);
		U.deleteFile(fDistOut);
	}

	public Statistic prepareStat(boolean bReadFromConfig) {
		Statistic rtn = new Statistic();

		if (bReadFromConfig) {
			U.loadFromXML(rtn, fStatConfig);
		} else {
			/**
			 * protected and private fields can also be read in the Statistic.
			 */
			rtn.info = "Leo Stat 1";
			rtn.keyVar = "id";
			rtn.valVar = "d1";
			rtn.parVars = new String[] { "i1", "s1" };
			rtn.bUniqueKeys = true;
			rtn.afterLoadObj("DataDemo.prepareStat");
		}

		return rtn;
	}

	public Data[] prepareData(int n) {
		Data[] data = new Data[n];
		for (int i = 0; i < data.length; i++) {
			data[i] = new Data(i, i + 0.1, "columns:" + i);
		}
		return data;
	}

	public void statDemo() {
		Statistic stat1 = prepareStat(false), stat2 = prepareStat(true);
		stat1.bUniqueKeys = true;
		stat2.bUniqueKeys = false;
		Data[] data = prepareData(10);
		for (int i = 0; i < data.length; i++) {
			if ((i + 1) % 2 == 1) {
				stat1.put(data[i]);
			} else {
				stat2.put(data[i]);
			}
		}

		stat1.put(data[2]);
		stat1.put(data[4]);
		stat2.put(data[1]);
		stat2.put(data[3]);

		Data data1 = new Data(100, 4.11, "columns:4");
		stat2.put(data1);

		stat2.put(data[6]);
		stat2.put(data[6]);
		stat2.put(data[8]);

		data[8].i1 = 101;
		stat2.put(data[8]);

		Data dataInfinity = new Data(100, 1.0 / 0.0, "columns: invalid");
		Data dataNaN = new Data(100, 0.0 / 0.0, "columns: invalid");		
		stat1.put(new Data(100, 1d, "columns: invalid"));
		stat1.put(new Data(100, 2d, "columns: invalid"));
		stat1.put(dataInfinity);
		stat1.put(dataNaN);
		stat1.put(new Data(101, 1.0 / 0.0, "columns: invalid"));
		stat1.put(new Data(101, 0.0 / 0.0, "columns: invalid"));
		
		stat1.saveToCsvFile(fStatOut);
		stat2.saveToCsvFile(fStatOut);

		Statistic stat3 = stat1.newClone();
		Data data2 = new Data(1, 4.1, "columns:4");
		stat3.put(data[2]);
		stat3.put(data2);
		stat3.put(data2);
		stat3.put(new Data(555, 5.1, "columns:5"));

		stat3.saveToCsvFile(fStatOut);

		stat1.merge(stat2);
		stat1.merge(stat3);
		stat1.info += " after merge";
		stat1.saveToCsvFile(fStatOut);

		//The plain useage of Statistic
		Statistic statPlain = new Statistic();
		statPlain.valid = "$%$<=3.3";
		statPlain.put(1.1);
		statPlain.put(2.0);
		statPlain.put(3.3);
		statPlain.put(2.1);
		statPlain.put(1.0);
		statPlain.put(1.0 / 0.0);//1.0/0.0 will produce an infinity. 
		statPlain.put(0.0 / 0.0);//1.0/0.0 will produce a NaN.  
		statPlain.put(null);//Null will not be put into the statstican columns. It will be rejected.
		statPlain.put(4.0);
		statPlain.saveToCsvFile(fStatOut);
	}

	public void statInvalidExpression() {
		Statistic stat = new Statistic();
		stat.info = "stat";
		stat.valVar = "#$i1$+$d1$#";
		Data[] data = prepareData(5);
		data[0].i1 = null;
		data[2].d1 = 2.1 / 0.0;
		for (int i = 0; i < data.length; i++) {
			stat.put(data[i]);
		}
		stat.print();
	}

	public Statistics prepareStats(boolean bReadFromConfig) {
		Statistics rtn = new Statistics();
		if (bReadFromConfig) {
			U.loadFromXML(rtn, fStatsConfig);
		} else {
			Statistic stat = new Statistic();
			rtn.info = "stats2";
			rtn.setPoints(new String[] { "p1" });
			stat.info = "p1@stat1";
			stat.keyVar = "id";
			stat.valVar = "i1";
			stat.parVars = new String[] { "d1", "s1" };
			stat.afterLoadObj("DataDemo.prepareStats");
			rtn.add(stat, false);
			rtn.afterLoadObj("DataDemo.prepareStats");
		}

		return rtn;
	}

	public void statsDemo() {
		Statistics stats1 = prepareStats(true), stats2 = prepareStats(false);
		Statistics stats1nc = stats1.newClone();
		Statistic stat1 = prepareStat(false);

		Data[] data = new Data[10];
		for (int i = 0; i < data.length; i++) {
			int ip1 = i + 1;
			data[i] = new Data(ip1, ip1 + 0.1, "columns:" + ip1);
			if (ip1 % 3 == 0) {
				if (ip1 % 2 == 0) {
					stats1.stat(data[i], "p1");
				} else {
					stats1.stat(data[i], "p2");
				}
			} else if (ip1 % 3 == 1) {
				if (ip1 % 2 == 0) {
					stats2.stat(data[i], "p1");
				} else {
					stats2.stat(data[i], "p2");
				}
			} else if (ip1 % 3 == 2) {
				if (ip1 % 2 == 0) {
					stats1nc.stat(data[i], "p1");
				} else {
					stats1nc.stat(data[i], "p2");
				}
			}
			if (ip1 % 2 == 0) {
				stat1.put(data[i]);
			}
		}

		stat1.put(data[1]);
		stat1.put(data[3]);

		stat1.saveToCsvFile(fStatsOut);
		stats1.sPatNumOut = "0.0";
		stats1.saveToCsvFile(fStatsOut);
		stats2.saveToCsvFile(fStatsOut);
		stats1nc.saveToCsvFile(fStatsOut);

		stats1.merge(stat1);
		stats1.merge(stats2);
		stats1.merge(stats1nc);

		stats1.info += " after merge";
		stats1.saveToCsvFile(fStatsOut);

		//The plain useage of Statistic
		Statistic statPlain = new Statistic();
		statPlain.valid = "$%$<=3.3";
		statPlain.put(1.1);
		statPlain.put(2.0);
		statPlain.put(3.3);
		statPlain.put(2.1);
		statPlain.put(1.0);
		statPlain.put(1.0 / 0.0);//1.0/0.0 will produce an infinity. 
		statPlain.put(0.0 / 0.0);//1.0/0.0 will produce a NaN.  
		statPlain.put(null);//Null will not be put into the statstican columns. It will be rejected.
		statPlain.put(4.0);

		stats1.add(statPlain, false);
		stats1.add(statPlain, false);

		/*
		 * Normally the info of a statistic should not be changed after being
		 * started. Statistic.info and .paramVars are used to determine whether
		 * two statistic could be merged.
		 */
		stat1.info = "p1@stat1";
		stats1.merge(stat1);
		stats1.info += " and change remerge stat1";
		stats1.sPatNumOut = "#.#";//The sPatNumOut of stats will overwrite that of each stat when it is set.		
		stats1.saveToCsvFile(fStatsOut);
		stats1.sPatNumOut = "0.00";//The sPatNumOut of stats will overwrite that of each stat when it is set.
		stats1.saveAggregateToCsvFile(fStatsOutAggregate);
	}

	public void distDemo() {
		Distribution dist = new Distribution();
		dist.put(1.0);
		dist.put(2.2);
		dist.put(2.5);
		dist.put(3.1);
		dist.put(1.1);
		dist.put(1.00001);
		dist.put(2.9999999);
		dist.put(4.0);
		dist.put(4.1);

		dist.calDist(1.0);
		dist.saveToCSVFile(fDistOut);

		dist.calDist(0.5, 2.0);
		dist.saveToCSVFile(fDistOut);

		dist.calDist(1.0, 1.0, 3.0);
		dist.saveToCSVFile(fDistOut);

		dist.calDistIntoNparts(3);
		dist.saveToCSVFile(fDistOut);

		Distribution dist2 = new Distribution("prepared distribtuion");
		dist2.prep(1.0, 1.0, 3.0);
		dist2.stat(0.0);
		dist2.stat(0.1);
		dist2.stat(1.0);
		dist2.stat(1.9);
		dist2.stat(2.0);
		dist2.stat(2.999999999);
		dist2.stat(3.0);
		dist2.stat(3.1);
		dist2.stat(4.0);
		dist2.calDist();
		dist2.saveToCSVFile(fDistOut);
		log("nTotal: " + dist2.getNTotal() + " nCounted: " + dist2.getNCounted());
	}

	public static void demo() {
		DataDemo demo = new DataDemo();
		demo.statDemo();
		demo.statInvalidExpression();
		demo.statsDemo();
		demo.distDemo();
	}

}
