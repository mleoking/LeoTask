package org.leores.util;

import org.leores.util.data.DataTable;

public class DelimitedReaderUtil extends Logger {
	public String delimiter;
	public String before;
	public String after;
	public String sFile;
	public String[] rowToStart;
	public String[] columnsToRead;
	public String[] rowToEnd;
	public String[] validRowPattern;
	public int[] iRtnColumns;

	public DelimitedReaderUtil() {
	}

	public DelimitedReaderUtil(String sFile, String[] rowToStart, String[] columnsToRead, String[] rowToEnd, String[] validRowPattern, int[] iRtnColumns) {
		this.sFile = sFile;
		this.rowToStart = rowToStart;
		this.columnsToRead = columnsToRead;
		this.rowToEnd = rowToEnd;
		this.validRowPattern = validRowPattern;
		this.iRtnColumns = iRtnColumns;
	}

	public DelimitedReaderUtil(String sFile, String[] rowToEnd) {
		this(sFile, null, null, rowToEnd, null, null);
	}

	public DelimitedReaderUtil(String sFile) {
		this(sFile, null, null, null, null, null);
	}

	public static DataTable read(String info, String sFile, String[] rowToStart, String[] columnsToRead, String[] rowToEnd, String[] validRowPattern, int... iRtnColumns) {
		return DelimitedReader.readValidDataTable(info, sFile, rowToStart, columnsToRead, rowToEnd, validRowPattern, iRtnColumns);
	}

	public DataTable read(String info, String[] rowToStart, String[] columnsToRead, String[] rowToEnd, String[] validRowPattern, int... iRtnColumns) {
		return read(info, sFile, rowToStart, columnsToRead, rowToEnd, validRowPattern, iRtnColumns);
	}

	public DataTable read(String info, String[] rowToStart, String[] columnsToRead, String[] validRowPattern, int... iRtnColumns) {
		return read(info, sFile, rowToStart, columnsToRead, rowToEnd, validRowPattern, iRtnColumns);
	}
	
	public DataTable read(String info, String[] rowToStart, String[] columnsToRead, String[] validRowPattern) {
		return read(info, sFile, rowToStart, columnsToRead, rowToEnd, validRowPattern, iRtnColumns);
	}

	public DataTable read(String info, String[] rowToStart, String[] columnsToRead) {
		return read(info, sFile, rowToStart, columnsToRead, rowToEnd, validRowPattern, iRtnColumns);
	}

	public DataTable read(String info, String[] columnsToRead) {
		return read(info, sFile, rowToStart, columnsToRead, rowToEnd, validRowPattern, iRtnColumns);
	}
	
	public DataTable read(String info, String[] columnsToRead, String[] validRowPattern, int... iRtnColumns) {
		return read(info, sFile, rowToStart, columnsToRead, rowToEnd, validRowPattern, iRtnColumns);
	}
}
