package org.leores.util;

import org.leores.ecpt.WrongParameterException;
import org.leores.util.able.NextRunnable;

public class Worker implements Runnable {
	public ThreadWorkers tTWorkers;
	public NextRunnable tNRunnable;
	public Integer tId;
	public Integer nRun;
	public Boolean bFinished;
	public Runnable tRunnable;

	public Worker(ThreadWorkers tW, NextRunnable nR, Integer id)
			throws WrongParameterException {
		if (tW != null && nR != null && id != null) {
			tTWorkers = tW;
			tNRunnable = nR;
			tId = id;
			nRun = 0;
			bFinished = false;
			tRunnable = null;
		} else {
			throw new WrongParameterException(tW,nR,id);
		}
	}

	public void run() {
		if (tNRunnable != null) {
			while ((tRunnable = tNRunnable.nextRun()) != null) {
				tRunnable.run();
				tRunnable = null;
				nRun++;
				tTWorkers.after1Run(this);				
			}
		}
		bFinished = true;
		tTWorkers.afterAllRuns(this);		
	}

}
