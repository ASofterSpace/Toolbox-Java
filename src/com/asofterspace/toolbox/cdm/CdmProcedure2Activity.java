/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.cdm;

import java.util.List;
import java.util.Set;
import com.asofterspace.toolbox.io.XmlElement;


public class CdmProcedure2Activity extends CdmNode {

	private String mappedActivityFilename;

	private String mappedActivityId;

	private String mappedProcedureFilename;

	private String mappedProcedureId;


	public CdmProcedure2Activity(CdmNode baseNode) {

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
				if ("procedure".equals(elem.getNodeName())) {
					this.mappedProcedureFilename = hrefSplit[0];
					this.mappedProcedureId = hrefSplit[1];
				}
			}
		}

		baseNode.setExtendingObject(this);
	}

	public CdmProcedure2Activity(CdmFileBase parentFile, XmlElement thisNode, CdmCtrl cdmCtrl) {

		this(cdmCtrl.getByXmlElement(parentFile, thisNode));
	}

	public boolean mapsProcedure(String procedureId) {
		return procedureId.equals(mappedProcedureId);
	}

	// TODO - this works as long as the file is in the same folder as this file,
	// but for CIs in different subfolders, this would need to be more elaborate...
	// (for now, we therefore just use mapsProcedure(id), as the id should be unique)
	public boolean mapsProcedure(String cdmFilename, String procedureId) {

		if (!cdmFilename.equals(mappedProcedureFilename)) {
			return false;
		}

		if (!procedureId.equals(mappedProcedureId)) {
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

	public String getmappedProcedureFilename() {
		return mappedProcedureFilename;
	}

	public String getmappedProcedureId() {
		return mappedProcedureId;
	}

	public void delete() {

		// delete the mapping itself from the parent file
		super.delete();

		// TODO :: when the CI containing us is completely empty, then it should be deleted... at least upon the next save xD
	}
}
