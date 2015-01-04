package org.leores.task.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.leores.math.rand.Binomial;
import org.leores.net.Network;
import org.leores.net.Networks;
import org.leores.net.Node;
import org.leores.plot.JGnuplot;
import org.leores.plot.JGnuplot.Plot;
import org.leores.task.Task;
import org.leores.task.Tasks;
import org.leores.task.app.EpiNode.EpiNodeEst;
import org.leores.task.app.EpiNode.EpiNodeSim;
import org.leores.util.ClassInfo;
import org.leores.util.ObjArray;
import org.leores.util.RandomUtil;
import org.leores.util.U;
import org.leores.util.able.NewInstanceable;
import org.leores.util.able.Processable1;
import org.leores.util.able.Processable2;
import org.leores.util.data.DataTableSet;
import org.leores.util.data.Statistic;
import org.leores.util.data.Statistics;

public class Epidemic extends Task {
	private static final long serialVersionUID = 3850229033440300313L;
	public Float a0, a1, a2, a3, a4, a5;//a0 proportion of global spreading
	public Float b0, b1, b2, b3, b4, b5;//b0 global infection rate
	public Float g;//g: recovery rate
	public String model = "SIR";
	public String fnets;

	public Integer N, S, I, R, S0, I0, R0;
	public Float t, s, i, r, s0, i0, r0;
	public List<Integer> idsI0, idsR0;
	public Float t1, s1, i1, r1, S1, I1, R1, t2, s2, i2, r2, S2, I2, R2;
	//n*p (i.e. beta*size) threshold below which we use binomial random number generator instead of naive loop for spreading. 
	public Float bnp;//bnp=-1 means stop using binomial random number generator.

	public Boolean bSkipInvalid = true;

	protected RandomUtil<Node> RU;
	protected Processable1<Boolean, Node> bInfected;
	protected Processable1<Boolean, Node> bSusceptible;
	protected Processable1<Boolean, Node> bRecovered;
	protected Float[] eb;//effective b rate = b*a if a != null.
	protected Float eb0;//effective global spreading rate = b0*a0 is a0 != null.
	protected Binomial binomial;

	protected NewInstanceable nIaNode = new EpiNode.EpiNodeSim.NewNode();
	protected static Object[][] nIaNodes = new Object[][] { { "Est", new EpiNode.EpiNodeEst.NewNode() } };

	protected Networks networks;
	protected List<Node> nodes;
	protected List<Node> nodesInfected;
	protected List<Node> nodesNewlyInfected;

	protected static Boolean bCacheNets = true;
	protected static Networks tNetworks;
	protected static String tSFNetworks;

	private static final Object tLock = new Object();

	public static ClassInfo getClassInfo() {
		ClassInfo rtn = new ClassInfo();

		rtn.tClass = Epidemic.class;
		rtn.name = "EpiPro";
		rtn.version = "1.0";
		rtn.license = "FreeBSD License";
		rtn.author = "Changwang Zhang";
		rtn.email = "mleoking@gmail.com";
		rtn.contact = "Dept. of Computer Science, University College London, Gower Street, London WC1E 6BT, United Kingdom.";
		rtn.description = "Simulating the epidemic spreading on multiple networks.";

		return rtn;
	}

	public static Networks loadNetworks(String sFile, NewInstanceable<Node> nIaNode) {
		Networks rtn = null;
		if (sFile.indexOf("nets") >= 0) {
			rtn = Networks.createFromFile(sFile, nIaNode, null);
		} else {
			Network net = Network.createFromFile(sFile, nIaNode, null);
			if (net != null) {
				rtn = new Networks();
				rtn.push(net);
			}
		}
		return rtn;
	}

	/**
	 * This function need to use a synchronised tNetLock as it has to ensure it
	 * will clone the network that it just updated. It try to avoid another
	 * threat t2 modifying the network before a threat t1 clone tNetwork just
	 * updated by itself (t1).
	 * 
	 * @return
	 */
	public Networks tNetworksClone() {
		Networks rtn = null;
		synchronized (tLock) {
			//update tNetwork if it is not the same net required by this sim.
			if (tNetworks == null || !tSFNetworks.equals(fnets)) {
				tNetworks = loadNetworks(fnets, nIaNode);
				tSFNetworks = fnets;
			}
			//clone
			if (tNetworks != null) {
				rtn = tNetworks.clone(nIaNode, null);
			}
			return rtn;
		}
	}

