package org.leores.net;

import java.io.Serializable;

import org.leores.util.*;

public class Element extends Logger implements Cloneable, Serializable {
	private static final long serialVersionUID = -5887751112431452110L;

	protected Integer id = null;
	public Object info = null;

	protected static String sDe = " ";
	protected static String sDeEsc = "#";
	protected static String sDeRegex = "( |\t)+";

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String sInfo() {
		beforeSaveInfo();
		String rtn = null;
		if (info != null) {
			rtn = info + "";
			rtn = rtn.replace(sDe, sDeEsc);
		}
		return rtn;
	}

	public void lInfo(String sInfo) {
		info = sInfo;
		if (info != null) {
			info = (info + "").replace(sDeEsc, sDe);
		}
		afterLoadInfo();
	}

	public void beforeSaveInfo() {
		return;
	}

	public void afterLoadInfo() {
		return;
	}

}
