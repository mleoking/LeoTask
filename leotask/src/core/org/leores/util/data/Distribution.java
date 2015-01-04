package org.leores.util.data;

import java.io.FileWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.leores.util.Logger;
import org.leores.util.U;
import org.leores.util.able.Processable1;
import org.leores.util.able.Processable2;

public class Distribution extends Logger implements Serializable {
	private static final long serialVersionUID = 3260826314035959813L;
	protected static double dDiscrete = 1E-10;
	public Integer nTotal;
	public Integer nCounted;
	public Double dMin;
	public Double dMax;
	public Double dInterval;
	public List<Double> data;
	protected List<DistElement> distElements;
	public String sPatNumOut;
	public Integer maxListOutSize; //the maximum output size of lists. default to be null: no limit.
	public String info;

	public static class DistElement implements Comparable, Serializable {
		private static final long serialVersionUID = 59637195809162373L;
		public Double dFromInclude;
		public Double dTo;
		public Integer n;
		public Double p;
		public List<Double> data;

		public int compareTo(Object o) {
			DistElement de = (DistElement) o;
			return dFromInclude.compareTo(de.dFromInclude);
		}
	}

	public Distribution(String sInfo) {
		nTotal = 0;
		nCounted = 0;
		dInterval = null;
		dMax = null;
		dMin = null;
		data = new ArrayList<Double>();
		distElements = new ArrayList<DistElement>();
		sPatNumOut = null;
		maxListOutSize = null;
		info = sInfo;
	}

	public Distribution() {
		this(null);
	}

	/**
	 * dInterval should not be smaller than or equal to 0!
	 * 
	 * @param dInterval
	 * @param dFrom
	 * @param dTo
	 * @return
	 */

	public boolean prepDistElements(Double dInterval, Double dFrom, Double dTo) {
		boolean rtn = false;
		if (dTo >= dFrom && dInterval > 0) {
			rtn = true;
			distElements.clear();
			this.dInterval = dInterval;
			int nElements = (int) ((dTo - dFrom) / dInterval + 1);
			((ArrayList) distElements).ensureCapacity(nElements);
			BigDecimal bd = new BigDecimal(dFrom);
			BigDecimal bdInterval = new BigDecimal(dInterval);
			BigDecimal bdTo = new BigDecimal(dTo);
			for (; bd.compareTo(bdTo) <= 0; bd = bd.add(bdInterval)) {
				DistElement de = new DistElement();
				de.dFromInclude = bd.doubleValue();
				de.dTo = bd.add(bdInterval).doubleValue();
				de.n = 0;
				de.p = 0.0;
				de.data = new ArrayList<Double>();
				distElements.add(de);
			}
		} else {
			log("Error dTo<dFrom! [dTo, dFrom]: ", dTo, dFrom);
		}
		return rtn;
	}

	public boolean prep(Double dInterval, Double dFrom, Double dTo) {
		nTotal = 0;
		nCounted = 0;
		this.dInterval = dInterval;
		this.dMin = dFrom;
		this.dMax = dTo;
		data = new ArrayList<Double>();
		distElements = new ArrayList<DistElement>();
		return prepDistElements(dInterval, dFrom, dTo);
	}

	public boolean put(Double dVal) {
		boolean rtn = false;
		if (dVal != null) {
			if (dMax == null || dVal > dMax) {
				dMax = dVal;
			}
			if (dMin == null || dVal < dMin) {
				dMin = dVal;
			}
			data.add(dVal);
			rtn = true;
			nTotal++;
		}
		return rtn;
	}

	public boolean statDiscrete(Double dVal) {
		boolean rtn = false;
		if (dVal != null) {
			rtn = true;
			boolean found = false;
			for (int i = 0, mi = distElements.size(); i < mi; i++) {
				DistElement de = distElements.get(i);
				if (dVal >= de.dFromInclude && dVal <= de.dTo) {
					de.n++;
					nCounted++;
					found = true;
					break;
				}
			}
			if (!found) {
				DistElement de = new DistElement();
				de.dFromInclude = dVal - dDiscrete;
				de.dTo = dVal + dDiscrete;
				de.n = 0;
				de.p = 0.0;
				distElements.add(de);
			}
		}
		return rtn;
	}

	/**
	 * This function does not put the columns into the columns list, just do
	 * statistics. The construction function Distribution(Double dInterval,
	 * Double dFrom, Double dTo) has to be used before using this function. This
	 * function will save lots of memory compared to the function of put.
	 * 
	 * Note: only use calDist() to get the distribution results after using
	 * stat!
	 * 
	 * @return
	 */
	public boolean stat(Double dVal) {
		boolean rtn = false;

		if (dVal != null) {
			if (dVal >= dMin && dVal <= dMax) {
				int id = (int) ((dVal - dMin) / dInterval);
				DistElement de = distElements.get(id);
				de.n++;
				nCounted++;
				//de.p = (double) de.n / nCounted;
			}
			nTotal++;
			rtn = true;
		}

		return rtn;
	}

