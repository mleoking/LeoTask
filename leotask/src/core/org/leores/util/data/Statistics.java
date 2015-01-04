package org.leores.util.data;

import java.io.FileWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.leores.plot.JGnuplot;
import org.leores.plot.JGnuplot.Plot;
import org.leores.util.Logger;
import org.leores.util.ObjArray;
import org.leores.util.U;
import org.leores.util.UniqueHashList;
import org.leores.util.data.Statistic.StatElement;

public class Statistics extends Logger implements Serializable, Cloneable {
	private static final long serialVersionUID = -6566559883011416032L;
	protected static String sPut = "@";
	protected static String sMethodPrefix = "%";

	protected List<Statistic> members;
	protected Map<String, List<Statistic>> statPointsMap;
	protected List<String> points;
	/**
	 * nMerged here only counts those Statistic<b>s</b> merging. Merging a
	 * single statistic is not counted in this variable.
	 */
	public Integer nMerged;
	//Default parameters for each statistic 
	protected String keyVar, valVar, valid;
	protected String[] parVars;
	protected Integer initCapacity;
	public String sPatNumOut;
	protected Boolean bUniqueKeys; //whether reject columns with a duplicate key. default to be true.
	protected Integer maxListOutSize; //the maximum output size of lists. default to be -1: no limit.

	public String info;

	public static class StatsElement implements Comparable<StatsElement>, Serializable {
		public ObjArray valVals;
		public ObjArray parVals;

		public StatsElement(ObjArray valVals, ObjArray parVals) {
			this.valVals = valVals;
			this.parVals = parVals;
		}

		public int compareTo(StatsElement eStats) {
			ObjArray parVals = null;
			if (eStats != null) {
				parVals = eStats.parVals;
			}
			return U.compare(this.parVals, parVals);
		}

		public boolean has(ObjArray parVals) {
			boolean rtn = U.compare(this.parVals, parVals) == 0;
			return rtn;
		}
	}

	public Statistics() {
		members = new ArrayList<Statistic>();
		nMerged = 0;
		info = null;
		points = null;

		keyVar = null;
		valVar = null;
		valid = null;
		parVars = null;
		initCapacity = null;
		sPatNumOut = null;
		bUniqueKeys = null;
		maxListOutSize = null;
	}

	//These set function overload the setting of the field value by ObjUtil.setFieldValue. 
	//So that the evaluation is not executed. There is no need to put sNoEval symbol in the xml setting file for these fields.	
	public void setKeyVar(String keyVar) {
		this.keyVar = keyVar;
	}

	public void setValVar(String valVar) {
		this.valVar = valVar;
	}

	public void setValid(String valid) {
		this.valid = valid;
	}

	public void setsPatNumOut(String sPatNumOut) {
		this.sPatNumOut = sPatNumOut;
	}

	public static boolean bPut(String info, String point) {
		boolean rtn = false;
		if (info != null && point != null) {
			rtn = info.contains(point + sPut);
		}
		return rtn;
	}

	public static String removeStatMethods(String str) {
		String rtn = str;
		if (rtn != null) {
			String sMethodPattern = sMethodPrefix + "(\\S+?)" + sPut;
			rtn = rtn.replaceAll(sMethodPattern, "");
		}
		return rtn;
	}

	public static String removeStatVariable(String str) {
		String rtn = str;
		if (rtn != null) {
			String sVariablePattern = sPut + "(.+?)$";
			rtn = rtn.replaceAll(sVariablePattern, sPut);
		}
		return rtn;
	}

	public static String statPointInfo(String str) {
		String rtn = str;
		if (rtn != null) {
			rtn = removeStatMethods(rtn);
			rtn = removeStatVariable(rtn);
		}
		return rtn;
	}

	/**
	 * This function will be automatically called by ObjUtil.loadFromXML
	 * 
	 * @param sLoadMethod
	 */

