package org.leores.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.leores.math.Calculateable;
import org.leores.math.CurveFitter;
import org.leores.math.rand.Binomial;
import org.leores.math.rand.DRand;
import org.leores.math.rand.Exponential;
import org.leores.math.rand.Gamma;
import org.leores.math.rand.Normal;
import org.leores.math.rand.Poisson;
import org.leores.math.rand.RandomEngine;
import org.leores.plot.JGnuplot;
import org.leores.plot.JGnuplot.Plot;
import org.leores.util.TestUtil;
import org.leores.util.U;
import org.leores.util.able.Processable0;
import org.leores.util.able.Processable2;
import org.leores.util.data.DataTable;
import org.leores.util.data.DataTableSet;
import org.leores.util.data.Distribution;

public class MathDemo extends Demo {

	public void randDist() {
		Distribution dist = new Distribution();
		Distribution distSys = new Distribution();
		long seed = System.currentTimeMillis();
		RandomEngine rand = RandomEngine.makeDefault(seed);
		Random randSys = new Random(seed);
		for (int i = 0; i < 1000; i++) {
			dist.put(rand.nextDouble());
			distSys.put(randSys.nextDouble());
		}
		dist.calDist(0.001);
		dist.saveToCSVFile("distRand.csv");
		distSys.calDist(0.001);
		distSys.saveToCSVFile("distRandSys.csv");
	}

	public void randSpeed() {
		final long seed = System.currentTimeMillis();
		final RandomEngine randTWF = RandomEngine.makeDefault(seed);
		final RandomEngine randDR = new DRand(seed);
		final Random randSys = new Random(seed);

		Runnable rTWF = new Runnable() {
			public void run() {
				randTWF.nextDouble();
			}
		};
		Runnable rDR = new Runnable() {
			public void run() {
				randDR.nextDouble();
			}
		};
		Runnable rSys = new Runnable() {
			public void run() {
				randSys.nextDouble();
			}
		};
		TestUtil.compareRunTime(10000, 2, rTWF, rSys, rDR);
	}

	public void genRandNumWithDistribution() {
		RandomEngine rand = RandomEngine.makeDefault();
		Binomial binomial = new Binomial(100, 0.5, rand);
		Poisson poisson = new Poisson(10, rand);
		Normal normal = new Normal(10, 1, rand);
		Exponential exponential = new Exponential(1, rand);
		Gamma gamma = new Gamma(1, 1, rand);
		for (int i = 0; i < 100; i++) {
			log(binomial.nextInt(), poisson.nextInt(), normal.nextDouble(), exponential.nextDouble(), gamma.nextDouble());
		}
	}

	class BinomialSim {
		public int n;
		public double p;
		public RandomEngine rand;

		public BinomialSim(int n, double p, RandomEngine rand) {
			this.n = n;
			this.p = p;
			this.rand = rand;
		}

		public int nextInt() {
			int x = 0;
			for (int i = 0; i < n; i++) {
				if (rand.nextDouble() < p)
					x++;
			}
			return x;
		}

		//The implementation is a variant of Luc Devroye's "Second Waiting Time Method" on page 522 of his text "Non-Uniform Random Variate Generation."
		public int nextInt2() {
			double log_q = Math.log(1.0 - p);
			int x = 0;
			double sum = 0;
			while (true) {
				sum += Math.log(rand.nextDouble()) / (n - x);
				if (sum < log_q) {
					return x;
				}
				x++;
			}
		}
	}

