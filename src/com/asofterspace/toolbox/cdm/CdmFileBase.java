/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.EmfFile;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.utils.TinyMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the base of a CdmFile; we have put all the conversion stuff in here, which is A LOT, to not clutter up the
 * rest - so please only ever actually use the full CdmFile which extends this here :)
 */
public abstract class CdmFileBase extends EmfFile {

	protected CdmCtrl cdmCtrl;

	protected boolean deleted = false;

	protected String ciType;

	private static List<String> KNOWN_CDM_VERSIONS;
	private static List<String> KNOWN_CDM_PREFIXES;


	/**
	 * You can construct a CdmFile instance by basing it on an existing file object.
	 */
	public CdmFileBase(File regularFile, CdmCtrl cdmCtrl) {

		super(regularFile);

		this.cdmCtrl = cdmCtrl;

		XmlElement root = getRoot();

		if (root != null) {
			ciType = root.getTagName();
		}
	}

	public String getCiType() {
		return ciType;
	}

	public void setCiType(String newType) {

		getRoot().setTagName(newType);

		ciType = newType;
	}

	/**
	 * Convert this CDM file to the given version and prefix - if either is null,
	 * keep the current one
	 */
	public void convertTo(String toVersion, String toPrefix) {

		// sanitize our input
		String origVersion = getCdmVersion();

		if (toVersion == null) {
			toVersion = origVersion;
		}

		if (toPrefix == null) {
			toPrefix = getCdmVersionPrefix();
		}

		while (toVersion.startsWith("/")) {
			toVersion = toVersion.substring(1);
		}

		while (toPrefix.endsWith("/")) {
			toPrefix = toPrefix.substring(0, toPrefix.length() - 1);
		}

		// step through the detailed version changes
		conversionStepThroughChanges(origVersion, toVersion);

		// switch out the namespaces (namespaces can be modified in the previous step,
		// e.g. xmlns:PUSServicegeneric being added or removed, so this one has to come afterwards!)
		conversionSwitchNamespaces(toVersion, toPrefix);
	}

	private void conversionSwitchNamespaces(String toVersion, String toPrefix) {

		XmlElement root = getRoot();

		if (root == null) {
			return;
		}

		String configurationcontrol = root.getAttribute("xmlns:configurationcontrol");
		if (configurationcontrol != null) {
			root.setAttribute("xmlns:configurationcontrol", toPrefix + "/" + CdmCtrl.CDM_NAMESPACE_MIDDLE + toVersion);
		}

		String mcmimplementationitems = root.getAttribute("xmlns:mcmimplementationitems");
		if (mcmimplementationitems != null) {
			root.setAttribute("xmlns:mcmimplementationitems", toPrefix + "/MonitoringControl/MCMImplementationItems/" + toVersion);
		}

		String monitoringcontrolcommon = root.getAttribute("xmlns:monitoringcontrolcommon");
		if (monitoringcontrolcommon != null) {
			root.setAttribute("xmlns:monitoringcontrolcommon", toPrefix + "/MonitoringControl/MonitoringControlCommon/" + toVersion);
		}

		String mcmchecks = root.getAttribute("xmlns:mcmchecks");
		if (mcmchecks != null) {
			root.setAttribute("xmlns:mcmchecks", toPrefix + "/MonitoringControl/MonitoringControlModel/MCMChecks/" + toVersion);
		}

		String checkandcondition = root.getAttribute("xmlns:checkandcondition");
		if (checkandcondition != null) {
			root.setAttribute("xmlns:checkandcondition", toPrefix + "/MonitoringControl/MonitoringControlCommon/CheckAndCondition/" + toVersion);
		}

		String activitylist = root.getAttribute("xmlns:activitylist");
		if (activitylist != null) {
			root.setAttribute("xmlns:activitylist", toPrefix + "/MonitoringControlImplementation/ActivityList/" + toVersion);
		}

		String monitoringcontrolmodel = root.getAttribute("xmlns:monitoringcontrolmodel");
		if (monitoringcontrolmodel != null) {
			root.setAttribute("xmlns:monitoringcontrolmodel", toPrefix + "/MonitoringControl/MonitoringControlModel/" + toVersion);
		}

		String container = root.getAttribute("xmlns:container");
		if (container != null) {
			root.setAttribute("xmlns:container", toPrefix + "/MonitoringControlImplementation/Packetization/Packetization/Container/" + toVersion);
		}

		String mapping_mcm2packet = root.getAttribute("xmlns:mapping_mcm2packet");
		if (mapping_mcm2packet != null) {
			root.setAttribute("xmlns:mapping_mcm2packet", toPrefix + "/MonitoringControlImplementation/Packetization/Mapping_MCM2Packet/" + toVersion);
		}

		String mappingservicepacket = root.getAttribute("xmlns:mapping_service2packet");
		if (mappingservicepacket != null) {
			root.setAttribute("xmlns:mapping_service2packet", toPrefix + "/MonitoringControlImplementation/Packetization/Mapping_Service2Packet/" + toVersion);
		}

		String parameter = root.getAttribute("xmlns:parameter");
		if (parameter != null) {
			root.setAttribute("xmlns:parameter", toPrefix + "/MonitoringControlImplementation/Packetization/Packetization/Parameter/" + toVersion);
		}

		String pusservicegeneric = root.getAttribute("xmlns:PUSServicegeneric");
		if (pusservicegeneric != null) {
			root.setAttribute("xmlns:PUSServicegeneric", toPrefix + "/MonitoringControlImplementation/PusServiceLayer/PUS_Service_generic/" + toVersion);
		}

		String packetprocessing = root.getAttribute("xmlns:packetprocessing");
		if (packetprocessing != null) {
			root.setAttribute("xmlns:packetprocessing", toPrefix + "/PacketProcessing/" + toVersion);
		}

		// TODO :: if the version here ever differs from 1.5, we will need to start keeping track of that version relative to the CDM version too
		// (and start keeping that in mind for validation, etc.)

		// qudv examples that seem to be correct:
		// for 1.14.1: "http://www.esa.int/dme/core/qudv/conceptualmodel/1.5" (note: NOT aligned with generic version prefix of 1.14.1!)
		// for 1.14.0: "http://www.esa.int/dme/core/qudv/conceptualmodel/1.5" (note: NOT aligned with generic version prefix of 1.14.0!)
		// for 1.14.0b: "http://www.esa.int/dme/core/qudv/conceptualmodel/1.5"
		// for 1.13: unknown!
		// for 1.13.0bd1: "http://www.esa.int/core/qudv/conceptualmodel/1.5"
		// for 1.12.1: "http://www.esa.int/dme/core/qudv/conceptualmodel/1.5" (TODO: there also seems to be something like http://www.esa.int/dme/core/1.5!)
		// for 1.12: "http://www.scopeset.de/core/qudv/conceptualmodel/1.5" (TODO: there also seems to be something like http://www.scopeset.de/core/1.5!)
		// for 1.11.3: unknown!

		String qudvPrefix = "http://www.esa.int/dme/core/qudv/";

		switch (toVersion) {

			case "1.14.1":
			case "1.14.0":
			case "1.14.0b":
				qudvPrefix = "http://www.esa.int/dme/core/qudv/";
				break;

			// case "1.13": TODO :: UNKNOWN

			case "1.13.0bd1":
				qudvPrefix = "http://www.esa.int/core/qudv/";
				break;

			case "1.12.1":
				qudvPrefix = "http://www.esa.int/dme/core/qudv/";
				break;

			case "1.12":
				qudvPrefix = "http://www.scopeset.de/core/qudv/";
				break;

			// case "1.11.3": TODO :: UNKNOWN
		}

		String qudvBlocksExtModel = root.getAttribute("xmlns:qudv.blocks_extModel");
		if (qudvBlocksExtModel != null) {
			root.setAttribute("xmlns:qudv.blocks_extModel", qudvPrefix + "blocks/1.5");
		}

		String qudvConceptExtModel = root.getAttribute("xmlns:qudv.conceptualmodel_extModel");
		if (qudvConceptExtModel != null) {
			root.setAttribute("xmlns:qudv.conceptualmodel_extModel", qudvPrefix + "conceptualmodel/1.5");
		}
	}

