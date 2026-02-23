/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.projects;

import com.asofterspace.toolbox.images.ColorRGBA;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.Record;


public class GenericProject {

	private String fullName;

	private String shortName;

	private ColorRGBA color;

	private ColorRGBA colorDirty;

	private String colorDirtySpecial;

	private Boolean onShortlist;

	private Boolean projIsWork;

	private Directory projectDir;


	public GenericProject(String key, Directory projectParentDir) {

		this.shortName = key;

		this.projectDir = new Directory(projectParentDir, shortName);

		this.projectDir.create();

		JsonFile projectConfFile = new JsonFile(projectDir, "project.json");
		Record projectConfRec = createRecord();
		if (!projectConfFile.exists()) {
			projectConfFile.save(projectConfRec);
		}
		try {
			projectConfRec = projectConfFile.getAllContents();
		} catch (JsonParseException ex) {
			System.err.println("The project config file " + projectConfFile.getAbsoluteFilename() +
				" could not be read due to:\n" + ex);
			System.exit(1);
		}

		readRecord(projectConfRec);
	}

	protected void readRecord(Record projectConfRec) {

		this.fullName = projectConfRec.getString("name");

		this.color = ColorRGBA.fromString(projectConfRec.getString("color"));

		String colorDirtyStr = projectConfRec.getString("colorDirty");
		if (colorDirtyStr != null) {
			this.colorDirty = ColorRGBA.fromString(colorDirtyStr);
		}

		this.colorDirtySpecial = projectConfRec.getString("colorDirtySpecial");

		this.onShortlist = projectConfRec.getBoolean("onShortlist");

		this.projIsWork = projectConfRec.getBoolean("isWork");
	}

	protected Record createRecord() {
		Record projectConfRec = Record.emptyObject();
		projectConfRec.set("color", "#EEEEEE");
		projectConfRec.set("name", this.fullName);
		return projectConfRec;
	}

	public String getFullName() {
		return fullName;
	}

	public String getShortName() {
		return shortName;
	}

	public ColorRGBA getColor() {
		return color;
	}

	public ColorRGBA getColorDirty() {
		if (colorDirty == null) {
			return ColorRGBA.multiply(getColor(), new ColorRGBA(128, 255, 0));
		}
		return colorDirty;
	}

	public String getColorDirtyStr() {
		if (colorDirtySpecial != null) {
			return colorDirtySpecial;
		}
		return getColorDirty().toHexString();
	}

	public boolean isOnShortlist() {
		if (onShortlist == null) {
			return false;
		}
		return onShortlist;
	}

	public boolean isWork() {
		if (projIsWork == null) {
			return false;
		}
		return projIsWork;
	}

	public Directory getProjectDir() {
		return projectDir;
	}

	@Override
	public boolean equals(Object other) {

		// If the other one does not even exist, we are not the same - because we exist!
		if (other == null) {
			return false;
		}

		if (other instanceof GenericProject) {
			GenericProject otherProject = (GenericProject) other;

			// If our values for shortName are different...
			if (this.shortName == null) {
				if (otherProject.shortName != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.shortName.equals(otherProject.shortName)) {
				// ... then we are not the same!
				return false;
			}

			// We have no reason to assume that we are not the same
			return true;
		}

		// If the other one cannot even be cast to us, then we are not the same!
		return false;
	}

	@Override
	public int hashCode() {
		if (this.shortName == null) {
			return 0;
		}
		return this.shortName.hashCode();
	}

}
