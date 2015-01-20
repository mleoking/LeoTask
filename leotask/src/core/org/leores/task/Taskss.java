package org.leores.task;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.leores.util.ClassInfo;
import org.leores.util.DateInterval;
import org.leores.util.FileUtil;
import org.leores.util.LoadHashMap;
import org.leores.util.Logger;
import org.leores.util.SerialUtil;
import org.leores.util.U;
import org.leores.util.able.Processable1;

public class Taskss extends Logger implements Serializable {
	private static final long serialVersionUID = 4852566341714978930L;
	public String name;
	//These are default values for Tasks.java parameters
	protected Double usage = 0.9;
	protected Integer nRepeats = 1;
	protected Integer nSteps = -1;
	protected Long seed = System.currentTimeMillis();
	protected Boolean bRenewSeed = false;
	public Integer nTaskss;
	protected Integer checkInterval = 100;
	protected Integer nCheckPoints = 5;
	public Date startDate;
	public Long duration;
	protected List<String> dataLevels = U.asList(new String[] { "S", "SA" });
	protected String sPatNumOut;
	public Integer nFinished;

	public String sFLoad = "tasks.xml";//By default the Taskss will run Tasks instead. Will run Taskss only if there is "taskss" in sFLoad
	public String sFCheckPoint = "check.ckp";
	public String sFData = "data.csv";
	public String sFPreFix;

	public LoadHashMap<String, Object> vMap;
	public Object info = "";

	protected String sTaskMethodStart;
	protected String sTaskMethodEnd;
	protected Boolean bSaveData = true;
	protected Boolean bPrintData = false;

	protected Boolean bAggregateFile = true;
	protected String sPatNameToRun;
	protected String sNameToRun;

	protected List<Tasks> members;

