package org.leores.util.able;

import java.lang.reflect.Field;

import org.leores.util.U;

public interface Processable2<R, A, B> {
	public R process(A a, B b);

	public static class TrimStringField implements Processable2<Boolean, Object, Field> {

		public Boolean process(Object obj, Field field) {
			boolean rtn = false;
			if (field != null && U.bAssignable(String.class, field.getType())) {
				String sValue = (String) U.getFieldValue(obj, field);
				if (sValue != null) {
					String sValue2 = sValue.trim();
					if (sValue2.length() < sValue.length()) {
						rtn = true;
					}
					U.setFieldValue(obj, field, sValue2);
				}
			}
			return rtn;
		}

	}

	public static class SampleListByIndexB<B> implements Processable2<Boolean, Integer, B> {
		public Integer step;

		/**
		 * The sample results will with these index: 0, 0+step, 0+2*step, ...
		 * 
		 * @param step
		 */
		public SampleListByIndexB(Integer step) {
			this.step = step;
		}

		public Boolean process(Integer i, B b) {
			return i % step == 0;
		}

	}

	public static class SampleListByIndex<B> implements Processable2<B, Integer, B> {
		public Integer step;

		/**
		 * The sample results will with these index: 0, 0+step, 0+2*step, ...
		 * 
		 * @param step
		 */
		public SampleListByIndex(Integer step) {
			this.step = step;
		}

		public B process(Integer i, B b) {
			B rtn = null;
			if (i % step == 0) {
				rtn = b;
			}
			return rtn;
		}
	}

	public static class RemoveRowWithInvalidNumber<B> implements Processable2<B[], Integer, B[]> {
		public String sInvalid;

		/**
		 * E.g. "$%$<0" : remove rows with negative number.
		 * 
		 * @param sInvalid
		 */
		public RemoveRowWithInvalidNumber(String sInvalid) {
			this.sInvalid = sInvalid;
		}

		public B[] process(Integer a, B[] b) {
			B[] rtn = b;
			for (int i = 0; i < b.length; i++) {
				Double d = U.toDouble(b[i] + "");
				if (d != null) {
					if (U.evalCheck(sInvalid, d)) {
						rtn = null;
						break;
					}
				}
			}
			return rtn;
		}
	};

}
