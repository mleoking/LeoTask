LeoTask
=======

LeoTask is a fast, flexible and reliable framework for computational research. 

## Features:

* Automatic & parallel parameter space exploration
* Flexible & configuration-based result aggregation
* Programming model focusing only on the key logic
* Reliable & automatic interruption recovery
* Dynamic & cloneable networks structures
* Integration with Gnuplot

## Example Application:

###Code (RollDice.java):

    public class RollDice extends Task {
    	private static final long serialVersionUID = -4612453806484156399L;
    	public Integer nSide; //Number of dice sides
    	public Integer nDice; //Number of dices to roll
    	public Integer sum;
    
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
    			sum += (int) (rand.nextDouble() * nSide);
    		}
    		return rtn;
    	}
    	
    	public static void mDrawPDF(Tasks tTasks) {
    		Task.afterAll(tTasks);
    		Statistics stats = tTasks.getStatistics();
    		DataTableSet dts1 = stats.getDataTableSet(null, "Fig1.*");//Get the data results

    		JGnuplot jg = new JGnuplot() {
    			{
    				terminal = "pdfcairo enhanced dashed size 5,3";//set to output pdf
    				output = "$info$.pdf";
    				beforeStyleVar = "lw=4;";//set the line width to 4
    				extra = "unset grid;";
    			}
    		};
    		plot1.add(dts1);
		dts1.get(0).info = "Sum";
		dts1.get(1).info = "Sum / No. of dices";
		jg.execute(plot1, jg.plot3d);

                ...
	    }
    }

###Configuration (rolldice.xml):

    <Tasks>
      <name val="task-rolldice"/><usage val="0.5"/><nRepeats val="5"/><checkInterval val="4"/>
      <sTaskMethodEnd val="mDrawPDF"/>
      <variables class="org.leores.task.app.RollDice">    
        <nSide val="2;4;6"/>
        <nDice val="2:1:5"/>
      </variables>
      <statistics>
        <members>
          <i><info val="Fig1%plotm+@afterRept@"/><valVar val="sum;#$sum$/$nDice$#"/><parVars val="nSide;nDice"/></i>
          <i><info val="Fig2%plot+@afterRept@"/><valVar val="sum"/><parVars val="nSide"/></i>
          <i><info val="Fig3%plot+@afterRept@"/><valVar val="sum"/><parVars val="nDice"/></i> 
        </members>
      </statistics>
    </Tasks>

To run the example application of Roll Dice, execute the following commnad

    java -jar leotask.jar -load=rolldice.xml

in the "Demo" folder. If you are using a MS windows system, you can also execute "rolldice.bat".

Note: the demo requires [Gnuplot 4.6.5](http://sourceforge.net/projects/gnuplot/files/gnuplot/4.6.5/) installed and its gnuplot command directory included in the
system’s PATH environment variable.