	private void conversionStepThroughChanges(String origVersion, String toVersion) {

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

	// remove namespaces when going from 1.13.0bd1 down to 1.12.1
	private void removeNamespacesDownTo1121() {

		final String nsp = "namespace";

		if ("configurationcontrol:McmCI".equals(getCiType())) {
			domRemoveAttributeFromElems("value", "xsi:type", "parameter:RawPktParameterValue", nsp);
		}
		if ("configurationcontrol:Packet2ActivityMapperCI".equals(getCiType())) {
			domRemoveAttributeFromElems("packetActivityImpl", nsp);
		}
		if ("configurationcontrol:Procedure2ActivityMapperCI".equals(getCiType())) {
			domRemoveAttributeFromElems("procedureActivityImpl", nsp);
		}
		if ("configurationcontrol:ProcedureCI".equals(getCiType())) {
			domRemoveAttributeFromElems("procedure", nsp);
		}
		if ("configurationcontrol:ScriptCI".equals(getCiType())) {
			domRemoveAttributeFromElems("script", nsp);
		}
		if ("configurationcontrol:Script2ActivityMapperCI".equals(getCiType())) {
			domRemoveAttributeFromElems("scriptActivityImpl", nsp);
		}
		if ("configurationcontrol:SharedPacketCI".equals(getCiType())) {
			domRemoveAttributeFromElems("container", nsp);
			domRemoveAttributeFromElems("packet", nsp);
			domRemoveAttributeFromElems("pktParameter", nsp);
		}
		if ("configurationcontrol:UserDefinedDisplay2MceMapperCI".equals(getCiType())) {
			domRemoveAttributeFromElems("udd2mceMapper", nsp);
		}
		if ("configurationcontrol:UserDefinedDisplayCI".equals(getCiType())) {
			domRemoveAttributeFromElems("userDefinedDisplay", nsp);
		}
	}

	// add these namespaces both when going up from 1.12.1 and when going down from 1.14.0bd1 :)
	private void addNamespacesIfMissingFor1130bd1AsItLovesNamespaces() {

		// add namespace for containers and friends
		final String nsp = "namespace";
		final String dnsp = "defaultNamespace";

		if ("configurationcontrol:McmCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("value", "xsi:type", "parameter:RawPktParameterValue", nsp, dnsp);
		}
		if ("configurationcontrol:PacketCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("container", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("pktParameter", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("defaultValue", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("dataField", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("packet", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("parameterValue", nsp, dnsp);
		}
		if ("configurationcontrol:PacketProcessingCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("packetProtocolTypes", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("customProcessings", nsp, dnsp);
		}
		if ("configurationcontrol:Packet2ActivityMapperCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("packetActivityImpl", nsp, dnsp);
		}
		if ("configurationcontrol:Packet2ReportingDataMapperCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("packetEventImpl", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("packetParameterMCMParameterImpl", nsp, dnsp);
		}
		if ("configurationcontrol:Procedure2McmMapperCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("procedureActivityImpl", nsp, dnsp);
		}
		if ("configurationcontrol:ProcedureCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("procedure", nsp, dnsp);
		}
		if ("configurationcontrol:PUSServicesCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("applicationProcess", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("pUSService", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("serviceRequest", nsp, dnsp);
			domSetAttributeForNonHrefElemsIfAttrIsMissing("serviceReport", nsp, dnsp);
		}
		if ("configurationcontrol:PusService2PacketMapperCI".equals(getCiType())) {
			domSetAttributeForElemsIfAttrIsMissing("serviceRequestReportImplementation", nsp, dnsp);
			domSetAttributeForElemsIfAttrIsMissing("serviceParameterImpl", nsp, dnsp);
		}
		if ("configurationcontrol:ScriptCI".equals(getCiType())) {
			domSetAttributeForNonHrefElemsIfAttrIsMissing("script", nsp, dnsp);
		}
		if ("configurationcontrol:Script2ActivityMapperCI".equals(getCiType())) {
			domSetAttributeForElemsIfAttrIsMissing("scriptActivityImpl", nsp, dnsp);
		}
		if ("configurationcontrol:SharedPacketCI".equals(getCiType())) {
			domSetAttributeForElemsIfAttrIsMissing("container", nsp, dnsp);
			domSetAttributeForElemsIfAttrIsMissing("packet", nsp, dnsp);
			domSetAttributeForElemsIfAttrIsMissing("pktParameter", nsp, dnsp);
		}
		if ("configurationcontrol:UserDefinedDisplay2MceMapperCI".equals(getCiType())) {
			domSetAttributeForElemsIfAttrIsMissing("udd2mceMapper", nsp, dnsp);
		}
		if ("configurationcontrol:UserDefinedDisplayCI".equals(getCiType())) {
			domSetAttributeForElemsIfAttrIsMissing("userDefinedDisplay", nsp, dnsp);
		}
	}

	private String sanitizeForAut(String identifier) {

		// replace all non-word characters with underscores - so all non-a-zA-Z_0-9 characters
		identifier = identifier.trim().replaceAll("\\W", "_");

		// if the identifier starts with a number, don't let it start with a number
		if (identifier.matches("^\\d")) {
			identifier = "_" + identifier;
		}

		// if the identifier is empty, don't let it be empty
		if (identifier.length() < 1) {
			identifier = "_";
		}

		return identifier;
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

		=> the isModified="false" in every (!) CI is missing in 1.13.0bd1, as it became optional - so from 1.13.0bd1 down to 1.12.1,
		   add it in if it is missing, but the other way around keep it if it is there :)


		Example 2 in 1.13.0bd1:
			  <arguments xsi:type="monitoringcontrolmodel:EngineeringArgument" xmi:id="_1">
				<engineeringDefaultValue xsi:type="monitoringcontrolmodel:EngineeringArgumentValue" xmi:id="_2">
				  <value xsi:type="monitoringcontrolmodel:ParameterRawValue" xmi:id="_3" value="9"/>
				</engineeringDefaultValue>
				<engineeringArgumentDefinition href="_4"/>
			  </arguments>

		Example 2 in 1.14.0b: << not 100% sure about this example, but it should be correct and we used it as the basis for now...
			  <arguments xsi:type="monitoringcontrolmodel:EngineeringArgument" xmi:id="_1" name="EngArg1">
				<engineeringDefaultValue xsi:type="monitoringcontrolmodel:EngineeringArgumentValue" xmi:id="_2">
				  <value xsi:type="monitoringcontrolmodel:ParameterRawValue" xmi:id="_3" value="9"/>
				</engineeringDefaultValue>
				<engineeringArgumentDefinition href="_4"/>
			  </arguments>

		=> the name was added to the <arguments> (we can use the name of the definition, and add "...Def" to the definition name)

		Example 2 in 1.14.0:
			  <arguments xsi:type="monitoringcontrolmodel:EngineeringArgument" xmi:id="_1" name="EngArg1">
				<engineeringDefaultValue xsi:type="monitoringcontrolmodel:EngineeringArgumentValue" xmi:id="_2">
				  <value xsi:type="monitoringcontrolmodel:ParameterEngValue" xmi:id="_3" value="9"/>
				</engineeringDefaultValue>
				<engineeringArgumentDefinition href="_4"/>
			  </arguments>

		=> ParameterRawValue inside EngineeringArgumentValues was transformed into ParameterEngValue


		Example 3 in 1.12.1:
			<configurationcontrol:PacketCI ...
			  <packet xmi:id="_1" name="ChangeIcuModeUIntEnum">
				<descriptions xmi:id="_2" language="English" description="description" qualifier="Short"/>
				<trailer href="_3"/>
				<dataField xmi:id="_4" name="ChangeIcuMode_dataField">
				  <innerElements xsi:type="container:innerContainer" xmi:id="_5" offset="0" repetitionNumber="1">
				<container href="_6"/>
				  </innerElements>
				  <innerElements xsi:type="container:innerContainer" xmi:id="_7" offset="0" repetitionNumber="1" container="_8"/>
				</dataField>

		Example 3 in 1.13.0bd1:
			<configurationcontrol:PacketCI ...
			  <packet xmi:id="_1" name="ChangeIcuModeUIntEnum" namespace="namespace">
				<descriptions xmi:id="_2" language="English" description="description" qualifier="Short"/>
				<trailer href="_3"/>
				<dataField xmi:id="_4" name="ChangeIcuMode_dataField" namespace="namespace">
				  <innerElements xsi:type="container:InnerContainer" xmi:id="_5" offset="0" repetitionNumber="1">
				<container href="_6"/>
				  </innerElements>
				  <innerElements xsi:type="container:InnerContainer" xmi:id="_7" offset="0" repetitionNumber="1" container="_8"/>
				</dataField>

		=> a namespace was added to packets and dataFields (but it is optional - so it does not have to be added for 1.13.0bd1,
		   but it has to be removed when going backwards!)
		=> <innerElements/> with xsi:type="container:innerContainer" have been changed to xsi:type="container:InnerContainer"


		Example 4 in 1.12.1:
			<configurationcontrol:PacketCI
			  <container name="containerName" xmi:id="_1" xsi:type="container:ParameterContainer">
				<innerParameters offset="0" parameter="_2" timeOffset="0.0" xmi:id="_3"/>
				<innerParameters offset="2" parameter="_4" timeOffset="0.0" xmi:id="_5"/>
			  </container>

		Example 4 in 1.13.0bd1:
			<configurationcontrol:PacketCI
			  <container xmi:id="_1" name="containerName" namespace="namespace">
				<innerElements xsi:type="container:InnerParameter" xmi:id="_3" offset="0" timeOffset="0.0" parameter="_2"/>
				<innerElements xsi:type="container:InnerParameter" xmi:id="_5" offset="0" timeOffset="0.0" parameter="_4"/>
			  </container>

		Example 4 in 1.13.0bd1 (we think, but definitely possible in 1.14.0, but the previous one also still possible), alternative way of writing this down:
			<configurationcontrol:PacketCI
			  <container xmi:id="_1" name="containerName" namespace="namespace">
				<innerElements xsi:type="container:InnerParameter" xmi:id="_3" offset="0" timeOffset="0.0">
				  <parameter xsi:type="parameter:SimplePktParameter" href="_2"/>
				</innerElements>
				<innerElements xsi:type="container:InnerParameter" xmi:id="_5" offset="0" timeOffset="0.0">
				  <parameter xsi:type="parameter:SimplePktParameter" href="_4"/>
				</innerElements>
			  </container>

		=> a namespace was added to containers (it is optional - so it does not have to be added for 1.13.0bd1,
		   but it has to be removed when going backwards!)
		=> containers lost their xsi:type
		=> innerParameters became innerElements with xsi:type container:InnerParameter
		=> there is an alternative way of splitting out the parameters and giving them an extra xsi:type,
		   but let's ignore that for now (mostly we do forward conversion anyway, and there this does not
		   have to be considered... TODO :: actually consider this for backwards conversions, too!)


