/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.virtualEmployees;

import com.asofterspace.toolbox.io.File;

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

		int top = 10;

		if (!leaveOut.contains(SideBarEntry.HUGO)) {
			html.append("<a class=\"sidebar\" href=\"http://localhost:3012/\" style=\"top: " + top + "pt;\">");
			html.append("<img class=\"avatar\" src=\"/pics/hugo.jpg\" />");
			html.append("</a>");
			top += 82;
		}

		if (!leaveOut.contains(SideBarEntry.MARI)) {
			html.append("<a class=\"sidebar\" href=\"http://localhost:3011/\" style=\"top: " + top + "pt; transform: scaleX(-1);\">");
			html.append("<img class=\"avatar\" src=\"/pics/mari.jpg\" />");
			html.append("</a>");
			top += 82;
		}

		int bottom = 10;

		if (!leaveOut.contains(SideBarEntry.WORKBENCH)) {
			html.append("<a class=\"sidebar\" href=\"http://localhost:3010/\" target=\"_blank\" style=\"bottom: " + bottom + "pt; top: unset;\">");
			html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/workbench.png\" />");
			html.append("</a>");
			/*
			html.append("<div class=\"projectbar\">");

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
			*/
			bottom += 45;
		}

		if (!leaveOut.contains(SideBarEntry.BROWSER)) {
			html.append("<a class=\"sidebar\" href=\"http://localhost:3013/\" target=\"_blank\" style=\"bottom: " + bottom + "pt; top: unset;\">");
			html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/browser.png\" />");
			html.append("</a>");
			bottom += 45;
		}

		/*
		if (!leaveOut.contains(SideBarEntry.EDITOR)) {
			html.append("<a class=\"sidebar\" href=\"http://localhost:3013/\" style=\"bottom: " + bottom + "pt; top: unset;\">");
			html.append("<img class=\"avatar\" style=\"border-radius: unset;\" src=\"/pics/editor.png\" />");
			html.append("</a>");
			bottom += 45;
		}
		*/

		return html.toString();
	}

	/**
	 * Returns an image file from the side bar as answer to a GET request, if the GET request has
	 * such an image as target, and null otherwise
	 */
	public static File getSideBarImageFile(String location) {

		File result = null;

		String basePath = System.getProperty("java.class.path") + "/../../";

		if (location.equals("/pics/hugo.jpg")) {
			result = new File(basePath + "assSecretary/server/pics/hugo.jpg");
		}

		if (location.equals("/pics/mari.jpg")) {
			result = new File(basePath + "assAccountant/server/pics/mari.jpg");
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

		/*
		// get project logo files from assWorkbench
		if (location.startsWith("/projectlogos/") && location.endsWith(".png") && !location.contains("..")) {
			String filename = location.substring("/projectlogos/".length());
			filename = basePath + "assWorkbench/server/projects/" + filename;
			result = new File(filename);
		}
		*/

		if ((result != null) && result.exists()) {
			return result;
		}
		return null;
	}

}
