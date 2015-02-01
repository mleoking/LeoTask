package org.leores.task;

import org.leores.util.*;
import org.leores.util.able.NextRunnable;
import org.leores.util.data.Statistics;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * To the programmer, declaring a method as synchronized is essentially the same
 * as wrapping the method body in a synchronized (this) block. If we declare a
 * static method as synchronized, then the lock is obtained on the corresponding
 * Class object. So that, static and method and non-static method are using two
 * different locks. That means, a non-static and static method can be excuted at
 * the same time.
 */

public class Tasks extends Logger implements Serializable, NextRunnable {
	private static final long serialVersionUID = 9166265851516474449L;
	public String name;
	//The default values of the parameters are in Taskss.java
	public Double usage;//percentage of cpu cores to be used
	public Integer nRepeats;//no. of repetitions for a simulation. -1: no limit;0: no repetition;default to be 1;
	public Integer nSteps;//no. of steps. -1: no limit;0: no steps;default to be -1;
	public Long seed;//random seed. -1: random generate a seed
	public Boolean bRenewSeed;//whether to renew seed after each repetition. False by default. Normally false is better than true.
	public Integer nTasks;
	public Integer checkInterval;//-1:no check points
	public Integer nCheckPoints;//0:no check points
	public Date startDate;
	public Long duration;//in milliseconds
	public List<String> dataLevels;//All levels: R;T;S;SA;S%T. S%T: save results for each task individually without merging them.
	public String sPatNumOut;
	public Integer nFinished;

	public String sFLoad;
	public String sFCheckPoint;
	public String sFData;
	public String sFPreFix;

	protected String sTaskMethodStart;
	protected String sTaskMethodEnd;
	protected Boolean bSaveData;
	protected Boolean bPrintData;
	protected Variables variables;
	protected Statistics statistics;
	protected Date endDate;
	protected Integer iTask;
	protected List<Integer> taskIdsToRun;
	protected List<Integer> taskIdsRunning;
	transient protected ThreadWorkers tTWorkers;

	//Staying time after finishing all tasks. 0: do not wait, negative: wait forever, positive: wait for certain milliseconds.
	//Stay will be interrupted when another thread invokes the java.lang.Object.notify() method or the java.lang.Object.notifyAll() method for this object
	protected Integer timeout = 0;//default to be 0;
	//Task apps could use the info and lo to store information needed to be saved to and load from checkpoint files.
	//info and lo have to be Serializable if the tasks were to save checkpoints.
	public LoadHashMap<String, Object> vMap;
	public Object info = "";

	private static final Object tLock = new Object();

	public static ClassInfo getClassInfo() {
		ClassInfo rtn = new ClassInfo();

		rtn.tClass = Tasks.class;
		rtn.name = "LeoTask";
		rtn.version = "1.0.0";
		rtn.license = "FreeBSD License";
		rtn.author = "Changwang Zhang";
		rtn.email = "mleoking@gmail.com";
		rtn.contact = "University College London";
		rtn.description = "Transparent parallel task running and results aggregation system.";

		return rtn;
	}

	public Object vMap(String key) {
		return vMap.get(key);
	}

	public Object v(String key) {
		return vMap(key);
	}

	public Tasks(boolean bLoadDefault) {
		if (bLoadDefault) {
			Taskss taskss = new Taskss();
			taskss.loadValues(this);//load tasks parameters' default values.
		}
		return;
	}

	public Tasks() {
		this(false);
		return;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public boolean loadCmdArgs(String[] args) {
		boolean rtn = false;

		String sFLoadArg = U.getCmdArg("-load=", args);
		String sFCheckPointArg = U.getCmdArg("-checkpoint=", args);

		if (sFLoadArg != null) {
			sFLoad = sFLoadArg;
			rtn = true;
		}

		if (sFCheckPointArg != null) {
			sFCheckPoint = sFCheckPointArg;
			rtn = true;
		}

		return rtn;
	}

	public Object invokeTaskClassMethod(Object obj, String mName, Object... args) {
		Object rtn = null;

		if (variables != null && variables.tClass != null) {
			Class tTaskClass = variables.tClass;
			rtn = U.invokeMethodByName(tTaskClass, obj, mName, false, args);
		}

		return rtn;
	}

	public Object invokeTaskClassStaticMethod(String mName, Object... args) {
		return invokeTaskClassMethod(null, mName, args);
	}

	private synchronized void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		invokeTaskClassStaticMethod("saveObject", oos);
	}

