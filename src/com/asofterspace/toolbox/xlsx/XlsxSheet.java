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
import com.asofterspace.toolbox.utils.StrUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class XlsxSheet {

	private final static char[] COLS = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	private String title;

	private XmlFile sheetFile;

	private XlsxFile parent;

	private boolean sharedStringFastModeActive = false;
	private List<XmlElement> sharedStrings = null;


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

	public Integer getCellContentInteger(String cellName) {
		Record result = getCellContent(cellName);
		if (result == null) {
			return null;
		}
		return result.asInteger();
	}

	public int getCellContentIntegerNonNull(String cellName) {
		Record result = getCellContent(cellName);
		if (result == null) {
			return 0;
		}
		Integer innerResult = result.asInteger();
		if (innerResult == null) {
			return 0;
		}
		return innerResult;
	}

	public Double getCellContentDouble(String cellName) {
		Record result = getCellContent(cellName);
		if (result == null) {
			return null;
		}
		return result.asDouble();
	}

	public double getCellContentDoubleNonNull(String cellName) {
		Record result = getCellContent(cellName);
		if (result == null) {
			return 0;
		}
		Double innerResult = result.asDouble();
		if (innerResult == null) {
			return 0;
		}
		return innerResult;
	}

	public String getCellContentString(String cellName) {
		Record result = getCellContent(cellName);
		if (result == null) {
			return null;
		}
		return result.asString();
	}

	public String getCellContentStringNonNull(String cellName) {
		Record result = getCellContent(cellName);
		if (result == null) {
			return "";
		}
		String innerResult = result.asString();
		if (innerResult == null) {
			return "";
		}
		return innerResult;
	}

	/**
	 * Gets the contents of all cells of that row
	 */
	public Map<String, Record> getRowContents(String rowNum) {

		Map<String, Record> results = new HashMap<>();
		List<XmlElement> cells = sheetFile.domGetElems("c");

		for (XmlElement cell : cells) {
			String which = cell.getAttribute("r");
			String numPart = nameToRow(which);
			if (rowNum.equals(numPart)) {
				results.put(which, getXmlElementContent(cell));
			}
		}

		return results;
	}

	/**
	 * Gets the contents of all cells of that column
	 */
	public Map<String, Record> getColContents(String colName) {

		Map<String, Record> results = new HashMap<>();
		List<XmlElement> cells = sheetFile.domGetElems("c");

		for (XmlElement cell : cells) {
			String which = cell.getAttribute("r");
			String colPart = nameToCol(which);
			if (colName.equals(colPart)) {
				results.put(which, getXmlElementContent(cell));
			}
		}

		return results;
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

	/**
	 * Gets the number of the highest-numbered row in the sheet that contains a cell, so is not entirely empty
	 */
	public int getHighestRowNum() {

		int result = 0;
		List<XmlElement> cells = sheetFile.domGetElems("c");

		for (XmlElement cell : cells) {
			int newRes = nameToRowI(cell.getAttribute("r"));
			if (newRes > result) {
				result = newRes;
			}
		}

		return result;
	}

	private Record getXmlElementContent(XmlElement cell) {

		String cellType = cell.getAttribute("t");
		XmlElement vChild = cell.getChild("v");

		if (vChild == null) {
			return null;
		}

		// cellType n (in OpenOffice) or no cellType (in Excel) means number
		if ((cellType == null) || cellType.equals("n")) {
			String intContentStr = vChild.getInnerText();

			try {
				if (intContentStr.contains(".")) {
					double doubleContent = Double.parseDouble(intContentStr);

					return new Record(doubleContent);
				}

				int intContent = Integer.parseInt(intContentStr);

				return new Record(intContent);

			} catch (NumberFormatException e) {
				// ooops... the string could not be parsed, humm...
				System.out.println("Could not parse: " + intContentStr);
			}
		}

		// cellType s means string (from the shared strings document)
		if (cellType.equals("s")) {
			// we have a string... and the strings are kept in a separate string file... so look there!
			String sharedStringIndexStr = vChild.getInnerText();
			try {
				int sharedStringIndex = Integer.parseInt(sharedStringIndexStr);

				List<XmlElement> sharedStrings = getSharedStrings();

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

	private XmlElement getOrCreateCell(String cellName) {

		List<XmlElement> matchingCells = sheetFile.domGetElems("c", "r", cellName);
		for (XmlElement c : matchingCells) {
			return c;
		}

		// the cell does not yet exist, so we have to manually add it to the sheet file

		XmlElement sheetData = sheetFile.getRoot().getChild("sheetData");

		String rowNum = nameToRow(cellName);
		XmlElement row = sheetData.getChild("row", "r", rowNum);

		// not even the row exists, so it needs to be added...
		if (row == null) {
			row = sheetData.createChild("row");
			row.setAttribute("r", rowNum);
		}

		XmlElement cell = row.createChild("c");
		cell.setAttribute("r", cellName);
		cell.setAttribute("s", "1"); // spans 1 by default
		String spansStr = row.getAttribute("spans");
		if (spansStr == null) {
			spansStr = "1:1";
		}
		int spansFrom = StrUtils.strToInt(spansStr.substring(0, spansStr.indexOf(":")));
		int spansTo = StrUtils.strToInt(spansStr.substring(spansStr.indexOf(":") + 1));
		int colNumI = nameToColI(cellName) + 1;
		if (colNumI < spansFrom) {
			spansFrom = colNumI;
		}
		if (colNumI > spansTo) {
			spansTo = colNumI;
		}
		row.setAttribute("spans", spansFrom + ":" + spansTo);

		// sort cells within the row
		Collections.sort(row.getChildNodes(), new Comparator<XmlElement>() {
			public int compare(XmlElement leftChild, XmlElement rightChild) {
				return nameToColI(leftChild.getAttribute("r")) - nameToColI(rightChild.getAttribute("r"));
			}
		});

		return cell;
	}

	public void setCellContent(String cellName, String newContent) {

		XmlElement cell = getOrCreateCell(cellName);

		// is the cell a string cell?
		if ("s".equals(cell.getAttribute("t"))) {
			// the cell is a string cell

			// edit the shared string itself - TODO :: actually, check if the string is in use anywhere else first!

			// we just set this to string... and the strings are kept in a separate string file... so put it there!
			String sharedStringIndexStr = cell.getChild("v").getInnerText();
			try {
				int sharedStringIndex = Integer.parseInt(sharedStringIndexStr);

				List<XmlElement> sharedStrings = getSharedStrings();

				XmlElement actualStringElement = sharedStrings.get(sharedStringIndex);
				XmlElement tChild = actualStringElement.getChild("t");

				if (tChild == null) {
					XmlElement rChild = actualStringElement.getChild("r");
					if (rChild != null) {
						tChild = rChild.getChild("t");
						if (tChild == null) {
							tChild = rChild.createChild("t");
						}
					} else {
						tChild = actualStringElement.createChild("t");
					}
				}

				if (newContent.equals(tChild.getInnerText())) {
					// nothing to be done, the value is already correct!
					return;
				}

				tChild.setInnerText(newContent);

			} catch (NumberFormatException e) {
				// ooops... the string could not be parsed, humm...
			}

		} else {
			// the cell is not yet a string cell

			// create the shared string

			cell.setAttribute("t", "s");

			// we just set this to string... and the strings are kept in a separate string file... so put it there!
			try {
				List<XmlElement> sharedStrings = getSharedStrings();

				int newSharedStringIndex = sharedStrings.size();

				XmlElement actualStringElement = parent.getSharedStrings().getRoot().createChild("si").createChild("t");

				actualStringElement.setInnerText(newContent);

				XmlElement vCell = cell.getChild("v");
				if (vCell == null) {
					vCell = cell.createChild("v");
				}

				vCell.setInnerText(newSharedStringIndex);

			} catch (NumberFormatException e) {
				// ooops... the string could not be parsed, humm...
			}
		}
	}

	public void setNumberCellContent(String cellName, String newContent) {

		XmlElement cell = getOrCreateCell(cellName);

		cell.removeAttribute("t");

		XmlElement vCell = cell.getChild("v");
		if (vCell == null) {
			vCell = cell.createChild("v");
		}

		vCell.setInnerText(newContent);
	}

	public void setCellContent(String cellName, int newContent) {
		setNumberCellContent(cellName, ""+newContent);
	}

	public void setCellContent(String cellName, long newContent) {
		setNumberCellContent(cellName, ""+newContent);
	}

	public void setCellContent(String cellName, double newContent) {
		setNumberCellContent(cellName, ""+newContent);
	}

	public void deleteCell(String cellName) {

		List<XmlElement> matchingCells = sheetFile.domGetElems("c", "r", cellName);

		for (XmlElement cell : matchingCells) {
			XmlElement row = cell.getXmlParent();
			cell.remove();
			if (row.getChildNodes().size() < 1) {
				row.remove();
			}
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
		if (cellName == null) {
			return null;
		}
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
		if (cellName == null) {
			return null;
		}
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

	public synchronized void setSharedStringFastModeActive(boolean activate) {
		if (activate) {
			sharedStrings = parent.getSharedStrings().getRoot().getElementsByTagNameHierarchy("sst", "si");
		}
		sharedStringFastModeActive = activate;
	}

	private List<XmlElement> getSharedStrings() {
		if (sharedStringFastModeActive) {
			return sharedStrings;
		}
		return parent.getSharedStrings().getRoot().getElementsByTagNameHierarchy("sst", "si");
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
