/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;


public class JsonParseException extends Exception {

	public static final long serialVersionUID = 34853945723402l;


	public JsonParseException(String message) {
		super(message);
	}

	public JsonParseException(String message, Throwable previousException) {
		super(message, previousException);
	}
}
