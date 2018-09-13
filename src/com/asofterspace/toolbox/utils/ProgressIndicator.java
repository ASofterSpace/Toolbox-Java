package com.asofterspace.toolbox.utils;

/**
 * Interface that anything can implement that wants to indicate some progress
 *
 * @author Moya (a softer space), 2018
 */
public interface ProgressIndicator {

	/**
	 * Set the current progress to a number between 0 and 1
	 */
	public void setProgress(double currentProgress);
	
	/**
	 * We are done with setting the progress (please do not call setProgress anymore afterwards!)
	 */
	public void done();

}
