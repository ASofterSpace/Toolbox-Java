/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.io.RecordKind;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class JSON extends Record {

	/**
	 * Create an empty JSON object
	 */
	public JSON() {

		super();
	}

	/**
	 * Create a JSON object based on another generic record
	 */
	public JSON(Record other) {

		super(other);
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

			kind = RecordKind.NULL;

			simpleContents = null;

			return "";
		}

		if (jsonString.startsWith("{")) {

			objContents = new TreeMap<String, Record>();

			kind = RecordKind.OBJECT;

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

			kind = RecordKind.ARRAY;

			arrContents = new ArrayList<Record>();

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

			kind = RecordKind.STRING;

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
			kind = RecordKind.BOOLEAN;

			simpleContents = true;

			return jsonString.substring(4);
		}

		if (jsonString.startsWith("false")) {
			kind = RecordKind.BOOLEAN;

			simpleContents = false;

			return jsonString.substring(5);
		}

		if (jsonString.startsWith("null")) {
			kind = RecordKind.NULL;

			simpleContents = null;

			return jsonString.substring(4);
		}

		kind = RecordKind.NUMBER;

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
	protected String toString(Record item, boolean compressed, String linePrefix) {

		switch (item.kind) {

			case STRING:
				return "\"" + escapeJSONstr(item.simpleContents.toString()) + "\"";

			case BOOLEAN:
			case NUMBER:
				return item.simpleContents.toString();

			case ARRAY:
				StringBuilder arrResult = new StringBuilder();

				arrResult.append("[");

				if (!compressed) {
					arrResult.append("\n" + linePrefix + "\t");
				}

				boolean arrFirstEntry = true;

				for (Record arrItem : item.arrContents) {

					if (arrFirstEntry) {
						arrFirstEntry = false;
					} else {
						arrResult.append(",");
						if (!compressed) {
							arrResult.append("\n" + linePrefix + "\t");
						}
					}

					arrResult.append(toString(arrItem, compressed, linePrefix + "\t"));
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

				for (Map.Entry<String, Record> entry : item.objContents.entrySet()) {

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
					Record content = entry.getValue();

					objResult.append("\"");
					objResult.append(key);
					objResult.append("\": ");
					objResult.append(toString(content, compressed, linePrefix + "\t"));
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

}
