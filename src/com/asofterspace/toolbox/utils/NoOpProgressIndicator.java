/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

/**
 * For when you don't feel like indicating progress... heh!
 *
 * @author Moya (a softer space), 2018
 */
public class NoOpProgressIndicator implements ProgressIndicator {

	public void setProgress(double currentProgress) {
		// do nothing - this is a no-op!
	}
	
	public void done() {
		// do nothing - this is a no-op!
	}

}
