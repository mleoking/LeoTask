package org.leores.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.leores.net.Link.Flag;
import org.leores.util.able.NewInstanceable;
import org.leores.util.able.Processable1;

public class Node extends Element {
	private static final long serialVersionUID = -834866406425497004L;

	public static class Degree implements Serializable {
		private static final long serialVersionUID = -5298089483314315431L;
		public Integer in = 0;
		public Integer out = 0;
		public Integer undirected = 0;

		public int all() {
			return in + out + undirected;
		}

		public int max() {
			int rtn = -1;
			if (in > rtn) {
				rtn = in;
			}
			if (out > rtn) {
				rtn = out;
			}
			if (undirected > rtn) {
				rtn = undirected;
			}
			return rtn;
		}
	}

	public static class NodeLinks implements Serializable {
		private static final long serialVersionUID = 3531445922204864535L;
		public List<Link> in;
		public List<Link> out;
		public List<Link> undirected;
	}

	public static class NewNode implements NewInstanceable<Node> {
		private static final long serialVersionUID = 5446695065782547540L;
		public static Node node;

		/**
		 * Clone is faster than the new Object construction. As the construction
		 * has to call a series of constructors, not only the constructor of the
		 * class itself but also the empty constructors of all its parent
		 * classes!
		 * 
		 * @return
		 */
		public Node newInstance() {
			if (node == null) {
				node = new Node(null);
			}
			return node.newClone();
		}
	}

	public static Integer nNode = 0;
	protected Integer id2;
	protected List<NodeLinks> links;

	public Node() {
		this(null, null, null);
	}

	public Node(Integer id) {
		this(id, null, null);
	}

	public Node(Integer id, Integer id2, String info) {// id could be null here
		initial(id, id2, info);
		links = new ArrayList<NodeLinks>();
		nNode++;
	}

	/**
	 * newClone set the new copy id to null and links to be a new ArrayList;
	 * 
	 * @return
	 */
	public Node newClone() {
		Node rtn = null;

		try {
			rtn = (Node) super.clone();
			rtn.id = null;
			rtn.links = new ArrayList<NodeLinks>();
		} catch (CloneNotSupportedException e) {
			log(e);
		}

		return rtn;
	}

	public boolean initial(Integer id, Integer id2, String info) {
		boolean rtn = true;
		this.id = id;
		this.id2 = id2;
		lInfo(info);
		return rtn;
	}

	public boolean initial() {
		return initial(null, null, null);
	}

	public void clear() {
		nNode = 0;
		links.clear();
	}

	/**
	 * Get the degree of the node.
	 * 
	 * @param netId
	 * @return An Degree object.
	 */
	public Degree getDegree(Network net) {
		Degree rtn = null;

		if (net != null) {
			NodeLinks nLinks = prepareNodeLinks(net.id, false);
			if (nLinks != null) {
				rtn = new Degree();
				if (nLinks.in != null) {
					rtn.in = nLinks.in.size();
				}
				if (nLinks.out != null) {
					rtn.out = nLinks.out.size();
				}
				if (nLinks.undirected != null) {
					rtn.undirected = nLinks.undirected.size();
				}
			}
		}

		return rtn;
	}

	protected NodeLinks prepareNodeLinks(Integer netId, boolean createIfNExist) {
		NodeLinks rtn = null;

		if (netId != null && netId >= 0) {
			if (createIfNExist) {
				while (links.size() <= netId) {
					links.add(null);
				}
				NodeLinks nodeLinks = links.get(netId);
				if (nodeLinks == null) {
					nodeLinks = new NodeLinks();
					links.set(netId, nodeLinks);
				}
				rtn = nodeLinks;
			} else if (netId < links.size()) {
				rtn = links.get(netId);
			}
		}

		return rtn;
	}

	protected List<Link> prepareLinkList(NodeLinks nodeLinks, Link link, boolean createIfNExist) {
		List<Link> rtn = null;

		if (nodeLinks != null && link != null) {
			if (!link.bDirected) {
				if (this == link.from || this == link.to) {
					if (createIfNExist && nodeLinks.undirected == null) {
						nodeLinks.undirected = new ArrayList<Link>();
					}
					rtn = nodeLinks.undirected;
				}
			} else if (this == link.from) {
				if (createIfNExist && nodeLinks.out == null) {
					nodeLinks.out = new ArrayList<Link>();
				}
				rtn = nodeLinks.out;
			} else if (this == link.to) {
				if (createIfNExist && nodeLinks.in == null) {
					nodeLinks.in = new ArrayList<Link>();
				}
				rtn = nodeLinks.in;
			}
		}

		return rtn;
	}

	public Link getLink(Link link) {
		Link rtn = null;

		if (link != null && link.net != null) {
			NodeLinks nLinks = prepareNodeLinks(link.net.id, false);
			List<Link> lLink = prepareLinkList(nLinks, link, false);
			if (lLink != null) {
				for (int i = 0, size = lLink.size(); i < size; i++) {
					Link eLink = lLink.get(i);
					if (link.equals(eLink)) {
						rtn = eLink;
						break;
					}
				}
			}
		}

		return rtn;
	}

