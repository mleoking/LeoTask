package org.leores.util;

public class ClassInfo {
	public Class tClass;
	public String name = "";
	public String version = "";
	public String license = "";
	public String author = "";
	public String email = "";
	public String contact = "";
	public String description = "";
	public String date = "";

	public String toShortStr() {
		return name + " " + version + " (C) " + date + author + " " + email;
	}

	public String toFullStr() {
		return tClass + " " + name + " " + version + " (C) " + date + license + author + " " + email + " " + contact + " " + description;
	}
}
