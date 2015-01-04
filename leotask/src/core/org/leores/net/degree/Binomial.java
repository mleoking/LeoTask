package org.leores.net.degree;

import org.leores.math.Arithmetic;
import org.leores.math.rand.RandomEngine;

public class Binomial extends DegreeGenerator {
	public double p;
	public double log_n, log_p, log_q;

	public Binomial(RandomEngine rand, int min, int max, double p) {
		super(rand, min, max);
		this.p = p;
	}

	public void setN(int n) {
		super.setN(n);
		this.log_p = Math.log(p);
		this.log_q = Math.log(1.0 - p);
		this.log_n = Arithmetic.logFactorial(n);
	}

	public double pk(int k) {
		//double rtn = comb(n, k) * pow(p, k) * pow(1 - p, n - k);
		int r = n - k;
		double rtn = Math.exp(this.log_n - Arithmetic.logFactorial(k) - Arithmetic.logFactorial(r) + this.log_p * k + this.log_q * r);
		return rtn;
	}
}
