package org.leores.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.leores.ecpt.UnsupportedTypeException;
import org.leores.ecpt.WrongFormatException;

public class Variables extends Logger implements Serializable {
	private static final long serialVersionUID = 944372659712500439L;
	public Class tClass;
	public List<Variable> variables;
	public Integer nValueSets;
	protected Integer iValueSet;

	public Variables(Class tClass) {
		if (tClass != null) {
			nValueSets = -1;
			this.tClass = tClass;
			variables = new ArrayList<Variable>();
		}
	}

	public Variables(String className) throws ClassNotFoundException {
		this(Class.forName(className));
	}

	public void reset() {
		iValueSet = 0;
		nValueSets = 0;
		if (variables != null) {
			int count = 1;
			boolean hasSet = false;
			for (int i = 0; i < variables.size(); i++) {
				Variable var = variables.get(i);
				var.reset();
				if (var.values != null && var.values.size() > 0) {
					count *= var.values.size();
					hasSet = true;
				}
			}
			if (hasSet) {
				nValueSets = count;
			}
		}

		return;
	}

	public boolean addVariable(String name, String sValues) {
		boolean rtn = false;

		Variable variable = (Variable) U.newInstance(Variable.class, tClass, name, sValues);
		if (variable != null) {
			variables.add(variable);
			reset();
			rtn = true;
		}

		return rtn;
	}

	public Variable findVariable(String sField) {
		Variable rtn = null;
		if (sField != null) {
			for (int i = 0, mi = variables.size(); i < mi; i++) {
				Variable var = variables.get(i);
				if (sField.equals(var.sField)) {
					rtn = var;
					break;
				}
			}
		}
		return rtn;
	}

	protected boolean next(int i) {
		boolean rtn = false;

		if (i >= 0 && i < variables.size()) {
			Variable var = variables.get(i);
			boolean bNext = var.next();
			bNext = bNext && var.hasNext();//The variable should hasNext after the move.
			if (bNext) {
				rtn = true;
			} else if (next(i + 1)) {
				var.reset();
				rtn = true;
			}
		}

		return rtn;
	}

	public boolean next() {
		boolean rtn = false;

		if (variables != null) {
			//when iValueSet == nValueSets, there is no next and can not loadValue.
			if (iValueSet < nValueSets) {
				iValueSet++;
			}
			if (next(0)) {
				rtn = true;
			}
		}

		return rtn;
	}

	public boolean hasNext() {
		boolean rtn = false;

		if (variables != null && variables.size() > 0) {
			rtn = variables.get(0).hasNext();
		}

		return rtn;
	}

	/**
	 * Lesson learned: When compare Object numbers, such as Integer rather than
	 * int, it is always safer to use .equals rather than ==. It happened in
	 * previous test that when tIValueSet and iValueSet were both 128,
	 * iValueSet.equals(tIValueSet) was true, iValueSet==tIValueSet was false.
	 * 
	 * @param tObj
	 * @param tIValueSet
	 *            from 0 to nValueSets-1
	 * @return
	 */

	public boolean loadValue(Object tObj, Integer tIValueSet) {
		boolean rtn = false;

		if (variables != null && tIValueSet != null && tIValueSet >= 0 && tIValueSet < nValueSets) {
			rtn = true;
			U.invokeObjMethodByName(tObj, "beforeLoadValue", false, "Variables.loadValue");
			if (!iValueSet.equals(tIValueSet)) {
				reset();
				while (!iValueSet.equals(tIValueSet)) {
					next();
				}
			}
			for (int i = 0; i < variables.size(); i++) {
				Variable var = variables.get(i);
				if (!var.loadValue(tObj)) {
					rtn = false;
					break;
				}
			}
			U.invokeObjMethodByName(tObj, "afterLoadValue", false, "Variables.loadValue");
		}

		return rtn;
	}

	public boolean loadValue(Object tObj) {
		return loadValue(tObj, iValueSet);
	}

}
