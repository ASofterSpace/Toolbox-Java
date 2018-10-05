package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.io.FileOutputStream;
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

	private XmlElement rootElement = null;
	
	private XmlElement currentElement = null;


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

		super();

		regularFile.copyTo(this);
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
	
	private void loadXmlContents() {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			SAXParser parser = factory.newSAXParser();

			currentElement = null;
			
			XmlHandler handler = new XmlHandler();
			parser.parse(getJavaFile(), handler);
			
			mode = XmlMode.XML_LOADED;

		} catch (SAXException | ParserConfigurationException | IOException e) {
			e.printStackTrace(System.out);
		}
	}
	
	/*
	private void loadXmlContents() {

		try {

			// let's try to load the regular XML contents...

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

			xmlcontents = documentBuilder.parse(this.getJavaFile());

			xmlcontents.getDocumentElement().normalize();

			mode = XmlMode.XML_LOADED;

		} catch (Exception xE) {

			// oh no - REGULAR xml, this was not!
			// maybe it is a binary file...
			// we can try to decode EMF binary manually - let's see how well we are doing!

			try {
				byte[] binaryContent = Files.readAllBytes(Paths.get(this.filename));

				StringBuilder cB = new StringBuilder();

				cB.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

				int len = binaryContent.length;
				int eObjectAmount = 0;
				byte attrCounter = 0;
				byte attrLength = 0;
				byte attrCurPos = 0;
				boolean inAttribute = false;
				StringBuilder attrBuilder = new StringBuilder();
				String element = null;
				String xmlns = null;
				String containedNamespace = null;
				boolean wroteElementStart = false;
				boolean beforeEquals = true;
				byte attrAmountTotal = 0;
				byte attrAmountCurrent = 0;
				byte xmlnsId = 0;
				int styleLength = 0;
				byte nextElementId = 0;
				boolean debug = false; // TODO :: remove debug switch

				for (int i = 0; i < len; i++) {

					byte cur = binaryContent[i];

					// we first have the signature (8 bytes)
					// then the version (1 byte)
					// then the style (4 bytes) if version is above 0 (nothing otherwise)
					// then the amount of EObjects (compressed integer - between 1 and 4 bytes)
					// then the EObjects themselves, with each EObject being:
					//   its ID (compressed int)
					//   its feature ID (compressed int, but -1 from the value in the file)
					//   and, until the feature ID that is read out is -1:
					//     a structure
					//     the feature ID of the structure
					// however, in the beginning we first have the xmlns namespace, then the
					// actual element name (however, expressed as namespace), followed by
					// the namespace prefix for everything that is following / contained!

					if (i < 12 + styleLength) {
						// ensure that we are starting with ‰emf.... - the exact signature that NEEDS to be intact for this to be valid
						int j = i + styleLength;
						if (((j == 0) && (cur != (byte)0x89)) ||
							((j == 1) && (cur != (byte)0x65)) ||
							((j == 2) && (cur != (byte)0x6d)) ||
							((j == 3) && (cur != (byte)0x66)) ||
							((j == 4) && (cur != (byte)0x0a)) ||
							((j == 5) && (cur != (byte)0x0d)) ||
							((j == 6) && (cur != (byte)0x1a)) ||
							((j == 7) && (cur != (byte)0x0a))) {
							throw new IOException("The file is neither valid XML nor a valid EMF binary file (the signature is invalid)!");
						}

						// sooo we encountered an EMF file... but that is all, we do not attempt to parse it!
						if (j > 7) {
							mode = XmlMode.EMF_LOADED;
							return;
						}

						switch (j) {
							case 8:
								// all CDM files we have seen so far were using version 0
								if (cur != (byte)0x00) {
									// however, for version 1, we know that an additional integer should follow describing
									// the styles that are being used...
									styleLength = 4;
									i = i + styleLength;
									System.out.println("The binary CDM file you are attempting to load is using a newer version than I ever encountered... trying to get it to work anyway...");
								}
								break;
							case 9:
								// TODO :: actually read a compressed integer, not just a byte and hope for the best!
								eObjectAmount = cur;
								break;
							case 10:
								nextElementId = (byte)(cur + 1);
								break;
						}
					} else {
						// if (inAttribute) {
						if (debug) {
							System.out.println("attrCurPos: " + attrCurPos);
							System.out.println("attrLength: " + attrLength);
							System.out.println("cur: " + cur);
						}

						if (attrCurPos == 0) {
							attrLength = cur;
							// ignore 0x40 (64d) - does this have something to do with compressed integers?
							if (cur == (byte)0x40) {
								continue;
							}
						} else {
							if (cur == (byte)0x18) {
								attrBuilder.append('#');
							} else {
								attrBuilder.append((char)cur);
							}
						}

						attrCurPos++;

						if (attrLength == attrCurPos) {
							inAttribute = false;
							if (xmlns == null) {
								xmlns = attrBuilder.toString();
								System.out.println("found xmlns: " + xmlns);
								i++;
								xmlnsId = binaryContent[i];
							} else {
								if (element == null) {
									element = attrBuilder.toString();
									System.out.println("found element: " + element);
								} else {
									if (containedNamespace == null) {
										containedNamespace = attrBuilder.toString();
										containedNamespace = containedNamespace.replaceAll("/", ".");
										System.out.println("found contained namespace: " + containedNamespace);
										// no idea what the next byte means
										i++;
									} else {
										if (wroteElementStart) {
											if (beforeEquals) {
												if ((binaryContent[i+1] == nextElementId) && (binaryContent[i+2] == nextElementId)) {
													cB.append(">\n");
													beforeEquals = true;
													wroteElementStart = false;
													element = null;
													xmlns = null;
													containedNamespace = null;
													i = i + 2;
													// swallow further nextElementIds (we have observed 28, 22, 222, ...)
													while (binaryContent[i+1] == nextElementId) {
														i++;
													}
													nextElementId++;
												} else {
													cB.append(' ');
													cB.append(attrBuilder);
													cB.append('=');
												}
											} else {
												cB.append('"');
												cB.append(attrBuilder);
												cB.append('"');
												i++;
												cB.append("(" + binaryContent[i] + ")");
											}
											beforeEquals = !beforeEquals;
										} else {
											i++;
											cB.append("<" + namespaceToElement(element) + ":" + attrBuilder.toString() + "(" + binaryContent[i] + ") xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:" + namespaceToElement(xmlns) + "=\"" + xmlns + "(" + xmlnsId + ")\"");
											wroteElementStart = true;
											beforeEquals = true;
										}
									}
								}
							}
							attrCurPos = 0;
							attrLength = 0;
							attrBuilder = new StringBuilder();
						}
					}
				}

				System.out.println("");
				System.out.println(cB.toString());

				mode = XmlMode.EMF_LOADED;

			} catch (IOException bE) {
				System.out.println(bE);

				mode = XmlMode.NONE_LOADED;
			}
		}
	}
	*/

	private String namespaceToElement(String namespace) {

		switch (namespace) {
			case "http://www.scopeset.de/ConfigurationTracking/1.12":
				return "configurationcontrol";
			case "http://www.scopeset.de/MonitoringControl/MonitoringControlModel/1.12":
			case "http://www.scopeset.de/MonitoringControl/1.12":
				return "monitoringControlElement";
			case "http://www.scopeset.de/MonitoringControlImplementation/UserDefinedDisplays/1.12":
				return "userDefinedDisplay";
			case "http://www.scopeset.de/MonitoringControlImplementation/UserDefinedDisplays/Mapping_UDD2MCM/1.12":
				return "udd2mceMapper";
			case "http://www.scopeset.de/core/qudv/conceptualmodel/1.5":
				return "qudv.conceptualmodel_extModel";
			case "http://www.scopeset.de/core/1.5":
				return "xmi";
			case "http://www.esa.int/dme/ConfigurationTracking/1.12.1":
			case "http://www.esa.int/dme/MonitoringControlImplementation/ProcedureScriptSwFunction/1.12.1":
			case "http://www.esa.int/dme/MonitoringControlImplementation/1.12.1":
			case "http://www.scopeset.de/PacketProcessing/1.0.0":
			case "http://www.scopeset.de/MonitoringControlImplementation/ProcedureScriptSwFunction/1.12":
			case "http://www.scopeset.de/MonitoringControlImplementation/Packetization/Packetization/Parameter/1.12":
				return "unknown";
		}
		return "unknown(" + namespace + ")";
	}

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

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(javaFile), StandardCharsets.UTF_8)) {
			
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			
			getRoot().writeToFile(writer);
			
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
