package com.asofterspace.toolbox.cdm;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmScript extends CdmNode {

	private String content;
	

	public CdmScript(CdmFile parent, Node scriptNode) {
	
		super(parent, scriptNode);

		this.content = getValue("scriptContent");
	}
	
	public String getSourceCode() {
		return content;
	}
	
	public void setSourceCode(String scriptContent) {
	
		NamedNodeMap scriptNodeAttributes = thisNode.getAttributes();
		
		Node scriptContentNode = scriptNodeAttributes.getNamedItem("scriptContent");
		
		if (scriptContentNode == null) {
			return;
		}
		
		scriptContentNode.setNodeValue(scriptContent);
		
		content = scriptContent;
	}

	/**
	 * Get all script2Activity mappings associated with this particular script - there could be several
	 * mappings mapping to this script!
	 */
	public List<CdmScript2Activity> getAssociatedScript2Activities() {
	
		List<CdmScript2Activity> results = new ArrayList<>();
		
		List<CdmFile> scriptToActivityMapperCis = CdmCtrl.getScriptToActivityMappingCIs();
	
		// String parentFilename = parent.getLocalFilename();
	
		// TODO :: maybe get the CdmScript2Activity instances from the CdmCtrl directly, instead of going via the CIs here!
		
		for (CdmFile scriptToActivityMapperCi : scriptToActivityMapperCis) {
			
			List<CdmScript2Activity> script2Activities = scriptToActivityMapperCi.getScript2Activities();
			
			for (CdmScript2Activity script2Activity : script2Activities) {
				// check if a script to activity mapper maps the script id of this particular script!
				// TODO :: also check if the filename maps (however, this is complicated, as the file
				// could be in a different folder etc. - so it is simpler to only check for the id,
				// which *should* be unique anyway!)
				if (script2Activity.mapsScript(id)) {
					results.add(script2Activity);
				}
			}
		}
		
		return results;
	}
	
	public void delete() {

		// delete entries from the script to activity mapper - as there could be several
		// activities mapped to this script...
		List<CdmScript2Activity> script2Activities = getAssociatedScript2Activities();

		// ... we iterate...
		for (CdmScript2Activity script2Activity : script2Activities) {

			// ... we delete the associated activities, if the user wants us to ...
			// TODO
			
			// ... and we delete the mappings themselves!
			script2Activity.delete();
		}
		
		// delete the script itself from the parent file
		super.delete();

		// check if there are still elements left now, and if not, delete the entire parent file
		NodeList elements = parent.getRoot().getChildNodes();
		
		// assume there are no elements left...
		boolean noElementsLeft = true;
		
		// ... iterate over all nodes...
		int len = elements.getLength();
		
		for (int i = 0; i < len; i++) {
			Node elem = elements.item(i);
		
			// ... ignoring text nodes...
			if (!"#text".equals(elem.getNodeName())) {
				// ... but if there are any others, then elements are actually left!
				noElementsLeft = false;
				break;
			}
		}
		
		// delete the entire parent file
		// (or actually set a deleted flag, to delete it when save() is called ^^)
		if (noElementsLeft) {
			parent.delete();
		}
	}

}