	private synchronized void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException, SecurityException, NoSuchFieldException {
		ois.defaultReadObject();
		invokeTaskClassStaticMethod("loadObject", ois);
	}

	public boolean setLoad(String str) {
		return U.loadFromString(this, str);
	}

	public boolean setLoadVMap(String str) {
		if (U.canEval(str)) {
			str = U.eval(str, this, true);
		}
		if (vMap == null) {
			vMap = new LoadHashMap<String, Object>();
		}
		return U.loadFromString(vMap, str);
	}

	public boolean writeDataTitle(String fn) {
		boolean rtn = false;

		if (fn != null) {
			ClassInfo ci = getClassInfo();
			U.saveToCsvFile(ClassInfo.class, fn);
			U.saveToCsvFile(ci, fn);
			U.saveToCsvFile(this.getClass(), fn);
			U.saveToCsvFile(this, fn);
			ci = (ClassInfo) invokeTaskClassStaticMethod("getClassInfo");
			U.saveToCsvFile(ClassInfo.class, fn);
			U.saveToCsvFile(ci, fn);
			if (variables != null && variables.tClass != null) {
				U.saveToCsvFile(variables.tClass, fn);
				rtn = true;
			}
		}

		return rtn;
	}

	protected boolean beforeLoadCP() {
		boolean rtn = false;

		File file = new File(sFCheckPoint);
		if (file.exists()) {
			rtn = true;
		}

		return rtn;
	}

	protected boolean afterLoadCP(boolean bLoaded, String sFCheckPointBeforeLoad) {
		boolean rtn = false;

		if (bLoaded && U.hasNoNull(variables)) {
			rtn = true;
			startDate = new Date();
			endDate = new Date();
			//restart those still running tasks when the checkpoint is made.
			taskIdsToRun.addAll(taskIdsRunning);
			taskIdsRunning.clear();
			log("Loaded from checkpoint: " + sFCheckPointBeforeLoad + ".");
		} else {
			log(LOG_ERROR, "Invalid checkpoint: " + sFCheckPointBeforeLoad + "! Please delete it! The program might be stopped while saving the checkpoint.");
		}

		return rtn;
	}

	/**
	 * Load the checkpoint from a file.
	 * 
	 * @return <b>-1</b>:there is an invalid checkpoint file, <b>0</b>: there is
	 *         no checkpoint file, <b>1</b>: there is a valid checkpoint file.
	 */

	public synchronized int loadCheckPoint() {
		int rtn = 0;

		if (beforeLoadCP()) {
			rtn = -1;
			String sFCheckPointBeforeLoad = sFCheckPoint;
			if (U.bExistFile(sFCheckPoint)) {
				log("Loading checkpoint: " + sFCheckPointBeforeLoad + " ...");
				boolean bLoaded = U.loadFromSerial(this, sFCheckPointBeforeLoad);
				if (afterLoadCP(bLoaded, sFCheckPointBeforeLoad)) {
					rtn = 1;
				}
			}
		}

		return rtn;
	}

	public String getLoadStr() {
		String rtn;
		String sStartDate = U.format(startDate, "MM/dd@HH:mm");
		rtn = "name=" + name + " nRepeats=" + nRepeats + " seed=" + seed + " nTasks=" + nTasks + " startDate=" + sStartDate + " info=" + info;
		return rtn;
	}

