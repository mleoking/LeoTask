package org.leores.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.leores.util.U;
import org.leores.util.able.NewInstanceable;
import org.leores.util.able.Processable1;

public class Networks extends Element {
	private static final long serialVersionUID = -4149150893833201437L;

	protected static Integer nNetworks = 0;
	protected List<Node> nodes;
	protected List<Network> networks;
	protected HashMap<Integer, Node> mId2ToNode;
	protected NewInstanceable<Node> nIaNode;
	protected NewInstanceable<Link> nIaLink;

	public Networks() {
		this(null, null, null, null, null, null, false);
	}

	public Networks(Integer nNet, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) {
		this(null, nNet, null, null, nIaNode, nIaLink, true);
	}

	public Networks(Integer nNet, Integer nNode, Integer nLink, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) {
		this(null, nNet, nNode, nLink, nIaNode, nIaLink, true);
	}

	public Networks(Integer id, Integer nNet, Integer nNode, Integer nLink, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink, boolean bNewNet) {
		if (id != null) {
			this.id = id;
		} else {
			this.id = 0;
		}
		if (nIaNode != null) {
			this.nIaNode = nIaNode;
		} else {
			this.nIaNode = new Node.NewNode();
		}

		if (nIaLink != null) {
			this.nIaLink = nIaLink;
		} else {
			this.nIaLink = new Link.NewLink();
		}

		if (nNode != null) {
			nodes = new ArrayList<Node>(nNode);
		} else {
			nodes = new ArrayList<Node>();
		}

		mId2ToNode = new HashMap<Integer, Node>();

		networks = new ArrayList<Network>();
		if (nNet != null) {
			networks = new ArrayList<Network>(nNet);
			for (int i = 0; i < nNet; i++) {
				Network net = null;
				if (bNewNet) {
					Integer nNetNode = null;
					if (nNode != null) {
						nNetNode = nNode / nNet;
					}
					net = new Network(i, nNetNode, nLink, this);
				}
				networks.add(net);
			}
		} else {
			networks = new ArrayList<Network>();
		}

		nNetworks++;
	}

	public void createNetworks(int nNet) {
		for (int i = 0; i < nNet; i++) {
			Network net = new Network(nNetworks(), null, null, this, nIaNode, nIaLink);
			networks.add(net);
		}
	}

	/**
	 * The parameter info is copied by direct assign<br>
	 * <br>
	 * netsClone.info=nets.info,
	 * netClone.info=net.info,nodeClone.info=node.info,linkClone.info=link.info<br>
	 * <br>
	 * 
	 * So unless the parameter info is String or of Number types (Double, Long,
	 * ...), it is shared by all cloned copies.
	 * 
	 * @param nIaCNode
	 * @param nIaCLink
	 * @return
	 */

	public Networks clone(NewInstanceable<Node> nIaCNode, NewInstanceable<Link> nIaCLink) {
		Integer nNode = nodes.size();
		Integer nNet = networks.size();
		if (nIaCNode == null) {
			nIaCNode = nIaNode;
		}
		if (nIaCLink == null) {
			nIaCLink = nIaLink;
		}
		Networks rtn = new Networks(id, 0, nNode, null, nIaCNode, nIaCLink, false);
		rtn.info = info;

		for (int i = 0; i < nNode; i++) {
			Node node = nodes.get(i);
			if (node != null) {
				Node nodeClone;
				if (nIaCNode == nIaNode) {//If there are the same Obj (not only of the same class)
					nodeClone = node.newClone();
				} else {
					nodeClone = nIaCNode.newInstance();
					nodeClone.info = node.info;
				}
				rtn.addNode(nodeClone);
			} else {
				rtn.addNullNode();
			}
		}

		for (int i = 0; i < nNet; i++) {
			Network net = networks.get(i);
			Network netClone = null;
			if (net != null) {
				netClone = net.clone(rtn, i, nIaCNode, nIaCLink);
			}
			rtn.networks.add(netClone);
		}

		return rtn;
	}

	/**
	 * clone here is actually rebuild another networks.
	 */
	public Networks clone() {
		return clone(null, null);
	}

	public void clear() {
		nNetworks = 0;
		nodes.clear();
		networks.clear();
		if (mId2ToNode != null) {
			mId2ToNode.clear();
		}
	}

