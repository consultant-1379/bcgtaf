package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

public class BcgKgbImport extends TorTestCaseHelper implements TestCase {
	
	Logger LOGGER = Logger.getLogger(BcgKgbImport.class);

	@Inject
	private OperatorRegistry<BCGKgbImportOperator> bCGImportProvider;


	@Inject
	private BCGImportGetter bCGImportGetter;


	/**
	 * @DESCRIPTION BCG import for nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	@TestId(id = "OSS_76076_BCG_KGB", title = "BCG KGB Import")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB,CDB"})
	@DataDriven(name = "bcg_KGB_import")
	public void bCGImport(
			@Input("TestID") String TestID,
			@Input("ImportMo") String ImportMo, 
			@Input("ImportNodeFDN") String ImportNodeFDN, 
			@Input("ImportNodeType") String ImportNodeType, 
			@Input("ImportFileName") String ImportFileName, 
			@Input("NumberOfMos") int NumberOfMos,
			@Input("PlanName") String PlanName,
			@Input("PED_autoLockUnlock") String PED_autoLockUnlock,
			@Input("modifier") String modifier,
			@Input("VerifyMo") String VerifyMo,
			@Input("Scheme") String Scheme,		
			@Input("ReConfiguration") String reconfiguration,
			@Input("CountDown") int countdown,
			@Output("Expected") String expected) {

		BCGKGBImportTestData bcgKgbImportTestData = new BCGKGBImportTestData();
		bcgKgbImportTestData.setImportmo(ImportMo);
		bcgKgbImportTestData.setImportnodefdn(ImportNodeFDN);
		bcgKgbImportTestData.setImportnodetype(ImportNodeType);
		bcgKgbImportTestData.setImportfilename(ImportFileName);
		bcgKgbImportTestData.setNumberofmos(NumberOfMos);
		bcgKgbImportTestData.setPlanname(PlanName);
		bcgKgbImportTestData.setPed_autolockunlock(PED_autoLockUnlock);
		bcgKgbImportTestData.setModifier(modifier);
		bcgKgbImportTestData.setVerifymo(VerifyMo);
		bcgKgbImportTestData.setReconfiguration(reconfiguration);
		bcgKgbImportTestData.setCountdown(countdown);
		bcgKgbImportTestData.setScheme(Scheme);
		bcgKgbImportTestData.setExpected(expected);

		BCGKgbImportOperator bCGKgbImportOperator = bCGImportProvider.provide(BCGKgbImportOperator.class);

		boolean preActionResult;
		boolean prepareImportResult;
		boolean validationoutput;

		LOGGER.info("Test case started");
		setTestcase(TestID,"");
		setTestInfo("Set the pre-condition for the Test Case");
		preActionResult = bCGKgbImportOperator.preAction(bcgKgbImportTestData);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");
		//TODO VERIFY:Nodes are not empty

		prepareImportResult = bCGKgbImportOperator.prepareImportFile(bcgKgbImportTestData);
		assertTrue(prepareImportResult);
		setTestStep("Import file created successfully");
		//TODO VERIFY:Import file created successfully

		setTestInfo("Run Import for nodes");
		String output = bCGKgbImportOperator.bcgImport(bcgKgbImportTestData);
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		setTestInfo("Run Import for nodes");
		validationoutput = bCGKgbImportOperator.validation(bcgKgbImportTestData);
		assertTrue(validationoutput);
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		assertEquals(output, expected);

		bCGKgbImportOperator.postAction(bcgKgbImportTestData);
		
		LOGGER.info("Test case completed");
	} 
}