	public boolean loadCmdArgs(String[] args) {
		boolean rtn = false;

		String sFLoadArg = U.getCmdArg("-load=", args);
		String sFCheckPointArg = U.getCmdArg("-checkpoint=", args);

		if (sFLoadArg != null) {
			sFLoad = sFLoadArg;
			rtn = true;
		} else {
			String sFLoadTaskss = "tasks#.xml";//Defalut Taskss load file
			if (FileUtil.bExistFile(sFLoadTaskss)) {
				sFLoad = sFLoadTaskss;
			}
		}

		if (sFCheckPointArg != null) {
			sFCheckPoint = sFCheckPointArg;
			rtn = true;
		}

		return rtn;
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

	public Object vMap(String key) {
		return vMap.get(key);
	}

	public Object v(String key) {
		return vMap(key);
	}

	/**
	 * load the default Tasks parameter values for those parameters whose values
	 * are null.
	 * 
	 * @param tasks
	 * @return number of parameters with default values loaded.
	 */
	public int loadValues(Tasks tasks) {
		int rtn = 0;

		//name,usage,nRepeats,nSteps,seed,bRenewSeed,checkInterval,nCheckPoints,dataLevels,sPatNumOut
		if (tasks != null) {
			if (tasks.name == null) {
				tasks.name = name;
				rtn++;
			}
			if (tasks.usage == null) {
				tasks.usage = usage;
				rtn++;
			}
			if (tasks.nRepeats == null) {
				tasks.nRepeats = nRepeats;
				rtn++;
			}
			if (tasks.nSteps == null) {
				tasks.nSteps = nSteps;
				rtn++;
			}
			if (tasks.seed == null) {
				tasks.seed = seed;
				rtn++;
			}
			if (tasks.bRenewSeed == null) {
				tasks.bRenewSeed = bRenewSeed;
				rtn++;
			}
			if (tasks.checkInterval == null) {
				tasks.checkInterval = checkInterval;
				rtn++;
			}
			if (tasks.nCheckPoints == null) {
				tasks.nCheckPoints = nCheckPoints;
				rtn++;
			}
			if (tasks.dataLevels == null) {
				tasks.dataLevels = dataLevels;
				rtn++;
			}
			if (tasks.sPatNumOut == null) {
				tasks.sPatNumOut = sPatNumOut;
				rtn++;
			}

			if (tasks.sFLoad == null) {
				tasks.sFLoad = sFLoad;
				rtn++;
			}
			if (tasks.sFCheckPoint == null) {
				tasks.sFCheckPoint = sFCheckPoint;
				rtn++;
			}
			if (tasks.sFData == null) {
				tasks.sFData = sFData;
				rtn++;
			}
			if (tasks.vMap == null) {
				tasks.vMap = vMap;
				rtn++;
			}

			if (tasks.sTaskMethodStart == null) {
				tasks.sTaskMethodStart = sTaskMethodStart;
				rtn++;
			}
			if (tasks.sTaskMethodEnd == null) {
				tasks.sTaskMethodEnd = sTaskMethodEnd;
				rtn++;
			}

			if (tasks.bSaveData == null) {
				tasks.bSaveData = bSaveData;
				rtn++;
			}
			if (tasks.bPrintData == null) {
				tasks.bPrintData = bPrintData;
				rtn++;
			}

		}

		return rtn;
	}

	public String getLoadStr() {
		String rtn;
		String sStartDate = U.format(startDate, "MM/dd@HH:mm");
		rtn = "LeoTask starts in batch mode.\n";
		rtn += "name=" + name + " nTaskss=" + nTaskss + " startDate=" + sStartDate + " info=" + info;
		return rtn;
	}

	protected boolean prep() {
		boolean rtn = false;
		nFinished = 0;
		nTaskss = 0;
		if (sFLoad.endsWith("#.xml")) {
			rtn = true;
			startDate = new Date();
			rtn = rtn && U.loadFromXML(this, sFLoad);
			if (sFPreFix == null) {
				sFPreFix = name + "_" + U.format(startDate, "MMdd@HHmm") + "_";
			}
			if (rtn && members != null && members.size() > 0) {
				nTaskss = members.size();
				for (int i = 0; i < nTaskss; i++) {
					Tasks tasks = members.get(i);
					loadValues(tasks);
					tasks.sFLoad = null;//set it to null to prevent tasks from loading config again.
					if (bAggregateFile) {
						tasks.sFPreFix = sFPreFix;
					}
				}
			} else {
				rtn = false;
			}
			sFData = sFPreFix + sFData;//set SFData here to avoid the sFPreFix to be included in the sFData used in loadValues(tasks);
			String sLoad = getLoadStr();
			log(sLoad);
		} else {
			Tasks tasks = new Tasks(false);
			loadValues(tasks);
			tasks.start();
		}
		return rtn;
	}

	public int start() {
		if (prep()) {
			for (int i = 0, mi = members.size(); i < mi; i++) {
				Tasks tasks = members.get(i);
				boolean bToRun = tasks != null;
				if (bToRun && sPatNameToRun != null && !tasks.name.matches(sPatNameToRun)) {
					bToRun = false;
				}
				if (bToRun && sNameToRun != null && !sNameToRun.contains(tasks.name)) {
					bToRun = false;

				}
				if (bToRun) {
					int iTasks = i + 1;
					log("--- " + iTasks + "/" + nTaskss + " " + tasks.name + " ---");
					if (tasks.start() > 0) {
						nFinished++;
					}
				}
			}
			end();
		}
		return nFinished;
	}

	public String getDataFileName(String sLevel) {
		int iDot = sFData.lastIndexOf('.');
		String rtn = U.insert(sFData, iDot, "@" + sLevel);
		return rtn;
	}

	public boolean saveData() {
		boolean rtn = false;

		if (bSaveData && dataLevels != null && dataLevels.size() > 0) {
			rtn = true;
			String sOutString = SerialUtil.toOutStringCSV(this, this.sPatNumOut, true, null);
			FileUtil.insertFileHead(sFData, sOutString);
		}

		return rtn;
	}

	public void end() {
		Date nowDate = new Date();
		DateInterval di = new DateInterval(nowDate, startDate);
		duration = di.inMilliseconds();
		saveData();
		log("------");
		log("Finished " + nFinished + "/" + nTaskss + " Sets of Tasks in " + di.inMinutes() + " Minutes.");
	}

	public static void main(String[] args) {
		Taskss taskss = new Taskss();
		taskss.loadCmdArgs(args);
		taskss.start();
		System.exit(0);
	}
}
