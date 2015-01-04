package org.leores.net.degree;

import java.util.ArrayList;
import java.util.List;

import org.leores.net.Link;
import org.leores.net.Network;
import org.leores.net.Node;

public class NetworkDegreeClone extends DegreeGenerator {

	public NetworkDegreeClone(String sNet) {
		this(Network.createFromFile(sNet, null, null));
	}

	public NetworkDegreeClone(Network net) {
		lDegree = net.getDegreeList(Link.Flag.UNDIRECTED);
		n = net.nNodes();
	}

	@Override
	public double pk(int k) {
		return -1;
	}

	public int degree(int i, int n) {
		int rtn = -1;

		if (i >= 0 && i <= n - 1 && n == this.n) {
			int n0k = 0;//the number of nodes with degree from 0 to k
			for (int k = 0, size = lDegree.size(); k < size; k++) {
				n0k += lDegree.get(k);
				if (i < n0k) {
					rtn = k;
					break;
				}
			}
		}

		return rtn;
	}
}
