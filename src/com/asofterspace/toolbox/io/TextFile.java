/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.TextEncoding;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


/**
 * A text file object describes a plain text file and its content as one big String
 */
public class TextFile extends File {

	protected String filecontent;

	protected TextEncoding usingEncoding = null;

	protected boolean savingAllowed = true;

	private boolean useISOorUTFreadAndUTFwriteEncoding = false;


	/**
	 * Please do not construct a file without a name ;)
	 */
	protected TextFile() {
	}

	/**
	 * Create a new file instance based on the fully qualified file name,
	 * using a slash as separator independent of the operating system,
	 * and automatically loading the file contents (if there are any)
	 * @param fullyQualifiedFileName
	 */
	public TextFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * Create a new file instance based on another File
	 * @param File
	 */
	public TextFile(File regularFile) {

		super(regularFile);
	}

	/**
	 * Create a new file instance based on a Java File
	 * @param javaFile
	 */
	public TextFile(java.io.File javaFile) {

		super(javaFile);
	}

	/**
	 * Create a new file instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public TextFile(Directory directory, String filename) {

		super(directory, filename);
	}

	public TextEncoding getEncoding() {

		if (usingEncoding == null) {
			return TextEncoding.UTF8_WITHOUT_BOM;
		}

		return usingEncoding;
	}

	private Charset encodingToCharset(TextEncoding encoding) {

		if (encoding == null) {
			return StandardCharsets.UTF_8;
		}

		switch (encoding) {

			case UTF8_WITH_BOM:
			case UTF8_WITHOUT_BOM:
				return StandardCharsets.UTF_8;

			case ISO_LATIN_1:
				return StandardCharsets.ISO_8859_1;

			default:
				return StandardCharsets.UTF_8;
		}
	}

	/**
	 * Determines whether the UTF8 BOM should be written or not,
	 * but does NOT immediately write the file - so after changing
	 * this, if you also want to change the file on the disk, call
	 * save() afterwards!
	 */
	public void setEncoding(TextEncoding encoding) {
		usingEncoding = encoding;
	}

	/**
	 * By default, text files attempt to load and save with the explicitly set encoding,
	 * if none is set default to UTF without BOM or (if a BOM is present) to UTF with BOM,
	 * and save with the exact same encoding as they loaded.
	 * Call this method and set the argument to true to change this behavior to instead
	 * explicitly load with ISO_LATIN_1 unless a BOM is present (in that case load as UTF),
	 * and save to UTF with BOM regardless of how it was loaded (or in general, to save with
	 * the encoding specified by calling setEncoding() after this function has been called.)
	 */
	public void setISOorUTFreadAndUTFwriteEncoding(boolean useISOorUTFreadAndUTFwriteEncoding) {
		this.useISOorUTFreadAndUTFwriteEncoding = useISOorUTFreadAndUTFwriteEncoding;

		if (useISOorUTFreadAndUTFwriteEncoding) {
			this.usingEncoding = TextEncoding.UTF8_WITH_BOM;
		}
	}

	/**
	 * Creates this file on the disk, which entails:
	 * - creating the parent directory
	 * - assigning an empty file content, if there is not already content
	 *   (if this file has already been assigned content, that content will
	 *   be kept!)
	 * - saving the file to disk
	 */
	public void create() {

		prepareToCreate();

		save();
	}

	protected void prepareToCreate() {

		createParentDirectory();

		if (filecontent == null) {
			filecontent = "";
		}
	}

	/**
	 * Loads the file contents from the file system
	 * @return file contents
	 */
	public String loadContent(boolean complainIfMissing) {

		filecontent = null;

		try {
			byte[] binaryContent = Files.readAllBytes(this.getJavaPath());

			TextEncoding loadEncoding = this.usingEncoding;
			TextEncoding defaultLoadEncoding = TextEncoding.UTF8_WITHOUT_BOM;

			if (useISOorUTFreadAndUTFwriteEncoding) {
				loadEncoding = null;
				defaultLoadEncoding = TextEncoding.ISO_LATIN_1;
			}

			// autodetect encoding - read as UTF8...
			if (loadEncoding == null) {
				loadEncoding = defaultLoadEncoding;

				// ... and detect presence of BOM
				if (binaryContent.length > 2) {
					if ((binaryContent[0] == (byte) 239) &&
						(binaryContent[1] == (byte) 187) &&
						(binaryContent[2] == (byte) 191)) {
						loadEncoding = TextEncoding.UTF8_WITH_BOM;
					}
				}

				if (!useISOorUTFreadAndUTFwriteEncoding) {
					this.usingEncoding = loadEncoding;
				}
			}

			filecontent = new String(binaryContent, encodingToCharset(loadEncoding));

			if (loadEncoding == TextEncoding.UTF8_WITH_BOM) {
				filecontent = filecontent.substring(1);
			}

		} catch (IOException e) {
			if (complainIfMissing) {
				System.err.println("[ERROR] Trying to load the file " + filename + ", an I/O Exception occurred - inconceivable!");
			}
		}
		return filecontent;
	}

