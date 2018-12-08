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

	private XmlFile contentTypes;
	private XmlFile workbook;
	private XmlFile workbookRels;
	private XmlFile sharedStrings;
		

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
		
		contentTypes = getZippedFile("[Content_Types].xml").getUnzippedFileAsXml();
		
		workbook = getZippedFile("xl/workbook.xml").getUnzippedFileAsXml();
		List<XmlElement> sheets = workbook.getRoot().getElementsByTagNameHierarchy("workbook", "sheets", "sheet");
		
		workbookRels = getZippedFile("xl/_rels/workbook.xml.rels").getUnzippedFileAsXml();
		List<XmlElement> sheetRels = workbookRels.getRoot().getElementsByTagNameHierarchy("Relationships", "Relationship");

		sharedStrings = getZippedFile("xl/sharedStrings.xml").getUnzippedFileAsXml();

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
					
					xlsxSheets.add(new XlsxSheet(title, sheetXml, this));
			
					break;
				}
			}
		}
	}
	
	XmlFile getContentTypes() {
	
		if (contentTypes == null) {
			loadXlsxContents();
		}

		return contentTypes;
	}
	
	XmlFile getWorkbookRels() {
	
		if (workbookRels == null) {
			loadXlsxContents();
		}
	
		return workbookRels;
	}
	
	XmlFile getSharedStrings() {

		if (sharedStrings == null) {
			loadXlsxContents();
		}

		return sharedStrings;
	}

	public void saveTo(String newLocation) {
	
		if (contentTypes != null) {
			contentTypes.save();
		}
		
		if (workbook != null) {
			workbook.save();
		}
		
		if (workbookRels != null) {
			workbookRels.save();
		}
		
		if (sharedStrings != null) {
			sharedStrings.save();
		}
		
		if (xlsxSheets != null) {
			for (XlsxSheet xlsxSheet : xlsxSheets) {
				xlsxSheet.save();
			}
		}
		
		super.saveTo(newLocation);
	}
	
	/**
	 * To convert, create a new Xlsm file based on this object here... and then actually add whatever is necessary
	 * to make it a full, regular Xlsm file!
	 */
	public XlsmFile convertToXlsm() {
	
		// create an XlsmFile naively based on this XlsxFile
		XlsmFile result = new XlsmFile(this);
		
		// actually make the necessary changes to make it a proper XlsmFile
		XmlFile xlsmContentTypes = result.getContentTypes();
		
		// get all Override elements with PartName="/xl/workbook.xml"
		List<XmlElement> workbookOverrides = xlsmContentTypes.domGetElems("Override", "PartName", "/xl/workbook.xml");
		
		// set the content type, which before was "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"
		for (XmlElement workbookOverride : workbookOverrides) {
			workbookOverride.setAttribute("ContentType", "application/vnd.ms-excel.sheet.macroEnabled.main+xml");
		}
		
		// ... aaand save our miraculous changes!
		xlsmContentTypes.save();

		return result;
	}
	
	/**
	 * Gives back a string representation of the xlsx file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.XlsxFile: " + filename;
	}

}
