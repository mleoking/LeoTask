package org.leores.util.data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.leores.ecpt.TRuntimeException;
import org.leores.plot.JGnuplot;
import org.leores.plot.JGnuplot.Plot;
import org.leores.util.Logger;
import org.leores.util.ObjArray;
import org.leores.util.U;

public class Statistic extends Logger implements Serializable, Cloneable {
	private static final long serialVersionUID = -5744032469179578813L;
	protected static String sEnd = "End";

	public String keyVar;
	public String valVar;
	public String valid;
	public String[] parVars;
	public Integer initCapacity;
	public String sPatNumOut;
	public Boolean bUniqueKeys; //whether reject columns with a duplicate key. default to be true.
	public Integer maxListOutSize; //the maximum output size of lists. default to be null: no limit.
	/**
	 * This is the hashLevel for StatElement.parVals. The default value is 10.
	 * When hashLevel is -1 or 2, it is unacceptable to have parameter values
	 * that contains itself as an element, either directly or indirectly. The
	 * behaviors of such situations are undefined.<br>
	 * <br>
	 * 
	 * <b>-1:</b> do not use hash, use for loop to go through all elements to
	 * find proper Statement, <br>
	 * <b>0:</b> use obj reference to hash (this option should almost be never
	 * used), 1: use array content to hash: Arrays.hashCode(), <br>
	 * <b>2:</b> use array deep content to hash: Arrays.deepHashCode().<br>
	 * <b>3:</b> convert the array to a string and do string.hashCode().<br>
	 * <b>10:</b> convert the array to a string and do string.hashCode() when
	 * array.length>1, when array.length=1, do Arrays.deepHashCode().
	 **/
	public Integer hashLevel;

	public Map<Object, Boolean> keyMap;
	public List<Object> keyVals;
	public List<StatElement> elements;
	protected Map<ObjArray, StatElement> elementMap;

	public Integer nMerged;
	public String info;

	//hashLevel should not be changed after starting conducting statistics. 

