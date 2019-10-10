/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.test;

/**
 * Interface that test classes can implement such that test suites
 * can run them consecutively
 *
 * @author Moya (a softer space), 2018
 */
public interface Test {

	void runAll() throws Throwable;
}
