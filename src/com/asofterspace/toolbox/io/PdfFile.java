/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * This class can be used to open, modify and create from scratch PDF files - yay :)
 */
public class PdfFile extends File {

	// we use Latin-1 for PDFs, as that allows us to load the entire byte range of 0..255 of ASCII files,
	// meaning that we can read any nonsensical streams and don't have to worry about incompatibilities
	// (if we just read and save we have no problem with Unicode characters either; if we actually set
	// Unicode text for some reason, then we will have to think a bit harder and maybe manually convert
	// the Unicode letters that we are aware of into their same-byte counterparts or whatever... ^^)
	private static final Charset PDF_CHARSET = StandardCharsets.ISO_8859_1;

	private boolean pdfLoaded = false;
	
	private Integer version;
	
	private List<PdfObject> objects;
	
	private List<String> xrefs;
	
	private List<String> trailer;
	
	private String startxref;
	
	// the following enum and objects are used to keep track of internal state during the PDF slurping process
	private enum PdfSection {
		VERSION,
		OBJECTS,
		IN_OBJECT,
		XREF,
		TRAILER,
		STARTXREF,
		EOF
	};
	
	private PdfSection currentSection;
	
	private PdfObject currentObject;

	
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
	
	private void initEmptyPdf() {
	
		// initialize to false, such that if we abort in the middle, we count as "non-loaded" (even if we loaded successfully before)
		pdfLoaded = false;
		
		objects = new ArrayList<>();
		
		xrefs = new ArrayList<>();
		
		trailer = new ArrayList<>();
		
		startxref = "";
	}
	
