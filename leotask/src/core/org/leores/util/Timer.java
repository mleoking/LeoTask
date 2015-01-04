package org.leores.util;

public class Timer extends Logger {
	private long tStart;

	public void start() {
		tStart = System.currentTimeMillis();
	}

	/**
	 * 
	 * @return the duration from start to stop in milliseconds.
	 */
	public long stop() {
		try {
			return System.currentTimeMillis() - tStart;
		} catch (NullPointerException e) { // start wasn't called  
			throw new IllegalStateException("call start() first", e);
		}
	}
}
