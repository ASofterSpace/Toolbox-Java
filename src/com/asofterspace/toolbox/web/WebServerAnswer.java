/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;


/**
 * This represents the data that a web server might send as answer
 * to a request
 *
 * @author Moya (a softer space, 2019)
 */
public interface WebServerAnswer {

	public long getContentLength();

	public String getPreferredCacheParadigm();

	public String getContentType();

	public byte[] getBinaryContent();

}
