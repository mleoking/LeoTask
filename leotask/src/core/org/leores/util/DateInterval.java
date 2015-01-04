package org.leores.util;

import java.util.Date;

public class DateInterval {
	protected long lIntervalms;

	public DateInterval(Date d1, Date d2) {
		lIntervalms = d1.getTime() - d2.getTime();
	}

	public long inMilliseconds() {
		return lIntervalms;
	}

	public double inSeconds() {
		return (double) lIntervalms / 1000;
	}

	public double inMinutes() {
		return (double) lIntervalms / (60 * 1000);
	}

	public double inHours() {
		return (double) lIntervalms / (60 * 60 * 1000);
	}

	public double inDays() {
		return (double) lIntervalms / (24 * 60 * 60 * 1000);
	}
}
