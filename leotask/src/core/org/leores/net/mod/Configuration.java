package org.leores.net.mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.leores.net.Link;
import org.leores.net.Network;
import org.leores.net.Networks;
import org.leores.net.Node;
import org.leores.net.degree.DegreeGenerator;
import org.leores.util.U;

public class Configuration extends Model {
	public List<DegreeGenerator> ldg;

	public Configuration(DegreeGenerator dg, Integer nNode) {
		initialize(null, nNode);
		ldg = new ArrayList<DegreeGenerator>();
		ldg.add(dg);
	}

	public Configuration(Integer nNode, DegreeGenerator... dgs) {
		initialize(null, nNode);
		ldg = new ArrayList<DegreeGenerator>();
		ldg.addAll(Arrays.asList(dgs));
	}

	protected boolean existTwoLinkableNodes(Network net, List<Node> lDNodes) {
		boolean rtn = false;

		int size = lDNodes.size();
		if (size > 1) {
			for (int i = 0; i < size - 1 && !rtn; i++) {
				Node nodei = lDNodes.get(i);
				for (int j = i + 1; j < size; j++) {
					Node nodej = lDNodes.get(j);
					if (!nodei.equals(nodej) && !nodei.bReachable(net, nodej)) {
						rtn = true;
						break;
					}
				}
			}

		}

		return rtn;
	}

	public int genLinks(Network net, DegreeGenerator dg, List<Node> nodes) {
		int rtn = -1;
		if (net != null && dg != null && nodes != null) {
			rtn = 0;
			int nn = nodes.size();
			List<Node> lDNodes = new ArrayList<Node>(nn * 2);

			for (int i = 0; i < nn; i++) {
				int degree = dg.degree(i, nn);
				Node node = nodes.get(i);
				for (int j = 0; j < degree; j++) {
					lDNodes.add(node);
				}
			}

			int size = lDNodes.size();
			while (size > 1 && existTwoLinkableNodes(net, lDNodes)) {
				int i1 = rand.nextInt(size);
				int i2 = rand.nextInt(size);

				Node node1 = lDNodes.get(i1);
				Node node2 = lDNodes.get(i2);
				//node1 and node2 are not the same node and there is no link exist between them either.
				if (!node1.equals(node2) && !node1.bReachable(net, node2)) {
					Link link = new Link(false, node1, node2, null);
					if (net.addLink(link)) {
						rtn++;
						//Remove the item with higher index first to avoid the other index changing.
						//Now this is done automatically by U.removeElements.
						U.removeElements(lDNodes, i1, i2);
					}
				}
				size = lDNodes.size();
			}
		}
		return rtn;
	}

	public Network genNetwork(Network net) {
		Network rtn = net;
		if (ldg.size() > 0) {
			DegreeGenerator dg = ldg.get(0);
			if (net == null) {
				rtn = newNetwork();
			}
			rtn.createNodes(nNode);
			genLinks(rtn, dg, rtn.getNodes());
		} else {
			rtn = null;
		}
		return rtn;
	}

	@Override
	public Networks genNetworks(Networks nets) {
		Networks rtn = nets;
		if (ldg.size() > 0) {
			if (rtn == null) {
				rtn = newNetworks(null);
			}
			rtn.createNodes(nNode);
			for (int i = 0, mi = ldg.size(); i < mi; i++) {
				DegreeGenerator dg = ldg.get(i);
				Network net = rtn.addNewNetwork();
				genLinks(net, dg, rtn.getNodes());
			}
		} else {
			rtn = null;
		}
		return rtn;
	}
}
