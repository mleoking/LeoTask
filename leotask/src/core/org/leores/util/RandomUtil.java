package org.leores.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.leores.math.rand.RandomEngine;

public class RandomUtil<E> extends Logger implements Serializable {
	public RandomEngine rand;

	public RandomUtil(RandomEngine rand) {
		if (rand != null) {
			this.rand = rand;
		} else {
			this.rand = RandomEngine.makeDefault();
		}

	}

	public E get1Element(List<E> list) {
		E rtn = null;

		int iRand = rand.nextInt(list.size());
		rtn = list.get(iRand);

		return rtn;
	}

	public E getAnotherElement(List<E> list, E e) {
		E rtn = null;

		int iRand;
		do {
			iRand = rand.nextInt(list.size());
			rtn = list.get(iRand);
		} while (rtn == e);

		return rtn;
	}

	/**
	 * A costly function that need list clone. Try to avoid using this.
	 * 
	 * @param list
	 * @param e
	 * @return
	 */

	public List<E> getOtherElements(ArrayList<E> list, E e) {
		List<E> rtn = null;

		rtn = (List<E>) list.clone();
		rtn.remove(e);

		return rtn;
	}

	/**
	 * Use a modified version of Knuth-Fisher-Yates shuffle algorithm to
	 * partially shuffle the list and return the expected number of random items
	 * in a list.
	 * 
	 * @param list
	 * @param n
	 * @param randomer
	 * @return
	 */
	public List<E> getNElementsKnuthFisherYatesShuffle(List<E> list, int n) {
		List<E> rtn = null;
		rtn = new ArrayList<E>(n);
		E[] es = (E[]) list.toArray();
		for (int i = es.length - 1; i > es.length - n - 1; i--) {
			int iRand = rand.nextInt(i + 1);
			E eRand = es[iRand];
			es[iRand] = es[i];
			//This is not necessary here as we do not really need the final shuffle result.
			//es[i] = eRand;
			rtn.add(eRand);
		}
		return rtn;
	}

	public List<E> getNElementsReservoirSampling(List<E> list, int n) {
		List<E> rtn = new ArrayList<E>(n);
		int lSize = list.size();
		for (int i = 0; i < n; i++)
			rtn.add(list.get(i));
		for (int i = n; i < lSize; i++) {
			int v = rand.nextInt(i + 1);
			if (v < n) {
				rtn.set(v, list.get(i));
			}
		}
		return rtn;
	}

	public List<E> getNElementsBitSet(List<E> list, int n) {
		List<E> rtn = new ArrayList<E>(n);
		int[] ids = genNBitSet(n, 0, list.size());
		for (int i = 0; i < ids.length; i++) {
			rtn.add(list.get(ids[i]));
		}
		return rtn;
	}

	/**
	 * Return n random elements in list. When n < list.size()/4 use
	 * getNElementsBitSet otherwise
	 * getNElementsResorvoirSampling.
	 * 
	 * @param list
	 * @param n
	 * @return
	 */
	public List<E> getNElements(List<E> list, int n) {
		List<E> rtn = null;
		if (list != null && n > 0) {
			int lSize = list.size();
			if (lSize > n) {
				if (n <= lSize / 4) {
					rtn = getNElementsBitSet(list, n);
				} else {
					rtn = getNElementsReservoirSampling(list, n);
					//rtn = getNElementsKnuthFisherYatesShuffle(list, n);
				}
			} else if (lSize == n) {
				rtn = new ArrayList<E>(n);
				rtn.addAll(list);
			} else {
				log("list.size < n! [lSize, n]:", lSize, n);
			}
		}
		return rtn;
	}

	/**
	 * Generate n distinctive random number within the range [min,max) from min
	 * to max-1.
	 * This function uses a modified version of the BitMap algorithm by Daniel
	 * Lemire.
	 * 
	 * @param n
	 * @param max
	 * @return
	 */
	public int[] genNBitSet(int n, int min, int max) {
		int range = max - min;
		int[] rtn = new int[n];
		switch (n) {
		case 1:
			rtn[0] = rand.nextInt(range) + min;
			break;
		case 2:
			rtn[0] = rand.nextInt(range) + min;
			do {
				rtn[1] = rand.nextInt(range) + min;
			} while (rtn[1] == rtn[0]);
			break;
		default:
			BitSet bs = new BitSet(range);//when range is large this initialisation of BitSet costs lots of time.
			int nGen = 0;
			while (nGen < n) {
				int v = rand.nextInt(range);
				if (!bs.get(v)) {
					bs.set(v);
					nGen++;
				}
			}
			int pos = 0;
			for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
				rtn[pos++] = i + min;
			}
			break;
		}
		return rtn;
	}

	public int[] genNReservoirSampling(int n, int min, int max) {
		int range = max - min;
		int[] rtn = new int[n];
		for (int i = 0; i < n; i++)
			rtn[i] = i + min;
		for (int i = n; i < range; i++) {
			int v = rand.nextInt(i + 1);
			if (v < n) {
				rtn[v] = i + min;
			}
		}
		return rtn;
	}

	/**
	 * Generate n distinctive random number within the range [min,max) from min
	 * to max-1.
	 * This function uses a modified version of the BitMap algorithm by Daniel
	 * Lemire and Reservoir Sampling
	 * 
	 * @param n
	 * @param max
	 * @return
	 */
	public int[] genN(int n, int min, int max) {
		int[] rtn = null;
		int range = max - min;
		if (n > 0 && n < range) {
			if (n <= range / 4) {
				rtn = genNBitSet(n, min, max);
			} else {
				rtn = genNReservoirSampling(n, min, max);
			}
		} else if (n == 0) {
			rtn = new int[0];
		} else if (n == range) {
			rtn = new int[n];
			for (int i = 0; i < n; i++)
				rtn[i] = i + min;
		} else {
			log(LOG_ERROR, "genN: not possible [n,min,max]:" + U.toStr(n, min, max));
		}
		return rtn;
	}

	/**
	 * Generate n distinctive random number within the range [0,max) from 0
	 * to max-1. This function uses a modified version of the BitMap algorithm
	 * by Daniel
	 * Lemire.
	 * 
	 * @param n
	 * @param max
	 * @return
	 */
	public int[] genN(int n, int max) {
		return genN(n, 0, max);
	}
}
