package org.leores.demo;

import org.leores.util.FileUtil;

public class FileUtilDemo extends Demo {

	public void insert() {
		String sFTest = "testfile.fud";
		FileUtil.createIfNotExist(sFTest);
		FileUtil.appendFile(sFTest, "Head\nThis is a test file\nTail\n");
		FileUtil.insertFile(sFTest, "Head\n".length(), "insertFile:insert in the middle of a file!\n".getBytes());		
		FileUtil.insertFileHead(sFTest, "insertHead: Text inserted before the file head\n");
		FileUtil.insertFileTail(sFTest, "insertTail: Text inserted after the file tail\n");
		FileUtil.appendFile(sFTest, "appendFile: another way to add content to the file tail\n");
	}

	public void delete() {
		//delete files if their names match a regular expression. Here delete all files with the extension of del.
		FileUtil.createIfNotExist("file1.del");
		FileUtil.createIfNotExist("file2.del");
		FileUtil.createIfNotExist("file3.del");
		FileUtil.createIfNotExist("file4.del");
		FileUtil.deleleFiles(".*\\.del$");
	}

	public static void demo() {
		FileUtilDemo demo = new FileUtilDemo();
		demo.insert();
		demo.delete();
	}
}