	public String toStr() {
		String rtn = "Networks" + sDe + id + sDe + networks.size() + sDe + nodes.size();
		String sInfo = sInfo();
		if (sInfo != null) {//We have to use sInfo() here rather than info because Element.beforeSaveInfo is called in sInfo().
			rtn += sDe + sInfo;
		}
		return rtn;
	}

	public String toString() {
		return "Nets" + id;
	}

	public boolean saveToFile(String sFile, boolean saveNodeInfo) {
		boolean rtn = false;

		if (sFile != null) {
			try {
				File file = new File(sFile);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fWriter = new FileWriter(sFile);
				fWriter.append(toStr() + "\n");
				if (saveNodeInfo) {
					fWriter.append("NodeInfo\n");
					for (int i = 0, size = nodes.size(); i < size; i++) {
						Node node = nodes.get(i);
						fWriter.append(node.toStr() + "\n");
					}
				}
				fWriter.append("SubNetworks\n");
				int nNet = networks.size();
				for (int i = 0; i < nNet; i++) {
					Network net = networks.get(i);
					if (net != null) {
						net.saveToFile(fWriter, false);
					}
				}
				fWriter.append("EndNetworks\n");
				fWriter.flush();
				fWriter.close();
				rtn = true;
			} catch (IOException e) {
				log(e);
			}
		}

		return rtn;
	}

	public boolean saveToFile(String sFile) {
		return saveToFile(sFile, false);
	}

	public static Networks createFromFile(String sFile, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) {
		Networks rtn = null;

		if (sFile != null) {
			File file = new File(sFile);
			if (file.exists()) {
				try {
					BufferedReader fReader = new BufferedReader(new FileReader(sFile));
					String sLine = fReader.readLine();
					if (sLine != null) {
						sLine = sLine.trim();
						String[] tokens = sLine.split(sDeRegex);
						if (tokens.length >= 3 && "Networks".equals(tokens[0])) {
							Integer id = new Integer(tokens[1]);
							Integer nNet = new Integer(tokens[2]);
							Integer nNode = null;
							boolean bCompleteRecord = false;
							if (tokens.length >= 4) {
								bCompleteRecord = true;
								nNode = new Integer(tokens[3]);
							}
							Networks nets = new Networks(id, nNet, nNode, null, nIaNode, nIaLink, false);
							if (tokens.length >= 5) {
								nets.lInfo(tokens[4]);
							}
							if (bCompleteRecord) {
								nets.createNodes(nNode);
								String fileSection = null;
								boolean bData = false;
								do {
									sLine = fReader.readLine();
									if (sLine != null) {
										sLine = sLine.trim();
										if (fileSection != null) {
											bData = true;
										}
										tokens = sLine.split(sDeRegex);
										if (tokens.length >= 1) {
											if ("NodeInfo".equals(tokens[0]) || "SubNetworks".equals(tokens[0])) {
												fileSection = tokens[0];
												bData = false;
											}
											if (bData) {
												if ("NodeInfo".equals(fileSection)) {
													if (tokens.length >= 1) {
														Integer nid = Integer.parseInt(tokens[0]);
														Integer nid2 = null;
														String ninfo = null;
														if (tokens.length >= 2 && !"null".equals(tokens[1])) {
															nid2 = Integer.parseInt(tokens[1]);
														}
														if (tokens.length >= 3 && !"null".equals(tokens[2])) {
															ninfo = tokens[2];
														}
														Node node = nets.getNode(nid);
														if (node != null) {
															node.initial(nid, nid2, ninfo);
														} else {
															U.tLog("Error NodeInfo: node.id=" + nid + " does not exist!");
														}
													} else {
														nets.log("Wrong format Node data line:" + sLine);
													}
												}
											}
										}
									}

								} while (sLine != null && !"SubNetworks".equals(fileSection));
							}
							for (int i = 0; i < nNet; i++) {
								Network net = Network.createFromFile(null, fReader, nets);
								if (net != null && net.id >= 0 && net.id < nNet) {
									nets.networks.set(net.id, net);
								}
							}
							fReader.close();
							rtn = nets;
						} else {
							U.tLog("Wrong format networks title:" + tokens);
						}
					}
				} catch (Exception e) {
					U.tLog(e);
				}
			} else {
				U.tLog("File does not exist: " + sFile);
			}
		}

		return rtn;
	}

