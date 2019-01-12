/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.io.XmlElement;

import java.util.List;
import java.util.HashSet;
import java.util.Set;


public class CdmProcedure extends CdmNode {

	private String content;


	public CdmProcedure(CdmNode baseNode) {

		super(baseNode);

		this.content = getAttribute("procedureContent");

		baseNode.setExtendingObject(this);
	}

	public CdmProcedure(CdmFileBase parentFile, XmlElement thisNode, CdmCtrl cdmCtrl) {

		this(cdmCtrl.getByXmlElement(parentFile, thisNode));
	}

	public String getSourceCode() {
		return content;
	}

	public void setSourceCode(String procedureContent) {

		setAttribute("procedureContent", procedureContent);

		content = procedureContent;
	}

	/**
	 * Get all procedure2Activity mappings associated with this particular procedure - there could be several
	 * mappings mapping to this procedure!
	 */
	public Set<CdmProcedure2Activity> getAssociatedprocedure2Activities() {

		Set<CdmProcedure2Activity> results = new HashSet<>();

		Set<CdmProcedure2Activity> procedure2Activities = cdmCtrl.getProcedureToActivityMappings();

		String id = getId();

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
		List<XmlElement> elements = getParentFile().getRoot().getChildNodes();

		// delete the entire parent file
		// (or actually set a deleted flag, to delete it when save() is called ^^)
		if (elements.size() < 1) {
			getParentFile().delete();
		}
	}

}
