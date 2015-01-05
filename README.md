LeoTask
=======

LeoTask is a fast, flexible and reliable framework for computational research. 

## Demo:
To run the example application of Roll Dice, execute the following commnad

    java -jar leotask.jar -load=rolldice.xml

in the "Demo" folder. If you are using a MS windows system, you can also execute "rolldice.bat".

Note: the demo requires [Gnuplot 4.6.5](http://sourceforge.net/projects/gnuplot/files/gnuplot/4.6.5/) installed and its gnuplot command directory included in the
system’s PATH environment variable.

## Features:

* Automatic & parallel parameter space exploration
* Flexible & configuration-based result aggregation
* Programming model focusing only on the key logic
* Reliable & automatic interruption recovery
* Dynamic & cloneable networks structures
* Integration with Gnuplot

## Example Application:

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
    }



