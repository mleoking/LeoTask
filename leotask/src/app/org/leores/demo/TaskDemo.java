package org.leores.demo;

import org.leores.task.Tasks;
import org.leores.task.Taskss;

public class TaskDemo {

	public void example1() {
		Tasks tasks = new Tasks();
		tasks.sFLoad = "rolldice.xml";
		tasks.start();
	}

	public void example2() {
		Taskss taskss = new Taskss();
		taskss.sFLoad = "rolldice.xml";
		taskss.start();
	}

	public void runTaskSets() {
		Taskss taskss = new Taskss();
		taskss.sFLoad = "taskss-rolldices.xml";
		taskss.start();
	}

	public static void demo() {
		TaskDemo demo = new TaskDemo();
		demo.example1();
		//demo.example2();
		//demo.runTaskSets();
	}

}
