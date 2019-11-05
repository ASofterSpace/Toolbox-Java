/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.io.IOException;


/**
 * A utility class for interacting with processes
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public class ProcessUtils {

	public static Process startProcess(String command, String argument) throws IOException {

		ProcessBuilder processBuilder = new ProcessBuilder(command, argument);

		processBuilder.redirectErrorStream(true);

		processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

		return processBuilder.start();
	}

}
