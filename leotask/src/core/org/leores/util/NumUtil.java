package org.leores.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumUtil extends StrUtil {
	public static int scale = 10;

	/**
	 * Round a value to the scale.
	 * 
	 * @param d
	 * @param scale
	 *            the number of decimals to be kept. 0: no decimals.
	 * @return
	 */
	public static Double round(Number num, int scale) {
		Double rtn = null;
		if (num != null) {
			BigDecimal bd = new BigDecimal(num.doubleValue());
			bd = bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
			rtn = bd.doubleValue();
		}
		return rtn;
	}

	public static Double divide(Number numerator, Number denominator) {
		Double rtn = null;

		if (numerator != null && denominator != null) {
			double dNumerator = numerator.doubleValue();
			double dDenominator = denominator.doubleValue();
			if (dDenominator != 0) {
				BigDecimal bdNumerator = new BigDecimal(dNumerator);
				BigDecimal bdDenominator = new BigDecimal(dDenominator);
				rtn = bdNumerator.divide(bdDenominator, scale, RoundingMode.HALF_UP).doubleValue();
			}
		}

		return rtn;
	}

	public static Double percentage(Number numerator, Number denominator) {
		Double rtn = null;

		if (numerator != null) {
			rtn = divide(numerator.doubleValue() * 100, denominator);
		}

		return rtn;
	}
}
