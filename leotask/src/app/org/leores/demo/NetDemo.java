package org.leores.demo;

import java.util.List;

import org.leores.net.Link;
import org.leores.net.Network;
import org.leores.net.Networks;
import org.leores.net.Node;
import org.leores.net.Link.Flag;
import org.leores.net.Node.Degree;
import org.leores.util.U;
import org.leores.util.able.NewInstanceable;
import org.leores.util.able.Processable1;

public class NetDemo extends Demo {

	public static class DemoNode extends Node {
		public String demoInfo;
		public static DemoNode dn;

		public static DemoNode newDemoNode() {
			if (dn == null) {
				dn = new DemoNode();
			}
			DemoNode rtn = (DemoNode) dn.newClone();
			return rtn;
		}

		public void beforeSaveInfo() {
			log("node" + id + " beforeSaveInfo info=" + info);
			return;
		}

		public void afterLoadInfo() {
			log("node" + id + " afterLoadInfo info=" + info);
			return;
		}

		public String toString() {
			return "Node(" + id + "," + demoInfo + ")";
		}
	}

	public static class NewDemoNodeable implements NewInstanceable<DemoNode> {
		public DemoNode newInstance() {
			return DemoNode.newDemoNode();
		}
	}

	public static class DemoLink extends Link {
		public String demoInfo;
		public static DemoLink dl;

		public static DemoLink newDemoLink() {
			if (dl == null) {
				dl = new DemoLink();
				dl.info = "demolink";
			}
			return (DemoLink) dl.newClone();
		}
	}

	public static class NewDemoLinkable implements NewInstanceable<DemoLink> {
		public DemoLink newInstance() {
			return DemoLink.newDemoLink();
		}
	}

	public NewInstanceable nDNa;
	public NewInstanceable nDLa;
	public Networks nets;
	public Networks nets2;
	public Network net;
	public Network net2;
	public String sFileOutNets;
	public String sFileOutNets2;
	public String sFileOutNets3;
	public String sFileOutNets4;
	public String sFileOutNetsNodeInfo1;
	public String sFileOutNetsNodeInfo2;
	public String sFileOutNet;
	public String sFileOutNet2;
	public String sFileOutNet3;
	public String sFileOutNet4;
	public String sFileOutNetNodeInfo1;
	public String sFileOutNetNodeInfo2;
	public String sFileIncompleteNetData;
	public String SFileImcompleteNetsData;

	public NetDemo() {
		nDNa = new NewDemoNodeable();
		nDLa = new NewDemoLinkable();

		sFileOutNets = "NetDemoNets.dat";
		sFileOutNets2 = "NetDemoNets2.dat";
		sFileOutNets3 = "NetDemoNets3.dat";
		sFileOutNets4 = "NetDemoNets4.dat";
		sFileOutNetsNodeInfo1 = "NetDemoNetsNodeInfo1.dat";
		sFileOutNetsNodeInfo2 = "NetDemoNetsNodeInfo2.dat";
		sFileOutNet = "NetDemoNet.dat";
		sFileOutNet2 = "NetDemoNet2.dat";
		sFileOutNet3 = "NetDemoNet3.dat";
		sFileOutNet4 = "NetDemoNet4.dat";
		sFileOutNetNodeInfo1 = "NetDemoNetNodeInfo1.dat";
		sFileOutNetNodeInfo2 = "NetDemoNetNodeInfo2.dat";
		sFileIncompleteNetData = "NetDemoNetIncomplete.dat";
		SFileImcompleteNetsData = "NetDemoNetsIncomplete.dat";
	}

	public void createSaveNetworks() {
		nets = new Networks(3, nDNa, nDLa);
		nets.getNetwork(0).createNodes(10);//id 0-9
		nets.getNetwork(1).createNodes(10);//id 10-19
		nets.getNetwork(2).createNodes(10);//id 20-29
		nets.createNodes(5);// id 30-34
		nets.info = "First networks example";

		//0.5, 0.7, and 0.9 are the weights of links
		nets.getNetwork(0).addLink(true, 1, 11, 0.5, "Net0:1-11T");
		nets.getNetwork(0).addLink(false, 2, 12, 0.7, "Net0:2-12F");
		nets.getNetwork(0).addLink(false, 3, 4, null, "Net0:3-4F");

		nets.getNetwork(1).addLink(true, 11, 21, null, "Net1:11-21T");
		nets.getNetwork(1).addLink(false, 12, 22, 0.9, "Net1:12-22F");
		nets.getNetwork(1).addLink(false, 13, 14, null, "Net1:13-14F");

		nets.getNetwork(2).addLink(true, 21, 11, null, "Net2:21-11T");
		nets.getNetwork(2).addLink(false, 22, 31, null, "Net2:22-31F");
		nets.getNetwork(2).addLink(false, 23, 34, null, "Net2:23-34F");

		nets.saveToFile(sFileOutNets);

		return;
	}