	protected boolean updateId2Map(Node node) {
		boolean rtn = false;

		if (node != null && node.id2 != null) {
			if (!mId2ToNode.containsKey(node.id2)) {
				mId2ToNode.put(node.id2, node);
			}
		}

		return rtn;
	}

	public void addNullNode() {
		nodes.add(null);
	}

	public boolean addNode(Node node) {
		boolean rtn = false;

		if (node != null && node.id == null) {
			node.id = nodes.size();
			updateId2Map(node);
			nodes.add(node);
			rtn = true;
		}

		return rtn;
	}

	public boolean removeNode(Node node) {
		boolean rtn = false;

		if (node != null && node.id >= 0 && node.id < nodes.size()) {
			for (int i = 0, size = networks.size(); i < size; i++) {
				Network net = networks.get(i);
				net.removeNode(node);
			}
			nodes.set(node.id, null);
			rtn = true;
		}

		return rtn;
	}

	/**
	 * Push the net into a nets. The net will change its nets, id, etc.
	 * 
	 * @param net
	 * @return
	 */
	public boolean push(Network net) {
		boolean rtn = false;

		if (net != null && net.nets == null) {
			Integer newNetId = networks.size();
			for (int i = 0, size = net.nNodes(); i < size; i++) {
				Node node = net.getNode(i);
				node.id = null;
				node.changeNetworkId(net.id, newNetId);
				addNode(node);
			}
			net.nets = this;
			net.id = newNetId;
			networks.add(net);
			rtn = true;
		} else {
			log(LOG_ERROR, this + " can not add " + net);
		}

		return rtn;
	}

	/**
	 * Clean those null nodes left by removeNode and update their ids according
	 * to their new posisiton in nodes.
	 * 
	 * @return number of cleaned null nodes.
	 */
	public int cleanNullNodes() {
		int rtn = 0;
		for (int mi = nNodes(), i = mi - 1; i >= 0; i--) {
			if (getNode(i) == null) {
				nodes.remove(i);
				rtn++;
			}
		}
		for (int i = 0, mi = nNodes(); i < mi; i++) {
			Node node = getNode(i);
			node.id = i;
		}
		return rtn;
	}

	/**
	 * Clean those null networks left by removeNetwork(Network) and update their
	 * ids according
	 * to their new posisiton in networks.
	 * 
	 * @return number of cleaned null networks.
	 */
	public int cleanNullNetworks() {
		int rtn = 0;
		for (int mi = nNetworks(), i = mi - 1; i >= 0; i--) {
			if (getNetwork(i) == null) {
				networks.remove(i);
				rtn++;
			}
		}
		for (int i = 0, mi = nNetworks(); i < mi; i++) {
			Network net = getNetwork(i);
			if (net.id != i) {
				if (!net.changeId(i)) {
					rtn = -1;
					log(LOG_ERROR, "Failed to change network id from " + net.id + " to " + i);
				}
			}
		}
		return rtn;
	}

	public boolean removeNetwork(Network network) {
		boolean rtn = false;
		if (network != null && network.nets != null && network.nets.id == id) {
			rtn = networks.set(network.id, null) != null;
		}
		return rtn;
	}

	/**
	 * Merge selected networks to network with ids[0]; You should manually call
	 * cleanNullNetworks() after this function if you want to clean the merged
	 * networks, only leaving network with ids[0]. cleanNullNetworks() will also
	 * change network ids according to their new positions in the networks list.
	 * 
	 * @param ids
	 * @return
	 */
	public boolean mergeNetwork(Integer... ids) {
		boolean rtn = false;
		if (ids.length > 1) {
			Network net = getNetwork(ids[0]);
			for (int i = 1; i < ids.length; i++) {
				Network neti = getNetwork(ids[i]);
				neti.uniteInto(net, true);
				neti.remove();
				removeNetwork(neti);
			}
			//cleanNullNetworks();
		}
		return rtn;
	}

