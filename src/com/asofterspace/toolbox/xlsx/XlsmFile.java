/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.xlsx;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.io.XmlFile;

import java.util.List;


/**
 * An xlsm file object describes a single xlsm file (macro-enabled xlsx file)
 * and enables simple access to its contents.
 */
public class XlsmFile extends XlsxFile {

	/**
	 * You can construct a XlsmFile instance by directly from a path name.
	 */
	public XlsmFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct an XlsmFile instance by basing it on an existing file object.
	 */
	public XlsmFile(File regularFile) {

		super(regularFile);
	}

	/**
	 * You can construct an XlsmFile instance by basing it on a directory and a filename.
	 */
	public XlsmFile(Directory parentDirectory, String filename) {

		super(parentDirectory, filename);
	}

	/**
	 * This is a macro-enabled XLSX file, so let's actually add some macros. ;)
	 */
	public void addMacro(File macroBinFile) {

		// add macro to [Content_Types].xml
		XmlFile contentTypes = getContentTypes();

		List<XmlElement> types = contentTypes.domGetElems("Types");

		if (types.size() > 0) {
			XmlElement override = types.get(0).createChild("Override");

			override.setAttribute("PartName", "/xl/" + macroBinFile.getLocalFilename());
			override.setAttribute("ContentType", "application/vnd.ms-office.vbaProject");
		}

		// add macro to /xl/_rels/workbook.xml.rels
		XmlFile workbookRels = getWorkbookRels();

		List<XmlElement> relationships = workbookRels.domGetElems("Relationships");

		if (relationships.size() > 0) {
			int highestUnfoundId = 1;

			List<XmlElement> findIds = workbookRels.domGetElems("Relationship");
			for (XmlElement findId : findIds) {
				String foundIdFull = findId.getAttribute("Id");
				if (foundIdFull == null) {
					continue;
				}
				String foundId = foundIdFull.substring(3);
				try {
					int foundIdInt = Integer.parseInt(foundId);
					if (foundIdInt >= highestUnfoundId) {
						highestUnfoundId = foundIdInt + 1;
					}
				} catch (NumberFormatException e) {
					// do not increase the unfound id in case of exceptions...
				}
			}

			XmlElement relationship = relationships.get(0).createChild("Relationship");
			relationship.setAttribute("Id", "rId" + highestUnfoundId);
			relationship.setAttribute("Type", "http://schemas.microsoft.com/office/2006/relationships/vbaProject");
			relationship.setAttribute("Target",  macroBinFile.getLocalFilename());
		}

		// add actual macro itself
		addZippedFile(macroBinFile, "xl/");
	}

	/**
	 * Gives back a string representation of the xlsm file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.XlsmFile: " + filename;
	}

}
