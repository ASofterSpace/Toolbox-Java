package com.asofterspace.toolbox.cdm;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmScript extends CdmNode {

	private String content;
	

	public CdmScript(CdmNode baseNode) {

		super(baseNode);
		
		this.content = getValue("scriptContent");
	}
	
	public CdmScript(CdmFile parentFile, Node thisNode, CdmCtrl cdmCtrl) {

		this(new CdmNode(parentFile, thisNode, cdmCtrl));
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
	public Set<CdmScript2Activity> getAssociatedScript2Activities() {

		Set<CdmScript2Activity> results = new HashSet<>();

		Set<CdmScript2Activity> script2Activities = cdmCtrl.getScriptToActivityMappings();

		for (CdmScript2Activity script2Activity : script2Activities) {
			// check if a script to activity mapper maps the script id of this particular script!
			// TODO :: also check if the filename maps (however, this is complicated, as the file
			// could be in a different folder etc. - so it is simpler to only check for the id,
			// which *should* be unique anyway!)
			if (script2Activity.mapsScript(id)) {
				results.add(script2Activity);
			}
		}
		
		return results;
	}
	
	public void delete() {

		// delete entries from the script to activity mapper - as there could be several
		// activities mapped to this script...
		Set<CdmScript2Activity> script2Activities = getAssociatedScript2Activities();

		// ... we iterate...
		for (CdmScript2Activity script2Activity : script2Activities) {

			// ... we delete the associated activities, if the user wants us to ...
			// TODO
			
			// ... and we delete the mappings themselves!
			script2Activity.delete();
		}
		
		// delete the script itself from the parent file and the model in the controller
		super.delete();

		// check if there are still elements left now, and if not, delete the entire parent file
		NodeList elements = getParentFile().getRoot().getChildNodes();
		
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
			getParentFile().delete();
		}
	}

}
