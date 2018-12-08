/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;


public class JsonFile extends File {

	private JSON jsonContent = null;


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

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or null if it cannot be found
	 */
	public String getValue(String key) {
		
		if (jsonContent == null) {
			loadJsonContents();
		}

		return jsonContent.getString(key);
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or 0 if it cannot be found
	 */
	public int getInteger(String key) {

		if (jsonContent == null) {
			loadJsonContents();
		}

		Integer result = jsonContent.getInteger(key);
		
		if (result == null) {
			return 0;
		}
		
		return result;
	}

	public void setAllContents(JSON newContent) {
		
		this.jsonContent = newContent;
	}

	public void set(String key, String value) {

		if (jsonContent == null) {
			loadJsonContents();
		}

		jsonContent.setString(key, value);
	}

	public void set(String key, Integer value) {

		if (jsonContent == null) {
			loadJsonContents();
		}

		jsonContent.set(key, new JSON(value));
	}
	
	public void save() {
		
		String uncompressedJson = jsonContent.toString(false);
		
		setContent(uncompressedJson);
		
		super.save();
	}
}
