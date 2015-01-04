package org.leores.util.data;

import java.io.FileWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.math.RoundingMode;

import org.leores.util.Logger;
import org.leores.util.U;

public class MaxMinAvg<D, V, P> extends Logger implements Serializable {
	private static final long serialVersionUID = -8843593173537608047L;

	public static class ValPar<V, P> implements Comparable<ValPar<V, P>>, Serializable {
		private static final long serialVersionUID = -6865012104095781653L;
		public Comparable<V> value;
		public P parameter;
		public static String sPatNumOut = null;

		public ValPar(Comparable<V> value, P parameter) {
			this.value = value;
			this.parameter = parameter;
		}

		public int compareTo(ValPar<V, P> o) {
			return value.compareTo((V) o.value);
		}

		public String toString() {
			String sValue = U.valToStr(value, sPatNumOut);
			String sParameter = U.valToStr(parameter, sPatNumOut);
			return "(V:" + sValue + ", P:" + sParameter + ")";
		}
	}

	public static class MaxMinAvgElement<D, V, P> implements Comparable<MaxMinAvgElement<D, V, P>>, Serializable {
		private static final long serialVersionUID = 5852727894099412950L;
		public Comparable<D> data;
		public List<ValPar<V, P>> lMax;
		public List<ValPar<V, P>> lMin;
		public BigDecimal vAvg;
		public BigDecimal vSum;
		public Integer vN;
		public String valName;
		public String parName;
		public Integer nMaxMin;

		public MaxMinAvgElement(Comparable<D> data, String valName, String parName, Integer nMaxMin) {
			this.data = data;
			lMax = new ArrayList<ValPar<V, P>>(nMaxMin + 1);
			lMin = new ArrayList<ValPar<V, P>>(nMaxMin + 1);
			vAvg = null;
			vSum = null;
			vN = 0;
			this.valName = valName;
			this.parName = parName;
			this.nMaxMin = nMaxMin;
		}

		public String toStrClass() {
			StringBuffer sBuffer = new StringBuffer();
			String bef = "\"";
			String aft = "\"";
			String de = ",";

			String[] classInfo = { "Class", "MaxMinElement", "DataClass", data.getClass().getName(), "ValName", valName, "ParName", parName, "nMaxMin", nMaxMin.toString() };
			String[] fieldInfo1 = { "Max", "Min", "Avg", "N" };
			String[] fieldInfo2 = U.toStrArray(data.getClass());

			sBuffer.append(U.wrap(classInfo, bef, aft, de) + "\n");
			sBuffer.append(U.wrap(fieldInfo1, bef, aft, de) + de);
			sBuffer.append(U.wrap(fieldInfo2, bef, aft, de) + "\n");

			return sBuffer.toString();
		}

		public String toStrData() {
			StringBuffer sBuffer = new StringBuffer();
			String bef = "\"";
			String aft = "\"";
			String de = ",";

			String[] fieldValue1 = { lMax.toString(), lMin.toString(), vAvg + "", vN + "" };
			String[][] dataStrArray = U.toStrArray(data);

			sBuffer.append(U.wrap(fieldValue1, bef, aft, de));
			if (dataStrArray != null) {
				sBuffer.append(de + U.wrap(dataStrArray[1], bef, aft, de));
			}
			sBuffer.append("\n");

			return sBuffer.toString();
		}

		public boolean put(ValPar<V, P> valpar) {
			boolean rtn = false;

			if (valpar != null) {
				vN++;
				lMax.add(valpar);
				if (lMax.size() > nMaxMin) {
					U.removeMinElement(lMax);
				}
				lMin.add(valpar);
				if (lMin.size() > nMaxMin) {
					U.removeMaxElement(lMin);
				}
				V value = (V) valpar.value;
				if (value != null) {
					BigDecimal bdValue = new BigDecimal(value.toString());
					if (vSum == null) {
						vSum = bdValue;
					} else {
						vSum = vSum.add(bdValue);
					}
					vAvg = vSum.divide(new BigDecimal(vN), 10, RoundingMode.HALF_UP);
				}

			}

			return rtn;
		}

		public void sort() {
			Collections.sort(lMax);
			Collections.sort(lMin);
		}

		public int compareTo(MaxMinAvgElement<D, V, P> o) {
			return data.compareTo((D) o.data);
		}

		public boolean hasData(Comparable<D> d) {
			return data.compareTo((D) d) == 0;
		}
	}

	public Integer nMaxMin;
	public List<MaxMinAvgElement<D, V, P>> elements;
	public String sPatNumOut;
	public String valName;
	public String parName;

	public MaxMinAvg(Integer nMaxMin, String valName, String parName, String sPatNumOut) {
		this.nMaxMin = nMaxMin;
		this.elements = new ArrayList<MaxMinAvgElement<D, V, P>>();
		this.sPatNumOut = sPatNumOut;
		this.valName = valName;
		this.parName = parName;
	}

	public MaxMinAvg(Integer nMaxMin, String sPatNumOut) {
		this(nMaxMin, null, null, sPatNumOut);
	}

	public MaxMinAvg(Integer nMaxMin) {
		this(nMaxMin, null, null, null);
	}

	public boolean put(Comparable<D> data, ValPar<V, P> valpar) {
		boolean rtn = false;

		if (nMaxMin > 0 && data != null && valpar != null) {
			MaxMinAvgElement<D, V, P> tMME = null;
			for (int i = 0, size = elements.size(); i < size; i++) {
				MaxMinAvgElement<D, V, P> mme = elements.get(i);
				if (mme.hasData(data)) {
					tMME = mme;
					break;
				}
			}

			if (tMME == null) {
				tMME = new MaxMinAvgElement<D, V, P>(data, valName, parName, nMaxMin);
				elements.add(tMME);
			}

			rtn = tMME.put(valpar);
		}

		return rtn;
	}

	public void sort(boolean bSortData) {
		if (bSortData) {
			Collections.sort(elements);
		}
		for (int i = 0, size = elements.size(); i < size; i++) {
			MaxMinAvgElement<D, V, P> mme = elements.get(i);
			mme.sort();
		}
	}

	public boolean saveToCsvFile(String fn) {
		boolean rtn = false;

		if (fn != null) {
			String oldNOP = ValPar.sPatNumOut;
			ValPar.sPatNumOut = sPatNumOut;
			boolean elementClassSaved = false;
			FileWriter fw = U.createFileWriter(fn);
			for (int i = 0, size = elements.size(); i < size; i++) {
				MaxMinAvgElement<D, V, P> mme = elements.get(i);
				if (!elementClassSaved) {
					U.append(fw, mme.toStrClass());
					elementClassSaved = true;
				}
				U.append(fw, mme.toStrData());
			}
			ValPar.sPatNumOut = oldNOP;
			rtn = true;
			U.close(fw);
		}

		return rtn;
	}
}
