/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * An emf file object describes a single emf xml or binary file and enables
 * simple access to its contents. (YES, an object of this class understands
 * both emf binary files - using hardcoded magick directly in here - AND xml
 * files - as it inherits from XmlFile - so if you are unsure if you want to
 * open an emf binary or regular xml file, just use this class here and do
 * not worry at all! ^^)
 */
public class EmfFile extends XmlFile {

	// the binary content of the EMF file
	byte[] binaryContent;

	// the current position inside binaryContent
	int i;


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

		// if this file does not seem to be EMF, so nothing has been loaded yet...
		if (XmlMode.NONE_LOADED.equals(mode)) {

			// ... then try loading XML instead!
			super.loadXmlContents();
		}
		// (btw. - this is DIFFERENT from the case of having seen this as valid EMF,
		// but not having understood it on our side - in that case, the mode is
		// EMF_UNSUPPORTED, and we do NOT want to parse as plain XML then, as we
		// know it is EMF and we are just too silly to read it!)
	}

	protected void loadEmfContents() {

		// in general, an EMF binary file consists of the following parts:
		// Signature
		// Metadata
		// eObjects and their attributes
		// id block containing the UUIDs

		rootElement = null;
		XmlElement currentElement = null;

		TinyXmlMap curAttributes = new TinyXmlMap();

		try {
			binaryContent = Files.readAllBytes(Paths.get(this.filename));
			int len = binaryContent.length;

			boolean debug = false; // TODO :: remove debug switch

			// we first have the signature (8 bytes) ...
			// (ensure that we are starting with ‰emf.... - the exact signature that NEEDS to be intact for this to be valid)
			if ((len < 8) ||
				(binaryContent[0] != (byte)0x89) ||
				(binaryContent[1] != (byte)0x65) ||
				(binaryContent[2] != (byte)0x6d) ||
				(binaryContent[3] != (byte)0x66) ||
				(binaryContent[4] != (byte)0x0a) ||
				(binaryContent[5] != (byte)0x0d) ||
				(binaryContent[6] != (byte)0x1a) ||
				(binaryContent[7] != (byte)0x0a)) {
				// try loading XML instead, as this is not an EMF binary file
				mode = XmlMode.NONE_LOADED;
				return;
			}

			byte nextElementId = 0;

			i = 8;

			// ... following the signature, we have:
			// the version (1 byte)
			// then the style (4 bytes) if version is above 0 (nothing otherwise)
			// then the amount of EObjects (compressed integer - between 1 and 4 bytes)

			// all CDM files we have seen so far were using version 0
			if (binaryContent[i] != (byte)0x00) {
				// however, for version 1, we know that an additional integer should follow describing
				// the styles that are being used... (buuut we ignore those styles? at least this
				// code here looks like we do xD - oh, and no one thought about versions above 1, should they ever exist!)
				i = i + 4;
				System.out.println("The binary CDM file you are attempting to load is using a newer version than I ever encountered... trying to get it to work anyway...");
			}

			i++;

			// here, we should have a compressed integer telling us the amount of eObjects,
			// but in the examples we looked at this number here was not reliable at all
			// and e.g. 02 in case of one or three eObjects in the file... meh...
			// (however, we read it out anyway, as it is a compressed integer and we
			// need to jump over the right amount of bytes!)
			int somenumber = readCompressedInteger();

			i++;

			// we get the id of the NEXT element - so the current id plus one...
			// (is this a compressed integer?)
			nextElementId = (byte)(binaryContent[i] + 1);

			i++;

			// no idea what this number means either (usually it is 01, it MIGHT
			// be the depth of the root element and therefore always 01 as the
			// root element is always at depth 1...)

			i++;

			// to read out the eObjectAmount, we go into the id block and read out the last id
			// (each id is reported as number + byte 18 + underscore + 22 ecore UUID letters)
			// TODO :: what if there is not a single id in the id block (as e.g. the file is
			// empty except for the signature)?
			// TODO :: what if there are more than 255 ids inside this file (then reading one
			// byte will definitely stop working!)
			int eObjectAmount = binaryContent[len - 25];

			// this is the length, in bytes, of the block describing the various ids
			// it is the last block in the file, but to make everything easier for us,
			// we want to read it out already NOW! :D
			// TODO :: again, what if there are none / more than 255 ids?
			// (the 2 here comes because the id block starts with 01xx where xx is the
			// eObjectAmount, again, and then these two bytes are followed by 25 bytes
			// for each eObject, as described in the comment block just above this one)
			int idBlockLength = 2 + 25 * eObjectAmount;

			// we reduce the length, as we do not want to read into the id block
			int idBlockOffset = len - idBlockLength;

			String[] eObjectIds = new String[eObjectAmount];
			byte[] bytesOfId = new byte[23];

			for (int eNumber = 0; eNumber < eObjectAmount; eNumber++) {
				System.arraycopy(binaryContent, idBlockOffset + 2 + (eNumber*25) + 2, bytesOfId, 0, 23);
				String eObjectId = new String(bytesOfId, StandardCharsets.UTF_8);
				eObjectIds[eNumber] = eObjectId;
			}

			// we reduce the length, as we do not want to read into the id block
			len -= idBlockLength;

			boolean inToken = false;
			StringBuilder tokenBuilder = new StringBuilder();
			String element = null;
			String xmlns = null;
			String containedNamespace = null;
			String curAttrKey = null;
			boolean wroteElementStart = false;
			boolean beforeEquals = true;
			byte xmlnsId = 0;
			int tokenLength = 0;
			int tokenCurPos = 0;
			int curEObjectNum = 0;

			// ... finally come the EObjects themselves, with each EObject being:
			//   its ID (compressed int)
			//   its feature ID (compressed int, but -1 from the value in the file)
			//   and, until the feature ID that is read out is -1:
			//	 a structure
			//	 the feature ID of the structure
			// however, in the beginning we first have the xmlns namespace, then the
			// actual element name (however, expressed as namespace), followed by
			// the namespace prefix for everything that is following / contained!
			for (; i < len; i++) {

				byte cur = binaryContent[i];

				// if (inToken) {
				if (debug) {
					System.out.println("tokenCurPos: " + tokenCurPos);
					System.out.println("tokenLength: " + tokenLength);
					System.out.println("cur: " + cur);
				}

				if (tokenCurPos == 0) {
					// read a compressed integer as length, whoop whoop! we can do it!
					tokenLength = readCompressedInteger();
				} else {
					// TODO :: do not iterate over every character, but instead do an array copy
					// and create a new String based on the array? (on the other hand, internally
					// it will have to iterate still... meh... maybe time it, see if that improves
					// anything or not)
					if (cur == (byte)0x18) {
						tokenBuilder.append('#');
					} else {
						tokenBuilder.append((char)cur);
					}
				}

				tokenCurPos++;

				if (tokenLength == tokenCurPos) {
					inToken = false;
					if (xmlns == null) {
						xmlns = tokenBuilder.toString();
						// System.out.println("found xmlns: " + xmlns);
						i++;
						xmlnsId = binaryContent[i];
					} else {
						if (element == null) {
							element = tokenBuilder.toString();
							// System.out.println("found element: " + element);
						} else {
							if (containedNamespace == null) {
								containedNamespace = tokenBuilder.toString();
								containedNamespace = containedNamespace.replaceAll("/", ".");
								// System.out.println("found contained namespace: " + containedNamespace);
								// no idea what the next byte means
								i++;
							} else {
								if (wroteElementStart) {
									if (beforeEquals) {
										if ((binaryContent[i+1] == nextElementId) && (binaryContent[i+2] == nextElementId)) {
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
											curAttrKey = tokenBuilder.toString();
										}

										// TODO :: also some others are links...
										// like baseElement? and subElements is even a list of links?
										if ("definition".equals(curAttrKey)) {
											// TODO :: think about case of link to other resource
											// TODO :: think about case above id 255
											i++;
											int linkedEObject = binaryContent[i];
											curAttributes.put(curAttrKey, eObjectIds[linkedEObject-1]);
											//i++;

											// keep beforeEquals true (and as it is switched in a few lines,
											// assign the opposite for now)
											beforeEquals = false;
										}
									} else {
										// just jump over this one (it is an increasing number?)
										i++;
										curAttributes.put(curAttrKey, tokenBuilder.toString());
									}
									beforeEquals = !beforeEquals;
								} else {
									i++;
									wroteElementStart = true;
									beforeEquals = true;

									curAttributes = new TinyXmlMap();
									XmlElement newEl = new XmlElement(namespaceAndTagToQName(element, tokenBuilder.toString()), curAttributes);
									if (rootElement == null) {
										rootElement = newEl;
										curAttributes.put("xmi:version", "2.0");
										curAttributes.put("xmlns:xmi", "http://www.omg.org/XMI");
										curAttributes.put("xmlns:" + namespaceToElement(xmlns), xmlns);
									} else {
										currentElement.addChild(newEl);
									}
									currentElement = newEl;

									// assign one of the IDs that we previously read out
									curAttributes.put("xmi:id", eObjectIds[curEObjectNum]);

									curEObjectNum++;
								}
							}
						}
					}
					tokenCurPos = 0;
					tokenLength = 0;
					tokenBuilder = new StringBuilder();
				}
			}

			if (wroteElementStart && beforeEquals && (tokenLength == 0) && (tokenCurPos == 0)) {
				// looks like we understood this fully, whoop whoop!
				mode = XmlMode.EMF_LOADED;
			} else {
				// looks like we got caught right in the middle of interpreting stuff by the end of the file...
				// meaning that probably we understood nothing, oops!
				mode = XmlMode.EMF_UNSUPPORTED;
			}

		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			System.out.println(e);

			mode = XmlMode.EMF_UNSUPPORTED;
		}
	}

	// TODO :: improve this function - we think it can go up to 4 bytes? (currently it only works for one, two or three bytes)
	private int readCompressedInteger() {

		int intCur = binaryContent[i];

		// if we have a byte below 0x40, then that is just the actual integer - very short, very simple!
		if (intCur < 64) {
			return intCur;
		}

		if (intCur < 128) {
			// if we have a byte at 0x40, then that means the next byte contains the actual integer (pretty pointless,
			//   but needed to "escape" the following integer)
			// if we have a byte at 0x41, then that means the next byte contains the actual integer, but add 256!
			//   etc.
			i++;
			return (int)binaryContent[i] + (256 * (intCur - 64));
		}

		// if we have a byte at 0x80, then that means the same as for 0x40, just with two bytes following
		i = i+2;
		return (int)binaryContent[i] + (256 * (int)binaryContent[i-1]) + (256*256 * (intCur - 128));
	}

	private String namespaceAndTagToQName(String namespace, String tag) {

		// some elements are actually encountered without namespace
		// TODO :: is this always true? does it depend on the context?
		// (enough if it is true for CDM files, not for EMF in general...)
		switch (tag) {
			case "Script":
				return "script";
		}

		// for CIs, this is what we do:
		return namespaceToElement(namespace) + ":" + tag;
	}

	/**
	 * This function converts a namespace to an actual element tag;
	 * this is part of the Ecore configuration that a real EMF parser
	 * would have; in our case, we have no configuration, but if you
	 * are using this EmfFile class anywhere, extend it and overwrite
	 * this method with namespaces that you actually know about!
	 */
	protected String namespaceToElement(String namespace) {
		return "unknown";
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

			XmlElement root = getRoot();

			if (root != null) {
				root.writeToFile(writer);
			}

			writer.flush();

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

		String rootName = "(none)";

		XmlElement root = this.getRoot();

		if (root != null) {
			rootName = root.getTagName();
		}

		return "com.asofterspace.toolbox.io.EmfFile: " + filename + " (root element: " + rootName + ")";
	}

}
