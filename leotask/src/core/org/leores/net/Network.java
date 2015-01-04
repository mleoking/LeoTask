package org.leores.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.leores.net.Link.Flag;
import org.leores.util.*;
import org.leores.util.able.NewInstanceable;
import org.leores.util.able.Processable1;

public class Network extends Element {
	private static final long serialVersionUID = 31657104899522855L;

	protected static Integer nNetwork = 0;
	protected Networks nets;
	protected List<Node> nodes;
	protected List<Link> links;
	protected HashMap<Integer, Node> mId2ToNode;
	protected NewInstanceable<Node> nIaNode;
	protected NewInstanceable<Link> nIaLink;
	public List<Node.Degree> lDegree;

	public Network() {
		this(null, null, null, null, null, null);
	}

	public Network(Integer id) {
		this(id, null, null, null, null, null);
	}

	public Network(NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) {
		this(null, null, null, null, nIaNode, nIaLink);
	}

	public Network(Integer nNode, Integer nLink, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) {
		this(null, nNode, nLink, null, nIaNode, nIaLink);
	}

	public Network(Integer id, Integer nNode, Integer nLink, Networks nets) {
		this(id, nNode, nLink, nets, nets.nIaNode, nets.nIaLink);
	}

	public Network(Integer id, Integer nNode, Integer nLink, Networks nets, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) {
		if (id != null) {
			this.id = id;
		} else {
			//0 Would avoid each node store unnecessary null in its links list.
			this.id = 0;
		}

		if (nNode != null) {
			nodes = new ArrayList<Node>(nNode);
		} else {
			nodes = new ArrayList<Node>();
		}

		if (nLink != null) {
			links = new ArrayList<Link>(nLink);
		} else {
			links = new ArrayList<Link>();
		}

		this.nets = nets;

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

		mId2ToNode = new HashMap<Integer, Node>();
		lDegree = new ArrayList<Node.Degree>();

		nNetwork++;
	}

	public boolean changeId(Integer newId) {
		boolean rtn = false;

		if (newId != id) {
			rtn = true;
			for (int i = 0, maxi = nNodes(); i < maxi; i++) {
				Node node = nodes.get(i);
				boolean bChanged = node.changeNetworkId(id, newId);
				if (!bChanged) {
					rtn = false;
					log(LOG_ERROR, this + " id change from " + id + " to " + newId + " failed!");
					break;
				}
			}

			if (rtn) {
				id = newId;
			}
		}

		return rtn;
	}

	public boolean remove() {
		boolean rtn = true;
		for (int i = 0, mi = nNodes(); i < mi; i++) {
			Node node = nodes.get(i);
			boolean bRemoved = node.removeNetwork(this);
			if (!bRemoved) {
				rtn = false;
				log(LOG_ERROR, this + " remove from node " + i + " failed!");
				break;
			}
		}
		return rtn;
	}