	protected boolean prep() {
		boolean rtn = true;
		int iLoadCP = loadCheckPoint();

		ClassInfo ci = getClassInfo();
		log(ci.toShortStr());

		if (iLoadCP == 0) {
			startDate = new Date();
			iTask = 1;
			nFinished = 0;
			taskIdsToRun = new LinkedList<Integer>();
			taskIdsRunning = new LinkedList<Integer>();

			if (sFLoad != null) {
				rtn = rtn && U.loadFromXML(this, sFLoad);
			}
			rtn = rtn && U.hasNoNull(variables);

			if (rtn) {
				nTasks = variables.nValueSets;
				if (nTasks > 0) {
					if (seed == null || seed == -1) {
						seed = System.currentTimeMillis();
					}
					if (sFPreFix == null) {
						sFPreFix = name + "_" + U.format(startDate, "MMdd@HHmm") + "_";
					}
					sFData = sFPreFix + sFData;
					sFCheckPoint = sFPreFix + sFCheckPoint;
					if (bSaveData) {
						for (int i = 0, size = dataLevels.size(); i < size; i++) {
							String dLevel = dataLevels.get(i);
							if (!dLevel.contains("S")) {//"S" statistic results will write writeDataTitle in the end so that tasks.duration can be recorded.
								String sFDataI = getDataFileName(dLevel);
								writeDataTitle(sFDataI);
							}
						}
					}
					statistics.addPoints(Task.points);
					statistics.addPoints(Task.sStatMethodPointTask, Task.sStatMethodPointTasks);
					statistics.buildStatPointsMap();
				} else {
					rtn = false;
					log("The load file does not include any task variables");
				}
			}
		} else if (iLoadCP == -1) {
			rtn = false;
		}

		//String outString = U.toOutString(this, "", "", "\t", sPatNumOut, true, null);
		//tLog(outString, false);		
		String sLoad = getLoadStr();
		log(sLoad);

		return rtn;
	}

	public int start() {
		if (prep()) {
			int cores = Runtime.getRuntime().availableProcessors();
			int nThreads = (int) (cores * usage);
			if (nThreads <= 0) {
				nThreads = 1;
			}
			log("cores=" + cores + " nThreads=" + nThreads);
			if (sTaskMethodStart != null) {
				invokeTaskClassStaticMethod(sTaskMethodStart, this);
			} else {
				invokeTaskClassStaticMethod("beforeAll", this);
			}
			tTWorkers = (ThreadWorkers) U.newInstance(ThreadWorkers.class, nThreads, this);
			if (tTWorkers != null) {
				tTWorkers.doAll();
				invokeTaskClassStaticMethod("afterDoAll", this);
				if (taskIdsToRun.size() > 0) {//There might be some task ids that are added but no worker picked it up.
					tTWorkers.doAll();
				}
			}
			end();
		}
		return nFinished;
	}

	protected boolean beforeSaveCP() {
		boolean rtn = false;

		//taskIdsToRun.size() == 0 : does not save check points when the tasks are *Starting tasks (taskIdsToRun.size() > 0) either loaded from running tasks in a checkpoint or specified in the load file or program.
		if (nCheckPoints > 0 && checkInterval > 0 && (iTask % checkInterval == 0) && taskIdsToRun.size() == 0) {
			rtn = true;
		}

		return rtn;
	}

	protected boolean afterSaveCP(boolean bSaved) {
		boolean rtn = bSaved;
		if (bSaved && nCheckPoints > 0) {
			int iBefore = iTask - checkInterval * nCheckPoints;
			if (iBefore > 0) {
				deleteCheckPoints(iBefore);
			}
		}
		return rtn;
	}

	public boolean saveCheckPoint(String sFile) {
		boolean bSaved = U.saveToSerial(this, sFile);
		if (bSaved) {
			log("Checkpoint saved to: " + sFile);
		}
		boolean rtn = afterSaveCP(bSaved);
		return rtn;
	}

	public synchronized boolean saveCheckPoint() {
		boolean rtn = false;

		if (beforeSaveCP()) {
			String sFCheckPointNow = U.insert(sFCheckPoint, ".", "." + iTask);
			rtn = saveCheckPoint(sFCheckPointNow);
		}

		return rtn;
	}