	public void cloneNetworks() {
		createSaveNetworks();
		Networks netsClone = nets.clone();
		netsClone.saveToFile(sFileOutNets2);

		Node node1 = nets.getNode(11);
		nets.removeNode(node1);
		nets.getNetwork(2).addLink(false, 30, 32, null, "Net2:30-32F");

		Processable1<Boolean, Link> pa1Link1 = new Processable1<Boolean, Link>() {
			public Boolean process(Link e) {
				return e.from.getId() == 12 && e.to.getId() == 22;
			}
		};
		Link link12_22 = netsClone.getNetwork(1).getLinks(pa1Link1).get(0);
		link12_22.weight = 1.9999;
		link12_22.info = "1.2345";

		nets.saveToFile(sFileOutNets3);
		netsClone.saveToFile(sFileOutNets4);
	}

	/**
	 * Clone the network/networks with NewInstanceable as a parameter will end
	 * up with
	 * network/networks with same structure but the nodes and links are all
	 * recreated using the NewInstanceable.
	 */
	public void cloneWithNewInstanceable() {
		class DemoNode2 extends Node {
			public void beforeSaveInfo() {
				info = "DemoNode2";
			}
		}

		class DemoLink2 extends Link {
			public void beforeSaveInfo() {
				info = "DemoLink2";
			}
		}

		NewInstanceable nIaNode = new NewInstanceable<DemoNode2>() {
			public DemoNode2 newInstance() {
				return new DemoNode2();
			}
		};

		NewInstanceable nIaLink = new NewInstanceable<DemoLink2>() {
			public DemoLink2 newInstance() {
				return new DemoLink2();
			}
		};

		createSaveNetwork();
		Network netClone = net.clone(null, net.getId(), nIaNode, nIaLink);
		netClone.saveToFile(sFileOutNet2, true);

		createSaveNetworks();
		Networks netsClone = nets.clone(nIaNode, nIaLink);
		netsClone.saveToFile(sFileOutNets2, true);
	}

	public void addMergeUniteNetworks() {
		createSaveNetworks();
		createSaveNetwork();
		nets.push(net);
		nets.push(net.clone());
		nets.push(net.clone());
		nets.saveToFile(sFileOutNets2);
		nets.mergeNetwork(0, 2, 4);
		nets.saveToFile(sFileOutNets3);
		Network netUnited = nets.unite();
		netUnited.saveToFile(sFileOutNet2);
		netUnited.removeNode(netUnited.getNode(1));
		netUnited.removeNode(netUnited.getNode(2));
		netUnited.cleanNullNodesLinks();
		netUnited.saveToFile(sFileOutNet3);
	}

	public void loadNetworks() {
		createSaveNetworks();
		nets2 = Networks.createFromFile(sFileOutNets, nDNa, nDLa);
		nets2.saveToFile(sFileOutNets2);
		return;
	}

	public void loadIncompleteNetworks() {
		nets2 = Networks.createFromFile(SFileImcompleteNetsData, nDNa, nDLa);
		nets2.saveToFile(sFileOutNets3, true);
		return;
	}

	public void createSaveNetwork() {
		net = new Network(nDNa, nDLa);
		net.info = "First Network";
		net.createNodes(10); //id 0-9
		net.addLink(false, 1, 2, null, "1-2");
		//adding a duplicated link. Failed. Could not add duplicated links. 
		//But you could have a directed and a undirected link between two nodes. 
		net.addLink(false, 1, 2, null, "1-2");
		net.addLink(true, 1, 2, null, "1->2");
		net.addLink(true, 2, 1, null, "2->1");
		net.addLink(false, 2, 4, null, "2-4");
		net.addLink(true, 2, 5, null, "2->5");
		net.addLink(false, 5, 1, null, "5-1");
		net.addLink(true, 5, 2, null, "5->2");
		net.addLink(true, 7, 2, null, "7->2");
		//adding a existing link will be neglected:
		net.addLink(true, 1, 2, null, "1->2");
		net.saveToFile(sFileOutNet);
		return;
	}

	public void cloneNetwork() {
		createSaveNetwork();
		Network netClone = net.clone();
		netClone.saveToFile(sFileOutNet2);

		Node node1 = net.getNode(1);
		net.removeNode(node1);
		net.addLink(true, 8, 9, null, "8->9");

		Processable1<Boolean, Link> pa1Link1 = new Processable1<Boolean, Link>() {
			public Boolean process(Link e) {
				return e.from.getId() == 2 && e.to.getId() == 5;
			}
		};
		Link link2_5 = netClone.getLinks(pa1Link1).get(0);
		link2_5.info = "1.2345";

		net.saveToFile(sFileOutNet3);
		netClone.saveToFile(sFileOutNet4);
	}

