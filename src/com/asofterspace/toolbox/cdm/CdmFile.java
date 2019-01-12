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


	/**
	 * Check if the CDM file in isolation is valid (for checking
	 * the full CDM, look into CdmCtrl.checkValidity()!)
	 * returns 0 if it is valid, and the amount of problems
	 * encountered if it is not;
	 * in the case of it not being valid, the List<String>
	 * that has been passed in will be filled with more detailed
	 * explanations about why it is not valid
	 */
	public int checkValidity(List<String> outProblemsFound) {

		// innocent unless proven otherwise
		int verdict = 0;

		// TODO :: check that in version 1.14.0, all arguments have names and arg values have names and values!
		// (and eng args have eng values rather than raw values...)

		// TODO :: check that all activity mappers are fully filled (e.g. no script or activity missing)

		// TODO :: check that all CIs have at least one child

		// check that all references actually lead to somewhere
		XmlElement startElement = getRoot();
		verdict += checkValidity_ReferenceLeadSomewhere(outProblemsFound, startElement);

		// check that every MCE has an MCE definition
		verdict += checkValidity_MCEsHaveDefinitions(outProblemsFound);

		// check that all values are adequate for their types
		// (e.g. limit checks with boolean values that are assigned to integer parameters will not work and,
		// if unreported, lead to wonky problems later on!)
		verdict += checkValidity_ValuesAndTypesAlign(outProblemsFound);

		return verdict;
	}

	private int checkValidity_ReferenceLeadSomewhere(List<String> outProblemsFound, XmlElement currentElement) {

		// innocent unless proven otherwise
		int verdict = 0;

		String nodeName = currentElement.getNodeName();

		if (nodeName != null) {

			// TODO :: check even more kinds of references!

			switch (nodeName) {
				case "monitoringControlElement":
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "definition");
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "controlledSystemRootDefinition");
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "defaultServiceAccessPoint");
					verdict += checkValidity_ReferenceLeadSomewhere_Multi(outProblemsFound, currentElement, "subElements");
				break;

				case "monitoringControlElementAspects":
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "baseElement");
				break;

				case "arguments":
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "engineeringArgumentDefinition");
				break;

				case "checksForParameterMonitoring":
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "highLimitEvent");
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "lowLimitEvent");
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "limitCheckDefinition");
				break;

				case "abstractDataType":
					verdict += checkValidity_ReferenceLeadSomewhere_Multi(outProblemsFound, currentElement, "realDisplayFormat");
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "lengthEncoding");
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "byteStreamDisplayFormat");
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "timeDisplayFormat");
					verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "stringDisplayFormat");
				break;
			}
		}

		// everything (and their dog) can contain an attribute called href that is then a single link
		verdict += checkValidity_ReferenceLeadSomewhere_Single(outProblemsFound, currentElement, "href");

		List<XmlElement> children = currentElement.getChildNodes();

		for (XmlElement child : children) {
			verdict += checkValidity_ReferenceLeadSomewhere(outProblemsFound, child);
		}

		return verdict;
	}

	private int checkValidity_ReferenceLeadSomewhere_Single(List<String> outProblemsFound, XmlElement currentElement, String key) {

		// innocent unless proven otherwise
		int verdict = 0;

		String ref = currentElement.getAttribute(key);
		if (ref != null) {
			if (cdmCtrl.getByUuid(ref) == null) {
				verdict++;
				outProblemsFound.add(this.getLocalFilename() + "#" + currentElement.getAttribute("xmi:id") +
					" has the attribute " + key + " which references the element " +
					ref + " - but it cannot be found!");
			}
		}

		return verdict;
	}

	private int checkValidity_ReferenceLeadSomewhere_Multi(List<String> outProblemsFound, XmlElement currentElement, String key) {

		// innocent unless proven otherwise
		int verdict = 0;

		String refs = currentElement.getAttribute(key);
		if (refs != null) {
			String[] refa = refs.split(" ");
			for (String ref : refa) {
				if ("".equals(ref)) {
					continue;
				}
				if (cdmCtrl.getByUuid(ref) == null) {
					verdict++;
					outProblemsFound.add(this.getLocalFilename() + "#" + currentElement.getAttribute("xmi:id") +
						" has the attribute " + key + " which references the element " +
						ref + " - but it cannot be found!");
				}
			}
		}

		return verdict;
	}

	private int checkValidity_ValuesAndTypesAlign(List<String> outProblemsFound) {

		// innocent unless proven otherwise
		int verdict = 0;

		List<XmlElement> engParas = domGetElems("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:EngineeringParameter");

		for (XmlElement engPara : engParas) {
			XmlElement engDataType = engPara.getChild("engineeringDataType");
			String engXsiDataType = engDataType.getAttribute("xsi:type");
			String href = engDataType.getAttribute("href");
			if (href != null) {
				XmlElement engDataTypeEl = cdmCtrl.getByUuid(href);
				String engXsiDataTypeEl = engDataTypeEl.getAttribute("xsi:type");
				if (!engXsiDataType.equals(engXsiDataTypeEl)) {
					verdict++;
					outProblemsFound.add(this.getLocalFilename() + "#" +
						engPara.getAttribute("xmi:id") +
						" has the type " + engXsiDataType +
						" but when we follow its reference we get element " +
						href + " with type " + engXsiDataTypeEl +
						" - these two really should be matching!");
				}
			}

			XmlElement engDispFormat = engPara.getChild("engineeringDisplayFormat");
			String engXsiDispFormat = engDispFormat.getAttribute("xsi:type");
			href = engDispFormat.getAttribute("href");
			if (href != null) {
				XmlElement engDispFormatEl = cdmCtrl.getByUuid(href);
				String engXsiDispFormatEl = engDispFormatEl.getAttribute("xsi:type");
				if (!engXsiDispFormat.equals(engXsiDispFormatEl)) {
					verdict++;
					outProblemsFound.add(this.getLocalFilename() + "#" +
						engPara.getAttribute("xmi:id") +
						" has the display format " + engXsiDispFormat +
						" but when we follow its reference we get element " +
						href + " with type " + engXsiDispFormatEl +
						" - these two really should be matching!");
				}
			}

			// usually, we want these to match exactly...
			if (!engXsiDispFormat.equals(engXsiDataType + "DisplayFormat")) {
				// ... but there are a few exceptions that are also okay...
				boolean okay = false;
				if (engXsiDispFormat.equals("monitoringcontrolcommon:StringDisplayFormat") &&
					engXsiDataType.equals("monitoringcontrolcommon:VariableString")) {
						okay = true;
				}
				if (engXsiDispFormat.equals("monitoringcontrolcommon:ByteStreamDisplayFormat") &&
					engXsiDataType.equals("monitoringcontrolcommon:VariableByteStream")) {
						okay = true;
				}
				if (engXsiDispFormat.equals("monitoringcontrolcommon:BitStreamDisplayFormat") &&
					engXsiDataType.equals("monitoringcontrolcommon:VariableBitStream")) {
						okay = true;
				}
				if (engXsiDispFormat.equals("monitoringcontrolcommon:TimeDisplayFormat") &&
					engXsiDataType.equals("monitoringcontrolcommon:AbsoluteCUCTime")) {
						okay = true;
				}
				if (engXsiDispFormat.equals("monitoringcontrolcommon:TimeDisplayFormat") &&
					engXsiDataType.equals("monitoringcontrolcommon:AbsoluteCDSTime")) {
						okay = true;
				}
				if (engXsiDispFormat.equals("monitoringcontrolcommon:TimeDisplayFormat") &&
					engXsiDataType.equals("monitoringcontrolcommon:EpochTime")) {
						okay = true;
				}
				if (engXsiDispFormat.equals("monitoringcontrolcommon:TimeDisplayFormat") &&
					engXsiDataType.equals("monitoringcontrolcommon:RelativeCUCTime")) {
						okay = true;
				}
				if (engXsiDataType.equals("monitoringcontrolcommon:EnumerationDataType")) {
					// TODO :: check enumerations more strictly!
					okay = true;
				}
				// ... however, if none of the exception occurred, then obviously this is NOT okay!
				if (!okay) {
					verdict++;
					outProblemsFound.add(this.getLocalFilename() + "#" +
						engPara.getAttribute("xmi:id") +
						" has the type " + engXsiDataType +
						" but it has the display format " + engXsiDispFormat +
						" - we would expect the display format " + engXsiDataType +
						"DisplayFormat!");
				}
			}

			XmlElement checksForPMon = engPara.getChild("checksForParameterMonitoring");
			if (checksForPMon != null) {
				String limitCheckDefHref = checksForPMon.getAttribute("limitCheckDefinition");
				if (engXsiDataType.equals("monitoringcontrolcommon:EnumerationDataType")) {
					verdict++;
					outProblemsFound.add(this.getLocalFilename() + "#" +
						engPara.getAttribute("xmi:id") +
						" has the type " + engXsiDataType +
						" but also has a limit check with id " +
						limitCheckDefHref +
						" - and EGS-CC does not like limit checks" +
						" for enumerations!");
				} else {
					XmlElement limitCheckDefEl = cdmCtrl.getByUuid(limitCheckDefHref);
					if (limitCheckDefEl != null) {
						for (XmlElement limit : limitCheckDefEl.getChildNodes()) {
							String value = limit.getAttribute("value");
							if (value != null) {
								if (engXsiDataType.startsWith("monitoringcontrolcommon:Unsigned")) {
									if (value.startsWith("-")) {
										verdict++;
										outProblemsFound.add(this.getLocalFilename() + "#" +
											engPara.getAttribute("xmi:id") +
											" has the type " + engXsiDataType +
											" but also has a limit check with id " +
											limitCheckDefHref +
											" which has " + limit.getNodeName() +
											" with value " + value +
											" - and negative values do not like being" +
											" limits for unsigned parameters!");
									}
									if (value.equals("true") || value.equals("false")) {
										verdict++;
										outProblemsFound.add(this.getLocalFilename() + "#" +
											engPara.getAttribute("xmi:id") +
											" has the type " + engXsiDataType +
											" but also has a limit check with id " +
											limitCheckDefHref +
											" which has " + limit.getNodeName() +
											" with value " + value +
											" - and booleans do not like being" +
											" limits for unsigned parameters!");
									}
								}
							}
						}
					}
				}
			}
		}

		return verdict;
	}

	private int checkValidity_MCEsHaveDefinitions(List<String> outProblemsFound) {

		// innocent unless proven otherwise
		int verdict = 0;

		List<XmlElement> mces = domGetElems("monitoringControlElement");

		for (XmlElement mce : mces) {

			String type = mce.getAttribute("xsi:type");

			String mceDefHref;

			if ("monitoringcontrolmodel:ControlledSystemRoot".equals(type)) {
				mceDefHref = mce.getAttribute("controlledSystemRootDefinition");
			} else {
				mceDefHref = mce.getAttribute("definition");
			}

			XmlElement mceDef = cdmCtrl.getByUuid(mceDefHref);

			if (mceDef == null) {
				verdict++;
				outProblemsFound.add(this.getLocalFilename() + "#" +
					mce.getAttribute("xmi:id") +
					" is a monitoringControlElement but has no definition attached!");
			} else {
				if (!"monitoringControlElementDefinition".equals(mceDef.getNodeName())) {
					verdict++;
					outProblemsFound.add(this.getLocalFilename() + "#" +
						mce.getAttribute("xmi:id") +
						" is a monitoringControlElement with definition " +
						mceDefHref + " - but the tag name of the definition is " +
						mceDef.getNodeName() + " instead of the expected monitoringControlElementDefinition!");
				}
			}
		}

		return verdict;
	}

}
