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

	private final static String PROJECT_KEYS = "keys";

	private Directory projectsDir;

	protected List<GenericProject> projects;


	public GenericProjectCtrl(String projectsDirStr) {

		projectsDir = new Directory(projectsDirStr);

		projects = new ArrayList<>();
	}

	// always call init after the contructor - the controller is not ready until init is called,
	// but calling init in the constructor makes java21 sad because it calls other methods
	// (which is exactly how it should be, they are there to be called exactly during construction,
	// aaaarghs... .-. this nonsense here is so much more prone to errors x_X')
	public void init() {

		JsonFile projectsFile = new JsonFile(projectsDir, "projects.json");
		if (!projectsFile.exists()) {
			Record projectsRec = Record.emptyObject();
			projectsRec.set(PROJECT_KEYS, Record.emptyArray());
			projectsFile.save(projectsRec);
		}
		try {
			Record projectsRec = projectsFile.getAllContents();

			List<String> projectsKeyList = projectsRec.getArrayAsStringList(PROJECT_KEYS);
			for (String curProjKey : projectsKeyList) {
				projects.add(createProject(curProjKey, projectsDir));
			}
		} catch (JsonParseException ex) {
			System.err.println("The input file " + projectsFile.getAbsoluteFilename() +
				" could not be read due to:\n" + ex);
			System.exit(1);
		}
	}

	protected GenericProject createProject(String curProjKey, Directory projectsDir) {
		return new GenericProject(curProjKey, projectsDir);
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

	public GenericProject resolveFullNameGenerically(String str) {
		if (str == null) {
			return null;
		}
		str = str.toLowerCase();
		for (GenericProject proj : projects) {
			if (str.equals(proj.getFullName().toLowerCase())) {
				return proj;
			}
		}
		return null;
	}
}
