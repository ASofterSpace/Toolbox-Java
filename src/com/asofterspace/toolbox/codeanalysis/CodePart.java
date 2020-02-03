/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeanalysis;

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

	private List<CodePart> transitivelyDependingOn;


	public CodePart(String name) {

		this.name = name;

		this.sourceCodeFiles = new ArrayList<>();

		this.dependingOn = new ArrayList<>();

		this.transitivelyDependingOn = new ArrayList<>();
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

	public void addDirectDependencyOn(CodePart otherPart) {

		if (otherPart == null) {
			return;
		}

		// we don't depend on ourselves, duh!
		if (this.equals(otherPart)) {
			return;
		}

		// if we add this as direct dependency, we no longer need it as transitive one
		if (transitivelyDependingOn.contains(otherPart)) {
			transitivelyDependingOn.remove(otherPart);
		}

		// add as direct dependency
		if (!dependingOn.contains(otherPart)) {
			dependingOn.add(otherPart);
		}
	}

	public void addTransitiveDependencyOn(CodePart otherPart) {

		if (otherPart == null) {
			return;
		}

		// we don't depend on ourselves, duh!
		if (this.equals(otherPart)) {
			return;
		}

		// add as transitive dependency
		if (!transitivelyDependingOn.contains(otherPart)) {
			if (!dependingOn.contains(otherPart)) {
				transitivelyDependingOn.add(otherPart);
			}
		}
	}

	public List<CodePart> getDirectDependencies() {
		return dependingOn;
	}

	public List<CodePart> getTransitiveDependencies() {
		return transitivelyDependingOn;
	}

	public List<CodePart> getAllDependencies() {
		List<CodePart> result = new ArrayList<>();
		result.addAll(dependingOn);
		result.addAll(transitivelyDependingOn);
		return result;
	}

	/**
	 * This actually performs a recursive lookup whereas the other methods for
	 * getting dependencies are really just getters for predefined fields
	 */
	public List<CodePart> calculateAllDependenciesRecursively() {

		List<CodePart> result = getAllDependencies();
		for (CodePart part : dependingOn) {
			result.addAll(part.getAllDependencies());
		}
		for (CodePart part : transitivelyDependingOn) {
			result.addAll(part.getAllDependencies());
		}
		return result;
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
