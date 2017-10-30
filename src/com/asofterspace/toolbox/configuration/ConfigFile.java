package com.asofterspace.toolbox.configuration;

import java.util.ArrayList;
import java.util.List;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;

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
		
		filename = name;
		
		loadFromFile();
	}
	
	/**
	 * Loads the configuration from a file (this is called internally
	 * and does not need to be called from the outside world)
	 */
	private void loadFromFile() {
		
		File correspondingFile = new File(FOLDER + filename + FILE_EXTENSION);
		
		content = new JSON(correspondingFile);
	}
	
	/**
	 * Stores the configuration on the file system (this is called
	 * internally and does not need to be called from the outside
	 * world)
	 */
	private void saveToFile() {
		
		File correspondingFile = new File(FOLDER + filename + FILE_EXTENSION);
		
		content.save(correspondingFile);
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
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {

		content.setString(key, value);
		
		saveToFile();
	}
	
}
