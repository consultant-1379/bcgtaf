package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

public class BcgKgbExport extends TorTestCaseHelper implements TestCase {
	
	Logger LOGGER = Logger.getLogger(BcgKgbExport.class);

	@Inject
	private OperatorRegistry<BCGKGBExportOperator> bcgExportProvider;

	@Inject
	private BCGExportGetter bcgExportGetter;

	@Inject
	private BCGKGBExportCliOperator operator;
	
	@BeforeSuite
	public void preCondition(){
		try{
			boolean flagfile = false;
			flagfile = operator.cleanUpFiles();
			/*if(flagfile){
				operator.restartMCS();
			}*/
			if(flagfile){
				LOGGER.info("Old Files Cleared");
			}
		}
		catch(Exception pre){
			LOGGER.info("Pre Condition failed");
		}
		
	}
	
	/**
	 * @DESCRIPTION BCG Export for Nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	@TestId(id = "OSS_76054_BCG_KGB", title = "BCG KGB Export")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB","CDB"})
	@DataDriven(name = "bcg_KGB_export")
	public void bCGExport(
			@Input("TestID") String TestID,
			@Input("exportNodename") String exportNodename,
			@Input("exportNodeType") String exportNodeType,
			@Input("exportNodeFdn") String exportNodeFdn,
			@Input("numberOfFdns") int numberOfFdns,
			@Input("exportFileName") String exportFileName,
			@Input("domain") String domain,
			@Input("verifyMo") String verifyMo,
			@Input("Compression") String compression,
			@Input("PED_autoLockUnlock") String PED_autoLockUnlock,
			@Input("TimeValue") String timeValue,
			@Input("DateValue") String dateValue,
			@Input("Expected") String expected) {

		BCGKGBExportData bcgExportTestData = new BCGKGBExportData();
		bcgExportTestData.setExportNodename(exportNodename);
		bcgExportTestData.setExportNodeType(exportNodeType);
		bcgExportTestData.setExportNodeFdn(exportNodeFdn);
		bcgExportTestData.setNumberOfFdns(numberOfFdns);
		bcgExportTestData.setExportFileName(exportFileName);
		bcgExportTestData.setDomain(domain);
		bcgExportTestData.setVerifyMo(verifyMo);
		bcgExportTestData.setCompression(compression);
		bcgExportTestData.setPED_autoLockUnlock(PED_autoLockUnlock);
		bcgExportTestData.setTimeValue(timeValue);
		bcgExportTestData.setDateValue(dateValue);
		bcgExportTestData.setExpected(expected);

		BCGKGBExportCliOperator bCGKGBExportOperator = (BCGKGBExportCliOperator) bcgExportProvider.provide(BCGKGBExportOperator.class);

		boolean preActionResult;
		boolean validationoutput;	
		boolean ExportResult;

		LOGGER.info("Test case started");
		setTestcase(TestID,"");
		setTestInfo("Run cstest to get the nodes");
		preActionResult = bCGKGBExportOperator.preAction(bcgExportTestData);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");
		//TODO VERIFY:Nodes are not empty

		setTestInfo("BCG Export for nodes");
		ExportResult = bCGKGBExportOperator.BcgExport(bcgExportTestData);
		assertTrue(ExportResult);
		setTestStep("exports are succedded");
		//TODO VERIFY:exports are succedded

		setTestInfo("Validate Export XML file");
		validationoutput = bCGKGBExportOperator.validation(bcgExportTestData);
		assertTrue(validationoutput);
		setTestStep("XML should not be empty");
		//TODO VERIFY:XML should not be empty
		LOGGER.info("Test case completed");
	}
}
