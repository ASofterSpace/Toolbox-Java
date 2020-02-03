/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.analysis;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.SimpleFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * This controller simplifies the interaction with code parts
 */
public class CodePartCtrl {

	private String basePartName;

	private String equivToBasePartName;

	private CodePart basePart;

	private List<CodePart> parts;


	public CodePartCtrl(String basePartName, String equivToBasePartName) {

		this.basePartName = basePartName;

		this.equivToBasePartName = equivToBasePartName;

		this.parts = new ArrayList<>();
	}

	public void loadCodeDirectory(String dirname) {

		Directory srcDir = new Directory(dirname);
		List<Directory> packageDirs = srcDir.getAllDirectories(false);

		for (Directory packageDir : packageDirs) {

			String partName = packageDir.getLocalDirname();

			CodePart part = new CodePart(partName);

			part.addAllSourceCodeFiles(packageDir.getAllFiles(true));

			if (equivToBasePartName.equals(partName)) {
				part.setName(basePartName);
				part.addAllSourceCodeFiles(srcDir.getAllFiles(false));
				basePart = part;
			} else {
				part.setName(basePartName + "." + partName);
			}

			parts.add(part);
		}
	}

	public void sortParts() {

		Collections.sort(parts, new Comparator<CodePart>() {
			public int compare(CodePart a, CodePart b) {
				return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
			}
		});
	}

	public void calculateDependenciesBetweenCodeParts() {

		// figure out which parts depend on which others
		for (CodePart part : parts) {

			// every part depends on the base part (even if it does not import anything from it
			// yet, it very well might do so in the future, and we do not want the dependency
			// tree to change then)
			part.addDirectDependencyOn(basePart);

			// now for each source code file in this part...
			for (SimpleFile sourceCode : part.getSourceCodeFiles()) {
				List<String> code = sourceCode.getContents();
				// ... go over all the lines...
				for (String line : code) {
					line = line.trim();
					// ... and if it is an import from the toolbox...
					String prefix = "import " + basePartName + ".";
					if (line.startsWith(prefix) && line.endsWith(";")) {
						String importedPart = line.substring(prefix.length());
						if (importedPart.indexOf(".") >= 0) {
							importedPart = basePartName + "." +
								importedPart.substring(0, importedPart.indexOf("."));
						} else {
							importedPart = basePartName;
						}
						if (equivToBasePartName.equals(importedPart)) {
							importedPart = basePartName;
						}
						// ... then find the corresponding part containing the imported code...
						for (CodePart depPart : parts) {
							if (importedPart.equals(depPart.getName())) {
								// ... and add it as dependency to this one!
								part.addDirectDependencyOn(depPart);
							}
						}
					}
				}
			}
		}

		// now iterate a second time to get all transitive dependencies, which we want to
		// show as explicit entries as well
		for (CodePart part : parts) {
			List<CodePart> allDependencies = part.calculateAllDependenciesRecursively();
			for (CodePart dep : allDependencies) {
				part.addTransitiveDependencyOn(dep);
			}
		}
	}

	public List<CodePart> getParts() {
		return parts;
	}

}
