package com.asofterspace.toolbox.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asofterspace.toolbox.Utils;

public class JSON {
	
	private boolean isSimpleValue;
	private String simpleValue;
	private Map<String, JSON> contents;
	
	private String restString;

	
	/**
	 * Create an empty JSON object
	 */
	public JSON() {
		init("{}");
	}
		
	/**
	 * Create a JSON object based on a given JSON string
	 */
	public JSON(String jsonString) {
		
		init(jsonString);
	}
	
	/**
	 * Create a JSON object based on a given list of strings representing
	 * JSON file contents
	 */
	public JSON(List<String> jsonStrings) {
		
		String jsonContent = Utils.strListToString(jsonStrings);
		
		init(jsonContent);
	}

	/**
	 * Create a JSON object based on a given file instance containing JSON
	 * data
	 */
	public JSON(File jsonFile) {

		List<String> jsonStrings = jsonFile.loadContents();
		
		String jsonContent = Utils.strListToString(jsonStrings);
		
		init(jsonContent);
	}

	/**
	 * Initialize this JSON object based on a given JSON string
	 * @param jsonString
	 */
	private void init(String jsonString) {
		
		jsonString = jsonString.trim();
		
		if (jsonString.startsWith("{")) {
			
			isSimpleValue = false;
			contents = new HashMap<String, JSON>();
			
			jsonString = jsonString.substring(1).trim();
			
			while (jsonString.startsWith("\"")) {
				jsonString = jsonString.substring(1).trim();
				int endIndex = jsonString.indexOf("\"");
				String key = jsonString.substring(0, endIndex);
				jsonString = jsonString.substring(endIndex);
				jsonString = jsonString.substring(jsonString.indexOf(":") + 1);
				JSON value = new JSON(jsonString);
				contents.put(key, value);
				jsonString = value.restString.trim();
				if (jsonString.startsWith(",")) {
					jsonString = jsonString.substring(1).trim();
				}
			}
			
			jsonString = jsonString.trim();
			
			if (jsonString.startsWith("}")) {
				jsonString = jsonString.substring(1).trim();
			}
			
			restString = jsonString;
			
		} else {
			isSimpleValue = true;
			
			int endIndex1 = jsonString.indexOf(",");
			int endIndex2 = jsonString.indexOf("}");

			if (endIndex2 < 0) {
				simpleValue = jsonString;
				restString = "";
			} else if ((endIndex1 < 0) || (endIndex1 > endIndex2)) {
				simpleValue = jsonString.substring(0, endIndex2);
				restString = jsonString.substring(endIndex2);
			} else {
				simpleValue = jsonString.substring(0, endIndex1);
				restString = jsonString.substring(endIndex1);
			}
			
			if (simpleValue.startsWith("\"") && simpleValue.endsWith("\"")) {
				simpleValue = simpleValue.substring(1, simpleValue.length() - 1);
			}
		}
	}

	@Override
	public String toString() {
		
		if (isSimpleValue) {
			return simpleValue;
		}
		
		StringBuilder result = new StringBuilder();
		
		result.append("{");
		
		boolean firstEntry = true;
		
		for (Map.Entry<String, JSON> entry : contents.entrySet()) {
			
			if (firstEntry) {
				firstEntry = false;
			} else {
			    result.append(", ");
			}
			
		    String key = entry.getKey();
		    JSON content = entry.getValue();
		    
		    result.append("\"");
		    result.append(key);
		    result.append("\": ");
		    if (content.isSimpleValue) {
		    	result.append("\"" + content.simpleValue + "\"");
		    } else {
		    	result.append(content.toString());
		    }
		}
		
		result.append("}");
		
		return result.toString();
	}
	
	/**
	 * Get the set of all defined keys in this JSON object
	 * @return the set of defined keys
	 */
	public Set<String> getKeys() {
		return contents.keySet();
	}

	/**
	 * Get the value corresponding to a specific key
	 * (which could be a JSON object just representing
	 * a plain string, or a complex JSON object on its own)
	 * @param key the key to search for
	 * @return the JSON object - possibly a plain string - stored
	 */
	public JSON get(Object key) {
		return contents.get(key.toString());
	}

	/**
	 * Gets a string value stored in a key (which will
	 * just return a string as string if the value is
	 * already a string, but if the value is a complex
	 * JSON object, then it will be coaxed into a string,
	 * which does not necessarily mean that it is a string
	 * that makes much sense - beware!)
	 * @param key the key to search for
	 * @return the JSON object - hopefully a plain string - as string
	 */
	public String getString(Object key) {
		return get(key).toString();
	}
	
	/**
	 * Sets a key to the JSON value
	 * @param key
	 * @param value
	 */
	public void set(Object key, JSON value) {
		contents.put(key.toString(), value);
	}

	/**
	 * Sets a key to the string value
	 * @param key
	 * @param value
	 */
	public void setString(Object key, String value) {
		JSON jsonValue = new JSON(value);
		contents.put(key.toString(), jsonValue);
	}

}
