package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

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

	public Node getRoot() {

		if (xmlcontents == null) {
			loadXmlContents();
		}

		return xmlcontents.getDocumentElement();
	}

	public XmlMode getMode() {
	
		if (xmlcontents == null) {
			loadXmlContents();
		}

		return mode;
	}

	public void save() {

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
			transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			Result output = new StreamResult(this.getJavaFile());
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
