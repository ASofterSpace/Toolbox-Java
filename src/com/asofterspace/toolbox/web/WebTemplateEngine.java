/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.SimpleFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * This is a templating engine written for simplifying the creation of websites
 *
 * @author Moya (a softer space, 2019)
 */
public class WebTemplateEngine {

	private Directory origDir;

	private JSON config;

	private Random randGen;

	// keep this as global variable such that isTrue can later on check against it
	private String currentFile;


	public WebTemplateEngine(Directory origDir, JSON config) {

		this.origDir = origDir;

		this.config = config;
	}

	public void compileTo(Directory targetDir, String contentkind, boolean convertPhpToHtm) {

		JSON files = config.get("files");

		int fileAmount = files.getLength();

		for (int i = 0; i < fileAmount; i++) {

			currentFile = files.getString(i);

			SimpleFile indexIn = new SimpleFile(origDir, currentFile);

			if (isWebTextFile(indexIn)) {

				String content = indexIn.getContent();

				if (currentFile.endsWith(".php")) {

					content = compilePhp(content, contentkind);

					if (convertPhpToHtm) {
						content = removePhp(content);

						content = makeLinksRelative(content);

						currentFile = currentFile.substring(0, currentFile.length() - 4) + ".htm";
					}
				}

				SimpleFile indexOut = new SimpleFile(targetDir, currentFile);

				indexOut.saveContent(content);

			} else {

				indexIn.copyToDisk(new File(targetDir, currentFile));
			}

		}
	}

	/**
	 * Returns true for PHP, Javascript, CSS, etc. files
	 * Returns false otherwise - e.g. for JPEG files
	 */
	private boolean isWebTextFile(File currentFile) {

		String path = currentFile.getFilename().toLowerCase();

		return path.endsWith(".php") ||
			   path.endsWith(".js") ||
			   path.endsWith(".json") ||
			   path.endsWith(".htm") ||
			   path.endsWith(".html") ||
			   path.endsWith(".css");
	}

	/**
	 * Perform the compilation / templating of a PHP file
	 * @param content  a string containing PHP source code
	 * @return the same string after templating
	 */
	private String compilePhp(String content, String contentkind) {

		// first of all, remove templating comments - so if someone has {{-- @include(bla) --}}, then do not even include
		content = removeTemplatingComments(content);

		// now perform the templating (meaning to actually follow up on those @includes)
		content = performTemplating(content);

		// aaaand remove comments again, so {{-- --}}
		content = removeTemplatingComments(content);

		// insert special content texts, such as @content(blubb)
		content = insertContentText(content, contentkind);

		// insert stuff based on ifs (maybe we should move the ifs further up, but then
		// we would need to check again and again if by e.g. following new templating,
		// new ifs have been uncovered...), such as @if(page="index.php"), @if(page!="index.php"),
		// and @if(pageStart="index")
		content = insertIfEndIfs(content);

		// insert random numbers, such as @rand(randnum)
		content = insertRandomNumbers(content);

		// insert counted numbers, such as @countup(countername)
		// (do this after ifEndIfs, such that if stuff was taken out by ifs,
		// it is not counted)
		content = insertCountedNumbers(content);

		// remove whitespace and empty lines please!
		content = removeWhitespaceAndEmptyLines(content);

		// insert version
		Integer currentVersion = config.getInteger("version");
		content = insertNewVersion(content, currentVersion);

		// return result
		return content;
	}

	/**
	 * Removes all PHP tags from a PHP file to convert it into
	 * regular HTML, e.g. for local previews
	 * @param content  a string containing PHP source code
	 * @return the same string without the PHP tags and their contents
	 */
	private String removePhp(String content) {

		// TODO :: improve (right now, this ignores comments, strings etc.)

		while (content.contains("<?php")) {

			String contentBefore = content.substring(0, content.indexOf("<?php"));

			String contentAfter = content.substring(content.indexOf("<?php"));
			contentAfter = contentAfter.substring(contentAfter.indexOf("?>") + 2);

			content = contentBefore + contentAfter;
		}

		// also replace xyz.php links with xyz.htm links (such that local links also work within the preview)
		content = content.replaceAll(".php", ".htm");

		return content;
	}

