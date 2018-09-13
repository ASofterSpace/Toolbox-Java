package com.asofterspace.toolbox.cdm;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class CdmNode {

	protected CdmFile parent;

	protected Node thisNode;
	
	protected NamedNodeMap attributes;

	protected String name;

	protected String namespace;

	protected String id;


	public CdmNode(CdmFile parent, Node thisNode) {

		this.parent = parent;

		this.thisNode = thisNode;

		this.attributes = thisNode.getAttributes();

		this.name = getValue("name");
		
		this.namespace = getValue("namespace");
		
		this.id = getValue("xmi:id");
	}
	
	protected String getValue(String key) {

		Node resultNode = attributes.getNamedItem(key);
		
		if (resultNode == null) {
			return null;
		}
		
		return resultNode.getNodeValue();
	}

	public CdmFile getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getId() {
		return id;
	}

	public void setName(String newName) {

		NamedNodeMap nodeAttributes = thisNode.getAttributes();

		Node nameNode = nodeAttributes.getNamedItem("name");

		if (nameNode == null) {
			return;
		}

		nameNode.setNodeValue(newName);

		name = newName;
	}

	public void delete() {

		// delete this itself from the parent
		thisNode.getParentNode().removeChild(thisNode);
	}

}