	public boolean addLink(Link link) {
		boolean rtn = false;

		if (link != null && link.net != null && getLink(link) == null) {
			NodeLinks nLinks = prepareNodeLinks(link.net.id, true);
			List<Link> lLink = prepareLinkList(nLinks, link, true);
			if (lLink != null) {
				lLink.add(link);
				rtn = true;
			}
		}

		return rtn;
	}

	public boolean removeLink(Link link) {
		boolean rtn = false;

		if (link != null && link.net != null) {
			NodeLinks nLinks = prepareNodeLinks(link.net.id, false);
			List<Link> lLink = prepareLinkList(nLinks, link, false);
			if (lLink != null) {
				rtn = lLink.remove(link);
			}

		}

		return rtn;
	}

	protected boolean removeLink(List<Link> lLink) {
		boolean rtn = false;

		if (lLink != null) {
			for (int i = 0, size = lLink.size(); i < size; i++) {
				Link link = lLink.get(i);
				//Here we can not use link.net.removeLink(link) as it will remove the link of this node as well.
				//It will cause null pointer error here because some of the item of lLink is removed before this loop finishes.
				Node node = link.getOtherNode(this);
				if (node != null) {
					node.removeLink(link);
					rtn = true;
				}
			}
		}

		return rtn;
	}

	public boolean bInNetwork(Network net) {
		boolean rtn = false;

		if (net != null) {
			NodeLinks nLinks = prepareNodeLinks(net.id, false);
			if (nLinks != null) {
				rtn = true;
			}
		}

		return rtn;
	}

	public boolean addNetwork(Network net) {
		boolean rtn = false;

		if (net != null) {
			NodeLinks nLinks = prepareNodeLinks(net.id, true);
			if (nLinks != null) {
				rtn = true;
			}
		}

		return rtn;
	}

	public boolean removeNetwork(Network net) {
		boolean rtn = false;

		if (net != null) {
			NodeLinks nLinks = prepareNodeLinks(net.id, false);
			if (nLinks != null) {
				removeLink(nLinks.in);
				removeLink(nLinks.out);
				removeLink(nLinks.undirected);
				links.set(net.id, null);
				rtn = true;
			}
		}

		return rtn;
	}

	public boolean changeNetworkId(Integer from, Integer to) {
		boolean rtn = false;

		NodeLinks nLinks = links.get(from);
		if (nLinks != null) {
			links.set(from, null);
			while (links.size() <= to) {
				links.add(null);
			}
			NodeLinks nLinksTo = links.get(to);
			if (nLinksTo == null) {
				rtn = true;
				links.set(to, nLinks);
			} else {
				rtn = false;
			}

		}

		return rtn;
	}

	protected boolean getLinks(List<Link> lTo, List<Link> lFrom, Processable1<Boolean, Link> pa1) {
		boolean rtn = false;

		if (lFrom != null) {
			for (int i = 0, size = lFrom.size(); i < size; i++) {
				Link link = lFrom.get(i);
				if (pa1 == null || pa1.process(link)) {
					lTo.add(link);
				}
			}
			rtn = true;
		}

		return rtn;
	}

	public List<Link> getLinks(Network net, Processable1<Boolean, Link> pa1) {
		List<Link> rtn = null;

		if (net != null) {
			NodeLinks nLinks = prepareNodeLinks(net.id, false);
			if (nLinks != null) {
				rtn = new ArrayList<Link>();
				getLinks(rtn, nLinks.in, pa1);
				getLinks(rtn, nLinks.out, pa1);
				getLinks(rtn, nLinks.undirected, pa1);
			}
		}

		return rtn;
	}

	/**
	 * Check whether tNode in the parameter is linked by the links in lLink.
	 * (tNode could be either From or To node of a link)
	 * 
	 * @param lLink
	 * @param tNode
	 * @return
	 */
	protected boolean bLinked(List<Link> lLink, Node tNode) {
		boolean rtn = false;

		if (lLink != null) {
			for (int i = 0, size = lLink.size(); i < size; i++) {
				Link link = lLink.get(i);
				Node node = link.getOtherNode(this);
				if (node == tNode) {
					rtn = true;
					break;
				}
			}
		}

		return rtn;
	}

	protected boolean bLinked(NodeLinks nLinks, Node tNode, int flags) {
		boolean rtn = false;

		if (nLinks != null) {
			if ((!rtn) && (flags & Flag.IN) > 0) {
				rtn = bLinked(nLinks.in, tNode);
			}
			if ((!rtn) && (flags & Flag.OUT) > 0) {
				rtn = bLinked(nLinks.out, tNode);
			}
			if ((!rtn) && (flags & Flag.UNDIRECTED) > 0) {
				rtn = bLinked(nLinks.undirected, tNode);
			}
		}

		return rtn;
	}

	/**
	 * According to flags, check whether this node has a IN/OUT/UNDIRECTED link
	 * from/to/with tNode in net
	 * 
	 * @param net
	 *            net=null means all networks
	 * @param tNode
	 * @param flags
	 *            Flag.IN, Flag.OUT, Flag.UNDIRECTED
	 * @return
	 */

