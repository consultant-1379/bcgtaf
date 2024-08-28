package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

public class S_BCGImport_FunctionalTest extends TorTestCaseHelper implements TestCase {
	
	protected static final Logger LOGGER = Logger.getLogger(S_BCGImport_FunctionalTest.class);

	@Inject
	private OperatorRegistry<S_BCGImportOperator> bCGImportProvider;


	@Inject
	private BCGImportGetter bCGImportGetter;


	/**
	 * @DESCRIPTION BCG import for nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	@TestId(id = "OSS_76084_BCG_KGB", title = "BCG KGB Common Fragment Import")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB,CDB"})
	@DataDriven(name = "s_bcgimport_functionaltest")
	public void bCGImport(
			@Input("ImportMo") String ImportMo, 
			@Input("ImportNodeFDN") String ImportNodeFDN, 
			@Input("ImportNodeType") String ImportNodeType, 
			@Input("ImportFileName") String ImportFileName, 
			@Input("NumberOfMos") int NumberOfMos,
			@Input("PlanName") String PlanName,
			@Input("PED_autoLockUnlock") String PED_autoLockUnlock,
			@Input("modifier") String modifier,
			@Input("VerifyMo") String VerifyMo,
			@Input("updateFields") String updateFields,//my changes
			@Input("mimName") String mimName,//my changes
			@Input("Scheme") String Scheme,		
			@Input("ReConfiguration") String reconfiguration,
			@Input("CountDown") int countdown,
			@Output("Expected") String expected) {

		S_BCGTestData s_bcgTestData = new S_BCGTestData();
		s_bcgTestData.setImportmo(ImportMo);
		s_bcgTestData.setImportnodefdn(ImportNodeFDN);
		s_bcgTestData.setImportnodetype(ImportNodeType);
		s_bcgTestData.setImportfilename(ImportFileName);
		s_bcgTestData.setNumberofmos(NumberOfMos);
		s_bcgTestData.setPlanname(PlanName);
		s_bcgTestData.setPed_autolockunlock(PED_autoLockUnlock);
		s_bcgTestData.setModifier(modifier);
		s_bcgTestData.setVerifymo(VerifyMo);
		s_bcgTestData.setUpdateFields(updateFields);//my changes
		s_bcgTestData.setMimName(mimName);//my changes
		s_bcgTestData.setReconfiguration(reconfiguration);
		s_bcgTestData.setCountdown(countdown);
		s_bcgTestData.setScheme(Scheme);
		s_bcgTestData.setExpected(expected);

		S_BCGImportOperator s_bCGImportOperator = bCGImportProvider.provide(S_BCGImportOperator.class);

		boolean preActionResult;
		boolean prepareImportResult;
		boolean validationoutput;
		LOGGER.info("Test Case Started");
		setTestInfo("Set the pre-condition for the Test Case");
		preActionResult = s_bCGImportOperator.preAction(s_bcgTestData);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");;
		//TODO VERIFY:Nodes are not empty

		prepareImportResult = s_bCGImportOperator.prepareImportFile(s_bcgTestData);
		assertTrue(prepareImportResult);
		setTestStep("Import file created successfully");
		//TODO VERIFY:Import file created successfully

		setTestInfo("Run Import for nodes");
		String output = s_bCGImportOperator.bcgImport(s_bcgTestData);
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		setTestInfo("Run Import for nodes");
		validationoutput = s_bCGImportOperator.validation(s_bcgTestData);
		assertTrue(validationoutput);
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		assertEquals(output, expected);

		s_bCGImportOperator.postAction(s_bcgTestData);
		LOGGER.info("Test Case COmpleted");
	} 
}

