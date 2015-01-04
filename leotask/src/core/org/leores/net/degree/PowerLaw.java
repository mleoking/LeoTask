package org.leores.net.degree;

import java.util.ArrayList;
import java.util.List;

import org.leores.math.rand.RandomEngine;
import org.leores.util.U;

public class PowerLaw extends DegreeGenerator {
	public double r;

	public PowerLaw(RandomEngine rand, int min, int max) {
		this(rand, min, max, 3);
	}

	/**
	 * max <= n^(1/2) is a preferred condition to help help generated better
	 * uncorrelated network in the configuration model. n is the nTotal number
	 * of
	 * nodes.
	 * 
	 * The generated degree k could be within [min, max].
	 * 
	 * @param rand
	 * @param min
	 * @param max
	 * @param r
	 */
	public PowerLaw(RandomEngine rand, int min, int max, double r) {
		super(rand, min, max);
		this.r = r;
		//log("PowerLaw. min:" + min + " max:" + max + " r:" + r);
	}

	@Override
	public double pk(int k) {
		double rtn = 2 * pow(k * (1 + k), -r) * (-pow(k, r) * (1 + k) + k * pow(1 + k, r)) * min * min / (r - 1);
		return rtn;
	}

	@Override
	public double calPdkSum() {
		this.pdkSum = 2 * min * min * pow(max * min, -r) * (pow(max, r) * min - max * pow(min, r)) / (r - 1);
		return pdkSum;
	}

	public double pmdk(int k) {
		double rtn = -1;
		if (k >= min && k <= max) {
			//The area enclosed by the power law distribution curve, the x axis, 
			//and two vertical line x=min and x=k+1.
			double mkArea = 2 * min * min * pow((1 + k) * min, -r) * (pow(1 + k, r) * min - (1 + k) * pow(min, r)) / (r - 1);
			rtn = mkArea / pdkSum;
		}
		return rtn;
	}

	public int degree() {
		int rtn = -1;
		double u = rand.nextDouble();
		double max_ = max + 1;
		if (r == 3) {
			rtn = (int) ((min * max_) / Math.sqrt(min * min + max_ * max_ * u));
		} else if (r == 2) {
			rtn = (int) (2 * max_ * min * min / (2 * min * min + max_ * u));
		}
		return rtn;
	}
}
