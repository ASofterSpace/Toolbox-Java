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
 * An xlsx file object describes a single xlsx file and enables simple access to
 * its contents.
 */
public class XlsxFile extends ZipFile {

	private List<XlsxSheet> xlsxSheets;


	/**
	 * You can construct a XlsxFile instance by directly from a path name.
	 */
	public XlsxFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct an XlsxFile instance by basing it on an existing file object.
	 */
	public XlsxFile(File regularFile) {

		super(regularFile);
	}
	
	/**
	 * For an XLSX file, call getSheets() to get access to its contents,
	 * not getContents() / setContents() as for a regular File (the
	 * regular File-based stuff will work, technically, but will be
	 * much less efficient and if you use both all hell might break
	 * loose... so yeah, only use the getSheets() function as entry-
	 * point for XLSX files, kthxbye!)
	 */
	public List<XlsxSheet> getSheets() {
	
		if (xlsxSheets == null) {
			loadXlsxContents();
		}

		return xlsxSheets;
	}

	protected void loadXlsxContents() {

		xlsxSheets = new ArrayList<>();
		
		XmlFile workbook = getZippedFile("xl/workbook.xml").getUnzippedFileAsXml();
		List<XmlElement> sheets = workbook.getRoot().getElementsByTagNameHierarchy("workbook", "sheets", "sheet");
		
		XmlFile workbookRels = getZippedFile("xl/_rels/workbook.xml.rels").getUnzippedFileAsXml();
		List<XmlElement> sheetRels = workbookRels.getRoot().getElementsByTagNameHierarchy("Relationships", "Relationship");

		for (XmlElement sheet : sheets) {
		
			String title = sheet.getAttribute("name");
			String rId = sheet.getAttribute("r:id");
			// sheets also have a sheetId, but we have not figured out what that does, so we are just ignoring it for now... =)
			
			if (rId == null) {
				continue;
			}
			
			for (XmlElement sheetRel : sheetRels) {
			
				if (rId.equals(sheetRel.getAttribute("Id"))) {

					XmlFile sheetXml = getZippedFile("xl/" + sheetRel.getAttribute("Target")).getUnzippedFileAsXml();
					
					xlsxSheets.add(new XlsxSheet(title, sheetXml));
			
					break;
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

		// TODO :: zip up the files again, be happy...
	}

	/**
	 * Gives back a string representation of the xlsx file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.XlsxFile: " + filename;
	}

}
