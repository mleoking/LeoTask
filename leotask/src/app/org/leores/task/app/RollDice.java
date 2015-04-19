package org.leores.task.app;

import org.leores.plot.JGnuplot;
import org.leores.plot.JGnuplot.Plot;
import org.leores.task.Task;
import org.leores.task.Tasks;
import org.leores.task.Taskss;
import org.leores.util.ClassInfo;
import org.leores.util.data.DataTableSet;
import org.leores.util.data.Statistics;

/**
 * This is the example application.
 * 
 * @author leoking
 * 
 */
public class RollDice extends Task {
	private static final long serialVersionUID = -4612453806484156399L;
	public Integer nSide; //Number of dice sides
	public Integer nDice; //Number of dices to roll
	public Integer sum;

	public static ClassInfo getClassInfo() {
		ClassInfo rtn = new ClassInfo();

		rtn.tClass = RollDice.class;
		rtn.name = "Roll Dice";
		rtn.version = "1.0";
		rtn.license = "FreeBSD License";
		rtn.author = "Changwang Zhang";
		rtn.email = "mleoking@gmail.com";
		rtn.contact = "Dept. of Computer Science, University College London, Gower Street, London WC1E 6BT, United Kingdom.";
		rtn.description = "The example application: Roll Dice.";

		return rtn;
	}

	public boolean prepTask() {
		boolean rtn = nSide > 0 && nDice > 0;
		return rtn;
	}

	public void beforeRept() {
		super.beforeRept();
		sum = 0;
	}

	public boolean step() {
		boolean rtn = iStep <= nDice;
		if (rtn) {
			sum += (int) (rand.nextDouble() * nSide + 1);
		}
		return rtn;
	}

	public static void mDrawPDF(Tasks tTasks) {
		Task.afterAll(tTasks);
		Statistics stats = tTasks.getStatistics();
		//Get the data results
		DataTableSet dts1 = stats.getDataTableSet(null, "Fig1.*");
		DataTableSet dts2 = stats.getDataTableSet(null, "Fig2.*");
		DataTableSet dts3 = stats.getDataTableSet(null, "Fig3.*");

		JGnuplot jg = new JGnuplot() {
			{
				terminal = "pdfcairo enhanced dashed size 5,3";//set to output pdf
				output = "$info$.pdf";
				beforeStyleVar = "lw=4;";//set the line width to 4
				extra = "unset grid;";
			}
		};

		Plot plot1 = new Plot("fig1x") {
			{
				xlabel = "No. of sides";
				ylabel = "No. of dices";
				extra2 = "set key at screen 0.9,0.9,0.9;";
			}
		};
		plot1.add(dts1);
		dts1.get(0).info = "Sum";
		dts1.get(1).info = "Sum / No. of dices";
		jg.execute(plot1, jg.plot3d);

		Plot plot2 = new Plot("fig2x") {
			{
				xlabel = "No. of sides";
				ylabel = "Sum";
				yrange = "[0:15]";
				extra2 = "unset key;";
			}
		};
		plot2.add(dts2);
		String sBarplot = "$style2d$\n$header$\n";
		sBarplot += "plot '-' using 2:xtic(1) w histograms;\n" + "$data(1,2d)$\n";
		jg.execute(plot2, sBarplot);

		Plot plot3 = new Plot("fig3x") {
			{
				xlabel = "No. of dices";
				ylabel = "Sum";
				extra2 = "unset key;";
			}
		};
		plot3.add(dts3);
		jg.execute(plot3, jg.plot2d);
	}
}
