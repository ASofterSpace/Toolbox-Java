/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.analysis;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.SimpleFile;

import java.util.ArrayList;
import java.util.List;


/**
 * This represents a single part of source code files, typically similar to a package
 */
public class CodePart {

	private String name;

	private List<SimpleFile> sourceCodeFiles;

	private List<CodePart> dependingOn;


	public CodePart(String name) {

		this.name = name;

		this.sourceCodeFiles = new ArrayList<>();

		this.dependingOn = new ArrayList<>();
	}

	public void addAllSourceCodeFiles(List<File> codeFiles) {

		for (File codeFile : codeFiles) {
			sourceCodeFiles.add(new SimpleFile(codeFile));
		}
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getName() {
		return name;
	}

	public int getClassAmount() {
		return sourceCodeFiles.size();
	}

	public List<SimpleFile> getSourceCodeFiles() {
		return sourceCodeFiles;
	}

	public void addDependencyOn(CodePart otherPart) {

		if (otherPart == null) {
			return;
		}

		// we don't depend on ourselves, duh!
		if (this.equals(otherPart)) {
			return;
		}

		if (!dependingOn.contains(otherPart)) {
			dependingOn.add(otherPart);
		}
	}

	public List<CodePart> getDependencies() {
		return dependingOn;
	}

	@Override
	public boolean equals(Object other) {

		if (other == null) {
			return false;
		}

		if (other instanceof CodePart) {

			CodePart otherPart = (CodePart) other;

			if (name == null) {
				return otherPart.name == null;
			}

			return name.equals(otherPart.name);
		}

		return false;
	}

	@Override
	public int hashCode() {

		if (name == null) {
			return 0;
		}

		return name.hashCode();
	}

}
