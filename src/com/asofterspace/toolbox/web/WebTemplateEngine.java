/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.StrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * This is a templating engine written for simplifying the creation of websites
 *
 * @author Moya (a softer space, 2019)
 */
public class WebTemplateEngine {

	private Directory origDir;

	private Record config;

	private Random randGen;

	// keep this as global variable such that isTrue can later on check against it
	private String currentFile;

	private static String startTimeStamp = null;


	public WebTemplateEngine(Directory origDir, Record config) {

		this.origDir = origDir;

		this.config = config;
	}

	public Record getConfig() {
		return config;
	}

	public boolean compileTo(Directory targetDir) {
		return compileTo(targetDir, "", true);
	}

	/**
	 * Return true if the compilation succeeded
	 * TODO :: add more checks to ensure the compilation was actually successful!
	 */
	public boolean compileTo(Directory targetDir, String contentkind, boolean convertPhpToHtm) {

		boolean result = true;

		Record files = config.get("files");

		if (files != null) {
			int fileAmount = files.getLength();

			for (int i = 0; i < fileAmount; i++) {

				currentFile = files.getString(i);

				// in case we have an entry such as /foo/bar/*,
				// just copy everything inside the  bar  folder without applying any conversions to it
				// (this is supposed to be used for basically folders consisting of lots of files
				// without any templating)
				if (currentFile.endsWith("/*")) {
					String dirName = currentFile.substring(0, currentFile.length() - 2);
					Directory parentDir = new Directory(origDir, dirName);
					parentDir.copyToDisk(new Directory(targetDir, dirName));
					continue;
				}

				SimpleFile indexIn = new SimpleFile(origDir, currentFile);

				if (!indexIn.exists()) {
					System.err.println("Could not find " + indexIn.getAbsoluteFilename() + "!");
					result = false;
				}

				if (isWebTextFile(indexIn)) {

					String content = indexIn.getContent();

					boolean isHtml = currentFile.endsWith(".htm") || currentFile.endsWith(".html");
					boolean isPhp = currentFile.endsWith(".php");
					boolean isCss = currentFile.endsWith(".css");

					// perform the PHP templating (both for regular HTML files and for PHP files)
					if (isHtml || isPhp) {
						content = compilePhp(content, contentkind);
					}

					// convert PHP to HTML - if we have a PHP file, and if the conversion is wanted
					if (isPhp && convertPhpToHtm) {
						content = removePhp(content);

						content = makeLinksRelative(content);

						currentFile = currentFile.substring(0, currentFile.length() - 4) + ".htm";
					}
					if (isCss && convertPhpToHtm) {
						content = makeCssLinksRelative(content);
					}

					SimpleFile indexOut = new SimpleFile(targetDir, currentFile);

					indexOut.saveContent(content);

				} else {

					indexIn.copyToDisk(new File(targetDir, currentFile));
				}
			}
		}

		// the WebEngine increases the version on its own, but if the caller does not save this new version
		// in the configuration that was passed in, then it will be lost when the surrounding program is
		// closed and re-started
		config.inc("version");

		return result;
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

		// insert glossaries, when requested (which contains @includes, so has to come before the performTemplating step)
		content = insertGlossaries(content, contentkind);

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

		// we could now do two simple replacealls for href= like this:
		//   content = content.replaceAll(" href=\"/\"", " href=\"index.htm\"");
		//   content = content.replaceAll(" href=\"/", " href=\"");
		// ... but instead we step over all matches manually, such that
		// we can add missing .htm extensions for the local preview if necessary
		StringBuilder contentBuilder = new StringBuilder();
		int curPos = 0;
		int endPos = 0;
		while (true) {
			curPos = content.indexOf(" href=\"", curPos);
			if (curPos < 0) {
				contentBuilder.append(content.substring(endPos));
				break;
			}
			curPos += 7;
			contentBuilder.append(content.substring(endPos, curPos));
			endPos = content.indexOf("\"", curPos);
			if (endPos < curPos) {
				endPos = curPos;
			}
			String midStr = content.substring(curPos, endPos);
			if (midStr.startsWith("/")) {
				midStr = midStr.substring(1);
			}
			// if we have href="/", we do not want to set href="",
			// but instead href="index.htm"
			if (midStr.equals("")) {
				midStr = "index.htm";
			}
			if (!midStr.contains(".")) {
				midStr += ".htm";
			}
			contentBuilder.append(midStr);
		}
		content = contentBuilder.toString();

		content = content.replaceAll(" src=\"/", " src=\"");

		// we ALSO make CSS links relative, as there could be embedded CSS here!
		content = makeCssLinksRelative(content);

		return content;
	}