	public Networks loadNetworks() {
		if (fnets != null) {
			if (bCacheNets) {
				networks = tNetworksClone();
			} else {
				networks = loadNetworks(fnets, nIaNode);
				//networks.clearLinkCache();//This networks now can not be cloned and saved. Link function only works in Node. 
			}
		} else if (N != null) {
			networks = new Networks(null, nIaNode, null);
			networks.createNodes(N);
		}

		return networks;
	}

	public boolean hasNegative(Float... ns) {
		boolean rtn = false;
		for (int i = 0; i < ns.length; i++) {
			if (ns[i] != null && ns[i] < 0) {
				rtn = true;
				break;
			}
		}
		return rtn;
	}

	public List<Integer> getSIds() {
		List<Integer> rtn = new ArrayList<Integer>();
		List<Node> susceptibleNodes = networks.getNodes(bSusceptible);
		if (susceptibleNodes != null) {
			for (int i = 0, mi = susceptibleNodes.size(); i < mi; i++) {
				Node node = susceptibleNodes.get(i);
				rtn.add(node.getId());
			}
		}
		return rtn;
	}

	public List<Integer> getIIds() {
		List<Integer> rtn = new ArrayList<Integer>();
		List<Node> infectedNodes = networks.getNodes(bInfected);
		if (infectedNodes != null) {
			for (int i = 0, mi = infectedNodes.size(); i < mi; i++) {
				Node node = infectedNodes.get(i);
				rtn.add(node.getId());
			}
		}
		return rtn;
	}

	public List<Integer> getRIds() {
		List<Integer> rtn = new ArrayList<Integer>();
		List<Node> recoveredNodes = networks.getNodes(bRecovered);
		if (recoveredNodes != null) {
			for (int i = 0, mi = recoveredNodes.size(); i < mi; i++) {
				Node node = recoveredNodes.get(i);
				rtn.add(node.getId());
			}
		}
		return rtn;
	}

	public NewInstanceable loadNewInstanceables() {
		for (int i = 0; i < nIaNodes.length && sMethods != null; i++) {
			String sKey = (String) nIaNodes[i][0];
			NewInstanceable nIa = (NewInstanceable) nIaNodes[i][1];
			if (sMethods.indexOf(sKey) >= 0) {
				nIaNode = nIa;
				break;
			}
		}
		return nIaNode;
	}

	public boolean prepTask() {
		boolean rtn = super.prepTask();
		if (rtn && (i0 != null || idsI0 != null || I0 != null)) {
			Float[] a = new Float[] { a1, a2, a3, a4, a5, a0 };
			if (hasNegative(a) && bSkipInvalid) {
				rtn = false;
				//log(LOG_WARNING, "Has negative [a0,a1,a3,a3]:" + U.toStr(a0, a1, a2, a3));				
			} else {
				if (hasNegative(a)) {
					s0 = 0f;
					i0 = 0f;
					r0 = 0f;
					I0 = 0;
					//N = 0;
				}

				loadNewInstanceables();
				loadNetworks();
				if (networks != null) {
					N = networks.nNodes();
					if (bnp == null) {
						bnp = 0.1f;
					}
					eb = new Float[] { b1, b2, b3, b4, b5 };
					for (int i = 0; i < eb.length; i++) {
						if (eb[i] != null && a[i] != null) {
							eb[i] = eb[i] * a[i];
						}
					}
					eb0 = b0;
					if (eb0 != null && a0 != null) {
						eb0 = eb0 * a0;
					}

					bInfected = new EpiNode.EpiNodeSim.BState(EpiNode.INFECTED);
					bSusceptible = new EpiNode.EpiNodeSim.BState(EpiNode.SUSCEPTIBLE);
					bRecovered = new EpiNode.EpiNodeSim.BState(EpiNode.RECOVERED);
					RU = new RandomUtil<Node>(rand);
					binomial = new Binomial(rand);

					nodesInfected = new ArrayList<Node>();
					nodesNewlyInfected = new ArrayList<Node>();

					if (idsI0 != null) {
						i0 = (float) idsI0.size() / N;
					}
					if (I0 != null) {
						i0 = (float) I0 / N;
					} else {
						I0 = Math.round(i0 * N);
					}

					if (r0 == null) {
						r0 = 0f;
					}
					if (idsR0 != null) {
						r0 = (float) idsR0.size() / N;
					}
					if (R0 != null) {
						r0 = (float) R0 / N;
					} else {
						R0 = Math.round(r0 * N);
					}

					s0 = 1 - i0 - r0;//i0 is not null.
					S0 = Math.round(s0 * N);

					initVars();
				} else {
					rtn = false;
					log(LOG_ERROR, "Failed to load networks! [fnets,N]:[" + fnets + "," + N + "].");
				}
			}
		} else {
			log(LOG_ERROR, "one of [i0,I0,idsI0] must be not null: ", i0, I0, idsI0);
			rtn = false;
		}

		return rtn;
	}

