/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;

import com.asofterspace.toolbox.codeeditor.base.Code;


public class CodeField {

	private String name;

	private String nameUpcase;

	private String type;


	public CodeField(String name, String type) {
		this.name = name;
		this.nameUpcase = name.substring(0, 1).toUpperCase() + name.substring(1);
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getNameUpcase() {
		return nameUpcase;
	}

	public String getType() {
		return type;
	}
}
