/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;


public class CanonicalJavaLikeImport {

	private String importValue = "";

	private boolean isStatic = false;


	public CanonicalJavaLikeImport(String val, String importKeyword, String staticKeyword) {

		val = val.toLowerCase();
		val = val.trim();

		if (val.startsWith(importKeyword)) {
			val = val.substring(importKeyword.length());
			val = val.trim();
		}

		if (val.startsWith(staticKeyword)) {
			val = val.substring(staticKeyword.length());
			val = val.trim();
			setStatic();
		}

		setImport(val);
	}


	public void setImport(String newValue) {
		this.importValue = newValue;
	}

	public String getImport() {
		return importValue;
	}

	public void setStatic() {
		this.isStatic = true;
	}

	public boolean isStatic() {
		return isStatic;
	}
}
