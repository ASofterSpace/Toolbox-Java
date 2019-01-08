/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.configuration;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;


public class ConfigFile {

	private String filename;

	private JSON content;

	private static final String FOLDER = "/config/";

	private static final String FILE_EXTENSION = ".cnf";


	/**
	 * Please do not construct a config file without a name ;)
	 */
	@SuppressWarnings("unused")
	private ConfigFile() {
	}

	/**
	 * Creates a config file with the given name, either absolute (if the path is absolute)
	 * or relative to the current working directory (if the path is relative)
	 * @param name The name of the config file
	 */
	public ConfigFile(String name) {

		setConfigFilename(name, false);
	}

	/**
	 * Creates a config file with the given name, either absolute (if the path is absolute)
	 * or relative; if the baseOnClasspath argument is true, then relative to the classpath,
	 * otherwise relative to the current working directory
	 * @param name The name of the config file
	 * @param baseOnClasspath true if we want to base a relative file on the classpath, false otherwise
	 */
	public ConfigFile(String name, Boolean baseOnClasspath) {

		setConfigFilename(name, baseOnClasspath);
	}

	private void setConfigFilename(String name, Boolean baseOnClasspath) {

		if (baseOnClasspath == null) {
			baseOnClasspath = false;
		}

		// start with the given name
 		filename = name;

		// apply an extension, if necessary
 		if (!filename.contains(".")) {
			filename += FILE_EXTENSION;
 		}

		// if we are not using an absolute path...
		if (!filename.startsWith("/")) {
			// ... then either use a relative file ...
			if (baseOnClasspath) {
				// ... based on the classpath ...
				filename = System.getProperty("java.class.path") + "/.." + FOLDER + filename;
			} else {
				// ... or based on the current working directory
				filename = "." + FOLDER + filename;
			}
 		}

 		loadFromFile();
	}

	/**
	 * Loads the configuration from a file (this is called internally
	 * and does not need to be called from the outside world)
	 */
	private void loadFromFile() {

		File correspondingFile = new File(filename);

		// if no file could be loaded, then we will have to default to an empty one
		if (correspondingFile == null) {
			content = new JSON();
		} else {
			// here, we do NOT want to complain if the content is missing - as it is entirely reasonable
			// that a program is opened for the first time, and the config has not been written before
			content = new JSON(correspondingFile.getContent(false));
		}
	}

	/**
	 * Stores the configuration on the file system (this is called
	 * internally and does not need to be called from the outside
	 * world)
	 */
	private void saveToFile() {

		String uncompressedJson = content.toString(false);

		File correspondingFile = new File(filename);

		correspondingFile.saveContent(uncompressedJson);
	}

	/**
	 * Gets all the contents as JSON container
	 * @return all the contents
	 */
	public JSON getAllContents() {

		return content;
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or null if it cannot be found
	 */
	public String getValue(String key) {

		return content.getString(key);
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or 0 if it cannot be found
	 */
	public int getInteger(String key) {

		return getInteger(key, 0);
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @param defaultValue
	 * @return the value stored in the key, or defaultValue if it cannot be found
	 */
	public int getInteger(String key, int defaultValue) {

		Integer result = content.getInteger(key);

		if (result == null) {
			return defaultValue;
		}

		return result;
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or false if it cannot be found
	 */
	public boolean getBoolean(String key) {

		return getBoolean(key, false);
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @param defaultValue
	 * @return the value stored in the key, or defaultValue if it cannot be found
	 */
	public boolean getBoolean(String key, boolean defaultValue) {

		Boolean result = content.getBoolean(key);

		if (result == null) {
			return defaultValue;
		}

		return result;
	}

	/**
	 * Sets all the contents based on a JSON container
	 * @param newContent
	 */
	public void setAllContents(JSON newContent) {

		this.content = newContent;

		saveToFile();
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {

		content.setString(key, value);

		saveToFile();
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, Integer value) {

		content.set(key, new JSON(value));

		saveToFile();
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, Boolean value) {

		content.set(key, new JSON(value));

		saveToFile();
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, JSON value) {

		content.set(key, value);

		saveToFile();
	}

}
