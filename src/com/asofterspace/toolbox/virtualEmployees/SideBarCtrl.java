/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.virtualEmployees;

import com.asofterspace.toolbox.projects.GenericProject;
import com.asofterspace.toolbox.projects.GenericProjectCtrl;

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

		if (!leaveOut.contains(SideBarEntry.HUGO)) {
			html.append("<a class=\"sidebar\" href=\"http://localhost:3012/\">");
			html.append("<img class=\"avatar\" src=\"/pics/hugo.jpg\" />");
			html.append("</a>");
		}

		if (!leaveOut.contains(SideBarEntry.MARI)) {
			html.append("<a class=\"sidebar\" href=\"http://localhost:3011/\" style=\"top: 92pt;\">");
			html.append("<img class=\"avatar\" src=\"/pics/mari.jpg\" />");
			html.append("</a>");
		}

		if (!leaveOut.contains(SideBarEntry.WORKBENCH)) {
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
		}

		return html.toString();
	}
}
