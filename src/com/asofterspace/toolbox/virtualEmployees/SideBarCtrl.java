/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.virtualEmployees;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.IoUtils;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.projects.GenericProject;
import com.asofterspace.toolbox.projects.GenericProjectCtrl;
import com.asofterspace.toolbox.web.WebServerAnswer;
import com.asofterspace.toolbox.web.WebServerAnswerInJson;

import java.util.ArrayList;
import java.util.List;


public class SideBarCtrl {

	public static String getSidebarHtmlStr() {
		List<SideBarEntry> leaveOut = new ArrayList<>();
		return getSidebarHtmlStr(leaveOut);
	}

	public static String getSidebarHtmlStr(SideBarEntry leaveOut) {
		List<SideBarEntry> leaveOuts = new ArrayList<>();
		leaveOuts.add(leaveOut);
		return getSidebarHtmlStr(leaveOuts);
	}

	public static String getSidebarHtmlStr(List<SideBarEntry> leaveOut) {

		StringBuilder html = new StringBuilder();
		StringBuilder script = new StringBuilder();
		script.append("\n<script>\n");

		int top = 10;
		int topDistance = 82;
		int entry = 1;

		if (!leaveOut.contains(SideBarEntry.HUGO)) {
			html.append("<a class=\"sidebar\" id=\"sidebar_" + entry + "\" title=\"Hugo\" href=\"http://localhost:3012/\" style=\"top: " + top + "pt;\">\n");
			html.append("<img class=\"avatar\" src=\"/pics/hugo.jpg\" />\n");
			html.append("</a>\n");
			script.append("document.getElementById('sidebar_" + entry + "').href = \"http://\" + window.location.hostname + \":3012/\";\n");
			entry++;
			top += topDistance;
		}

		if (!leaveOut.contains(SideBarEntry.MARI)) {
			html.append("<a class=\"sidebar\" id=\"sidebar_" + entry + "\" title=\"Mari\" href=\"http://localhost:3011/\" style=\"top: " + top + "pt; transform: scaleX(-1);\">\n");
			html.append("<img class=\"avatar\" src=\"/pics/mari.jpg\" />\n");
			html.append("</a>\n");
			script.append("document.getElementById('sidebar_" + entry + "').href = \"http://\" + window.location.hostname + \":3011/\";\n");
			entry++;
			top += topDistance;
		}

		if (!leaveOut.contains(SideBarEntry.ZARA)) {
			html.append("<a class=\"sidebar\" id=\"sidebar_" + entry + "\" title=\"Zara\" href=\"http://localhost:3014/\" style=\"top: " + top + "pt; transform: scaleX(-1);\">\n");
			html.append("<img class=\"avatar\" src=\"/pics/zara.jpg\" />\n");
			html.append("</a>\n");
			script.append("document.getElementById('sidebar_" + entry + "').href = \"http://\" + window.location.hostname + \":3014/\";\n");
			entry++;
			top += topDistance;
		}


		int bottom = 10;
		int bottomDistance = 46;

		if (!leaveOut.contains(SideBarEntry.WORKBENCH)) {
			html.append("<a class=\"sidebar\" id=\"sidebar_" + entry + "\" onmouseover=\"_ve_showProjects()\" onmouseleave=\"_ve_hideProjects()\" href=\"http://localhost:3010/\" target=\"_blank\" style=\"bottom: " + bottom + "pt;\">\n");
			html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/workbench.png\" />\n");
			html.append("</a>\n");
			script.append("window._ve_showProjects = function() {\n");
			script.append("    document.getElementById('_ve_projectbar').style.display = 'block';\n");
			script.append("}\n");
			script.append("window._ve_hideProjects = function() {\n");
			script.append("    document.getElementById('_ve_projectbar').style.display = 'none';\n");
			script.append("}\n");
			script.append("document.getElementById('sidebar_" + entry + "').href = \"http://\" + window.location.hostname + \":3010/\";\n");
			entry++;

			html.append("<div class=\"projectbar\" id=\"_ve_projectbar\" onmouseover=\"_ve_showProjects()\" onmouseleave=\"_ve_hideProjects()\" style=\"display:none; bottom:53pt; right:8pt; padding:0pt 4pt 5pt 0pt; z-index:100; background-color: rgba(0, 0, 0, 0.7);\">");

			GenericProjectCtrl projectCtrl = new GenericProjectCtrl(
				System.getProperty("java.class.path") + "/../../assWorkbench/server/projects");
			List<GenericProject> projects = projectCtrl.getGenericProjects();

			for (GenericProject proj : projects) {
				if (proj.isOnShortlist()) {
					html.append("\n");
					html.append("  <a href=\"localhost:3010/projects/" + proj.getShortName() + "/?open=logbook\" target=\"_blank\" class=\"project\" style=\"border-color: " + proj.getColor().toHexString() + "\">");
					html.append("    <span class=\"vertAligner\"></span><img src=\"projectlogos/" + proj.getShortName() + "/logo.png\" />");
					html.append("  </a>");
				}
			}

			html.append("</div>");

			bottom += bottomDistance;
		}

