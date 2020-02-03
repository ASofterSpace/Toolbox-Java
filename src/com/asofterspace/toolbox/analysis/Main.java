/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.analysis;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.utils.Record;


public class Main {

	// this is the base part, all others are children of it
	private final static String BASE_PART = "com.asofterspace.toolbox";

	// this child is actually included in the base part
	private final static String EQUIV_TO_BASE_PART = "utils";


	public static void main(String[] args) {

		CodePartCtrl codePartCtrl = new CodePartCtrl(BASE_PART, EQUIV_TO_BASE_PART);

		// load all the source code of the Toolbox, organized in parts
		codePartCtrl.loadCodeDirectory("src/com/asofterspace/toolbox");

		codePartCtrl.sortParts();

		// output what we have found so far
		System.out.println("We loaded the following parts:");
		System.out.println("");

		int classAmount = 0;
		for (CodePart part : codePartCtrl.getParts()) {
			int curClassAmount = part.getClassAmount();
			System.out.println(part.getName() + " containing " + curClassAmount + " classes");
			classAmount += curClassAmount;
		}

		System.out.println("");
		System.out.println("All parts in total contain " + classAmount + " classes.");

		codePartCtrl.calculateDependenciesBetweenCodeParts();

		// output the information in a nice output file
		JsonFile outputFile = new JsonFile("analysis.json");
		Record output = Record.emptyObject();
		Record outParts = Record.emptyArray();
		output.set("parts", outParts);
		for (CodePart part : codePartCtrl.getParts()) {
			Record outPart = Record.emptyObject();
			outParts.append(outPart);
			outPart.set("name", part.getName());
			Record outDependsOn = Record.emptyArray();
			outPart.set("directlyDependsOn", outDependsOn);
			for (CodePart dep : part.getDirectDependencies()) {
				outDependsOn.append(dep.getName());
			}
			Record outDependsOnTrans = Record.emptyArray();
			outPart.set("transitivelyDependsOn", outDependsOnTrans);
			for (CodePart dep : part.getTransitiveDependencies()) {
				outDependsOnTrans.append(dep.getName());
			}
			Record outAllDependencies = Record.emptyArray();
			outPart.set("allDependencies", outAllDependencies);
			for (CodePart dep : part.getAllDependencies()) {
				outAllDependencies.append(dep.getName());
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
