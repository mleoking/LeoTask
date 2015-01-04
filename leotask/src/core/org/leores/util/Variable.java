package org.leores.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

import org.leores.ecpt.WrongParameterException;

public class Variable extends Logger implements Serializable {
	private static final long serialVersionUID = -8087990681997357462L;
	public String sField;
	public List values;
	public Integer iValue;

	@SuppressWarnings("all")
	public Variable(Class tClass, String name, String sValues) throws WrongParameterException {
		iValue = 0;
		sField = name;
		Class fieldType = String.class; //Default to be String if the field does not exist in a class. This is useful when using setter methods to set a number of fields at one time.
		Class componentType = null;
		if (!U.hasEvaluation(sValues)) {//fieldType will be String when there is evaluation patterns in sValues.
			Method mSetter = U.getSetterMethod(tClass, sField, fieldType);
			if (mSetter == null) {
				Field field = U.getField(tClass, name, U.modAll);
				if (field != null) {
					fieldType = field.getType();
					componentType = U.getComponentType(field);
				} else {
					throw new WrongParameterException("Field: " + name + " does not exist in " + tClass + "!", tClass, name, sValues);
				}
			}
		}
		if (componentType == null) {
			values = U.parseList(fieldType, sValues);
		} else {//the field is a list or an array
			values = new ArrayList();
			List<String> lSValue = U.parseList(String.class, sValues);
			for (int i = 0, mi = lSValue.size(); i < mi; i++) {
				String sValue = lSValue.get(i);
				List lValue = U.parseList(componentType, U.eval(sValue, null, U.EVAL_NullIgnore | U.EVAL_InvalidIgnore));//The eval here only processes special values such as $null$.
				if (U.bAssignable(List.class, fieldType)) {
					values.add(lValue);
				} else if (U.bAssignable(Object[].class, fieldType)) {
					values.add(U.toArray(lValue, componentType));
				}
			}
		}

		if (values == null || values.size() == 0) {
			throw new WrongParameterException("No valid value found!", tClass, name, sValues);
		}
	}

	public void reset() {
		iValue = 0;
	}

	public int getIVal(Object oVal) {
		int rtn = -1;

		if (values != null) {
			for (int i = 0, mi = values.size(); i < mi; i++) {
				Object oValI = values.get(i);
				if (U.compare(oVal, oValI) == 0) {
					rtn = i + 1;//return number starts from 1 rather than 0.
					break;
				}
			}
		}

		return rtn;
	}

	public boolean hasNext() {
		boolean rtn = false;

		if (values != null && iValue < values.size()) {
			rtn = true;
		}

		return rtn;
	}

	public boolean next() {
		boolean rtn = false;

		if (hasNext()) {
			iValue++;
			rtn = true;
		}

		return rtn;
	}

	public boolean loadValue(Object tObj) {
		boolean rtn = false;

		if (values != null && tObj != null && iValue < values.size()) {
			Object tValue = values.get(iValue);
			rtn = U.setFieldValue(tObj, sField, tValue, U.modAll, true, true);
		}

		return rtn;
	}
}