	public void afterLoadObj(String sLoadMethod) {
		//Apply the default values for each statistic if its parameter is not set manually.
		for (int i = 0; i < members.size(); i++) {//lData.size() may increasesif some lData can be splited.
			Statistic stati = members.get(i);
			List<Statistic> stats = stati.split();
			if (stats != null) {
				members.addAll(stats);
				stati = null;
				members.remove(i);
				i--;
			} else {
				if (stati.keyVar == null) {
					stati.keyVar = keyVar;
				}
				if (stati.valVar == null) {
					stati.valVar = valVar;
				}
				if (stati.valid == null) {
					stati.valid = valid;
				}
				if (stati.parVars == null) {
					stati.parVars = parVars;
				}
				if (stati.initCapacity == null) {
					stati.initCapacity = initCapacity;
				}
				if (stati.sPatNumOut == null) {
					stati.sPatNumOut = sPatNumOut;
				}
				if (stati.bUniqueKeys == null) {
					stati.bUniqueKeys = bUniqueKeys;
				}
				if (stati.maxListOutSize == null) {
					stati.maxListOutSize = maxListOutSize;
				}
				stati.afterLoadObj("Statistics.afterLoadObj");
			}
		}
		buildStatPointsMap();
	}

	public Statistics newClone() {
		Statistics rtn = new Statistics();

		if (points != null) {
			U.copy(rtn, this, "points");
		}
		rtn.nMerged = 0;
		rtn.info = info;
		for (int i = 0, size = members.size(); i < size; i++) {
			Statistic stat = members.get(i);
			if (stat != null) {
				Statistic statNewClone = stat.newClone();
				rtn.add(statNewClone, false);
			}
		}
		rtn.afterLoadObj("newClone");

		return rtn;
	}

	public static List<Statistic> find(List<Statistic> lStat, String regex) {
		List<Statistic> rtn = null;
		if (lStat != null && regex != null) {
			rtn = new ArrayList<Statistic>();
			for (int i = 0, mi = lStat.size(); i < mi; i++) {
				Statistic stat = lStat.get(i);
				if (stat.info != null && stat.info.matches(regex)) {
					rtn.add(stat);
				}
			}
		}
		return rtn;
	}

	/**
	 * Find Statistic lData whose info match the regular expression
	 * <b>regex</b>.
	 * 
	 * @param regex
	 * @return
	 */

	public List<Statistic> find(String regex) {
		return find(members, regex);
	}

	public List<Object> findGet1AvgVals(String regex) {
		List<Object> rtn = null;
		List<Statistic> lStat = find(regex);
		if (lStat != null && lStat.size() > 0) {
			rtn = lStat.get(0).getAvgVals();
		}
		return rtn;
	}

	public List<Statistic> findPrint(String regex) {
		List<Statistic> rtn = find(regex);
		if (rtn != null) {
			for (int i = 0, mi = rtn.size(); i < mi; i++) {
				rtn.get(i).print();
			}
		}
		return rtn;
	}

	public List<Statistic> get(String point) {
		List<Statistic> rtn = null;
		if (statPointsMap != null) {
			rtn = statPointsMap.get(point);
		}
		return rtn;
	}

	public static String sMethodPattern(String statMethodPoint) {
		String rtn = null;
		if (statMethodPoint != null) {
			rtn = sMethodPrefix + "([^\\s" + sMethodPrefix + "]+?)";
			rtn += Pattern.quote(statMethodPoint) + sPut;
		}
		return rtn;
	}

