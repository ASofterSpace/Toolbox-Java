package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.coders.UuidEncoderDecoder;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A generic CDM Node, that could really literally be anything...
 * Everything else kind of derives from this :)
 */
public class CdmNode {

	protected CdmFile parentFile;

	protected Node thisNode;
	
	protected NamedNodeMap attributes;

	protected String name;

	protected String namespace;
	
	protected String type;

	protected String id;


	public CdmNode(CdmFile parentFile, Node thisNode) {

		this.parentFile = parentFile;

		this.thisNode = thisNode;

		this.attributes = thisNode.getAttributes();

		this.name = getValue("name");
		
		this.namespace = getValue("namespace");
		
		this.type = getValue("xsi:type");
		
		this.id = getValue("xmi:id");
	}
	
	public CdmNode(CdmNode other) {
		parentFile = other.parentFile;
		thisNode = other.thisNode;
		attributes = other.attributes;
		name = other.name;
		namespace = other.namespace;
		type = other.type;
		id = other.id;
	}

	public String getValue(String key) {

		Node resultNode = attributes.getNamedItem(key);
		
		if (resultNode == null) {
			return null;
		}
		
		return resultNode.getNodeValue();
	}

	public CdmFile getParentFile() {
		return parentFile;
	}

	public Node getNode() {
		return thisNode;
	}
	
	public String getTagName() {
		if (thisNode == null) {
			return null;
		}
		return thisNode.getNodeName();
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getType() {
		return type;
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
		
		// delete this from the full model in the controller
		CdmCtrl.removeFromModel(this);
	}

	/**
	 * Prints information about this node to System.out
	 */
	public void print() {
		
		if (id == null) {
			System.out.println("ID: none");
		} else {
			System.out.println("ID: " + UuidEncoderDecoder.convertEcoreUUIDtoJava(id));
		}
		
		if (name == null) {
			System.out.println("Name: none");
		} else {
			System.out.println("Name: " + name);
		}
		
		if (namespace == null) {
			System.out.println("Namespace: none");
		} else {
			System.out.println("Namespace: " + namespace);
		}
		
		String type = getValue("xsi:type");
		if (type == null) {
			System.out.println("Type: none");
		} else {
			System.out.println("Type: " + type);
		}
		
		String xmlTag = thisNode.getNodeName();
		if (xmlTag == null) {
			System.out.println("XML Tag: none");
		} else {
			System.out.println("XML Tag: " + xmlTag);
		}
		
		System.out.println("Contained in: " + getParentFile().getPathRelativeToCdmRoot());
	}

}