	/**
	 * Ensure that the contents have been loaded
	 * @param complainIfMissing Complain on sys err if the file is missing
	 */
	protected void ensureContents(boolean complainIfMissing) {

		if (filecontent == null) {
			loadContent(complainIfMissing);
		}
	}

	/**
	 * Gets the file content from the last time it was read
	 * from the file system or set explicitly
	 * @return file contents
	 */
	public String getContent() {

		return getContent(true);
	}

	/**
	 * Gets the file contentsfrom the last time it was read
	 * from the file system or set explicitly
	 * @param complainIfMissing Complain on sys err if the file is missing
	 * @return file contents
	 */
	public String getContent(boolean complainIfMissing) {

		// if the content has not yet been fetched... fetch it!
		ensureContents(complainIfMissing);

		return filecontent;
	}

	/**
	 * Clear the content (without saving)
	 */
	public void clearContent() {

		filecontent = "";
	}

	/**
	 * Explicitly sets the contents of this file instance
	 * as text consisting of several \n-separated lines
	 * (this does NOT automagically write them to the hard
	 * drive - if that is wanted, use saveContents()!)
	 * @param contents file contents to be set
	 */
	public void setContent(StringBuilder content) {
		setContent(content.toString());
	}

	/**
	 * Explicitly sets the contents of this file instance
	 * as text consisting of several \n-separated lines
	 * (this does NOT automagically write them to the hard
	 * drive - if that is wanted, use saveContents()!)
	 * @param contents file contents to be set
	 */
	public void setContent(String content) {

		filecontent = content;
	}

	public void preventSaving() {
		savingAllowed = false;
	}

	public void allowSaving() {
		savingAllowed = true;
	}

	/**
	 * Saves the current file contents of this instance
	 * to the file system
	 */
	public void save() {

		if (!savingAllowed) {
			return;
		}

		// the following line works also if usingEncoding is null, in which case
		// we want to default to UTF8_WITHOUT_BOM anyway
		boolean usingBom = usingEncoding == TextEncoding.UTF8_WITH_BOM;

		java.io.File targetFile = initSave();

		if (usingBom) {
			// initialize the file with the BOM
			try (FileOutputStream stream = new FileOutputStream(targetFile, false)) {

				// 0xEF 0xBB 0xBF
				byte[] utf8Bom = {(byte) 239, (byte) 187, (byte) 191};

				stream.write(utf8Bom);

			} catch (IOException e) {
				System.err.println("[ERROR] An IOException occurred when trying to write the UTF8 BOM to the file " + filename + " - inconceivable!");
			}
		}

		// fill file with the actual data
		// if we are using a UTF8 BOM, append now (to the BOM we just wrote)
		// if we are not using a UTF8 BOM, do not append (but just overwrite), as we have not yet written
		// to the file (as we have not written any BOM or anything else)
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(targetFile, usingBom),
			encodingToCharset(usingEncoding))) {

			writer.write(filecontent);

		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to write to the file " + filename + " - inconceivable!");
		}
	}

	/**
	 * Sets the file contents of this instance and writes
	 * them to the file system
	 * @param content file content
	 */
	public void saveContent(String content) {

		setContent(content);

		save();
	}

	/**
	 * Sets the file contents of this instance and writes
	 * them to the file system
	 * @param content file content
	 */
	public void saveContent(StringBuilder content) {

		setContent(content.toString());

		save();
	}

	/**
	 * Copy this file to another another file instance
	 */
	@Override
	public void copyToFileObject(File other) {

		super.copyToFileObject(other);

		if (other instanceof TextFile) {

			TextFile textOther = (TextFile) other;

			if (this.filecontent == null) {
				textOther.filecontent = null;
			} else {
				textOther.filecontent = this.filecontent;
			}
		}
	}

	/**
	 * Gives back a string representation of the file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.TextFile: " + filename;
	}

}
