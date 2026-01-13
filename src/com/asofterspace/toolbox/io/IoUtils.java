/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.CallbackWithString;
import com.asofterspace.toolbox.utils.StrUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
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

		// allow grabbing errors as well
		builder.redirectErrorStream(true);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(builder.start().getInputStream()))) {
			String curline = reader.readLine();

			while (curline != null) {
				result.add(curline);
				curline = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println("There was an I/O Exception while executing an external command: " + e);
		}

		return result;
	}

	/**
	 * Just execute a simple command, waiting until it returns
	 */
	public static void execute(String command) {
		/*
		try {
			Process process = Runtime.getRuntime().exec(command);
			try {
				process.waitFor();
			} catch (InterruptedException e2) {
				// well, stop waiting...
			}
		} catch (IOException e) {
			System.err.println("There was an I/O Exception while executing the external command '" + command + "' synchronously: " + e);
		}
		*/
		execute(command, null);
	}

	/**
	 * Just execute a simple command, waiting until it returns, and for each line call the callback
	 */
	public static void execute(String command, CallbackWithString callback) {
		/*
		try {
			Process process = Runtime.getRuntime().exec(command);

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String curline = reader.readLine();

				while (curline != null) {
					callback.call(curline);
					curline = reader.readLine();
				}
			} catch (IOException e) {
				System.err.println("There was an I/O Exception while executing an external command: " + e);
			}

		} catch (IOException e) {
			System.err.println("There was an I/O Exception while executing an external command synchronously: " + e);
		}
		*/

		List<String> input = new ArrayList<>();
		String[] commands = command.split(" ");
		for (String curCommand : commands) {
			input.add(curCommand);
		}

		ProcessBuilder builder = new ProcessBuilder(input);

		try {
			if (callback == null) {
				builder.start();
			} else {
				// we want to get stdout and stderr!
				builder.redirectErrorStream(true);

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(builder.start().getInputStream()))) {
					String curline = reader.readLine();

					while (curline != null) {
						callback.call(curline);
						curline = reader.readLine();
					}
				} catch (IOException e2) {
					throw e2;
				}
			}
		} catch (IOException e) {
			System.err.println("There was an I/O Exception while executing an external command: " + e);
		}
	}

	public static Process executeAsync(String command, String... arguments) throws IOException {
		List<String> args = new ArrayList<>();
		for (String arg : arguments) {
			args.add(arg);
		}
		return executeAsync(command, args);
	}

	public static Process executeAsync(String command, List<String> arguments) throws IOException {

		List<String> cmdAndArgs = new ArrayList<>();
		cmdAndArgs.add(command);
		cmdAndArgs.addAll(arguments);

		ProcessBuilder processBuilder = new ProcessBuilder(cmdAndArgs);

		processBuilder.redirectErrorStream(true);

		processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

		return processBuilder.start();
	}

	/**
	 * Just execute a simple command asynchronously
	 */
	public static Process executeAsync(String command) {
		try {
			return Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			System.err.println("There was an I/O Exception while executing an external command asynchronously: " + e);
		}
		return null;
	}

	public static Process executeAsyncInDir(String command, Directory dir) {
		if (dir == null) {
			return executeAsync(command);
		}
		try {
			return Runtime.getRuntime().exec(command, null, dir.getJavaFile());
		} catch (IOException e) {
			System.err.println("There was an I/O Exception while executing an external command in dir asynchronously: " + e);
		}
		return null;
	}

	/**
	 * Returns null (if no args are given) or all args concatenated with spaces
	 */
	public static String assembleArgumentsIntoOne(String[] args) {
		if (args.length < 1) {
			return null;
		}

		if (args.length == 1) {
			if ("".equals(args[0])) {
				return null;
			}
			return args[0];
		}

		// when several arguments are given, assume that it is just one file whose name contains spaces
		StringBuilder result = new StringBuilder();
		String sep = "";
		for (int i = 0; i < args.length; i++) {
			result.append(sep);
			result.append(args[i]);
			sep = " ";
		}
		String resStr = result.toString();
		if ("".equals(resStr)) {
			return null;
		}
		return resStr;
	}

	public static void shutdownOS() {
		execute("shutdown -s");
	}

	public static void restartOS() {
		execute("shutdown -r");
	}

	public static String canonicalizePath(String path) {
		File file = new File(path);
		return file.getCanonicalFilename();
	}

	/**
	 * Makes file and folder names safe inside the given entryPoint (optionally recursively
	 * inside as well)
	 * Making safe includes:
	 * - removing the # sign
	 * - replaceing the ' sign with -
	 * - removing leading and trailing space characters
	 * Returns true if at least one replacement was made, false otherwise
	 */
	public static boolean makeFileAndFolderNamesSafe(Directory entryPoint, boolean recursively) {
		java.io.File entryPointAsJavaFile = entryPoint.getJavaFile();
		return makeFileAndFolderNamesSafeInternally(entryPointAsJavaFile, recursively);
	}

	private static boolean makeFileAndFolderNamesSafeInternally(java.io.File entryPoint, boolean recursively) {
		boolean result = false;
		java.io.File[] children = entryPoint.listFiles();
		Directory curParentDir = null;
		if (children != null) {
			for (java.io.File curChild : children) {
				String oldName = curChild.getName();
				if (oldName != null) {
					String adjustedName = oldName;
					adjustedName = StrUtils.replaceAll(adjustedName, "'", "-");
					adjustedName = StrUtils.replaceAll(adjustedName, "#", "");
					adjustedName = adjustedName.trim();
					if (!oldName.equals(adjustedName)) {
						if (curParentDir == null) {
							File oldFile = new File(curChild);
							curParentDir = oldFile.getParentDirectory();
						}
						File newFile = new File(curParentDir, adjustedName);
						curChild.renameTo(newFile.getJavaFile());
						result = true;
					}
				}
				if (recursively && curChild.isDirectory()) {
					boolean innerResult = makeFileAndFolderNamesSafeInternally(curChild, recursively);
					result = (result || innerResult);
				}
			}
		}
		return result;
	}

}
