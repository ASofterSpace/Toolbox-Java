/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.projects;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.List;


public class GenericProjectCtrl {

	private final static String PROJECT_NAMES = "names";

	private Directory projectsDir;

	protected List<GenericProject> projects;


	public GenericProjectCtrl(String projectsDirStr) {

		projectsDir = new Directory(projectsDirStr);

		JsonFile projectsFile = new JsonFile(projectsDir, "projects.json");
		Record projectsRec = Record.emptyObject();
		projectsRec.set(PROJECT_NAMES, Record.emptyArray());
		if (!projectsFile.exists()) {
			projectsFile.save(projectsRec);
		}
		try {
			projectsRec = projectsFile.getAllContents();
		} catch (JsonParseException ex) {
			System.err.println("The input file " + projectsFile.getAbsoluteFilename() +
				" could not be read due to:\n" + ex);
			System.exit(1);
		}
		List<String> projectsNameList = projectsRec.getArrayAsStringList(PROJECT_NAMES);
		projects = new ArrayList<>();
		for (String curProjName : projectsNameList) {
			projects.add(createProject(curProjName, projectsDir));
		}
	}

	protected GenericProject createProject(String curProjName, Directory projectsDir) {
		return new GenericProject(curProjName, projectsDir);
	}

	public List<GenericProject> getGenericProjects() {
		return projects;
	}

	public Directory getProjectsDir() {
		return projectsDir;
	}

	public List<File> getProjectFiles() {
		boolean recursively = true;
		return projectsDir.getAllFiles(recursively);
	}

	public GenericProject resolveShortNameGenerically(String str) {
		if (str == null) {
			return null;
		}
		for (GenericProject proj : projects) {
			if (str.equals(proj.getShortName())) {
				return proj;
			}
		}
		return null;
	}
}