	/**
	 * Takes in content containing stuff like href="/bla.htm"
	 * and transforms it into stuff like href="bla.htm" - this
	 * means that we can display anything that lies on the root
	 * path very easily locally as a preview (which is nice!),
	 * but makes it impossible for sub-pages to properly operate
	 * (oops...)
	 */
	private String makeLinksRelative(String content) {

		// if we have href="/", we do not want to set href="",
		// but instead href="index.htm"
		content = content.replaceAll(" href=\"/\"", " href=\"index.htm\"");

		content = content.replaceAll(" href=\"/", " href=\"");

		content = content.replaceAll(" src=\"/", " src=\"");

		return content;
	}

	/**
	 * Takes a file content containing @include(xyz) and replaces
	 * this with the content of xyz; continues as long as @include
	 * is present (so this can be done recursively, however infinite
	 * loops are not checked and will result in infinite running
	 * without a regular way to abort... oops!)
	 */
	private String performTemplating(String content) {

		while (content.contains("@include(")) {

			String contentBefore = content.substring(0, content.indexOf("@include("));

			String contentAfter = content.substring(content.indexOf("@include("));
			String contentMiddle = contentAfter.substring(9);
			contentMiddle = contentMiddle.substring(0, contentMiddle.indexOf(")"));
			contentAfter = contentAfter.substring(contentAfter.indexOf(")") + 1);

			SimpleFile includedFile = new SimpleFile(origDir, contentMiddle);

			content = contentBefore + includedFile.getContent() + contentAfter;
		}

		return content;
	}

	/**
	 * Takes a string containing source code and removes
	 * {{-- comments like these --}}
	 * @param content  a string containing source code
	 * @return the same string, but without {{-- comments --}}
	 */
	private String removeTemplatingComments(String content) {

		while (content.contains("{{--")) {

			String contentBefore = content.substring(0, content.indexOf("{{--"));

			String contentAfter = content.substring(content.indexOf("{{--"));
			contentAfter = contentAfter.substring(contentAfter.indexOf("--}}") + 4);

			content = contentBefore + contentAfter;
		}

		return content;
	}

	/**
	 * Takes a string containing source code and removes starting and
	 * trailing spaces as well as excessive newlines
	 * @param content  a string containing source code
	 * @return the same string but without excessive whitespace
	 */
	private String removeWhitespaceAndEmptyLines(String content) {

		while (content.startsWith(" ")) {
			content = content.substring(1);
		}

		while (content.contains(" \n") || content.contains("\t\n")) {
			content = content.replaceAll(" \n", "\n");
			content = content.replaceAll("\t\n", "\n");
		}

		while (content.contains("\n ") || content.contains("\n\t")) {
			content = content.replaceAll("\n ", "\n");
			content = content.replaceAll("\n\t", "\n");
		}

		while (content.endsWith(" ")) {
			content = content.substring(0, content.length() - 1);
		}

		while (content.contains("\n\n")) {
			content = content.replaceAll("\n\n", "\n");
		}

		return content;
	}

	/**
	 * Takes a string containing source code and inserts text
	 * replacing @content(foobar) placeholders
	 * @param content  a string containing source code
	 * @return the same string, but with placeholders filled
	 */
	private String insertContentText(String content, String contentkind) {

		JSON contentConfig = config.get("content" + contentkind);

		while (content.contains("@content(")) {

			int atIndex = content.indexOf("@content(");

			String beforeContent = content.substring(0, atIndex);

			String contentKey = content.substring(atIndex + 9, content.length());

			atIndex = contentKey.indexOf(")");

			String afterContent = contentKey.substring(atIndex + 1);

			contentKey = contentKey.substring(0, atIndex);

			content = beforeContent + contentConfig.getString(contentKey) + afterContent;
		}

		return content;
	}

