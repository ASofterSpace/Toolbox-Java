/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;


/**
 * A class that can prettify the result of calling toString in EGS-CC
 */
public class EgsccPrettifier {

	public static String prettify(String str) {

		if (str == null) {
			return "null";
		}

		str += "   ";

		StringBuilder result = new StringBuilder();

		int indent = 0;

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == ']') {
				indent -= 2;
				result.append("\n");
				for (int j = 0; j < indent; j++) {
					result.append(" ");
				}
				result.append("]");
				if (str.charAt(i+1) == ',') {
					result.append(",");
					i++;
				}
				if (str.charAt(i+1) == ' ') {
					i++;
				}
				if ((str.charAt(i+1) == '}') || (str.charAt(i+1) == ']')) {
					continue;
				}
				c = '\n';
			}
			if (c == '}') {
				indent -= 2;
				result.append("\n");
				for (int j = 0; j < indent; j++) {
					result.append(" ");
				}
				result.append("}");
				if (str.charAt(i+1) == ',') {
					result.append(",");
					i++;
				}
				if (str.charAt(i+1) == ' ') {
					i++;
				}
				if ((str.charAt(i+1) == '}') || (str.charAt(i+1) == ']')) {
					continue;
				}
				c = '\n';
			}
			if (c == '=') {
				result.append(" ");
			}
			result.append(c);
			if (c == '=') {
				result.append(" ");
			}
			if (c == '[') {
				indent += 2;
				if (str.charAt(i+1) == ']') {
					result.append("]");
					indent -= 2;
					i++;
					if (str.charAt(i+1) == ',') {
						result.append(",");
						i++;
					}
					if (str.charAt(i+1) == ' ') {
						i++;
					}
				}
				result.append("\n");
				c = '\n';
			}
			if (c == '{') {
				indent += 2;
				if (str.charAt(i+1) == '}') {
					result.append("}");
					indent -= 2;
					i++;
					if (str.charAt(i+1) == ',') {
						result.append(",");
						i++;
					}
					if (str.charAt(i+1) == ' ') {
						i++;
					}
				}
				result.append("\n");
				c = '\n';
			}
			if (c == ',') {
				if (str.charAt(i+1) == ' ') {
					i++;
				}
				result.append("\n");
				c = '\n';
			}
			if (c == '\n') {
				for (int j = 0; j < indent; j++) {
					result.append(" ");
				}
			}
		}

		return result.toString();
	}

}
