/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A utility class for interacting with processes
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public class ProcessUtils {

	public static Process startProcess(String command, String... arguments) throws IOException {
		List<String> args = new ArrayList<>();
		for (String arg : arguments) {
			args.add(arg);
		}
		return startProcess(command, args);
	}

	public static Process startProcess(String command, List<String> arguments) throws IOException {

		List<String> cmdAndArgs = new ArrayList<>();
		cmdAndArgs.add(command);
		cmdAndArgs.addAll(arguments);

		ProcessBuilder processBuilder = new ProcessBuilder(cmdAndArgs);

		processBuilder.redirectErrorStream(true);

		processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

		return processBuilder.start();
	}

}
