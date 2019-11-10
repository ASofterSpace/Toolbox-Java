/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.Image;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * This class can be used to open, modify and create from scratch PDF files - yay :)
 */
public class PdfFile extends BinaryFile {

	// we use Latin-1 for PDFs, as that allows us to load the entire byte range of 0..255 of ASCII files,
	// meaning that we can read any nonsensical streams and don't have to worry about incompatibilities
	// (if we just read and save we have no problem with Unicode characters either; if we actually set
	// Unicode text for some reason, then we will have to think a bit harder and maybe manually convert
	// the Unicode letters that we are aware of into their same-byte counterparts or whatever... ^^)
	static final Charset PDF_CHARSET = BinaryFile.BINARY_CHARSET;

	private boolean pdfLoadingStarted = false;
	private boolean pdfLoaded = false;

	private Integer version;

	private List<PdfObject> objects;

	private List<String> xrefs;

	private PdfDictionary trailer;

	private String startxref;

	private StringBuilder trailerText;

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

		pdfLoadingStarted = true;

		// initialize to false, such that if we abort in the middle, we count as "non-loaded" (even if we loaded successfully before)
		pdfLoaded = false;

		objects = new ArrayList<>();

		xrefs = new ArrayList<>();

		trailer = new PdfDictionary();

		startxref = "";
	}

	private void loadPdfContents() {

		initEmptyPdf();

		trailerText = new StringBuilder();

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

					int offset = 0;

					byte nextByte = 0;

					// ignore the second line end marker in case of \r\n (which seems to actually be the PDF default!)
					if (cur < len) {
						nextByte = binaryContent[cur];

						if ((nextByte == '\n') || (nextByte == '\r')) {

							// ignore the second line end marker of the current line,
							// e.g. in front of the stream keyword
							cur++;

							// ignore the second line end marker after the stream keyword
							// (here it is NOT CHECKED that there are also two line end markers
							// after the stream keyword, but seriously, if the PDF file is not
							// even consistent in using either \n or \r\n, then we are pretty
							// much doomed anyway... DOOMED! ^^)
							offset++;
						}
					}

					if (gotLine(line)) {
						return;
					}

					// TODO 1 :: if we reached the end, break away (at least for now, as we do not handle multiple ends yet)
					if (currentSection == PdfSection.EOF) {
						break;
					}

					// if we are starting a stream, read its length and then read its contents directly as
					// streams are NOT line-based and any \r or \n in there is just a regular stream byte
					if ((currentSection == PdfSection.IN_OBJECT) && line.endsWith("stream") && (!line.endsWith("endstream"))) {
						Integer streamLen = currentObject.preGetStreamLength();

						if (streamLen == null) {
							// the length cannot be read out - either there is no length field,
							// or it refers to an object that comes later (great .-.), which
							// we could read out by following the xref table at the end...
							// but that one is sometimes broken in the wild, so let's instead
							// try to go until endstream\nendobj\n, hoping that this is not part
							// of the regular stream content
							streamLen = binaryContent.length - cur - 1;
							String searchFor;
							if (offset == 0) {
								searchFor = "endstream" + (char) curByte + "endobj" + (char) curByte;
							} else {
								searchFor = "endstream" + (char) curByte + (char) nextByte + "endobj" + (char) curByte + (char) nextByte;
							}

							buffer = new byte[streamLen];
							System.arraycopy(binaryContent, cur, buffer, 0, streamLen);
							line = new String(buffer, PDF_CHARSET);

							streamLen = line.indexOf(searchFor);

							if (streamLen < 0) {
								streamLen = 0;
							}

							if (offset == 0) {
								if ((streamLen > 0) && (buffer[streamLen - 1] == curByte)) {
									streamLen -= 1;
								}
							} else {
								if ((streamLen > 1) && (buffer[streamLen - 2] == curByte) && (buffer[streamLen - 1] == nextByte)) {
									streamLen -= 2;
								}
							}
						}

						buffer = new byte[streamLen];
						System.arraycopy(binaryContent, cur, buffer, 0, streamLen);

						line = new String(buffer, PDF_CHARSET);

						currentObject.readStreamContents(line);

						cur += streamLen + offset;
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
				// TODO :: maybe it would be better to preserve comments if we open and close PDF files?
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
						currentObject = new PdfObject(this, line);
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
					trailer.loadFromString(trailerText.toString());
					currentSection = PdfSection.STARTXREF;
					return false; // no error
				}

				trailerText.append(line + "\n");

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

	/**
	 * We might have e.g. for a stream length either
	 * /Length 9
	 * or
	 * /Length 9 0 R
	 *
	 * In the first case, the stream has length 9.
	 * In the second case, the stream length is the content of PDF Object (9, 0).
	 * Using the resolve function on "9" or "9 0 R" respectively gives you the
	 * actual length - in both cases :)
	 */
	String resolve(String link) {

		if (link == null) {
			return null;
		}

		// first of all, check if this is a link...
		if (link.contains(" ")) {

			// if yes, follow the link...
			String[] linkArr = link.split(" ");

			if (linkArr.length > 1) {

				try {
					int linkNumber = Integer.valueOf(linkArr[0]);
					int linkGeneration = Integer.valueOf(linkArr[1]);

					PdfObject obj = getObject(linkNumber, linkGeneration);

					// if we are called while the reading process is ongoing,
					// the obj might not yet have been read... oops!
					if (obj == null) {
						return null;
					}

					return obj.getContent();

				} catch (NumberFormatException e) {
					return null;
				}
			}
		}

		// if no, then this is just flat data - simply return it
		return link;
	}

	public void create(String text) {

		initEmptyPdf();

		this.version = 0;

		int objNum = 1;

		PdfObject obj = new PdfObject(this, objNum++, 0);
		obj.setDictValue("/Type", "/Catalog");
		obj.setDictValue("/Pages", "2 0 R"); // TODO :: use reference instead of hardcoded link
		objects.add(obj);

		obj = new PdfObject(this, objNum++, 0);
		obj.setDictValue("/Type", "/Pages");
		obj.setDictValue("/Count", "1");
		obj.setDictValue("/Kids", "[3 0 R]"); // TODO :: use reference(s) instead of hardcoded link(s)
		objects.add(obj);

		obj = new PdfObject(this, objNum++, 0);
		obj.setDictValue("/Type", "/Range");
		obj.setDictValue("/Parent", "2 0 R"); // TODO :: use reference(s) instead of hardcoded link(s)
		obj.setDictValue("/MediaBox", "[0 0 612 792]");
		obj.setDictValue("/Contents", "4 0 R"); // TODO :: use reference(s) instead of hardcoded link(s)
		PdfDictionary f1 = new PdfDictionary();
		f1.set("/F1", "5 0 R");
		PdfDictionary font = new PdfDictionary();
		font.set("/Font", f1);
		obj.setDictValue("/Resources", font);
		objects.add(obj);

		obj = new PdfObject(this, objNum++, 0);
		String streamContent = "BT\n/F1 24 Tf\n250 700 Td (" + text + ") Tj\nET";
		obj.setDictValue("/Length", "" + streamContent.length());
		obj.setStreamContent(streamContent);
		objects.add(obj);

		obj = new PdfObject(this, objNum++, 0);
		obj.setDictValue("/Type", "/Font");
		obj.setDictValue("/Subtype", "/Type1");
		obj.setDictValue("/BaseFont", "/Helvetica"); // TODO :: embed font inline
		objects.add(obj);

		int xrefSize = objects.size() + 1;
		xrefs.add("0 " + xrefSize);

		trailer.set("/Size", "" + xrefSize);
		trailer.set("/Root", "1 0 R"); // TODO :: search objects, get the one with Type Catalog, and put its reference here (usually it is 1 0, but not necessarily)

		pdfLoaded = true;
	}

	public List<PdfObject> getObjects() {

		if (!pdfLoaded) {
			loadPdfContents();
		}

		return objects;
	}

	public PdfObject getObject(int number, int generation) {

		// this may get called during the reading process, so we are just checking if we already
		// started - not if we finished
		if (!pdfLoadingStarted) {
			loadPdfContents();
		}

		for (PdfObject obj : objects) {
			if (number == obj.getNumber()) {
				if (generation == obj.getGeneration()) {
					return obj;
				}
			}
		}

		return null;
	}

	/**
	 * Export embedded pictures as files
	 */
	public List<File> exportPictures(Directory targetDir) {

		List<File> resultList = new ArrayList<>();

		List<PdfObject> objects = getObjects();

		for (PdfObject obj : objects) {
			if ("/XObject".equals(obj.getDictValue("/Type"))) {
				if ("/Image".equals(obj.getDictValue("/Subtype"))) {
					String filter = obj.getDictValue("/Filter");
					if (filter == null) {
						filter = "";
					}
					switch (filter) {
						case "": // Portable Pix Map
						case "/FlateDecode": // PNG? or just generic, could be anything?

							// read the color space to see what kind of a picture we have on our hands...
							String colorSpace = obj.getDictValue("/ColorSpace");

							if ("/DeviceGray".equals(colorSpace)) {
								BinaryFile pgmFile = new BinaryFile(targetDir, "Image" + obj.getNumber() + ".pgm");
								String header = "P5\n" + obj.getDictValue("/Width") + " " + obj.getDictValue("/Height") + "\n255\n";
								pgmFile.saveContentStr(header + obj.getPlainStreamContent());
								resultList.add(pgmFile);
								break;
							}

							// by default, assume DeviceRGB
							// TODO :: do something about the ICCBased ColorSpace, if it is present?
							// (right now we are just ignoring it... which however a lot of programs seem to do quite happily ^^)
							BinaryFile ppmFile = new BinaryFile(targetDir, "Image" + obj.getNumber() + ".ppm");
							String header = "P6\n" + obj.getDictValue("/Width") + " " + obj.getDictValue("/Height") + "\n255\n";
							ppmFile.saveContentStr(header + obj.getPlainStreamContent());
							resultList.add(ppmFile);
							break;
						case "/DCTDecode": //JPEG
						case "/JPX": // JPEG2000
							BinaryFile jpgFile = new BinaryFile(targetDir, "Image" + obj.getNumber() + ".jpg");
							jpgFile.saveContentStr(obj.getStreamContent());
							resultList.add(jpgFile);
							break;
						default:
							System.err.println("The image cannot be saved as the filter is not understood! :(");
							break;
					}
				}
			}
		}

		return resultList;
	}

	/**
	 * Get the embedded pictures as images
	 */
	public List<Image> getPictures() {

		List<Image> result = new ArrayList<>();

		// TODO :: actually directly get the images, without going via files and temp directories and all that!
		Directory tempDir = new Directory("temp");
		List<File> pictures = exportPictures(tempDir);
		for (File picFile : pictures) {
			result.add(ImageFile.readImageFromFile(picFile));
		}
		tempDir.delete();

		return result;
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
			obj.appendToPdfFile(pdf);
			pdf.append("\r\n");
		}

		pdf.append("xref\r\n");
		for (String xrefLine : xrefs) {
			pdf.append(xrefLine + "\r\n");
		}

		pdf.append("\r\n");

		pdf.append("trailer\r\n");
		trailer.appendToPdfFile(pdf, "\r\n");

		pdf.append("\r\nstartxref\r\n");
		// TODO :: insert actual byte offset of the latest xref section... although some pdfviewers seem to not explode if we leave it out
		// for now, we just keep startxref empty (instead of calculating it properly)
		startxref = "";
		pdf.append(startxref + "\r\n");

		pdf.append("%%EOF");

		super.saveContentStr(pdf.toString(), PDF_CHARSET);
	}

	/**
	 * Gives back a string representation of the pdf file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.PdfFile: " + filename;
	}

}
