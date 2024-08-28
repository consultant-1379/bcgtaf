package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;


public class BCGImport_FunctionalTest extends TorTestCaseHelper implements TestCase {

	Logger LOGGER = Logger.getLogger(BCGImport_FunctionalTest.class);
	@Inject
	private OperatorRegistry<BCGImportOperator> bCGImportProvider;


	@Inject
	private BCGImportGetter bCGImportGetter;


	/**
	 * @DESCRIPTION BCG import for nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	@TestId(id = "OSS_76079_BCG_CDB", title = "BCG CDB Import")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB,CDB"})
	@DataDriven(name = "bcgimport_functionaltest")
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
			@Input("id") String id,
			@Input("VerifyMo") String VerifyMo,
			@Output("Expected") String expected) {

		BCGTestData bcgTestData = new BCGTestData();
		bcgTestData.setImportmo(ImportMo);
		bcgTestData.setImportnodefdn(ImportNodeFDN);
		bcgTestData.setImportnodetype(ImportNodeType);
		bcgTestData.setImportfilename(ImportFileName);
		bcgTestData.setNumberofmos(NumberOfMos);
		bcgTestData.setPlanname(PlanName);
		bcgTestData.setPed_autolockunlock(PED_autoLockUnlock);
		bcgTestData.setModifier(modifier);
		bcgTestData.setVerifymo(VerifyMo);
		bcgTestData.setExpected(expected);
		bcgTestData.setId(id);
    
		BCGImportOperator bCGImportOperator = bCGImportProvider.provide(BCGImportOperator.class);

		boolean preActionResult;
		boolean prepareImportResult;
		boolean validationoutput;

		LOGGER.info("Tese case started");
		setTestcase(TestID,"");
		setTestInfo("Set the pre-condition for the Test Case");
		preActionResult = bCGImportOperator.preAction(bcgTestData);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");;
		//TODO VERIFY:Nodes are not empty

		prepareImportResult = bCGImportOperator.prepareImportFile(bcgTestData);
		assertTrue(prepareImportResult);
		setTestStep("Import file created successfully");
		//TODO VERIFY:Import file created successfully

		setTestInfo("Run Import for nodes");
		String output = bCGImportOperator.bcgImport(bcgTestData);
		assertEquals(output, "Import has succeeded");
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		validationoutput = bCGImportOperator.validation(bcgTestData);
		assertTrue(validationoutput);
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		assertEquals(output, expected);

		bCGImportOperator.postAction(bcgTestData);
		LOGGER.info("Test case completed");
	} 
}
