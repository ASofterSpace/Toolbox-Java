package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.io.XmlElement;

import org.xml.sax.Attributes;


/**
 * A generic CDM Node, that could really literally be anything...
 * Everything else kind of derives from this :)
 */
public class CdmNode extends XmlElement {

	protected CdmFile parentFile;

	protected CdmCtrl cdmCtrl;


	public CdmNode(CdmFile parentFile, XmlElement thisNode, CdmCtrl cdmCtrl) {

		super();
		
		this.parentFile = parentFile;

		this.cdmCtrl = cdmCtrl;
		
		thisNode.copyTo(this);
	}
	
	public CdmNode(CdmNode other) {
	
		super();
	
		parentFile = other.parentFile;
		
		cdmCtrl = other.cdmCtrl;
		
		other.copyTo(this);
	}

	public CdmFile getParentFile() {
		return parentFile;
	}

	public String getTagName() {
		return getNodeName();
	}

	public String getName() {
		return getAttribute("name");
	}

	public String getNamespace() {
		return getAttribute("namespace");
	}

	public String getType() {
		return getAttribute("xsi:type");
	}

	public String getId() {
		return getAttribute("xmi:id");
	}

	public void setName(String newName) {
		setAttribute("name", newName);
	}

	public void delete() {

		// delete this itself from the parent
		getXmlParent().removeChild(this);
		
		// delete this from the full model in the controller
		cdmCtrl.removeFromModel(this);
	}

	/**
	 * Prints information about this node to System.out
	 */
	public void print() {
		
		String name = getName();
		String namespace = getNamespace();
		String type = getType();
		String id = getId();
		
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
		
		if (type == null) {
			System.out.println("Type: none");
		} else {
			System.out.println("Type: " + type);
		}
		
		String xmlTag = getNodeName();
		if (xmlTag == null) {
			System.out.println("XML Tag: none");
		} else {
			System.out.println("XML Tag: " + xmlTag);
		}
		
		System.out.println("Contained in: " + getParentFile().getPathRelativeToCdmRoot());
	}

}
