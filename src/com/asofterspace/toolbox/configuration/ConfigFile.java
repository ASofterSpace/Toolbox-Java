/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.configuration;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.Record;


public class ConfigFile extends JsonFile {

	private static final String FOLDER = "/config/";

	private static final String FILE_EXTENSION = ".cnf";


	/**
	 * Please do not construct a config file without a name ;)
	 */
	protected ConfigFile() {
	}

	/**
	 * Creates a config file with the given name, either absolute (if the path is absolute)
	 * or relative to the current working directory (if the path is relative)
	 * @param name The name of the config file
	 */
	public ConfigFile(String name) {

		super(getConfigFilename(name, false));

		createParentDirectory();
	}

	/**
	 * Creates a config file with the given name, either absolute (if the path is absolute)
	 * or relative; if the baseOnClasspath argument is true, then relative to the classpath,
	 * otherwise relative to the current working directory
	 * @param name The name of the config file
	 * @param baseOnClasspath true if we want to base a relative file on the classpath, false otherwise
	 */
	public ConfigFile(String name, Boolean baseOnClasspath) {

		super(getConfigFilename(name, baseOnClasspath));

		createParentDirectory();
	}

	private static String getConfigFilename(String name, Boolean baseOnClasspath) {

		if (baseOnClasspath == null) {
			baseOnClasspath = false;
		}

		// start with the given name
		String filename = name;

		// apply an extension, if necessary
		if (!filename.contains(".")) {
			filename += FILE_EXTENSION;
		}

		// if we are not using an absolute or explicitly relative path (so starting with ./ or ../)...
		if (!(filename.startsWith("/") || filename.startsWith("./") || filename.startsWith("../"))) {
			// ... then either use a relative file ...
			if (baseOnClasspath) {
				// ... based on the classpath ...
				filename = System.getProperty("java.class.path") + "/.." + FOLDER + filename;
			} else {
				// ... or based on the current working directory
				filename = "." + FOLDER + filename;
			}
		}

		return filename;
	}

	// setting works exactly like in JSON files - but we are making it a bit simpler:
	// we are also saving, and also ensuring the parent folder exists
	public void setAllContents(Record newContent) {
		super.setAllContents(newContent);
		create();
	}
	public void set(String key, String value) {
		super.set(key, value);
		create();
	}
	public void set(String key, Integer value) {
		super.set(key, value);
		create();
	}
	public void set(String key, Boolean value) {
		super.set(key, value);
		create();
	}
	public void set(String key, JSON value) {
		super.set(key, value);
		create();
	}
}
