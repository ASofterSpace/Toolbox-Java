package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmMonitoringControlElement extends CdmNode {

	private String definitionId;

	private String defaultRouteId;

	private String defaultServiceAccessPointId;
	
	private List<String> subElements;


	public CdmMonitoringControlElement(CdmFile parent, Node mceNode) {

		super(parent, mceNode);
		
		this.definitionId = getValue("definition");
		
		this.defaultRouteId = getValue("defaultRoute");
		
		this.defaultServiceAccessPointId = getValue("defaultServiceAccessPoint");
		
		// subelements can be stored in an attribute such as:
		// subElements="_AAAAAA _AAAAAA"
		// OR in a child node such as:
		// <subElements href="#_AAAAAA"/>

		// TODO :: not just subElements, but EVERYTHING that is key="value"
		// can also be expressed as child:
		// <key href="value" (possibly xmi:id, xsi:type, or whatever)/>

		this.subElements = new ArrayList<>();
		
		String subStrs = getValue("subElements");
		if (subStrs != null) {
			String[] subStrsArr = subStrs.split(" ");
			for (String subStr : subStrsArr) {
				this.subElements.add(Utils.getIdFromEcoreLink(subStr));
			}
		}
		
		NodeList subElementList = mceNode.getChildNodes();

		int len = subElementList.getLength();

		for (int i = 0; i < len; i++) {
			Node subElement = subElementList.item(i);
			if ("subElements".equals(subElement.getNodeName())) {
				this.subElements.add(Utils.getIdFromEcoreLink(subElement.getAttributes().getNamedItem("href").getNodeValue()));
			}
		}
	}
	
	public List<String> getSubElementIds() {
		return subElements;
	}
	
	public CdmMonitoringControlElement getContainingElement() {

		List<CdmMonitoringControlElement> mces = CdmCtrl.getMonitoringControlElements();
		
		if (mces == null) {
			return null;
		}
		
		// TODO :: we should also check the filename, not just the id, in case it is given as
		// file#id (or at least drop the filename, so we can compare the id more nicely...)
		for (CdmMonitoringControlElement mce : mces) {
			List<String> subElementIds = mce.getSubElementIds();
			if ((subElementIds != null) && (subElementIds.contains(id))) {
				return mce;
			}
		}
		
		return null;
	}
	
	public String getPath() {
	
		CdmMonitoringControlElement containedIn = getContainingElement();
		
		if (containedIn == null) {
			return name;
		}
		
		return containedIn.getPath() + CdmCtrl.MCM_PATH_DELIMITER + name;
	}
	
	public CdmActivity addActivity(String newActivityName, String newActivityAlias) {
	
		// TODO :: add base element / definition
		
		// TODO :: add permitted route
		
		// TODO :: add default route
		
		// TODO :: add default ServiceAccessPoint
		
		// generate a new random ID
		String activityId = Utils.generateEcoreUUID();

		// actually create the element
		Element newActivity = parent.createElement("monitoringControlElementAspects");
		newActivity.setAttribute("xsi:type", "monitoringcontrolmodel:Activity");
		newActivity.setAttribute("xmi:id", activityId);
		newActivity.setAttribute("name", newActivityName);
		newActivity.setAttribute("hasPredictedValue", "false");

		if ((newActivityAlias != null) && !("".equals(newActivityAlias))) {
			String newAliasId = Utils.generateEcoreUUID();
			Element newAlias = parent.createElement("aliases");
			newAlias.setAttribute("xsi:type", "monitoringcontrolcommon:MonitoringAndControlAlias");
			newAlias.setAttribute("xmi:id", newAliasId);
			newAlias.setAttribute("alias", newActivityAlias);
			newActivity.appendChild(newAlias);
		}
		
		thisNode.appendChild(newActivity);

		return new CdmActivity(parent, newActivity);
	}

}
