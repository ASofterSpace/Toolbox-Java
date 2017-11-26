package com.asofterspace.toolbox.configuration;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.web.JSON;

public class ConfigFile {
	
	private String filename;
	
	private JSON content;
	
	private static final String FOLDER = "./config/";
	
	private static final String FILE_EXTENSION = ".cnf";

	
	/**
	 * Please do not construct a config file without a name ;)
	 */
	@SuppressWarnings("unused")
	private ConfigFile() {
	}

	/**
	 * Creates a config file with the given name
	 * @param name The name of the config file (without its extension or directory-part)
	 */
	public ConfigFile(String name) {

		// if a full pathname is given, then just use that
		// (however, an extension will still be appended in the end anyway!)
		filename = name;

		if (!filename.contains(".")) {
			filename = filename + FILE_EXTENSION;
		}

		if (!filename.contains("/")) {
			// if just a word or somesuch is given (without any /-signs),
			// then we probably want a local-ish file
			filename = FOLDER + filename;
		}
		
		loadFromFile();
	}

	/**
	 * Loads the configuration from a file (this is called internally
	 * and does not need to be called from the outside world)
	 */
	private void loadFromFile() {
		
		File correspondingFile = new File(filename);
		
		content = new JSON(correspondingFile.getContent());
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

		Integer result = content.getInteger(key);
		
		if (result == null) {
			return 0;
		}
		
		return result;
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
	
}
