package com.ericsson.bcgcdb.test.cases ;

import java.text.DecimalFormat;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.testng.annotations.Test;

import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

public class BCGImport_STKPI_FunctionalTest extends TorTestCaseHelper implements TestCase {

	@Inject
	private OperatorRegistry<BCGImport_STKPI_Operator> bCGImportProvider;


	@Inject
	private BCGImportGetter bCGImportGetter;


	/**
	 * @DESCRIPTION BCG import for nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
//	@TestId(id = "OSS-27134_Func_1", title = "BCG ST KPI Import")
	@Context(context = {Context.CLI})
	@Test(groups={"STCDB"})
	@DataDriven(name = "bcgSTKPI_Import")
	public void bCGImport(
			@Input("TestID") String TestID,
			@Input("Desc") String Desc,
			@Input("ImportMo") String ImportMo, 
			@Input("ImportType") String ImportType, 
			@Input("ImportFileName") String ImportFileName, 
			@Input("PlanName") String PlanName,
			@Input("modifier") String modifier,
			@Input("MimRate") String MimRate,
			@Input("Mos") int Mos,
			@Input("varianceAllowed") double varianceAllowed,
			@Output("Expected") String expected) {

		BCGImport_STKPI_TestData bcgTestData = new BCGImport_STKPI_TestData();
		bcgTestData.setImportMo(ImportMo);
		bcgTestData.setImportType(ImportType);
		bcgTestData.setImportFileName(ImportFileName);
		bcgTestData.setPlanName(PlanName);
		bcgTestData.setModifier(modifier);
		bcgTestData.setMimRate(MimRate);
		bcgTestData.setExpected(expected);
		bcgTestData.setMos(Mos);

		BCGImport_STKPI_Operator bCGImportKPIOperator = bCGImportProvider.provide(BCGImport_STKPI_Operator.class);

		boolean preActionResult;
		boolean importResult;
		boolean validationoutput;
		double moRate;
		double mimRate;
		double variance;
		String pattern = "#.00";
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		
		setTestcase(TestID,Desc);
		setTestInfo("Set the pre-condition for the Test Case");
		preActionResult = bCGImportKPIOperator.preAction(bcgTestData);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");;
		//TODO VERIFY:Nodes are not empty

		setTestInfo("Run Import for nodes");
	    importResult = bCGImportKPIOperator.bcgImport(bcgTestData);
		assertTrue(importResult);
		setTestStep("Imports are successful");
		//TODO VERIFY:Imports are successful

		setTestInfo("Run Import for nodes");
		moRate = bCGImportKPIOperator.verifyExportRateAboveMinimum(ImportFileName,MimRate);
		mimRate = Double.parseDouble(MimRate);
		variance = ((mimRate - moRate)*100)/mimRate;
		
		String convertedActualRate = decimalFormat.format(moRate);
		String convertedExpectedRate = decimalFormat.format(mimRate);
		String convertedVariance = decimalFormat.format(variance);
		
		setAdditionalResultInfo("Actual MO Rate :: "+ convertedActualRate  + " MO/sec");
		setAdditionalResultInfo("Expected MO Rate :: "+ convertedExpectedRate + " MO/sec");
		setAdditionalResultInfo("Actual variance :: "+ convertedVariance+" % ");
		setAdditionalResultInfo("Expected variance Allowed :: "+varianceAllowed +" % ");
		 
		assertTrue((variance <= (varianceAllowed)));
		assertTrue("MO rate is less than the previous baseline ",moRate>= Double.parseDouble(MimRate));
		//TODO VERIFY:Imports are successful

	} 
}