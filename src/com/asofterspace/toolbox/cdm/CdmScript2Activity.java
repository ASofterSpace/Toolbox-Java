package com.asofterspace.toolbox.cdm;

import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmScript2Activity extends CdmNode {

	private String mappedActivityFilename;
	
	private String mappedActivityId;
	
	private String mappedScriptFilename;
	
	private String mappedScriptId;


	public CdmScript2Activity(CdmNode baseNode) {

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
				if ("script".equals(elem.getNodeName())) {
					this.mappedScriptFilename = hrefSplit[0];
					this.mappedScriptId = hrefSplit[1];
				}
			}
		}
	}

	public CdmScript2Activity(CdmFile parentFile, Node thisNode, CdmCtrl cdmCtrl) {

		this(new CdmNode(parentFile, thisNode, cdmCtrl));
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
