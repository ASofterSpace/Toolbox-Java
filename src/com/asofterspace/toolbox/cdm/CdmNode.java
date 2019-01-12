/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.io.XmlElement;

import org.xml.sax.Attributes;


/**
 * A generic CDM Node, that could really literally be anything...
 * Everything else kind of derives from this :)
 */
public class CdmNode extends XmlElement {

	protected CdmFileBase parentFile;

	protected CdmCtrl cdmCtrl;


	public CdmNode(CdmFileBase parentFile, XmlElement thisNode, CdmCtrl cdmCtrl) {

		super();

		this.parentFile = parentFile;

		this.cdmCtrl = cdmCtrl;

		thisNode.copyTo(this);

		thisNode.setExtendingObject(this);
	}

	protected CdmNode(CdmNode other) {

		super();

		parentFile = other.parentFile;

		cdmCtrl = other.cdmCtrl;

		other.copyTo(this);
	}

	public CdmFileBase getParentFile() {
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

	/**
	 * For MCEs and MCE aspects, this gives their path.
	 * For others, this is null.
	 */
	public String getPath() {

		String xmlTag = getNodeName();

		if ("monitoringControlElement".equals(xmlTag)) {
			XmlElement curMCE = getExtendingObject();
			if (curMCE instanceof CdmMonitoringControlElement) {
				return ((CdmMonitoringControlElement) curMCE).getPath();
			}
		}

		if ("monitoringControlElementAspects".equals(xmlTag)) {
			XmlElement parEl = getXmlParent();
			if (parEl != null) {
				XmlElement curMCE = parEl.getExtendingObject();
				if (curMCE instanceof CdmMonitoringControlElement) {
					return ((CdmMonitoringControlElement) curMCE).getPath() + "." + getName();
				}
			}
		}

		return null;
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
			System.out.println("UUID: none");
			System.out.println("Ecore UUID: none");
		} else {
			System.out.println("UUID: " + UuidEncoderDecoder.convertEcoreUUIDtoJava(id));
			System.out.println("Ecore UUID: " + id);
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

		if ("monitoringControlElement".equals(xmlTag) ||
			"monitoringControlElementAspects".equals(xmlTag)) {
			String path = getPath();
			if (path == null) {
				System.out.println("MCM Path: none found, but there should be one :(");
			} else {
				System.out.println("MCM Path: " + path);
			}
		} else {
			System.out.println("MCM Path: none (as this is not contained in the MCM tree)");
		}

		System.out.println("Contained in: " + getParentFile().getPathRelativeToCdmRoot());
	}

}
