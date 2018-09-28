package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.cdm.CdmCtrl;
import com.asofterspace.toolbox.cdm.CdmFile;
import com.asofterspace.toolbox.cdm.CdmNode;
import com.asofterspace.toolbox.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.toolbox.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.NoOpProgressIndicator;
import com.asofterspace.toolbox.utils.ProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;


public class CdmTest implements Test {

	private static final String CDM_TEST_PATH = "test/testcdm";

	@Override
	public void runAll() {

		createAndValidateCdmTest();

		findEntityInCdmTest();
		
		convertCdmPacketCIsTest();
	}

	public void createAndValidateCdmTest() {

		TestUtils.start("Create and Validate CDM");

		// ensure the directory is clear
		Directory testDir = new Directory("test");
		testDir.clear();

		try {
			Boolean creationResult = CdmCtrl.createNewCdm(CDM_TEST_PATH, "1.14.0", "http://www.esa.int/egscc/", "root_route_sap_ex_type");

			if (!(creationResult == true)) {
				TestUtils.fail("We tried to create a new CDM, the creation result was not true!");
				return;
			}

			List<String> problems = new ArrayList<>();

			int valid = CdmCtrl.checkValidity(problems);

			if (valid != 0) {
				TestUtils.fail("We tried to create a new CDM, but when validating the created CDM it seemed invalid!");
				return;
			}
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to create a new CDM, but got this exception: " + e.getMessage());
			return;
		}

		TestUtils.succeed();
	}

	public void findEntityInCdmTest() {

		TestUtils.start("Find Entity in CDM");

		// we know that the previous test just ran, so we can attempt to read stuff out from that test... :)

		Directory cdmDir = new Directory(CDM_TEST_PATH);
		ProgressIndicator noProgress = new NoOpProgressIndicator();

		try {
			CdmCtrl.loadCdmDirectory(cdmDir, noProgress);
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to load a CDM, but got this exception: " + e.getMessage());
			return;
		}

		List<CdmNode> foundNodes = CdmCtrl.findByName("mcmRoot");
		CdmNode foundNode;

		if (foundNodes.size() == 1) {
			foundNode = foundNodes.get(0);
			if (!"mcmRoot".equals(foundNode.getName())) {
				TestUtils.fail("We wanted to find a node called mcmRoot, but we found one called " + foundNode.getName() + " instead!");
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

		foundNodes = CdmCtrl.findByUuid(foundNode.getId());

		if (foundNodes.size() != 1) {
			TestUtils.fail("We wanted to find one node with a specific UUID, but we found " + foundNodes.size() + "!");
			return;
		}

		foundNode = CdmCtrl.getByUuid(foundNode.getId());

		if (foundNode == null) {
			TestUtils.fail("We wanted to find one node with a specific UUID, but we could not actually find it!");
			return;
		}

		foundNodes = CdmCtrl.findByXmlTag("monitoringControlElementDefinition");

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

		TestUtils.succeed();
	}
	
	public void convertCdmPacketCIsTest() {
		
		TestUtils.start("Convert CDM Packet CIs");

		// convert from 1.13.0bd1 to 1.14.0b
		Directory cdmDir = new Directory("testdata/cdm/convertCdmPacketCIsTest1");
		ProgressIndicator noProgress = new NoOpProgressIndicator();

		try {
			CdmCtrl.loadCdmDirectory(cdmDir, noProgress);
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to load a CDM, but got this exception: " + e.getMessage());
			return;
		}
		
		CdmCtrl.convertTo("1.14.0b", null);
		
		Set<CdmFile> files = CdmCtrl.getCdmFiles();
		
		CdmFile file = files.iterator().next();
		
		List<Element> packets = file.domGetElems("packet");
		for (Element packet : packets) {
			CdmNode cdmPacket = new CdmNode(file, packet);
			switch (cdmPacket.getName()) {
				case "PacketWithTMOnly":
				case "PacketWithTMandTC":
					if (!"TM".equals(cdmPacket.getValue("packetType"))) {
						TestUtils.fail("While converting the PacketCI, the packet " + cdmPacket.getName() + " did not receive the expected packetType!");
						return;
					}
					break;
				case "PacketWithTCOnly":
					if (!"TC".equals(cdmPacket.getValue("packetType"))) {
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
		cdmDir = new Directory("testdata/cdm/convertCdmPacketCIsTest2");
		noProgress = new NoOpProgressIndicator();

		try {
			CdmCtrl.loadCdmDirectory(cdmDir, noProgress);
		} catch (AttemptingEmfException | CdmLoadingException e) {
			TestUtils.fail("We tried to load a CDM, but got this exception: " + e.getMessage());
			return;
		}
		
		CdmCtrl.convertTo("1.13.0bd1", null);
		
		files = CdmCtrl.getCdmFiles();
		
		file = files.iterator().next();
		
		List<Element> pktParameters = file.domGetElems("pktParameter");
		for (Element pktParameter : pktParameters) {
			CdmNode cdmPktParameter = new CdmNode(file, pktParameter);
			if (cdmPktParameter.getName().startsWith("paraTM")) {
				if (!"Telemetry".equals(cdmPktParameter.getValue("sourceType"))) {
					TestUtils.fail("While converting the PacketCI, the pktParameter " + cdmPktParameter.getName() + " did not receive the expected sourceType!");
					return;
				}
			}
			if (cdmPktParameter.getName().startsWith("paraTC")) {
				if (!"Command".equals(cdmPktParameter.getValue("sourceType"))) {
					TestUtils.fail("While converting the PacketCI, the pktParameter " + cdmPktParameter.getName() + " did not receive the expected sourceType!");
					return;
				}
			}
		}
		
		TestUtils.succeed();
	}

}
