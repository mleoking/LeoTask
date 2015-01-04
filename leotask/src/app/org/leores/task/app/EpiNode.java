package org.leores.task.app;

import java.io.Serializable;

import org.leores.net.Node;
import org.leores.util.able.NewInstanceable;
import org.leores.util.able.Processable1;

public abstract class EpiNode extends Node {
	private static final long serialVersionUID = -3122036634049424827L;

	public static class EpiNodeSim extends EpiNode {
		private static final long serialVersionUID = 2045331275362579078L;
		public byte state;
		public byte iWay;
		protected static EpiNodeSim tEpiNodeSim;

		public static EpiNodeSim tEpiNodeSim() {
			if (tEpiNodeSim == null) {
				tEpiNodeSim = new EpiNodeSim();
			}
			return tEpiNodeSim;
		}

		public static class NewNode implements NewInstanceable<EpiNodeSim>, Serializable {
			private static final long serialVersionUID = 9095673145888595465L;

			public EpiNodeSim newInstance() {
				return (EpiNodeSim) EpiNodeSim.tEpiNodeSim().newClone();
			}
		}

		public static class BState implements Processable1<Boolean, Node>, Serializable {
			private static final long serialVersionUID = -7890922999922808727L;
			public byte state;

			public BState(byte state) {
				this.state = state;
			}

			public Boolean process(Node e) {
				EpiNodeSim ee = (EpiNodeSim) e;
				return this.state == ee.state;
			}
		}

		public static class BInfectedWay implements Processable1<Boolean, Node>, Serializable {
			private static final long serialVersionUID = -250325307424988450L;
			public byte iWay;

			public BInfectedWay(byte iWay) {
				this.iWay = iWay;
			}

			public Boolean process(Node e) {
				EpiNodeSim ee = (EpiNodeSim) e;
				return this.iWay == ee.iWay;
			}

		}

		public String sState() {
			return sState(state);
		}

		public String sIWay() {
			return sIWay(iWay);
		}

		@Override
		public void setState(byte state) {
			this.state = state;
		}

		@Override
		public void setInfectedWay(byte iWay) {
			this.iWay = iWay;
		}

		public boolean bState(byte state) {
			return this.state == state;
		}
	}

	public static class EpiNodeEst extends EpiNode {
		private static final long serialVersionUID = 6799603742103915292L;
		//the probability that the node is susceptible
		public float ps;
		//the probability that the node is infected
		public float pi;
		//the probability that the node is recovered
		public float pr;
		//the probability that the node the is not being infected by any other nodes
		public float pnbi;
		protected static EpiNodeEst tEpiNodeEst;

		public static EpiNodeEst tEpiNodeEst() {
			if (tEpiNodeEst == null) {
				tEpiNodeEst = new EpiNodeEst();
			}
			return tEpiNodeEst;
		}

		public static class NewNode implements NewInstanceable<EpiNodeEst>, Serializable {
			private static final long serialVersionUID = -8240563853726461568L;

			public EpiNodeEst newInstance() {
				return (EpiNodeEst) EpiNodeEst.tEpiNodeEst().newClone();
			}
		}

		@Override
		public void setState(byte state) {
			pnbi = 1;
			if (state == SUSCEPTIBLE) {
				ps = 1;
				pi = 0;
				pr = 0;
			} else if (state == INFECTED) {
				ps = 0;
				pi = 1;
				pr = 0;
			} else if (state == RECOVERED) {
				ps = 0;
				pi = 0;
				pr = 1;
			}
		}

		@Override
		public void setInfectedWay(byte iWay) {
		}
	}

	public final static byte SUSCEPTIBLE = 1;
	public final static byte INFECTED = 2;
	public final static byte RECOVERED = 3;

	public final static byte I_NOT = 0;
	public final static byte I_INITIAL = 1;
	public final static byte I_LOCAL = 2;
	public final static byte I_GLOBAL = 3;
	public final static byte I_LOCAL2 = 4;//This is used in analysing the Conficker worm's second local spreading mechanism.
	public final static byte I_INITNET = 5;//Used in the extended epidemic spreading scenario for the external nodes infected by the epidemic in the initially infected network.
	public final static byte I_NETS = 10;

	public EpiNode() {
		super();
	}

	public abstract void setState(byte state);
	
	public abstract void setInfectedWay(byte iWay);

	public static String sState(byte i) {
		String[] states = { "x", "s", "i", "r" };
		return states[i];
	}

	public static String sIWay(byte i) {
		String[] ways = { "n", "i", "l", "g" };
		return ways[i];
	}
}
