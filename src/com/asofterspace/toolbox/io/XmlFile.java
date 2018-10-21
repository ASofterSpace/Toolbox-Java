package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;


/**
 * An xml file object describes a single xml file and enables simple access to
 * its contents.
 */
public class XmlFile extends File {

	protected XmlMode mode = XmlMode.NONE_LOADED;

	protected XmlElement rootElement = null;
	
	protected XmlElement currentElement = null;


	/**
	 * You can construct a XmlFile instance by directly from a path name.
	 */
	public XmlFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct an XmlFile instance by basing it on an existing file object.
	 */
	public XmlFile(File regularFile) {

		super(regularFile);
	}
	
	/**
	 * For an XML file, call getRoot() to get access to its contents,
	 * not getContents() / setContents() as for a regular File (the
	 * regular File-based stuff will work, technically, but will be
	 * much less efficient and if you use both all hell might break
	 * loose... so yeah, only use the getRoot() function as entry-
	 * point for XML files, kthxbye!)
	 */
	public XmlElement getRoot() {

		if (rootElement == null) {
			loadXmlContents();
		}

		return rootElement;
	}

	public XmlMode getMode() {

		if (rootElement == null) {
			loadXmlContents();
		}

		return mode;
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
			currentElement.setInnerText(new String(ch, start, length));
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			currentElement = currentElement.getXmlParent();
		}
	}
	
	protected void loadXmlContents() {

		parseXml(getJavaFile());
	}
	
	/**
	 * The source can be a Java File object or an InputStream
	 */
	protected void parseXml(Object source) {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			SAXParser parser = factory.newSAXParser();

			rootElement = null;
			currentElement = null;
			
			XmlHandler handler = new XmlHandler();
			
			if (source instanceof java.io.File) {
			
				parser.parse((java.io.File) source, handler);
			
			} else if (source instanceof InputStream) {
			
				parser.parse((InputStream) source, handler);
			
			} else {
				System.err.println("Could not parse XML as the source was neither a File nor an InputStream!");
			}
			
			mode = XmlMode.XML_LOADED;

		} catch (SAXException | ParserConfigurationException | IOException e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Checks if there are any elements in the current DOM whose tag starts with the given prefix;
	 * returns true if such elements can be found and false otherwise
	 */
	public boolean domIsTagPrefixInUse(String prefix) {

		return domIsTagPrefixInUseForSubTree(prefix, getRoot());
	}

	private boolean domIsTagPrefixInUseForSubTree(String prefix, XmlElement subTreeRoot) {

		if (subTreeRoot.getNodeName().startsWith(prefix)) {
			return true;
		}

		List<XmlElement> children = subTreeRoot.getChildNodes();

		for (XmlElement child : children) {
			if (domIsTagPrefixInUseForSubTree(prefix, child)) {
				return true;
			}
		}

		return false;
	}

	public List<XmlElement> domGetElems(String tagName) {

		return getRoot().getElementsByTagName(tagName);
	}

	public List<XmlElement> domGetElems(String tagName, String hasAttributeName, String hasAttributeValue) {

		List<XmlElement> result = new ArrayList<>();

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		for (XmlElement elem : elems) {
			String elemAttr = elem.getAttribute(hasAttributeName);
			if (elemAttr != null) {
				if (hasAttributeValue.equals(elemAttr)) {
					result.add(elem);
				}
			}
		}

		return result;
	}

	public List<XmlElement> domGetChildrenOfElems(String tagName) {

		List<XmlElement> result = new ArrayList<XmlElement>();

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		for (XmlElement elem : elems) {
			result.addAll(elem.getChildNodes());
		}

		return result;
	}

	public List<XmlElement> domGetChildrenOfElems(String tagName, String childName) {

		List<XmlElement> result = new ArrayList<XmlElement>();

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		for (XmlElement elem : elems) {
			for (XmlElement child : elem.getChildNodes()) {
				if (childName.equals(child.getNodeName())) {
					result.add(child);
				}
			}
		}

		return result;
	}

	// here we are matching children of (elements with tagName and given attribute),
	// NOT (children with given attribute) of elements with tagName
	public List<XmlElement> domGetChildrenOfElems(String tagName, String hasAttributeName, String hasAttributeValue) {

		List<XmlElement> result = new ArrayList<XmlElement>();

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		for (XmlElement elem : elems) {
			if (hasAttributeValue.equals(elem.getAttribute(hasAttributeName))) {
				result.addAll(elem.getChildNodes());
			}
		}
		
		return result;
	}

	public void domSetAttributeForElems(String tagName, String setAttributeName, String setAttributeValue) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			elem.setAttribute(setAttributeName, setAttributeValue);
		}
	}
	
	public void domSetAttributeForElems(String tagName, String hasAttributeName, String hasAttributeValue, String setAttributeName, String setAttributeValue) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			if (hasAttributeValue.equals(elem.getAttribute(hasAttributeName))) {
				elem.setAttribute(setAttributeName, setAttributeValue);
			}
		}
	}

	public void domSetAttributeForElems(String[] tagNames, String setAttributeName, String setAttributeValue) {

		List<XmlElement> elems = getRoot().getElementsByTagNames(tagNames);
		
		for (XmlElement elem : elems) {
			elem.setAttribute(setAttributeName, setAttributeValue);
		}
	}
	
	public void domSetAttributeForElems(String[] tagNames, String hasAttributeName, String hasAttributeValue, String setAttributeName, String setAttributeValue) {

		List<XmlElement> elems = getRoot().getElementsByTagNames(tagNames);
		
		for (XmlElement elem : elems) {
			if (hasAttributeValue.equals(elem.getAttribute(hasAttributeName))) {
				elem.setAttribute(setAttributeName, setAttributeValue);
			}
		}
	}

	public void domSetAttributeForElemsIfAttrIsMissing(String tagName, String setAttributeName, String setAttributeValue) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			if (elem.getAttribute(setAttributeName) == null) {
				elem.setAttribute(setAttributeName, setAttributeValue);
			}
		}
	}

	public void domSetAttributeForElemsIfAttrIsMissing(String tagName, String hasAttributeName, String hasAttributeValue, String setAttributeName, String setAttributeValue) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			if (hasAttributeValue.equals(elem.getAttribute(hasAttributeName))) {
				if (elem.getAttribute(setAttributeName) == null) {
					elem.setAttribute(setAttributeName, setAttributeValue);
				}
			}
		}
	}

	public void domSetAttributeForNonHrefElemsIfAttrIsMissing(String tagName, String setAttributeName, String setAttributeValue) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			if (elem.getAttribute("href") == null) {
				if (elem.getAttribute(setAttributeName) == null) {
					elem.setAttribute(setAttributeName, setAttributeValue);
				}
			}
		}
	}

	public void domSetAttributeForNonHrefElemsIfAttrIsMissing(String tagName, String hasAttributeName, String hasAttributeValue, String setAttributeName, String setAttributeValue) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			if (hasAttributeValue.equals(elem.getAttribute(hasAttributeName))) {
				if (elem.getAttribute("href") == null) {
					if (elem.getAttribute(setAttributeName) == null) {
						elem.setAttribute(setAttributeName, setAttributeValue);
					}
				}
			}
		}
	}

	public void domRemoveAttributeFromElems(String tagName, String removeAttributeName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			elem.removeAttribute(removeAttributeName);
		}
	}

	public void domRemoveAttributeFromElems(String tagName, String hasAttributeName, String hasAttributeValue, String removeAttributeName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			if (hasAttributeValue.equals(elem.getAttribute(hasAttributeName))) {
				elem.removeAttribute(removeAttributeName);
			}
		}
	}

	/**
	 * This is a quite optimized way for removing children of elemens... please make sure that
	 * you never remove children inside removed children, as this function will not perform any
	 * checks against that!
	 */
	public void domRemoveChildrenFromElems(String tagName, String removeChildName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(removeChildName);
		
		for (XmlElement elem : elems) {
			XmlElement parent = elem.getXmlParent();

			if (parent == null) {
				continue;
			}

			if (tagName.equals(parent.getNodeName())) {
				parent.removeChild(elem);
			}
		}
	}

	/**
	 * This is a quite optimized way for removing children of elemens... please make sure that
	 * you never remove children inside removed children, as this function will not perform any
	 * checks against that!
	 */
	public void domRemoveChildrenFromElems(String tagName, String hasAttributeName, String hasAttributeValue, String removeChildName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(removeChildName);
		
		for (XmlElement elem : elems) {
			XmlElement parent = elem.getXmlParent();

			if (parent == null) {
				continue;
			}

			if (tagName.equals(parent.getNodeName())) {
				if (hasAttributeValue.equals(parent.getAttribute(hasAttributeName))) {
					parent.removeChild(elem);
				}
			}
		}
	}

	public void domRenameElems(String fromTagName, String toTagName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(fromTagName);
		
		for (XmlElement elem : elems) {
			elem.setNodeName(toTagName);
		}
	}

	public void domRenameElems(String fromTagName, String hasAttributeName, String hasAttributeValue, String toTagName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(fromTagName);
		
		for (XmlElement elem : elems) {
			if (hasAttributeValue.equals(elem.getAttribute(hasAttributeName))) {
				elem.setNodeName(toTagName);
			}
		}
	}

	public void domRenameChildrenOfElems(String tagName, String fromChildName, String toChildName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(fromChildName);
		
		for (XmlElement elem : elems) {
			XmlElement parent = elem.getXmlParent();

			if (parent == null) {
				continue;
			}

			if (tagName.equals(parent.getNodeName())) {
				elem.setNodeName(toChildName);
			}
		}
	}

	public void domRenameChildrenOfElems(String tagName, String hasAttributeName, String hasAttributeValue, String fromChildName, String toChildName) {
	
		List<XmlElement> elems = getRoot().getElementsByTagName(fromChildName);
		
		for (XmlElement elem : elems) {
			XmlElement parent = elem.getXmlParent();

			if (parent == null) {
				continue;
			}

			if (tagName.equals(parent.getNodeName())) {
				if (hasAttributeValue.equals(parent.getAttribute(hasAttributeName))) {
					elem.setNodeName(toChildName);
				}
			}
		}
	}

	public void domRenameAttributes(String tagName, String fromAttributeName, String toAttributeName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			String attrVal = elem.getAttribute(fromAttributeName);
			if (attrVal != null) {
				elem.removeAttribute(fromAttributeName);
				elem.setAttribute(toAttributeName, attrVal);
			}
		}
	}

	public void domRenameAttributes(String tagName, String hasAttributeName, String hasAttributeValue, String fromAttributeName, String toAttributeName) {

		List<XmlElement> elems = getRoot().getElementsByTagName(tagName);
		
		for (XmlElement elem : elems) {
			if (hasAttributeValue.equals(elem.getAttribute(hasAttributeName))) {
				String attrVal = elem.getAttribute(fromAttributeName);
				if (attrVal != null) {
					elem.removeAttribute(fromAttributeName);
					elem.setAttribute(toAttributeName, attrVal);
				}
			}
		}
	}

	public void print() {

		try (OutputStreamWriter printout = new OutputStreamWriter(System.out)) {

			printout.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

			getRoot().writeToFile(printout);

			printout.flush();

		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to print the file " + getFilename() + " - inconceivable!");
		}
	}

	public void save() {

		saveTo(this);
	}

	public void saveTo(File newLocation) {

		saveTo(newLocation.filename);
	}

	public void saveTo(String newLocation) {

		filename = newLocation;
		
		java.io.File javaFile = getJavaFile();

		// create parent directories
		javaFile.getParentFile().mkdirs();

		// get the root element before creating the writer (in case of this being the first time that the XML content
		// is loaded in some sort of automated setting)
		XmlElement root = getRoot();

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(javaFile), StandardCharsets.UTF_8)) {
			
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			
			if (root != null) {
				root.writeToFile(writer);
			}

			writer.flush();
			
		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to write to the file " + getFilename() + " - inconceivable!");
		}
	}

	/**
	 * Gives back a string representation of the xml file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.XmlFile: " + filename + " (root element: " + this.getRoot().getNodeName() + ")";
	}

}
