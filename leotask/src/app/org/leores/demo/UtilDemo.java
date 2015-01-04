package org.leores.demo;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.leores.util.Logger;
import org.leores.util.ObjArray;
import org.leores.util.SysUtil;
import org.leores.util.SysUtil.Command;
import org.leores.util.U;
import org.leores.util.able.Processable2;
import org.leores.util.data.Statistics;

public class UtilDemo extends Demo {

	public void parseList() {
		String str = "10@3;6;0;1:3:10}0.1:0.2:1;0.5@4;0.99;0.88;0.77";
		List ls = U.parseList(String.class, str); // "}" block the string into two strings.
		List l1 = U.parseList(Integer.class, (String) ls.get(0));
		List l2 = U.parseList(Double.class, (String) ls.get(1));
		log(str);
		log("l1=" + l1);
		log("l2=" + l2);
	}

	public void execute() {
		Command cmd1 = SysUtil.execCmd("gnuplot gnuplot1.plt -e \"pause mouse key\"", true, true);
		//log(cmd1.waitFor());
		Command cmd2 = SysUtil.execCmd("gnuplot gnuplot2.plt -e \"pause mouse key\"", true, true);
		//log(SysUtil.execute("wgnuplot gnuplot2.plt -persist", true));
		//log(SysUtil.execute("wgnuplot gnuplot2.plt -e \"pause mouse\"", true));
		SysUtil.execCmd("cmd /c dir", true);
		SysUtil.execCmd("java", true);
		SysUtil.execCmd("calc", true);
		SysUtil.execCmd("notepad", true);
	}

	public void logProcessor() {
		Processable2 logProcessorObj1 = new Processable2<String, Integer, String>() {
			public String process(Integer a, String b) {
				U._tLog(U.LOG_ERROR, "logProcessorObj1:" + U.toStr(a, b) + "\n");
				return "processed1:" + b;
			}
		};
		Processable2 logProcessorTLogger = new Processable2<String, Integer, String>() {
			public String process(Integer a, String b) {
				U._tLog(U.LOG_INFO, "logProcessorTLogger:" + U.toStr(a, b) + "\n");
				return null;//return null will stop following processing and outputting of b.
			}
		};
		class Class1 extends Logger {
			public String log(String str) {
				return super.log(str);
			}
		}
		Class1 obj1 = new Class1(), obj2 = new Class1();
		obj1.setLogProcessor(logProcessorObj1);
		Logger.getTLogger().setLogProcessor(logProcessorTLogger);
		obj1.log("Log from obj1");
		log("-------------------------------");
		obj2.log("Log from obj2");
		obj1.setLogProcessor(null);
		Logger.getTLogger().setLogProcessor(null);
	}

	public void regexEscape() {
		String str = "abc.*d+.??()";
		log(str + " matches " + str + " = " + str.matches(str));
		String str2 = U.regexQuote(str);
		log(str + " matches " + str2 + " = " + str.matches(str2));
		String str3 = Pattern.quote(str);
		log(str + " matches " + str3 + " = " + str.matches(str3));
	}

	public void objArray() {
		//		Integer[] a1 = { 2, 1 };
		//		Integer[] a2 = { 1, 32 };
		//		Integer[] a3 = { 1, 32 };
		//				Double[] a1 = { 0.15, 0.15 };
		//				Double[] a2 = { 0.95, 1.0 };
		//				Double[] a3 = { 0.95, 1.0 };
		//				Double[] a1 = { 0.0 };
		//				Double[] a2 = { 2.0 };
		//				Double[] a3 = { 2.0 };
		Double[] a1 = { 363.2375 };
		Double[] a2 = { 427.39750000000004 };
		Double[] a3 = { 371.1475 };
		ObjArray oa21 = new ObjArray(a1, 2);//minLength==0: no adjustment when the length of array is small 
		ObjArray oa22 = new ObjArray(a2, 2);
		ObjArray oa23 = new ObjArray(a3, 2);
		ObjArray oa31 = new ObjArray(a1, 3);
		ObjArray oa32 = new ObjArray(a2, 3);
		ObjArray oa33 = new ObjArray(a3, 3);
		ObjArray oa101 = new ObjArray(a1, 10);
		ObjArray oa102 = new ObjArray(a2, 10);
		ObjArray oa103 = new ObjArray(a3, 10);
		log("deepHash a1:" + Arrays.deepHashCode(a1) + " a2:" + Arrays.deepHashCode(a2) + " a3:" + Arrays.deepHashCode(a3));
		log("deepHash2[oa21,oa22,oa23]:", oa21.hashCode(), oa22.hashCode(), oa23.hashCode());//hash conflict
		log("deepHash3[oa31,oa32,oa33]:", oa31.hashCode(), oa32.hashCode(), oa33.hashCode());//some hash conflict
		log("deepHash10[oa101,oa102,oa103]:", oa101.hashCode(), oa102.hashCode(), oa103.hashCode());//no hash conflict
	}
	
	public void parseStrArray() {
		String str = "%plot-@ xxx%print+@yyyy%plt-@zzzzz";
		String[] sa1 = U.parseStrArray(str, "%([^\\s%]+?)\\-@");
		String[] sa2 = U.parseStrArray(str, "%([^\\s%]+?)\\+@");
		log(sa1);
		log(sa2);
		String sMethodPattern1 = Statistics.sMethodPattern("-");
		String sMethodPattern2 = Statistics.sMethodPattern("+");
		String[] sa3 = U.parseStrArray(str, sMethodPattern1);
		String[] sa4 = U.parseStrArray(str, sMethodPattern2);
		log(sMethodPattern1 + " " + sMethodPattern2);
		log(sa3);
		log(sa4);
	}

	public void regexMatch() {
		String str = "nets-(90000,5,4).dat";
		String regex = "(\\d+),(\\d+),(\\d+)";
		log(U.regexMatch(str, regex));
	}

	public void toValidFileName() {
		String sFile = "a/b*c:d<,>,|,\\,/,?,";
		log(sFile);
		log(U.toValidFileName(sFile));
	}

	public static void demo() {
		UtilDemo demo = new UtilDemo();		
		demo.parseList();
		demo.execute();
		demo.logProcessor();
		demo.regexEscape();
		demo.objArray();
		demo.parseStrArray();
		demo.regexMatch();
		demo.toValidFileName();
	}
}
