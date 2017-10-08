package com.asofterspace.toolbox.configuration;

import java.util.ArrayList;
import java.util.List;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;

public class ConfigFile {
	
	private String filename;
	
	private List<ConfigItem> configItems;
	
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
		
		JSON jsonContents = new JSON(correspondingFile);

		configItems = new ArrayList<ConfigItem>();
		
		for (String key : jsonContents.getKeys()) {
			
			String value = jsonContents.getString(key);
			
			ConfigItem newItem = new ConfigItem(key, value);
			
			configItems.add(newItem);
		}
	}

	/**
	 * Stores the configuration on the file system (this is called
	 * internally and does not need to be called from the outside
	 * world)
	 */
	private void saveToFile() {
		
		// TODO
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or null if it cannot be found
	 */
	public String get(String key) {
		
		for (ConfigItem item : configItems) {
			
			if (item.hasKey(key)) {
				return item.getValue();
			}
		}
		
		return null;
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {

		for (ConfigItem item : configItems) {
			
			if (item.hasKey(key)) {
				item.setValue(value);
				saveToFile();
				return;
			}
		}
		
		ConfigItem newItem = new ConfigItem(key, value);
		
		configItems.add(newItem);
		
		saveToFile();
	}
	
}