	public void loadNetwork() {
		createSaveNetwork();
		net2 = Network.createFromFile(sFileOutNet, nDNa, nDLa);
		net2.saveToFile(sFileOutNet2);
		return;
	}

	public void loadIncompleteNetwork() {
		net = Network.createFromFile(sFileIncompleteNetData, nDNa, nDLa);
		net.saveToFile(sFileOutNet3, true);
		return;
	}

	public void saveLoadNodeInfo() {
		createSaveNetworks();
		nets.getNode(1).info = "info1";
		nets.getNode(2).info = "info2";
		nets.saveToFile(sFileOutNetsNodeInfo1, true);
		nets2 = Networks.createFromFile(sFileOutNetsNodeInfo1, nDNa, nDLa);
		nets2.saveToFile(sFileOutNetsNodeInfo2, true);

		createSaveNetwork();
		net.getNode(1).info = "info1";
		net.getNode(3).info = "info3";
		net.saveToFile(sFileOutNetNodeInfo1, true);
		net2 = Network.createFromFile(sFileOutNetNodeInfo1, nDNa, nDLa);
		net2.saveToFile(sFileOutNetNodeInfo2, true);
	}

	public void nodeMethods() {
		createSaveNetwork();
		DemoNode dNode1 = (DemoNode) net.getNode(1);
		DemoNode dNode2 = (DemoNode) net.getNode(2);
		List<Node> nodes;

		dNode1.demoInfo = "dn1";

		log("dNode2:");
		nodes = U.refineList(dNode2.getLinkedNodes(net, null, Flag.IN | Flag.OUT | Flag.UNDIRECTED), null);
		log("AllLinkedNodes:", nodes);
		nodes = dNode2.getLinkedNodes(net, null, Flag.IN);
		log("InNodes:", nodes);
		nodes = dNode2.getLinkedNodes(net, null, Flag.OUT);
		log("OutNodes:", nodes);
		nodes = dNode2.getLinkedNodes(net, null, Flag.UNDIRECTED);
		log("UndirectedNodes:", nodes);
		nodes = dNode2.getReachableNodes(net, null);
		log("ReachableNodes:", nodes);
		nodes = dNode2.getReachableToMeNodes(net, null);
		log("getReachableToMeNodes:", nodes);

		Degree degree = dNode2.getDegree(net);
		log("degree:", degree);

		Processable1<Boolean, Link> tBaLink1 = new Processable1<Boolean, Link>() {
			public Boolean process(Link e) {
				return e.from.getId() == 1 || e.to.getId() == 1;
			}
		};
		Processable1 tBaLink2 = new Processable1<Boolean, DemoLink>() {
			public Boolean process(DemoLink e) {
				return "heavy".equals(e.demoInfo);
			}
		};
		Processable1<String, Link> pa1Link1 = new Processable1<String, Link>() {
			public String process(Link e) {
				String con = "-";
				if (e.bDirected)
					con = "->";
				return e.from + con + e.to;
			}
		};

		List<Link> links = dNode2.getLinks(net, tBaLink1);
		log("links with 1:", links, pa1Link1);

		DemoLink dl1 = (DemoLink) links.get(1);
		dl1.demoInfo = "heavy";

		links = dNode2.getLinks(net, tBaLink2);
		log("links with demoInfo heavy:", links, pa1Link1);

		net.removeLink(links.get(0));
		links = dNode2.getLinks(net, tBaLink1);
		log("links with 1 (after remove 1 link):", links, pa1Link1);

		log(dNode2.getId() + " bReachable to " + dNode1.getId() + " :", dNode2.bReachable(net, dNode1) + "");
		net.removeLink(links.get(1));
		links = dNode2.getLinks(net, tBaLink1);
		log("links with 1 (after remove 2 link):", links, pa1Link1);
		log(dNode2.getId() + " bReachable to " + dNode1.getId() + " :", dNode2.bReachable(net, dNode1) + "");

		net.removeNode(dNode1);
		links = dNode2.getLinks(net, tBaLink1);
		log("links with 1 (after remove node1):", links, pa1Link1);

		net.saveToFile(sFileOutNet2);
	}

	public static void demo() {
		NetDemo demo = new NetDemo();
		demo.createSaveNetworks();
		demo.cloneNetworks();
		demo.cloneWithNewInstanceable();
		demo.addMergeUniteNetworks();
		demo.loadNetworks();
		demo.loadIncompleteNetworks();
		demo.createSaveNetwork();
		demo.cloneNetwork();
		demo.loadNetwork();
		demo.loadIncompleteNetwork();
		demo.saveLoadNodeInfo();
		demo.nodeMethods();
	}

}
