package org.leores.demo;

import org.leores.task.Tasks;
import org.leores.task.Taskss;

public class TaskDemo {

	public void runTask() {
		Taskss taskss = new Taskss();
		taskss.sFLoad = "rolldice.xml";
		taskss.start();
	}

	public void runTaskWay2() {
		Tasks tasks = new Tasks(true);		
		tasks.sFLoad = "rolldice.xml";
		tasks.start();
	}

	public void runTaskSets() {
		Taskss taskss = new Taskss();
		taskss.sFLoad = "rolldice#.xml";
		taskss.start();
	}

	public static void demo() {
		TaskDemo demo = new TaskDemo();
		demo.runTask();
		//demo.runTaskWay2();
		demo.runTaskSets();
	}

}
