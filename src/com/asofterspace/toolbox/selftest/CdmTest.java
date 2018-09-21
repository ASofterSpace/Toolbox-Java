package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.cdm.CdmCtrl;
import com.asofterspace.toolbox.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.toolbox.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;

import java.util.ArrayList;
import java.util.List;


public class CdmTest implements Test {

	@Override
	public void runAll() {

		createAndValidateCdmTest();
	}
	
	public void createAndValidateCdmTest() {
	
		TestUtils.start("Create and Validate CDM Test");
		
		// ensure the directory is clear
		Directory testDir = new Directory("test");
		testDir.clear();

		try {
			Boolean creationResult = CdmCtrl.createNewCdm("test/testcdm", "1.14.0", "http://www.esa.int/egscc/", "root_route_sap_ex_type");

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

}