	public Network unite() {
		Integer nNode = nodes.size();
		Integer nNet = networks.size();
		Network rtn = null;

		if (nNet > 0) {
			rtn = new Network(0, nNode, null, null, nIaNode, nIaLink);

			for (int i = 0; i < nNode; i++) {
				Node node = nodes.get(i);
				if (node != null) {
					Node nodeClone = node.newClone();
					nodeClone.id2 = node.id;//has to set id2 here. as uniteInto will use id2 to get node when rtn.nets == null;
					rtn.addNode(nodeClone);
				} else {
					rtn.addNullNode();
				}
			}

			for (int i = 0; i < nNet; i++) {
				Network net = getNetwork(i);
				if (net != null) {
					net.uniteInto(rtn, false);
				}
			}
		}

		return rtn;
	}

	public int nNetworks() {
		return networks.size();
	}

	public int nNetworks(Processable1<Boolean, Network> pa1) {
		return U.sizeSubList(networks, pa1);
	}

	public int nNodes() {
		return nodes.size();
	}

	public int nNodes(Processable1<Boolean, Node> pa1) {
		return U.sizeSubList(nodes, pa1);
	}

	public int nLinks() {
		int rtn = 0;
		for (int i = 0, size = networks.size(); i < size; i++) {
			Network net = networks.get(i);
			rtn += net.nLinks();
		}
		return rtn;
	}

	public Node newNode() {
		return nIaNode.newInstance();
	}

	public Link newLink() {
		return nIaLink.newInstance();
	}

	public Network newNetwork() {
		return new Network(null, null, null, null, nIaNode, nIaLink);
	}

	public Network addNewNetwork() {
		Network rtn = new Network(nNetworks(), null, null, this, nIaNode, nIaLink);
		networks.add(rtn);
		return rtn;
	}

	/**
	 * iNet starts from 0.
	 * 
	 * @param iNet
	 * @return
	 */
	public Network getNetwork(Integer iNet) {
		Network rtn = null;

		if (iNet >= 0 && iNet < networks.size()) {
			rtn = networks.get(iNet);
		}

		return rtn;
	}

	public List<Network> getNetworks(Processable1<Boolean, Network> pa1) {
		return U.subList(networks, pa1);
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Node> getNodes(Processable1<Boolean, Node> pa1) {
		return U.subList(nodes, pa1);
	}

	public Node getNode(Integer iNode) {
		Node rtn = null;

		if (iNode != null && iNode >= 0 && iNode < nodes.size()) {
			return nodes.get(iNode);
		}

		return rtn;
	}

	public Node getNodeById2(Integer iNode2) {
		Node rtn = null;

		if (iNode2 != null) {
			rtn = mId2ToNode.get(iNode2);
		}

		return rtn;
	}

	public int createNodes(Integer nNode) {
		int rtn = 0;

		if (nNode != null) {
			for (int i = 0; i < nNode; i++) {
				Node node = newNode();
				if (addNode(node)) {
					rtn++;
				}
			}
		}

		return rtn;
	}

	public String getDegreeStat() {
		String rtn = "";

		for (int i = 0, mi = networks.size(); i < mi; i++) {
			Network net = networks.get(i);
			rtn += net + "\n";
			rtn += net.getDegreeStat();
		}

		return rtn;
	}

	/**
	 * This function remove clear the link cache list in network to release
	 * memory.<br>
	 * <b>!!Note!! After using clearLinkCache, several functions will not work
	 * properly including, save, clone, etc.. </b> <br>
	 * Link related functions in Network and Networks will stop working. But
	 * node related functions are not affected. Link related functions in Node
	 * still works as usual.
	 */
	public void removeLinkCache() {
		for (int i = 0, mi = networks.size(); i < mi; i++) {
			Network net = networks.get(i);
			net.reomveLinkCache();
		}
	}

	/**
	 * Remove the link cache list in nodes to release
	 * memory.<br>
	 * <b>!!Note!! After using this function, almost all functions in Node.java
	 * will stop working. </b> <br>
	 * Link related functions in Network and Networks will still work.
	 */
	public void removeNodeLinkCache() {
		for (int i = 0, mi = nodes.size(); i < mi; i++) {
			Node node = nodes.get(i);
			node.links = null;
		}
	}

}
