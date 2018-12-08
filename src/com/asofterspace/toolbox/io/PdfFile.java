/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.util.ArrayList;
import java.util.List;


/**
 * This class can be used to open, modify and create from scratch PDF files - yay :)
 */
public class PdfFile extends File {

	private boolean pdfLoaded = false;
	
	private Integer version;
	
	private List<PdfObject> objects;

	
	/**
	 * You can construct a PdfFile instance by directly from a path name.
	 */
	public PdfFile(String fullyQualifiedFileName) {
	
		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a PdfFile instance by basing it on an existing file object.
	 */
	public PdfFile(File regularFile) {
	
		super(regularFile);
	}
	
	private void loadPdfContents() {
	
		// initialize to false, such that if we abort in the middle, we count as "non-loaded" (even if we loaded successfully before)
		pdfLoaded = false;

		boolean complainIfMissing = true;

		List<String> contentList = loadContents(complainIfMissing);

		if ((contentList == null) || (contentList.size() < 1)) {
			System.err.println("[ERROR] Trying to load the file " + filename + ", but it contains no data - inconceivable!");
			return;
		}
		
		objects = new ArrayList<PdfObject>();
		
		String line = contentList.get(0);
		if (line.startsWith("%PDF-1.")) {
			try {
				version = Integer.valueOf(line.substring(7));
			} catch (NumberFormatException e) {
				System.err.println("[ERROR] The PDF file " + filename + " unexpectedly has an unexpected version!");
				return;
			}
		} else {
			System.err.println("[ERROR] The PDF file " + filename + " is unexpectedly missing a version!");
			return;
		}
			
		for (int i = 1; i < contentList.size(); i++) {
		
			line = contentList.get(i);

			if (line.equals("")) {
				// ignore empty lines
				continue;
			}
			
			if (line.equals("%µµµµ")) {
				// ignore this line... we have no idea what it means ^^
				continue;
			}
			
			if (line.endsWith("obj")) {
				try {
					PdfObject newObj = new PdfObject(line);
					objects.add(newObj);
					i++;
					StringBuilder objContents = new StringBuilder();
					while (!(contentList.get(i).equals("endobj"))) {
						objContents.append(contentList.get(i));
						i++;
					}
					newObj.readContents(objContents.toString());
					continue;

				} catch (Exception e) {
					System.err.println("[ERROR] The PDF file " + filename + " contains an unreadable object!");
					return;
				}
			}
			
			System.err.println("[ERROR] The PDF file " + filename + " contains some unexpected content!");
			return;
		}
		
		if (version == null) {
			System.err.println("[ERROR] The PDF file " + filename + " has no version!");
		}
		
		// btw., be aware - there could be more than one %%EOF marker!
		// this happens due to incremental updates, in which an original PDF is kept,
		// and a new version's changes are simply appended at the end - see:
		// https://stackoverflow.com/questions/11896858/does-the-eof-in-a-pdf-have-to-appear-within-the-last-1024-bytes-of-the-file
		
		// only set the PDF to loaded once we reached this line without errors
		pdfLoaded = true;
	}
	
	public void create() {
	
		version = 0;
		
		objects = new ArrayList<PdfObject>();
		
		int objNum = 0;
		
		PdfObject obj = new PdfObject(objNum++, 0);
		obj.setType("Catalog");
		objects.add(obj);
		
		obj = new PdfObject(objNum++, 0);
		obj.setType("Pages");
		objects.add(obj);
		
		pdfLoaded = true;
	}

	public void save() {
	
		if (!pdfLoaded) {
			loadPdfContents();
		}
		
		StringBuilder pdf = new StringBuilder();
		
		pdf.append("%PDF-1.");
		pdf.append(version);
		pdf.append("\r\n");
		
		for (PdfObject obj : objects) {
			pdf.append(obj);
		}
		
		int xrefSize = objects.size() + 1;
		
		pdf.append("xref\r\n");
		pdf.append("0 " + xrefSize + "\r\n");
		
		pdf.append("trailer\r\n");
		pdf.append("<<\r\n");
		pdf.append("Size " + xrefSize + "\r\n");
		pdf.append("/Root 1 0 R\r\n"); // TODO :: search objects, get the one with Type Catalog, and put its reference here (usually it is 1 0, but not necessarily)
		pdf.append(">>\r\n");
		pdf.append("startxref\r\n");
		
		pdf.append("%%EOF");
		
		super.saveContent(pdf.toString());
	}
}
