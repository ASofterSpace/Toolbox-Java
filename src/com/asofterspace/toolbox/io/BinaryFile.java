/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
	public static final Charset BINARY_CHARSET = StandardCharsets.ISO_8859_1;

	
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
	
	public byte[] loadContent() {
	
		try {
			return Files.readAllBytes(Paths.get(this.filename));
					
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			System.err.println("[ERROR] Trying to load the file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}
		
		return new byte[0];
	}

	public String loadContentStr() {
	
		return new String(loadContent(), BINARY_CHARSET);
	}

	public void saveContent(byte[] content) {
		// TODO
	}

	public void saveContentStr(String content) {
	
		super.saveContentDirectly(content, BINARY_CHARSET);
	}
	
}