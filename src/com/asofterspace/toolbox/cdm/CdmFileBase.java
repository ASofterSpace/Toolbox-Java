package com.asofterspace.toolbox.cdm;

import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This is the base of a CdmFile; we have put all the conversion stuff in here, which is A LOT, to not clutter up the
 * rest - so please only ever actually use the full CdmFile which extends this here :)
 */
public abstract class CdmFileBase extends XmlFile {

	protected boolean deleted = false;

	protected String ciType;

	/**
	 * You can construct a CdmFile instance by basing it on an existing file object.
	 */
	public CdmFileBase(File regularFile) {

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

		// we need to switch out THIS namespace first, as otherwise root elements cannot be renamed in the next step
		conversionSwitchConfigurationControlNamespace(toVersion, toPrefix);
		
		// step through the detailed version changes
		conversionStepThroughChanges(origVersion, toVersion);

		// switch out the other, more volatile namespaces (namespaces can be modified in the previous step,
		// e.g. xmlns:PUSServicegeneric being added or removed, so this one has to come afterwards!)
		conversionSwitchOtherNamespaces(toVersion, toPrefix);
	}
	
	private void conversionSwitchConfigurationControlNamespace(String toVersion, String toPrefix) {

		Node root = getRoot();

		if (root == null) {
			return;
		}

		NamedNodeMap scriptAttributes = root.getAttributes();

		Node configurationcontrol = scriptAttributes.getNamedItem("xmlns:configurationcontrol");
		if (configurationcontrol != null) {
			configurationcontrol.setNodeValue(toPrefix + "/" + CdmCtrl.CDM_NAMESPACE_MIDDLE + toVersion);
		}
	}

