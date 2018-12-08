/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZippedFile {

	private String name;
	
	private File unzippedFile;


	public ZippedFile(String name, File unzippedFile) {
	
		this.name = name;
		
		this.unzippedFile = unzippedFile;
	}
	
	public String getName() {
		return name;
	}
	
	public File getUnzippedFile() {
		return unzippedFile;
	}
	
	public XmlFile getUnzippedFileAsXml() {
		return new XmlFile(unzippedFile);
	}

}