	/**
	 * A copy of the current network would be united into the net and returned.
	 * 
	 * @param net
	 * @param uniteNodes
	 * @return
	 */
	public boolean uniteInto(Network net, boolean uniteNodes) {
		boolean rtn = false;

		if (net != null) {
			rtn = true;
			Integer nNode = nodes.size();
			Integer nLink = links.size();

			if (net.nets == null) {
				if (uniteNodes) {
					for (int i = 0; i < nNode; i++) {
						Node node = nodes.get(i);
						if (node != null) {
							Node nodeClone;
							if (net.nIaNode == nIaNode) {
								nodeClone = node.newClone();
							} else {
								nodeClone = net.nIaNode.newInstance();
								nodeClone.info = node.info;
							}
							//node.newClone set the clone.id to be null
							//using id2 cause it might be that we are uniting a net within a nets into a net not in a nets/not in the same nets.
							nodeClone.id2 = node.id;//
							net.addNode(nodeClone);
						} else {
							net.addNullNode();
						}
					}
				}
				for (int i = 0; i < nLink; i++) {
					Link link = links.get(i);
					if (link != null) {
						Link linkClone;
						if (net.nIaLink == nIaLink) {
							linkClone = link.newClone();
						} else {
							linkClone = net.nIaLink.newInstance();
							linkClone.bDirected = link.bDirected;
							linkClone.weight = link.weight;
							linkClone.info = link.info;
						}
						//link.bDirected and .info will be automatically cloned. nFrom and nTo should be not null.					
						linkClone.from = net.getNodeById2(link.from.id, false);
						linkClone.to = net.getNodeById2(link.to.id, false);
						net.addLink(linkClone);
					} else {
						net.addNullLink();
					}
				}
			} else {//when merging networks within in a same nets.
				if (uniteNodes) {
					for (int i = 0; i < nNode; i++) {
						//There should not be null in nodes when nets!=null
						Node node = nodes.get(i);
						net.addNode(node.id);
					}
				}
				for (int i = 0; i < nLink; i++) {
					Link link = links.get(i);
					if (link != null) {
						Link linkClone;
						if (net.nIaLink == nIaLink) {
							linkClone = link.newClone();
						} else {
							linkClone = net.nIaLink.newInstance();
							linkClone.bDirected = link.bDirected;
							linkClone.weight = link.weight;
							linkClone.info = link.info;
						}
						//link.bDirected and .info will be automatically cloned. nFrom and nTo should be not null.					
						linkClone.from = net.getNode(link.from.id, false);//Here do not use getNodeById2
						linkClone.to = net.getNode(link.to.id, false);
						net.addLink(linkClone);
					} else {
						net.addNullLink();
					}
				}
			}
		}

		return rtn;
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
	 * @param nets
	 * @param idCloned
	 * @param nIaCNode
	 * @param nIaCLink
	 * @return
	 */
	public Network clone(Networks nets, Integer idCloned, NewInstanceable<Node> nIaCNode, NewInstanceable<Link> nIaCLink) {
		Integer nNode = nodes.size();
		Integer nLink = links.size();
		if (idCloned == null) {
			idCloned = id;
		}
		if (nIaCNode == null) {
			nIaCNode = nIaNode;
		}
		if (nIaCLink == null) {
			nIaCLink = nIaLink;
		}
		Network rtn = new Network(idCloned, nNode, nLink, nets, nIaCNode, nIaCLink);
		rtn.info = info;
		uniteInto(rtn, true);
		return rtn;
	}

	/**
	 * clone here is actually rebuild another network.
	 */
	public Network clone(Networks nets, Integer idCloned) {
		return clone(nets, idCloned, null, null);
	}

	public Network clone() {
		return clone(null, null, null, null);
	}

	public void clear() {
		nNetwork = 0;
		nodes.clear();
		links.clear();
		if (mId2ToNode != null) {
			mId2ToNode.clear();
		}
	}

	public Node newNode() {
		return nIaNode.newInstance();
	}

	public Link newLink() {
		return nIaLink.newInstance();
	}

	public String toStr() {
		String rtn = "Network" + sDe + id + sDe + nodes.size() + sDe + links.size();
		String sInfo = sInfo();
		if (sInfo != null) {//We have to use sInfo() here rather than info because Element.beforeSaveInfo is called in sInfo().
			rtn += sDe + sInfo;
		}
		return rtn;
	}

	public List<Node.Degree> updateDegreeList(Processable1<Boolean, Node> pa1) {
		List<Node.Degree> rtn = lDegree;

		rtn.clear();
		for (int i = 0, size = nodes.size(); i < size; i++) {
			Node node = nodes.get(i);
			if (node != null && (pa1 == null || pa1.process(node))) {
				//degree should not be null here as node in nodes should be in this network!
				Node.Degree degree = node.getDegree(this);
				while (rtn.size() < degree.max() + 1) {
					Node.Degree nd = new Node.Degree();
					rtn.add(nd);
				}
				Node.Degree eNodeDegree;
				eNodeDegree = rtn.get(degree.in);
				eNodeDegree.in++;
				eNodeDegree = rtn.get(degree.out);
				eNodeDegree.out++;
				eNodeDegree = rtn.get(degree.undirected);
				eNodeDegree.undirected++;
			}
		}

		return rtn;
	}

	public List<Integer> getDegreeList(int flags, Processable1<Boolean, Node> pa1) {
		List<Integer> rtn = new ArrayList<Integer>();

		updateDegreeList(pa1);
		for (int i = 0, size = lDegree.size(); i < size; i++) {
			Integer nDegree = 0;
			Node.Degree eDegree = lDegree.get(i);
			if ((flags & Link.Flag.IN) > 0) {
				nDegree += eDegree.in;
			}
			if ((flags & Link.Flag.OUT) > 0) {
				nDegree += eDegree.out;
			}
			if ((flags & Link.Flag.UNDIRECTED) > 0) {
				nDegree += eDegree.undirected;
			}
			rtn.add(nDegree);
		}

		return rtn;
	}

	public List<Integer> getDegreeList(int flags) {
		return getDegreeList(flags, null);
	}

	public String getDegreeStat() {
		String rtn = "";
		int[] flags = new int[] { Link.Flag.IN, Link.Flag.OUT, Link.Flag.UNDIRECTED };
		String[] sFlags = new String[] { "InDegree", "OutDegree", "UndirectedDegree" };

		for (int i = 0; i < flags.length; i++) {
			int degreeSum = 0, degreeSquareSum = 0;
			double ak = 0, ak2 = 0;
			List<Integer> lDegree = getDegreeList(flags[i]);
			for (int j = 0, size = lDegree.size(); j < size; j++) {
				Integer nDegree = lDegree.get(j);
				degreeSum += j * nDegree;
				degreeSquareSum += j * j * nDegree;
			}
			ak = (double) degreeSum / nNodes();
			ak2 = (double) degreeSquareSum / nNodes();
			rtn += sFlags[i] + " avgK:" + ak + " avgK2:" + ak2 + " " + lDegree + "\n";
		}

		return rtn;
	}

	public boolean saveToFile(FileWriter fWriter, boolean saveNodeInfo) throws IOException {
		boolean rtn = false;

		if (fWriter != null) {
			//Output the network title
			fWriter.append(toStr() + "\n");
			//Output the degree statistics. These statistics will be ignored when loading a network.
			fWriter.append(getDegreeStat());
			//Output node ids beloing to this network if it is one of network in a group (fnets).
			int i = 0, nsize = nodes.size(), lsize = links.size();
			if (nets != null) {
				int nIdsPerLine = 20;
				fWriter.append("Nodes\n");
				for (i = 0; i < nsize; i++) {
					Node node = nodes.get(i);
					if (node != null) {
						fWriter.append(node.id + sDe);
					}
					if (i != 0 && i % nIdsPerLine == 0) {
						fWriter.append("\n");
					}
				}
				if (i == 0 || (i - 1) % nIdsPerLine != 0) {
					fWriter.append("\n");
				}
			} else if (saveNodeInfo) {
				fWriter.append("NodeInfo\n");
				for (i = 0; i < nsize; i++) {
					Node node = nodes.get(i);
					fWriter.append(node.toStr() + "\n");
				}
			}
			//Output the links
			fWriter.append("Links\n");
			for (i = 0; i < lsize; i++) {
				Link link = links.get(i);
				if (link != null) {
					fWriter.append(link.toStr() + "\n");
				}
			}
			fWriter.append("EndNetwork\n");
		}

		return rtn;
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
				rtn = saveToFile(fWriter, saveNodeInfo);
				fWriter.flush();
				fWriter.close();
			} catch (IOException e) {
				log(e);
			}
		}

