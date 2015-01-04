package org.leores.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.leores.ecpt.TRuntimeException;
import org.leores.ecpt.UnMatchException;
import org.leores.ecpt.WrongParameterException;
import org.leores.math.rand.RandomEngine;
import org.leores.util.*;
import org.leores.util.data.Statistics;

/**
 * 
 * @author leoking
 *         This class will not be serialized by default.
 */
public class Task extends Logger implements Runnable, Serializable {
	private static final long serialVersionUID = 1759479385848062950L;
	protected static final String sStatMethodPointTask = "-";
	protected static final String sStatMethodPointTasks = "+";
	protected static String[] points = new String[] { "beforePrep", "beforeTask", "beforeRept", "beforeStep", "afterStep", "afterRept", "afterTask" };

	protected Tasks tasks;
	protected RandomEngine rand;
	protected Boolean bRenewSeed;
	public Integer iRept;
	public Integer iStep;
	public Integer id;
	//id2 is not directly used by the task system. It is designed to be used by users.
	//e.g. It could be used to achieve parallel repetition running by configure the repetitions to be 1 and set id2 to be values ranging from 1 to the number of repetitions in the XML configuration file.
	//Note: bRenewSeed should be set to be true when using id2 to do repetitions, otherwise all repeats will use a same random seed and give the same result.
	public Integer id2;
	protected Statistics statistics;
	public String sMethods;
	public String logInfo;
	protected transient Method mTask, mRept, mStep, mEnd;
	protected Boolean bmtx = false, bmrx = false, bmsx = false, bmex = false;
	public LoadHashMap<String, Object> vMap;
	public Object info;

	protected static String smLoad;

	public Object vMap(String key) {
		return vMap.get(key);
	}

	public Object v(String key) {
		return vMap(key);
	}

	public static ClassInfo getClassInfo() {
		ClassInfo rtn = new ClassInfo();

		rtn.tClass = Task.class;
		rtn.name = "The Basic Task";
		rtn.version = "0.0.0";
		rtn.license = "FreeBSD License";
		rtn.author = "Changwang Zhang";
		rtn.email = "mleoking@gmail.com";
		rtn.contact = "University College London";
		rtn.description = "The basic task class. Every task app should extend this class.";

		return rtn;
	}

	public static void beforeAll(Tasks tTasks) {
		ClassInfo ci = (ClassInfo) tTasks.invokeTaskClassStaticMethod("getClassInfo");
		U.tLog(ci.toShortStr());
		return;
	}

	/**
	 * This is where you can add extra task ids by using<br>
	 * <b>tTasks.addTaskIds2Run(Integer taskId)</b> or
	 * <b>tTasks.addTaskIds2Run(List lids)</b><br>
	 * It is called in Tasks.start()
	 * 
	 * @param tTasks
	 */
	public static void afterDoAll(Tasks tTasks) {
		return;
	}

	/**
	 * It is called in Tasks.end()
	 * 
	 * @param tTasks
	 */
	public static void afterAll(Tasks tTasks) {
		tTasks.getStatistics().statMethods(sStatMethodPointTasks, tTasks.sFPreFix, smLoad);
	}

	public Task() {
		return;
	}

	public Task(Tasks ss, Long seed, Boolean bRenewSeed, Integer iId) throws WrongParameterException {

		if (!initialize(ss, seed, bRenewSeed, iId)) {
			throw new WrongParameterException(ss, seed, bRenewSeed, iId);
		}

		return;
	}

	public synchronized static void saveObject(ObjectOutputStream oos) throws IOException {

	}

	public synchronized static void loadObject(ObjectInputStream ois) throws IOException, ClassNotFoundException, SecurityException, NoSuchFieldException {

	}

	public boolean initialize(Tasks tasks, Long seed, Boolean bRenewSeed, Integer id) {
		boolean rtn = false;

		if (tasks != null && seed != null && bRenewSeed != null && id != null) {
			this.tasks = tasks;
			this.rand = RandomEngine.makeDefault(seed);
			this.id = id;
			this.bRenewSeed = bRenewSeed;
			if (tasks.vMap != null) {
				this.vMap = (LoadHashMap<String, Object>) tasks.vMap.clone();
			}
			iRept = 0;
			iStep = 0;
			rtn = true;
		}

		return rtn;
	}

