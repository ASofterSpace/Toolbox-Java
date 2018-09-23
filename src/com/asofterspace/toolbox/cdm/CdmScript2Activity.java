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

	public CdmScript2Activity(CdmFile parent, Node thisNode) {

		this(new CdmNode(parent, thisNode));
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
	
	public String getMappedScriptFilename() {
		return mappedScriptFilename;
	}
	
	public String getMappedScriptId() {
		return mappedScriptId;
	}
	
	public void delete() {
	
		// delete the script itself from the parent file
		super.delete();
		
		// delete us from the list of mappings, as the CI containing us will be kept always, so we do not need to keep
		// track of being deleted and later on truly delete us (like in the case of a CdmScript), but can instead just
		// remove ourselves entirely from the program right now
		CdmCtrl.getScriptToActivityMappings().remove(this);
	}
}
