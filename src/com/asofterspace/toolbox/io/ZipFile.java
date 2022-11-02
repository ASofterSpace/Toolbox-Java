/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * A zip file object describes a single zip file that usually contains a whole
 * file hierarchy inside and enables simple access to its contents.
 */
public class ZipFile extends File {

	private List<ZippedFile> zippedFiles;

	private Directory workdir = null;


	/**
	 * You can construct a ZipFile instance by directly from a path name.
	 */
	public ZipFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a ZipFile instance by basing it on an existing file object.
	 */
	public ZipFile(File regularFile) {

		super(regularFile);
	}

	/**
	 * You can construct a ZipFile instance by basing it on a directory and a filename.
	 */
	public ZipFile(Directory parentDirectory, String filename) {

		super(parentDirectory, filename);
	}

	private void createWorkDir() {

		// cleanup the previous workdir - if there has been one so far
		cleanupWorkDir();

		// create a new work dir dedicated to this particular zip file
		workdir = IoUtils.createDedicatedWorkDir();
	}

	private void cleanupWorkDir() {

		if (workdir != null) {

			workdir.delete();

			workdir = null;
		}
	}

	/**
	 * For a ZIP file, call getZippedFiles() to get access to its contents,
	 * not getContents() / setContents() as for a regular File (the
	 * regular File-based stuff will work, technically, but will be
	 * much less efficient and if you use both all hell might break
	 * loose... so yeah, only use the getZippedFiles() function as entry-
	 * point for ZIP files, kthxbye!)
	 */
	public List<ZippedFile> getZippedFiles() {

		if (zippedFiles == null) {
			loadZipContents();
		}

		return zippedFiles;
	}

	/**
	 * Get one particular file based on the relative path inside the zip file
	 */
	public ZippedFile getZippedFile(String zipRelativePath) {

		if (zippedFiles == null) {
			loadZipContents();
		}

		for (ZippedFile zippedFile : zippedFiles) {
			if (zippedFile.getName().equals(zipRelativePath)) {
				return zippedFile;
			}
		}

		return null;
	}

	/**
	 * Add one particular file to the zip file, storing it in a relative path
	 * which identifies the internal folder only (!), but does NOT include the filename!
	 * The zipRelativePath should NOT start with a slash/backslash, but it may or may not
	 * end on one.
	 */
	public void addZippedFile(File fileToAdd, String zipRelativePath) {

		if (fileToAdd == null) {
			return;
		}

		if (zipRelativePath == null) {
			return;
		}

		if (!zipRelativePath.endsWith("/") && !zipRelativePath.endsWith("\\")) {
			zipRelativePath += "/";
		}

		if (zippedFiles == null) {
			loadZipContents();
		}

		Directory addFileInDirectory = workdir.createChildDir(zipRelativePath);

		File addedFile = fileToAdd.copyToDisk(addFileInDirectory);

		zippedFiles.add(new ZippedFile(zipRelativePath + addedFile.getLocalFilename(), addedFile));
	}

	protected void loadZipContents() {

		// create a new workdir for this loading
		createWorkDir();

		List<ZippedFile> result = new ArrayList<>();

		try (ZipInputStream data = new ZipInputStream(new FileInputStream(filename))) {

			ZipEntry entry = null;

			try {

				while (true) {

					entry = data.getNextEntry();

					if (entry == null) {
						break;
					}

					if (entry.isDirectory()) {

						workdir.createChildDir(entry.getName());

					} else {

						String filepath = workdir.getDirname() + "/" + entry.getName();

						File unzippedFile = new File(filepath);

						unzippedFile.createParentDirectory();

						BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(filepath));

						byte[] buffer = new byte[65536];

						int readResult = 0;

						while ((readResult = data.read(buffer)) != -1) {
							output.write(buffer, 0, readResult);
						}
						output.close();

						result.add(new ZippedFile(entry.getName(), unzippedFile));
					}

					data.closeEntry();
				}

			} finally {
				data.closeEntry();
			}

		} catch (IOException e) {
			System.err.println("Ooops! The zip file " + filename + " could not be opened as something is wrong:\n" + e);
		}

		zippedFiles = result;
	}

	public void saveTo(String newLocation) {

		java.io.File outputFile = new java.io.File(newLocation);

		try (ZipOutputStream data = new ZipOutputStream(new FileOutputStream(outputFile))) {

			if (zippedFiles == null) {
				loadZipContents();
			}

			for (ZippedFile zippedFile : zippedFiles) {

				data.putNextEntry(new ZipEntry(zippedFile.getName()));

				byte[] binaryContent = Files.readAllBytes(zippedFile.getUnzippedFile().getJavaPath());

				data.write(binaryContent, 0, binaryContent.length);

				data.closeEntry();
			}

		} catch (IOException e) {
			System.err.println("Ooops! The zip file " + filename + " could not be created as something is wrong:\n" + e);
		}
	}

	/**
	 * Aaaalways close ZipFiles after use - as this deletes their workdir; otherwise they will stay
	 * lingering unzipped forever! (Or, well, until the next restart - at startup, we clean the workdir... ^^)
	 */
	public void close() {
		cleanupWorkDir();
	}

	/**
	 * Gives back a string representation of the zip file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.ZipFile: " + filename;
	}

}