	/**
	 * Only use this function after using stat(Double dVal) or
	 * statDiscrete(Double dVal).
	 * 
	 * @return
	 */

	public List<DistElement> calDist() {
		List<DistElement> rtn = distElements;
		Collections.sort(distElements);

		for (int i = 0, size = distElements.size(); i < size; i++) {
			DistElement de = distElements.get(i);
			de.p = (double) de.n / nCounted;
		}

		return rtn;
	}

	public int clear() {
		int rtn = data.size();
		data.clear();
		distElements.clear();
		nCounted = 0;
		nTotal = 0;
		return rtn;
	}

	public int getNTotal() {
		return nTotal;
	}

	public int getNCounted() {
		return nCounted;
	}

	public List<DistElement> calDist(Double dInterval, Double dFrom, Double dTo) {
		List<DistElement> rtn = null;

		if (dInterval != null && dInterval > 0 && nTotal > 0) {
			if (dFrom == null) {
				dFrom = dMin;
			}
			if (dTo == null) {
				dTo = dMax;
			}
			if (prepDistElements(dInterval, dFrom, dTo)) {
				nCounted = 0;
				for (int i = 0, size = data.size(); i < size; i++) {
					Double d = data.get(i);
					if (d >= dFrom && d <= dTo) {
						int id = (int) ((d - dFrom) / dInterval);
						DistElement de = distElements.get(id);
						de.n++;
						de.data.add(d);
						nCounted++;
					}
				}
				for (int i = 0, size = distElements.size(); i < size; i++) {
					DistElement de = distElements.get(i);
					de.p = (double) de.n / nCounted;
				}
				rtn = distElements;
			}
		} else {
			log("Wrong dInterval value: " + dInterval);
		}

		return rtn;
	}

	public List<DistElement> calDist(Double dInterval, Double dFrom) {
		return calDist(dInterval, dFrom, null);
	}

	public List<DistElement> calDist(Double dInterval) {
		return calDist(dInterval, null, null);
	}

	/**
	 * Automatically find a proper interval to separate the range of columns
	 * values
	 * into n parts
	 * 
	 * @param n
	 * @return
	 */
	public List<DistElement> calDistIntoNparts(Integer n) {
		List<DistElement> rtn = null;
		if (n >= 1 && nTotal > 0) {
			Double dInterval = (dMax - dMin) / n;
			if (dInterval == 0) {
				dInterval = 1.0 / 10000;
			} else {
				dInterval += dInterval / 10000;
			}
			rtn = calDist(dInterval);
		}
		return rtn;
	}

	public DataTable getDataTable(DataTable dataTable, int... iCols) {
		DataTable rtn = dataTable;
		if (distElements.size() > 0) {
			int[] iColsTU = iCols;
			if (iColsTU == null) {
				iColsTU = new int[] { 0, 1, 2, 3 };
			}
			if (rtn == null) {
				rtn = new DataTable(info, iColsTU.length);
			}
			String[] colNames = new String[iColsTU.length];
			String[] sDistCols = new String[] { "dFromInclude", "dTo", "n", "p" };
			for (int i = 0; i < iColsTU.length; i++) {
				colNames[i] = sDistCols[iColsTU[i]];
			}
			rtn.setColNames(colNames);
			for (int i = 0, mi = distElements.size(); i < mi; i++) {
				DistElement dei = distElements.get(i);
				Object[] oDistCols = new Object[] { dei.dFromInclude, dei.dTo, dei.n, dei.p };
				Object[] row = new Object[iColsTU.length];
				for (int j = 0; j < iColsTU.length; j++) {
					row[j] = oDistCols[iColsTU[j]];
				}
				rtn.add(row);
			}
		}
		return rtn;
	}

	public DataTable getDataTable() {
		return getDataTable(null, 0, 3);
	}

	protected void output(FileWriter fw, String str) {
		if (!U.append(fw, str)) {
			log(false, str);
		}
	}

	@SuppressWarnings("all")
	public void output(String fn, String before, String after, String delimiter) {
		Processable1 pa1 = new Processable1.MaxListOutSize(maxListOutSize);
		boolean bOutClass = true;
		String outString = U.toOutString(this, before, after, delimiter, sPatNumOut, bOutClass, pa1);
		FileWriter fw = U.createFileWriter(fn);
		output(fw, outString);
		for (int i = 0, mi = distElements.size(); i < mi; i++) {
			DistElement dei = distElements.get(i);
			outString = U.toOutString(dei, before, after, delimiter, sPatNumOut, bOutClass, pa1);
			output(fw, outString);
			bOutClass = false;
		}
		output(fw, U.wrap(before, after, delimiter, "End") + "\n");
		U.close(fw);
	}

	public void saveToCSVFile(String fn) {
		if (fn != null) {
			output(fn, "\"", "\"", ",");
		}
	}

	public void print() {
		output(null, "", "", "\t");
	}
}