		Example 5 in 1.13.0bd1:
			<configurationcontrol:DataTypesCI ...
			  <abstractDataType xsi:type="monitoringcontrolcommon:EnumerationDataType" xmi:id="_1" name="name" isMostSignificantBitFirst="true" bitLength="32">
				<enumDisplayFormat href="_2"/>
			  </abstractDataType>
			</configurationcontrol:DataTypesCI>


		Example 5 in 1.14.0b:
			<configurationcontrol:DataTypesCI ...
			  <abstractDataType xsi:type="monitoringcontrolcommon:EnumerationDataType" xmi:id="_1" name="name" isMostSignificantBitFirst="true" bitLength="32" defaultText="default">
				<enumDisplayFormat href="_2"/>
				<enumerationLiterals xmi:id="_3" x="1" y="one"/>
			  </abstractDataType>
			</configurationcontrol:DataTypesCI>

		=> a defaultText was added
		=> a monitoringcontrolcommon:EnumerationDataType now needs to contain at least one enumerationLiterals instance


		Example 6 in 1.14.0b: << definitely in 1.13.0bd1, but based on example 2 we assume actually in 1.14.0b the same as in 1.13.0bd1
			<monitoringControlElementAspects ... xsi:type="monitoringcontrolmodel:EngineeringParameter">
				<defaultValue value="..." xmi:id="_1" xsi:type="monitoringcontrolmodel:ParameterRawValue"/>

		Example 6 in 1.14.0:
			<monitoringControlElementAspects ... xsi:type="monitoringcontrolmodel:EngineeringParameter">
				<defaultValue value="..." xmi:id="_1" xsi:type="monitoringcontrolmodel:ParameterEngValue"/>

		=> ParameterRawValue inside EngineeringParameters was transformed into ParameterEngValue
		=> this is basically like example 2, but different, because everyone is trying to confuse us as much as possible!
		*/

