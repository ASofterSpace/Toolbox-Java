/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.virtualEmployees;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.IoUtils;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.projects.GenericProject;
import com.asofterspace.toolbox.projects.GenericProjectCtrl;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.web.WebServerAnswer;
import com.asofterspace.toolbox.web.WebServerAnswerInJson;

import java.util.ArrayList;
import java.util.List;


public class SideBarCtrl {

	private static Record rootRecord = null;

	private static List<Record> virtualEmployeeRecords = null;


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


		// VIRTUAL EMPLOYEES

		List<Record> veRecords = getVirtualEmployeeRecords();

		int maxVirtualEmployees = 4;

		for (Record veRec : veRecords) {
			if (!leaveOut.contains(new SideBarEntryForEmployee(veRec.getString("name")))) {
				maxVirtualEmployees--;

				if (maxVirtualEmployees < 0) {
					break;
				}

				html.append("<a class=\"sidebar\" id=\"sidebar_" + entry + "\" title=\"" + veRec.getString("name") + "\" ");
				html.append("href=\"http://localhost:" + veRec.getInteger("port") + "/\" style=\"top: " + top + "pt;");
				if (veRec.getBoolean("flip", false)) {
					html.append(" transform: scaleX(-1);");
				}
				html.append("\">\n");
				html.append("<img class=\"avatar\" src=\"/pics/" + veRec.getString("name").toLowerCase() + ".jpg\" />\n");
				html.append("</a>\n");
				script.append("document.getElementById('sidebar_" + entry + "').href = ");
				script.append("\"http://\" + window.location.hostname + \":" + veRec.getInteger("port") + "/\";\n");
				entry++;
				top += topDistance;
			}
		}


		// BUTTON TO SHOW FULL LIST

		html.append("<div class='sidebar' style='top: " + top + "pt; text-align: center; background: rgba(255, 255, 255, 0.3); border-radius: 8pt; height: 10.5pt; font-weight: bold; padding-top: 4pt;' ");
		html.append("onMouseOver='document.getElementById(\"sidebar_full_list_container\").style.display = \"block\";' ");
		html.append("onMouseOut='document.getElementById(\"sidebar_full_list_container\").style.display = \"none\";' ");
		html.append(">");
		html.append("&lt; &lt; &lt;");
		html.append("</div>");

		html.append("<div id='sidebar_full_list_container' style='position: fixed; inset: 25pt 55pt 25pt 25pt; display: none;' ");
		html.append("onMouseOver='document.getElementById(\"sidebar_full_list_container\").style.display = \"block\";' ");
		html.append("onMouseOut='document.getElementById(\"sidebar_full_list_container\").style.display = \"none\";' ");
		html.append(">");
		html.append("<div style='position: fixed; inset: 30pt 90pt 30pt 30pt; background: rgba(255, 255, 255, 0.9); box-shadow: 0px 0px 8px 8px rgba(255, 255, 255, 0.9); border-radius: 16pt;'>");

		int LEFT_OFFSET = 18;

		int left = LEFT_OFFSET;

		for (Record veRec : veRecords) {
			html.append("<a class=\"sidebar\" id=\"sidebar_full_" + entry + "\" title=\"" + veRec.getString("name") + "\" ");
			html.append("href=\"http://localhost:" + veRec.getInteger("port") + "/\" style=\"left: " + left + "pt; top: 12pt; width: 65pt; color: #000; text-align: center;\">\n");
			html.append("<img class=\"avatar\" src=\"/pics/" + veRec.getString("name").toLowerCase() + ".jpg\" />\n");
			html.append("<br>\n");
			html.append(veRec.getString("name") + "\n");
			html.append("</a>\n");
			script.append("document.getElementById('sidebar_full_" + entry + "').href = ");
			script.append("\"http://\" + window.location.hostname + \":" + veRec.getInteger("port") + "/\";\n");

			left += 62+15;
		}


		left = LEFT_OFFSET;

		top = 225;

		html.append("<a class=\"sidebar\" id=\"sidebar_full_" + entry + "\" href=\"http://localhost:3013/\" target=\"_blank\" ");
		html.append("style=\"left: " + left + "pt; top: " + top + "pt; color: #000; text-align: center;\">\n");
		html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/browser.png\" />\n");
		html.append("<br>\n");
		html.append("Browser\n");
		html.append("</a>\n");
		script.append("document.getElementById('sidebar_full_" + entry + "').href = \"http://\" + window.location.hostname + \":3013/\";\n");

		left += 62;

		html.append("<div class=\"sidebar\" onclick=\"window._ve_openLocally('editor')\" style=\"left: " + left + "pt; top: " + top + "pt; color: #000; text-align: center;\">\n");
		html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/editor.png\" />\n");
		html.append("<br>\n");
		html.append("Editor\n");
		html.append("</div>\n");


		left = LEFT_OFFSET;

		GenericProjectCtrl projectCtrl = new GenericProjectCtrl(
			System.getProperty("java.class.path") + "/../../assWorkbench/server/projects");
		List<GenericProject> projects = projectCtrl.getGenericProjects();

		html.append("<div class='projectbar' style='right: unset;'>\n");

		for (GenericProject proj : projects) {
			html.append("\n");
			html.append("  <a href=\"localhost:3010/projects/" + proj.getShortName() + "/?open=logbook\" target=\"_blank\" class=\"project\" style=\"border-color: " + proj.getColor().toHexString() + "; position: absolute; left: " + left + "pt; bottom: 25pt; width: 85pt; height: 55pt; border-style: solid; border-width: 3pt; border-radius: 8pt;\">");
			html.append("    <span class=\"vertAligner\"></span><img src=\"projectlogos/" + proj.getShortName() + "/logo.png\" />");
			html.append("  </a>");
			left += 99;
		}

		html.append("</div>\n");

		html.append("</div>");


		html.append("</div>");
		html.append("</div>");
		html.append("</div>");
		html.append("</div>");


		// TOOLS

		int bottom = 10;
		int bottomDistance = 46;

		if (!leaveOut.contains(SideBarEntryForTool.WORKBENCH)) {
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
		if (leaveOut.contains(SideBarEntryForTool.BROWSER)) {
			html.append("target=\"_blank\" ");
		}
		html.append("style=\"bottom: " + bottom + "pt;\">\n");
		html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/browser.png\" />\n");
		html.append("</a>\n");
		script.append("document.getElementById('sidebar_" + entry + "').href = \"http://\" + window.location.hostname + \":3013/\";\n");
		entry++;
		bottom += bottomDistance;

		if (!leaveOut.contains(SideBarEntryForTool.EDITOR)) {
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

		List<Record> veRecords = getVirtualEmployeeRecords();

		for (Record veRec : veRecords) {
			if (location.equals("/pics/" + veRec.getString("name").toLowerCase() + ".jpg")) {
				result = new File(basePath + veRec.getString("avatar"));
			}
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

	private static List<Record> getVirtualEmployeeRecords() {

		if (virtualEmployeeRecords == null) {

			String basePath = getBasePath();

			String dbFilePath = basePath + "virtualEmployees/database.json";

			try {
				JsonFile dbFile = new JsonFile(dbFilePath);
				rootRecord = dbFile.getAllContents();
				virtualEmployeeRecords = rootRecord.getArray("virtualEmployees");
			} catch (JsonParseException e) {
				System.out.println("Sorry, the file '" + basePath + "' cannot be loaded!");
			}
		}

		return virtualEmployeeRecords;
	}

}
