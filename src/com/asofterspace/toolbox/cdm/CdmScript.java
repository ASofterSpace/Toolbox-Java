package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.io.XmlElement;

import java.util.List;
import java.util.HashSet;
import java.util.Set;


public class CdmScript extends CdmNode {

	private String content;
	

	public CdmScript(CdmNode baseNode) {

		super(baseNode);
		
		this.content = getAttribute("scriptContent");
	}
	
	public CdmScript(CdmFileBase parentFile, XmlElement thisNode, CdmCtrl cdmCtrl) {

		this(new CdmNode(parentFile, thisNode, cdmCtrl));
	}

	public String getSourceCode() {
		return content;
	}
	
	public void setSourceCode(String scriptContent) {
	
		setAttribute("scriptContent", scriptContent);

		content = scriptContent;
	}

	/**
	 * Get all script2Activity mappings associated with this particular script - there could be several
	 * mappings mapping to this script!
	 */
	public Set<CdmScript2Activity> getAssociatedScript2Activities() {

		Set<CdmScript2Activity> results = new HashSet<>();

		Set<CdmScript2Activity> script2Activities = cdmCtrl.getScriptToActivityMappings();

		String id = getId();
		
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
		List<XmlElement> elements = getParentFile().getRoot().getChildNodes();
		
		// delete the entire parent file
		// (or actually set a deleted flag, to delete it when save() is called ^^)
		if (elements.size() < 1) {
			getParentFile().delete();
		}
	}

}
