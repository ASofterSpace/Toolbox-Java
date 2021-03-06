/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.cdm.CdmCtrl;
import com.asofterspace.toolbox.cdm.CdmFile;
import com.asofterspace.toolbox.cdm.CdmNode;
import com.asofterspace.toolbox.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.toolbox.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.cdm.exceptions.CdmSavingException;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.NoOpProgressIndicator;
import com.asofterspace.toolbox.utils.ProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;


public class CdmTest implements Test {

	@Override
	public void runAll() {

		AllTests.clearTestDirectory();

		createAndValidateCdmTest();

		findEntityInCdmTest();

		convertCdmPacketCIsTest();

		// commented out for now, as we have not yet implemented this functionality
		// parseBinaryCDM("Mcm 1"); // contains McmCI, one MCE, one MCE Def

		parseBinaryCDM("Mcm 2"); // contains just an McmCI (invalid CDM, but valid EMF!)

		parseBinaryCDM("Script 1"); // contains ScriptCI, one script

		parseBinaryCDM("Script 2"); // contains ScriptCI, one script with loooong length!

		parseBinaryCDM("Packet 1"); // contains PacketCI (valid CDM!)
	}

	public void createAndValidateCdmTest() {

		TestUtils.start("Create and Validate CDM");

		CdmCtrl cdmCtrl = new CdmCtrl();

		try {
			Boolean creationResult = cdmCtrl.createNewCdm(AllTests.TEST_PATH, "1.14.0", "http://www.esa.int/egscc/", "root_route_sap_ex_type");

			if (!(creationResult == true)) {
				TestUtils.fail("We tried to create a new CDM, the creation result was not true!");
				return;
			}

			List<String> problems = new ArrayList<>();

			int valid = cdmCtrl.checkValidity(problems);

			if (valid != 0) {
				TestUtils.fail("We tried to create a new CDM, but when validating the created CDM it seemed invalid!");
				return;
			}
		} catch (AttemptingEmfException | CdmLoadingException | CdmSavingException e) {
			TestUtils.fail("We tried to create a new CDM, but got this exception: " + e.getMessage());
			return;
		}

		TestUtils.succeed();
	}

	public void findEntityInCdmTest() {

		TestUtils.start("Find Entity in CDM");

		CdmCtrl cdmCtrl = new CdmCtrl();

		// we know that the previous test just ran, so we can attempt to read stuff out from that test... :)

		Directory cdmDir = new Directory(AllTests.TEST_PATH);
		ProgressIndicator noProgress = new NoOpProgressIndicator();

		try {
			cdmCtrl.loadCdmDirectory(cdmDir, noProgress);
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to load a CDM, but got this exception: " + e.getMessage());
			return;
		}

		List<CdmNode> foundNodes = cdmCtrl.findByName("mcmRoot");
		CdmNode foundNode;

		if (foundNodes.size() == 1) {
			foundNode = foundNodes.get(0);
			if (!"mcmRoot".equals(foundNode.getName())) {
				TestUtils.fail("We wanted to find a node called mcmRoot, but we found one called " + foundNode.getName() + " instead!");
				return;
			}
			if (!"mcmRoot".equals(foundNode.getPath())) {
				TestUtils.fail("We wanted to find a node called mcmRoot which was supposed to be the root element, but the one we found had the path " + foundNode.getPath() + " instead!");
				return;
			}
			if (!"Resource_Mcm.cdm".equals(foundNode.getParentFile().getPathRelativeToCdmRoot())) {
				TestUtils.fail("The node that we found should be contained in the file Resource_Mcm.cdm, but it is contained in " + foundNode.getParentFile().getPathRelativeToCdmRoot() + " instead!");
				return;
			}
		} else {
			TestUtils.fail("We wanted to find one node called mcmRoot, but we found " + foundNodes.size() + "!");
			return;
		}

		foundNodes = cdmCtrl.findByUuid(foundNode.getId());

		if (foundNodes.size() != 1) {
			TestUtils.fail("We wanted to find one node with a specific UUID, but we found " + foundNodes.size() + "!");
			return;
		}

		foundNode = cdmCtrl.getByUuid(foundNode.getId());

		if (foundNode == null) {
			TestUtils.fail("We wanted to find one node with a specific UUID, but we could not actually find it!");
			return;
		}

		foundNodes = cdmCtrl.findByXmlTag("monitoringControlElementDefinition");

		if (foundNodes.size() == 1) {
			foundNode = foundNodes.get(0);
			if (!"mcmRoot_Definition".equals(foundNode.getName())) {
				TestUtils.fail("We wanted to find a node called mcmRoot_Definition, but we found one called " + foundNode.getName() + " instead!");
				return;
			}
			if (!"Resource_Mcm.cdm".equals(foundNode.getParentFile().getPathRelativeToCdmRoot())) {
				TestUtils.fail("The node that we found should be contained in the file Resource_Mcm.cdm, but it is contained in " + foundNode.getParentFile().getPathRelativeToCdmRoot() + " instead!");
				return;
			}
		} else {
			TestUtils.fail("We wanted to find one node with the XML tag monitoringControlElementDefinition, but we found " + foundNodes.size() + "!");
			return;
		}

		List<CdmNode> foundByPath = cdmCtrl.findByPath("mcmRoot.DefaultRoute");

		if (foundByPath.size() < 1) {
			TestUtils.fail("We wanted to find one node by its path mcmRoot.DefaultRoute, but we did not find any!");
			return;
		}
		if (foundByPath.size() > 1) {
			TestUtils.fail("We wanted to find one node by its path mcmRoot.DefaultRoute, but we found several!");
			return;
		}

		String foundPath = foundByPath.get(0).getPath();
		if (!"mcmRoot.DefaultRoute".equals(foundPath)) {
			TestUtils.fail("We found one node by its path mcmRoot.DefaultRoute, but when querying the node for its path we actually got back " + foundPath + "!");
			return;
		}

		TestUtils.succeed();
	}

