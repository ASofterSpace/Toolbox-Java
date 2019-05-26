/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class IoUtils {

	public static final Directory WORKDIR = new Directory("workdir");


	/**
	 * Cleanup the local workdir (which is used e.g. for temporarily keeping unzipped files...
	 * but there is no need to keep unzipped files from the last run of this program, in case
	 * somehow the files were not properly closed and deleted back then
	 */
	public static void cleanAllWorkDirs() {

		WORKDIR.delete();
	}

	public static Directory createDedicatedWorkDir() {

		UUID dedicatedId = UUID.randomUUID();

		return IoUtils.WORKDIR.createChildDir(dedicatedId.toString());
	}

	/**
	 * Takes a path like C:\a/b\c and transforms it into C:/a/b/c if we are under Windows,
	 * or keeps it as C:\a/b\c if we are under Linux (with the assumption that backslashes
	 * under Windows are separators which we want to convert to Linux separators)
	 */
	public static String osPathStrToLinuxPathStr(String path) {

		String sep = System.getProperty("file.separator");

		return path.replace(sep, "/");
	}

	/**
	 * Executes the given program synchronously
	 * and gives back the output of the external call
	 */
	public static List<String> execute(String program, Directory directory, String... arguments) {

		List<String> result = new ArrayList<>();

		List<String> input = new ArrayList<>();
		input.add(program);
		if (arguments != null) {
			for (String arg : arguments) {
				input.add(arg);
			}
		}

		ProcessBuilder builder = new ProcessBuilder(input);

		if (directory != null) {
			builder.directory(directory.getJavaFile());
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(builder.start().getInputStream()))) {
			String curline = reader.readLine();

			while (curline != null) {
				result.add(curline);
				curline = reader.readLine();
			}
		} catch (IOException e) {
			// oops!
		}

		return result;
	}

}
