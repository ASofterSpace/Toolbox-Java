package com.asofterspace.toolbox.cdm;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmProcedure extends CdmNode {

	private String content;
	

	public CdmProcedure(CdmNode baseNode) {

		super(baseNode);
		
		this.content = getValue("procedureContent");
	}
	
	public CdmProcedure(CdmFile parentFile, Node thisNode, CdmCtrl cdmCtrl) {

		this(new CdmNode(parentFile, thisNode, cdmCtrl));
	}

	public String getSourceCode() {
		return content;
	}
	
	public void setSourceCode(String procedureContent) {
	
		NamedNodeMap procedureNodeAttributes = thisNode.getAttributes();
		
		Node procedureContentNode = procedureNodeAttributes.getNamedItem("procedureContent");
		
		if (procedureContentNode == null) {
			return;
		}
		
		procedureContentNode.setNodeValue(procedureContent);
		
		content = procedureContent;
	}

	/**
	 * Get all procedure2Activity mappings associated with this particular procedure - there could be several
	 * mappings mapping to this procedure!
	 */
	public Set<CdmProcedure2Activity> getAssociatedprocedure2Activities() {

		Set<CdmProcedure2Activity> results = new HashSet<>();

		Set<CdmProcedure2Activity> procedure2Activities = cdmCtrl.getProcedureToActivityMappings();

		for (CdmProcedure2Activity procedure2Activity : procedure2Activities) {
			// check if a procedure to activity mapper maps the procedure id of this particular procedure!
			// TODO :: also check if the filename maps (however, this is complicated, as the file
			// could be in a different folder etc. - so it is simpler to only check for the id,
			// which *should* be unique anyway!)
			if (procedure2Activity.mapsProcedure(id)) {
				results.add(procedure2Activity);
			}
		}
		
		return results;
	}
	
	public void delete() {

		// delete entries from the procedure to activity mapper - as there could be several
		// activities mapped to this procedure...
		Set<CdmProcedure2Activity> procedure2Activities = getAssociatedprocedure2Activities();

		// ... we iterate...
		for (CdmProcedure2Activity procedure2Activity : procedure2Activities) {

			// ... we delete the associated activities, if the user wants us to ...
			// TODO
			
			// ... and we delete the mappings themselves!
			procedure2Activity.delete();
		}
		
		// delete the procedure itself from the parent file and the model in the controller
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
