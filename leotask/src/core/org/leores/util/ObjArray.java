package org.leores.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This Class Wrap the array class to provide additional function:
 * 
 * 1) hashCode based on content. 3) equals based on hashCode. 2) compareTo
 * function using U.compare().
 * 
 * 
 * @author Changwang Zhang
 * 
 */

public class ObjArray extends Logger implements Comparable<ObjArray>, Serializable {
	private static final long serialVersionUID = -7991674869729544002L;

	public static class ObjArrayComparator<A> implements Comparator<A[]> {
		public boolean bAscending;

		public ObjArrayComparator(boolean bAscending) {
			this.bAscending = bAscending;
		}

		public ObjArrayComparator() {
			this(true);
		}

		public int compare(A[] o1, A[] o2) {
			int rtn = U.compare(o1, o2);
			if (!bAscending) {
				rtn = -rtn;
			}
			return rtn;
		}

	}

	protected Object[] objs;
	protected Integer hashLevel;
	//When hashLevel==2 and objs.length<minLength, hashCode() will calculate doubled objs to avoid conflicts when objs.length is small.
	//To avoid this feature set minLength=0 ;
	protected Integer minLength;

	public ObjArray(Object[] objs, Integer hashLevel, Integer minLength) {
		this.objs = objs;
		this.hashLevel = hashLevel;
		this.minLength = minLength;
	}

	public ObjArray(Object[] objs, Integer hashLevel) {
		this(objs, hashLevel, 0);
	}

	public ObjArray(Object[] objs) {
		this(objs, 1, 0);
	}

	public static String[] toStrArray(Object... objs) {
		String[] rtn = new String[objs.length];
		for (int i = 0; i < rtn.length; i++) {
			rtn[i] = objs[i] + "";
		}
		return rtn;
	}

	public static String toStr(Object... objs) {
		StringBuffer sBuff = new StringBuffer();
		for (int i = 0; i < objs.length; i++) {
			sBuff.append(objs[i] + ",");
		}
		return sBuff.toString();
	}

	public int hashCode() {
		int rtn = 0;
		Object[] objsToHash = objs;
		if (objs != null && objs.length < minLength) {
			objsToHash = new Object[objs.length * 3];
			System.arraycopy(objs, 0, objsToHash, 0, objs.length);
			System.arraycopy(objs, 0, objsToHash, objs.length - 1, objs.length);
			System.arraycopy(objs, 0, objsToHash, 2 * objs.length - 1, objs.length);
		}
		String str;
		switch (hashLevel) {
		case 0:
			rtn = objsToHash.hashCode();
			break;
		case 1:
			rtn = Arrays.hashCode(objsToHash);
			break;
		case 2:
			rtn = Arrays.deepHashCode(objsToHash);
			break;
		case 3://Convert to a String and hash
			str = toStr(objsToHash);
			rtn = str.hashCode();
			break;
		case 10://Optimised hash, used by Statistic
			if (objsToHash.length > 1) {//Convert to a String and hash
				str = toStr(objsToHash);
				rtn = str.hashCode();
			} else {
				rtn = Arrays.deepHashCode(objsToHash);
			}
			break;
		default:
			log("Error: Wrong hashLevel:", hashLevel);
			break;
		}
		return rtn;
	}

	public void set(int i, Object obj) {
		objs[i] = obj;
	}

	public Object get(int i) {
		return objs[i];
	}

	public boolean equals(Object obj2) {
		boolean rtn = false;
		if (obj2 != null) {
			ObjArray objArray2 = (ObjArray) obj2;
			rtn = this.hashCode() == objArray2.hashCode();
		}
		return rtn;
	}

	public int compareTo(ObjArray objArray2) {
		Object[] objs2 = null;
		if (objArray2 != null) {
			objs2 = objArray2.objs;
		}
		return U.compare(this.objs, objs2, true);
	}

	public int compareTo(Object[] objs2) {
		return U.compare(this.objs, objs2, true);
	}

	public void set(Object[] objs) {
		this.objs = objs;
	}

	public Object[] asArray() {
		return objs;
	}

	public List asList() {
		return Arrays.asList(objs);
	}

	public int length() {
		int rtn = -1;
		if (objs != null) {
			rtn = objs.length;
		}
		return rtn;
	}

	public String toString() {
		String rtn = asList().toString();
		return rtn;
	}
}
