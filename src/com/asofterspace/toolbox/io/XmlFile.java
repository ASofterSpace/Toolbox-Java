package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;


/**
 * An xml file object describes a single xml file and enables simple access to
 * its contents.
 */
public class XmlFile extends File {

	protected Document xmlcontents = null;

	protected XmlMode mode = XmlMode.NONE_LOADED;


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

	/**
	 * By default, the DOM stuff in Java does NOT intern strings that are often found again and again, leading to
	 * insanely large memory requirements... however, with this helper method we can intern at least some often-
	 * used strings in the XML, and this means that we can e.g. convert GAIA merged (which is 191 MB XML) from
	 * one version to the next using no more than 1 GB heap (such that this even works with 32-Bit JAVA)
	 * TODO :: in the foreseeable future, write our own, even-much-better XML parser :D
	 */
	private void internAllStrings(Element curNode) {

		if (!curNode.getNodeName().contains(":")) {
			getDocument().renameNode(curNode, null, curNode.getNodeName().intern());
		}

		NamedNodeMap attrs = curNode.getAttributes();

		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				curNode.setAttribute(attrs.item(i).getNodeName().intern(), attrs.item(i).getNodeValue().intern());
			}
		}

		NodeList children = curNode.getChildNodes();

		if (children == null) {
			return;
		}

		int childrenLen = children.getLength();

		for (int j = 0; j < childrenLen; j++) {
			Node childNode = children.item(j);
			if (childNode instanceof Element) {
				internAllStrings((Element) childNode);
			}
		}
	}

	private void loadXmlContents() {

		try {

			// let's try to load the regular XML contents...

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

			xmlcontents = documentBuilder.parse(this.getJavaFile());

			xmlcontents.getDocumentElement().normalize();

			mode = XmlMode.XML_LOADED;

			internAllStrings(xmlcontents.getDocumentElement());

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

	public Element createElement(String name) {

		if (xmlcontents == null) {
			loadXmlContents();
		}

		if (xmlcontents == null) {
			return null;
		}

		return xmlcontents.createElement(name);
	}

	public Document getDocument() {

		if (xmlcontents == null) {
			loadXmlContents();
		}

		return xmlcontents;
	}

	public Element getRoot() {

		if (xmlcontents == null) {
			loadXmlContents();
		}

		if (xmlcontents == null) {
			return null;
		}

		return xmlcontents.getDocumentElement();
	}

	public XmlMode getMode() {

		if (xmlcontents == null) {
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

	private boolean domIsTagPrefixInUseForSubTree(String prefix, Node subTreeRoot) {

		if (subTreeRoot.getNodeName().startsWith(prefix)) {
			return true;
		}

		NodeList children = subTreeRoot.getChildNodes();

		if (children == null) {
			return false;
		}

		int childrenLen = children.getLength();

		for (int i = 0; i < childrenLen; i++) {

			Node childNode = children.item(i);

			// ignore children that are not full elements
			if (childNode instanceof Element) {
				if (domIsTagPrefixInUseForSubTree(prefix, childNode)) {
					return true;
				}
			}
		}

		return false;
	}

	public List<Element> domGetElems(String tagName) {

		List<Element> result = new ArrayList<Element>();

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					result.add((Element) elemNode);
				}
			}
		}

		return result;
	}

	public List<Element> domGetElems(String tagName, String hasAttributeName, String hasAttributeValue) {

		List<Element> result = new ArrayList<Element>();

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Node elemAttr = elemNode.getAttributes().getNamedItem(hasAttributeName);
					if (elemAttr != null) {
						if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
							result.add((Element) elemNode);
						}
					}
				}
			}
		}

		return result;
	}

	public List<Element> domGetChildrenOfElems(String tagName) {

		List<Element> result = new ArrayList<Element>();

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				NodeList children = elemNode.getChildNodes();
				if (children == null) {
					break;
				}
				int childrenLen = children.getLength();

				for (int j = 0; j < childrenLen; j++) {
					Node childNode = children.item(j);
					if (childNode instanceof Element) {
						result.add((Element) childNode);
					}
				}
			}
		}

		return result;
	}

	public List<Element> domGetChildrenOfElems(String tagName, String childName) {

		List<Element> result = new ArrayList<Element>();

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				NodeList children = elemNode.getChildNodes();
				if (children == null) {
					break;
				}
				int childrenLen = children.getLength();

				for (int j = 0; j < childrenLen; j++) {
					Node childNode = children.item(j);
					if (childNode instanceof Element) {
						if (childName.equals(childNode.getNodeName())) {
							result.add((Element) childNode);
						}
					}
				}
			}
		}

		return result;
	}

	public List<Element> domGetChildrenOfElems(String tagName, String hasAttributeName, String hasAttributeValue) {

		List<Element> result = new ArrayList<Element>();

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elem = elems.item(i);
				Node elemAttr = elem.getAttributes().getNamedItem(hasAttributeName);
				if (elemAttr != null) {
					if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
						NodeList children = elem.getChildNodes();
						if (children == null) {
							break;
						}
						int childrenLen = children.getLength();

						for (int j = 0; j < childrenLen; j++) {
							Node childNode = children.item(j);
							if (childNode instanceof Element) {
								result.add((Element) childNode);
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Assuming that we have <element attrOrChildName="_bla"/> or
	 * <element><attrOrChildName href="_bla"/></element>, this function
	 * returns _bla (or null if it finds neither)
	 */
	public String domGetLinkFromAttrOrChild(Node element, String attrOrChildName) {

		Node elAttr = element.getAttributes().getNamedItem(attrOrChildName);

		if (elAttr != null) {
			return elAttr.getNodeValue();
		}

		// if we did not find an attrOrChildName as attribute, maybe we can find one as child?
		NodeList children = element.getChildNodes();
		if (children != null) {
			int childrenLen = children.getLength();

			for (int j = 0; j < childrenLen; j++) {
				Node childNode = children.item(j);
				if (attrOrChildName.equals(childNode.getNodeName())) {
					Node href = childNode.getAttributes().getNamedItem("href");

					if (href != null) {
						return href.getNodeValue();
					}
				}
			}
		}

		return null;
	}

	public void domSetAttributeForElems(String tagName, String setAttributeName, String setAttributeValue) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					elem.setAttribute(setAttributeName, setAttributeValue);
				}
			}
		}
	}

	public void domSetAttributeForElems(String tagName, String hasAttributeName, String hasAttributeValue, String setAttributeName, String setAttributeValue) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					Node elemAttr = elem.getAttributes().getNamedItem(hasAttributeName);
					if (elemAttr != null) {
						if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
							elem.setAttribute(setAttributeName, setAttributeValue);
						}
					}
				}
			}
		}
	}

	public void domSetAttributeForElemsIfAttrIsMissing(String tagName, String setAttributeName, String setAttributeValue) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					Node elemAttr = elem.getAttributes().getNamedItem(setAttributeName);
					if (elemAttr == null) {
						elem.setAttribute(setAttributeName, setAttributeValue);
					}
				}
			}
		}
	}

	public void domSetAttributeForElemsIfAttrIsMissing(String tagName, String hasAttributeName, String hasAttributeValue, String setAttributeName, String setAttributeValue) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					Node elemAttr = elem.getAttributes().getNamedItem(hasAttributeName);
					if (elemAttr != null) {
						if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
							elemAttr = elem.getAttributes().getNamedItem(setAttributeName);
							if (elemAttr == null) {
								elem.setAttribute(setAttributeName, setAttributeValue);
							}
						}
					}
				}
			}
		}
	}

	public void domSetAttributeForNonHrefElemsIfAttrIsMissing(String tagName, String setAttributeName, String setAttributeValue) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					Node hrefAttr = elem.getAttributes().getNamedItem("href");
					if (hrefAttr == null) {
						Node elemAttr = elem.getAttributes().getNamedItem(setAttributeName);
						if (elemAttr == null) {
							elem.setAttribute(setAttributeName, setAttributeValue);
						}
					}
				}
			}
		}
	}

	public void domSetAttributeForNonHrefElemsIfAttrIsMissing(String tagName, String hasAttributeName, String hasAttributeValue, String setAttributeName, String setAttributeValue) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					Node hrefAttr = elem.getAttributes().getNamedItem("href");
					if (hrefAttr == null) {
						Node elemAttr = elem.getAttributes().getNamedItem(hasAttributeName);
						if (elemAttr != null) {
							if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
								elemAttr = elem.getAttributes().getNamedItem(setAttributeName);
								if (elemAttr == null) {
									elem.setAttribute(setAttributeName, setAttributeValue);
								}
							}
						}
					}
				}
			}
		}
	}

	public void domRemoveAttributeFromElems(String tagName, String removeAttributeName) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					elem.removeAttribute(removeAttributeName);
				}
			}
		}
	}

	public void domRemoveAttributeFromElems(String tagName, String hasAttributeName, String hasAttributeValue, String removeAttributeName) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					Node elemAttr = elem.getAttributes().getNamedItem(hasAttributeName);
					if (elemAttr != null) {
						if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
							elem.removeAttribute(removeAttributeName);
						}
					}
				}
			}
		}
	}

	/**
	 * This is a quite optimized way for removing children of elemens... please make sure that
	 * you never remove children inside removed children, as this function will not perform any
	 * checks against that!
	 */
	public void domRemoveChildrenFromElems(String tagName, String removeChildName) {

		// search for the children first, and then for each one check if the parents are correct...
		NodeList elems = getDocument().getElementsByTagName(removeChildName);

		if (elems != null) {
			int len = elems.getLength();

			// create a buffer of elements, speeding this function call up from 6 minutes to 1 second
			List<Element> elemBuffer = new ArrayList<>(len);

			for (int i = 0; i < len; i++) {

				Node elem = elems.item(i);

				if (elem instanceof Element) {
					elemBuffer.add((Element) elem);
				}
			}

			// use the buffer to actually remove the elements in question
			for (Element elem : elemBuffer) {

				Node parent = elem.getParentNode();

				if (parent == null) {
					continue;
				}

				if (tagName.equals(parent.getNodeName())) {
					parent.removeChild(elem);
				}
			}
		}
	}

	/**
	 * This is a quite optimized way for removing children of elemens... please make sure that
	 * you never remove children inside removed children, as this function will not perform any
	 * checks against that!
	 */
	public void domRemoveChildrenFromElems(String tagName, String hasAttributeName, String hasAttributeValue, String removeChildName) {

		// search for the children first, and then for each one check if the parents are correct...
		NodeList elems = getDocument().getElementsByTagName(removeChildName);

		if (elems != null) {
			int len = elems.getLength();

			// create a buffer of elements, speeding this function call up from 6 minutes to 1 second
			List<Element> elemBuffer = new ArrayList<>(len);

			for (int i = 0; i < len; i++) {

				Node elem = elems.item(i);

				if (elem instanceof Element) {
					elemBuffer.add((Element) elem);
				}
			}

			// use the buffer to actually remove the elements in question
			for (Element elem : elemBuffer) {

				Node parent = elem.getParentNode();

				if (parent == null) {
					continue;
				}

				if (tagName.equals(parent.getNodeName())) {
					Node parentAttr = parent.getAttributes().getNamedItem(hasAttributeName);
					if (parentAttr != null) {
						if (hasAttributeValue.equals(parentAttr.getNodeValue())) {
							parent.removeChild(elem);
						}
					}
				}
			}
		}
	}

	public void domRenameElems(String fromTagName, String toTagName) {

		NodeList elems = getDocument().getElementsByTagName(fromTagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				getDocument().renameNode(elemNode, null, toTagName);
			}
		}
	}

	public void domRenameElems(String fromTagName, String hasAttributeName, String hasAttributeValue, String toTagName) {

		NodeList elems = getDocument().getElementsByTagName(fromTagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				Node elemAttr = elemNode.getAttributes().getNamedItem(hasAttributeName);
				if (elemAttr != null) {
					if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
						getDocument().renameNode(elemNode, null, toTagName);
					}
				}
			}
		}
	}

	public void domRenameChildrenOfElems(String tagName, String fromChildName, String toChildName) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elem = elems.item(i);
				NodeList children = elem.getChildNodes();
				if (children == null) {
					break;
				}
				int childrenLen = children.getLength();

				for (int j = childrenLen - 1; j >= 0; j--) {
					Node childNode = children.item(j);
					if (fromChildName.equals(childNode.getNodeName())) {
						getDocument().renameNode(childNode, null, toChildName);
					}
				}

			}
		}
	}

	public void domRenameChildrenOfElems(String tagName, String hasAttributeName, String hasAttributeValue, String fromChildName, String toChildName) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elem = elems.item(i);
				Node elemAttr = elem.getAttributes().getNamedItem(hasAttributeName);
				if (elemAttr != null) {
					if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
						NodeList children = elem.getChildNodes();
						if (children == null) {
							break;
						}
						int childrenLen = children.getLength();

						for (int j = childrenLen - 1; j >= 0; j--) {
							Node childNode = children.item(j);
							if (fromChildName.equals(childNode.getNodeName())) {
								getDocument().renameNode(childNode, null, toChildName);
							}
						}
					}
				}

			}
		}
	}

	public void domRenameAttributes(String tagName, String fromAttributeName, String toAttributeName) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					Node origAttr = elem.getAttributes().getNamedItem(fromAttributeName);
					if (origAttr != null) {
						elem.setAttribute(toAttributeName, origAttr.getNodeValue());
						elem.removeAttribute(fromAttributeName);
					}
				}
			}
		}
	}

	public void domRenameAttributes(String tagName, String hasAttributeName, String hasAttributeValue, String fromAttributeName, String toAttributeName) {

		NodeList elems = getDocument().getElementsByTagName(tagName);
		if (elems != null) {
			int len = elems.getLength();

			for (int i = 0; i < len; i++) {
				Node elemNode = elems.item(i);
				if (elemNode instanceof Element) {
					Element elem = (Element) elemNode;
					Node elemAttr = elem.getAttributes().getNamedItem(hasAttributeName);
					if (elemAttr != null) {
						if (hasAttributeValue.equals(elemAttr.getNodeValue())) {
							Node origAttr = elem.getAttributes().getNamedItem(fromAttributeName);
							if (origAttr != null) {
								elem.setAttribute(toAttributeName, origAttr.getNodeValue());
								elem.removeAttribute(fromAttributeName);
							}
						}
					}
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

		// create parent directories
		getJavaFile().getParentFile().mkdirs();

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
			transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			Result output = new StreamResult(getJavaFile());
			xmlcontents.setXmlStandalone(true);
			Source input = new DOMSource(xmlcontents);

			transformer.transform(input, output);

		} catch (TransformerException e) {
			System.err.println(e);
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
