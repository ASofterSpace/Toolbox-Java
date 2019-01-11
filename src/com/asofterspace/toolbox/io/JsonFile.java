/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;


public class JsonFile extends SimpleFile {

	private JSON jsonContent = null;


	/**
	 * Please do not construct a file without a name ;)
	 */
	protected JsonFile() {
	}

	/**
	 * You can construct a JsonFile instance by directly from a path name.
	 */
	public JsonFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a JsonFile instance by basing it on an existing file object.
	 */
	public JsonFile(File regularFile) {

		super(regularFile);
	}

	protected void loadJsonContents() {

		jsonContent = new JSON(getContent(false));
	}

	/**
	 * Gets all the contents as JSON container
	 * @return all the contents
	 */
	public JSON getAllContents() {

		if (jsonContent == null) {
			loadJsonContents();
		}

		return jsonContent;
	}

	private void ensureContent() {

		if (jsonContent == null) {
			loadJsonContents();
		}
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or null if it cannot be found
	 */
	public String getValue(String key) {

		ensureContent();

		return jsonContent.getString(key);
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or null if it cannot be found
	 */
	public Integer getInteger(String key) {

		ensureContent();

		Integer result = jsonContent.getInteger(key);

		return result;
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @param defaultValue
	 * @return the value stored in the key, or defaultValue if it cannot be found
	 */
	public int getInteger(String key, int defaultValue) {

		Integer result = getInteger(key);

		if (result == null) {
			return defaultValue;
		}

		return result;
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or null if it cannot be found
	 */
	public Boolean getBoolean(String key) {

		ensureContent();

		Boolean result = jsonContent.getBoolean(key);

		return result;
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @param defaultValue
	 * @return the value stored in the key, or defaultValue if it cannot be found
	 */
	public boolean getBoolean(String key, boolean defaultValue) {

		Boolean result = getBoolean(key);

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

		this.jsonContent = newContent;
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {

		ensureContent();

		jsonContent.setString(key, value);
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, Integer value) {

		ensureContent();

		jsonContent.set(key, new JSON(value));
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, Boolean value) {

		ensureContent();

		jsonContent.set(key, new JSON(value));
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	public void set(String key, JSON value) {

		ensureContent();

		jsonContent.set(key, value);
	}

	/**
	 * Creates this file on the disk, which entails:
	 * - creating the parent directory
	 * - assigning an empty file content, if there is not already content
	 *   (if this file has already been assigned content, that content will
	 *   be kept!)
	 * - saving the file to disk
	 */
	public void create() {

		createParentDirectory();

		if (jsonContent == null) {
			jsonContent = new JSON();
		}

		save();
	}

	public void save() {

		String uncompressedJson = jsonContent.toString(false);

		setContent(uncompressedJson);

		super.save();
	}
}
