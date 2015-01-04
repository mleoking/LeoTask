package org.leores.net.mod;

import org.leores.math.rand.RandomEngine;
import org.leores.net.Link;
import org.leores.net.Network;
import org.leores.net.Networks;
import org.leores.net.Node;
import org.leores.util.*;
import org.leores.util.able.NewInstanceable;

public class FullyConnected extends Model {

	public FullyConnected(Integer nNode) {
		initialize(null, nNode);
	}

	public Network genNetwork(Network net) {
		Network rtn = net;
		if (rtn == null) {
			rtn = newNetwork();
		}
		rtn.createNodes(nNode);
		for (int i = 0; i < nNode; i++) {
			Node nodei = rtn.getNode(i);
			for (int j = i + 1; j < nNode; j++) {
				Node nodej = rtn.getNode(j);
				Link link = new Link(false, nodei, nodej, null);
				rtn.addLink(link);
			}
		}

		return rtn;
	}

	public Networks genNetworks(Networks nets) {
		return null;
	}

}
