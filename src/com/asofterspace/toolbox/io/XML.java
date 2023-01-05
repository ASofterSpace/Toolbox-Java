/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.RecordKind;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * This is a way of representing XML elements which conforms to the Record API;
 * it can be used for convenience (to get all the power of Records also with XML),
 * but is not as optimized for actually handling XMLs as XmlElement
 * (However, you can just call new XmlElement(xml) to get that one instead)
 */
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
	 * Parse a string containing XML
	 */
	public XML(String xmlString) {

		this(XmlParser.parseXml(xmlString));
	}

	/**
	 * Parse the contents of a file containing XML
	 */
	public XML(java.io.File xmlFile) {

		this(XmlParser.parseXml(xmlFile));
	}

	/**
	 * Parse an inputstream containing XML
	 */
	public XML(InputStream xmlStream) {

		this(XmlParser.parseXml(xmlStream));
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
		return escapeXMLstr(strToEscape, "&#10;");
	}

	public static String escapeXMLstr(Object strToEscape, String escapeNewlinesTo) {
		return escapeXMLstr(strToEscape, escapeNewlinesTo, false);
	}

	public static String escapeXMLstr(Object strToEscape, String escapeNewlinesTo, boolean doHtmlReplacements) {

		if (strToEscape == null) {
			return "";
		}

		String text = strToEscape.toString();

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {

			char c = text.charAt(i);

			if (doHtmlReplacements) {
				boolean found = true;
				switch (c) {
					case 'ä':
						result.append("&auml;");
						break;
					case 'ö':
						result.append("&ouml;");
						break;
					case 'ü':
						result.append("&uuml;");
						break;
					case 'Ä':
						result.append("&Auml;");
						break;
					case 'Ö':
						result.append("&Ouml;");
						break;
					case 'Ü':
						result.append("&Uuml;");
						break;
					case 'æ':
						result.append("&aelig;");
						break;
					case 'Æ':
						result.append("&AElig;");
						break;
					case 'ß':
						result.append("&szlig;");
						break;
					default:
						found = false;
						break;
				}
				if (found) {
					continue;
				}
			}

			switch (c) {
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '\n':
					result.append(escapeNewlinesTo);
					break;
				case '\r':
					break;
				case '\t':
					result.append("&#9;");
					break;
				case '\'':
					result.append("&apos;");
					break;
				case '\"':
					result.append("&quot;");
					break;
				case '&':
					result.append("&amp;");
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

	public static String unescapeXMLstr(String str) {

		str = str.replace("&#10;", "\n");
		str = str.replace("&#xD;&#xA;", "\n");
		str = str.replace("&#xD;", "\n");
		str = str.replace("&#xA;", "\n");
		str = str.replace("&#9;", "\t");
		str = str.replace("&#039;", "'");
		str = str.replace("&#160;", " ");
		str = str.replace("&#8364;", "€");
		str = str.replace("&gt;", ">");
		str = str.replace("&lt;", "<");
		str = str.replace("&apos;", "'");
		str = str.replace("&quot;", "\"");
		str = str.replace("&auml;", "ä");
		str = str.replace("&ouml;", "ö");
		str = str.replace("&uuml;", "ü");
		str = str.replace("&Äuml;", "Ä");
		str = str.replace("&Ouml;", "Ö");
		str = str.replace("&Uuml;", "Ü");
		str = str.replace("&aelig;", "æ");
		str = str.replace("&AElig;", "Æ");
		str = str.replace("&szlig;", "ß");
		str = str.replace("&amp;", "&");

		return str;
	}

	/**
	 * Takes in something like:
	 * Bla! Blubb? <a href="foo.bar">Foo bar</a> and so on!
	 *
	 * Returns something like:
	 * Bla! Blubb? Foo bar and so on!
	 */
	public static String removeXmlTagsFromText(String str) {

		if (str == null) {
			return null;
		}

		StringBuilder result = new StringBuilder();
		int offset = 0;
		int pos = str.indexOf("<", offset);

		while (pos >= 0) {
			result.append(str.substring(offset, pos));
			offset = pos;
			offset = str.indexOf(">", offset);
			if (offset < 0) {
				return result.toString();
			}
			offset++;
			pos = str.indexOf("<", offset);
		}

		result.append(str.substring(offset));

		return result.toString();
	}

}
