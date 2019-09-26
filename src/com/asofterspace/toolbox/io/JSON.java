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
	private void init(String jsonString) {
		init(jsonString, 0);
	}

	private int init(String jsonString, int pos) {

		pos = jumpOverWhitespaces(jsonString, pos);

		if (pos >= jsonString.length()) {

			kind = RecordKind.NULL;

			simpleContents = null;

			return pos;
		}

		if (jsonString.charAt(pos) == '{') {

			objContents = new TreeMap<String, Record>();

			kind = RecordKind.OBJECT;

			pos++;

			while (true) {
				pos = jumpOverWhitespaces(jsonString, pos);

				if (pos >= jsonString.length()) {
					break;
				}

				if (jsonString.charAt(pos) == '}') {
					pos++;
					break;
				}

				// this is "foo": "bar" (here being in the "foo" part)
				// this should be the case - keys should be inside quote marks
				if (jsonString.charAt(pos) == '"') {
					int endIndex = jsonString.indexOf("\"", pos+1);
					String key = jsonString.substring(pos+1, endIndex).trim();
					pos = jsonString.indexOf(":", endIndex+1) + 1;
					JSON value = new JSON();
					pos = value.init(jsonString, pos);
					objContents.put(key, value);
					pos = jumpOverWhitespacesAndCommas(jsonString, pos);
					continue;
				}

				// this is foo: "bar", or 'foo': "bar"
				// this should NOT be the case - the key is not in a quote mark!
				// but we will grudgingly accept it anyway, as we are nice people...
				int endIndex = jsonString.indexOf(":", pos);
				String key = jsonString.substring(pos, endIndex).trim();
				// in case someone did escape the key, but with ' instead of ", also handle that gracefully...
				if ((key.length() > 2) && (key.charAt(0) == '\'') && (key.charAt(key.length()-1) == '\'')) {
					key = key.substring(1, key.length() - 1);
				}
				JSON value = new JSON();
				pos = value.init(jsonString, endIndex + 1);
				objContents.put(key, value);
				pos = jumpOverWhitespacesAndCommas(jsonString, pos);
			}

			return pos;
		}

		if (jsonString.charAt(pos) == '[') {

			kind = RecordKind.ARRAY;

			arrContents = new ArrayList<Record>();

			pos++;

			pos = jumpOverWhitespaces(jsonString, pos);

			while (pos < jsonString.length()) {

				if (jsonString.charAt(pos) == ']') {
					return pos + 1;
				}

				JSON newJSONelement = new JSON();
				pos = newJSONelement.init(jsonString, pos);
				arrContents.add(newJSONelement);
				pos = jumpOverWhitespacesAndCommas(jsonString, pos);
			}

			return pos;
		}

		String doStringWith = null;

		// this is "foo": "bar" (here being in the "bar" part)
		if (jsonString.charAt(pos) == '"') {
			doStringWith = "\"";
		}

		// this is "foo": 'bar', which is WRONG, but we want to be so generous as to still accept it...
		if (jsonString.charAt(pos) == '\'') {
			doStringWith = "'";
		}

		if (doStringWith != null) {

			kind = RecordKind.STRING;

			pos++;

			int endIndex = jsonString.indexOf(doStringWith, pos);
			String simpleContentsStr = jsonString.substring(pos, endIndex);
			// also allow escaping " (basically, by checking here is simpleContentsStr
			// ends with \ - in which case we add the " instead and carry on searching forward)
			while (jsonString.charAt(endIndex - 1) == '\\') {
				int newEndIndex = jsonString.indexOf(doStringWith, endIndex + 1);
				simpleContentsStr = simpleContentsStr.substring(0, simpleContentsStr.length() - 1) +
									jsonString.substring(endIndex, newEndIndex);
				endIndex = newEndIndex;
			}
			pos = endIndex + 1;

			// escape front to back - if we find, e.g., \\, this is a slash, but if we find \n, this is a newline
			StringBuilder simpleContentBuilder = new StringBuilder();
			int curPos = 0;
			int nextPos = simpleContentsStr.indexOf("\\", curPos);
			while (nextPos > -1) {
				simpleContentBuilder.append(simpleContentsStr.substring(curPos, nextPos));
				curPos = nextPos + 1;
				if (nextPos+1 >= simpleContentsStr.length()) {
					break;
				}
				curPos++;
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
						curPos--;
				}
				nextPos = simpleContentsStr.indexOf("\\", curPos);
			}
			simpleContentBuilder.append(simpleContentsStr.substring(curPos, simpleContentsStr.length()));

			simpleContents = simpleContentBuilder.toString();

			return pos;
		}

		if (pos + 3 < jsonString.length()) {

			String subStr = jsonString.substring(pos, pos + 4);

			if (subStr.equals("null")) {
				kind = RecordKind.NULL;

				simpleContents = null;

				return pos + 4;
			}

			if (subStr.equals("true")) {
				kind = RecordKind.BOOLEAN;

				simpleContents = true;

				return pos + 4;
			}

			if (pos + 4 < jsonString.length()) {
				if (jsonString.substring(pos, pos + 5).equals("false")) {
					kind = RecordKind.BOOLEAN;

					simpleContents = false;

					return pos + 5;
				}
			}
		}

		kind = RecordKind.NUMBER;

		int charPos = pos;

		while (charPos < jsonString.length()) {

			Character curChar = jsonString.charAt(charPos);

			if (Character.isDigit(curChar) ||
				curChar.equals('.') || curChar.equals('-') ||
				curChar.equals('e') || curChar.equals('E')) {

				charPos++;

			} else {

				// we are not reading any further numerical digits - escape!
				break;
			}
		}

		String numStr = jsonString.substring(pos, charPos);

		pos = charPos;

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

		return pos;
	}

	private int jumpOverWhitespaces(String jsonString, int pos) {
		while (pos < jsonString.length()) {
			char c = jsonString.charAt(pos);
			if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
				pos++;
			} else {
				break;
			}
		}
		return pos;
	}

	private int jumpOverWhitespacesAndCommas(String jsonString, int pos) {
		while (pos < jsonString.length()) {
			char c = jsonString.charAt(pos);
			if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == ',') {
				pos++;
			} else {
				break;
			}
		}
		return pos;
	}

	@Override
	protected String toString(Record item, boolean compressed, String linePrefix) {

		switch (item.kind) {

			case STRING:
				if (item.simpleContents == null) {
					return "null";
				}
				return "\"" + escapeJSONstr(item.simpleContents.toString()) + "\"";

			case BOOLEAN:
			case NUMBER:
				if (item.simpleContents == null) {
					return "null";
				}
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
