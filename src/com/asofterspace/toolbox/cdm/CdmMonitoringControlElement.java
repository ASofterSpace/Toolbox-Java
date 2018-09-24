package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.utils.EnumeratedCollection;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmMonitoringControlElement extends CdmNode implements TreeNode {

	// delayed initialization - ready to be used after CdmCtrl.reloadTreeRoots()
	private CdmMonitoringControlElement containingElement;

	private String definitionId;

	private String defaultRouteId;

	private String defaultServiceAccessPointId;
	
	private List<String> subElementIds;
	
	// delayed initialization - ready to be used after CdmCtrl.reloadTreeRoots()
	private List<CdmMonitoringControlElement> subElements;


	public CdmMonitoringControlElement(CdmNode baseNode) {

		super(baseNode);
		
		// this means that we are the mcm tree root element - for now ;)
		this.containingElement = null;
		
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

		this.subElementIds = new ArrayList<>();
		
		String subStrs = getValue("subElements");
		if (subStrs != null) {
			String[] subStrsArr = subStrs.split(" ");
			for (String subStr : subStrsArr) {
				this.subElementIds.add(UuidEncoderDecoder.getIdFromEcoreLink(subStr));
			}
		}
		
		NodeList subElementList = thisNode.getChildNodes();

		int len = subElementList.getLength();

		for (int i = 0; i < len; i++) {
			Node subElement = subElementList.item(i);
			if ("subElements".equals(subElement.getNodeName())) {
				this.subElementIds.add(UuidEncoderDecoder.getIdFromEcoreLink(subElement.getAttributes().getNamedItem("href").getNodeValue()));
			}
		}
		
		this.subElements = new ArrayList<>();
	}
	
	public CdmMonitoringControlElement(CdmFile parentFile, Node thisNode) {

		this(new CdmNode(parentFile, thisNode));
	}

	public List<String> getSubElementIds() {
		return subElementIds;
	}

	public List<CdmMonitoringControlElement> getSubElements() {
		return subElements;
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
		String activityId = UuidEncoderDecoder.generateEcoreUUID();

		// actually create the element
		Element newActivity = getParentFile().createElement("monitoringControlElementAspects");
		newActivity.setAttribute("xsi:type", "monitoringcontrolmodel:Activity");
		newActivity.setAttribute("xmi:id", activityId);
		newActivity.setAttribute("name", newActivityName);
		newActivity.setAttribute("hasPredictedValue", "false");

		if ((newActivityAlias != null) && !("".equals(newActivityAlias))) {
			String newAliasId = UuidEncoderDecoder.generateEcoreUUID();
			Element newAlias = getParentFile().createElement("aliases");
			newAlias.setAttribute("xsi:type", "monitoringcontrolcommon:MonitoringAndControlAlias");
			newAlias.setAttribute("xmi:id", newAliasId);
			newAlias.setAttribute("alias", newActivityAlias);
			newActivity.appendChild(newAlias);
		}
		
		thisNode.appendChild(newActivity);
		
		CdmActivity activityNode = new CdmActivity(getParentFile(), newActivity);
		
		// update cdm ctrl model with the new node
		CdmCtrl.addToModel(activityNode);
		
		return activityNode;
	}
	
	/**
	 * If the containing element is null, then yepp, we are the root of the MCM tree - whoop whoop!
	 */
	public boolean isRoot() {
		return containingElement == null;
	}
	
	public CdmMonitoringControlElement getContainingElement() {
		return containingElement;
	}
	
	// this is used by the CdmCtrl.reloadTreeRoots() function to set up the MCM tree cleverly - do not interfere from the outside without thinking hard!
	public void setContainingElement(CdmMonitoringControlElement newContainingElement) {
		this.containingElement = newContainingElement;
	}
	
	// this is used by the CdmCtrl.reloadTreeRoots() function to set up the MCM tree cleverly - do not interfere from the outside without thinking hard!
	// let each MCE find a path towards its children
	// (each MCE gets its children fast from our internal id map and tells them that they are not root anymore,
	// which causes them to update their own internal link up such that they know who is their daddy)
	// funnily enough, we do NOT do this recursively, but in three iterations (see the CdmCtrl) - meaning that
	// we are safe from cycling forever trivially, as we do not go deeper and deeper into the tree, but for
	// each leaf just once tell it that it is not root (TODO :: unless several nodes own the same leaf, in which case... öhm...
	// the root simply gets overwritten in the setContainingElement function - we could maybe introduce a flag
	// that is asked for later for the validation to validate that no node has two parents!)
	public void initSubTreeFromHere() {
		for (String id : subElementIds) {
			CdmNode subNode = CdmCtrl.getByUuid(id);
			if (subNode instanceof CdmMonitoringControlElement) {
				((CdmMonitoringControlElement) subNode).setContainingElement(this);
				subElements.add((CdmMonitoringControlElement) subNode);
			}
		}
	}
	
	// ------------------------------------------------------------------------------------------
	// the following are methods provided such that we are conforming with the TreeNode interface
	// ------------------------------------------------------------------------------------------
	
	public Enumeration children() {
		return new EnumeratedCollection(subElements);
	}
	
	public boolean getAllowsChildren() {
		return true;
	}
	
	public TreeNode getChildAt(int childIndex) {
		return subElements.get(childIndex);
	}
	
	public int getChildCount() {
		return subElements.size();
	}
	
	public int getIndex(TreeNode node) {
		return subElements.indexOf(node);
	}
	
	public TreeNode getParent() {
		return containingElement;
	}
	
	public boolean isLeaf() {
		// actually, none of these elements are leafs, because they are all MCEs - they all could contain parameters etc. :)
		// return subElements.size() == 0;
		return false;
	}
	
	public String toString() {
		return getName();
	}

	// ----------------------------------------------------------------------------------------------
	// the previous ones are methods provided such that we are conforming with the TreeNode interface
	// ----------------------------------------------------------------------------------------------

}