	protected boolean deleteCheckPoints(int iBefore) {
		boolean rtn = true;

		String path = ".";
		File folder = new File(path);
		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			String fname = files[i].getName();
			if (fname.endsWith(".ckp") && fname.contains(sFPreFix)) {
				boolean bToDelete = true;
				if (iBefore > 0) {
					bToDelete = false;
					String[] tokens = fname.split("\\.");
					if (tokens.length >= 3) {
						Integer iFile = (Integer) U.newInstance(Integer.class, tokens[1]);
						if (iFile != null) {
							if (iFile <= iBefore) {
								bToDelete = true;
							}
						}
					}
				}
				if (bToDelete) {
					File file = new File(fname);
					if (!file.delete()) {
						rtn = false;
						break;
					}
				}
			}
		}

		return rtn;
	}

	/**
	 * When task!=null, this function is called to save the statistic results of
	 * each task individually without merging them.
	 * 
	 * @param task
	 */
	public synchronized void logStatData(Task task) {
		Statistics stats = statistics;
		String sName = name;
		if (task != null) {
			stats = task.statistics;
			sName += "@" + task.getNameOrId();
		}
		if (stats != null) {
			if (stats.sPatNumOut == null) {
				stats.sPatNumOut = sPatNumOut;
			}
			if (dataLevels.contains("S") || dataLevels.contains("SA")) {
				String logFile = sFData;
				if (bSaveData) {
					if (task == null) {
						writeDataTitle(logFile);
					}
					if (dataLevels.contains("SA")) {
						U.saveToCsvFile(sName + "@SA", logFile);
						stats.saveAggregateToCsvFile(logFile);
					}
					if (dataLevels.contains("S")) {
						U.saveToCsvFile(sName + "@S", logFile);
						stats.saveToCsvFile(logFile);
					}
				}
				if (bPrintData) {
					if (dataLevels.contains("S")) {
						stats.print();
					}
					if (dataLevels.contains("SA")) {
						stats.printAggregate();
					}
				}
			}
		}
	}

	protected void end() {
		Date nowDate = new Date();
		DateInterval di = new DateInterval(nowDate, startDate);

		log("Log statistic data" + " Elapsed Time: " + di.inMinutes() + " Minutes.");
		duration = di.inMilliseconds();
		if (!dataLevels.contains("S%T")) {
			logStatData(null);
		}
		boolean bDeleted = deleteCheckPoints(-1);
		if (!bDeleted) {
			log("Failed to delete checkpoints: *.ckp! Please delete them!");
		}
		if (sTaskMethodEnd != null) {
			invokeTaskClassStaticMethod(sTaskMethodEnd, this);
		} else {
			invokeTaskClassStaticMethod("afterAll", this);//Task.afterAll(this); can not effectively call afterAll in sub class of Task.
		}
		endDate = new Date();
		DateInterval di2 = new DateInterval(endDate, startDate);
		log("Finished " + nFinished + "/" + nTasks + " Tasks in " + di2.inMinutes() + " Minutes.");

		if (timeout != 0) {
			if (timeout > 0) {
				stay(timeout * 1000);
			} else {
				stay();
			}
		}
		return;
	}

	public String getDataFileName(String sLevel) {
		int iDot = sFData.lastIndexOf('.');
		String rtn = U.insert(sFData, iDot, "@" + sLevel);
		return rtn;
	}

	protected synchronized void doLogData(Task task, String sLevel) {
		if (bSaveData) {
			String sFile = getDataFileName(sLevel);
			U.saveToCsvFile(task, sFile, sPatNumOut);
		}
		if (bPrintData) {
			String outString = U.toOutString(task, "", "", "\t", sPatNumOut, false, null);
			log(outString, false);
		}
		return;
	}

	public void logData(Task task, String sLevel) {
		if (dataLevels.contains(sLevel)) {
			doLogData(task, sLevel);
		}
		return;
	}

	public void afterRept(Task task) {
		logData(task, "R");
		return;
	}

	public void afterTask(Task task) {
		logData(task, "T");
		return;
	}

	public synchronized void end(Task task) {
		if (dataLevels.contains("S%T")) {
			if (nFinished == 0 && bSaveData) {
				writeDataTitle(sFData);
			}
			logStatData(task);
		} else if (statistics != null) {
			statistics.merge(task.statistics);
		}
		//remove the first occurrence of the value (not the index) of task.id
		taskIdsRunning.remove((Object) task.id);
		nFinished++;
		return;
	}

	public Class getTaskAppClass() {
		Class rtn = variables.tClass;
		return rtn;
	}

	public synchronized boolean addTaskIdsToRun(Integer taskId) {
		boolean rtn = false;

		if (taskId != null && taskId >= 1 && taskId <= nTasks) {
			rtn = true;
			taskIdsToRun.add(taskId);
		}

		return rtn;
	}

	/**
	 * 
	 * @param lids
	 * @return true if this list changed as a result of the call
	 */
	public synchronized boolean addTaskIdsToRun(List lids) {
		return taskIdsToRun.addAll(lids);
	}

	public synchronized Runnable nextRun() {
		Runnable rtn = null;

		boolean hasTaskIds2Run = taskIdsToRun.size() > 0;
		if (hasTaskIds2Run || iTask <= nTasks) {
			Task task = (Task) U.newInstance(variables.tClass);
			if (task != null) {
				Integer taskId = iTask;
				String sAct = "Start ";
				if (hasTaskIds2Run) {
					taskId = taskIdsToRun.remove(0);
					sAct = "*Start ";
				} else {
					variables.next();
					iTask++;
				}
				boolean bInitialized = task.initialize(this, seed, bRenewSeed, taskId);
				bInitialized = bInitialized && variables.loadValue(task, taskId - 1);//if task.initial returns false, variables.loadValue will not be executed.				
				bInitialized = bInitialized && task.initialize();
				if (!bInitialized) {
					sAct = "!Skip ";
				}
				Date nowDate = new Date();
				DateInterval di = new DateInterval(nowDate, startDate);
				String sTaskNameAndInfo = task.getNameAndInfo();
				if (sTaskNameAndInfo.length() > 0) {
					sTaskNameAndInfo = "\t|\t" + sTaskNameAndInfo;
				}
				log(sAct + "Task: " + taskId + "/" + nTasks + " Elapsed Time: " + di.inSeconds() + " Seconds." + sTaskNameAndInfo);
				if (bInitialized) {
					if (statistics != null) {
						task.statistics = statistics.newClone();
					}
					rtn = task;
					taskIdsRunning.add(taskId);
				}
				saveCheckPoint();
			}
		}

		while (rtn == null && (taskIdsToRun.size() > 0 || iTask <= nTasks)) {
			rtn = nextRun();
		}

		return rtn;
	}

	/**
	 * This function will be called before ObjUtil.loadFromXML and
	 * .loadFromSerial
	 * 
	 * @param sLoadMethod
	 */
	public void beforeLoadObj(String sLoadMethod) {
		return;
	}

	/**
	 * This function will be called after ObjUtil.loadFromXML and
	 * .loadFromSerial
	 * 
	 * @param sLoadMethod
	 */
	public void afterLoadObj(String sLoadMethod) {
		return;
	}

	/**
	 * A wrapper for the Object.wait(long timeout). This method has to
	 * synchronized otherwise it will cause exceptions.
	 * 
	 * @param timeout
	 *            the maximum time to wait in milliseconds.
	 * @return true: wait is not interrupted; false: wait is interrupted.
	 */
	public synchronized boolean stay(long timeout) {
		boolean rtn = true;
		try {
			log("Staying for " + timeout / 1000 + " Seconds...");
			wait(timeout);
		} catch (InterruptedException e) {
			rtn = false;
			log(e);
		}
		return rtn;
	}

	/**
	 * A wrapper for the Object.wait(). This method has to synchronized
	 * otherwise it will cause exceptions.
	 * 
	 * @return true: wait is not interrupted; false: wait is interrupted.
	 */
	public synchronized boolean stay() {
		boolean rtn = true;
		try {
			log("Staying forever...");
			wait();
		} catch (InterruptedException e) {
			rtn = false;
			log(e);
		}
		return rtn;
	}

	public static void main(String[] args) {
		Tasks tasks = new Tasks();
		tasks.loadCmdArgs(args);
		tasks.start();
	}
}
