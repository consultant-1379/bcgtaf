package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

import org.apache.log4j.Logger;

import com.ericsson.bcgcdb.test.cases.BCGExport_STKPI_TestData;

import java.text.DecimalFormat;

public class BCGExport_STKPI_FunctionalTest extends TorTestCaseHelper implements TestCase {


	Logger LOGGER = Logger.getLogger(BCGExport_STKPI_FunctionalTest.class);

	@Inject
	private OperatorRegistry<BCGExport_STKPI_Operator> bCGExportProvider;

	@Inject
	private BCGExportGetter bCGExportGetter;

	@Inject
	private BCGExport_STKPI_CliOperator operator;


	@BeforeSuite
	public void cacheOffAndMcRestart(){

		if(operator.settingCache("CACHEOFF"))
			setAdditionalResultInfo("Cache-OFF Success");
		else
			setAdditionalResultInfo("Cache-OFF Fail");

	}
	/**
	 * @DESCRIPTION BCG Export for nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	//	@TestId(id = "OSS-32698_Func_3", title = "BCG Export")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB","CDB"})
	@DataDriven(name = "bcgSTKPI_Export")
	public void bCGExport(
			@Input("TestID") String testId,
			@Input("Desc") String desc,
			@Input("ExportNodeFdn") String exportNodeFdn,
			@Input("NodeType") String nodeType,
			@Input("Value") String value, 
			@Input("Filename") String fileName, 
			@Input("Domain") String domain, 
			@Input("Compression") String compression,
			@Input("Mimrate") double mimRate,
			@Input("VarianceAllowed") double varianceAllowed,
			@Input("Iteration") int iteration,
			@Output("Expected") boolean expected) {

		BCGExport_STKPI_TestData bcgExportTestData = new BCGExport_STKPI_TestData();
		bcgExportTestData.setExportNodeFdn(exportNodeFdn);
		bcgExportTestData.setNodeType(nodeType);
		bcgExportTestData.setValue(value);
		bcgExportTestData.setFilename(fileName);
		bcgExportTestData.setDomain(domain);
		bcgExportTestData.setCompression(compression);
		bcgExportTestData.setMimrate(mimRate);
		bcgExportTestData.setItenation(iteration);
		bcgExportTestData.setExpected(expected);

			BCGExport_STKPI_Operator bCGExportOperator = bCGExportProvider.provide(BCGExport_STKPI_Operator.class);

		boolean preActionResult;
		boolean exportResult;
		double[] exportData;
		double actualRate;
		double variance;
		double exportedNodes;
		double exportedMos;
		double[] moRateForEachExport;
		String pattern = "#.00";
		String nodepattern = "#";
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		DecimalFormat nodedecimalFormat = new DecimalFormat(nodepattern);
		setTestcase(testId,desc);
		setTestInfo("Set the pre-condition for the Test Case");
		setTestStep("Nodes are not empty");
		preActionResult = bCGExportOperator.preActionforexport(bcgExportTestData);
		assertTrue(preActionResult);
		
		setTestStep("Export has succeeded");
		setTestInfo("Run Export for Nodes");
		exportResult = bCGExportOperator.BcgExport(bcgExportTestData);
		assertEquals(exportResult,expected);

/*		setTestInfo("Validation for Export file");
		validationoutput = bCGExportOperator.validationforExport(bcgExportTestData);
		assertTrue(validationoutput);
		setTestStep("Validation for Export was successful");*/	
		
		setTestStep("MO rate for Export");	
		setTestInfo("Calculating MO rate for Export ");
		exportData = bCGExportOperator.verifyExportRateAboveMinimum(fileName);
		actualRate = exportData[0];
		exportedNodes = exportData[1];
		exportedMos = exportData[2];
		variance = ((mimRate - actualRate)*100)/mimRate;
		
		moRateForEachExport = bCGExportOperator.getMoRate(fileName);
		
		for (int i = 0 ;i < moRateForEachExport.length; i++){
			setAdditionalResultInfo("MO/Sec for Export "+(i+1)+ " = "+decimalFormat.format(moRateForEachExport[i]));
		}
		String convertedActualRate = decimalFormat.format(actualRate);
		String convertedExpectedRate = decimalFormat.format(mimRate);
		String convertedVariance = decimalFormat.format(variance);
		
		setAdditionalResultInfo("Actual MO rate = "+convertedActualRate +" MO/Sec");
		setAdditionalResultInfo("Expected MO rate = "+convertedExpectedRate + " MO/Sec");
		setAdditionalResultInfo("Actual variance = "+convertedVariance+"%");
		setAdditionalResultInfo("Variance Allowed <= "+varianceAllowed +"%");
		setAdditionalResultInfo("Nodes Exported = "+nodedecimalFormat.format(exportedNodes));
		setAdditionalResultInfo("MOs Exported = "+nodedecimalFormat.format(exportedMos));
		 
		assertTrue((variance <= (varianceAllowed)));
	}

	@AfterSuite
	public void afterSuiteRan(){
	if(operator.settingCache("CACHEON"))
			setAdditionalResultInfo("Cache-ON Success");
		else
			setAdditionalResultInfo("Cache-ON Fail");
	}
}
