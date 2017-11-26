package com.asofterspace.toolbox.web;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.asofterspace.toolbox.web.JSONkind;

import com.asofterspace.toolbox.Utils;

public class JSON {
	
	private JSONkind kind;
	
	private Map<String, JSON> objContents;
	private List<JSON> arrContents;
	private Object simpleContents;

	
	/**
	 * Create an empty JSON object
	 */
	public JSON() {

		kind = JSONkind.OBJECT;
		
		objContents = new TreeMap<String, JSON>();
	}
	
	/**
	 * Create a JSON object based on a given JSON string
	 */
	public JSON(String jsonString) {
		
		init(jsonString);
	}
	
	/**
	* Create a JSON object based on an integer value
	*/
	public JSON(Integer intValue) {
	
		kind = JSONkind.NUMBER;
		
		simpleContents = intValue;
	}
	
	/**
	* Create a JSON object based on a double value
	*/
	public JSON(Double doubleValue) {
	
		kind = JSONkind.NUMBER;
		
		simpleContents = doubleValue;
	}
	
	/**
	* Create a JSON object based on a boolean value
	*/
	public JSON(Boolean boolValue) {
	
		kind = JSONkind.NUMBER;
		
		simpleContents = boolValue;
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
	 * Initialize this JSON object based on a given JSON string
	 * and return the rest that is left over of the string after
	 * initialization
	 * E.g. {"foo": [1, true, {"bar": 9}]} would give:
	 * init({"foo": [1, true, {"bar": 9}]}) returns "",
	 * which calls
	 * init([1, true, {"bar": 9}]) returns "",
	 * which calls
	 * init(1, true, {"bar": 9}) returns true, {"bar": 9},
	 * init(true, {"bar": 9}) returns {"bar": 9},
	 * init({"bar": 9}) returns ""
	 * which calls
	 * init(9) returns "",
	 * 
	 * @param jsonString
	 */
	private String init(String jsonString) {
		
		jsonString = jsonString.trim();

		if (jsonString.equals("")) {
			
			kind = JSONkind.NULL;
			
			simpleContents = null;
			
			return "";
		}
		
		if (jsonString.startsWith("{")) {

			objContents = new TreeMap<String, JSON>();
			
			kind = JSONkind.OBJECT;
			
			jsonString = jsonString.substring(1).trim();
			
			while (jsonString.startsWith("\"")) {
				jsonString = jsonString.substring(1).trim();
				int endIndex = jsonString.indexOf("\"");
				String key = jsonString.substring(0, endIndex);
				jsonString = jsonString.substring(endIndex);
				jsonString = jsonString.substring(jsonString.indexOf(":") + 1);
				JSON value = new JSON();
				jsonString = value.init(jsonString).trim();
				objContents.put(key, value);
				if (jsonString.startsWith(",")) {
					jsonString = jsonString.substring(1).trim();
				}
			}
			
			jsonString = jsonString.trim();
			
			if (jsonString.startsWith("}")) {
				jsonString = jsonString.substring(1).trim();
			}
			
			return jsonString;
		}
		
		if (jsonString.startsWith("[")) {
			
			kind = JSONkind.ARRAY;
			
			arrContents = new ArrayList<JSON>();
			
			jsonString = jsonString.substring(1);
			
			while (jsonString.length() > 0) {
				JSON newJSONelement = new JSON();
				jsonString = newJSONelement.init(jsonString).trim();
				arrContents.add(newJSONelement);
				
				while (jsonString.startsWith(",")) {
					jsonString = jsonString.substring(1).trim();
				}
				
				if (jsonString.startsWith("]")) {
					return jsonString.substring(1);
				}
			}
			
			return jsonString;
		}
		
		if (jsonString.startsWith("\"")) {
			kind = JSONkind.STRING;
			
			jsonString = jsonString.substring(1);
			
			// TODO :: also allow escaping " (basically, by checking here is simpleContents
			// ends with \ - in which case we add the " instead and carry on searching forward)
			simpleContents = jsonString.substring(0, jsonString.indexOf("\""));
			jsonString = jsonString.substring(jsonString.indexOf("\"") + 1);
		
			return jsonString;
		}
		
		if (jsonString.startsWith("true")) {
			kind = JSONkind.BOOLEAN;
			
			simpleContents = true;
			
			return jsonString.substring(4);
		}
		
		if (jsonString.startsWith("false")) {
			kind = JSONkind.BOOLEAN;
			
			simpleContents = false;
			
			return jsonString.substring(4);
		}
		
		if (jsonString.startsWith("null")) {
			kind = JSONkind.NULL;
			
			simpleContents = null;
			
			return jsonString.substring(4);
		}

		kind = JSONkind.NUMBER;

		String numStr = "";
		
		while (jsonString.length() > 0) {
			
			Character curChar = jsonString.charAt(0);
			
			if (Character.isDigit(curChar) || curChar.equals('.') || curChar.equals('-')) {
				
				numStr += curChar;

				jsonString = jsonString.substring(1);
			
			} else {
				
				// we are not reading any further numerical digits - escape!
				break;
			}
		}
		
		if (numStr.contains(".")) {
			
			// create a double

			simpleContents = Double.valueOf(numStr);
			
		} else {
			
			// create an integer
			
			simpleContents = Integer.valueOf(numStr);
		}

		return jsonString;
	}

	@Override
	public String toString() {
		
		return toString(null);
	}

	/**
	 * Stores this JSON object in a string
	 * @param compressed  whether to store this object compressed (true, default) or
	 *                    uncompressed (false) - in which case it will be easier to
	 *                    read by humans, but take up more space
	 */
	public String toString(Boolean compressed) {
		
		if (compressed == null) {
			compressed = true;
		}
		
		return toString(compressed, "");
	}
	
	private String toString(boolean compressed, String linePrefix) {
		
		switch (kind) {

			case STRING:
				return "\"" + simpleContents.toString() + "\"";
				
			case BOOLEAN:
			case NUMBER:
				return simpleContents.toString();
			
			case ARRAY:
				StringBuilder arrResult = new StringBuilder();
				
				arrResult.append("[");
				
				if (!compressed) {
					arrResult.append("\n" + linePrefix + "\t");
				}

				boolean arrFirstEntry = true;
				
				for (JSON item : arrContents) {

					if (arrFirstEntry) {
						arrFirstEntry = false;
					} else {
						arrResult.append(",");
						if (!compressed) {
							arrResult.append("\n" + linePrefix + "\t");
						}
					}
					
					arrResult.append(item.toString(compressed, linePrefix + "\t"));
				}

				if (!compressed) {
					arrResult.append("\n" + linePrefix);
				}
				arrResult.append("]");
				
				return arrResult.toString();
				
			case OBJECT:
				StringBuilder objResult = new StringBuilder();
				
				objResult.append("{");

				if (!compressed) {
					objResult.append("\n" + linePrefix + "\t");
				}

				boolean objFirstEntry = true;
				
				for (Map.Entry<String, JSON> entry : objContents.entrySet()) {
					
					if (objFirstEntry) {
						objFirstEntry = false;
					} else {
						objResult.append(",");
						if (!compressed) {
							objResult.append("\n" + linePrefix + "\t");
						}
					}
					
				    String key = entry.getKey();
				    JSON content = entry.getValue();
				    
				    objResult.append("\"");
				    objResult.append(key);
				    objResult.append("\": ");
				    objResult.append(content.toString(compressed, linePrefix + "\t"));
				}

				if (!compressed) {
					objResult.append("\n" + linePrefix);
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
		if (objContents == null) {
			return null;
		}
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
	 * Get a list of JSON values corresponding to a JSON array
	 * stored in a particular key of a JSON object
	 * @param key the key to search for
	 * @return the JSON array stored in the key
	 */
	public List<JSON> getArray(String key) {
		
		JSON result = get(key);
		
		return result.arrContents;
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
	 * Gets an integer value stored in a key of a JSON object
	 * @param key  the key to be searched for
	 * @return the integer value stored in the key
	 */
	public Integer getInteger(String key) {

		JSON result = get(key);
		
		if (result == null) {
			return null;
		}

		if (result.kind == JSONkind.NUMBER) {
			return (Integer) result.simpleContents;
		}

		if (result.kind == JSONkind.BOOLEAN) {
			if ((Boolean) result.simpleContents) {
				return 1;
			} else {
				return 0;
			}
		}

		if (result.kind == JSONkind.NULL) {
			return null;
		}
		
		return 0;
	}

	/**
	 * Sets a key of the JSON object to the JSON value
	 * @param key
	 * @param value
	 */
	public void set(String key, JSON value) {
		kind = JSONkind.OBJECT;
		if (objContents == null) {
			objContents = new TreeMap<String, JSON>();
		}
		objContents.put(key.toString(), value);
	}

	/**
	 * Sets a key to the string value
	 * @param key
	 * @param value
	 */
	public void setString(String key, String value) {

		JSON jsonValue = new JSON("\"" + escapeJSONstr(value) + "\"");
		
		set(key, jsonValue);
	}
	
	/**
	 * Takes in a string such as:
	 * foo"bar
	 * Returns a string such as:
	 * foo\"bar
	 * @param str  a string that possibly contains " signs
	 * @return a string in which every " sign is escaped
	 */
	private String escapeJSONstr(String str) {
		
		// TODO
		
		return str;
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

}