	public synchronized int statMethods(String statMethodPoint, Object... oPars) {
		int rtn = 0;
		if (statMethodPoint != null && oPars != null) {
			List<Statistic> lStat = get(statMethodPoint);
			if (lStat != null) {
				UniqueHashList<String> ulSMethods = new UniqueHashList<String>();
				for (int i = 0, mi = lStat.size(); i < mi; i++) {
					Statistic stat = lStat.get(i);
					String[] sMethods = U.parseStrArray(stat.info, sMethodPattern(statMethodPoint));
					for (int j = 0; j < sMethods.length; j++) {
						ulSMethods.add(sMethods[j]);
						Method method = U.getMethod(stat.getClass(), sMethods[j], oPars);
						if (method != null) {
							U.invokeMethod(stat, method, oPars);
						} else {
							U.invokeObjMethodByName(stat, sMethods[j], false);
						}
						rtn++;
					}
				}
				for (int i = 0, mi = ulSMethods.size(); i < mi; i++) {
					String sMethod = ulSMethods.get(i);
					Object[] oParsTU = new Object[oPars.length + 1];
					oParsTU[0] = lStat;
					System.arraycopy(oPars, 0, oParsTU, 1, oPars.length);
					Method method = U.getMethod(this.getClass(), sMethod, oParsTU);
					if (method != null) {
						U.invokeMethod(this, method, oParsTU);
					}
				}
			}
		}
		return rtn;
	}

	public static DataTableSet getDataTableSet(DataTableSet dts, List<Statistic> lStat, String info) {
		DataTableSet rtn = dts;
		if (lStat != null && lStat.size() > 0) {
			if (rtn == null) {
				rtn = new DataTableSet(info);
			}
			for (int i = 0, mi = lStat.size(); i < mi; i++) {
				Statistic stat = lStat.get(i);
				DataTable dt = stat.getDataTable(null, null, null);
				rtn.add(dt);
			}
			if (rtn.members.size() == 0) {
				rtn = null;
			}
		} else {
			rtn = null;
		}
		return rtn;
	}

	public DataTableSet getDataTableSet(DataTableSet dts, String regex) {
		List<Statistic> lStat = null;
		if (regex == null) {
			lStat = members;
		} else {
			lStat = find(regex);
		}
		DataTableSet rtn = getDataTableSet(dts, lStat, info);
		return rtn;
	}

	public static Plot getPlot(Plot plot, List<Statistic> lStat, String info) {
		Plot rtn = plot;
		DataTableSet dts = getDataTableSet(null, lStat, null);
		if (dts != null) {
			if (rtn == null) {
				rtn = new Plot(info);
			}
			rtn.add(dts);
			rtn.autoSetLabels();
		} else {
			rtn = null;
		}
		return rtn;
	}

	public Plot getPlot(Plot plot, String regex) {
		List<Statistic> lStat = null;
		if (regex == null) {
			lStat = members;
		} else {
			lStat = find(regex);
		}
		Plot rtn = getPlot(plot, lStat, info);
		return rtn;
	}

	public int plotm(JGnuplot jGnuplot, List<Statistic> lStat, String sFPreFix, String sLoad) {
		int rtn = 0;
		JGnuplot jg = jGnuplot;
		if (jg == null) {
			jg = new JGnuplot();
			jg.sPatNumOut = sPatNumOut;
		}
		U.loadFromString(jg, sLoad, false);
		List<Statistic> lStatTU = lStat;
		if (lStatTU == null) {
			lStatTU = members;
		}
		UniqueHashList<String> ulStatInfo = new UniqueHashList<String>();
		for (int i = 0, mi = lStatTU.size(); i < mi; i++) {
			Statistic stat = lStatTU.get(i);
			ulStatInfo.add(statPointInfo(stat.info));
		}
		for (int i = 0, mi = ulStatInfo.size(); i < mi; i++) {
			String statInfo = ulStatInfo.get(i);
			List<Statistic> lStatI = new ArrayList<Statistic>();
			String title = statInfo;
			String sParVars = "";
			for (int j = 0, mj = lStatTU.size(); j < mj; j++) {
				Statistic stat = lStatTU.get(j);
				if (statInfo.equals(statPointInfo(stat.info))) {
					title += stat.valVar + ",";
					lStatI.add(stat);
					if (sParVars.length() == 0) {
						sParVars = U.wrap(stat.parVars, "", "", ",");
					}
				}
			}
			title += "%" + sParVars;
			if (info != null) {
				title = info + " " + title;
			}
			Plot plot = getPlot(null, lStatI, title);
			if (plot != null) {
				U.loadFromString(plot, sLoad, false);
				if (sFPreFix != null) {
					jg.compile(plot, null, sFPreFix + title + ".plt");
				} else {
					jg.execute(plot);
				}
				rtn++;
			} else {
				//log(LOG_WARNING, "Empty plot: " + lStatI);
			}
		}
		return rtn;
	}

