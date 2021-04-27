/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;


public class XmlParser {

	protected XmlElement rootElement = null;

	protected XmlElement currentElement = null;


	/**
	 * The source can be a Java File object or an InputStream
	 */
	public static XmlElement parseXml(Object source) {

		XmlParser parser = new XmlParser();

		return parser.doParseXml(source);
	}

	private XmlElement doParseXml(Object source) {

		rootElement = null;
		currentElement = null;

		if (source == null) {
			return rootElement;
		}

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			SAXParser parser = factory.newSAXParser();

			XmlHandler handler = new XmlHandler();

			if (source instanceof java.io.File) {

				parser.parse((java.io.File) source, handler);

			} else if (source instanceof InputStream) {

				parser.parse((InputStream) source, handler);

			} else if (source instanceof String) {

				byte[] sourceByteArr = ((String) source).getBytes(StandardCharsets.UTF_8);

				InputStream sourceStream = new ByteArrayInputStream(sourceByteArr);

				parser.parse(sourceStream, handler);

			} else {

				System.err.println("Could not parse XML as the source was neither a File nor an InputStream!");
			}

		} catch (SAXException | ParserConfigurationException | IOException e) {
			e.printStackTrace(System.err);
		}

		return rootElement;
	}

	private class XmlHandler extends DefaultHandler {

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			XmlElement newElement = new XmlElement(qName, attributes);
			if (currentElement == null) {
				rootElement = newElement;
			} else {
				currentElement.addChild(newElement);
			}
			currentElement = newElement;
		}

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			currentElement.appendInnerText(new String(ch, start, length));
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			currentElement = currentElement.getXmlParent();
		}
	}

}
