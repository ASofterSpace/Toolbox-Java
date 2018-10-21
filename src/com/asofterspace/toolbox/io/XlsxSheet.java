package com.asofterspace.toolbox.io;

import java.util.List;


public class XlsxSheet {

	private String title;

	private XmlFile sheetFile;

	private XlsxFile parent;


	public XlsxSheet(String title, XmlFile sheetFile, XlsxFile parent) {

		this.title = title;
		
		this.sheetFile = sheetFile;
		
		this.parent = parent;
	}
	
	public String getCellContent(String cellName) {
	
		List<XmlElement> matchingCells = sheetFile.domGetElems("c", "r", cellName);
		
		for (XmlElement cell : matchingCells) {
			if ("s".equals(cell.getAttribute("t"))) {
				// we have a string... and the strings are kept in a separate string file... so look there!
				String sharedStringIndexStr = cell.getChild("v").getInnerText();
				try {
					int sharedStringIndex = Integer.parseInt(sharedStringIndexStr);

					List<XmlElement> sharedStrings = parent.getSharedStrings().getRoot().getElementsByTagNameHierarchy("sst", "si");

					XmlElement actualStringElement = sharedStrings.get(sharedStringIndex);

					return actualStringElement.getChild("t").getInnerText();

				} catch (NumberFormatException e) {
					// ooops... the string could not be parsed, humm...
				}
			}
			return "unknown";
		}

		return null;
	}

	public void setCellContent(String cellName, String newContent) {
	
		List<XmlElement> matchingCells = sheetFile.domGetElems("c", "r", cellName);
	
		for (XmlElement cell : matchingCells) {
			
			// the cell already exists and we are just editing it in place
			cell.setAttribute("t", "s");
			
			// we just set this to string... and the strings are kept in a separate string file... so put it there!
			String sharedStringIndexStr = cell.getChild("v").getInnerText();
			try {
				int sharedStringIndex = Integer.parseInt(sharedStringIndexStr);

				List<XmlElement> sharedStrings = parent.getSharedStrings().getRoot().getElementsByTagNameHierarchy("sst", "si");

				XmlElement actualStringElement = sharedStrings.get(sharedStringIndex);

				actualStringElement.getChild("t").setInnerText(newContent);

			} catch (NumberFormatException e) {
				// ooops... the string could not be parsed, humm...
			}

			return;
		}
		
		// the cell does not yet exist, so we have to manually add it to the sheet file
		// TODO
	}
	
	public String getTitle() {
		return title;
	}
	
	public void save() {
		sheetFile.save();
	}

}
