package org.leores.util;

import java.util.HashMap;

import org.leores.ecpt.TRuntimeException;

public class LoadHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 2637966156374453176L;

	public LoadHashMap(String loadStr) {
		super();
		if (!U.loadFromString(this, loadStr)) {
			throw new TRuntimeException("LoadHashMap: Failed to load [" + loadStr + "]");
		}
	}

	public LoadHashMap() {
		super();
	}
}