	public int plotm(List<Statistic> lStat, String sFPreFix, String sLoad) {
		List<Statistic> lStatTU = lStat;
		if (lStatTU == null) {
			lStatTU = members;
		}
		lStatTU = find(lStatTU, ".*" + sMethodPrefix + "plotm.*");
		return plotm(null, lStatTU, null, sLoad);
	}

	public int pltm(List<Statistic> lStat, String sFPreFix, String sLoad) {
		List<Statistic> lStatTU = lStat;
		if (lStatTU == null) {
			lStatTU = members;
		}
		lStatTU = find(lStatTU, ".*" + sMethodPrefix + "pltm.*");
		return plotm(null, lStatTU, sFPreFix, sLoad);
	}

	public void printm(List<Statistic> lStat, String sFPreFix, String sLoad) {
		List<Statistic> lStatTU = lStat;
		if (lStatTU == null) {
			lStatTU = members;
		}
		lStatTU = find(lStatTU, ".*" + sMethodPrefix + "(printm).*");
		UniqueHashList<String> ulStatInfo = new UniqueHashList<String>();
		for (int i = 0, mi = lStatTU.size(); i < mi; i++) {
			Statistic stat = lStatTU.get(i);
			ulStatInfo.add(statPointInfo(stat.info));
		}
		for (int i = 0, mi = ulStatInfo.size(); i < mi; i++) {
			String statInfo = ulStatInfo.get(i);
			List<Statistic> lStatI = new ArrayList<Statistic>();
			for (int j = 0, mj = lStatTU.size(); j < mj; j++) {
				Statistic stat = lStatTU.get(j);
				if (statInfo.equals(statPointInfo(stat.info))) {
					lStatI.add(stat);
				}
			}
			outputAggregate(lStatI, null, "", "", "\t");
		}
	}

	public List<String> getPoints() {
		return points;
	}

	public void setPoints(List<String> points) {
		this.points = points;
	}

	public void setPoints(String[] points) {
		this.points = new ArrayList<String>(Arrays.asList(points));
	}

	public int addPoints(String... points) {
		int rtn = -1;
		if (points != null) {
			rtn = 0;
			if (this.points == null) {
				this.points = new ArrayList<String>();
			}
			for (int i = 0; i < points.length; i++) {
				if (points[i] != null && (!this.points.contains(points[i]))) {
					this.points.add(points[i]);
					rtn++;
				}
			}
		}
		return rtn;
	}

	public synchronized boolean buildStatPointsMap() {
		boolean rtn = false;
		if (points != null && points.size() > 0) {
			statPointsMap = null;
			statPointsMap = new HashMap<String, List<Statistic>>();
			for (int i = 0, size = members.size(); i < size; i++) {
				Statistic stati = members.get(i);
				if (stati.info != null) {
					for (int j = 0, sizej = points.size(); j < sizej; j++) {
						String point = points.get(j);
						if (bPut(stati.info, point)) {
							rtn = true;
							List<Statistic> lStat = statPointsMap.get(point);
							if (lStat == null) {
								lStat = new ArrayList<Statistic>();
								statPointsMap.put(point, lStat);
							}
							lStat.add(stati);
						}
					}
				}
			}
		}
		return rtn;
	}

	public synchronized boolean stat(Object obj, List<Statistic> lStat) {
		boolean rtn = false;

		for (int i = 0, size = lStat.size(); i < size; i++) {
			Statistic stati = lStat.get(i);
			boolean bPut = stati.put(obj);
			rtn = rtn || bPut;
		}

		return rtn;
	}

