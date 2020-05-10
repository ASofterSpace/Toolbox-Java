/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

/**
 * Interface that anything can implement that is a callback which receives some status
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public interface CallbackWithStatus {

	void call(int status);
}
