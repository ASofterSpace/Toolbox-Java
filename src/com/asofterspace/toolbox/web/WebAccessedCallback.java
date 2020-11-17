/**
 * Unlicensed code created by A Softer Space, 2017
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

/**
 * This interface describes a generic callback for having gotten content from the web
 *
 * @author Moya (a softer space, 2017)
 */
public interface WebAccessedCallback {

	/**
	 * An error occurred during retrieval
	 */
	void gotError();

	/**
	 * The requested content has been retrieved
	 * @param content  The content that was requested
	 */
	void gotContent(String content);

	/**
	 * We got a response code from the web resource
	 */
	void gotResponseCode(Integer code);
}