	private void conversionSwitchOtherNamespaces(String toVersion, String toPrefix) {

		Node root = getRoot();

		if (root == null) {
			return;
		}

		NamedNodeMap scriptAttributes = root.getAttributes();

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

		Node container = scriptAttributes.getNamedItem("xmlns:container");
		if (container != null) {
			container.setNodeValue(toPrefix + "/MonitoringControlImplementation/Packetization/Packetization/Container/" + toVersion);
		}

		Node mapping_mcm2packet = scriptAttributes.getNamedItem("xmlns:mapping_mcm2packet");
		if (mapping_mcm2packet != null) {
			mapping_mcm2packet.setNodeValue(toPrefix + "/MonitoringControlImplementation/Packetization/Mapping_MCM2Packet/" + toVersion);
		}

		Node mappingservicepacket = scriptAttributes.getNamedItem("xmlns:mapping_service2packet");
		if (mappingservicepacket != null) {
			mappingservicepacket.setNodeValue(toPrefix + "/MonitoringControlImplementation/Packetization/Mapping_Service2Packet/" + toVersion);
		}

		Node parameter = scriptAttributes.getNamedItem("xmlns:parameter");
		if (parameter != null) {
			parameter.setNodeValue(toPrefix + "/MonitoringControlImplementation/Packetization/Packetization/Parameter/" + toVersion);
		}

		Node pusservicegeneric = scriptAttributes.getNamedItem("xmlns:PUSServicegeneric");
		if (pusservicegeneric != null) {
			pusservicegeneric.setNodeValue(toPrefix + "/MonitoringControlImplementation/PusServiceLayer/PUS_Service_generic/" + toVersion);
		}

		Node packetprocessing = scriptAttributes.getNamedItem("xmlns:packetprocessing");
		if (packetprocessing != null) {
			packetprocessing.setNodeValue(toPrefix + "/PacketProcessing/" + toVersion);
		}

		// TODO :: also convert the UDD xmlns, at least these exist (but we do not know the xmlns name right now)
		// prefix /MonitoringControlImplementation/UserDefinedDisplays/Mapping_UDD2MCM/ version
		// prefix /MonitoringControlImplementation/UserDefinedDisplays/ version

		// TODO :: if the version here ever differs from 1.5, we will need to start keeping track of that version relative to the CDM version too
		// (and start keeping that in mind for validation, etc.)

		// qudv examples that seem to be correct:
		// for 1.14.0: "http://www.esa.int/dme/core/qudv/conceptualmodel/1.5" (note: NOT aligned with generic version prefix of 1.14.0!)
		// for 1.14.0b: "http://www.esa.int/dme/core/qudv/conceptualmodel/1.5"
		// for 1.13.0bd1: "http://www.esa.int/core/qudv/conceptualmodel/1.5"

		String qudvPrefix = "http://www.esa.int/dme/core/qudv/";

		switch (toVersion) {

			case "1.14.0":
			case "1.14.0b":
				qudvPrefix = "http://www.esa.int/dme/core/qudv/";
				break;

			case "1.13.0bd1":
				qudvPrefix = "http://www.esa.int/core/qudv/";
				break;

			// TODO :: figure this out for the other CDM versions too!
		}

		Node qudvBlocksExtModel = scriptAttributes.getNamedItem("xmlns:qudv.blocks_extModel");
		if (qudvBlocksExtModel != null) {
			qudvBlocksExtModel.setNodeValue(qudvPrefix + "blocks/1.5");
		}

		Node qudvConceptExtModel = scriptAttributes.getNamedItem("xmlns:qudv.conceptualmodel_extModel");
		if (qudvConceptExtModel != null) {
			qudvConceptExtModel.setNodeValue(qudvPrefix + "conceptualmodel/1.5");
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

	private void conversionRenameRoot(String prevNamespace, String newNamespace, String newCiName) {
							
		Node root = getRoot();
		
		if (root == null) {
			return;
		}
		
		// store the original namespace
		NamedNodeMap scriptAttributes = root.getAttributes();
		Node configurationcontrol = scriptAttributes.getNamedItem("xmlns:" + prevNamespace);
		if (configurationcontrol != null) {
			// get the actual namespace string (including http:// and all) that we assigned
			prevNamespace = configurationcontrol.getNodeValue();
		}
		
		// actually register the namespace formally
		// getDocument().createElementNS(prevNamespace, newNamespace + ":" + newCiName);

		// assign the CI name, using the namespace we just registered
		getDocument().renameNode(root, prevNamespace, newNamespace + ":" + newCiName);
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

			case "1.12":
				switch (dest) {
					// up
					case "1.12.1":
						break;
				}
				break;
				
			case "1.12.1":
				switch (dest) {
					// down
					case "1.12":
						break;
					
					// up
					case "1.13.0bd1":
						// deleting the isModified attribute (see example 1) is NOT necessary, as it is still valid in 1.13.0bd1

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

							// innerParameters became innerElements with xsi:type container:InnerParameter (see example 4)
							domSetAttributeForElems("innerParameters", "xsi:type", "container:InnerParameter");
							domRenameElems("innerParameters", "innerElements");
						}
						
						break;
				}
				break;
				
			case "1.13.0bd1":
				switch (dest) {
					// down
					case "1.12.1":
						// let's add the isModified attribute with default false (see example 1) in case it is missing!
						{
							Element root = getRoot();
							Node isModified = root.getAttributes().getNamedItem("isModified");
							if (isModified == null) {
								root.setAttribute("isModified", "false");
							}
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

							// add the xsi:type to containers (see example 4)
							domSetAttributeForElems("container", "xsi:type", "container:ParameterContainer");

							// innerParameters became innerElements with xsi:type container:InnerParameter (see example 4)
							// - and now we transform them back! ;)
							domRenameElems("innerElements", "xsi:type", "container:InnerParameter",
								"innerParameters");
							domRemoveAttributeFromElems("innerParameters", "xsi:type");
						}
						
						break;
					
					// up
					case "1.14.0b":
						// adjust names of arguments as seen in example 2
						if ("configurationcontrol:McmCI".equals(getCiType())) {

							int newargcounter = 1;

							List<Element> elems = domGetElems("arguments");
							for (Element argument : elems) {
								Node argname = argument.getAttributes().getNamedItem("name");
								if (argname == null) {
									// TODO :: get the name of the definition instead, if there is one available
									argument.setAttribute("name", "Argument " + newargcounter);
									newargcounter++;
								}
							}
						}

						// adjust enumerations (see example 5)
						if ("configurationcontrol:DataTypesCI".equals(getCiType())) {

							domSetAttributeForElemsIfAttrIsMissing("abstractDataType",
								"xsi:type", "monitoringcontrolcommon:EnumerationDataType",
								"defaultText", "default");

							// add a default enumerationLiteral, if there is none there
							List<Element> enumerations = domGetElems("abstractDataType", "xsi:type", "monitoringcontrolcommon:EnumerationDataType");
							for (Element enumeration : enumerations) {
								boolean hasLiteralChild = false;

								NodeList children = enumeration.getChildNodes();
								if (children != null) {
									int childrenLen = children.getLength();

									for (int j = 0; j < childrenLen; j++) {
										Node childNode = children.item(j);
										if ("enumerationLiterals".equals(childNode.getNodeName())) {
											hasLiteralChild = true;
											break;
										}
									}
								}

								// no child has been found... create one! :)
								if (!hasLiteralChild) {
									Element newLiteral = createElement("enumerationLiterals");
									// just add a mapping f(1) = "one" - what possible harm could be done? ^^
									newLiteral.setAttribute("x", "1");
									newLiteral.setAttribute("y", "one");
									newLiteral.setAttribute("xmi:id", UuidEncoderDecoder.generateEcoreUUID());
									enumeration.appendChild(newLiteral);
								}
							}
						}
						
						// adjust packets - this here applies at least to the xsi:type="parameter:SimplePktParameter", but possibly all of them...
						if ("configurationcontrol:PacketCI".equals(getCiType())) {
							// add the packetType to the packets based on the sourceType of a contained pktParameter
							// (use TM by default, if none are contained)
							domRemoveAttributeFromElems("packet", "packetType");

							domRemoveAttributeFromElems("pktParameter", "sourceType");
						}

						if ("configurationcontrol:McmCI".equals(getCiType())) {
							// remove sourceDataType and sourceDisplayFormat, as the field no longer seems to exist
							// (could also be a 1.14.0b > 1.14.0 change instead!)
							// TODO :: implement the opposite for the other direction somehow
							domRemoveChildrenFromElems("monitoringControlElementAspects", "sourceDataType");
							domRemoveChildrenFromElems("monitoringControlElementAspects", "sourceDisplayFormat");

							// remove available arguments, as the field no longer seems to exist
							// (could also be a 1.14.0b > 1.14.0 change instead!)
							// TODO :: implement the opposite for the other direction somehow
							domRemoveAttributeFromElems("monitoringControlElementAspects", "xsi:type", "monitoringcontrolmodel:DeducedArgumentDefinition", "availableArguments");
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
							conversionRenameRoot("configurationcontrol", "configurationcontrol", "UnitsAndQuantitiesCI");
						}

						break;
				}
				break;
				
			case "1.14.0b":
				switch (dest) {
					// down
					case "1.13.0bd1":
						// adjust names of arguments back as seen in example 2 - by removing them cold-bloodedly ;)
						if ("configurationcontrol:McmCI".equals(getCiType())) {
							domRemoveAttributeFromElems("arguments", "name");
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
						}
						
						// adjust packets back - this here applies at least to the xsi:type="parameter:SimplePktParameter", but possibly all of them...
						if ("configurationcontrol:PacketCI".equals(getCiType())) {
							// TODO :: this could also be set to "Command" - to figure out which one it is,
							// check which container this is used in, and which packet that one is used in,
							// until you get up to the <packet> element with packetType="TM" or packetType="TC"
							domSetAttributeForElemsIfAttrIsMissing("pktParameter", "sourceType", "Telemetry");

							// remove the packetType from the packets after we used it to figure out the correct sourceType for the pktParameter
							domRemoveAttributeFromElems("packet", "packetType");
						}

						if ("configurationcontrol:PUSServicesCI".equals(getCiType())) {
							// remove type from verification stage
							// (could also be a 1.14.0b > 1.14.0 change instead!)
							domRemoveAttributeFromElems("defaultVerificationStages", "xsi:type");
							domRemoveAttributeFromElems("configurationcontrol:PUSServicesCI", "xmlns:PUSServicegeneric");
							domRemoveAttributeFromElems("configurationcontrol:PUSServicesCI", "xmlns:xsi");
						}

						// rename units and quantities CI back
						if ("configurationcontrol:UnitsAndQuantitiesCI".equals(getCiType())) {
							conversionRenameRoot("configurationcontrol", "configurationcontrol", "UnitsAndQuantatiesCI");
						}
						
						break;
					
					// up
					case "1.14.0":
						// adjust parameter raw to eng as seen in example 2
						if ("configurationcontrol:McmCI".equals(getCiType())) {

							// do this both for engineeringDefaultValue > value
							// and defaultValue xsi:type=monitoringcontrolmodel:EngineeringArgumentValue > value
							List<Element> children = domGetChildrenOfElems("engineeringDefaultValue");
							children.addAll(domGetChildrenOfElems("defaultValue",
								"xsi:type", "monitoringcontrolmodel:EngineeringArgumentValue"));
							// also add defaultValues that are children of EngineeringParameters, see example 6
							children.addAll(domGetChildrenOfElems("monitoringControlElementAspects",
								"xsi:type", "monitoringcontrolmodel:EngineeringParameter"));
							for (Element argValue : children) {
								// for example 2, take value, and for example 6, take defaultValue
								if (("value".equals(argValue.getNodeName())) || ("defaultValue".equals(argValue.getNodeName()))) {
									Node argValueType = argValue.getAttributes().getNamedItem("xsi:type");
									if (argValueType == null) {
										if (argValue instanceof Element) {
											Element argValueEl = (Element) argValue;
											argValueEl.setAttribute("xsi:type", "monitoringcontrolmodel:ParameterEngValue");
										}
									} else {
										if ("monitoringcontrolmodel:ParameterRawValue".equals(argValueType.getNodeValue())) {
											argValueType.setNodeValue("monitoringcontrolmodel:ParameterEngValue");
										}
									}
								}
							}
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

							// do this both for engineeringDefaultValue > value
							// and defaultValue xsi:type=monitoringcontrolmodel:EngineeringArgumentValue > value
							List<Element> children = domGetChildrenOfElems("engineeringDefaultValue");
							children.addAll(domGetChildrenOfElems("defaultValue",
								"xsi:type", "monitoringcontrolmodel:EngineeringArgumentValue"));
							// also add defaultValues that are children of EngineeringParameters, see example 6
							children.addAll(domGetChildrenOfElems("monitoringControlElementAspects",
								"xsi:type", "monitoringcontrolmodel:EngineeringParameter"));
							for (Element argValue : children) {
								// for example 2, take value, and for example 6, take defaultValue
								if (("value".equals(argValue.getNodeName())) || ("defaultValue".equals(argValue.getNodeName()))) {
									Node argValueType = argValue.getAttributes().getNamedItem("xsi:type");
									if (argValueType == null) {
										if (argValue instanceof Element) {
											Element argValueEl = (Element) argValue;
											argValueEl.setAttribute("xsi:type", "monitoringcontrolmodel:ParameterRawValue");
										}
									} else {
										if ("monitoringcontrolmodel:ParameterEngValue".equals(argValueType.getNodeValue())) {
											argValueType.setNodeValue("monitoringcontrolmodel:ParameterRawValue");
										}
									}
								}
							}
						}
						
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
