/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.configuration;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.Record;

import java.util.List;


/**
 * A ConfigFile is basically a more simple to use JsonFile.
 * There are two main differences between the two though.
 *
 * 1) The first difference is that a JsonFile (just as all other io file classes here) only
 * starts reading from disk when you actually start reading a value from it - just creating
 * the file instance will not do anything. This however means that every single call can
 * result in a parse exception.
 * On the other hand, a config file reads the content immediately upon creation, and throws
 * an exception or not - but does not throw any exceptions anymore afterwards when you
 * actually access its values.
 * The first different behavior is why this is in a different package, and why it is so
 * genuinely useful for config files - of which we generally assume that they exist anyway
 * and that we want to read and edit them - not overwrite with completely new data without
 * reading first.
 *
 * 2) JsonFile (again, just as all other io file classes here) only saves its content to
 * disk when explicitly instructed to do so. Just by setting a value, nothing is saved yet,
 * making it easier to write better performing software in case you need to set several
 * values at once.
 * On the other hand, a ConfigFile automatically saves every single time that you set a
 * value - which is again more in line with what you would expect from software config-
 * uration, that should be immediately written out to disk once the user makes one change
 * such that if the program crashes later on, the state is not lost.
 */
public class ConfigFile extends JsonFile {

	private static final String FOLDER = "/config/";

	public static final String FILE_EXTENSION = ".cnf";


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
	public ConfigFile(String name) throws JsonParseException {

		super(getConfigFilename(name, false));

		createParentDirectory();

		// EVERY ConfigFile constructor MUST call ensureContent()!
		ensureContent();
	}

	/**
	 * Creates a config file with the given name, either absolute (if the path is absolute)
	 * or relative; if the baseOnClasspath argument is true, then relative to the classpath,
	 * otherwise relative to the current working directory
	 * @param name The name of the config file
	 * @param baseOnClasspath true if we want to base a relative file on the classpath, false otherwise
	 */
	public ConfigFile(String name, Boolean baseOnClasspath) throws JsonParseException {

		super(getConfigFilename(name, baseOnClasspath));

		createParentDirectory();

		// EVERY ConfigFile constructor MUST call ensureContent()!
		ensureContent();
	}

	/**
	 * Creates a config file with the given name, absolute or not, and directly filled with
	 * new content - so NO content will be read from the disk, but it will be written immediately!
	 */
	public ConfigFile(String name, Boolean baseOnClasspath, Record newContent) {

		this(name, baseOnClasspath, newContent, false);
	}

	/**
	 * Creates a config file with the given name, absolute or not, and directly filled with
	 * new content - so NO content will be read from the disk, but it will be written immediately!
	 */
	public ConfigFile(String name, Boolean baseOnClasspath, Record defaultContent, boolean onlyUseDefaultIfBroken) {

		super(getConfigFilename(name, baseOnClasspath));

		createParentDirectory();

		if (onlyUseDefaultIfBroken) {
			try {
				ensureContent();
				return;
			} catch (JsonParseException e) {
				// fallthrough to setting the content
			}
		}

		// EVERY ConfigFile constructor MUST call ensureContent() - except for this one,
		// as we ensure there is content by setting it directly!
		super.setAllContents(defaultContent);

		create();
	}

	protected void ensureContent() throws JsonParseException {

		try {
			super.ensureContent();
		} catch (JsonParseException e) {
			throw new JsonParseException(e.getMessage() + " \nin file " + getAbsoluteFilename(), e);
		}
	}

	public static String getConfigFilename(String name, Boolean baseOnClasspath) {

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

	/**
	 * Gets all the contents as JSON container
	 * @return all the contents
	 */
	@Override
	public JSON getAllContents() {

		return jsonContent;
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or null if it cannot be found
	 */
	@Override
	public String getValue(String key) {

		return jsonContent.getString(key);
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @param defaultValue
	 * @return the value stored in the key, or defaultValue if it cannot be found
	 */
	public String getValue(String key, String defaultValue) {

		String result = getValue(key);

		if (result == null) {
			return defaultValue;
		}

		return result;
	}

	/**
	 * Gets a list of values stored with the given key,
	 * so if the JSON is {"bla": ["1", "2", "3"]},
	 * then getList("bla") gives a list containing "1", "2" and "3"
	 * @param key
	 * @return the list stored in the key, or an empty list if it cannot be found
	 */
	@Override
	public List<String> getList(String key) {

		return jsonContent.getArrayAsStringList(key);
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @return the value stored in the key, or null if it cannot be found
	 */
	@Override
	public Integer getInteger(String key) {

		Integer result = jsonContent.getInteger(key);

		return result;
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @param defaultValue
	 * @return the value stored in the key, or defaultValue if it cannot be found
	 */
	@Override
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
	@Override
	public Boolean getBoolean(String key) {

		Boolean result = jsonContent.getBoolean(key);

		return result;
	}

	/**
	 * Gets the value stored with the given key
	 * @param key
	 * @param defaultValue
	 * @return the value stored in the key, or defaultValue if it cannot be found
	 */
	@Override
	public boolean getBoolean(String key, boolean defaultValue) {

		Boolean result = getBoolean(key);

		if (result == null) {
			return defaultValue;
		}

		return result;
	}

	// setting works exactly like in JSON files - but we are making it a bit simpler:
	// we are also saving, and also ensuring the parent folder exists
	@Override
	public void setAllContents(Record newContent) {

		super.setAllContents(newContent);

		create();
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	@Override
	public void set(String key, String value) {

		jsonContent.setString(key, value);

		create();
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	@Override
	public void set(String key, Integer value) {

		Record recordVal = new Record(value);

		JSON jsonVal = new JSON(recordVal);

		jsonContent.set(key, jsonVal);

		create();
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	@Override
	public void set(String key, Boolean value) {

		Record recordVal = new Record(value);

		JSON jsonVal = new JSON(recordVal);

		jsonContent.set(key, jsonVal);

		create();
	}

	/**
	 * Stores the value in the configuration with the given key
	 * @param key
	 * @param value
	 */
	@Override
	public void set(String key, JSON value) {

		jsonContent.set(key, value);

		create();
	}
}
