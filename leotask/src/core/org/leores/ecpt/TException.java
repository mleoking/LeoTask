package org.leores.ecpt;

import org.leores.util.U;

public class TException extends Exception {
	public TException() {
		super();
		return;
	}

	public TException(Object obj) {
		super(U.toStr(obj));
		return;
	}

	public TException(Object... objs) {
		this("", objs);
		return;
	}

	public TException(String str, Object... objs) {
		super(str + U.toStr(objs));
		return;
	}
}
