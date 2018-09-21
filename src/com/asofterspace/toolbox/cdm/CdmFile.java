package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.Utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class abstracts one particular CDM file (or, as one might call it, one particular configuration item.)
 * It could be a ScriptCI, a Script2ActivityMapperCI, an McmCI, ...
 */
public class CdmFile extends CdmFileBase {

	/**
	 * You can construct a CdmFile instance by basing it on an existing file object.
	 */
	public CdmFile(File regularFile) {

		super(regularFile);
	}
	
	public String getPathRelativeToCdmRoot() {
	
		Directory cdmRootDir = CdmCtrl.getLastLoadedDirectory();
		Path cdmRootPath = cdmRootDir.getJavaFile().toPath().toAbsolutePath();
		Path cdmFilePath = getJavaFile().toPath().toAbsolutePath();
		
		Path relativePath = cdmRootPath.relativize(cdmFilePath);
		
		return relativePath.toString();
	}

	/**
	 * Get all the monitoring control elements defined in this CDM file, NOT their definitions!
	 * (this does not check if this even is an McmCI - you should check it first, to not search through others forever ^^)
	 */
	public List<CdmMonitoringControlElement> getMonitoringControlElements() {

		List<CdmMonitoringControlElement> results = new ArrayList<>();

		if (deleted) {
			return results;
		}

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node mce = elements.item(i);
				if ("monitoringControlElement".equals(mce.getNodeName())) {
					results.add(new CdmMonitoringControlElement(this, mce));
				}
			} catch (NullPointerException e) {
				System.err.println("ERROR: The " + Utils.th(i) + " child node in " + getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}

		return results;
	}

	/**
	 * Get all the scripts defined in this CDM file
	 * (this does not check if this even is a ScriptCI - you should check it first, to not search through others forever ^^)
	 */
	public List<CdmScript> getScripts() {

		List<CdmScript> results = new ArrayList<>();

		if (deleted) {
			return results;
		}

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("script".equals(elem.getNodeName())) {
					results.add(new CdmScript(this, elem));
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A script in " + getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}

		return results;
	}

	/**
	 * Get all the script to activity mapper entries defined in this CDM file
	 * (this does not check if this even is a Script2ActivityMapperCI - you should check it first, to not search through others forever ^^)
	 */
	public List<CdmScript2Activity> getScript2Activities() {

		List<CdmScript2Activity> results = new ArrayList<>();

		if (deleted) {
			return results;
		}

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("scriptActivityImpl".equals(elem.getNodeName())) {
					results.add(new CdmScript2Activity(this, elem));
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A scriptActivityImpl in " + getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}

		return results;
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
		Element newMapping = createElement("scriptActivityImpl");
		newMapping.setAttribute("xmi:id", mappingId);
		newMapping.setAttribute("name", mappingName);
		newMapping.setAttribute("namespace", mappingNamespace);

		Element newMappedActivity = createElement("activity");
		newMappedActivity.setAttribute("href", activityFile + "#" + activityId);
		newMapping.appendChild(newMappedActivity);

		Element newMappedScript = createElement("script");
		newMappedScript.setAttribute("href", scriptFile + "#" + scriptId);
		newMapping.appendChild(newMappedScript);

		getRoot().appendChild(newMapping);

		return new CdmScript2Activity(this, newMapping);
	}

	/**
	 * Get all the activities defined in this CDM file, NOT their definitions defined in mce definitions!
	 * (this does not check if this even is an McmCI - you should check it first, to not search through others forever ^^)
	 */
	public List<CdmActivity> getActivities() {

		List<CdmActivity> results = new ArrayList<>();

		if (deleted) {
			return results;
		}

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node mce = elements.item(i);
				if ("monitoringControlElement".equals(mce.getNodeName())) {
					NodeList mceAspects = mce.getChildNodes();
					int mceAspectLen = mceAspects.getLength();
					for (int j = 0; j < mceAspectLen; j++) {
						Node mceAspect = mceAspects.item(j);
						if ("monitoringControlElementAspects".equals(mceAspect.getNodeName())) {
							if ("monitoringcontrolmodel:Activity".equals(mceAspect.getAttributes().getNamedItem("xsi:type").getNodeValue())) {
								results.add(new CdmActivity(this, mceAspect));
							}
						}
					}
				}
			} catch (NullPointerException e) {
				System.err.println("ERROR: The " + Utils.th(i) + " child node in " + getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}

		return results;
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
	
	private void recursivelyFindByKey(Node currentNode, String key, String value, List<CdmNode> result) {
		
		NamedNodeMap attributes = currentNode.getAttributes();

		if (attributes != null) {
			Node resultNode = attributes.getNamedItem(key);
			if (resultNode != null) {
				if (value.equals(resultNode.getNodeValue())) {
						result.add(new CdmNode(this, currentNode));
				}
			}
		}
		
		NodeList children = currentNode.getChildNodes();
		int len = children.getLength();
		for (int i = 0; i < len; i++) {
			recursivelyFindByKey(children.item(i), key, value, result);
		}
	}
	
	public void findByXmlTag(String xmlTag, List<CdmNode> result) {
	
		NodeList elements = getDocument().getElementsByTagName(xmlTag);
		
		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			result.add(new CdmNode(this, elements.item(i)));
		}
	}
}