	public boolean bLinked(Network net, Node tNode, int flags) {
		boolean rtn = false;

		//flags > 0 means there is at least a IN/OUT/UNDIRECTED flag raised.
		if (tNode != null && flags > 0) {
			NodeLinks nLinks = null;
			if (net != null) {
				nLinks = prepareNodeLinks(net.id, false);
				rtn = bLinked(nLinks, tNode, flags);
			} else if (links != null) {
				//search through all networks
				for (int i = 0, size = links.size(); i < size; i++) {
					nLinks = links.get(i);
					rtn = bLinked(nLinks, tNode, flags);
					if (rtn) {
						break;
					}
				}
			}
		}

		return rtn;
	}

	/**
	 * Check whether the node nDest is reachable by the current node in net.
	 * 
	 * @param net
	 *            net=null means all networks
	 * @param nDst
	 * @return
	 */
	public boolean bReachable(Network net, Node nDst) {
		boolean rtn = bLinked(net, nDst, Flag.OUT | Flag.UNDIRECTED);
		return rtn;
	}

	/**
	 * Check whether the node nSrc is reachable to the current node in net.
	 * 
	 * @param net
	 *            net=null means all networks
	 * @param nSrc
	 * @return
	 */
	public boolean bReachableToMe(Network net, Node nSrc) {
		boolean rtn = bLinked(net, nSrc, Flag.IN | Flag.UNDIRECTED);
		return rtn;
	}

	protected boolean getLinkedNodes(List<Node> lNode, List<Link> lLink, Processable1<Boolean, Node> pa1) {
		boolean rtn = false;

		if (lLink != null) {
			for (int i = 0, size = lLink.size(); i < size; i++) {
				Link link = lLink.get(i);
				Node node = link.getOtherNode(this);
				if (pa1 == null || pa1.process(node)) {
					lNode.add(node);
				}
			}
			rtn = true;
		}

		return rtn;
	}

	protected boolean getLinkedNodes(List<Node> lNode, NodeLinks nLinks, Processable1<Boolean, Node> pa1, int flags) {
		boolean rtn = false;

		if (nLinks != null) {
			if ((flags & Flag.IN) > 0) {
				getLinkedNodes(lNode, nLinks.in, pa1);
			}
			if ((flags & Flag.OUT) > 0) {
				getLinkedNodes(lNode, nLinks.out, pa1);
			}
			if ((flags & Flag.UNDIRECTED) > 0) {
				getLinkedNodes(lNode, nLinks.undirected, pa1);
			}
			rtn = true;
		}

		return rtn;
	}

	/**
	 * According to flags, get the nodes that this node has a IN/OUT/UNDIRECTED
	 * link from/to/with in net
	 * 
	 * @param net
	 *            net=null means all networks
	 * @param pa1
	 * @param flags
	 *            Flag.IN, Flag.OUT, Flag.UNDIRECTED
	 * @return
	 */

	public List<Node> getLinkedNodes(Network net, Processable1<Boolean, Node> pa1, int flags) {
		List<Node> rtn = null;

		if (flags > 0) {
			NodeLinks nLinks = null;
			rtn = new ArrayList<Node>();
			if (net != null) {
				nLinks = prepareNodeLinks(net.id, false);
				getLinkedNodes(rtn, nLinks, pa1, flags);
			} else if (links != null) {
				//search through all networks
				for (int i = 0, size = links.size(); i < size; i++) {
					nLinks = links.get(i);
					getLinkedNodes(rtn, nLinks, pa1, flags);
				}
			}
		}

		return rtn;
	}

	/**
	 * Get the nodes that is reachable by the the current node, including out
	 * and undirected linked nodes, satisfying the condition that pa1 == null ||
	 * pa1.process(node) == true.
	 * 
	 * @param net
	 *            net==null means all networks
	 * @param pa1
	 *            pa1==null means all reachable nodes.
	 * @return
	 */

	public List<Node> getReachableNodes(Network net, Processable1<Boolean, Node> pa1) {
		List<Node> rtn = getLinkedNodes(net, pa1, Flag.OUT | Flag.UNDIRECTED);
		return rtn;
	}

	/**
	 * Get the nodes that is reachable to the current node, including in and
	 * undirected linked nodes, satisfying the condition that pa1 == null ||
	 * pa1.process(node) == true.
	 * 
	 * @param net
	 *            net=null means all networks
	 * @param pa1
	 *            pa1==null means all nodes reachable TO me.
	 * @return
	 */
	public List<Node> getReachableToMeNodes(Network net, Processable1<Boolean, Node> pa1) {
		List<Node> rtn = getLinkedNodes(net, pa1, Flag.IN | Flag.UNDIRECTED);
		return rtn;
	}

	public String toString() {
		String rtn = "Node" + id;
		return rtn;
	}

	public String toStr() {
		String rtn = id + sDe + id2;
		String sInfo = sInfo();
		if (sInfo != null) {
			rtn += sDe + sInfo;
		}
		return rtn;
	}
}
