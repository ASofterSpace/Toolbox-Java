/**
 * Unlicensed code created by A Softer Space, 2019
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


public class XML extends Record {

	private String name;

	private TinyXmlMap attributes;


	/**
	 * Create an empty XML object
	 */
	public XML() {

		super();
	}

	/**
	 * Create a XML object based on another generic record
	 */
	public XML(Record other) {

		super(other);
	}

	/**
	 * Create a XML object based on an XmlElement, which we
	 * are actually using internally
	 */
	public XML(XmlElement other) {

		this.name = other.getTagName();

		this.attributes = other.getAttributes();

		// XML is RecordKind.OBJECT if it has differently named children (with their names being the object keys)
		// XML is RecordKind.ARRAY if it has (at least some) samely named children
		// XML is RecordKind.STRING if it has no children (with innerText being the String)

		List<XmlElement> xmlChildren = other.getChildNodes();

		if (xmlChildren.size() < 1) {
			kind = RecordKind.STRING;
			simpleContents = other.getInnerText();
		} else {
			kind = RecordKind.OBJECT;
			objContents = new TreeMap<String, Record>();

			for (XmlElement child : xmlChildren) {
				if (objContents.containsKey(child.getTagName())) {
					kind = RecordKind.ARRAY;
					arrContents = new ArrayList<Record>();
					break;
				}
				objContents.put(child.getTagName(), null);
			}

			if (kind == RecordKind.OBJECT) {
				for (XmlElement child : xmlChildren) {
					XML childRecord = new XML(child);
					objContents.put(child.getTagName(), childRecord);
				}
			} else {
				for (XmlElement child : xmlChildren) {
					XML childRecord = new XML(child);
					arrContents.add(childRecord);
				}
			}
		}
	}

	/**
	 * Create a XML object based on a given XML string
	 */
	/**
	TODO
	public XML(String xmlString) {

		init(xmlString);
	}
	*/

	public String getName() {

		return name;
	}

	public TinyXmlMap getAttributes() {

		if (attributes == null) {
			return new TinyXmlMap();
		}

		return attributes;
	}

	@Override
	protected String toString(Record item, boolean compressed, String linePrefix) {

		// TODO :: creating a whole xml element just to convert to string is
		// a huge overhead, right? maybe write a dedicated toString-function
		// here?

		XmlElement result = new XmlElement(this);

		return result.toString();
	}

	/**
	 * Takes in a string such as:
	 * foo<bar
	 * Returns a string such as:
	 * foo&lt;bar
	 * @param str  a string that possibly contains <, >, etc. signs
	 * @return a string in which all such signs are escaped
	 */
	public static String escapeXMLstr(Object strToEscape) {

		if (strToEscape == null) {
			return "";
		}

		String text = strToEscape.toString();

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {

			char c = text.charAt(i);

			switch (c) {
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '\n':
					result.append("&#10;");
					break;
				case '\r':
					break;
				case '\t':
					result.append("&#9;");
					break;
				case '&':
					result.append("&amp;");
					break;
				case '\'':
					result.append("&apos;");
					break;
				case '\"':
					result.append("&quot;");
					break;
			default:
				if (c > 0x7e) {
					result.append("&#");
					result.append((int) c);
					result.append(";");
				} else {
					result.append(c);
				}
			}
		}

		return result.toString();
	}

}