	public void convertCdmPacketCIsTest() {

		TestUtils.start("Convert CDM Packet CIs");

		CdmCtrl cdmCtrl = new CdmCtrl();

		// convert from 1.13.0bd1 to 1.14.0b
		Directory cdmDir = new Directory(AllTests.CDM_TEST_DATA_PATH + "/convertCdmPacketCIsTest1");
		ProgressIndicator noProgress = new NoOpProgressIndicator();

		try {
			cdmCtrl.loadCdmDirectory(cdmDir, noProgress);
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to load a CDM, but got this exception: " + e.getMessage());
			return;
		}

		cdmCtrl.convertTo("1.14.0b", null);

		Set<CdmFile> files = cdmCtrl.getCdmFiles();

		CdmFile file = files.iterator().next();

		List<XmlElement> packets = file.domGetElems("packet");
		for (XmlElement packet : packets) {
			CdmNode cdmPacket = new CdmNode(file, packet, cdmCtrl);
			switch (cdmPacket.getName()) {
				case "PacketWithTMOnly":
				case "PacketWithTMandTC":
					if (!"TM".equals(cdmPacket.getAttribute("packetType"))) {
						TestUtils.fail("While converting the PacketCI, the packet " + cdmPacket.getName() + " did not receive the expected packetType!");
						return;
					}
					break;
				case "PacketWithTCOnly":
					if (!"TC".equals(cdmPacket.getAttribute("packetType"))) {
						TestUtils.fail("While converting the PacketCI, the packet " + cdmPacket.getName() + " did not receive the expected packetType!");
						return;
					}
					break;
				default:
					TestUtils.fail("While converting the PacketCI, a wonky new packet (" + cdmPacket.getName() + ") appeared!");
					return;
			}
		}

		// convert from 1.14.0b to 1.13.0bd1
		cdmDir = new Directory(AllTests.CDM_TEST_DATA_PATH + "/convertCdmPacketCIsTest2");
		noProgress = new NoOpProgressIndicator();

		try {
			cdmCtrl.loadCdmDirectory(cdmDir, noProgress);
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to load a CDM, but got this exception: " + e.getMessage());
			return;
		}

		cdmCtrl.convertTo("1.13.0bd1", null);

		files = cdmCtrl.getCdmFiles();

		file = files.iterator().next();

		List<XmlElement> pktParameters = file.domGetElems("pktParameter");
		for (XmlElement pktParameter : pktParameters) {
			CdmNode cdmPktParameter = new CdmNode(file, pktParameter, cdmCtrl);
			if (cdmPktParameter.getName().startsWith("paraTM")) {
				if (!"Telemetry".equals(cdmPktParameter.getAttribute("sourceType"))) {
					TestUtils.fail("While converting the PacketCI, the pktParameter " + cdmPktParameter.getName() + " did not receive the expected sourceType!");
					return;
				}
			}
			if (cdmPktParameter.getName().startsWith("paraTC")) {
				if (!"Command".equals(cdmPktParameter.getAttribute("sourceType"))) {
					TestUtils.fail("While converting the PacketCI, the pktParameter " + cdmPktParameter.getName() + " did not receive the expected sourceType!");
					return;
				}
			}
		}

		TestUtils.succeed();
	}

	public void parseBinaryCDM(String which) {

		TestUtils.start("Parse Binary CDM " + which);

		which = which.toLowerCase().replaceAll(" ", "");

		CdmCtrl cdmCtrl = new CdmCtrl();

		Directory cdmDir = new Directory(AllTests.CDM_TEST_DATA_PATH + "/parseBinaryCDMsTest/" + which);
		ProgressIndicator noProgress = new NoOpProgressIndicator();

		try {
			cdmCtrl.loadCdmDirectory(cdmDir, noProgress);
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to load a binary CDM, but got this exception: " + e.getMessage());
			return;
		}

		CdmCtrl xmlCdmCtrl = new CdmCtrl();

		cdmDir = new Directory(AllTests.CDM_TEST_DATA_PATH + "/parseBinaryCDMsTest/" + which + "_xml");

		try {
			xmlCdmCtrl.loadCdmDirectory(cdmDir, noProgress);
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to load an XML CDM, but got this exception: " + e.getMessage());
			return;
		}

		List<String> differences = cdmCtrl.findDifferencesFrom(xmlCdmCtrl);

		if (differences.size() > 0) {
			TestUtils.fail("There were differences between the parsed binary and XML versions of the same CDM!");
			return;
		}

		TestUtils.succeed();
	}

}