	public void binomialSpeedDist() {
		final RandomEngine rand = RandomEngine.makeDefault();
		//		final int n = 5000;
		//		final double p = 9.4E-8;
		final int n = 10000;
		final double p = 100d / n;
		final Binomial biCal = new Binomial(n, p, rand);
		final BinomialSim biSim = new BinomialSim(n, p, rand);

		log(biCal.nextInt(), biSim.nextInt());

		Processable0<Double> pCal = new Processable0<Double>() {
			public Double process() {
				return (double) biCal.nextInt();
			}
		};
		Processable0<Double> pCalS = new Processable0<Double>() {
			public Double process() {
				return (double) biCal.nextIntS();
			}
		};
		Processable0<Double> pSim = new Processable0<Double>() {
			public Double process() {
				return (double) biSim.nextInt();
			}
		};

		TestUtil.compareRunTime(1000, 3, pCal, pCalS, pSim);

		Distribution distCal = new Distribution("distCal");
		distCal.prep(1.0, 0.0, 200.0);
		Distribution distCalS = new Distribution("distCalS");
		distCalS.prep(1.0, 0.0, 200.0);
		Distribution distSim = new Distribution("distSim");
		distSim.prep(1.0, 0.0, 200.0);
		for (int i = 0; i < 10000; i++) {
			distCal.stat((double) biCal.nextInt());
			distSim.stat((double) biSim.nextInt());
			distCalS.stat((double) biCal.nextIntS());
		}
		distCal.calDist();
		distSim.calDist();
		distCalS.calDist();
		JGnuplot jg = new JGnuplot();
		Plot plot = new Plot("distCal-CalS-Sim");
		DataTableSet dts = plot.addNewDataTableSet("Compare binomial");
		dts.add(distCal.getDataTable(), distCalS.getDataTable(), distSim.getDataTable());
		jg.execute(plot);
	}

	public void binomialSmallP() {
		int n = 5000;
		double p = 9.4E-8;
		RandomEngine rand = RandomEngine.makeDefault();
		Binomial biCal = new Binomial(n, p, rand);
		BinomialSim biSim = new BinomialSim(n, p, rand);
		int nCal = 0, nSim = 0, nCalS = 0;
		for (int i = 0; i < 10000; i++) {
			int ibCal = biCal.nextInt();
			if (ibCal > 0) {
				nCal++;
				log("ibCal: " + ibCal + " nCal: " + nCal);
			}
			int ibCalS = biCal.nextIntS();
			if (ibCalS > 0) {
				nCalS++;
				log("ibCalS:" + ibCalS + " nCalS: " + nCalS);
			}
			int ibSim = biSim.nextInt();
			if (ibSim > 0) {
				nSim++;
				log("ibSim: " + ibSim + " nSim: " + nSim);
			}
		}
		log("nCal: " + nCal + " nSim: " + nSim + " nCalS: " + nCalS);
	}

