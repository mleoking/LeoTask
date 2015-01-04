package org.leores.net;

import java.io.Serializable;

import org.leores.util.*;
import org.leores.util.able.NewInstanceable;

public class Link extends Element {
	private static final long serialVersionUID = 4225350389021346030L;

	public static class Flag implements Serializable {
		private static final long serialVersionUID = 1600634581856522821L;
		public static final int IN = 1; // Binary 00001
		public static final int OUT = 2; // Binary 00010
		public static final int UNDIRECTED = 4; // Binary 00100
	}

	public static class NewLink implements NewInstanceable<Link>, Serializable {
		private static final long serialVersionUID = -1974351908171133995L;
		public static Link link;

		/**
		 * Clone is faster than the new Object construction. As the construction
		 * has to call a series of constructors, not only the constructor of the
		 * class itself but also the empty constructors of all its parent
		 * classes!
		 * 
		 * @return
		 */
		public Link newInstance() {
			if (link == null) {
				link = new Link(null);
			}
			return link.newClone();
		}
	}

	public static Integer nLink = 0;
	public Network net;
	public Boolean bDirected;
	public Node from;
	public Node to;
	public Double weight;

	public Link() {
		this(null, null, null, null, null, null, null);
	}

	public Link(Integer id) {
		this(id, null, null, null, null, null, null);
	}

	public Link(Boolean bDirected, Node from, Node to, String info) {
		this(null, null, bDirected, from, to, null, info);
	}

	public Link(Integer id, Network net, Boolean bDirected, Node from, Node to, Double weight, String info) {
		initial(id, net, bDirected, from, to, weight, info);
		nLink++;
	}

	protected boolean initial(Integer id, Network net, Boolean bDirected, Node from, Node to, Double weight, String info) {
		boolean rtn = true;
		this.id = id;
		this.net = net;
		this.bDirected = bDirected;
		this.from = from;
		this.to = to;
		this.weight = weight;
		lInfo(info);
		return rtn;
	}

	protected boolean initial(Boolean bDirected, Node from, Node to, Double weight, String info) {
		return initial(null, null, bDirected, from, to, weight, info);
	}

	/**
	 * newClone set the new copy id to null;
	 * 
	 * @return
	 */
	public Link newClone() {
		Link rtn = null;

		try {
			rtn = (Link) super.clone();
			//java will new copies for Boolean rather than copy the reference.
			rtn.id = null;
			rtn.net = null;
			rtn.from = null;
			rtn.to = null;
		} catch (CloneNotSupportedException e) {
			log(e);
		}

		return rtn;
	}

	public static void clear() {
		nLink = 0;
	}

	public String toStr() {
		String rtn = null;

		if (from != null && to != null && sDe != null) {
			beforeSaveInfo();
			String sExtra = "";
			String sInfo = sInfo();
			if (sInfo != null) {//We have to use sInfo() here rather than info because Element.beforeSaveInfo is called in sInfo().
				sExtra = sDe + weight + sDe + sInfo;
			} else if (weight != null) {
				sExtra = sDe + weight;
			}
			rtn = from.id + sDe + to.id + sDe + bDirected + sExtra;
		}

		return rtn;
	}

	public Node getOtherNode(Node node) {
		Node rtn = null;

		if (node != null && from != null && to != null) {
			if (from == node) {
				rtn = to;
			} else if (to == node) {
				rtn = from;
			}
		}

		return rtn;
	}

	public boolean equals(Link link) {
		boolean rtn = false;

		if (link != null && bDirected == link.bDirected) {
			if (from == link.from && to == link.to) {
				rtn = true;
			} else if (bDirected == false && to == link.from && from == link.to) {
				rtn = true;
			}
		}

		return rtn;
	}

	public boolean add() {
		boolean rtn = false;

		if (net != null && bDirected != null && from != null && to != null) {
			rtn = true;
			if (bDirected) {
				rtn = rtn && from.addLink(this);
				rtn = rtn && to.addLink(this);
			} else {
				rtn = rtn && from.addLink(this);
				if (from != to) {
					rtn = rtn && to.addLink(this);
				}
			}
		}

		return rtn;
	}

	public boolean remove() {
		boolean rtn = true;

		if (net != null && bDirected != null && from != null && to != null) {
			if (bDirected) {
				rtn = rtn && from.removeLink(this);
				rtn = rtn && to.removeLink(this);
			} else {
				rtn = rtn && from.removeLink(this);
				if (from != to) {
					rtn = rtn && to.removeLink(this);
				}
			}
		}

		return rtn;
	}

	public String toString() {
		String rtn = from + "-";
		if (bDirected) {
			rtn += ">";
		}
		rtn += to;
		return rtn;
	}
}
