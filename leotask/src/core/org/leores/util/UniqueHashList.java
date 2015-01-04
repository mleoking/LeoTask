package org.leores.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The Class E here should implement both hashCode() and equals(Object o)
 * function to guarantee the HashMap works fine.
 * 
 * @param <E>
 */
public class UniqueHashList<E> extends Logger implements Serializable {
	private static final long serialVersionUID = -1125074227269678218L;

	public List<E> elementList;
	protected Map<E, E> elementMap;

	public UniqueHashList() {
		elementList = new ArrayList<E>();
		elementMap = new HashMap<E, E>();
	}

	public void clear() {
		elementList.clear();
		elementMap.clear();
	}

	public E find(E e) {
		E rtn = null;
		boolean bHashCollision = false;

		rtn = elementMap.get(e);
		if (rtn != null && !rtn.equals(e)) {
			bHashCollision = true;
			rtn = null;
		}

		if (bHashCollision) {
			for (int i = 0, size = elementList.size(); i < size; i++) {
				E ei = elementList.get(i);
				if (ei.equals(e)) {
					rtn = ei;
					break;
				}
			}
		}

		return rtn;
	}

	public boolean add(E e) {
		boolean rtn = false;

		E eFound = find(e);
		if (eFound == null) {
			rtn = true;
			elementList.add(e);
			elementMap.put(e, e);
		}

		return rtn;
	}

	public int addAll(Collection<? extends E> c) {
		int oldsize = elementList.size();

		Object[] ca = c.toArray();
		for (int i = 0; i < ca.length; i++) {
			add((E) ca[i]);
		}

		int rtn = elementList.size() - oldsize;

		return rtn;
	}

	public E remove(E e) {
		E rtn = null;

		E eFound = find(e);
		if (eFound != null && elementList.remove(eFound)) {
			if (elementMap.remove(eFound) != null) {
				rtn = eFound;
			}
		}

		return rtn;
	}

	public int remove(List<E> le) {
		int rtn = -1;

		if (le != null) {
			rtn = 0;
			for (int i = 0, size = le.size(); i < size; i++) {
				E e = le.get(i);
				if (remove(e) != null) {
					rtn++;
				}
			}
		}

		return rtn;
	}

	public List<E> asList() {
		return elementList;
	}

	public E get(int i) {
		return elementList.get(i);
	}

	public int size() {
		return elementList.size();
	}

	public String toString() {
		return "" + elementList;
	}
}