		return rtn;
	}

	public boolean saveToFile(String sFile) {
		return saveToFile(sFile, false);
	}

	protected boolean loadNodeLink(BufferedReader fReader, boolean bCompleteRecord) throws IOException {
		boolean rtn = false;

		if (fReader != null) {
			String sLine;
			String fileSection = null;
			boolean bData = false;
			do {
				sLine = fReader.readLine();
				if (sLine != null) {
					sLine = sLine.trim();
					if (fileSection != null) {
						bData = true;
					}
					String[] tokens = sLine.split(sDeRegex);
					if (tokens.length >= 1) {
						if ("Nodes".equals(tokens[0]) || "NodeInfo".equals(tokens[0]) || "Links".equals(tokens[0]) || "EndNetwork".equals(tokens[0])) {
							fileSection = tokens[0];
							bData = false;
						}
						if (bData) {
							if ("Nodes".equals(fileSection)) {
								for (int i = 0; i < tokens.length; i++) {
									Integer nId = Integer.parseInt(tokens[i]);
									addNode(nId);
								}
							} else if ("NodeInfo".equals(fileSection)) {
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
									Node node = getNode(nid);
									if (node != null) {
										node.initial(nid, nid2, ninfo);
									} else {
										log("Error NodeInfo: node.id=" + nid + " does not exist!");
									}
								} else {
									log("Wrong format Node data line:" + sLine);
								}
							} else if ("Links".equals(fileSection)) {
								if (tokens.length >= 2) {
									Integer iFrom = Integer.parseInt(tokens[0]);
									Integer iTo = Integer.parseInt(tokens[1]);
									Boolean bDirected = false;
									Double weight = null;
									String info = null;
									if (tokens.length >= 3) {
										bDirected = Boolean.parseBoolean(tokens[2]);
									}
									if (tokens.length >= 4 && !"null".equals(tokens[3])) {
										weight = Double.parseDouble(tokens[3]);
									}
									if (tokens.length >= 5 && !"null".equals(tokens[4])) {
										info = tokens[4];
									}
									if (bCompleteRecord) {
										addLink(bDirected, iFrom, iTo, weight, info);
									} else {
										createNodeLink(bDirected, iFrom, iTo, weight, info);
									}
								} else {
									log("Wrong format Link data line:" + sLine);
								}
							}
						}
					}
				}
			} while (sLine != null && !"EndNetwork".equals(fileSection));
			rtn = true;
		}

		return rtn;
	}

	protected static Network createFromFile(Integer tId, BufferedReader fReader, Networks nets, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) throws IOException {
		Network rtn = null;

		if (fReader != null) {
			String sLine;
			boolean bNetFound = false;
			String[] tokens = null;

			sLine = fReader.readLine();
			while (sLine != null) {
				sLine = sLine.trim();
				tokens = sLine.split(sDeRegex);
				if (tokens.length >= 2 && "Network".equals(tokens[0])) {
					bNetFound = true;
					break;
				}
				sLine = fReader.readLine();
			}

			if (bNetFound) {
				boolean bCompleteRecord;
				Integer iNet = new Integer(tokens[1]);
				if (tId == null || tId.equals(iNet)) {
					Network network = null;
					if (tokens.length >= 4) {
						bCompleteRecord = true;
						Integer nNode = Integer.parseInt(tokens[2]);
						Integer nLink = Integer.parseInt(tokens[3]);
						network = new Network(iNet, nNode, nLink, nets, nIaNode, nIaLink);
						if (nets == null) {
							network.createNodes(nNode);
						}
					} else {
						bCompleteRecord = false;
						network = new Network(iNet, null, null, nets, nIaNode, nIaLink);
					}
					if (tokens.length >= 5) {
						network.lInfo(tokens[4]);
					}

					network.loadNodeLink(fReader, bCompleteRecord);
					rtn = network;
				}
			}
		}

		return rtn;
	}

	protected static Network createFromFile(Integer tId, BufferedReader fReader, Networks nets) throws IOException {
		return createFromFile(tId, fReader, nets, nets.nIaNode, nets.nIaLink);
	}

	public static Network createFromFile(String sFile, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) {
		Network rtn = null;

		if (sFile != null) {
			File file = new File(sFile);
			if (file.exists()) {
				try {
					BufferedReader fReader = new BufferedReader(new FileReader(sFile));
					rtn = createFromFile(null, fReader, null, nIaNode, nIaLink);
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

	public void addNullLink() {
		links.add(null);
	}

	public boolean addNode(Node node) {
		boolean rtn = false;

		if (node != null && !(node.bInNetwork(this))) {
			if (nets != null) {
				nets.addNode(node);
				node.addNetwork(this);
				nodes.add(node);
				rtn = true;
			} else if (node.id == null) { // If nets.add is executed node should get an id afterwards.
				node.id = nodes.size();
				updateId2Map(node);
				node.addNetwork(this);
				nodes.add(node);
				rtn = true;
			}
		} else {
			/*
			 * addLink and createNodeLink both call this function without
			 * checking whether the parameter node is in this net. So that, it
			 * would be normal if the function is called with a node that is
			 * already in this net.
			 */
			//log(LOG_WARNING, this + " node id: " + id + " is not found or " + node + " is already in " + this);
		}

		return rtn;
	}

	public boolean addNode(Integer id) {
		boolean rtn = false;
		Node node = getNode(id, false);

		if (node != null && !(node.bInNetwork(this))) {
			rtn = addNode(node);
		} else {
			log(LOG_WARNING, this + " node id: " + id + " is not found or " + node + " is already in " + this);
		}

		return rtn;
	}

	/**
	 * It is very costly to remove a node.
	 * 
	 * @param node
	 * @return
	 */
	public boolean removeNode(Node node) {
		boolean rtn = false;

		if (node != null && node.bInNetwork(this)) {
			rtn = true;
			for (int i = 0, mi = links.size(); i < mi; i++) {
				Link link = links.get(i);
				if (link != null && (link.from == node || link.to == node)) {
					links.set(i, null);
				}
			}
			rtn = rtn & node.removeNetwork(this);
			if (nets != null) {
				//When networks exist, a network does not need to keep the node's index in the net's nodes list;
				rtn = rtn & nodes.remove(node);
			} else {
				nodes.set(node.id, null);
			}
		}

		return rtn;
	}

	/**
	 * Clean those null nodes left by removeNode and update their ids according
	 * to their new posisiton in nodes.
	 * 
	 * @return number of cleaned null nodes. -1 means clean failed.
	 */
	public int cleanNullNodes() {
		int rtn = 0;
		if (nets != null) {
			rtn = -1;
			//log(LOG_WARNING, "Can not clean null nodes: network " + id + " is in networks " + nets.id);
		} else {
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
		}
		return rtn;
	}

	public int cleanNullLinks() {
		int rtn = 0;
		for (int mi = nLinks(), i = mi - 1; i >= 0; i--) {
			if (getLink(i) == null) {
				links.remove(i);
				rtn++;
			}
		}
		for (int i = 0, mi = nLinks(); i < mi; i++) {
			Link link = getLink(i);
			link.id = i;
		}
		return rtn;
	}

	public int cleanNullNodesLinks() {
		return cleanNullNodes() + cleanNullLinks();
	}

	public boolean addLink(Link link) {
		boolean rtn = false;

		if (link != null && link.net == null && link.id == null) {
			link.net = this;
			link.id = links.size();
			addNode(link.from);
			addNode(link.to);
			if (link.add()) {
				links.add(link);
				rtn = true;
			}
		}

		return rtn;
	}

	public boolean removeLink(Link link) {
		boolean rtn = false;

		if (link != null && link.net == this && link.id != null) {
			if (link.remove()) {
				links.set(link.id, null);
				rtn = true;
			}
		}

		return rtn;
	}

	public Node getNodeInNet(Integer iNodeInNet) {
		Node rtn = null;
		if (iNodeInNet != null && iNodeInNet >= 0 && iNodeInNet < nodes.size()) {
			rtn = nodes.get(iNodeInNet);
		}
		return rtn;
	}

	public Node getNode(Integer iNode, boolean bInThisNet) {
		Node rtn = null;

		if (nets != null) {
			rtn = nets.getNode(iNode);
			if (bInThisNet && rtn != null && (!rtn.bInNetwork(this))) {
				rtn = null;
			}
		} else {
			rtn = getNodeInNet(iNode);
		}

		return rtn;
	}

	public Node getNode(Integer iNode) {
		return getNode(iNode, true);
	}

	public Node getNodeById2(Integer iNode2, boolean bInThisNet) {
		Node rtn = null;

		if (nets != null) {
			rtn = nets.getNodeById2(iNode2);
			if (bInThisNet && rtn != null && (!rtn.bInNetwork(this))) {
				rtn = null;
			}
		} else if (iNode2 != null) {
			rtn = mId2ToNode.get(iNode2);
		}

		return rtn;
	}

	public Link addLink(Boolean bDirected, Integer iFrom, Integer iTo, Double weight, String info) {
		Link rtn = null;

		if (bDirected != null && iFrom != null && iTo != null) {
			Node nFrom = getNode(iFrom, false);
			Node nTo = getNode(iTo, false);
			if (nFrom != null && nTo != null) {
				//Link link = new Link(bDirected, nFrom, nTo, info);
				Link link = newLink();
				link.initial(bDirected, nFrom, nTo, weight, info);
				if (addLink(link)) {
					rtn = link;
				}
			} else {
				log("Failed to get nodes with id:", iFrom, iTo);
			}
		}

		return rtn;
	}

	public Link addLink(Boolean bDirected, Integer iFrom, Integer iTo, Double weight) {
		return addLink(bDirected, iFrom, iTo, weight, null);
	}

	public Link addLink(Boolean bDirected, Integer iFrom, Integer iTo) {
		return addLink(bDirected, iFrom, iTo, null, null);
	}

	public Link createNodeLink(Boolean bDirected, Integer iFrom2, Integer iTo2, Double weight, String info) {
		Link rtn = null;

		if (bDirected != null && iFrom2 != null && iTo2 != null) {
			Node nFrom = getNodeById2(iFrom2, false);
			Node nTo = getNodeById2(iTo2, false);
			if (nFrom == null) {
				nFrom = newNode();
				nFrom.initial(null, iFrom2, null);
				addNode(nFrom);
			}
			if (nTo == null) {
				nTo = newNode();
				nTo.initial(null, iTo2, null);
				addNode(nTo);
			}
			//Link link = new Link(bDirected, nFrom, nTo, info);
			Link link = newLink();
			link.initial(bDirected, nFrom, nTo, weight, info);
			if (addLink(link)) {
				rtn = link;
			}
		}

		return rtn;
	}

	public Link createNodeLink(Boolean bDirected, Integer iFrom2, Integer iTo2, Double weight) {
		return createNodeLink(bDirected, iFrom2, iTo2, weight, null);
	}

	public Link createNodeLink(Boolean bDirected, Integer iFrom2, Integer iTo2, String info) {
		return createNodeLink(bDirected, iFrom2, iTo2, null, info);
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

	public int nNodes() {
		return nodes.size();
	}

	public int nNodes(Processable1<Boolean, Node> pa1) {
		return U.sizeSubList(nodes, pa1);
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Node> getNodes(Processable1<Boolean, Node> pa1) {
		return U.subList(nodes, pa1);
	}

	public int nLinks() {
		return links.size();
	}

	public int nLinks(Processable1<Boolean, Link> pa1) {
		return U.sizeSubList(links, pa1);
	}

	public List<Link> getLinks() {
		return links;
	}

	public Link getLink(Integer iLink) {
		Link rtn = null;
		if (iLink != null && iLink >= 0 && iLink < nLinks()) {
			rtn = links.get(iLink);
		}
		return rtn;
	}

	public List<Link> getLinks(Processable1<Boolean, Link> pa1) {
		return U.subList(links, pa1);
	}

	public String toString() {
		String rtn = "Net" + id;
		return rtn;
	}

	/**
	 * Remove the link cache list in network to release
	 * memory.<br>
	 * <b>!!Note!! After using this function, several functions will not work
	 * properly including, save, clone, etc.. </b> <br>
	 * Link related functions in Network and Networks will stop working. But
	 * node related functions are not affected. Link related functions in Node
	 * still works as usual.
	 */
	public void reomveLinkCache() {
		links = null;
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
