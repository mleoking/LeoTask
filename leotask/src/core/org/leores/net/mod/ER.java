package org.leores.net.mod;

import java.util.List;

import org.leores.math.rand.RandomEngine;
import org.leores.net.Link;
import org.leores.net.Network;
import org.leores.net.Networks;
import org.leores.net.Node;
import org.leores.util.Logger;
import org.leores.util.able.NewInstanceable;

public class ER extends Model {
	public Integer nLink;
	public Double pConnect;

	//Gne original ER net model
	public ER(Integer nNode, Integer nLink) {
		initialize(null, nNode);
		this.nLink = nLink;
	}

	//Gnp variation of ER net model
	public ER(Integer nNode, Double pConnect) {
		initialize(null, nNode);
		this.pConnect = pConnect;
	}

	public Network genNetwork(Network net) {
		Network rtn = net;

		if (net == null) {
			rtn = newNetwork();
		}

		rtn.createNodes(nNode);
		if (pConnect != null) {//Gnp variation of ER net model
			for (int i = 0, size = rtn.nNodes(); i < size; i++) {
				for (int j = i + 1; j < size; j++) {
					if (rand.nextDouble() < pConnect) {
						Node n1 = rtn.getNode(i);
						Node n2 = rtn.getNode(j);
						Link link = new Link(false, n1, n2, null);
						rtn.addLink(link);
					}
				}
			}
		} else if (nLink != null) {//Gne original ER net model
			for (int i = 0; i < nLink; i++) {
				boolean added = false;
				do {
					int i1 = rand.nextInt(nNode);
					int i2;
					do {
						i2 = rand.nextInt(nNode);
					} while (i2 == i1);

					Node n1 = rtn.getNode(i1);
					Node n2 = rtn.getNode(i2);
					Link link = new Link(false, n1, n2, null);
					added = rtn.addLink(link);
				} while (!added);
			}
		}

		return rtn;
	}

	public Networks genNetworks(Networks nets) {
		return null;
	}

}