	protected void initVars() {
		s = s1 = s2 = s0;
		i = i1 = i2 = i0;
		r = r1 = r2 = r0;
		S = S0;
		I = I0;
		R = R0;
	}

	/**
	 * Initialise node states.
	 */
	protected boolean prepRept() {
		initVars();
		nodes = networks.getNodes();
		nodesInfected.clear();
		nodesNewlyInfected.clear();
		for (int i = 0; i < N; i++) {
			EpiNode eNode = (EpiNode) nodes.get(i);
			eNode.setState(EpiNode.SUSCEPTIBLE);
			eNode.setInfectedWay(EpiNode.I_NOT);
		}
		List<Node> nodesToIR = null;
		if (idsI0 == null) {
			nodesToIR = RU.getNElements(nodes, I + R);
		} else {
			nodesToIR = new ArrayList<Node>();
			for (int i = 0, mi = idsI0.size(); i < mi; i++) {
				int id = idsI0.get(i);
				Node node = nodes.get(id);
				nodesToIR.add(node);
			}
			if (idsR0 != null) {
				for (int i = 0, mi = idsR0.size(); i < mi; i++) {
					int id = idsR0.get(i);
					Node node = nodes.get(id);
					nodesToIR.add(node);
				}
			}
		}

		for (int i = 0; i < I; i++) {
			EpiNode eNode = (EpiNode) nodesToIR.get(i);
			eNode.setState(EpiNode.INFECTED);
			eNode.setInfectedWay(EpiNode.I_INITIAL);
			nodesInfected.add(eNode);

		}
		for (int i = I; i < I + R; i++) {
			EpiNode eNode = (EpiNode) nodesToIR.get(i);
			eNode.setState(EpiNode.RECOVERED);
		}
		t = 0f;

		return super.prepRept();
	}

	/**
	 * 
	 * @param n
	 * @param p
	 * @return -1 when bnp<0: binomial generation is disabled. Otherwise a
	 *         non-negaive binomial number.
	 */
	protected int genBinomial(int n, float p) {
		int rtn = -1;
		if (bnp >= 0) {
			float np = n * p;
			if (np >= n) {
				rtn = n;
			} else if (np <= 0f) {
				rtn = 0;
			} else if (np > bnp) {
				rtn = binomial.generateBinomial(n, p);
			} else {
				rtn = binomial.generateBinomialS(n, p);
			}

		}
		return rtn;
	}

	protected boolean infect(Node nFrom, Node nTo, byte iWay) {
		boolean rtn = false;
		EpiNodeSim enTo = (EpiNodeSim) nTo;
		if (enTo.state == EpiNode.SUSCEPTIBLE) {
			rtn = true;
			enTo.setState(EpiNode.INFECTED);
			enTo.setInfectedWay(iWay);
			nodesNewlyInfected.add(enTo);
			I++;
			S--;
		}
		return rtn;
	}

	/**
	 * 
	 * @param nFrom
	 *            source of the infection. The parameter is not used by default.
	 *            It can be used by child classes to track the infection
	 *            process.
	 * @param lnTo
	 * @param rate
	 * @param iWay
	 */

	protected int infect(Node nFrom, List<Node> lnTo, float rate, byte iWay) {
		int rtn = 0, size = lnTo.size();
		int nNewlyInfected = genBinomial(size, rate);
		if (nNewlyInfected > 0) {
			int[] iNodesToInfected = RU.genN(nNewlyInfected, size);
			for (int i = 0, mi = iNodesToInfected.length; i < mi; i++) {
				Node nTo = lnTo.get(iNodesToInfected[i]);
				if (infect(nFrom, nTo, iWay)) {
					rtn++;
				}
			}
		} else if (nNewlyInfected < 0) {
			rtn = infectNaive(nFrom, lnTo, rate, iWay);
		}
		return rtn;
	}

