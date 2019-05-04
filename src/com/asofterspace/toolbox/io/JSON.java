/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.io.JSONkind;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


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
	* Create a JSON object based on a long value
	*/
	public JSON(Long longValue) {

		kind = JSONkind.NUMBER;

		simpleContents = longValue;
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
	 * Tells us whether this JSON object is empty or not
	 */
	public boolean isEmpty() {

		switch (kind) {

			case STRING:
			case BOOLEAN:
			case NUMBER:
				return "".equals(simpleContents.toString());

			case ARRAY:
				return arrContents.size() < 1;

			case OBJECT:
				return objContents.size() < 1;

			default:
				return true;
		}
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

			jsonString = jsonString.substring(1);

			while (true) {
				jsonString = jsonString.trim();

				if (jsonString.startsWith("}")) {
					jsonString = jsonString.substring(1).trim();
					break;
				}

				// this is "foo": "bar" (here being in the "foo" part)
				// this should be the case - keys should be inside quote marks
				if (jsonString.startsWith("\"")) {
					jsonString = jsonString.substring(1).trim();
					int endIndex = jsonString.indexOf("\"");
					String key = jsonString.substring(0, endIndex);
					jsonString = jsonString.substring(endIndex);
					jsonString = jsonString.substring(jsonString.indexOf(":") + 1);
					JSON value = new JSON();
					jsonString = value.init(jsonString).trim();
					objContents.put(key, value);
					if (jsonString.startsWith(",")) {
						jsonString = jsonString.substring(1);
					}
					continue;
				}

				// this is foo: "bar", or 'foo': "bar"
				// this should NOT be the case - the key is not in a quote mark!
				// but we will grudgingly accept it anyway, as we are nice people...
				int endIndex = jsonString.indexOf(":");
				String key = jsonString.substring(0, endIndex).trim();
				// in case someone did escape the key, but with ' instead of ", also handle that gracefully...
				if ((key.length() > 2) && key.startsWith("'") && key.endsWith("'")) {
					key = key.substring(1, key.length() - 1);
				}
				jsonString = jsonString.substring(endIndex + 1).trim();
				JSON value = new JSON();
				jsonString = value.init(jsonString).trim();
				objContents.put(key, value);
				if (jsonString.startsWith(",")) {
					jsonString = jsonString.substring(1);
				}
			}

			return jsonString;
		}

		if (jsonString.startsWith("[")) {

			kind = JSONkind.ARRAY;

			arrContents = new ArrayList<JSON>();

			jsonString = jsonString.substring(1).trim();

			while (jsonString.length() > 0) {

				if (jsonString.startsWith("]")) {
					return jsonString.substring(1);
				}

				JSON newJSONelement = new JSON();
				jsonString = newJSONelement.init(jsonString).trim();
				arrContents.add(newJSONelement);

				while (jsonString.startsWith(",")) {
					jsonString = jsonString.substring(1).trim();
				}

			}

			return jsonString;
		}

		String doStringWith = null;

		// this is "foo": "bar" (here being in the "bar" part)
		if (jsonString.startsWith("\"")) {
			doStringWith = "\"";
		}

		// this is "foo": 'bar', which is WRONG, but we want to be so generous as to still accept it...
		if (jsonString.startsWith("'")) {
			doStringWith = "'";
		}

		if (doStringWith != null) {

			kind = JSONkind.STRING;

			jsonString = jsonString.substring(1);

			String simpleContentsStr = jsonString.substring(0, jsonString.indexOf(doStringWith));
			jsonString = jsonString.substring(jsonString.indexOf(doStringWith) + 1);

			// also allow escaping " (basically, by checking here is simpleContentsStr
			// ends with \ - in which case we add the " instead and carry on searching forward)
			while (simpleContentsStr.endsWith("\\")) {
				simpleContentsStr = simpleContentsStr.substring(0, simpleContentsStr.length()-1) + doStringWith;
				simpleContentsStr += jsonString.substring(0, jsonString.indexOf(doStringWith));
				jsonString = jsonString.substring(jsonString.indexOf(doStringWith) + 1);
			}

			// escape front to back - if we find, e.g., \\, this is a slash, but if we find \n, this is a newline
			StringBuilder simpleContentBuilder = new StringBuilder();
			int pos = 0;
			int nextPos = simpleContentsStr.indexOf("\\", pos);
			while (nextPos > -1) {
				simpleContentBuilder.append(simpleContentsStr.substring(pos, nextPos));
				pos = nextPos + 1;
				if (nextPos+1 >= simpleContentsStr.length()) {
					break;
				}
				pos++;
				switch (simpleContentsStr.charAt(nextPos+1)) {
					case 'n':
						simpleContentBuilder.append('\n');
						break;
					case 'r':
						simpleContentBuilder.append('\r');
						break;
					case 't':
						simpleContentBuilder.append('\t');
						break;
					case 'b':
						simpleContentBuilder.append('\b');
						break;
					case 'f':
						simpleContentBuilder.append('\f');
						break;
					case '\\':
						simpleContentBuilder.append('\\');
						break;
					default:
						// append the backslash, but do not increase the pos as far as we otherwise would,
						// as we did not actually do anything with the previous character!
						simpleContentBuilder.append('\\');
						pos--;
				}
				nextPos = simpleContentsStr.indexOf("\\", pos);
			}
			simpleContentBuilder.append(simpleContentsStr.substring(pos, simpleContentsStr.length()));

			simpleContents = simpleContentBuilder.toString();

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

			return jsonString.substring(5);
		}

		if (jsonString.startsWith("null")) {
			kind = JSONkind.NULL;

			simpleContents = null;

			return jsonString.substring(4);
		}

		kind = JSONkind.NUMBER;

		String numStr = "";

		int charPos = 0;

		while (charPos < jsonString.length()) {

			Character curChar = jsonString.charAt(charPos);

			if (Character.isDigit(curChar) ||
				curChar.equals('.') || curChar.equals('-') ||
				curChar.equals('e') || curChar.equals('E')) {

				numStr += curChar;

			} else {

				// we are not reading any further numerical digits - escape!
				break;
			}

			charPos++;
		}

		jsonString = jsonString.substring(charPos);

		numStr = numStr.replace(",", "");

		if (numStr.contains(".")) {

			// create a double

			simpleContents = Double.valueOf(numStr);

		} else {

			// create a long

			if (numStr.equals("")) {
				simpleContents = (Long) 0L;
			} else {
				simpleContents = Long.valueOf(numStr);
			}
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
	 *					uncompressed (false) - in which case it will be easier to
	 *					read by humans, but take up more space
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
				return "\"" + escapeJSONstr(simpleContents.toString()) + "\"";

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
						if (compressed) {
							objResult.append(", ");
						} else {
							objResult.append(",\n" + linePrefix + "\t");
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
	 * Converts the JSON object {"foo": "bar", "blubb": "blobb"} to an XmlElement
	 * such as <json><foo>bar</foo><blubb>blobb</blubb></json>
	 * If onlyIncludeFields is left empty, everything is converted; if there is
	 * at least one field given, then ONLY the given fields are converted!
	 */
	public XmlElement toXml(String... onlyIncludeFields) {

		XmlElement result = new XmlElement("json");

		if (objContents == null) {
			return result;
		}

		if (onlyIncludeFields.length == 0) {
			for (String field : objContents.keySet()) {
				// TODO :: if that child has subchildren,
				// handle them correctly and assign as object inside XML :)
				result.createChild(field).setInnerText(getString(field));
			}
		} else {
			for (String field : onlyIncludeFields) {
				// TODO :: if that child has subchildren,
				// handle them correctly and assign as object inside XML :)
				String fieldData = getString(field);
				if (fieldData != null) {
					result.createChild(field).setInnerText(fieldData);
				}
			}
		}

		return result;
	}

	/**
	 * Returns the value of this JSON object as string, without any surrounding " or somesuch;
	 * meant to be used when you know that your JSON object is of the simple type String, and
	 * you want to get that String
	 */
	public String asString() {

		if (simpleContents == null) {
			return null;
		}

		return simpleContents.toString();
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
	 * in a JSON object or to a specific index in a JSON
	 * array
	 * @param key the key to search for
	 * @return the JSON object
	 */
	public JSON get(Object key) {

		if ((key instanceof Integer) && (arrContents != null)) {
			return arrContents.get((Integer) key);
		}

		if (objContents == null) {
			return null;
		}
		return objContents.get(key.toString());
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
	 * Get a list of JSON values corresponding to a JSON array
	 * stored in a particular key of a JSON object
	 * @param key the key to search for
	 * @return the JSON array stored in the key
	 */
	public List<JSON> getArray(Object key) {

		JSON result = get(key);

		if ((result == null) || (result.arrContents == null)) {
			return new ArrayList<>();
		}

		return result.arrContents;
	}

	/**
	 * Get a list of strings corresponding to a JSON array
	 * filled with strings stored in a particular key of a
	 * JSON object (entries that are not strings will be ignored!)
	 * @param key the key to search for
	 * @return the list of strings stored in the array at the key
	 */
	public List<String> getArrayAsStringList(Object key) {

		JSON result = get(key);

		if ((result == null) || (result.arrContents == null)) {
			return new ArrayList<>();
		}

		List<String> resultList = new ArrayList<>();

		for (JSON entry : result.arrContents) {
			if (entry != null) {
				if (entry.kind == JSONkind.STRING) {
					resultList.add(entry.simpleContents.toString());
				}
			}
		}

		return resultList;
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
		JSON result = get(key);

		if (result == null) {
			return null;
		}

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
	 * Gets a boolean value stored in a key of a JSON object
	 * @param key  the key to be searched for
	 * @return the boolean value stored in the key
	 */
	public Boolean getBoolean(Object key) {

		JSON result = get(key);

		if (result == null) {
			return null;
		}

		if (result.kind == JSONkind.BOOLEAN) {
			return (Boolean) result.simpleContents;
		}

		return null;
	}

	/**
	 * Gets an int value stored in a key of a JSON object
	 * @param key  the key to be searched for
	 * @return the integer value stored in the key
	 */
	public Integer getInteger(Object key) {

		JSON result = get(key);

		if (result == null) {
			return null;
		}

		if (result.kind == JSONkind.NUMBER) {
			if (result.simpleContents instanceof Long) {
				return (Integer) (int) (long) ((Long) result.simpleContents);
			}
			if (result.simpleContents instanceof Integer) {
				return (Integer) result.simpleContents;
			}
			if (result.simpleContents instanceof Double) {
				return (Integer) (int) Math.round((Double) result.simpleContents);
			}
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
	 * Gets an long value stored in a key of a JSON object
	 * @param key  the key to be searched for
	 * @return the integer value stored in the key
	 */
	public Long getLong(Object key) {

		JSON result = get(key);

		if (result == null) {
			return null;
		}

		if (result.kind == JSONkind.NUMBER) {
			if (result.simpleContents instanceof Long) {
				return (Long) result.simpleContents;
			}
			if (result.simpleContents instanceof Integer) {
				return (Long) (long) (int) (Integer) result.simpleContents;
			}
			if (result.simpleContents instanceof Double) {
				return (Long) Math.round((Double) result.simpleContents);
			}
		}

		if (result.kind == JSONkind.BOOLEAN) {
			if ((Boolean) result.simpleContents) {
				return 1L;
			} else {
				return 0L;
			}
		}

		if (result.kind == JSONkind.NULL) {
			return null;
		}

		return 0L;
	}

	/**
	 * Gets an double value stored in a key of a JSON object
	 * @param key  the key to be searched for
	 * @return the integer value stored in the key
	 */
	public Double getDouble(Object key) {

		JSON result = get(key);

		if (result == null) {
			return null;
		}

		if (result.kind == JSONkind.NUMBER) {
			if (result.simpleContents instanceof Long) {
				return (Double) (double) (long) (Long) result.simpleContents;
			}
			if (result.simpleContents instanceof Integer) {
				return (Double) (double) (int) (Integer) result.simpleContents;
			}
			if (result.simpleContents instanceof Double) {
				return (Double) result.simpleContents;
			}
		}

		if (result.kind == JSONkind.BOOLEAN) {
			if ((Boolean) result.simpleContents) {
				return 1.0;
			} else {
				return 0.0;
			}
		}

		if (result.kind == JSONkind.NULL) {
			return null;
		}

		return 0.0;
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
	public static String escapeJSONstr(Object strToEscape) {

		if (strToEscape == null) {
			return "";
		}

		String str = strToEscape.toString();

		str = str.replace("\\", "\\\\");
		str = str.replace("\"", "\\\"");
		str = str.replace("\n", "\\n");
		str = str.replace("\r", "\\r");
		str = str.replace("\t", "\\t");
		str = str.replace("\b", "\\b");
		str = str.replace("\f", "\\f");

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