	public Statistic() {
		keyVar = null;
		valVar = null;
		valid = null;
		parVars = null;
		initCapacity = null;
		sPatNumOut = null;
		info = null;
		/*
		 * For bUniqueKeys, 'null' mean the default value. null is interpreted
		 * as true when bUniqueKeys is not set. bUniqueKeys is set to be null
		 * initially rather than 'true' because we need to distinguish whether
		 * bUniqueKeys is manually set to be true or not in Statistics's
		 * afterLoadObj
		 */
		bUniqueKeys = null;
		maxListOutSize = null;
		hashLevel = 10;
		logRecordLevel = LOG_WARNING;
		nMerged = 0;

		afterLoadObj("Statistic");
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

	/**
	 * This function will be automatically called by ObjUtil.loadFromXML and
	 * Statistics.afterLoadObj
	 * 
	 * @param sLoadMethod
	 */

	public void afterLoadObj(String sLoadMethod) {
		int capacity = 128;
		if (initCapacity != null) {
			capacity = initCapacity;
		}

		keyMap = null;
		keyVals = null;
		elements = null;
		elementMap = null;

		keyVals = new ArrayList<Object>(capacity);
		keyMap = new HashMap<Object, Boolean>(capacity);
		elements = new ArrayList<StatElement>(capacity);
		elementMap = new HashMap<ObjArray, StatElement>(capacity);

		if (info != null && valVar != null) {
			if (!valVar.contains(U.sEnumerate)) {
				if (!info.contains("%" + valVar)) {
					info += "%" + valVar;
				}
				String sParVars = U.wrap(parVars, "", "", ";");
				if (sParVars != null && !info.contains("%" + sParVars)) {
					info += "%" + sParVars;
				}
			}
		}
	}

	/**
	 * This function will be automatically called by ObjUtil.SaveToFile
	 * 
	 * @param sSaveMethod
	 */

	public void beforeSaveObj(String sSaveMethod) {
		return;
	}

	/**
	 * Create a new clone of the statistic. The new cloned statistic has the
	 * same parameters as the original one but is with empty columns.
	 * 
	 * @return
	 */

	public Statistic newClone(String sValVar) {
		Statistic rtn = null;
		try {
			rtn = (Statistic) super.clone();
			if (parVars != null) {
				U.copy(rtn, this, "parVars");
			}
			rtn.nMerged = 0;
			rtn.log = "";
			if (sValVar != null) {
				rtn.valVar = sValVar;
			}
			rtn.afterLoadObj("newClone");
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return rtn;
	}

	/**
	 * Create a new clone of the statistic. The new cloned statistic has the
	 * same parameters as the original one but is with empty columns.
	 * 
	 * @return
	 */

	public Statistic newClone() {
		return newClone(null);
	}

	/**
	 * A Statistic with splitable valVar can not be used directly. It has to be
	 * splited and be added to a Statistics.
	 * 
	 * @return
	 */
	public List<Statistic> split() {
		List<Statistic> rtn = null;

		if (valVar != null) {
			List<String> valVars = U.parseList(String.class, valVar);
			int mi = valVars.size();
			if (mi > 1) {
				rtn = new ArrayList<Statistic>();
				for (int i = 0; i < mi; i++) {
					String valVari = valVars.get(i);
					Statistic stati = newClone(valVari);
					rtn.add(stati);
				}
			} else if (mi == 1) {//Remove the trailing ; or ,
				valVar = valVars.get(0);
			}
		}

		return rtn;
	}

	public static class StatElement implements Comparable<StatElement>, Serializable {
		private static final long serialVersionUID = 2083565234594966145L;
		public Statistic tStat;
		public ObjArray parVals;
		public List keyVals;
		public Object vMax, vKeyMax;//vKeyMax is the key of vMax rather than the maximum key.
		public Object vMin, vKeyMin;
		public Object vAvg;
		public BigDecimal vSum;
		public BigDecimal vStd;//Uncorrected sample standard deviation 
		public Integer vN;//The count of NUMBER values when there is at least one. When there is no NUMBER values, vN equals the count of all values. 
		public List vals;
		protected boolean bUptodate;
		public static MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

		public StatElement(ObjArray parVals, Statistic stat) {
			tStat = stat;
			this.parVals = parVals;
			keyVals = new ArrayList();
			vals = new ArrayList();
			bUptodate = true;
		}

		public int compareTo(StatElement eStat) {
			int rtn = 1;
			if (eStat != null) {//if eStat == null returns 1
				rtn = U.compare(this.parVals, eStat.parVals);
			}
			return rtn;
		}

		public boolean hasSameParVals(StatElement eStat) {
			boolean rtn = compareTo(eStat) == 0;
			return rtn;
		}

		/**
		 * null elements in parVals will not compare and be jumped over.
		 * 
		 * @param parVals
		 * @return
		 */
		public boolean match(ObjArray parVals) {
			boolean rtn = false;
			if (this.parVals == null) {
				rtn = parVals == null;
			} else if (parVals != null && this.parVals.length() == parVals.length()) {
				rtn = true;
				for (int i = 0, mi = parVals.length(); i < mi; i++) {
					Object oVal0 = this.parVals.get(i);
					Object oVal1 = parVals.get(i);
					if (oVal1 != null && U.compare(oVal0, oVal1) != 0) {
						rtn = false;
						break;
					}
				}
			}
			return rtn;
		}

		public boolean has(ObjArray parVals) {
			boolean rtn = U.compare(this.parVals, parVals) == 0;
			return rtn;
		}

		public void sort() {
			if (keyVals.size() == vals.size()) {
				U.sortValKeyLists(vals, keyVals);
			} else {
				try {
					Collections.sort(vals);
				} catch (NullPointerException e) {
					tStat.log(LOG_ERROR, tStat.info + ": null values found!");
				}
			}
		}

		protected Object key(int i) {
			Object rtn = null;
			if (keyVals.size() == vals.size()) {
				rtn = keyVals.get(i);
			}
			return rtn;
		}

		protected BigDecimal genBigDecimal(String str) {
			return (BigDecimal) U.newInstance(BigDecimal.class, str);
		}

		public void update() {
			if (!bUptodate) {
				List<Integer> lValidId = new ArrayList<Integer>();
				vMax = null;
				vMin = null;
				vAvg = null;
				vSum = null;
				vStd = null;
				vN = 0;

				vKeyMax = null;
				vKeyMin = null;

				int mi = vals.size();
				if (mi > 0) {
					Object oVal = vals.get(0);
					BigDecimal bdVal = genBigDecimal(oVal + "");
					if (bdVal != null) {
						oVal = bdVal;
					}
					vMin = oVal;
					vMax = oVal;
					vKeyMax = key(0);
					vKeyMin = key(0);
				}
				for (int i = 0; i < mi; i++) {
					Object oVal = vals.get(i);
					BigDecimal bdVal = genBigDecimal(oVal + "");
					if (bdVal != null) {
						oVal = bdVal;
					}

					if (U.compare(vMax, oVal) < 0) {
						vMax = oVal;
						vKeyMax = key(i);
					}
					if (U.compare(vMin, oVal) > 0) {
						vMin = oVal;
						vKeyMin = key(i);
					}

					if (bdVal != null) {
						if (U.bValidNumber(bdVal)) {
							if (vSum != null) {
								vSum = vSum.add(bdVal);
							} else {
								vSum = bdVal;
							}
							vN++;
							lValidId.add(i);
						} else {
							tStat.log(LOG_WARNING, tStat.info + ": invalid number excluded from the statistics [" + oVal + "] for the keyVal [" + key(i) + "]");
						}
					} else if (vSum != null) {
						tStat.log(LOG_WARNING, tStat.info + ": invalid number excluded from the statistics [" + oVal + "] for the keyVal [" + key(i) + "]");
					}
				}

				if (vSum != null && vN > 0) {
					if (vN > 1) {
						BigDecimal vSquareDiffSum = new BigDecimal(0);
						vAvg = vSum.divide(new BigDecimal(vN), mc);
						for (int i = 0; i < vN; i++) {
							int id = lValidId.get(i);
							Object val = vals.get(id);
							BigDecimal bdVal = (BigDecimal) U.newInstance(BigDecimal.class, val + "");
							if (bdVal != null) {
								BigDecimal bd1 = ((BigDecimal) vAvg).subtract(bdVal);
								bd1 = bd1.multiply(bd1);
								vSquareDiffSum = vSquareDiffSum.add(bd1);
							}
						}
						//Uncorrected sample standard deviation
						vSquareDiffSum = vSquareDiffSum.divide(new BigDecimal(vN), mc);
						vStd = new BigDecimal(Math.sqrt(vSquareDiffSum.doubleValue()));
					} else {//vN == 1
						vAvg = vSum;
						vStd = new BigDecimal(0);
					}
				}

				if (vAvg == null && vMax != null && vMin != null && U.compare(vMax, vMin) == 0) {
					vAvg = vMax;
				}

				if (vN == 0) {
					vN = vals.size();
				}

				bUptodate = true;
			}
		}

		public boolean put(Object keyVal, Object val) {
			if (keyVal != null) {
				keyVals.add(keyVal);
			}
			vals.add(val);
			bUptodate = false;
			return true;
		}

		public boolean merge(StatElement eStat) {
			boolean rtn = false;

			if (eStat != null) {
				rtn = true;
				keyVals.addAll(eStat.keyVals);
				vals.addAll(eStat.vals);
				bUptodate = false;
			}

			return rtn;
		}

		public String toStrData(String bef, String aft, String de) {
			update();

			StringBuffer sBuffer = new StringBuffer();
			String[] varValues = tStat.strs(vMax, vMin, vStd, vAvg, vN, vKeyMax, vKeyMin, vals, keyVals);
			if (parVals != null && parVals.length() > 0) {
				String[] parValues = U.toStrArray(parVals.asList(), tStat.sPatNumOut);
				sBuffer.append(U.wrap(parValues, bef, aft, de) + de);
			}
			sBuffer.append("|" + de + U.wrap(varValues, bef, aft, de) + "\n");

			return sBuffer.toString();
		}
	}

	/**
	 * This function can still guarantee the correctness of the found element
	 * when there is a hash collision. Because, when there is a collision, the
	 * function will go through all elements to find the the one with paraVals.
	 * The collision should be a small probability event.
	 * 
	 * @param parVals
	 * @return
	 */

	public StatElement findStatElement(ObjArray parVals) {
		StatElement rtn = null;
		boolean bHashCollision = false;

		if (hashLevel > -1) {
			rtn = elementMap.get(parVals);
			if (rtn != null && !rtn.has(parVals)) {
				bHashCollision = true;
				/**
				 * This warning happens when there is a collision (same value)
				 * of hashcode for different parVals.
				 */
				//log(LOG_WARNING, "Statistic: Hashcode collision for " + rtn.parVals + ":" + rtn.parVals.hashCode() + " and " + parVals + ":" + parVals.hashCode());
				//rtn = null;
			}
		}

		/**
		 * If there is a hash collision found or hashLevel<=-1, the function
		 * will search all elements to find the StateElement with the parVals.
		 */
		if (hashLevel <= -1 || bHashCollision) {
			if (bHashCollision) {
				log(LOG_WARNING, "Statistic: Solved a hash collision [parVars, oldParVals, newParVals, hash]:" + U.toStr(U.asList(parVars), rtn.parVals, parVals, U.hash(parVals)));
			}
			rtn = null;
			for (int i = 0, size = elements.size(); i < size; i++) {
				StatElement eStati = elements.get(i);
				if (eStati.has(parVals)) {
					rtn = eStati;
					break;
				}
			}
		}

		return rtn;
	}

	public boolean addStatElement(StatElement eStat) {
		boolean rtn = false;

		if (eStat != null) {
			ObjArray parVals = eStat.parVals;
			rtn = true;
			elements.add(eStat);
			if (hashLevel > -1) {
				StatElement preEStat = elementMap.put(parVals, eStat);
				if (preEStat != null) {
					/**
					 * This will happen when there is a hashcode collision for
					 * two different parVals. This collision is solvable in
					 * <b>findStatElement</b> as it search all elements instead
					 * when a hashcode collision is found.
					 */
					//log(LOG_WARNING, "Statistic: Hashcode collision found for [preParVals, newParVals]:" + U.toStr(preEStat.parVals, parVals));
					//rtn = false;
				}
			}
		}

		return rtn;
	}

	public boolean bUniqueKeys() {
		return (bUniqueKeys == null || bUniqueKeys == true);
	}

	/**
	 * Put the columns obj into the statistic pool. The valVar and parVars of
	 * the obj have to be comparable!
	 * 
	 * @param obj
	 * @return
	 */
	public boolean put(Object data) {
		boolean rtn = false;

		Object keyVal = null, val = null;
		ObjArray parVals = null;
		boolean bAccepted = true;
		if (keyVar != null && !"".equals(keyVar)) {
			keyVal = U.getFieldValue(data, keyVar, U.modAll);
			//Here we have to use the function bUniqueKeys() rather than bUniqueKeys variable because bUniqueKeys could be null.
			if (bUniqueKeys() && keyMap.containsKey(keyVal)) {
				bAccepted = false;
				log(LOG_WARNING, info + ": Reject Duplicate Key [" + keyVal + "]");
			} else {
				keyVals.add(keyVal);
				keyMap.put(keyVal, true);
			}

		}
		if (bAccepted) {
			boolean bValid = true;
			if (valid != null) {
				bValid = U.evalCheck(valid, data);
			}
			if (bValid) {
				Object[] parValsObjs = U.getFieldValues(data, parVars, U.modAll);
				if (parValsObjs != null) {//when parVars is not set, parValsObjs will be null
					parVals = new ObjArray(parValsObjs, hashLevel);
				}
				StatElement eStat = findStatElement(parVals);

				if (eStat == null) {
					eStat = new StatElement(parVals, this);
					addStatElement(eStat);
				}

				if (valVar != null && !"".equals(valVar)) {
					try {
						val = U.getFieldValue(data, valVar, U.modAll);
					} catch (TRuntimeException e) {
						log(LOG_ERROR, info + ": " + e.getMessage());
					}
				} else {
					val = data;
				}

				rtn = eStat.put(keyVal, val);
			}
		}

		return rtn;
	}

	/**
	 * The two statistics' info must be exactly the same. The two
	 * statistics' parVars and valVar must be the same.
	 * 
	 * @param stat
	 * @return
	 */
	public boolean bMergeable(Statistic stat) {
		boolean rtn = false;

		if (stat != null) {
			//The two statistics' info must at least one contains another.
			if (info == null && stat.info == null) {
				rtn = true;
			} else if (info != null && stat.info != null) {
				rtn = info.equals(stat.info);
			}
			//The two statistics' parVars and valVar must be the same.
			rtn = rtn && (U.compare(parVars, stat.parVars) == 0) && (U.compare(valVar, stat.valVar) == 0);
		}

		return rtn;
	}

	/**
	 * The uniqueness of the key value is not guaranteed when use this merge,
	 * even if <b>bUniqueKeys</b> is set to be true. <br>
	 * <br>
	 * 
	 * Two statistics will only be merged if one of their <b>info</b> contains
	 * another one's <b>info</b> and they have the same <b>parVars</b> and
	 * <b>valVar</b>.
	 * 
	 * @param stat
	 * @return
	 */

	public boolean merge(Statistic stat) {
		boolean rtn = false;

		if (bMergeable(stat)) {
			rtn = true;
			for (int i = 0, size = stat.elements.size(); i < size; i++) {
				StatElement eStati = stat.elements.get(i);
				StatElement eStat = findStatElement(eStati.parVals);
				if (eStat == null) {
					eStati.tStat = null;
					eStati.tStat = this;
					addStatElement(eStati);
				} else {
					eStat.merge(eStati);
				}
			}
			keyVals.addAll(stat.keyVals);
			keyMap.putAll(stat.keyMap);
			nMerged += stat.nMerged + 1;
			log += stat.log;
		}

		return rtn;
	}

	public void sort(boolean bSortInElement) {
		Collections.sort(elements);
		if (bSortInElement) {
			for (int i = 0, size = elements.size(); i < size; i++) {
				StatElement eStat = elements.get(i);
				eStat.sort();
			}
		}
	}

	/**
	 * Get average values whose parameters matches <b>columns</b>'s parameters.
	 * 
	 * @param bSortElement
	 *            whether sort elements according to their parameter vals.
	 * @param lParVals
	 *            if lParVals!=null the parameter values corresponding to the
	 *            returned average values will be added in this list
	 *            (lParVals).
	 * @param columns
	 *            whose parameters values are used to match.
	 * @return
	 */
	public List<Object> getAvgVals(List<ObjArray> lParVals, Object data, boolean bSortElement) {
		List<Object> rtn = new ArrayList<Object>();

		if (bSortElement) {
			sort(false);
		}

		for (int i = 0, mi = elements.size(); i < mi; i++) {
			StatElement eStat = elements.get(i);
			if (data != null && parVars != null) {
				Object[] parValsObjs = U.getFieldValues(data, parVars, U.modAll);
				ObjArray parVals = new ObjArray(parValsObjs, hashLevel);
				if (!eStat.match(parVals)) {
					continue;
				}
			}
			eStat.update();
			rtn.add(eStat.vAvg);
			if (lParVals != null) {
				lParVals.add(eStat.parVals);
			}
		}

		return rtn;
	}

	/**
	 * Get average values sorted according to their corresponding parameter
	 * values.
	 * 
	 * @return
	 */
	public List<Object> getAvgVals() {
		return getAvgVals(null, null, true);
	}

	public String o2s(Object obj) {
		String rtn = null;

		if (obj == null || U.bAssignable(String.class, obj.getClass())) {
			rtn = obj + "";
		} else if (U.bAssignable(List.class, obj.getClass())) {
			List lObj = (List) obj;
			if (maxListOutSize != null && maxListOutSize >= 0 && maxListOutSize < lObj.size()) {
				List lOut = lObj.subList(0, maxListOutSize);
				rtn = U.valToStr(lOut, sPatNumOut) + "...";
			} else {
				rtn = U.valToStr(lObj, sPatNumOut);
			}
		} else {
			rtn = U.valToStr(obj, sPatNumOut);
		}

		return rtn;
	}

	public String[] strs(Object... objs) {
		String[] rtn = null;

		if (objs != null) {
			rtn = new String[objs.length];
			for (int i = 0; i < objs.length; i++) {
				rtn[i] = o2s(objs[i]);
			}
		}

		return rtn;
	}

	public String toStrClass(String bef, String aft, String de) {
		StringBuffer sBuffer = new StringBuffer();

		String[] classInfo1 = { "Class", "keyVar", "valVar", "valid", "bUniqueKeys", "keyN", "keyVals", "maxListOutSize", "sPatNumOut", "hashLevel", "nMerged", "log", "info" };
		String[] classInfo2 = strs(this.getClass().getName(), keyVar, valVar, valid, bUniqueKeys, keyVals.size(), keyVals, maxListOutSize, sPatNumOut, hashLevel, nMerged, log,
				Statistics.removeStatMethods(info));
		String[] valInfo = { "Max-" + valVar, "Min-" + valVar, "Std-" + valVar, "Avg-" + valVar, "N-" + valVar, keyVar + "-Max-" + valVar, keyVar + "-Min-" + valVar, "Vals-" + valVar,
				"Vals-" + keyVar };

		sBuffer.append(U.wrap(classInfo1, bef, aft, de) + "\n");
		sBuffer.append(U.wrap(classInfo2, bef, aft, de) + "\n");
		if (parVars != null && parVars.length > 0) {
			sBuffer.append(U.wrap(parVars, bef, aft, de) + de);
		}
		sBuffer.append("|" + de + U.wrap(valInfo, bef, aft, de) + "\n");
		return sBuffer.toString();
	}

	public void output(String fn, String bef, String aft, String de, boolean bSortElement, boolean bSortInElement) {
		beforeSaveObj("output");
		if (bSortElement) {
			sort(bSortInElement);
		}
		if (fn != null) {
			try {
				FileWriter fw = new FileWriter(U.createIfNotExist(fn), true);
				fw.append(toStrClass(bef, aft, de));
				for (int i = 0, size = elements.size(); i < size; i++) {
					StatElement eStat = elements.get(i);
					fw.append(eStat.toStrData(bef, aft, de));
				}
				fw.append("End\n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				log(e);
			}
		} else {
			log(false, toStrClass(bef, aft, de));
			for (int i = 0, size = elements.size(); i < size; i++) {
				StatElement eStat = elements.get(i);
				log(false, eStat.toStrData(bef, aft, de));
			}
			log(false, "End\n");
		}

	}

	public void output(String fn, String bef, String aft, String de) {
		output(fn, bef, aft, de, true, false);
	}

	public void saveToCsvFile(String fn) {
		if (fn != null) {
			output(fn, "\"", "\"", ",");
		}
	}

	public void print() {
		output(null, "", "", "\t");
	}

	/**
	 * 
	 * @param dataTable
	 * @param data
	 *            If data!=null then only return those records whose parVars
	 *            value match data's. null in parVars in data is ignored (match
	 *            all value for that par).
	 * @param pa2ProcessList
	 * @param iPars
	 *            null means all pars.
	 * @return
	 */
	public DataTable getDataTable(DataTable dataTable, Object data, int... iPars) {
		DataTable rtn = dataTable;
		List<ObjArray> lParVal = new ArrayList<ObjArray>();
		List lAvgVal = getAvgVals(lParVal, data, true);
		if (lAvgVal.size() > 0) {
			int nColumns = 1;
			int[] iParsTU = null;
			if (parVars != null) {
				iParsTU = iPars;
				if (iParsTU == null) {
					iParsTU = new int[parVars.length];
					for (int i = 0; i < iParsTU.length; i++) {
						iParsTU[i] = i;
					}
				}
				nColumns = iParsTU.length + 1;
				if (rtn == null) {
					rtn = new DataTable(valVar, nColumns);
				}
				String[] colNames = new String[nColumns];
				for (int i = 0; i < nColumns - 1; i++) {
					colNames[i] = parVars[iParsTU[i]];
				}
				colNames[nColumns - 1] = valVar;
				rtn.setColNames(colNames);
			}
			for (int i = 0, mi = lAvgVal.size(); i < mi; i++) {
				Object[] row = new Object[nColumns];
				if (parVars != null) {
					ObjArray oa = lParVal.get(i);
					for (int j = 0; j < nColumns - 1; j++) {
						row[j] = oa.get(iParsTU[j]);
					}
				}
				row[nColumns - 1] = lAvgVal.get(i);
				rtn.add(row);
			}
		} else {
			rtn = null;
		}

		return rtn;
	}

	public DataTable getDataTable(int... iPars) {
		return getDataTable(null, null, iPars);
	}

	/**
	 * 
	 * @param plot
	 * @param data
	 *            If data!=null then only return those records whose parVars
	 *            value match data's. null in parVars in data is ignored (match
	 *            all value for that par).
	 * @param pa2ProcessList
	 * @param iPars
	 * @return
	 */
	public Plot getPlot(Plot plot, Object data, int... iPars) {
		Plot rtn = plot;
		DataTable dt = getDataTable(null, data, iPars);
		if (dt != null) {
			String sInfo = Statistics.removeStatMethods(info);
			if (rtn == null) {
				rtn = new Plot(sInfo);
			}
			DataTableSet dts = rtn.getDataTableSet(0);
			if (dts == null) {
				dts = rtn.addNewDataTableSet(null);
			}
			dts.add(dt);
			rtn.autoSetLabels();
		} else {
			rtn = null;
		}
		return rtn;
	}

	public Plot getPlot(int... iPars) {
		return getPlot(null, null, iPars);
	}

	public Plot plot(JGnuplot jGnuplot, String sFile, String sLoad) {
		Plot rtn = getPlot(null, null, null);
		if (rtn != null) {
			JGnuplot jg = jGnuplot;
			if (jg == null) {
				jg = new JGnuplot();
				jg.sPatNumOut = sPatNumOut;
			}
			if (U.bLoadString(sLoad)) {
				U.loadFromString(jg, sLoad, false);
				U.loadFromString(rtn, sLoad, false);
			}
			if (sFile != null) {
				jg.compile(rtn, null, sFile);
			} else {
				jg.execute(rtn, null);
			}
		}
		return rtn;
	}

	public Plot plot() {
		return plot(null, null, null);
	}

	public Plot plot(String sFPreFix, String sLoad) {
		return plot(null, null, sLoad);
	}

	public Plot plt(String sFPreFix, String sLoad) {
		String sInfo = Statistics.removeStatMethods(info);
		String sFile = sFPreFix + sInfo + ".plt";
		return plot(null, sFile, sLoad);
	}

	public void saveToPltFile(String sFile, String sLoad) {
		plot(null, sFile, sLoad);
	}

	public void saveToPltFile(String sFile) {
		plot(null, sFile, null);
	}

	public String toString() {
		return info;
	}
}