		switch (orig) {

			case "1.11.3":
				switch (dest) {
					// up
					case "1.12":
						break;
				}
				break;

			case "1.12":
				switch (dest) {
					// down
					case "1.11.3":
						break;

					// up
					case "1.12.1":

						// set name for the CI
						switch (getCiType()) {
							case "configurationcontrol:DataTypesCI":
							case "configurationcontrol:McmCI":
							case "configurationcontrol:ScriptCI":

								String defaultCiName = getLocalFilename();

								if (defaultCiName.toLowerCase().endsWith(".cdm")) {
									defaultCiName = defaultCiName.substring(0, defaultCiName.length() - 4);
								}

								domSetAttributeForElemsIfAttrIsMissing(getCiType(), "name", defaultCiName);
								domSetAttributeForElemsIfAttrIsMissing(getCiType(), "isModified", "false");
						}

						if ("configurationcontrol:McmCI".equals(getCiType())) {
							// rename grammarBasedExpression to expression
							domRenameChildrenOfElems("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:SyntheticParameter", "grammarBasedExpression", "expression");
						}

						break;
				}
				break;

			case "1.12.1":
				switch (dest) {
					// down
					case "1.12":
						// TODO :: do we need to un-do adding names? (or are names for McmCIs etc. allowed, but optional in 1.12?)

						if ("configurationcontrol:McmCI".equals(getCiType())) {
							// rename expression back to grammarBasedExpression
							domRenameChildrenOfElems("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:SyntheticParameter", "expression", "grammarBasedExpression");
						}

						break;

					// up
					case "1.13.0bd1":
						// deleting the isModified attribute (see example 1) is NOT necessary, as it is still valid in 1.13.0bd1

						if ("configurationcontrol:McmCI".equals(getCiType())) {
							// rename activityInhibtionPeriod to activityInhibitionPeriod
							domRenameAttributes("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:Event", "activityInhibtionPeriod", "activityInhibitionPeriod");

							// rename callibration to calibration - observed for xsi:type="monitoringcontrolmodel:EngineeringArgumentDefinition"
							domRenameAttributes("monitoringControlElementAspects", "callibration", "calibration");

							// rename deducableArguments to deducibleArguments - observed for monitoringcontrolmodel:DeducedArgumentDefinition
							domRenameAttributes("monitoringControlElementAspects", "deducableArguments", "deducibleArguments");

							domRenameAttributes("applicationReference", "aplicationIdentifier", "applicationIdentifier");

							// set hasPredictedValue which became mandatory
							domSetAttributeForElemsIfAttrIsMissing("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:Event", "hasPredictedValue", "false");
							domSetAttributeForElemsIfAttrIsMissing("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:EngineeringArgumentDefinition", "hasPredictedValue", "false");

							// iterate over all events and set their eventtype also for their definitions
							// (this should have also already been part of 1.12.1, but apparently in some CDMs this was missing anyway...)
							List<XmlElement> events = domGetElems("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:Event");
							for (XmlElement event : events) {
								String baseHref = null;
								String typeHref = null;

								for (XmlElement child : event.getChildNodes()) {
									if ("baseElement".equals(child.getTagName())) {
										baseHref = child.getAttribute("href");
									}
									if ("eventType".equals(child.getTagName())) {
										typeHref = child.getAttribute("href");
									}
								}

								if ((baseHref != null) && (typeHref != null)) {
									CdmNode eventDef = cdmCtrl.getByUuid(baseHref);
									XmlElement eventTypeEl = eventDef.createChild("eventType");
									// TODO :: actually resolve the proper local path in the link - here we just copy the path from the other file,
									// which might not be right!
									eventTypeEl.setAttribute("href", typeHref);
								}
							}

							// rename all fields, giving them names that the automation component is happy with...
							List<XmlElement> fields = domGetElems("fields");
							for (XmlElement field : fields) {
								String name = field.getAttribute("name");
								if (name == null) {
									field.setAttribute("name", "field_" + cdmCtrl.getFieldCounter());
								} else {
									field.setAttribute("name", sanitizeForAut(name));
								}
							}

							// iterate over all mce aspects, and make their names unique - probably having them non-unique was not a problem
							// before IR3, as AUT was not really in use / not using them all, but definitely from IR3 onwards having these
							// names non-unique was troublesome!
							List<String> knownMceAspectNames = cdmCtrl.getKnownMceAspectNames();

							// only uniquify the mce aspects, NOT the mce definition aspects - however, if the definitions are actually
							// attached to the mce aspects as they should be, then they will be updated accordingly anyway
							List<XmlElement> mceAspects = domGetChildrenOfElems("monitoringControlElement", "monitoringControlElementAspects");

							for (XmlElement mceAspect : mceAspects) {

								String name = mceAspect.getAttribute("name");

								if (knownMceAspectNames.contains(name)) {

									int i = 2;

									while (knownMceAspectNames.contains(name + "_" + i)) {
										i++;
									}

									name = name + "_" + i;

									mceAspect.setAttribute("name", name);

									// also set the name of the definition, if there is one...
									CdmNode baseElement = cdmCtrl.getByUuid(mceAspect.getLinkFromAttrOrChild("baseElement"));

									if (baseElement != null) {
										baseElement.setAttribute("name", name);
									}
								}

								knownMceAspectNames.add(name);
							}

							// add a referenceValue to all ValueDeltaCheckDefinitions
							List<XmlElement> vdcdefs = domGetElems("monitoringControlElementAspects", "xsi:type", "checkandcondition:ValueDeltaCheckDefinition");

							for (XmlElement vdcdef : vdcdefs) {
								XmlElement refVal = vdcdef.createChild("referenceValue");
								refVal.setAttribute("xmi:id", UuidEncoderDecoder.generateEcoreUUID());
								cdmCtrl.addToModel(this, refVal);
							}
						}

						if ("configurationcontrol:PacketCI".equals(getCiType())) {
							// transforming <innerElements/> with xsi:type="container:innerContainer"
							// to xsi:type="container:InnerContainer" (see example 3)
							domSetAttributeForElems("innerElements", "xsi:type", "container:innerContainer",
								"xsi:type", "container:InnerContainer");

							// adding a namespace to packets, dataFields (see example 3)
							// and containers (see example 4) is NOT necessary,
							// as it is just optional in 1.13.0bd1

							// remove the xsi:type from containers (see example 4)
							domRemoveAttributeFromElems("container", "xsi:type");
							domRemoveAttributeFromElems("dataField", "xsi:type");

							// innerParameters became innerElements with xsi:type container:InnerParameter (see example 4)
							domSetAttributeForElems("innerParameters", "xsi:type", "container:InnerParameter");
							domRenameElems("innerParameters", "innerElements");

							// add the name to containers (see example 4 - in 1.12 optional, in 1.13.0bd1 required)
							List<XmlElement> elems = domGetElems("container");
							for (XmlElement container : elems) {
								String contname = container.getAttribute("name");
								if (contname == null) {
									container.setAttribute("name", "container_" + cdmCtrl.getContainerCounter());
								}
							}
						}

						if ("configurationcontrol:PUSServicesCI".equals(getCiType())) {
							// rename globalWaitingMagin to globalWaitingMargin
							domRenameAttributes("applicationProcess", "globalWaitingMagin", "globalWaitingMargin");
							domSetAttributeForElemsIfAttrIsMissing("applicationProcess", "globalWaitingMargin", "0");

							// rename checksStartOfExectuion to checksStartOfExecution
							domRenameAttributes("applicationProcess", "checksStartOfExectuion", "checksStartOfExecution");
						}

						if ("configurationcontrol:PusService2PacketMapperCI".equals(getCiType())) {
							// rename SimplePktParameter to pktParameter
							domSetAttributeForElems("configurationcontrol:PusService2PacketMapperCI", "xmlns:parameter", "placeholder");
							domSetAttributeForElemsIfAttrIsMissing("SimplePktParameter", "xsi:type", "parameter:SimplePktParameter");
							domRenameElems("SimplePktParameter", "pktParameter");
						}

						if ("configurationcontrol:UserDefinedDisplay2MceMapperCI".equals(getCiType())) {
							domRenameChildrenOfElems("udd2mceMapper", "monitoringControlElement", "displayContext");
						}

						// rename procedure mapper CI
						if ("configurationcontrol:Procedure2ActivityMapperCI".equals(getCiType())) {
							setCiType("configurationcontrol:Procedure2McmMapperCI");
						}

						if ("configurationcontrol:Packet2ActivityMapperCI".equals(getCiType())) {
							domRemoveChildrenFromElems("packetActivityImpl", "serviceAccessPoint");
						}

						if ("configurationcontrol:Script2ActivityMapperCI".equals(getCiType())) {
							domRemoveChildrenFromElems("scriptActivityImpl", "serviceAccessPoint");
						}

						if ("configurationcontrol:DataTypesCI".equals(getCiType())) {
							domSetAttributeForElemsIfAttrIsMissing("abstractDataType", "xsi:type", "monitoringcontrolcommon:BitStream", "bitLength", "1");
							domSetAttributeForElemsIfAttrIsMissing("abstractDataType", "xsi:type", "monitoringcontrolcommon:ByteStream", "byteLength", "1");
						}

						addNamespacesIfMissingFor1130bd1AsItLovesNamespaces();

						break;
				}
				break;

			case "1.13.0bd1":
				switch (dest) {
					// down
					case "1.12.1":
						// let's add the isModified attribute with default false (see example 1) in case it is missing!
						{
							XmlElement root = getRoot();
							String isModified = root.getAttribute("isModified");
							if (isModified == null) {
								root.setAttribute("isModified", "false");
							}
						}

						if ("configurationcontrol:McmCI".equals(getCiType())) {
							// rename activityInhibitionPeriod back to activityInhibtionPeriod
							domRenameAttributes("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:Event", "activityInhibitionPeriod", "activityInhibtionPeriod");

							// rename calibration back to callibration - observed for xsi:type="monitoringcontrolmodel:EngineeringArgumentDefinition"
							domRenameAttributes("monitoringControlElementAspects", "calibration", "callibration");

							// rename deducibleArguments back to deducableArguments - observed for monitoringcontrolmodel:DeducedArgumentDefinition
							domRenameAttributes("monitoringControlElementAspects", "deducibleArguments", "deducableArguments");

							domRenameAttributes("applicationReference", "applicationIdentifier", "aplicationIdentifier");

							// hasPredictedValue does not need to be removed from events and engineering argument definitions, as it was optional in 1.12.1

							// remove referenceValue from all ValueDeltaCheckDefinitions
							domRemoveChildrenFromElems("monitoringControlElementAspects", "xsi:type", "checkandcondition:ValueDeltaCheckDefinition", "referenceValue");
							// TODO :: remove from cdm ctrl model (maybe add return value to the previous function for that)
						}

						if ("configurationcontrol:PacketCI".equals(getCiType())) {
							// transforming <innerElements/> with xsi:type="container:InnerContainer"
							// back to xsi:type="container:innerContainer" (see example 3)
							domSetAttributeForElems("innerElements", "xsi:type", "container:InnerContainer",
								"xsi:type", "container:innerContainer");

							// removing the namespace from packets, dataFields (see example 3)
							// and containers (see example 4)
							domRemoveAttributeFromElems("packet", "namespace");
							domRemoveAttributeFromElems("dataField", "namespace");
							domRemoveAttributeFromElems("container", "namespace");

							/*
							oookay, so the situation is a little bit complicated:
							if a container only has innerElements that are of type container:InnerParameter,
							  then the container should get the type container:ParameterContainer and the
							  innerElements should be transformed into innerParameters without type
							however, if the container has other elements (such as innerElements with type
							  container:innerContainer), then it should not be transformed
							in any case however, the generic container that is not a ParameterContainer
							  also works for parameters that are expressed as innerElements, so let's
							  just keep all the containers generic and do NOT convert back :)

							// add the xsi:type to containers (see example 4)
							domSetAttributeForNonHrefElemsIfAttrIsMissing("container", "xsi:type", "container:ParameterContainer");

							// innerParameters became innerElements with xsi:type container:InnerParameter (see example 4)
							// - and now we transform them back! ;)
							domRenameElems("innerElements", "xsi:type", "container:InnerParameter",
								"innerParameters");
							domRemoveAttributeFromElems("innerParameters", "xsi:type");
							*/

							// no need to remove container name - as the name was optional, but present, in 1.12.1

							// no need to rename the fields back

							// no need to un-uniquify the mce aspect names
						}

						if ("configurationcontrol:PUSServicesCI".equals(getCiType())) {
							// rename globalWaitingMargin back to globalWaitingMagin
							domRenameAttributes("applicationProcess", "globalWaitingMargin", "globalWaitingMagin");

							// rename checksStartOfExecution back to checksStartOfExectuion
							domRenameAttributes("applicationProcess", "checksStartOfExecution", "checksStartOfExectuion");
						}

						if ("configurationcontrol:PusService2PacketMapperCI".equals(getCiType())) {
							// rename pktParameter back to SimplePktParameter
							// (could also be a 1.14.0b > 1.14.0 change instead!)
							domRemoveAttributeFromElems("pktParameter", "xsi:type", "parameter:SimplePktParameter", "xsi:type");
							domRenameElems("pktParameter", "SimplePktParameter");
							if (!domIsTagPrefixInUse("parameter:")) {
								domRemoveAttributeFromElems("configurationcontrol:PusService2PacketMapperCI", "xmlns:parameter");
							}
						}

						if ("configurationcontrol:UserDefinedDisplay2MceMapperCI".equals(getCiType())) {
							domRenameChildrenOfElems("udd2mceMapper", "displayContext", "monitoringControlElement");
						}

						// rename procedure mapper CI back
						if ("configurationcontrol:Procedure2McmMapperCI".equals(getCiType())) {
							setCiType("configurationcontrol:Procedure2ActivityMapperCI");
						}

						if ("configurationcontrol:Packet2ActivityMapperCI".equals(getCiType())) {
							// TODO :: for each element with tag name packetActivityImpl, add a child with tag name serviceAccessPoint
							// with attribute href pointing (correctly, going over file boundaries and quoting whitespace in the name)
							// to a useful default service access point xmi:id
						}

						if ("configurationcontrol:Script2ActivityMapperCI".equals(getCiType())) {
							// TODO :: for each element with tag name scriptActivityImpl, add a child with tag name serviceAccessPoint
							// with attribute href pointing (correctly, going over file boundaries and quoting whitespace in the name)
							// to a useful default service access point xmi:id
						}

						// bytestream and bitstream lengths do not need to be removed, as they were optional in 1.12.1

						// some namespaces have to be removed, as they do not exist in 1.12.1
						removeNamespacesDownTo1121();

						break;

					// up
					case "1.13":
						break;
				}
				break;

			case "1.13":
				switch (dest) {
					// down
					case "1.13.0bd1":
						break;

					// up
					case "1.14.0b":
						// adjust names of arguments as seen in example 2
						if ("configurationcontrol:McmCI".equals(getCiType())) {

							List<XmlElement> elems = domGetElems("arguments");
							for (XmlElement argument : elems) {
								String argname = argument.getAttribute("name");
								if (argname == null) {

									// try getting the name from the definition, any definition will do - this is REALLY HELPFUL,
									// as then then automation scripts keep working!
									CdmNode node = cdmCtrl.getByUuid(argument.getLinkFromAttrOrChild("engineeringArgumentDefinition"));
									if (node == null) {
										node = cdmCtrl.getByUuid(argument.getLinkFromAttrOrChild("repeatArgumentDefinition"));
									}
									if (node == null) {
										node = cdmCtrl.getByUuid(argument.getLinkFromAttrOrChild("deducedArgumentDefinition"));
									}
									if (node == null) {
										node = cdmCtrl.getByUuid(argument.getLinkFromAttrOrChild("activityCallArgumentDefinition"));
									}
									if (node == null) {
										node = cdmCtrl.getByUuid(argument.getLinkFromAttrOrChild("aggregateArgumentDefinition"));
									}
									if (node == null) {
										node = cdmCtrl.getByUuid(argument.getLinkFromAttrOrChild("matrixArgumentDefinition"));
									}
									if (node == null) {
										node = cdmCtrl.getByUuid(argument.getLinkFromAttrOrChild("selectionArgumentDefinition"));
									}

									if (node != null) {
										if (node.getName() != null) {
											argument.setAttribute("name", node.getName());
											continue;
										}
									}

									// if no name could be found, set a silly default name
									argument.setAttribute("name", "argument_" + cdmCtrl.getArgumentCounter());
								}
							}

							// remove sourceDataType and sourceDisplayFormat, as the field no longer seems to exist
							// (could also be a 1.14.0b > 1.14.0 change instead!)
							// TODO :: implement the opposite for the other direction somehow
							domRemoveChildrenFromElems("monitoringControlElementAspects", "sourceDataType");
							domRemoveChildrenFromElems("monitoringControlElementAspects", "sourceDisplayFormat");

							// remove available arguments, as the field no longer seems to exist
							// (could also be a 1.14.0b > 1.14.0 change instead!)
							// TODO :: implement the opposite for the other direction somehow
							domRemoveAttributeFromElems("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:DeducedArgumentDefinition", "availableArguments");

							// make all reference values external ones, unless they are already something else
							domSetAttributeForElemsIfAttrIsMissing("referenceValue", "xsi:type", "checkandcondition:ExternalReferenceValue");
						}

						// adjust enumerations (see example 5)
						if ("configurationcontrol:DataTypesCI".equals(getCiType())) {

							domSetAttributeForElemsIfAttrIsMissing("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:EnumerationDataType",
								"defaultText", "default");

							// add a default enumerationLiteral, if there is none there
							List<XmlElement> enumerations = domGetElems("abstractDataType", "xsi:type", "monitoringcontrolcommon:EnumerationDataType");
							for (XmlElement enumeration : enumerations) {
								boolean hasLiteralChild = false;

								List<XmlElement> children = enumeration.getChildNodes();
								for (XmlElement child : children) {
									if ("enumerationLiterals".equals(child.getTagName())) {
										hasLiteralChild = true;
										break;
									}
								}

								// no child has been found... create one! :)
								if (!hasLiteralChild) {
									XmlElement newLiteral = enumeration.createChild("enumerationLiterals");
									// just add a mapping f(1) = "one" - what possible harm could be done? ^^
									newLiteral.setAttribute("x", "1");
									newLiteral.setAttribute("y", "one");
									newLiteral.setAttribute("xmi:id", UuidEncoderDecoder.generateEcoreUUID());
									cdmCtrl.addToModel(this, newLiteral);
								}
							}

							// add bitLength to reals, enums, ints, specials, ...
							domSetAttributeForElemsIfAttrIsMissing("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:Real", "bitLength", "32");
							domSetAttributeForElemsIfAttrIsMissing("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:EnumerationDataType", "bitLength", "8");
							domSetAttributeForElemsIfAttrIsMissing("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:SignedInteger", "bitLength", "32");
							domSetAttributeForElemsIfAttrIsMissing("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:Special32bits", "bitLength", "32");
							domSetAttributeForElemsIfAttrIsMissing("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:UnsignedInteger", "bitLength", "32");
						}

						// adjust packets - this here applies at least to the xsi:type="parameter:SimplePktParameter", but possibly all of them...
						if ("configurationcontrol:PacketCI".equals(getCiType())) {
							// Sooo a <packet> contains possibly several <parameterValues>, and each <parameterValues> has a parameter
							// attribute with the ID of a <pktParameter>.
							// In 1.13.0bd1, each <pktParameter> has a mandatory sourceType attribute, with the value "Telemetry",
							// "Command" or "TelemetryAndCommand".
							// In 1.14.0b, each <packet> has a packetType attribute with the value "TM" or "TC".
							// So mapping from 1.14.0b back to 1.13.0bd1 is easy - set Telemetry or Command to all pktParameters depending on their
							//   containing packet. The ones that are not contained in a packet can be Telemetry as a fine default.
							// However, mapping from 1.13.0bd1 to 1.14.0 is potentially lossy - if a pktParameter is TelemetryAndCommand,
							//   or if several pktParameters disagree, then the packet cannot be set to a perfect value - again, let's take
							//   TM as default for those cases.

							// So, in this direction, go through all <packet>s and get the sourceTypes of their <pktParameter>s
							List<XmlElement> packets = domGetElems("packet");
							for (XmlElement packet : packets) {

								// we now iterate over all <parameterValue>s and resolve the <pktParameter>s, checking if we
								// find "Telemetry" or "Command" or "TelemetryOrCommand" anywhere (in the end, if we found
								// Command but nothing else, assign TC, or else if we found no Command or if we also found
								// other entries, then assign TM)
								boolean foundCommand = false;
								boolean foundTelemetryOrElse = false;

								// get all associated <parameterValue>s...
								List<XmlElement> children = packet.getChildNodes();

								for (XmlElement child : children) {
									if ("parameterValues".equals(child.getTagName())) {

										// get attribute "parameter"
										String parameterAttr = child.getAttribute("parameter");

										if (parameterAttr == null) {
											continue;
										}

										// get that pktParameter instance from CdmCtrl
										CdmNode pktParameterNode = cdmCtrl.getByUuid(parameterAttr);

										if (pktParameterNode == null) {
											continue;
										}

										if (!"pktParameter".equals(pktParameterNode.getTagName())) {
											continue;
										}

										// set the pktParameter instance sourceType accordingly
										String sourceType = pktParameterNode.getAttribute("sourceType");

										if ("Command".equals(sourceType)) {
											// if we ONLY find Commands for all pktParameters, assign TC - so check further
											foundCommand = true;
										} else {
											// if we found at least one pktParameter with Telemetry, assign TM - so no need for more checking
											foundTelemetryOrElse = true;
											break;
										}
									}
								}

								if (foundCommand && !foundTelemetryOrElse) {
									packet.setAttribute("packetType", "TC");
								} else {
									packet.setAttribute("packetType", "TM");
								}
							}

							domRemoveAttributeFromElems("pktParameter", "sourceType");
						}

						if ("configurationcontrol:PUSServicesCI".equals(getCiType())) {

							// add type to verification stage
							// (could also be a 1.14.0b > 1.14.0 change instead!)

							domSetAttributeForElems("configurationcontrol:PUSServicesCI", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
							domSetAttributeForElems("configurationcontrol:PUSServicesCI", "xmlns:PUSServicegeneric", "placeholder");

							domSetAttributeForElemsIfAttrIsMissing("defaultVerificationStages", "xsi:type", "PUSServicegeneric:PUSVerificationStage");

							// do these changes need to be un-done on the way back, or can they be kept?
							domSetAttributeForElemsIfAttrIsMissing("defaultVerificationStages", "stageType", "TCAcceptance");
							domRemoveAttributeFromElems("defaultVerificationStages", "nextStage");
						}

						// rename units and quantities CI
						if ("configurationcontrol:UnitsAndQuantatiesCI".equals(getCiType())) {
							setCiType("configurationcontrol:UnitsAndQuantitiesCI");
						}

						// update automation imports
						// (could also be a 1.14.0b > 1.14.0 change instead!)
						// TODO :: also update the internal procedure cdm nodes!
						if ("configurationcontrol:ProcedureCI".equals(getCiType())) {

							List<XmlElement> procedures = domGetElems("procedure");

							for (XmlElement procedure : procedures) {
								String procedureContent = procedure.getAttribute("procedureContent");

								if (procedureContent == null) {
									continue;
								}

								if (procedureContent.contains("esa.egscc.test.area.IA08.")) {
									procedureContent = procedureContent.replaceAll("esa.egscc.test.area.IA08.", "esa.egscc.mcm.");

									procedure.setAttribute("procedureContent", procedureContent);
								}
							}
						}

						// do not remove the namespace for containers and friends, as the field still exists in 1.14.0b

						break;
				}
				break;

			case "1.14.0b":
				switch (dest) {
					// down
					case "1.13":
						// adjust names of arguments back as seen in example 2 - by removing them cold-bloodedly ;)
						if ("configurationcontrol:McmCI".equals(getCiType())) {
							domRemoveAttributeFromElems("arguments", "name");

							// no need to un-do making all reference values external ones, as this is optionally available in 1.13.0bd1
						}

						// adjust enumerations back (see example 5)
						if ("configurationcontrol:DataTypesCI".equals(getCiType())) {

							domRemoveAttributeFromElems("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:EnumerationDataType",
								"defaultText");

							// remove the enumeration literals (copy - they need to be removed!)
							domRemoveChildrenFromElems("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:EnumerationDataType",
								"enumerationLiterals");

							// do not remove bitLength from reals, enums, ints, specials, ... - as that was optional in 1.13.0bd1
						}

						// adjust packets back - this here applies at least to the xsi:type="parameter:SimplePktParameter", but possibly all of them...
						if ("configurationcontrol:PacketCI".equals(getCiType())) {
							// Sooo a <packet> contains possibly several <parameterValues>, and each <parameterValues> has a parameter
							// attribute with the ID of a <pktParameter>.
							// In 1.13.0bd1, each <pktParameter> has a mandatory sourceType attribute, with the value "Telemetry",
							// "Command" or "TelemetryAndCommand".
							// In 1.14.0b, each <packet> has a packetType attribute with the value "TM" or "TC".
							// So mapping from 1.14.0b back to 1.13.0bd1 is easy - set Telemetry or Command to all pktParameters depending on their
							//   containing packet. The ones that are not contained in a packet can be Telemetry as a fine default.
							// However, mapping from 1.13.0bd1 to 1.14.0 is potentially lossy - if a pktParameter is TelemetryAndCommand,
							//   or if several pktParameters disagree, then the packet cannot be set to a perfect value - again, let's take
							//   TM as default for those cases.

							// So, in this direction, go through all <packets> and their <parameterValues> and set the <pktParameter> sourceType accordingly...
							List<XmlElement> parameterValues = domGetChildrenOfElems("packet", "parameterValues");
							for (XmlElement parameterValue : parameterValues) {

								// figure out to what value we actually want to set the pktParameter sourceType...
								String packetType = parameterValue.getXmlParent().getAttribute("packetType");

								if (packetType == null) {
									continue;
								}

								// by default, use Command (as TC is the default in 1.14.0b that sometimes is actually left out)...
								String targetSourceType = "Command";

								// ... in case of TM, use Telemetry!
								if ("TM".equals(packetType)) {
									targetSourceType = "Telemetry";
								}

								// get attribute "parameter"
								String parameterAttr = parameterValue.getAttribute("parameter");

								if (parameterAttr == null) {
									continue;
								}

								// get that pktParameter instance from CdmCtrl
								CdmNode pktParameterNode = cdmCtrl.getByUuid(parameterAttr);

								if (pktParameterNode == null) {
									continue;
								}

								if (!"pktParameter".equals(pktParameterNode.getTagName())) {
									continue;
								}

								// set the pktParameter instance sourceType accordingly
								pktParameterNode.setAttribute("sourceType", targetSourceType);
							}

							// ... then set it to the default value for all the <pktParameter>s that are still left after this
							domSetAttributeForElemsIfAttrIsMissing("pktParameter", "sourceType", "Telemetry");

							// remove the packetType from the packets after we used it to figure out the correct sourceType for the pktParameter
							domRemoveAttributeFromElems("packet", "packetType");
						}

						if ("configurationcontrol:PUSServicesCI".equals(getCiType())) {
							// remove type from verification stage
							// (could also be a 1.14.0b > 1.14.0 change instead!)
							domRemoveAttributeFromElems("defaultVerificationStages", "xsi:type");
							if (!domIsTagPrefixInUse("PUSServicegeneric:")) {
								domRemoveAttributeFromElems("configurationcontrol:PUSServicesCI", "xmlns:PUSServicegeneric");
							}
							if (!domIsTagPrefixInUse("xsi:")) {
								domRemoveAttributeFromElems("configurationcontrol:PUSServicesCI", "xmlns:xsi");
							}
						}

						// rename units and quantities CI back
						if ("configurationcontrol:UnitsAndQuantitiesCI".equals(getCiType())) {
							setCiType("configurationcontrol:UnitsAndQuantatiesCI");
						}

						// update automation imports back
						// (could also be a 1.14.0b > 1.14.0 change instead!)
						// TODO :: also update the internal procedure cdm nodes!
						if ("configurationcontrol:ProcedureCI".equals(getCiType())) {

							List<XmlElement> procedures = domGetElems("procedure");

							for (XmlElement procedure : procedures) {
								String content = procedure.getAttribute("procedureContent");

								if (content == null) {
									continue;
								}

								if (content.contains("esa.egscc.mcm.")) {
									content = content.replaceAll("esa.egscc.mcm.", "esa.egscc.test.area.IA08.");

									procedure.setAttribute("procedureContent", content);
								}
							}
						}

						addNamespacesIfMissingFor1130bd1AsItLovesNamespaces();

						break;

					// up
					case "1.14.0":
						// adjust parameter raw to eng as seen in example 2
						if ("configurationcontrol:McmCI".equals(getCiType())) {

							/*
							// do this both for engineeringDefaultValue > value
							// and defaultValue xsi:type=monitoringcontrolmodel:EngineeringArgumentValue > value
							List<XmlElement> children = domGetChildrenOfElems("engineeringDefaultValue");
							children.addAll(domGetChildrenOfElems("defaultValue",
								"xsi:type", "monitoringcontrolmodel:EngineeringArgumentValue"));
							// also add defaultValues that are children of EngineeringParameters, see example 6
							children.addAll(domGetChildrenOfElems("monitoringControlElementAspects",
								"xsi:type", "monitoringcontrolmodel:EngineeringParameter"));
							for (XmlElement argValue : children) {
								// for example 2, take value, and for example 6, take defaultValue
								if (("value".equals(argValue.getTagName())) || ("defaultValue".equals(argValue.getTagName()))) {
									String argValueType = argValue.getAttribute("xsi:type");
									if (argValueType == null) {
										argValue.setAttribute("xsi:type", "monitoringcontrolmodel:ParameterEngValue");
									} else {
										if ("monitoringcontrolmodel:ParameterRawValue".equals(argValueType)) {
											argValue.setAttribute("xsi:type", "monitoringcontrolmodel:ParameterEngValue");
										}
									}
								}
							}
							*/

							// muuuch simpler: ParameterRawValue AND ParameterUncalibratedValue become ParameterSourceValue
							// and YES, there are ParameterRawValue instances inside engineering parameters, but that makes sense: engineering means they can have both raw and engineering, not just one OR the other!
							String[] tags = {"value", "defaultValue", "expectedValue", "highLimit", "lowLimit", "highDeltaThreshold", "lowDeltaThreshold"};
							domSetAttributeForElems(tags, "xsi:type", "monitoringcontrolmodel:ParameterRawValue", "xsi:type", "monitoringcontrolmodel:ParameterSourceValue");
							domSetAttributeForElems(tags, "xsi:type", "monitoringcontrolmodel:ParameterUncalibratedValue", "xsi:type", "monitoringcontrolmodel:ParameterSourceValue");
						}

						break;
				}
				break;

			case "1.14.0":
				switch (dest) {
					// down
					case "1.14.0b":
						// adjust parameters eng back to raw as seen in example 2
						if ("configurationcontrol:McmCI".equals(getCiType())) {

							/*
							// do this both for engineeringDefaultValue > value
							// and defaultValue xsi:type=monitoringcontrolmodel:EngineeringArgumentValue > value
							List<XmlElement> children = domGetChildrenOfElems("engineeringDefaultValue");
							children.addAll(domGetChildrenOfElems("defaultValue",
								"xsi:type", "monitoringcontrolmodel:EngineeringArgumentValue"));
							// also add defaultValues that are children of EngineeringParameters, see example 6
							children.addAll(domGetChildrenOfElems("monitoringControlElementAspects",
								"xsi:type", "monitoringcontrolmodel:EngineeringParameter"));
							for (XmlElement argValue : children) {
								// for example 2, take value, and for example 6, take defaultValue
								if (("value".equals(argValue.getTagName())) || ("defaultValue".equals(argValue.getTagName()))) {
									String argValueType = argValue.getAttribute("xsi:type");
									if (argValueType == null) {
										argValue.setAttribute("xsi:type", "monitoringcontrolmodel:ParameterRawValue");
									} else {
										if ("monitoringcontrolmodel:ParameterEngValue".equals(argValueType)) {
											argValue.setAttribute("xsi:type", "monitoringcontrolmodel:ParameterRawValue");
										}
									}
								}
							}
							*/

							// muuuch simpler: ParameterRawValue AND ParameterUncalibratedValue become ParameterSourceValue, so on the way back Source to Raw, and nothing to Uncal - never saw anyone use that ^^
							String[] tags = {"value", "defaultValue", "expectedValue", "highLimit", "lowLimit", "highDeltaThreshold", "lowDeltaThreshold"};
							domSetAttributeForElems(tags, "xsi:type", "monitoringcontrolmodel:ParameterSourceValue", "xsi:type", "monitoringcontrolmodel:ParameterRawValue");
						}

						break;

					// up
					case "1.14.1":

							domRemoveAttributeFromElems("externalScheduleInformation", "time");

						break;
				}
				break;

			case "1.14.1":
				switch (dest) {
					// down
					case "1.14.0":

							domSetAttributeForElems("externalScheduleInformation", "time", "1970-01-01T01:00:00.000");

						break;
				}
				break;
		}
	}

