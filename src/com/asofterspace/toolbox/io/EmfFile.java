package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * An emf file object describes a single emf xml or binary file and enables
 * simple access to its contents. (YES, an object of this class understands
 * both emf binary files - using hardcoded magick directly in here - AND xml
 * files - as it inherits from XmlFile - so if you are unsure if you want to
 * open an emf binary or regular xml file, just use this class here and do
 * not worry at all! ^^)
 */
public class EmfFile extends XmlFile {

	/**
	 * You can construct a EmfFile instance by directly from a path name.
	 */
	public EmfFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct an EmfFile instance by basing it on an existing file object.
	 */
	public EmfFile(File regularFile) {

		super(regularFile);
	}

	protected void loadXmlContents() {

		loadEmfContents();

		// if the EMF loading did not work ...
		if (!XmlMode.EMF_LOADED.equals(mode)) {

			// ... then try loading XML instead!
			super.loadXmlContents();
		}
	}

	protected void loadEmfContents() {

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
				//	 a structure
				//	 the feature ID of the structure
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
						// try loading XML instead, as this is not an EMF binary file
						mode = XmlMode.NONE_LOADED;
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

	/*
	TODO :: we want to create an overloaded saveTo also such that we can save
	EMF binary... but so far, we are a long way from it!
	@Overload
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
	*/

	/**
	 * Gives back a string representation of the emf file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.EmfFile: " + filename + " (root element: " + this.getRoot().getNodeName() + ")";
	}

}
