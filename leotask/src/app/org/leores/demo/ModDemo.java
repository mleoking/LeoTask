package org.leores.demo;

import java.util.ArrayList;
import java.util.List;

import org.leores.math.rand.RandomEngine;
import org.leores.net.Link;
import org.leores.net.Network;
import org.leores.net.Networks;
import org.leores.net.ana.Metric;
import org.leores.net.degree.Binomial;
import org.leores.net.degree.DegreeGenerator;
import org.leores.net.degree.NetworkDegreeClone;
import org.leores.net.degree.PowerLaw;
import org.leores.net.mod.Model;
import org.leores.net.mod.Configuration;
import org.leores.net.mod.ER;
import org.leores.net.mod.FullyConnected;
import org.leores.net.mod.SameSubnets;
import org.leores.util.U;

public class ModDemo extends Demo {
	public String sFileOutNet = "ModDemoNet.dat";
	public String sFileOutNet2 = "ModDemoNet2.dat";
	public String sFileOutNet3 = "ModDemoNet3.dat";

	public void printNetMetric(String sFile) {
		Metric metric = new Metric(sFile);
		metric.printMetrics();
	}

	public void mER() {
		Model ermp = new ER(1000, 0.025);
		Network net = ermp.genNetwork(null);
		net.saveToFile(sFileOutNet);

		Model erme = new ER(2000, 1500);
		Network net2 = erme.genNetwork(null);
		net2.saveToFile(sFileOutNet2);
	}

	public void degreeAble(int n, int r) {
		log("n=" + n + " r=" + r);

		DegreeGenerator pl = new PowerLaw(null, 1, (int) Math.sqrt(n), r);
		List<Integer> degrees = new ArrayList<Integer>();

		for (int i = 0; i < n; i++) {
			int k = pl.degree();
			degrees.add(k);
		}
		log("degrees by pl.degree():", degrees);

		degrees.clear();
		for (int i = 0; i < n; i++) {
			int k = pl.degree(i, n);
			degrees.add(k);
		}
		log("degrees by pl.degree(i,n):", degrees);
	}

	public void mConfiguration() {
		int n = 100, r = 3;
		degreeAble(n, r);
		DegreeGenerator pl = new PowerLaw(null, 1, (int) Math.sqrt(n), r);
		Model cm = new Configuration(pl, n);
		Network net = cm.genNetwork(null);
		net.saveToFile(sFileOutNet);
	}

	public void printDegreeStat(String sFile) {
		log(sFile);
		if (sFile.indexOf("nets") >= 0) {
			Networks nets = Networks.createFromFile(sFile, null, null);
			log(nets.getDegreeStat());
		} else {
			Network net = Network.createFromFile(sFile, null, null);
			log(net.getDegreeStat());
		}
	}

	public void nRealization(Model netm, String sFile, int n) {
		String sFileRealization = null;
		for (int i = 0; i < n; i++) {
			sFileRealization = sFile;
			if (n > 1) {
				int idot = sFile.lastIndexOf(".");
				sFileRealization = U.insert(sFile, idot, "-" + i);
			}
			Network net = netm.genNetwork(null);
			net.saveToFile(sFileRealization);
			printDegreeStat(sFileRealization);
		}
	}

	public void genPowerLawNetwork() {
		String sFile = null;
		int n = 10;
		int min = 2;
		int max = 20;
		double r = 3;
		sFile = "net-pl-cm-(" + min + "-" + max + "," + r + "," + n + ").dat";
		DegreeGenerator pl = new PowerLaw(null, min, max, r);
		Model cm = new Configuration(pl, n);
		//nRealization(cm, sFile, 10);
		Network net = cm.genNetwork(null);
		net.saveToFile(sFile);
		printNetMetric(sFile);
	}

	public void genERNetwork() {
		String sFile = null;

		int nNode = 1000;
		int nLink = 2000;
		Model erm_gne = new ER(nNode, nLink);
		sFile = "net-er-gne-(" + nNode + "," + nLink + ").dat";
		//nRealization(erm_gne, sFile, 1);
		Network net = erm_gne.genNetwork(null);
		net.saveToFile(sFile);
	}

	public void genDegreeCloneNetwork() {
		String sFile = "net-er-gne-(1000,2000).dat";
		NetworkDegreeClone ndc = new NetworkDegreeClone(sFile);
		Model cm = new Configuration(ndc, ndc.n);
		nRealization(cm, sFile, 3);
	}

	public void genFullyConnectedNetwork() {
		String sFile = "net-fc-4.dat";
		Model mNet = new FullyConnected(4);
		Network net = mNet.genNetwork(null);
		net.saveToFile(sFile);
	}

	public void genNetworks() {
		String sFile = "fnets-5-er-gne-(100,200)-5-pl-cm-(3-10,3,100).dat";
		Model mNetER = new ER(100, 200);
		DegreeGenerator pl = new PowerLaw(null, 3, 10, 3);
		Model mNetPL = new Configuration(pl, 100);
		Networks nets1 = new Networks();
		for (int i = 0; i < 5; i++) {
			Network netER = mNetER.genNetwork(null);
			nets1.push(netER);
		}
		for (int i = 0; i < 5; i++) {
			Network netPL = mNetPL.genNetwork(null);
			nets1.push(netPL);
		}
		nets1.saveToFile(sFile);

		sFile = "fnets-5-er-gne-(100,200).dat";
		Model mNet = new ER(100, 200);
		SameSubnets mNets = new SameSubnets(5, mNet);
		Networks nets2 = mNets.genNetworks(null);
		nets2.saveToFile(sFile);
	}

	public void genConfigurationNets() {
		int n = 100;
		RandomEngine re = RandomEngine.makeDefault();
		DegreeGenerator dgPL = new PowerLaw(re, 2, 100);
		DegreeGenerator dgER = new Binomial(re, 0, 8, 4.0 / n);
		Model mod = new Configuration(n, dgPL, dgER);
		Networks nets = mod.genNetworks(null);
		log(nets.getDegreeStat());
		nets.saveToFile("nets-" + n + "-pl-er.dat");
	}

	public static void demo() {
		ModDemo demo = new ModDemo();
		demo.mER();
		demo.degreeAble(10, 3);
		demo.mConfiguration();
		demo.genPowerLawNetwork();
		demo.genDegreeCloneNetwork();
		demo.genERNetwork();
		demo.genNetworks();
		demo.genFullyConnectedNetwork();
		demo.genNetworks();
		demo.printDegreeStat("net-er-gne-(1000,2000).dat");
		demo.genConfigurationNets();
	}

}
