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
		System.err.println("setCellContent was called for cell " + cellName + " but that cell does not yet exist and no code exists that can add a new cell");
	}
	
	/**
	 * The position can be R for right, C for center or L for left
	 * The content is just the string data (with &amp;P being the current page
	 * number, &amp;N being the total page number in the worksheet, and &amp;D
	 * being the current date)
	 */
	public void setFooterContent(String position, String content) {
		
		List<XmlElement> oddFooters = sheetFile.getRoot().getElementsByTagNameHierarchy("worksheet", "headerFooter", "oddFooter");

		if (oddFooters.size() < 1) {
			
			// TODO :: add a new oddFooter
			System.err.println("A new footer has to be added, as none is there yet, but there is no source code for that");
			
		} else {
		
			for (XmlElement oddFooter : oddFooters) {
			
				String footerCont = oddFooter.getInnerText();

				String leftFooterCont = "";
				String centerFooterCont = "";
				String rightFooterCont = "";

				// the following code works under the assumption that we always have L C R, in that order...
				// so far, we did not observe anything else in the wild, but we should improve this anyway
				// to be even more robust and also be able to handle e.g. &amp;Rblubb&amp;C&amp;P
				if (footerCont.contains("&L")) {
					leftFooterCont = footerCont.substring(footerCont.indexOf("&L")+2);
				}
				if (leftFooterCont.contains("&C")) {
					leftFooterCont = leftFooterCont.substring(0, leftFooterCont.indexOf("&C"));
				}
				if (leftFooterCont.contains("&R")) {
					leftFooterCont = leftFooterCont.substring(0, leftFooterCont.indexOf("&R"));
				}
				
				if (footerCont.contains("&C")) {
					centerFooterCont = footerCont.substring(footerCont.indexOf("&C")+2);
				}
				if (centerFooterCont.contains("&R")) {
					centerFooterCont = centerFooterCont.substring(0, centerFooterCont.indexOf("&R"));
				}
				
				if (footerCont.contains("&R")) {
					rightFooterCont = footerCont.substring(footerCont.indexOf("&R")+2);
				}
				
				switch (position.toUpperCase()) {
					case "L":
						leftFooterCont = content;
						break;
					case "C":
						centerFooterCont = content;
						break;
					case "R":
						rightFooterCont = content;
						break;
					default:
						System.err.println("setFooterContent(" + position + ", " + content + ") has been called, but the position must be L, C or R - nothing else!");
				}
				
				String newFooterCont = "";
				if (!leftFooterCont.equals("")) {
					newFooterCont += "&L" + leftFooterCont;
				}
				if (!centerFooterCont.equals("")) {
					newFooterCont += "&C" + centerFooterCont;
				}
				if (!rightFooterCont.equals("")) {
					newFooterCont += "&R" + rightFooterCont;
				}
				oddFooter.setInnerText(newFooterCont);
			}
		}
	}
	
	public String getTitle() {
		return title;
	}
	
	public void save() {
		sheetFile.save();
	}

}