	public boolean prepMethods(String sMethods) {
		boolean rtn = true;
		if (sMethods != null) {
			String[] tokens = U.split(sMethods);
			for (int i = 0; i < tokens.length; i++) {
				Method method = getValidMethod(tokens[i]);
				if (method == null) {
					rtn = false;
					log(LOG_ERROR, "Task.prepMethods: method is not found or does not return Boolean/boolean: [" + tokens[i] + "].");
					break;
				}
				if (tokens[i].startsWith("mt")) {
					if (tokens[i].startsWith("mtx")) {
						bmtx = true;
					}
					mTask = method;
				} else if (tokens[i].startsWith("mr")) {
					if (tokens[i].startsWith("mrx")) {
						bmrx = true;
					}
					mRept = method;
				} else if (tokens[i].startsWith("ms")) {
					if (tokens[i].startsWith("msx")) {
						bmsx = true;
					}
					mStep = method;
				} else if (tokens[i].startsWith("me")) {
					if (tokens[i].startsWith("mex")) {
						bmex = true;
					}
					mEnd = method;
				} else {
					rtn = false;
					log(LOG_ERROR, "Wrong sMethods format [" + tokens[i] + "] in [" + sMethods + "]! A method should start with mt, mr, ms, or me. ");
					break;
				}
			}

		}
		return rtn;
	}

