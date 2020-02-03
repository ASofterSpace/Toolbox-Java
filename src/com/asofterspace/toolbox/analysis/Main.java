/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.analysis;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Main {

	// this is the base part, all others are children of it
	private final static String BASE_PART = "com.asofterspace.toolbox";

	// this child is actually included in the base part
	private final static String EQUIV_TO_BASE_PART = "utils";


	public static void main(String[] args) {

		// load all the source code of the Toolbox, organized in parts
		Directory srcDir = new Directory("src/com/asofterspace/toolbox");
		List<Directory> packageDirs = srcDir.getAllDirectories(false);
		List<ToolboxPart> parts = new ArrayList<>();
		ToolboxPart basePart = null;

		for (Directory packageDir : packageDirs) {

			String partName = packageDir.getLocalDirname();

			ToolboxPart part = new ToolboxPart(partName);

			part.addAllSourceCodeFiles(packageDir.getAllFiles(true));

			if (EQUIV_TO_BASE_PART.equals(partName)) {
				part.setName(BASE_PART);
				part.addAllSourceCodeFiles(srcDir.getAllFiles(false));
				basePart = part;
			} else {
				part.setName(BASE_PART + "." + partName);
			}

			parts.add(part);
		}

		Collections.sort(parts, new Comparator<ToolboxPart>() {
			public int compare(ToolboxPart a, ToolboxPart b) {
				return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
			}
		});

		// output what we have found so far
		System.out.println("We loaded the following parts:");
		System.out.println("");

		int classAmount = 0;
		for (ToolboxPart part : parts) {
			int curClassAmount = part.getClassAmount();
			System.out.println(part.getName() + " containing " + curClassAmount + " classes");
			classAmount += curClassAmount;
		}

		System.out.println("");
		System.out.println("All parts in total contain " + classAmount + " classes.");

		// figure out which parts depend on which others
		for (ToolboxPart part : parts) {

			// every part depends on the base part (even if it does not import anything from it
			// yet, it very well might do so in the future, and we do not want the dependency
			// tree to change then)
			part.addDependencyOn(basePart);

			// now for each source code file in this part...
			for (SimpleFile sourceCode : part.getSourceCodeFiles()) {
				List<String> code = sourceCode.getContents();
				// ... go over all the lines...
				for (String line : code) {
					line = line.trim();
					// ... and if it is an import from the toolbox...
					String prefix = "import " + BASE_PART + ".";
					if (line.startsWith(prefix) && line.endsWith(";")) {
						String importedPart = line.substring(prefix.length());
						if (importedPart.indexOf(".") >= 0) {
							importedPart = BASE_PART + "." +
								importedPart.substring(0, importedPart.indexOf("."));
						} else {
							importedPart = BASE_PART;
						}
						if (EQUIV_TO_BASE_PART.equals(importedPart)) {
							importedPart = BASE_PART;
						}
						// ... then find the corresponding part containing the imported code...
						for (ToolboxPart depPart : parts) {
							if (importedPart.equals(depPart.getName())) {
								// ... and add it as dependency to this one!
								part.addDependencyOn(depPart);
							}
						}
					}
				}
			}
		}

		// output the information in a nice output file
		JsonFile outputFile = new JsonFile("analysis.json");
		Record output = Record.emptyObject();
		Record outParts = Record.emptyArray();
		output.set("parts", outParts);
		for (ToolboxPart part : parts) {
			Record outPart = Record.emptyObject();
			outParts.append(outPart);
			outPart.set("name", part.getName());
			Record outDependsOn = Record.emptyArray();
			outPart.set("dependsOn", outDependsOn);
			for (ToolboxPart dep : part.getDependencies()) {
				outDependsOn.append(dep.getName());
			}
		}
		outputFile.setAllContents(output);
		outputFile.save();

		System.out.println("");
		System.out.println("The results have been saved to:");
		System.out.println(outputFile.getCanonicalFilename());

		System.out.println("");
		System.out.println("Analysis done, have a nice day :)");
	}
}