	private void loadPdfContents() {
	
		initEmptyPdf();

		currentSection = PdfSection.VERSION;

		try {
			byte[] binaryContent = Files.readAllBytes(Paths.get(this.filename));
			int len = binaryContent.length;
			int cur = 0;
			int lineStart = 0;

			while (cur < len) {
			
				byte curByte = binaryContent[cur];
				cur++;
				
				if ((curByte == '\n') || (curByte == '\r')) {
					
					int lineLen = cur - 1 - lineStart;
					byte[] buffer = new byte[lineLen];
					System.arraycopy(binaryContent, lineStart, buffer, 0, lineLen);
					String line = new String(buffer, PDF_CHARSET);
					
					if (gotLine(line)) {
						return;
					}
					
					// TODO 1 :: if we reached the end, break away (at least for now, as we do not handle multiple ends yet)
					if (currentSection == PdfSection.EOF) {
						break;
					}

					// ignore the second line end marker in case of \r\n (which seems to actually be the PDF default!)
					if (cur+1 < len) {
						byte nextByte = binaryContent[cur+1];
						
						if ((nextByte == '\n') || (nextByte == '\r')) {
							cur++;
						}
					}

					// if we are starting a stream, read its length and then read its contents directly as
					// streams are NOT line-based and any \r or \n in there is just a regular stream byte
					if ((currentSection == PdfSection.IN_OBJECT) && line.endsWith("stream") && (!line.endsWith("endstream"))) {
						lineLen = currentObject.getStreamLength();

						buffer = new byte[lineLen];
						System.arraycopy(binaryContent, cur+1, buffer, 0, lineLen);
						line = new String(buffer, PDF_CHARSET);

						currentObject.readStreamContents(line);

						cur += lineLen + 1;
					}
					
					lineStart = cur;
					
				}
			}
			
			pdfLoaded = true;

		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			System.err.println("[ERROR] Trying to load the file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}
		
		// only set the PDF to loaded once we reached this line without errors
		pdfLoaded = true;
	}
	
	// returns true if there was an error, false if not
	private boolean gotLine(String line) {

		if (line.equals("")) {
			// ignore empty lines
			return false;
		}

		if (line.startsWith("%")) {
			if (currentSection != PdfSection.VERSION) {
				// ignore this line... as it is a comment ^^
				return false;
			}
		}
		
		switch (currentSection) {

			case VERSION:
				if (line.startsWith("%PDF-1.")) {
					try {
						version = Integer.valueOf(line.substring(7));
						currentSection = PdfSection.OBJECTS;
						return false; // no error
					} catch (NumberFormatException e) {
						System.err.println("[ERROR] The PDF file " + filename + " unexpectedly has an unexpected version!");
					}
				} else {
					System.err.println("[ERROR] The PDF file " + filename + " is unexpectedly missing a version!");
				}
				break;
			
			case OBJECTS:

				if (line.equals("xref")) {
					currentSection = PdfSection.XREF;
					return false; // no error
				}

				if (line.endsWith("obj")) {
					try {
						currentObject = new PdfObject(line);
						objects.add(currentObject);
						currentSection = PdfSection.IN_OBJECT;
						return false; // no error
					} catch (Exception e) {
						System.err.println("[ERROR] The PDF file " + filename + " contains an unreadable object!");
					}
				} else {
					System.err.println("[ERROR] The PDF file " + filename + " contains some unexpected content!");
				}

				break;
			
			case IN_OBJECT:
				if (line.equals("endobj")) {
					currentObject.doneReadingContents();
					currentSection = PdfSection.OBJECTS;
				} else {
					currentObject.readContents(line);
				}
				return false; // no error
				
			case XREF:
				if (line.equals("trailer")) {
					currentSection = PdfSection.TRAILER;
					return false; // no error
				}
				
				xrefs.add(line);

				return false; // no error

			case TRAILER:
				if (line.equals("startxref")) {
					currentSection = PdfSection.STARTXREF;
					return false; // no error
				}
				
				trailer.add(line);

				return false; // no error
				
			case STARTXREF:
				if (!line.equals("xref")) {
					startxref = line;
				}
				
				currentSection = PdfSection.EOF;
				
				return false; // no error
		}
		
		// TODO 1 :: there could be more than one %%EOF marker!
		// this happens due to incremental updates, in which an original PDF is kept,
		// and a new version's changes are simply appended at the end - see:
		// https://stackoverflow.com/questions/11896858/does-the-eof-in-a-pdf-have-to-appear-within-the-last-1024-bytes-of-the-file
		// ... however, so far we are only reading out until the first startxref, get that value, and then stop instead of reading the whole file!
		
		return true;
	}
	
	public void create(String text) {
	
		initEmptyPdf();

		this.version = 0;
		
		int objNum = 1;
		
		PdfObject obj = new PdfObject(objNum++, 0);
		obj.setType("Catalog");
		obj.setContent("%/OpenAction\r\n/Pages 2 0 R"); // TODO :: use reference instead of hardcoded link
		objects.add(obj);
		
		obj = new PdfObject(objNum++, 0);
		obj.setType("Pages");
		obj.setContent("/Count 1\r\n/Kids [3 0 R]"); // TODO :: use reference(s) instead of hardcoded link(s)
		objects.add(obj);
		
		obj = new PdfObject(objNum++, 0);
		obj.setType("Range");
		obj.setContent("/Parent 2 0 R\r\n/Resources << /Font << /F1 5 0 R >> >>\r\n/MediaBox [0 0 612 792]\r\n/Contents 4 0 R"); // TODO :: use reference(s) instead of hardcoded link(s)
		objects.add(obj);
		
		obj = new PdfObject(objNum++, 0);
		String streamContent = "BT\n/F1 24 Tf\n250 700 Td (" + text + ") Tj\nET";
		obj.setContent("<< /Length " + streamContent.length() + " >>");
		obj.setStreamContent(streamContent);
		objects.add(obj);
		
		obj = new PdfObject(objNum++, 0);
		obj.setType("Font");
		obj.setContent("/Subtype /Type1\r\n/BaseFont /Helvetica"); // TODO :: embed font inline
		objects.add(obj);

		int xrefSize = objects.size() + 1;
		xrefs.add("0 " + xrefSize);
		
		trailer.add("<<");
		trailer.add("/Size " + xrefSize);
		trailer.add("/Root 1 0 R"); // TODO :: search objects, get the one with Type Catalog, and put its reference here (usually it is 1 0, but not necessarily)
		trailer.add(">>");
		
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
		
		// the PDF specification asks us to include a comment with at least four characters whose codes are 128 or higher
		// we use the opportunity to mention who we are ^^
		pdf.append("% µµµµµµµµµµµµµµµµµµµµµµµµµµµ %\r\n");
		pdf.append("% Generated by A Softer Space %\r\n");
		pdf.append("% µµµµµµµµµµµµµµµµµµµµµµµµµµµ %\r\n");
		
		pdf.append("\r\n");

		for (PdfObject obj : objects) {
			pdf.append(obj);
			pdf.append("\r\n");
		}
		
		pdf.append("xref\r\n");
		for (String xrefLine : xrefs) {
			pdf.append(xrefLine + "\r\n");
		}
		
		pdf.append("\r\n");
		
		pdf.append("trailer\r\n");
		for (String trailerLine : trailer) {
			pdf.append(trailerLine + "\r\n");
		}
		
		pdf.append("startxref\r\n");
		// TODO :: insert actual byte offset of the latest xref section... although some pdfviewers seem to not explode if we leave it out
		// for now, we just keep startxref empty (instead of calculating it properly)
		startxref = "";
		pdf.append(startxref + "\r\n");
		
		pdf.append("%%EOF");
		
		super.saveContentDirectly(pdf.toString(), PDF_CHARSET);
	}
}
