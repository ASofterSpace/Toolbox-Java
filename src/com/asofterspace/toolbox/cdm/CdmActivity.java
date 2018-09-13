package com.asofterspace.toolbox.cdm;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmActivity extends CdmNode {

	// TODO :: keep track of the base element / activity definition explicitly
	// (however - take care; right now, we only get the real activities, NOT the activity definitions inside mce definitions,
	// so the definition activities right now are not given to us by the CdmCtrl, and if we change that then we need to change many other
	// places, so ideally we would like to keep it this way - just never display activities from inside mce definitions anywhere!)
	private String baseElementId;

	private String hasPredictedValue;

	private String permittedRouteId;

	private String defaultRouteId;

	private String defaultServiceAccessPointId;

	private String alias;

	
	// the activityNode is a monitoringControlElementAspects node which is a child of a monitoringControlElement node,
	// and NOT a direct child of a CI root node!
	public CdmActivity(CdmFile parent, Node activityNode) {

		super(parent, activityNode);
	
		this.baseElementId = getValue("baseElement");
		
		this.hasPredictedValue = getValue("hasPredictedValue");
		
		this.permittedRouteId = getValue("permittedRoute");
		
		this.defaultRouteId = getValue("defaultRoute");
		
		this.defaultServiceAccessPointId = getValue("defaultServiceAccessPoint");
		
		NodeList aliassesAndArgs = activityNode.getChildNodes();

		int len = aliassesAndArgs.getLength();

		for (int i = 0; i < len; i++) {
			Node aliasOrArg = aliassesAndArgs.item(i);
			if ("aliases".equals(aliasOrArg.getNodeName())) {
				this.alias = aliasOrArg.getAttributes().getNamedItem("alias").getNodeValue();
			}
		}

		// TODO :: also take care of arguments, e.g.
		// <arguments xsi:type="monitoringcontrolmodel:EngineeringArgument" xmi:id="_AAAAACqUzEIAAAAAAAABoA" engineeringArgumentDefinition="______91W8zUAAAAAAAAB8w">
		//   <engineeringDefaultValue xsi:type="..." xmi:id="_2Fdl8qboEeiEK5o2bemhxQ" parameter="_2Fdl8KboEeiEK5o2bemhxQ"/>
		// </arguments>
	}

	public boolean isDefinition() {
		// if it has a baseElement attribute then it is a "real" activity, if it does not have it, then it is a definition
		return baseElementId == null;
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