	public boolean initialize() {
		return prepMethods(sMethods);
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

	/**
	 * 
	 * @param sPar
	 * @return The value number of a parameter <b>sPar</b>, ranging from [1,n]
	 *         where n is the total count of potential values.
	 */
	public int iVal(String sPar) {
		int rtn = -1;
		if (sPar != null) {
			Object oVal = U.getFieldValue(this, sPar, U.modAll);
			Variable var = tasks.variables.findVariable(sPar);
			rtn = var.getIVal(oVal);
		}
		return rtn;
	}

	/**
	 * All points can guarantee the correctness of statistic results after
	 * reload from checkpoints. There would be no repetition introduced by
	 * reloading from a checkpoint. Because the statistic data (in
	 * Task.statistics) of each task is merged into the final statistic results
	 * (in Tasks.statistics) only when a task has ended (Task.end). The merging
	 * of statistics and deleting the task id from the running list is an atom
	 * operation in Tasks.afterTask().
	 * 
	 * @param statPoint
	 * @return
	 */

	protected void stat(String statPoint) {
		if (statistics != null) {
			statistics.stat(this, statPoint);
		}
	}

	protected void point(String sPoint) {
		stat(sPoint);
		if (Statistics.bPut(logInfo, sPoint)) {
			logPoint(sPoint);
		}
	}

	protected void logPoint(String sPoint) {
		log(id + ":" + iRept + ":" + iStep + "\t" + U.eval(logInfo, this, tasks.sPatNumOut));
	}

	protected Method getValidMethod(String sMethod) {
		Method rtn = null;
		if (sMethod.length() > 0) {
			rtn = U.getMethod(this.getClass(), sMethod);
			if (rtn != null) {
				Class cMRtn = rtn.getReturnType();
				if (!U.bAssignable(boolean.class, cMRtn) && !U.bAssignable(Boolean.class, cMRtn)) {
					rtn = null;
				}
			}
		}
		return rtn;
	}

	protected Method getMethod(String sMethod) {
		Method rtn = null;
		if (sMethod.length() > 0) {
			rtn = U.getMethod(this.getClass(), sMethod);
		}
		return rtn;
	}

	protected int statMethods() {
		String sLoad = smLoad;
		if (info != null) {
			if (sLoad == null) {
				sLoad = "";
			}
			sLoad += "info=" + info + ";";
			//sLoad += "$setInfo(1,)$;";//To remove the plot title
		}
		return statistics.statMethods(sStatMethodPointTask, tasks.sFPreFix + "I" + id + "_", sLoad);
	}

	protected boolean prepTask() {
		return true;
	}

	protected void beforeTask() {
		iRept = 0;
		point("beforeTask");
	}

	protected boolean task() {
		boolean bContinue = true;
		while (bContinue) {
			bContinue = doRept();
		}
		return true;
	}

	protected void afterTask() {
		if (mEnd != null) {
			U.invokeMethod(this, mEnd);
		} else {
			statMethods();
		}
		point("afterTask");
		tasks.afterTask(this);
	}

	protected boolean doTask() {
		boolean rtn = false;
		if (bmtx) {
			rtn = (Boolean) U.invokeMethod(this, mTask);
		} else {
			if (prepTask()) {
				beforeTask();
				if (mTask == null) {
					rtn = task();
				} else {
					rtn = (Boolean) U.invokeMethod(this, mTask);
				}
				afterTask();
			}
		}
		return rtn;
	}

	/**
	 * iRept range [1,nRepeats].
	 * 
	 * @return
	 */
	protected boolean prepRept() {
		return (tasks.nRepeats < 0 || iRept < tasks.nRepeats);
	}

	protected void beforeRept() {
		iRept++;
		iStep = 0;
		if (bRenewSeed) {
			long seed = System.currentTimeMillis();
			rand.setSeed(seed);
		}
		point("beforeRept");
	}

	protected boolean rept() {
		boolean bContinue = true;
		while (bContinue) {
			bContinue = doStep();
		}
		return true;
	}

	protected void afterRept() {
		point("afterRept");
		tasks.afterRept(this);
		return;
	}

	protected boolean doRept() {
		boolean rtn = false;
		if (bmrx) {
			rtn = (Boolean) U.invokeMethod(this, mRept);
		} else {
			if (prepRept()) {
				beforeRept();
				if (mRept == null) {
					rtn = rept();
				} else {
					rtn = (Boolean) U.invokeMethod(this, mRept);
				}
				afterRept();
			}
		}
		return rtn;
	}

	/**
	 * iStep ranges: [1,nSteps]
	 * 
	 * @return
	 */
	protected boolean prepStep() {
		return (tasks.nSteps < 0 || iStep < tasks.nSteps);
	}

	protected void beforeStep() {
		iStep++;
		point("beforeStep");
	}

	protected boolean step() {
		log("Running default empty task step!");
		return false;
	}

	protected void afterStep() {
		point("afterStep");
		return;
	}

	protected boolean doStep() {
		boolean rtn = false;
		if (bmsx) {
			rtn = (Boolean) U.invokeMethod(this, mStep);
		} else {
			if (prepStep()) {
				beforeStep();
				if (mStep == null) {
					rtn = step();
				} else {
					rtn = (Boolean) U.invokeMethod(this, mStep);
				}
				afterStep();
			}
		}
		return rtn;
	}

	public boolean start() {
		boolean rtn = doTask();
		end();
		return rtn;
	}

	protected void end() {
		tasks.end(this);//this has to be called every time start is called to ensure the Tasks know the current task is finished.		
	}

	public void run() {
		start();
	}

	/**
	 * Can be used as mMethod. For example use mFalse as mStep to avoid doing
	 * any steps.
	 * 
	 * @return false
	 */
	public boolean mFalse() {
		return false;
	}

	/**
	 * Can be used as mMethod. For example use mTrue as mStep to do all steps.
	 * 
	 * @return false
	 */
	public boolean mTrue() {
		return true;
	}

	public void mVoid() {
		return;
	}

	public static void mVoid(Tasks tTasks) {
		return;
	}

	/**
	 * This function will be called before Variables.loadValue
	 * 
	 * @param sLoadMethod
	 */
	public void beforeLoadValue(String sLoadMethod) {
		return;
	}

	/**
	 * This function will be called after Variables.loadValue
	 * 
	 * @param sLoadMethod
	 */
	public void afterLoadValue(String sLoadMethod) {
		return;
	}
}
