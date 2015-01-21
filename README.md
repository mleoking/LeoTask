#LeoTask
=======

LeoTask is a parallel task running and results aggregation framework. It is a free and open-source project designed to facilitate running computational intensive tasks [1]. The framework implements the MapReduce model, allocating tasks to multi-cores of a computer and aggregating results according to a XML based configuration file. The framework includes mechanisms to automatically recover applications from interruptions caused by accidents (e.g. Power Cut). Applications using the framework can continue running after an interruption without losing its calculated results.

## Features:

* Automatic & parallel parameter space exploration
* Flexible & configuration-based result aggregation
* Programming model focusing only on the key logic
* Reliable & automatic interruption recovery
* ...

## Utilities
* Dynamic & cloneable networks structures: a node, a link, a network, and a network set (within which networks can overlap with each other).
* Integration with Gnuplot
* Network generation according to common network models
* DelimitedReader: a sophisticated reader that explores CSV (Comma-separated values) files like a database
* Fast random number generator based on the Mersenne Twister algorithm
* ...

## Example Application:

Please refer to [the introduction](https://github.com/mleoking/leotask/blob/master/leotask/introduction.pdf?raw=true) for building an example application using the framework.

###Code (RollDice.java):
```java
public class RollDice extends Task {
    public Integer nSide; //Number of dice sides
    public Integer nDice; //Number of dices to roll
    public Integer sum;//Sum of the results of nDice dices
   
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
        <nDice val="2:1:5"/><!--from 2 to 5 with a step of 1, i.e. 2;3;4;5 -->
    </variables>
    <statistics>
        <members>
            <i><info val="Fig1%pltm+@afterRept@"/><valVar val="sum;#$sum$/$nDice$#"/>
               <parVars val="nSide;nDice"/></i>
            <i><info val="Fig2%plt+@afterRept@"/><valVar val="sum"/><parVars val="nSide"/></i>
            <i><info val="Fig3%plt+@afterRept@"/><valVar val="sum"/><parVars val="nDice"/></i> 
        </members>
    </statistics>
</Tasks>
```

Before running the example application, please install [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) and include the the directories of the command _java_ in system's _PATH_ environment variable.

Chang the current directory to the "Demo" folder and then execute the following commnad

    java -jar leotask.jar -load=rolldice.xml

If you are using a MS windows system, you can also execute "rolldice.bat".

## References:

[1] Changwang Zhang, Shi Zhou, Benjamin M. Chain (January 2015). "[LeoTask: a fast, flexible and reliable framework for computational research](http://arxiv.org/abs/1501.01678)" (arXiv:1501.01678). Cornell University. [(PDF)](http://arxiv-web3.library.cornell.edu/pdf/1501.01678v1)

