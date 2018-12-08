/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * This (hopefully) simplifies access to local web resources
 *
 * @author Moya (a softer space, 2018)
 */
public class WebPreviewer {

	public static void openLocalFileInBrowser(String previewFileName) {

		try {
			String absolutePath = new java.io.File(previewFileName).getCanonicalPath().toString().replace("\\", "/");

			if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI("file:///" + absolutePath));
			}
		} catch (IOException | URISyntaxException e) {
			System.err.println("[ERROR] trying to open the local file " + previewFileName + " in a browser resulted in an I/O Exception - not quite inconceivable!");
		}
	}

}