	// this one is right, the other one is left
	public List<String> findDifferencesFrom(CdmFileBase otherFile) {

		List<String> result = new ArrayList<>();

		XmlElement thisRoot = getRoot();
		XmlElement otherRoot = otherFile.getRoot();

		// do not complain if both files do not contain root elements
		if ((thisRoot == null) && (otherRoot == null)) {
			return result;
		}

		// do complain if only one does not contain a root element
		if (thisRoot == null) {
			result.add(getPathRelativeToCdmRoot() + " in the right CDM does not contain a root element.");
		}
		if (otherRoot == null) {
			result.add(otherFile.getPathRelativeToCdmRoot() + " in the left CDM does not contain a root element.");
		}

		findDifferencesRecursivelyFrom("", otherFile.getRoot(), "", getRoot(), result);

		return result;
	}

	private void findDifferencesRecursivelyFrom(String otherPath, XmlElement otherEl, String curPath, XmlElement curEl, List<String> outResult) {

		// check this node's name
		if (!curEl.getTagName().equals(otherEl.getTagName())) {
			outResult.add(getPathRelativeToCdmRoot() + " in the right CDM contains the element " + curPath + curEl.getTagName() + ", which has the name " + otherPath + otherEl.getTagName() + " in the left CDM.");
		}

		// check this node's attributes
		TinyMap curAttrs = curEl.getAttributes();
		TinyMap otherAttrs = otherEl.getAttributes();
		for (int i = 0; i < curAttrs.size(); i++) {
			// ignore the ID TODO :: make this behavior configurable!
			if ("xmi:id".equals(curAttrs.getKey(i))) {
				continue;
			}
			String otherNode = otherAttrs.get(curAttrs.getKey(i));
			if (otherNode == null) {
				outResult.add(getPathRelativeToCdmRoot() + " contains the element " + curPath + curEl.getTagName() + ", which in the right CDM contains the attribute \"" + curAttrs.getKey(i) + "\" that is missing from the left CDM.");
			} else {
				if (!curAttrs.get(i).equals(otherNode)) {
					outResult.add(getPathRelativeToCdmRoot() + " contains the element " + curPath + curEl.getTagName() + " with the attribute \"" + curAttrs.getKey(i) + "\", which in the right CDM has the value \"" + curAttrs.get(i) + "\" as opposed to the value \"" + otherNode + "\" in the left CDM.");
				}
			}
		}
		for (int i = 0; i < otherAttrs.size(); i++) {
			// ignore the ID TODO :: make this behavior configurable!
			if ("xmi:id".equals(otherAttrs.getKey(i))) {
				continue;
			}
			String curNode = curAttrs.get(otherAttrs.getKey(i));
			if (curNode == null) {
				outResult.add(getPathRelativeToCdmRoot() + " contains the element " + otherPath + otherEl.getTagName() + ", which in the left CDM contains the attribute \"" + otherAttrs.getKey(i) + "\" that is missing from the right CDM.");
			}
			// no need for the else here - if there was a different value, then it was already reported a few lines above ^^
		}

		// check this node's children
		List<XmlElement> rightChildEls = curEl.getChildNodes();
		List<XmlElement> leftChildEls = otherEl.getChildNodes();

		int childrenLen = rightChildEls.size();

		if (rightChildEls.size() > leftChildEls.size()) {
			childrenLen = leftChildEls.size();
			outResult.add(getPathRelativeToCdmRoot() + " in the right CDM contains the element " + curPath + curEl.getTagName() + ", which has more children than the corresponding element in the left CDM.");
		}

		if (leftChildEls.size() > rightChildEls.size()) {
			outResult.add(getPathRelativeToCdmRoot() + " in the left CDM contains the element " + otherPath + otherEl.getTagName() + ", which has more children than the corresponding element in the right CDM.");
		}

		curPath += curEl.getTagName() + ".";
		otherPath += otherEl.getTagName() + ".";

		for (int i = 0; i < childrenLen; i++) {
			findDifferencesRecursivelyFrom(otherPath, leftChildEls.get(i), curPath, rightChildEls.get(i), outResult);
		}
	}

