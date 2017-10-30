package com.asofterspace.toolbox.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.asofterspace.toolbox.io.JSONkind;

import com.asofterspace.toolbox.Utils;

public class JSON {
	
	private JSONkind kind;
	
	private Map<String, JSON> objContents;
	private List<JSON> arrContents;
	private Object simpleContents;
	
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

			objContents = new HashMap<String, JSON>();
			
			kind = JSONkind.OBJECT;
			
			jsonString = jsonString.substring(1).trim();
			
			while (jsonString.startsWith("\"")) {
				jsonString = jsonString.substring(1).trim();
				int endIndex = jsonString.indexOf("\"");
				String key = jsonString.substring(0, endIndex);
				jsonString = jsonString.substring(endIndex);
				jsonString = jsonString.substring(jsonString.indexOf(":") + 1);
				JSON value = new JSON(jsonString);
				objContents.put(key, value);
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

		} else if (jsonString.startsWith("[")) {
			
			kind = JSONkind.ARRAY;
			
			arrContents = new ArrayList<JSON>();
			
			// TODO :: load array contents
				
		} else {
			
			kind = JSONkind.STRING;
			
			int endIndex1 = jsonString.indexOf(",");
			int endIndex2 = jsonString.indexOf("}");
			
			String simpleContentStr;

			if (endIndex2 < 0) {
				simpleContentStr = jsonString;
				restString = "";
			} else if ((endIndex1 < 0) || (endIndex1 > endIndex2)) {
				simpleContentStr = jsonString.substring(0, endIndex2);
				restString = jsonString.substring(endIndex2);
			} else {
				simpleContentStr = jsonString.substring(0, endIndex1);
				restString = jsonString.substring(endIndex1);
			}
			
			if (simpleContentStr.startsWith("\"") && simpleContentStr.endsWith("\"")) {
				simpleContentStr = simpleContentStr.substring(1, simpleContentStr.length() - 1);
			}
			
			simpleContents = simpleContentStr;
		}
	}

	@Override
	public String toString() {
		
		switch (kind) {

			case STRING:
				return "\"" + simpleContents.toString() + "\"";
				
			case BOOLEAN:
			case NUMBER:
				return simpleContents.toString();
			
			case ARRAY:
				StringBuilder arrResult = new StringBuilder();
				
				arrResult.append("[");

				boolean arrFirstEntry = true;
				
				for (JSON item : arrContents) {

					if (arrFirstEntry) {
						arrFirstEntry = false;
					} else {
						arrResult.append(", ");
					}
					
					arrResult.append(item.toString());
				}
				arrResult.append("]");
				
				return arrResult.toString();
				
			case OBJECT:
				StringBuilder objResult = new StringBuilder();
				
				objResult.append("{");
				
				boolean objFirstEntry = true;
				
				for (Map.Entry<String, JSON> entry : objContents.entrySet()) {
					
					if (objFirstEntry) {
						objFirstEntry = false;
					} else {
						objResult.append(", ");
					}
					
				    String key = entry.getKey();
				    JSON content = entry.getValue();
				    
				    objResult.append("\"");
				    objResult.append(key);
				    objResult.append("\": ");
				    objResult.append(content.toString());
				}
				
				objResult.append("}");
				
				return objResult.toString();
				
			default:
				return "null";
		}
	}

	/**
	 * Get the kind of JSON object this is
	 * @return the kind
	 */
	public JSONkind getKind() {
		return kind;
	}

	/**
	 * Get the set of all defined keys in this JSON object
	 * @return the set of defined keys
	 */
	public Set<String> getKeys() {
		return objContents.keySet();
	}

	/**
	 * Get the JSON-value corresponding to a specific key
	 * in a JSON object
	 * @param key the key to search for
	 * @return the JSON object
	 */
	public JSON get(String key) {
		return objContents.get(key);
	}

	/**
	 * Get the length of the array represented by this JSON
	 * array
	 * @return the length of the JSON array
	 */
	public int getLength() {
		return arrContents.size();
	}

	/**
	 * Get the JSON-value corresponding to a specific index
	 * in a JSON array
	 * @param index the index to get
	 * @return the JSON object
	 */
	public JSON get(Integer index) {
		return arrContents.get(index);
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
	public String getString(String key) {
		JSON result = get(key);
		
		// in case of a string, return the contained string WITHOUT
		// enclosing ""-signs
		if (result.kind == JSONkind.STRING) {
			return result.simpleContents.toString();
		}
		
		// if something else than a string is contained, return whatever
		// it is that is contained
		return result.toString();
	}

	/**
	 * Sets a key of the JSON object to the JSON value
	 * @param key
	 * @param value
	 */
	public void set(String key, JSON value) {
		kind = JSONkind.OBJECT;
		if (objContents == null) {
			objContents = new HashMap<String, JSON>();
		}
		objContents.put(key.toString(), value);
	}

	/**
	 * Sets a key to the string value
	 * @param key
	 * @param value
	 */
	public void setString(String key, String value) {

		JSON jsonValue = new JSON(value);
		
		set(key, jsonValue);
	}
	
	/**
	 * Sets an index of the JSON array to the JSON value
	 * @param index
	 * @param value
	 */
	public void set(Integer index, JSON value) {
		kind = JSONkind.ARRAY;
		if (arrContents == null) {
			arrContents = new ArrayList<JSON>();
		}
		arrContents.set(index, value);
	}

	/**
	 * Appends a JSON value to the JSON array
	 * @param value
	 */
	public void append(JSON value) {
		kind = JSONkind.ARRAY;
		if (arrContents == null) {
			arrContents = new ArrayList<JSON>();
		}
		arrContents.add(value);
	}

	public void save(File targetFile) {
		
		targetFile.saveContent(this.toString());
	}

}
