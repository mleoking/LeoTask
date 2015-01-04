package org.leores.net.ana;

import java.math.BigDecimal;
import java.util.List;

import org.leores.net.Link;
import org.leores.net.Network;
import org.leores.util.Logger;
import org.leores.net.*;

public class Metric extends Logger {
	protected Network tNet;

	public Metric(Network net) {
		tNet = net;
	}

	public Metric(String sFNet) {
		tNet = Network.createFromFile(sFNet, null, null);
	}

	/**
	 * Calculating Correlation Function r according to Eq.(4) in [1]. The
	 * variables correspond to (dA-dB)/(dC-dB) of Eq.(4) in [1]. It is also
	 * called Assortativity Coefficient. <br>
	 * <br>
	 * Here only calculate undirected links. <br>
	 * <br>
	 * [1]M. E. J. Newman, Assortative Mixing in Networks, Phys. Rev. Lett.,
	 * vol. 89, no. 20, p. 208701, Oct. 2002.
	 * 
	 * @return r. <b>r=1</b>:perfectly assortative, <b>r=0</b>: no assortative,
	 *         <b>r=-1</b>:perfectly disassortative.
	 */

	public Double correlationFunction() {
		Double rtn = null;

		if (tNet != null) {
			List<Link> links = tNet.getLinks();
			int M = links.size();
			BigDecimal iA = new BigDecimal(0), iB = new BigDecimal(0), iC = new BigDecimal(0);
			double dA = 0.0, dB = 0.0, dC = 0.0;
			for (int i = 0; i < M; i++) {
				Link link = links.get(i);
				Node.Degree ndFrom = link.from.getDegree(tNet);
				Node.Degree ndTo = link.to.getDegree(tNet);
				int j = ndFrom.undirected;
				int k = ndTo.undirected;
				iA = iA.add(new BigDecimal(j * k));
				iB = iB.add(new BigDecimal(j + k));
				iC = iC.add(new BigDecimal(j * j + k * k));
			}
			dA = iA.doubleValue() / M;
			dB = iB.doubleValue() / (2 * M);
			dB = dB * dB;
			dC = iC.doubleValue() / (2 * M);
			if (dA == dB && dB == dC) {
				rtn = 1.0;
			} else {
				rtn = (dA - dB) / (dC - dB);
			}
			//tLog("CorrelationFunction: " + rtn + " Links: " + M);
		}

		return rtn;
	}

	public Double avgDegree() {
		Double rtn = null;
		if (tNet != null) {
			int degreeSum = 0;

			List<Integer> lDegree = tNet.getDegreeList(Link.Flag.UNDIRECTED);
			for (int i = 0, size = lDegree.size(); i < size; i++) {
				Integer nDegree = lDegree.get(i);
				degreeSum += i * nDegree;
			}
			rtn = (double) degreeSum / tNet.nNodes();
		}

		return rtn;
	}

	public Double avgDegreeSquare() {
		Double rtn = null;
		if (tNet != null) {
			int degreeSquareSum = 0;

			List<Integer> lDegree = tNet.getDegreeList(Link.Flag.UNDIRECTED);
			for (int i = 0, size = lDegree.size(); i < size; i++) {
				Integer nDegree = lDegree.get(i);
				degreeSquareSum += i * i * nDegree;
			}
			rtn = (double) degreeSquareSum / tNet.nNodes();
		}

		return rtn;
	}

	/**
	 * g1b1 = (<k^2>-<k>)/<k>
	 * 
	 * @return
	 */
	public Double g1b1() {
		Double rtn = null;
		Double avgDegree = avgDegree();
		Double avgDegreeSquare = avgDegreeSquare();
		if (avgDegree != null && avgDegreeSquare != null) {
			rtn = ((double) avgDegreeSquare - avgDegree) / avgDegree;
		}

		return rtn;
	}

	public void printMetrics() {
		Double cf = correlationFunction();
		Double ad = avgDegree();
		Double ads = avgDegreeSquare();
		Double g1b1 = g1b1();
		log("CorrelationFunction: " + cf + " AverageDegree:" + ad + " AverageDegreeSquare:" + ads + " g1b1:" + g1b1);
	}
}
