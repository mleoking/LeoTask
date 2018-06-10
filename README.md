# LeoTask

LeoTask is a parallel task running and results aggregation (MapReduce) framework. It is a free and open-source project designed to facilitate running computational intensive tasks [1]. The framework implements the MapReduce model, allocating tasks to multi-cores of a computer and aggregating results according to a XML based configuration file. The framework includes mechanisms to automatically recover applications from interruptions caused by accidents (e.g. Power Cut). Applications using the framework can continue running after an interruption without losing its calculated results.

[**Download**](leotask/demo/leotask.zip?raw=true) | [**Introduction**](leotask/doc/introduction.pdf?raw=true) | [**Applications**](https://github.com/mleoking/LeoTaskApp) | [**Discussion**](http://groups.google.com/forum/#!forum/leotask) | [**Wiki**](https://github.com/mleoking/LeoTask/wiki)

## Features:

* Automatic & parallel parameter space exploration.
* Flexible & configuration-based result aggregation.
* Programming model focusing only on the key logic.
* Reliable & automatic interruption recovery.
* Ultra lightweight ~ 300KB Jar.

## Utilities:
* [All dynamic & cloneable networks structures](leotask/src/app/org/leores/demo/NetDemo.java): a node, a link, a network, a network set (within which networks can overlap with each other), multiplex networks.
* [Integration with Gnuplot](leotask/src/app/org/leores/demo/JGnuplotDemo.java): hybrid programming with Gnuplot, output statistic results as Gnuplot scripts.
* [Network generation according to common network models](leotask/src/app/org/leores/demo/ModDemo.java): random networks, scale-free networks, etc.
* [DelimitedReader](leotask/src/app/org/leores/demo/DelimitedReaderDemo.java): a sophisticated reader that explores CSV (Comma-Separated Values) files like a database.
* [Fast random number generator based on the Mersenne Twister algorithm](leotask/src/app/org/leores/demo/RandomUtilDemo.java).
* [Versatile curve fitter and function value optimizer (minimizer)](leotask/src/app/org/leores/demo/MathDemo.java).

## Example Application:

Please refer to [the introduction](leotask/doc/introduction.pdf?raw=true) for building an example application using the framework.

### Code (RollDice.java):
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
            sum += (int) (rand.nextDouble() * nSide + 1);
        }
        return rtn;
    }
}
```

### Configuration (rolldice.xml):
```xml
<Tasks>
    <name val="task-rolldice"/><usage val="0.9"/><nRepeats val="2000"/><checkInterval val="4"/>
    <variables class="org.leores.task.app.RollDice">    
        <nSide val="2;4;6"/>
        <nDice val="2:1:5"/><!--from 2 to 5 with a step of 1, i.e. 2;3;4;5 -->
    </variables>
    <statistics>
        <members>
            <i><info val="afterRept@"/><valVar val="sum;#$sum$/$nDice$#"/><parVars val="nSide;nDice"/></i>
            <i><info val="afterRept@"/><valVar val="sum"/><parVars val="nSide"/></i>
            <i><info val="afterRept@"/><valVar val="sum"/><parVars val="nDice"/></i> 
        </members>
    </statistics>
</Tasks>
```

Before running the example application, please install [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) and include the the directory of the command _java_ in the system's _PATH_ environment variable. Windows system users can alternatively download and install (_install.bat_) the all-in-one runtime environment package: [LeoTaskRunEnv](https://github.com/mleoking/LeoTaskApp/releases/download/v1.0.0/LeoTaskRunEnv.zip)

Chang the current directory to the "Demo" folder and then execute the following commnad

    java -jar leotask.jar -load=rolldice.xml

If you are using a MS windows system, you can also execute "rolldice.bat".

## References:

[1] Changwang Zhang, Shi Zhou, Benjamin M. Chain (2015). "[LeoTask: a fast, flexible and reliable framework for computational research](http://arxiv.org/abs/1501.01678)", arXiv:1501.01678. [(PDF)](http://arxiv-web3.library.cornell.edu/pdf/1501.01678v1)



