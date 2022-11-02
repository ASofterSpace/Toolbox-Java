/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.xlsx;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.SortOrder;
import com.asofterspace.toolbox.utils.SortUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class XlsxSheet {

	private final static char[] COLS = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	private String title;

	private XmlFile sheetFile;

	private XlsxFile parent;


	public XlsxSheet(String title, XmlFile sheetFile, XlsxFile parent) {

		this.title = title;

		this.sheetFile = sheetFile;

		this.parent = parent;
	}

	/**
	 * Gets the content of a single cell
	 */
	public Record getCellContent(String cellName) {

		List<XmlElement> matchingCells = sheetFile.domGetElems("c", "r", cellName);

		for (XmlElement cell : matchingCells) {

			return getXmlElementContent(cell);
		}

		return null;
	}

	/**
	 * Gets the contents of all cells
	 */
	public Map<String, Record> getCellContents() {

		Map<String, Record> results = new HashMap<>();
		List<XmlElement> cells = sheetFile.domGetElems("c");

		for (XmlElement cell : cells) {
			results.put(cell.getAttribute("r"), getXmlElementContent(cell));
		}

		return results;
	}

	private Record getXmlElementContent(XmlElement cell) {

		String cellType = cell.getAttribute("t");
		XmlElement vChild = cell.getChild("v");

		if (vChild == null) {
			return null;
		}

		// no cellType means integer
		if (cellType == null) {
			String intContentStr = vChild.getInnerText();

			try {
				int intContent = Integer.parseInt(intContentStr);

				return new Record(intContent);

			} catch (NumberFormatException e) {
				// ooops... the string could not be parsed, humm...
			}
		}

		// cellType s means string (from the shared strings document)
		if (cellType.equals("s")) {
			// we have a string... and the strings are kept in a separate string file... so look there!
			String sharedStringIndexStr = vChild.getInnerText();
			try {
				int sharedStringIndex = Integer.parseInt(sharedStringIndexStr);

				List<XmlElement> sharedStrings = parent.getSharedStrings().getRoot().getElementsByTagNameHierarchy("sst", "si");

				XmlElement actualStringElement = sharedStrings.get(sharedStringIndex);

				XmlElement tChild = actualStringElement.getChild("t");
				if (tChild == null) {
					XmlElement rChild = actualStringElement.getChild("r");
					if (rChild != null) {
						tChild = rChild.getChild("t");
					}
				}
				if (tChild != null) {
					String strContent = tChild.getInnerText();
					if (strContent != null) {
						return new Record(strContent);
					}
				}

			} catch (NumberFormatException e) {
				// ooops... the string could not be parsed, humm...
			}
		}

		return null;
	}

	public void setCellContent(String cellName, String newContent) {

		List<XmlElement> matchingCells = sheetFile.domGetElems("c", "r", cellName);

		for (XmlElement cell : matchingCells) {

			// the cell already exists and we are just editing it in place

			if ("s".equals(cell.getAttribute("t"))) {
				// edit the shared string itself - TODO :: actually, check if the string is in use anywhere else first!

				// we just set this to string... and the strings are kept in a separate string file... so put it there!
				String sharedStringIndexStr = cell.getChild("v").getInnerText();
				try {
					int sharedStringIndex = Integer.parseInt(sharedStringIndexStr);

					List<XmlElement> sharedStrings = parent.getSharedStrings().getRoot().getElementsByTagNameHierarchy("sst", "si");

					XmlElement actualStringElement = sharedStrings.get(sharedStringIndex).getChild("t");

					actualStringElement.setInnerText(newContent);

				} catch (NumberFormatException e) {
					// ooops... the string could not be parsed, humm...
				}

			} else {
				// create the shared string

				cell.setAttribute("t", "s");

				// we just set this to string... and the strings are kept in a separate string file... so put it there!
				try {
					List<XmlElement> sharedStrings = parent.getSharedStrings().getRoot().getElementsByTagNameHierarchy("sst", "si");

					int newSharedStringIndex = sharedStrings.size();

					XmlElement actualStringElement = parent.getSharedStrings().getRoot().createChild("si").createChild("t");

					actualStringElement.setInnerText(newContent);

					cell.getChild("v").setInnerText(newSharedStringIndex);

				} catch (NumberFormatException e) {
					// ooops... the string could not be parsed, humm...
				}
			}
		}

		if (matchingCells.size() < 1) {
			// the cell does not yet exist, so we have to manually add it to the sheet file
			// TODO
			System.err.println("setCellContent was called for cell " + cellName + " but that cell does not yet exist and no code exists that can add a new cell");
		}
	}

	public void setNumberCellContent(String cellName, String newContent) {

		List<XmlElement> matchingCells = sheetFile.domGetElems("c", "r", cellName);

		for (XmlElement cell : matchingCells) {

			// the cell already exists and we are just editing it in place
			cell.removeAttribute("t");

			cell.getChild("v").setInnerText(newContent);

			return;
		}

		// the cell does not yet exist, so we have to manually add it to the sheet file
		// TODO
		System.err.println("setNumberCellContent was called for cell " + cellName + " but that cell does not yet exist and no code exists that can add a new cell");
	}

	public void setCellContent(String cellName, int newContent) {
		setNumberCellContent(cellName, ""+newContent);
	}

	public void setCellContent(String cellName, long newContent) {
		setNumberCellContent(cellName, ""+newContent);
	}

	public void deleteCell(String cellName) {

		List<XmlElement> matchingCells = sheetFile.domGetElems("c", "r", cellName);

		for (XmlElement cell : matchingCells) {
			cell.remove();
		}
	}

	public void deleteCell(int col, int row) {

		deleteCell(colRowToName(col, row));
	}

	public void deleteCellBlock(String topLeftCellName, String bottomRightCellName) {

		int leftCol = nameToColI(topLeftCellName);
		int rightCol = nameToColI(bottomRightCellName);
		int topRow = nameToRowI(topLeftCellName);
		int bottomRow = nameToRowI(bottomRightCellName);

		for (int row = topRow; row <= bottomRow; row++) {
			for (int col = leftCol; col <= rightCol; col++) {
				deleteCell(col, row);
			}
		}
	}

	public static String colRowToName(int col, int row) {

		String result = "";

		// TODO :: also handle more than ZZ columns (which currently will NOT work, even catastrophically - an exception will be thrown!)
		if (col >= COLS.length) {
			result += COLS[(col / COLS.length) - 1];
			col = col % COLS.length;
		}

		result += COLS[col];

		return result + row;
	}

	public static String nameToCol(String cellName) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < cellName.length(); i++) {
			if (Character.isLetter(cellName.charAt(i))) {
				result.append(cellName.charAt(i));
			}
		}
		return result.toString();
	}

	public static int nameToColI(String cellName) {
		// TODO :: for the love of St. Michael, do this better .o.
		// TODO :: also handle more than ZZ columns (I mean... maybe this already works, but it was not tested so far)
		int result = 0;
		int offset = 1;
		cellName = nameToCol(cellName);
		for (int i = cellName.length() - 1; i >= 0; i--) {
			char curChar = cellName.charAt(i);
			for (int j = 0; j < COLS.length; j++) {
				if (COLS[j] == curChar) {
					result += (j+1) * offset;
					offset *= COLS.length;
					break;
				}
			}
		}
		// we want A to be 0, not 1
		result -= 1;

		return result;
	}

	public static String nameToRow(String cellName) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < cellName.length(); i++) {
			if (Character.isDigit(cellName.charAt(i))) {
				result.append(cellName.charAt(i));
			}
		}
		return result.toString();
	}

	public static int nameToRowI(String cellName) {
		return Integer.valueOf(nameToRow(cellName));
	}

	public boolean hasFooter() {

		List<XmlElement> oddFooters = sheetFile.getRoot().getElementsByTagNameHierarchy("worksheet", "headerFooter", "oddFooter");

		return oddFooters.size() > 0;
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

	@Override
	public String toString() {

		StringBuilder cellStr = new StringBuilder();

		Map<String, Record> cellContents = getCellContents();
		Set<String> cellKeys = cellContents.keySet();
		List<String> sortedKeys = SortUtils.sort(cellKeys, SortOrder.NUMERICAL);
		String sep = "";
		for (String key : sortedKeys) {
			cellStr.append(sep);
			sep = ",";
			cellStr.append(key);
		}

		return "XlsxSheet [title: " + this.title + ", cells: [" + cellStr.toString() + "]]";
	}

}
