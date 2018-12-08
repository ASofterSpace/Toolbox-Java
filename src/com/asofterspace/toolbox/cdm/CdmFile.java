/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * This class abstracts one particular CDM file (or, as one might call it, one particular configuration item.)
 * It could be a ScriptCI, a Script2ActivityMapperCI, an McmCI, ...
 */
public class CdmFile extends CdmFileBase {

	/**
	 * You can construct a CdmFile instance by basing it on an existing file object.
	 */
	public CdmFile(File regularFile, CdmCtrl cdmCtrl) {

		super(regularFile, cdmCtrl);
	}

	void addContentsToCdmCtrl() {

		// we have been deleted, we do not add anything anywhere! Hah!
		if (deleted) {
			return;
		}

		recursivelyAddToCdmCtrl(getRoot(), getCiType());
	}

	private void recursivelyAddToCdmCtrl(XmlElement curNode, String ciType) {

		// TODO :: also update the xmiIdMap when elements are added dynamically (e.g. activities, scripts, mappers, etc.),
		// or when they are removed

		// check if this even is a node that we are interested in - that is, if it has an EMF ID (only nodes with IDs and their children are of interest!)
		if (curNode == null) {
			return;
		}

		String nodeId = curNode.getAttribute("xmi:id");

		if (nodeId == null) {
			return;
		}

		// now actually add the current node to the internal model in the controller - first to specialized lists, and then to the full id map...
		String nodeName = curNode.getNodeName();

		CdmNode cdmNode = new CdmNode(this, curNode, cdmCtrl);

		switch (ciType) {

			case CdmCtrl.CI_SCRIPT:
				if ("script".equals(nodeName)) {
					cdmNode = new CdmScript(cdmNode);
					break;
				}
				break;

			case CdmCtrl.CI_SCRIPT_TO_ACTIVITY:
				if ("scriptActivityImpl".equals(nodeName)) {
					cdmNode = new CdmScript2Activity(cdmNode);
					break;
				}
				break;

			case CdmCtrl.CI_PROCEDURE:
				if ("procedure".equals(nodeName)) {
					cdmNode = new CdmProcedure(cdmNode);
					break;
				}
				break;

			case CdmCtrl.CI_PROCEDURE_TO_ACTIVITY:
			case CdmCtrl.CI_PROCEDURE_TO_ACTIVITY_OLD:
				if ("procedureActivityImpl".equals(nodeName)) {
					cdmNode = new CdmProcedure2Activity(cdmNode);
					break;
				}
				break;

			case CdmCtrl.CI_MCM:
				if ("monitoringControlElement".equals(nodeName)) {
					cdmNode = new CdmMonitoringControlElement(cdmNode);
					break;
				}
				if ("monitoringControlElementAspects".equals(nodeName)) {
					if ("monitoringcontrolmodel:Activity".equals(cdmNode.getType())) {
						cdmNode = new CdmActivity(cdmNode);
						break;
					}
				}
				break;
		}

		// update cdm ctrl model with the new node
		cdmCtrl.addToModel(cdmNode);

		// it has been confirmed, this node is of interest to us... let's recursively call ourselves for all the children
		List<XmlElement> children = curNode.getChildNodes();

		for (XmlElement child : children) {
			// how sad that Java does not know tail call optimization! this would be beautiful! :D
			recursivelyAddToCdmCtrl(child, ciType);
		}
	}

	/**
	 * Add a new mapping to the file (but do not save it immediately)
	 */
	public CdmScript2Activity addScript2Activity(String mappingName, String scriptFile, String scriptId, String activityFile, String activityId) {

		// TODO :: make this configurable
		String mappingNamespace = CdmCtrl.DEFAULT_NAMESPACE;

		// generate a new random ID
		String mappingId = UuidEncoderDecoder.generateEcoreUUID();

		// actually create the element
		XmlElement newMapping = getRoot().createChild("scriptActivityImpl");
		newMapping.setAttribute("xmi:id", mappingId);
		newMapping.setAttribute("name", mappingName);
		newMapping.setAttribute("namespace", mappingNamespace);

		XmlElement newMappedActivity = newMapping.createChild("activity");
		newMappedActivity.setAttribute("href", activityFile + "#" + activityId);

		XmlElement newMappedScript = newMapping.createChild("script");
		newMappedScript.setAttribute("href", scriptFile + "#" + scriptId);

		CdmScript2Activity newNode = new CdmScript2Activity(this, newMapping, cdmCtrl);
		
		// update cdm ctrl model with the new node
		cdmCtrl.addToModel(newNode);
		
		return newNode;
	}

	/**
	 * In the interest of speed when calling this function, you have to ensure that the UUID
	 * is also an Ecore one! No passing Java UUIDs to this function, you! :P
	 */
	public void findByUuid(String ecoreUuid, List<CdmNode> result) {
		recursivelyFindByKey(getRoot(), "xmi:id", ecoreUuid, result);
	}

	public void findByName(String name, List<CdmNode> result) {
		recursivelyFindByKey(getRoot(), "name", name, result);
	}

	public void findByType(String type, List<CdmNode> result) {
		recursivelyFindByKey(getRoot(), "xsi:type", type, result);
	}

	private void recursivelyFindByKey(XmlElement currentNode, String key, String value, List<CdmNode> result) {

		String resultStr = currentNode.getAttribute(key);

		if (value.equals(resultStr)) {
				result.add(new CdmNode(this, currentNode, cdmCtrl));
		}

		List<XmlElement> children = currentNode.getChildNodes();
		for (XmlElement child : children) {
			recursivelyFindByKey(child, key, value, result);
		}
	}

	public void findByXmlTag(String xmlTag, List<CdmNode> result) {

		List<XmlElement> elements = getRoot().getElementsByTagName(xmlTag);

		for (XmlElement element : elements) {
			result.add(new CdmNode(this, element, cdmCtrl));
		}
	}

	/**
	 * Gets a rough size that can be used to compare several CIs and check which one is larger -
	 * e.g. to find a useful default for inserting something into
	 */
	public int getRoughSize() {

		XmlElement root = getRoot();

		if (root == null) {
			return 0;
		}

		return root.getChildNodes().size();
	}
}
