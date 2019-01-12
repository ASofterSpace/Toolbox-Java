/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.io.XmlElement;

import java.util.List;
import java.util.Set;


public class CdmScript2Activity extends CdmNode {

	private String mappedActivityFilename;

	private String mappedActivityId;

	private String mappedScriptFilename;

	private String mappedScriptId;


	public CdmScript2Activity(CdmNode baseNode) {

		super(baseNode);

		List<XmlElement> elements = getChildNodes();

		for (XmlElement elem : elements) {

			String href = elem.getAttribute("href");

			if (href == null) {
				continue;
			}

			// this here relies on the fact that inside the mapper, ALL links are file-based, and none are local... but fuuu does it feel ugly!
			String[] hrefSplit = href.split("#");
			if (hrefSplit.length > 1) {
				if ("activity".equals(elem.getNodeName())) {
					this.mappedActivityFilename = hrefSplit[0];
					this.mappedActivityId = hrefSplit[1];
				}
				if ("script".equals(elem.getNodeName())) {
					this.mappedScriptFilename = hrefSplit[0];
					this.mappedScriptId = hrefSplit[1];
				}
			}
		}

		baseNode.setExtendingObject(this);
	}

	public CdmScript2Activity(CdmFileBase parentFile, XmlElement thisNode, CdmCtrl cdmCtrl) {

		this(cdmCtrl.getByXmlElement(parentFile, thisNode));
	}

	public boolean mapsScript(String scriptId) {
		return scriptId.equals(mappedScriptId);
	}

	// TODO - this works as long as the file is in the same folder as this file,
	// but for CIs in different subfolders, this would need to be more elaborate...
	// (for now, we therefore just use mapsScript(id), as the id should be unique)
	public boolean mapsScript(String cdmFilename, String scriptId) {

		if (!cdmFilename.equals(mappedScriptFilename)) {
			return false;
		}

		if (!scriptId.equals(mappedScriptId)) {
			return false;
		}

		return true;
	}

	public String getMappedActivityFilename() {
		return mappedActivityFilename;
	}

	public String getMappedActivityId() {
		return mappedActivityId;
	}

	public CdmActivity getMappedActivity() {

		if (mappedActivityId == null) {
			return null;
		}

		Set<CdmActivity> activities = cdmCtrl.getActivities();

		if (activities == null) {
			return null;
		}

		for (CdmActivity activity : activities) {
			if (mappedActivityId.equals(activity.getId())) {
				return activity;
			}
		}

		return null;
	}

	public String getMappedScriptFilename() {
		return mappedScriptFilename;
	}

	public String getMappedScriptId() {
		return mappedScriptId;
	}

	public void delete() {

		// delete the mapping itself from the parent file
		super.delete();

		// TODO :: when the CI containing us is completely empty, then it should be deleted... at least upon the next save xD
	}
}