	/**
	 * 
	 * @param nFrom
	 *            source of the infection. The parameter is not used by default.
	 *            It can be used by child classes to track the infection
	 *            process.
	 * @param lnTo
	 * @param rate
	 * @param iWay
	 */
	protected int infectNaive(Node nFrom, List<Node> lnTo, Float rate, byte iWay) {
		int rtn = 0;
		for (int i = 0, mi = lnTo.size(); i < mi; i++) {
			EpiNode nTo = (EpiNode) lnTo.get(i);
			if (rand.nextDouble() < rate && infect(nFrom, nTo, iWay)) {
				rtn++;
			}
		}
		return rtn;
	}

	/**
	 * 
	 * @param inFrom
	 *            source of the infection. The parameter is not used by default.
	 *            It can be used by child classes to track the infection
	 *            process.
	 * @param inTos
	 * @param iWay
	 */
	protected int infect(Node nFrom, int[] inTos, byte iWay) {
		int rtn = 0;
		for (int j = 0; j < inTos.length; j++) {
			EpiNode nTo = (EpiNode) nodes.get(inTos[j]);
			if (infect(nFrom, nTo, iWay)) {
				rtn++;
			}
		}
		return rtn;
	}

	protected void recover(List<Node> lNodes2Recover, float rate) {
		int size = lNodes2Recover.size();
		int nNewlyRecovered = genBinomial(size, rate);
		if (nNewlyRecovered > 0) {
			int[] idsNewlyRecovered = RU.genN(nNewlyRecovered, size);
			int size2 = size;
			for (int i = 0, mi = idsNewlyRecovered.length; i < mi; i++) {
				EpiNodeSim eNodeRec = null;
				int iRec = idsNewlyRecovered[i];
				//The node to recover has been moved to a position in the front.
				while (iRec >= size2) {//The node to recover can be moved several times.
					//Find the position where the node to recover has been moved to
					iRec = idsNewlyRecovered[size - 1 - iRec];
				}

				eNodeRec = (EpiNodeSim) lNodes2Recover.get(iRec);
				if (model.equals("SIR")) {
					eNodeRec.setState(EpiNode.RECOVERED);
					R++;
				} else if (model.equals("SIS")) {
					eNodeRec.setState(EpiNode.SUSCEPTIBLE);
					S++;
				}
				EpiNodeSim eNodeEnd = (EpiNodeSim) lNodes2Recover.get(size2 - 1);
				lNodes2Recover.set(iRec, eNodeEnd);
				lNodes2Recover.remove(size2 - 1);//remove the last one;
				size2--;
				I--;
			}
		} else if (nNewlyRecovered < 0) {
			recoverNaive(lNodes2Recover, rate);
		}
	}

	protected void recoverNaive(List<Node> lNodes2Recover, float rate) {
		for (int i = lNodes2Recover.size() - 1; i >= 0; i--) {
			if (rand.nextDouble() < rate) {
				EpiNode eNode = (EpiNode) lNodes2Recover.get(i);
				if (model.equals("SIR")) {
					eNode.setState(EpiNode.RECOVERED);
					R++;
				} else if (model.equals("SIS")) {
					eNode.setState(EpiNode.SUSCEPTIBLE);
					S++;
				}
				lNodes2Recover.remove(i);
				I--;
			}
		}
	}

	protected void update() {
		s = (float) S / N;
		i = (float) I / N;
		r = (float) R / N;
		t += 1;
		if (t1 != null && t <= t1) {
			s1 = s;
			i1 = i;
			r1 = r;
			S1 = N * s1;
			I1 = N * i1;
			R1 = N * r1;
		}
		if (t2 != null && t <= t2) {
			s2 = s;
			i2 = i;
			r2 = r;
			S2 = N * s2;
			I2 = N * i2;
			R2 = N * r2;
		}
		nodesInfected.addAll(nodesNewlyInfected);
		nodesNewlyInfected.clear();
	}

	public boolean step() {
		return msSpreadNets();
	}

	public boolean msSpreadNets() {
		boolean rtn = i > 0;
		if (rtn) {
			int nNets = networks.nNetworks();
			for (int i = 0, mi = nodesInfected.size(); i < mi; i++) {
				Node nodei = nodesInfected.get(i);
				for (int j = 0; j < nNets; j++) {
					if (eb[j] != null && eb[j] > 0) {
						Network netj = networks.getNetwork(j);
						List<Node> lReachNodesNetj = nodei.getReachableNodes(netj, null);
						infect(nodei, lReachNodesNetj, eb[j], (byte) (EpiNode.I_NETS + j));
					}
				}
				if (eb0 != null && eb0 > 0) {
					infect(nodei, nodes, eb0, EpiNode.I_GLOBAL);
				}
			}
			recover(nodesInfected, g);
			update();
		}
		return rtn;
	}