	private String insertRandomNumbers(String content) {

		while (content.contains("@rand(")) {

			int atIndex = content.indexOf("@rand(");

			String beforeContent = content.substring(0, atIndex);

			String contentKey = content.substring(atIndex + 6, content.length());

			atIndex = contentKey.indexOf(")");

			String afterContent = contentKey.substring(atIndex + 1);

			contentKey = contentKey.substring(0, atIndex);

			int randValue = 0;

			if (randGen == null) {
				randGen = new Random();
			}

			try {
				int maxRand = Integer.parseInt(contentKey);
				randValue = randGen.nextInt(maxRand);

			} catch (NumberFormatException e) {
				System.err.println("Tried to interpret @rand(" + contentKey +
					"), but could not convert " + contentKey + " to integer!");
			}

			content = beforeContent + randValue + afterContent;
		}

		return content;
	}

	private String insertCountedNumbers(String content) {

		Map<String, Integer> counters = new HashMap<>();

		while (content.contains("@countup(")) {

			int atIndex = content.indexOf("@countup(");

			String beforeContent = content.substring(0, atIndex);

			String contentKey = content.substring(atIndex + 9, content.length());

			atIndex = contentKey.indexOf(")");

			String afterContent = contentKey.substring(atIndex + 1);

			contentKey = contentKey.substring(0, atIndex);

			int counterValue = 0;

			if (counters.containsKey(contentKey)) {
				counterValue = counters.get(contentKey) + 1;
			}

			counters.put(contentKey, counterValue);

			content = beforeContent + counterValue + afterContent;
		}

		return content;
	}

	private String insertIfEndIfs(String content) {

		while (content.contains("@if(")) {

			int atIndex = content.indexOf("@if(");

			String beforeContent = content.substring(0, atIndex);

			String contentKey = content.substring(atIndex + 4, content.length());

			atIndex = contentKey.indexOf(")");

			String afterContent = contentKey.substring(atIndex + 1);

			contentKey = contentKey.substring(0, atIndex);

			// TODO :: also enable else and elif and stuff like that!

			int endIndex = afterContent.indexOf("@endif");

			if (endIndex < 0) {
				System.err.println("@if(" + contentKey + ") is not closed!");
				return content;
			}

			String middleContent = afterContent.substring(0, endIndex);

			afterContent = afterContent.substring(endIndex + 6);

			if (isTrue(contentKey)) {
				content = beforeContent + middleContent + afterContent;
			} else {
				content = beforeContent + afterContent;
			}
		}

		return content;
	}

	private boolean isTrue(String expression) {
		if (expression.replace(" ", "").startsWith("page=")) {
			return expression.replace(" ", "").equals("page=\"" + currentFile + "\"");
		}
		if (expression.replace(" ", "").startsWith("page!=")) {
			return !expression.replace(" ", "").equals("page!=\"" + currentFile + "\"");
		}
		if (expression.replace(" ", "").startsWith("pageStart=")) {
			return ("pageStart=" + currentFile).startsWith(expression.replace(" ", "").replace("\"", ""));
		}
		if (expression.replace(" ", "").startsWith("pageStart!=")) {
			return !("pageStart!=" + currentFile).startsWith(expression.replace(" ", "").replace("\"", ""));
		}
		if ("true".equals(expression.trim())) {
			return true;
		}
		return false;
	}

	/**
	 * Takes a string containing source code and inserts incrementing
	 * text for @version placeholders
	 * @param content  a string containing source code
	 * @return the same string, but with @version placeholders replaced
	 */
	private String insertNewVersion(String content, Integer version) {

		while (content.contains("@version")) {
			content = content.replaceAll("@version", ""+version);
		}

		return content;
	}
}
