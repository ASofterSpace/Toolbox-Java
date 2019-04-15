/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.io.XmlElement;

import java.util.ArrayList;
import java.util.List;


public class CdmActivity extends CdmNode {

	/*
	private String baseElementId;

	private String hasPredictedValue;

	private String permittedRouteId;

	private String defaultRouteId;

	private String defaultServiceAccessPointId;
	*/

	private String alias;

	private boolean isDefinition;


	// the activityNode is a monitoringControlElementAspects node which is a child of a monitoringControlElement node,
	// and NOT a direct child of a CI root node!
	public CdmActivity(CdmNode baseNode) {

		super(baseNode);

		/*
		this.baseElementId = getAttribute("baseElement");

		this.hasPredictedValue = getAttribute("hasPredictedValue");

		this.permittedRouteId = getAttribute("permittedRoute");

		this.defaultRouteId = getAttribute("defaultRoute");

		this.defaultServiceAccessPointId = getAttribute("defaultServiceAccessPoint");
		*/

		List<XmlElement> aliassesAndArgs = getChildNodes();

		for (XmlElement aliasOrArg : aliassesAndArgs) {
			if ("aliases".equals(aliasOrArg.getTagName())) {
				this.alias = aliasOrArg.getAttribute("alias");
			}
		}

		this.isDefinition = "monitoringControlElementDefinition".equals(getXmlParent().getTagName());

		// TODO :: also take care of arguments, e.g.
		// <arguments xsi:type="monitoringcontrolmodel:EngineeringArgument" xmi:id="_AAAAACqUzEIAAAAAAAABoA" engineeringArgumentDefinition="______91W8zUAAAAAAAAB8w">
		//   <engineeringDefaultValue xsi:type="..." xmi:id="_2Fdl8qboEeiEK5o2bemhxQ" parameter="_2Fdl8KboEeiEK5o2bemhxQ"/>
		// </arguments>

		baseNode.setExtendingObject(this);
	}

	public CdmActivity(CdmFileBase parentFile, XmlElement thisNode, CdmCtrl cdmCtrl) {

		this(cdmCtrl.getByXmlElement(parentFile, thisNode));
	}

	public boolean isDefinition() {
		return isDefinition;
	}

	public String getAlias() {
		return alias;
	}

	public void delete() {

		// delete the activity itself from the parent file
		super.delete();

		// TODO - delete the definition as well if this is a regular activity, or delete the activity/ies as well if this is a definition
		// (however, if this is a regular activity, and there are others that have the same definition, then delete nothing except this activity!)

		// TODO - delete all mappings involving this activity

		// TODO - if we did delete some other mappings, somehow tell the ScriptTabs about that, such that they can update their own mapping and info views?
	}

}