	public boolean stat(Object obj, String point) {
		boolean rtn = false;

		if (obj != null && point != null) {
			List<Statistic> lStat = get(point);
			if (lStat != null) {
				rtn = true;
				stat(obj, lStat);
			}
		}

		return rtn;
	}

	public synchronized boolean add(Statistic stat, boolean bUpdateMap) {
		boolean rtn = members.add(stat);
		if (bUpdateMap) {
			buildStatPointsMap();
		}
		return rtn;
	}

	public synchronized void clear() {
		members.clear();
	}

	/**
	 * Merge a stat into the statistics. A stat can only be merged into at most
	 * one stat in the statistics.
	 * 
	 * @param stat
	 * @param bUpdateMap
	 * @return true: a new stat that is not mergeable by any existing ones is
	 *         added.
	 */

	public synchronized boolean merge(Statistic stat, boolean bUpdateMap) {
		boolean bNew = false;
		if (stat != null) {
			bNew = true;
			for (int j = 0, size = members.size(); j < size; j++) {
				Statistic statj = members.get(j);
				if (statj.merge(stat)) {
					bNew = false;
					break;//A stat can only be merged into at most one stat in the statistics.
				}
			}
			if (bNew) {
				add(stat, bUpdateMap);
			}
		}

		return bNew;
	}

	public boolean merge(Statistic stat) {
		return merge(stat, true);
	}

	public synchronized void merge(Statistics stats) {
		if (stats != null) {
			boolean bNewStatAdded = false;
			for (int i = 0, sizei = stats.members.size(); i < sizei; i++) {
				Statistic stati = stats.members.get(i);
				boolean bNew = merge(stati, false);
				bNewStatAdded = bNewStatAdded || bNew;
			}
			if (bNewStatAdded) {
				buildStatPointsMap();
			}
			nMerged += stats.nMerged + 1;
		}
	}

	protected void output(FileWriter fw, String str) {
		if (!U.append(fw, str)) {
			log(false, str);
		}
	}

	/**
	 * The sPatNumOut of Statistics will overwrite that of each Statistic
	 * when sPatNumOut is set.
	 * 
	 * @param fn
	 * @param bef
	 * @param aft
	 * @param de
	 */
	public void output(List<Statistic> oMembers, String fn, String bef, String aft, String de) {
		if (oMembers == null) {
			oMembers = members;
		}
		String outString = U.toOutString(this, bef, aft, de, sPatNumOut, true, null);
		output(U.createFileWriter(fn), outString);
		for (int i = 0, size = oMembers.size(); i < size; i++) {
			Statistic stati = oMembers.get(i);
			String statisPatNumOut = stati.sPatNumOut;
			if (sPatNumOut != null && !"".equals(sPatNumOut)) {
				stati.sPatNumOut = sPatNumOut;
			}
			stati.output(fn, bef, aft, de);
			stati.sPatNumOut = statisPatNumOut;
		}
	}

	public void saveToCsvFile(String fn) {
		if (fn != null) {
			output(null, fn, "\"", "\"", ",");
		}
	}

	public void print() {
		output(null, null, "", "", "\t");
	}

