/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


/**
 * A file object containing binary data, but without in-memory persistence of data
 */
public class BinaryFile extends File {

	// we use Latin-1 for binaries, as that allows us to load the entire byte range of 0..255 of ASCII files,
	// meaning that we can read any nonsensical streams and don't have to worry about incompatibilities
	// (if we just read and save we have no problem with Unicode characters either; if we actually set
	// Unicode text for some reason, then we will have to think a bit harder and maybe manually convert
	// the Unicode letters that we are aware of into their same-byte counterparts or whatever... ^^)
	public static final Charset BINARY_CHARSET = Utils.BINARY_CHARSET;


	/**
	 * You can construct a BinaryFile instance by directly from a path name.
	 */
	public BinaryFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a BinaryFile instance by basing it on an existing file object.
	 */
	public BinaryFile(File regularFile) {

		super(regularFile);
	}

	/**
	 * Create a new BinaryFile instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public BinaryFile(Directory directory, String filename) {

		super(directory, filename);
	}

	public byte[] loadContent() {

		try {
			return Files.readAllBytes(Paths.get(this.filename));

		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			System.err.println("[ERROR] Trying to load the file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}

		return new byte[0];
	}

	public String loadContentStr() {

		return new String(loadContent(), Utils.BINARY_CHARSET);
	}

	public void saveContent(byte[] content) {

		// fill file with data
		// TODO :: maybe buffer the output?
		try (FileOutputStream stream = new FileOutputStream(initSave())) {

			stream.write(content);

		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to write to the file " + filename + " - inconceivable!");
		}
	}

	public void saveContentStr(String content) {

		saveContentStr(content, Utils.BINARY_CHARSET);
	}

	/**
	 * Allows saving content directly, without it being interpreted as list of lines and acted upon
	 * Attention: Due to the nature of this function, it does NOT change the content buffered in this
	 * file instance; when reading content, it will be read from the buffer or disk, but not from the
	 * string you just supplied!
	 * Often used charsets are:
	 * StandardCharsets.ISO_8859_1 (Latin-1, covers the entire byte range of ASCII)
	 * StandardCharsets.UTF_8 (compatible with ASCII 0..127, but adds up to 4 byte wide character to get all of Unicode)
	 */
	public void saveContentStr(String content, Charset charset) {

		// fill file with data
		// TODO :: maybe buffer the output?
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(initSave()), charset)) {

			writer.write(content);

		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to write to the file " + filename + " - inconceivable!");
		}
	}

}
