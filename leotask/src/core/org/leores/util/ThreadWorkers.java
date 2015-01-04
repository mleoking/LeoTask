package org.leores.util;

import org.leores.ecpt.WrongParameterException;
import org.leores.util.able.NextRunnable;

public class ThreadWorkers extends Logger {
	public Integer nThreads;
	public NextRunnable tNRunnable;
	public Thread[] threads;
	public Worker[] workers;
	public Integer nRun;
	public Integer nFinishedWorkers;

	public ThreadWorkers(Integer nTs, NextRunnable nR) throws WrongParameterException {
		if (nTs != null && nR != null && nTs > 0) {
			nThreads = nTs;
			tNRunnable = nR;
			nRun = 0;
			nFinishedWorkers = 0;
		} else {
			throw new WrongParameterException(nTs, nR);
		}
		return;
	}

	public int startThreads() {
		int rtn = 0;

		if (nThreads != null && tNRunnable != null && nThreads > 0) {
			nRun = 0;
			nFinishedWorkers = 0;
			workers = new Worker[nThreads];
			threads = new Thread[nThreads];
			for (int i = 0; i < nThreads; i++) {
				workers[i] = (Worker) U.newInstance(Worker.class, this, tNRunnable, i);
				if (workers[i] != null) {
					threads[i] = new Thread(workers[i], "Thread" + i);
					threads[i].start();
					rtn++;
				} else {
					log("Failed to create worker[" + i + "]!");
				}
			}
		}

		return rtn;
	}

	public boolean bFinshed() {
		boolean rtn = false;

		if (nThreads != null && tNRunnable != null && nThreads > 0) {
			if (nFinishedWorkers >= nThreads) {
				rtn = true;
			}
		} else {
			rtn = true;
		}

		return rtn;
	}

	public int doAll() {
		int rtn = 0;

		int nTStarted = startThreads();
		if (nTStarted > 0) {
			while (!bFinshed()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log(e);
				}
			}
			end();
			rtn = nRun;
		}

		return rtn;
	}

	public void end() {
		if (workers != null) {
			for (int i = 0; i < workers.length; i++) {
				workers[i] = null;
			}
		}
		if (threads != null) {
			for (int i = 0; i < threads.length; i++) {
				threads[i] = null;
			}
		}
	}

	public synchronized void after1Run(Worker worker) {
		nRun++;
	}

	public synchronized void afterAllRuns(Worker worker) {
		nFinishedWorkers++;
	}

	public Runnable[] getCurrentRunables() {
		Runnable[] rtn = new Runnable[nThreads];

		for (int i = 0; i < nThreads; i++) {
			rtn[i] = workers[i].tRunnable;
		}

		return rtn;
	}
}