		html.append("<a class=\"sidebar\" id=\"sidebar_" + entry + "\" href=\"http://localhost:3013/\" ");
		// when we are currently on the browser page, instead of leaving it out, we change the default behavior
		// to opening another one in a new tab when clicked
		if (leaveOut.contains(SideBarEntry.BROWSER)) {
			html.append("target=\"_blank\" ");
		}
		html.append("style=\"bottom: " + bottom + "pt;\">\n");
		html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/browser.png\" />\n");
		html.append("</a>\n");
		script.append("document.getElementById('sidebar_" + entry + "').href = \"http://\" + window.location.hostname + \":3013/\";\n");
		entry++;
		bottom += bottomDistance;

		if (!leaveOut.contains(SideBarEntry.EDITOR)) {
			script.append("window._ve_openLocally = function(whatToOpen) {\n");
			script.append("    var request = new XMLHttpRequest();\n");
			script.append("    request.open(\"POST\", \"_ve_openLocally\", true);\n");
			script.append("    request.setRequestHeader(\"Content-Type\", \"application/json\");\n");
			script.append("    request.send('{\"whatToOpen\": \"' + whatToOpen + '\"}');\n");
			script.append("}\n");
			html.append("<div class=\"sidebar\" onclick=\"window._ve_openLocally('editor')\" style=\"bottom: " + bottom + "pt; top: unset;\">\n");
			html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/editor.png\" />\n");
			html.append("</div>\n");
			bottom += bottomDistance;
		}

		script.append("</script>\n");
		html.append(script);

		return html.toString();
	}

	private static String getBasePath() {
		return System.getProperty("java.class.path") + "/../../";
	}

	/**
	 * Returns an image file from the side bar as answer to a GET request, if the GET request has
	 * such an image as target, and null otherwise
	 */
	public static File getSideBarImageFile(String location) {

		File result = null;

		String basePath = getBasePath();

		if (location.equals("/pics/hugo.jpg")) {
			result = new File(basePath + "assSecretary/server/pics/hugo.jpg");
		}

		if (location.equals("/pics/mari.jpg")) {
			result = new File(basePath + "assAccountant/server/pics/mari.jpg");
		}

		if (location.equals("/pics/zara.jpg")) {
			result = new File(basePath + "assTrainer/server/pics/zara.jpg");
		}

		if (location.equals("/pics/workbench.png")) {
			result = new File(basePath + "assWorkbench/server/pics/workbench.png");
		}

		if (location.equals("/pics/browser.png")) {
			result = new File(basePath + "assBrowser/server/pics/browser.png");
		}

		if (location.equals("/pics/editor.png")) {
			result = new File(basePath + "assEditor/res/ico.png");
		}

		// get project logo files from assWorkbench
		if (location.startsWith("/projectlogos/") && location.endsWith(".png") && !location.contains("..")) {
			String filename = location.substring("/projectlogos/".length());
			filename = basePath + "assWorkbench/server/projects/" + filename;
			result = new File(filename);
		}

		if ((result != null) && result.exists()) {
			return result;
		}
		return null;
	}

	public static WebServerAnswer handlePost(String fileLocation, String jsonData) {

		if ((fileLocation == null) || (jsonData == null)) {
			return null;
		}

		if (!fileLocation.startsWith("/")) {
			fileLocation = "/" + fileLocation;
		}

		String basePath = getBasePath();

		try {
			switch (fileLocation) {
				case "/_ve_openLocally":
					JSON json = new JSON(jsonData);
					if ("editor".equals(json.getString("whatToOpen"))) {
						IoUtils.execute(basePath + "assEditor/assEditor.bat");
					}
					return new WebServerAnswerInJson("{\"success\": true}");
			}
		} catch (JsonParseException e) {
			// whoops
		}

		return null;
	}

}
