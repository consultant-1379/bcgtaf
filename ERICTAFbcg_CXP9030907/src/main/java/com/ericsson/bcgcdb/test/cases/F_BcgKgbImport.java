package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.testng.annotations.Test;

import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

public class F_BcgKgbImport extends TorTestCaseHelper implements TestCase {

	@Inject
	private OperatorRegistry<F_BCGImportOperator> bCGImportProvider;


	@Inject
	private BCGImportGetter bCGImportGetter;


	/**
	 * @DESCRIPTION BCG import for nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	@TestId(id = "OSS_76086_BCG_KGB", title = "BCG KGB Mo's Import")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB,CDB"})
	@DataDriven(name = "import_update")
	public void bCGImport(
			@Input("ImportMo") String ImportMo, 
			@Input("ImportNodeFDN") String ImportNodeFDN, 
			@Input("ImportNodeType") String ImportNodeType, 
			@Input("ImportFileName") String ImportFileName, 
			@Input("NumberOfMos") int NumberOfMos,
			@Input("PlanName") String PlanName,
			@Input("PED_autoLockUnlock") String PED_autoLockUnlock,
			@Input("adminState") String adminState,
			@Input("modifier") String modifier,
			@Input("VerifyMo") String VerifyMo,
			@Input("updateFields") String updateFields,//my changes
			@Input("Scheme") String Scheme,		
			@Input("ReConfiguration") String reconfiguration,
			@Input("CountDown") int countdown,
			@Output("Expected") String expected) {

		F_BCGTestData f_bcgTestData = new F_BCGTestData();
		f_bcgTestData.setImportmo(ImportMo);
		f_bcgTestData.setImportnodefdn(ImportNodeFDN);
		f_bcgTestData.setImportnodetype(ImportNodeType);
		f_bcgTestData.setImportfilename(ImportFileName);
		f_bcgTestData.setNumberofmos(NumberOfMos);
		f_bcgTestData.setPlanname(PlanName);
		f_bcgTestData.setPed_autolockunlock(PED_autoLockUnlock);
		f_bcgTestData.setAdminState(adminState);
		f_bcgTestData.setModifier(modifier);
		f_bcgTestData.setVerifymo(VerifyMo);
		f_bcgTestData.setReconfiguration(reconfiguration);
		f_bcgTestData.setCountdown(countdown);
		f_bcgTestData.setScheme(Scheme);
		f_bcgTestData.setExpected(expected);

		F_BCGImportOperator f_bCGImportOperator = bCGImportProvider.provide(F_BCGImportOperator.class);

		boolean preActionResult;
		boolean prepareImportResult;
		boolean validationoutput;

		setTestInfo("Set the pre-condition for the Test Case");
		preActionResult = f_bCGImportOperator.preAction(f_bcgTestData);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");;
		//TODO VERIFY:Nodes are not empty

		prepareImportResult = f_bCGImportOperator.prepareImportFile(f_bcgTestData);
		assertTrue(prepareImportResult);
		setTestStep("Import file created successfully");
		//TODO VERIFY:Import file created successfully

		setTestInfo("Run Import for nodes");
		String output = f_bCGImportOperator.bcgImport(f_bcgTestData);
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		setTestInfo("Run Import for nodes");
		validationoutput = f_bCGImportOperator.validation(f_bcgTestData);
		assertTrue(validationoutput);
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		assertEquals(output, expected);

		f_bCGImportOperator.postAction(f_bcgTestData);
	} 
}