	public void effectsOfRenewSeed() {
		long t = System.currentTimeMillis();
		RandomEngine rand1 = RandomEngine.makeDefault(t);
		RandomEngine rand2 = RandomEngine.makeDefault(t);
		Distribution dist1 = new Distribution("dist");
		Distribution dist2 = new Distribution("distRenew");
		dist1.prep(1d, 0d, 200d);
		dist2.prep(1d, 0d, 200d);
		final int n = 1000;
		final double p = 100d / n;
		final BinomialSim biSim1 = new BinomialSim(n, p, rand1);
		final BinomialSim biSim2 = new BinomialSim(n, p, rand2);
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 50; j++) {
				dist1.stat((double) biSim1.nextInt());
				dist2.stat((double) biSim2.nextInt());
			}
			rand2.setSeed(System.currentTimeMillis());
		}
		dist1.calDist();
		dist2.calDist();
		JGnuplot jg = new JGnuplot();
		Plot plot = new Plot("effectsOfRenewSeed");
		DataTableSet dts = plot.addNewDataTableSet("effectsOfRenewSeed");
		dts.add(dist1.getDataTable(), dist2.getDataTable());
		jg.execute(plot);
	}

	/**
	 * Use a modified verion of the CurveFitter from the ImageJ project.
	 * 
	 * Use the Simplex method do Curve fitting for all functions. Functions can
	 * be customised through implement the Calculateable interface or using the
	 * evaluation expression.
	 */
	public void curveFitting() {
		RandomEngine rand = RandomEngine.makeDefault();
		double[] x = { 2, 1, 3, 4, 5, 6 };//order does not matter
		double[] y1 = new double[x.length], y2 = new double[x.length], y3 = new double[x.length];
		//create the function value with random noise.
		for (int i = 0; i < x.length; i++) {
			double noise = rand.nextDouble();
			if (i % 2 == 0) {
				noise = -noise;
			}
			y1[i] = 0.1 + x[i] + noise;
			y2[i] = 0.5 + x[i] * x[i] + noise;
			y3[i] = 1 + 2 * x[i] + x[i] * x[i] + 0.5 * Math.log(x[i]) + noise;
		}

		log("x=" + U.parseList(x));
		log("y1=" + U.parseList(y1));
		log("y2=" + U.parseList(y2));
		log("y3=" + U.parseList(y3));
		log("");

		Calculateable cal2 = new Calculateable() {
			public double calculate(double[] params, double x) {
				return params[0] + params[1] * x + params[2] * x * x;
			}
		};

		CurveFitter cf1 = new CurveFitter(x, y1), cf2 = new CurveFitter(x, y2), cf3 = new CurveFitter(x, y3);
		cf3.sPatNumOut = "#.##";//only show two digit decimal.
		log("---cf1---");
		cf1.doFit(cf1.STRAIGHT_LINE);
		log(cf1.getResultString());
		log("---cf2---");
		cf2.doCustomFit(cal2, 3, null);
		log(U.parseList(cf2.getParams()));//only read the first 3 parameters and ignore the last parameter.
		log(cf2.getResultString());
		log("FitGoodness:" + cf2.getFitGoodness() + " Residuals: " + U.parseList(cf2.getResiduals()) + " Standard deviation of the residuals:" + cf2.getSD());
		log("---cf3---");
		String formula = "$p(0)$+$p(1)$*$x$+$p(2)$*$x$^2+$p(3)$*LOG($x$)";
		cf3.doCustomFit(formula, 4, null);
		String rFormula = cf3.getResultFormula();
		log(rFormula);
		log(cf3.getResultString());

		DataTableSet dts;
		DataTable dt;
		List<Double> lx = U.parseList(Double.class, "0.1:0.1:6");
		JGnuplot jg = new JGnuplot() {
			{
				afterStyleVar = "lw1=0;ps2=0;";
			}
		};
		double[] p1 = cf1.getParams(), p2 = cf2.getParams(), p3 = cf3.getParams();

		Plot pcf1 = new Plot("pcf1") {
			{
				xlabel = "x";
				ylabel = "y";
			}
		};
		dts = pcf1.addNewDataTableSet(null);
		dts.addNewDataTable("data", x, y1);
		List<Double> ly1 = new ArrayList<Double>();
		for (int i = 0, mi = lx.size(); i < mi; i++) {
			ly1.add(p1[0] + p1[1] * lx.get(i));
		}
		dts.addNewDataTable("Fitted", lx, ly1);
		jg.execute(pcf1);

		Plot pcf2 = new Plot("pcf2") {
			{
				xlabel = "x";
				ylabel = "y";
			}
		};
		dts = pcf2.addNewDataTableSet(null);
		dts.addNewDataTable("data", x, y2);
		List<Double> ly2 = new ArrayList<Double>();
		for (int i = 0, mi = lx.size(); i < mi; i++) {
			ly2.add(cal2.calculate(cf2.getParams(), lx.get(i)));
		}
		dts.addNewDataTable("Fitted", lx, ly2);
		jg.execute(pcf2);

		Plot pcf3 = new Plot("pcf3") {
			{
				xlabel = "x";
				ylabel = "y";
			}
		};
		dts = pcf3.addNewDataTableSet(null);
		dts.addNewDataTable("data", x, y3);
		List<Double> ly3 = new ArrayList<Double>();
		for (int i = 0, mi = lx.size(); i < mi; i++) {
			String expression = rFormula.replace("x", lx.get(i) + "");
			ly3.add(U.eval1Expression(expression).doubleValue());
		}
		dts.addNewDataTable("Fitted", lx, ly3);
		jg.execute(pcf3);
	}

	public static void demo() {
		MathDemo demo = new MathDemo();
		demo.randDist();
		demo.randSpeed();
		demo.genRandNumWithDistribution();
		demo.binomialSpeedDist();
		demo.binomialSmallP();
		demo.effectsOfRenewSeed();
		demo.curveFitting();
	}

}
