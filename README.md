LeoTask
=======

LeoTask is a fast, flexible and reliable framework for computational research. Please refer to [the introduction](https://github.com/mleoking/leotask/blob/master/leotask/introduction.pdf?raw=true) for building an example application using the framework.

## Features:

* Automatic & parallel parameter space exploration
* Flexible & configuration-based result aggregation
* Programming model focusing only on the key logic
* Reliable & automatic interruption recovery
* Dynamic & cloneable networks structures
* Integration with Gnuplot

## Example Application:

###Code (RollDice.java):
```java
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
```

###Configuration (rolldice.xml):
```xml
<Tasks>
    <name val="task-rolldice"/><usage val="0.5"/><nRepeats val="5"/><checkInterval val="4"/>
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
```

Before running the example application, please 1) install [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) and [Gnuplot 4.6.5](http://sourceforge.net/projects/gnuplot/files/gnuplot/4.6.5/), and 2) include the the directories of the command _java_ and _gnuplot_ in system’s _PATH_ environment variable.

To run the example application of Roll Dice, chang the current directory to the "Demo" folder and then execute the following commnad

    java -jar leotask.jar -load=rolldice.xml

If you are using a MS windows system, you can also execute "rolldice.bat".

