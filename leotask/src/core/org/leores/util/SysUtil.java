package org.leores.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;

import org.leores.ecpt.TRuntimeException;

public class SysUtil extends LogUtil {

	public static String getCmdPathFileStr(String command) {
		String rtn = null;
		if (command != null) {
			String[] sCmdExts = { "", ".com", ".exe", ".bat" };
			StringTokenizer st = new StringTokenizer(System.getenv("PATH"), File.pathSeparator);
			String path, sFile;
			while (st.hasMoreTokens()) {
				path = st.nextToken();
				for (String sExt : sCmdExts) {
					sFile = path + File.separator + command + sExt;
					if (new File(sFile).isFile()) {
						rtn = sFile;
						break;
					}
				}
			}
		}
		return rtn;
	}

	public static boolean bCmdExist(String command) {
		return getCmdPathFileStr(command) != null;
	}

	public static class Command implements Runnable {
		protected String[] rtn;
		public String command;
		public Boolean bLog;
		public final Object tLock = new Object();

		public Command(String command, boolean bLog) {
			rtn = null;
			this.command = command;
			this.bLog = bLog;
		}

		/**
		 * Causes the current thread to wait, if necessary, until the command
		 * has terminated.
		 * 
		 * @return [0]: the exit value of the command process (a number); [1]:
		 *         the err stream; [2]: the normal out stream.
		 */
		public String[] waitForRtn() {
			synchronized (tLock) {
				return rtn;
			}
		}

		public void run() {
			try {
				synchronized (tLock) {
					final Process proc = Runtime.getRuntime().exec(command);
					BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					String line = "";
					StringBuffer errBuff = new StringBuffer();
					while ((line = reader.readLine()) != null) {
						errBuff.append(line + "\n");
					}
					reader.close();
					reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					line = "";
					StringBuffer outBuff = new StringBuffer();
					while ((line = reader.readLine()) != null) {
						outBuff.append(line + "\n");
					}
					reader.close();
					rtn = new String[3];
					int procRtn = proc.waitFor();
					rtn[0] = procRtn + "";
					rtn[1] = errBuff.toString();
					rtn[2] = outBuff.toString();
					if (bLog) {
						if (rtn[1] != null && rtn[1].length() > 0) {
							tLog(LOG_ERROR, rtn[1]);
						}
						if (rtn[2] != null && rtn[2].length() > 0) {
							tLog(rtn[2]);
						}
					}
				}
			} catch (Exception e) {
				tLog(e);
			}
		}
	}

	public static Command execCmd(String command, boolean bLog, boolean bInNewThread) {
		Command rtn = null;
		if (command != null) {
			rtn = new Command(command, bLog);
			if (bInNewThread) {
				Thread tCmd = new Thread(rtn, "Running Command: " + command);
				tCmd.start();
				try {
					Thread.sleep(200); // wait for 200ms to let the tCmd thread to call Commmand.run() and lock the tLock first
				} catch (InterruptedException e) {
					tLog(e);
				}
			} else {
				rtn.run();
			}
		}
		return rtn;
	}

	public static Command execCmd(String command, boolean bLog) {
		return execCmd(command, bLog, false);
	}

	public static boolean setCurrentDirectory(String name) {
		boolean rtn = false; // Boolean indicating whether directory was set
		File directory; // Desired current working directory

		directory = new File(name).getAbsoluteFile();
		if (directory.exists() || directory.mkdirs()) {
			rtn = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
		}

		return rtn;
	}
}