	private String makeCssLinksRelative(String content) {

		content = content.replaceAll(" url\\('/", " url\\('");

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

		Record contentConfig = config.get("content" + contentkind);

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

	private String contentKindToLang(String contentKind) {

		if (contentKind.equals("")) {
			return "en";
		}

		return contentKind;
	}

	private String insertGlossaries(String content, String contentKind) {

		final String contentLang = contentKindToLang(contentKind);

		List<String> encounteredKeys = new ArrayList<>();
		List<String> encounteredLinks = new ArrayList<>();

		while (content.contains("@glossary(")) {

			int atIndex = content.indexOf("@glossary(");

			String beforeContent = content.substring(0, atIndex);

			String contentKey = content.substring(atIndex + 10, content.length());

			atIndex = contentKey.indexOf(")");

			String afterContent = contentKey.substring(atIndex + 1);

			contentKey = contentKey.substring(0, atIndex);

			JsonFile glossary = new JsonFile(origDir, contentKey);
			try {
				Record glossaryRec = glossary.getAllContents();
				Record categories = glossaryRec.get("categories");
				List<Record> categoriesWithTerms = new ArrayList<>();

				StringBuilder html = new StringBuilder();

				// this one contains the keys in plaintext (not after calling glossaryKeyToId() on them),
				// and we use them to magically add links that did not previously exist to descriptions
				List<String> allKeys = new ArrayList<>();

				for (int i = 0; i < categories.size(); i++) {
					Record category = categories.get(i);
					List<Record> terms = category.getArray("terms");
					boolean addedAKey = false;
					for (int j = 0; j < terms.size(); j++) {
						Record term = terms.get(j);
						// ignore keys which contain no explanation at all
						if (term.getString("content_" + contentLang).equals("")) {
							continue;
						}
						encounteredKeys.add(glossaryKeyToId(term.getString("name_" + contentLang)));
						allKeys.add(term.getString("name_" + contentLang));
						addedAKey = true;
					}
					if (addedAKey) {
						categoriesWithTerms.add(category);
					}
				}

				// second iteration - now we actually add the keys and descriptions and so on, and actually
				// generate the html
				for (int i = 0; i < categoriesWithTerms.size(); i++) {

					Record category = categoriesWithTerms.get(i);

					html.append("@include(sectionstart.php)");
					html.append("<h1>" + category.getString("name_" + contentLang) + "</h1>");

					List<Record> terms = category.getArray("terms");

					Collections.sort(terms, new Comparator<Record>() {
						public int compare(Record a, Record b) {
							return a.getString("name_" + contentLang).toLowerCase().compareTo(
								   b.getString("name_" + contentLang).toLowerCase());
						}
					});

					for (int j = 0; j < terms.size(); j++) {
						Record term = terms.get(j);

						String curContent = term.getString("content_" + contentLang);

						// ignore keys which contain no explanation at all
						if (curContent.equals("")) {
							continue;
						}

						// simplify replacement for adding links on the fly
						curContent = " " + curContent + " ";

						// add links on the fly
						for (String onTheFlyLinkKey : allKeys) {
							// but to not add links to an entry itself in its own description
							if (onTheFlyLinkKey.equals(term.getString("name_" + contentLang))) {
								continue;
							}
							curContent = StrUtils.replaceAll(curContent, " " + onTheFlyLinkKey + " ",
								" @link(" + onTheFlyLinkKey + ") ");
							curContent = StrUtils.replaceAll(curContent, " " + onTheFlyLinkKey + "'s ",
								" @link(" + onTheFlyLinkKey + ")'s ");
							curContent = StrUtils.replaceAll(curContent, " " + onTheFlyLinkKey + ")",
								" @link(" + onTheFlyLinkKey + "))");
							curContent = StrUtils.replaceAll(curContent, " " + onTheFlyLinkKey + ",",
								" @link(" + onTheFlyLinkKey + "),");
						}

						// convert links to actual hyperlinks
						while (curContent.contains("@link(")) {
							int start = curContent.indexOf("@link(");
							String midContent = curContent.substring(start + 6);
							curContent = curContent.substring(0, start);
							int end = midContent.indexOf(")");
							String endContent = "";
							if (end >= 0) {
								endContent = midContent.substring(end + 1);
								midContent = midContent.substring(0, end);
							}

							String linkId = glossaryKeyToId(midContent);

							// if the link as-is cannot be found...
							if (!encounteredKeys.contains(linkId)) {
								// ... and we have a plural form...
								if (midContent.endsWith("s") || midContent.endsWith("n")) {
									// ... and the singular does exist...
									if (encounteredKeys.contains(linkId.substring(0, linkId.length() - 1))) {
										// ... link to that one instead!
										linkId = linkId.substring(0, linkId.length() - 1);
									}
								}
							}
							encounteredLinks.add(linkId);
							midContent = "<a href='#" + linkId + "'>" + midContent + "</a>";
							curContent = curContent + midContent + endContent;
						}

						curContent = curContent.trim();

						html.append("<div class=\"content\" id=\"" +
							glossaryKeyToId(term.getString("name_" + contentLang)) + "\">");
						html.append("<b>");
						html.append(term.getString("name_" + contentLang));
						html.append("</b>");
						html.append(" .. ");
						html.append(curContent);
						html.append("</div>");
					}
					html.append("@include(sectionend.php)");
				}

				content = beforeContent + html.toString() + afterContent;

			} catch (JsonParseException e) {
				System.err.println("The contents of " + glossary.getAbsoluteFilename() + " could not be read!");
			}
		}

		StringBuilder nonWorkingLinks = new StringBuilder();
		String sep = "";
		outerLoop:
		for (String link : encounteredLinks) {
			for (String key : encounteredKeys) {
				if (link.equals(key)) {
					continue outerLoop;
				}
			}
			nonWorkingLinks.append(sep);
			sep = ", ";
			nonWorkingLinks.append(link);
		}
		String nonWorkingLinkStr = nonWorkingLinks.toString();
		if (nonWorkingLinkStr.length() > 0) {
			System.err.println("Links encountered in " + contentLang + " language which did not lead to " +
				"keys actually present in the glossary:");
			System.err.println(nonWorkingLinkStr);
		}

		return content;
	}

	private String glossaryKeyToId(String key) {
		key = key.toLowerCase();
		key = StrUtils.replaceAll(key, " ", "");
		return key;
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

		if (startTimeStamp == null) {
			startTimeStamp = StrUtils.replaceAll(StrUtils.replaceAll(StrUtils.replaceAll(StrUtils.replaceAll(
				DateUtils.serializeDateTime(DateUtils.now()), " ", ""), "-", ""), ".", ""), ":", "");
		}

		while (content.contains("@startTimeStamp")) {
			content = content.replaceAll("@startTimeStamp", ""+startTimeStamp);
		}

		return content;
	}
}
