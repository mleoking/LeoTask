LeoTask
=======

LeoTask is a fast, flexible and reliable framework for computational research. 

## Demo:
Two example applications using the framework are included in the package of org.leores.task.app.
To run the example application of Roll Dice, execute the following commnad

    java -jar leotask.jar -load=rolldice.xml

in the Demo folder. If you are using windows system, you can also execute "rolldice.bat". 

Note: the demo requires [Gnuplot 4.6.5](http://sourceforge.net/projects/gnuplot/files/gnuplot/4.6.5/) installed and its gnuplot command directory included in the
system’s PATH environment variable.

The demo will generate 6 files: 1 data file (.csv), 3 gnuplot script files (.plt), and 3 figures (.pdf).

## Features:

* Automatic & parallel parameter space exploration
* Flexible & configuration-based result aggregation
* Programming model focusing only on the key logic
* Reliable & automatic interruption recovery
* Dynamic & cloneable networks structures
* Integration with Gnuplot




