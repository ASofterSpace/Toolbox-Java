package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class abstracts one particular CDM file (or, as one might call it, one particular configuration item.)
 * It could be a ScriptCI, a Script2ActivityMapperCI, an McmCI, ...
 */
public class CdmFile extends XmlFile {

	private String ciType;

	private boolean deleted = false;

	/**
	 * You can construct a CdmFile instance by basing it on an existing file object.
	 */
	public CdmFile(File regularFile) {

		super(regularFile);

		Node root = getRoot();

		if (root != null) {
			ciType = root.getNodeName();
		}
	}

	public String getCiType() {
		return ciType;
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
		String mappingId = Utils.generateEcoreUUID();

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
	 * Convert this CDM file to the given version and prefix - if either is null,
	 * keep the current one
	 */
	public void convertTo(String toVersion, String toPrefix) {
	
		String origVersion = getCdmVersion();
	
		if (toVersion == null) {
			toVersion = origVersion;
		}
		
		if (toPrefix == null) {
			toPrefix = getCdmVersionPrefix();
		}
		
		// sanitize our input
		while (toVersion.startsWith("/")) {
			toVersion = toVersion.substring(1);
		}
		
		while (toPrefix.endsWith("/")) {
			toPrefix = toPrefix.substring(0, toPrefix.length() - 1);
		}
	
		Node root = getRoot();
		
		if (root == null) {
			return;
		}
		
		NamedNodeMap scriptAttributes = root.getAttributes();

		Node configurationcontrol = scriptAttributes.getNamedItem("xmlns:configurationcontrol");
		if (configurationcontrol != null) {
			configurationcontrol.setNodeValue(toPrefix + "/" + CdmCtrl.CDM_NAMESPACE_MIDDLE + toVersion);
		}

		Node mcmimplementationitems = scriptAttributes.getNamedItem("xmlns:mcmimplementationitems");
		if (mcmimplementationitems != null) {
			mcmimplementationitems.setNodeValue(toPrefix + "/MonitoringControl/MCMImplementationItems/" + toVersion);
		}

		Node monitoringcontrolcommon = scriptAttributes.getNamedItem("xmlns:monitoringcontrolcommon");
		if (monitoringcontrolcommon != null) {
			monitoringcontrolcommon.setNodeValue(toPrefix + "/MonitoringControl/MonitoringControlCommon/" + toVersion);
		}

		Node monitoringcontrolmodel = scriptAttributes.getNamedItem("xmlns:monitoringcontrolmodel");
		if (monitoringcontrolmodel != null) {
			monitoringcontrolmodel.setNodeValue(toPrefix + "/MonitoringControl/MonitoringControlModel/" + toVersion);
		}

		// TODO :: also convert to the appropriate qudv, if we are aware which one it is!
		// sadly, the qudv prefixes are not even aligned with the CDM prefixes, e.g. in 1.14.0 prefix is esa/egscc,
		// but qudv prefix is esa/dme (or wait, was that just because we did a manual conversion wrong?
		// re-check if this is the case!), while in 1.12, qudv prefix is scopeset... (in both, qudv version is 1.5)
		// confirmed good example: xmlns:configurationcontrol="http://www.esa.int/dme/ConfigurationTracking/1.14.0b" xmlns:qudv.blocks_extModel="http://www.esa.int/dme/core/qudv/blocks/1.5" xmlns:qudv.conceptualmodel_extModel="http://www.esa.int/dme/core/qudv/conceptualmodel/1.5"
		
		List<String> knownVersions = CdmCtrl.getKnownCdmVersions();
		int origIndex = knownVersions.indexOf(origVersion);
		int destIndex = knownVersions.indexOf(toVersion);

		// if we do not know the versions, then we cannot do anything further to convert...
		if ((origIndex < 0) || (destIndex < 0)) {
			// TODO :: maybe throw some kind of exception here
			return;
		}
		
		// if the versions between which we are converting are the same, then do nothing :)
		if (origIndex == destIndex) {
			return;
		}
		
		// chain all the conversions up, first go in one direction...
		while (origIndex < destIndex) {
			applyChangesFromVersionToVersion(knownVersions.get(origIndex), knownVersions.get(origIndex + 1));
			origIndex++;
		}
		
		// ... and then in the other, whatever ;)
		while (destIndex < origIndex) {
			applyChangesFromVersionToVersion(knownVersions.get(origIndex), knownVersions.get(origIndex - 1));
			origIndex--;
		}
	}
	
	/**
	 * Go from one version to another - the version strings have already been changed,
	 * this here only changes the actual layout of the CIs themselves such that they
	 * are valid; this will always be called with orig and dest being only separated
	 * by one step, so not need to check e.g. for orig 1.12 and dest 1.14.0 - instead,
	 * all the changes are applied gradually
	 */
	private void applyChangesFromVersionToVersion(String orig, String dest) {

		/*
		Examples for same CDM expressed in different versions:
		
		Example 1 in 1.12.1:
			<?xml version="1.0" encoding="UTF-8"?>
			<configurationcontrol:McmCI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:configurationcontrol="http://www.esa.int/dme/ConfigurationTracking/1.12.1" xmi:id="_1" externalVersionLabel="1" isModified="false" onlineRevisionIdentifier="1" name="mcmRootCI">
			  <monitoringControlElement xmi:id="_2" name="mcmRoot" definition="_3"/>
			  <monitoringControlElementDefinition xmi:id="_3" name="mcmRootDef"/>
			</configurationcontrol:McmCI>

		Example 1 in 1.13.0bd1:
			<?xml version="1.0" encoding="UTF-8"?>
			<configurationcontrol:McmCI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:configurationcontrol="http://www.esa.int/ConfigurationTracking/1.13.0bd1" xmi:id="_1" externalVersionLabel="1" onlineRevisionIdentifier="1" name="mcmRootCI">
			  <monitoringControlElement xmi:id="_2" name="mcmRoot" definition="_3"/>
			  <monitoringControlElementDefinition xmi:id="_3" name="mcmRootDef"/>
			</configurationcontrol:McmCI>
		
		=> the isModified="false" in the McmCI is missing in 1.13.0bd1... is that important?

		Example 2 in 1.13.0bd1:
			  <arguments xsi:type="monitoringcontrolmodel:EngineeringArgument" xmi:id="_1">
				<engineeringDefaultValue xsi:type="monitoringcontrolmodel:EngineeringArgumentValue" xmi:id="_2">
				  <value xsi:type="monitoringcontrolmodel:ParameterRawValue" xmi:id="_3" value="9"/>
				</engineeringDefaultValue>
				<engineeringArgumentDefinition href="_4"/>
			  </arguments>
			  
		// TODO :: get example for 1.14.0b, so that we know if the changes appeared from 1.13.0bd1 to 1.14.0b,
		// or from 1.14.0b to 1.14.0...

		Example 2 in 1.14.0:
			  <arguments xsi:type="monitoringcontrolmodel:EngineeringArgument" xmi:id="_1" name="EngArg1">
				<engineeringDefaultValue xsi:type="monitoringcontrolmodel:EngineeringArgumentValue" xmi:id="_2">
				  <value xsi:type="monitoringcontrolmodel:ParameterEngValue" xmi:id="_3" value="9"/>
				</engineeringDefaultValue>
				<engineeringArgumentDefinition href="_4"/>
			  </arguments>

		=> the name was added to the <arguments> (we can use the name of the definition, and add "...Def" to the definition name)
		=> ParameterRawValue inside EngineeringArgumentValues was transformed into ParameterEngValue
		=> a value is actually necessary inside an engineeringDefaultValue (in this example, the value was there, but I think
		   I saw a 1.13.0bd1 CDM with a engineeringDefaultValue that contained actually no value xD); instead of setting a value
		   in that case we can just delete the entire engineeringDefaultValue instance!
		*/
		
		switch (orig) {
			case "1.13.0bd1":
				switch (dest) {
					case "1.14.0b":
						// TODO :: adjust names and arguments etc. as seen in example 1
					break;
				}
				break;
			case "1.14.0b":
				switch (dest) {
					case "1.13.0bd1":
						// TODO :: adjust names and arguments etc. back as seen in example 1
					break;
				}
				break;
		}
	}

	/**
	 * Get the CDM version that this CDM file belongs to, or null if none can be identified.
	 */
	public String getCdmVersion() {

		try {
			Node root = getRoot();

			NamedNodeMap scriptAttributes = root.getAttributes();
			String cdmVersion = scriptAttributes.getNamedItem("xmlns:configurationcontrol").getNodeValue();

			String searchFor = "/" + CdmCtrl.CDM_NAMESPACE_MIDDLE;
			
			if (cdmVersion.contains(searchFor)) {
				cdmVersion = cdmVersion.substring(cdmVersion.indexOf(searchFor) + searchFor.length());
			}

			return cdmVersion;

		} catch (NullPointerException e) {

			return null;
		}
	}

	/**
	 * Get the CDM version prefix that this CDM file belongs to, or null if none can be identified.
	 */
	public String getCdmVersionPrefix() {

		try {
			Node root = getRoot();

			NamedNodeMap scriptAttributes = root.getAttributes();
			String cdmVersionPrefix = scriptAttributes.getNamedItem("xmlns:configurationcontrol").getNodeValue();

			String searchFor = "/" + CdmCtrl.CDM_NAMESPACE_MIDDLE;
			
			if (cdmVersionPrefix.contains(searchFor)) {
				cdmVersionPrefix = cdmVersionPrefix.substring(0, cdmVersionPrefix.indexOf(searchFor) + 1);
			}

			return cdmVersionPrefix;

		} catch (NullPointerException e) {

			return null;
		}
	}

	public void delete() {

		// remember for later that we have been deleted
		deleted = true;
	}

	public void save() {

		if (deleted) {
			// if deleted, then actually delete the file from disk
			super.delete();
		} else {
			// actually save the file for real :)
			super.save();
		}
	}

}
