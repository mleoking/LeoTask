package org.leores.net.degree;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.leores.math.rand.RandomEngine;
import org.leores.util.Logger;
import org.leores.util.U;

public abstract class DegreeGenerator extends Logger {
	public RandomEngine rand;
	public int min;
	public int max;
	public int n;
	//The whole area enclosed by the distribution curve, the x axis, 
	//and two vertical line x=min and x=max.
	public double pdkSum;
	public List<Integer> lDegree;

	public DegreeGenerator() {
	}

	public DegreeGenerator(RandomEngine rand, int min, int max) {
		initialize(rand, min, max);
	}

	public void initialize(RandomEngine rand, int min, int max) {
		if (rand != null) {
			this.rand = rand;
		} else {
			this.rand = RandomEngine.makeDefault();
		}
		this.min = min;
		this.max = max;
		this.n = -1;
		this.pdkSum = -1;
	}

	public abstract double pk(int k);

	/**
	 * probability of degree k. This is different from probability of k as this
	 * will consider the probability from min to max as the whole range.
	 * 
	 * @param k
	 * @return
	 */
	public double pdk(int k) {
		double rtn = -1;
		if (k >= min && k <= max) {
			rtn = pk(k) / pdkSum;
		}
		return rtn;
	}

	public double pow(double a, double b) {
		return Math.pow(a, b);
	}

	public int round(double a) {
		return (int) Math.round(a);
	}

	public long comb(int n, int k) {
		long rtn = -1;
		if (n >= k && k >= 0) {
			BigDecimal numerator = new BigDecimal(1), denominator = new BigDecimal(1);
			for (int i = n; i >= (n - k + 1); i--) {
				numerator = numerator.multiply(new BigDecimal(i));
			}
			for (int i = k; i >= 1; i--) {
				denominator = denominator.multiply(new BigDecimal(i));
			}
			rtn = numerator.divide(denominator).longValue();
		}
		return rtn;
	}

	public double calPdkSum() {
		pdkSum = 0;
		for (int k = min; k <= max; k++) {
			pdkSum += pk(k);
		}
		return pdkSum;
	}

	public void setN(int n) {
		this.n = n;
	}

	public List<Integer> buildDegreeList(int n) {
		if (n > 0 && n != this.n) {
			setN(n);
			calPdkSum();
			lDegree = new ArrayList<Integer>();
			long nmk = 0; //the number of nodes from min to k
			int nlinks = 0;
			for (int k = min; k <= max; k++) {
				int nk = round(pdk(k) * n);
				nmk += nk;
				if (nk < 0 || nmk < 0) {
					log(k, nk, nmk);
				}
				nlinks += k * nk;
				lDegree.add(nk);
			}
			if (nmk != n) {
				log("nmk<>n!nmk:" + nmk + " n:" + n);
				int nAdded = 0;
				while (nmk != n) {
					Integer i0 = lDegree.get(0);
					if (nmk < n) {
						lDegree.set(0, i0 + 1);
						nmk++;
						nAdded++;
						nlinks += min;
					} else if (nmk > n) {
						lDegree.set(0, i0 - 1);
						nmk--;
						nAdded--;
						nlinks -= min;
					}

				}
				log("Fixed by adding " + nAdded + " min degree nodes");
			}
			//nlinks much be even
			if (nlinks % 2 != 0) {
				log("nlinks is not even. nlinks:" + nlinks);
				Integer i0 = lDegree.get(0);
				Integer i1 = lDegree.get(1);
				if (min % 2 == 0) {
					lDegree.set(0, i0 + 1);
					lDegree.set(1, i1 - 1);
					nlinks = nlinks + min - (min + 1);
					log("Fixed by adding 1 min degree and delete 1 min+1 degree node. nlinks:" + nlinks);
				} else {
					lDegree.set(0, i0 - 1);
					lDegree.set(1, i1 + 1);
					nlinks = nlinks - min + (min + 1);
					log("Fixed by adding 1 min+1 degree and delete 1 min degree node. nlinks:" + nlinks);
				}
			}
		}

		return lDegree;
	}

	public int degree() {
		return -1;
	};

	/**
	 * i starts from 0 to n-1
	 * 
	 * @param i
	 * @param n
	 * @return
	 */
	public int degree(int i, int n) {
		int rtn = -1;

		if (i >= 0 && i <= n - 1) {
			buildDegreeList(n);
			int nmk = 0;//the number of nodes from min to k+min.
			for (int k = 0, size = lDegree.size(); k < size; k++) {
				nmk += lDegree.get(k);
				//e.g. nmk = 4, i could be 0, 1, 2, 3
				if (i < nmk) {
					rtn = k + min;
					break;
				}
			}
		}

		return rtn;
	}
}