	public String getPathRelativeToCdmRoot() {

		Directory cdmRootDir = cdmCtrl.getLastLoadedDirectory();
		Path cdmRootPath = cdmRootDir.getJavaFile().toPath().toAbsolutePath();
		Path cdmFilePath = getJavaFile().toPath().toAbsolutePath();

		Path relativePath = cdmRootPath.relativize(cdmFilePath);

		return relativePath.toString();
	}

	/**
	 * Get the CDM version that this CDM file belongs to, or null if none can be identified.
	 */
	public String getCdmVersion() {

		try {
			XmlElement root = getRoot();

			String cdmVersion = root.getAttribute("xmlns:configurationcontrol");

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
			XmlElement root = getRoot();

			String cdmVersionPrefix = root.getAttribute("xmlns:configurationcontrol");

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

	@Override
	protected String namespaceToElement(String namespace) {

		// TODO :: optimize this entire function if stuff is slow, as it seems... well... slow just code-wise xD

		if (KNOWN_CDM_VERSIONS == null) {
			KNOWN_CDM_VERSIONS = CdmCtrl.getKnownCdmVersions();
		}
		if (KNOWN_CDM_PREFIXES == null) {
			KNOWN_CDM_PREFIXES = CdmCtrl.getKnownCdmPrefixes();
		}

		for (int i = 0; i < KNOWN_CDM_VERSIONS.size(); i++) {
			String p = KNOWN_CDM_PREFIXES.get(i);
			String v = KNOWN_CDM_VERSIONS.get(i);
			if (namespace.equals(p + "ConfigurationTracking/" + v)) {
					return "configurationcontrol";
			}
			if (namespace.equals(p + "MonitoringControl/" + v)) {
					return "monitoringControlElement";
			}
			if (namespace.equals(p + "MonitoringControl/MonitoringControlModel/" + v)) {
					return "monitoringControlElement";
			}
			if (namespace.equals(p + "MonitoringControlImplementation/UserDefinedDisplays/" + v)) {
					return "userDefinedDisplay";
			}
			if (namespace.equals(p + "MonitoringControlImplementation/UserDefinedDisplays/Mapping_UDD2MCM/" + v)) {
					return "udd2mceMapper";
			}
		}

		switch (namespace) {
			// TODO :: also get all the different qudv versions
			case "http://www.scopeset.de/core/qudv/conceptualmodel/1.5":
				return "qudv.conceptualmodel_extModel";
			case "http://www.scopeset.de/core/1.5":
				return "xmi";
		}
		return "unknown(" + namespace + ")";
	}

}
