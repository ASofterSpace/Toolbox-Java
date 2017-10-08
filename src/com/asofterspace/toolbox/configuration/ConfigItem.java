package com.asofterspace.toolbox.configuration;

public class ConfigItem {
	
	private String itemkey;
	
	private String itemvalue;
	

	/**
	 * Please do not construct a config item without a name ;)
	 */
	@SuppressWarnings("unused")
	private ConfigItem() {
	}

	/**
	 * Creates a new config item with a key and a default value (null)
	 * @param key
	 */
	public ConfigItem(String key) {
		
		itemkey = key;
		itemvalue = null;
	}

	/**
	 * Creates a new config item with a key and a value
	 * @param key
	 * @param value
	 */
	public ConfigItem(String key, String value) {
		
		itemkey = key;
		itemvalue = value;
	}

	/**
	 * Checks whether this config item has the given key
	 * @param key
	 * @return true if the key is the same, else otherwise
	 */
	public boolean hasKey(String key) {

		return itemkey.equals(key);
	}

	/**
	 * Gets the value of this config item
	 * @return the value as string
	 */
	public String getValue() {

		return itemvalue;
	}

	/**
	 * Sets the value of this config item to the given string
	 * @param value
	 */
	public void setValue(String value) {

		itemvalue = value;
	}

}
