package com.asofterspace.toolbox.cdm;

import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmProcedure2Activity extends CdmNode {

	private String mappedActivityFilename;
	
	private String mappedActivityId;
	
	private String mappedProcedureFilename;
	
	private String mappedProcedureId;


	public CdmProcedure2Activity(CdmNode baseNode) {

		super(baseNode);
		
		NodeList elements = thisNode.getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			
			Node elem = elements.item(i);
			NamedNodeMap elemAttributes = elem.getAttributes();
			
			if (elemAttributes == null) {
				continue;
			}
			
			Node hrefNode = elemAttributes.getNamedItem("href");
			
			if (hrefNode == null) {
				continue;
			}
			
			String href = hrefNode.getNodeValue();
			
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
	}

	public CdmProcedure2Activity(CdmFile parentFile, Node thisNode) {

		this(new CdmNode(parentFile, thisNode));
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
		
		Set<CdmActivity> activities = CdmCtrl.getActivities();
		
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