	public void outputAggregate(List<Statistic> oMembers, String fn, String bef, String aft, String de) {
		if (oMembers == null) {
			oMembers = members;
		}
		int nMembers = oMembers.size();
		List<Integer> memberIds2Aggregate = new ArrayList<Integer>();
		String outString;

		FileWriter fw = U.createFileWriter(fn);

		for (int i = 0; i < nMembers; i++) {
			memberIds2Aggregate.add(i);
		}

		outString = U.toOutString(this, bef, aft, de, sPatNumOut, true, null);
		output(fw, outString);

		while (memberIds2Aggregate.size() > 0) {
			int id0 = memberIds2Aggregate.get(0);
			Statistic stat0 = oMembers.get(id0);
			String[] parVars = stat0.parVars;
			List<Statistic> membersWithSameParVars = new ArrayList<Statistic>();
			List<StatsElement> elements = new ArrayList<StatsElement>();
			Map<ObjArray, StatsElement> elementMap = new HashMap<ObjArray, StatsElement>(stat0.elements.size());

			//Here is using i < memberIds2Aggregate.size() rather than using size = memberIds2Aggregate.size() and i < size
			//Because the size of memberIds2Aggregate changes in the loop. We use memberIds2Aggregate.size() to take this into account.
			for (int i = 0; i < memberIds2Aggregate.size(); i++) {
				int idi = memberIds2Aggregate.get(i);
				Statistic stati = oMembers.get(idi);
				if (U.compare(stati.parVars, parVars) == 0) {
					membersWithSameParVars.add(stati);
					memberIds2Aggregate.remove(i);
					/*
					 * i-- because one element is deleted from
					 * memberIds2Aggregate. so that i only increases when the
					 * current statistic is not with the same parVars
					 */
					i--;
				}
			}

			int mSize = membersWithSameParVars.size();
			String[] valVars = new String[mSize];
			String[] infos = new String[mSize];

			for (int i = 0; i < mSize; i++) {
				Statistic stat = membersWithSameParVars.get(i);
				valVars[i] = stat.valVar;
				infos[i] = removeStatMethods(stat.info);
				List<StatElement> statElements = stat.elements;
				for (int j = 0, eSize = statElements.size(); j < eSize; j++) {
					StatElement eStat = statElements.get(j);
					ObjArray parVals = eStat.parVals;
					boolean bHashCollision = false;

					eStat.update();

					StatsElement eStats = elementMap.get(parVals);
					if (eStats != null && !eStats.has(parVals)) {
						bHashCollision = true;
						eStats = null;
					}

					if (bHashCollision) {
						for (int k = 0, seSize = elements.size(); k < seSize; k++) {
							StatsElement eStatsk = elements.get(k);
							if (eStatsk.has(parVals)) {
								eStats = eStatsk;
								break;
							}
						}
					}

					//no existing StatsElement found to match the parVals;
					if (eStats == null) {
						Object[] oValVals = new Object[mSize];
						ObjArray oaValVals = new ObjArray(oValVals);
						oaValVals.set(i, eStat.vAvg);
						eStats = new StatsElement(oaValVals, parVals);
						elements.add(eStats);
						elementMap.put(parVals, eStats);
					} else {
						eStats.valVals.set(i, eStat.vAvg);
					}
				}
			}

			StringBuffer sBuffer = new StringBuffer();
			sBuffer.append("StatInfos" + de + U.wrap(infos, bef, aft, de) + "\n");
			if (parVars != null) {
				sBuffer.append(U.wrap(parVars, bef, aft, de) + de);
			}
			sBuffer.append("|" + de + U.wrap(valVars, bef, aft, de) + "\n");
			output(fw, sBuffer.toString());

			Collections.sort(elements);
			for (int i = 0, seSize = elements.size(); i < seSize; i++) {
				StringBuffer sBufferi = new StringBuffer();
				StatsElement eStats = elements.get(i);
				String[] sValVals = U.toStrArray(eStats.valVals.asList(), sPatNumOut);
				if (eStats.parVals != null) {
					String[] sParVals = U.toStrArray(eStats.parVals.asList(), sPatNumOut);
					sBufferi.append(U.wrap(sParVals, bef, aft, de) + de);
				}
				sBufferi.append("|" + de + U.wrap(sValVals, bef, aft, de) + "\n");
				output(fw, sBufferi.toString());
			}
			output(fw, "End\n");
		}

		U.close(fw);
	}

	/**
	 * Save aggregated statistic results to csv files. The average results of
	 * those statistic lData with same parVars will be put in a group.
	 * 
	 * To note: statistic lData using different stat points will also be put
	 * together only if they share the same parVars.
	 * 
	 * @param fn
	 */
	public void saveAggregateToCsvFile(String fn) {
		if (fn != null) {
			outputAggregate(null, fn, "\"", "\"", ",");
		}
	}

	public void printAggregate() {
		outputAggregate(null, null, "", "", "\t");
	}

	public String toString() {
		return members + "";
	}

}