	public boolean msSpreadNetsEst() {
		boolean rtn = i * N > 1E-5;
		if (rtn) {
			double pnbi0 = 1.0;
			if (eb0 != null && eb0 > 0) {
				for (int i = 0; i < N; i++) {
					EpiNodeEst nodei = (EpiNodeEst) nodes.get(i);
					pnbi0 *= 1 - eb0 * nodei.pi;
				}
			}
			int nNets = networks.nNetworks();
			for (int i = 0; i < N; i++) {
				EpiNodeEst nodei = (EpiNodeEst) nodes.get(i);
				nodei.pnbi = 1;
				for (int j = 0; j < nNets; j++) {
					if (eb[j] != null && eb[j] > 0) {
						Network netj = networks.getNetwork(j);
						List<Node> nodesToInfectNodei = nodei.getReachableToMeNodes(netj, null);
						for (int k = 0, mk = nodesToInfectNodei.size(); k < mk; k++) {
							EpiNodeEst nodeTINi = (EpiNodeEst) nodesToInfectNodei.get(k);
							nodei.pnbi *= 1 - eb[j] * nodeTINi.pi;
						}
					}
				}
				double pnbi0i = pnbi0;
				if (eb0 != null && eb0 > 0) {
					pnbi0i = pnbi0i / (1 - eb0 * nodei.pi);
				}
				nodei.pnbi *= pnbi0i;
			}
			double nS = 0, nI = 0, nR = 0;
			for (int i = 0; i < N; i++) {
				EpiNodeEst nodei = (EpiNodeEst) nodes.get(i);
				if (model.equals("SIR")) {
					nodei.ps = nodei.ps * nodei.pnbi;
					nodei.pr = nodei.pr + g * nodei.pi;
					nodei.pi = 1 - (nodei.ps + nodei.pr);//1 - (eNode.ps + eNode.pr) is better than 1 - eNode.ps - eNode.pr because the accurary of Java double operation!
					if (nodei.pi > 1 || nodei.pi < 0 || nodei.ps > 1 || nodei.ps < 0 || nodei.pr > 1 || nodei.pr < 0) {
						log("nodei p error [ps, pi, pr] : ", nodei.ps, nodei.pi, nodei.pr);
					}
				} else if (model.equals("SIS")) {
					nodei.ps = nodei.ps * nodei.pnbi + g * nodei.pi;
					nodei.pi = 1 - nodei.ps;
				}
				nS += nodei.ps;
				nI += nodei.pi;
				nR += nodei.pr;
			}
			S = (int) nS;
			I = (int) nI;
			R = (int) nR;
			update();
		}
		return rtn;
	}

	public boolean msSpreadNetsNaive() {
		boolean rtn = i > 0;
		if (rtn) {
			List<Node> lnInfected = networks.getNodes(bInfected);
			Integer nNets = networks.nNetworks();
			for (int i = 0, mi = lnInfected.size(); i < mi; i++) {
				Node nodei = lnInfected.get(i);
				for (int j = 0; j < nNets; j++) {
					if (eb[j] != null && eb[j] > 0) {
						Network netj = networks.getNetwork(j);
						List<Node> lReachNodesNetj = nodei.getReachableNodes(netj, null);
						infect(nodei, lReachNodesNetj, eb[j], (byte) (EpiNode.I_NETS + j));
					}
				}
				if (eb0 != null && eb0 > 0) {
					infect(nodei, nodes, eb0, EpiNode.I_GLOBAL);
				}
			}
			for (int i = 0, size = lnInfected.size(); i < size; i++) {
				EpiNode eNode = (EpiNode) lnInfected.get(i);
				if (rand.nextDouble() < g) {
					if (model.equals("SIR")) {
						eNode.setState(EpiNode.RECOVERED);
						R++;
					} else if (model.equals("SIS")) {
						eNode.setState(EpiNode.SUSCEPTIBLE);
						S++;
					}
					I--;
				}
			}
			update();
		}
		return rtn;
	}

}
