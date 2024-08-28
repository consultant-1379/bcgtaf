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

public class BcgKgbdg2Import extends TorTestCaseHelper implements TestCase {
	
	Logger LOGGER = Logger.getLogger(BcgKgbdg2Import.class);

	@Inject
	private OperatorRegistry<BCGKGBdg2ImportCliOperator> bcgExportProvider;

	@Inject
	private BCGExportGetter bcgExportGetter;

	@Inject
	private BCGKGBdg2ImportCliOperator operator;
	
	/*@BeforeSuite
	public void preCondition(){
		try{
			boolean flagfile = false;
			flagfile = operator.cleanUpFiles();
			if(flagfile){
				operator.restartMCS();
			}
		}
		catch(Exception pre){
			LOGGER.info("Pre Action failed");
		}
		
	}*/
	
	/**
	 * @DESCRIPTION BCG Export for Nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	@TestId(id = "OSS_76054_BCG_KGB", title = "BCG KGB Export")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB","CDB"})
	@DataDriven(name = "bcg_KGB_dg2import")
	public void bCGExport(
			@Input("TestID") String TestID,
			@Input("exportNodename") String exportNodename,
			@Input("ImportMo") String ImportMo,
			@Input("modifier") String modifier,
			@Input("numberofmos") int numberofmos,
			@Input("PlanName") String PlanName,
			@Input("id") int id,
			@Input("exportNodeType") String exportNodeType,
			@Input("exportNodeFdn") String exportNodeFdn,
			@Input("numberOfFdns") int numberOfFdns,
			@Input("exportFileName") String exportFileName,
			 @Input("Importfilename") String Importfilename,
			@Input("domain") String domain,
			@Input("verifyMo") String verifyMo,
			@Input("Compression") String compression,
			@Input("PED_autoLockUnlock") String PED_autoLockUnlock,
			@Input("TimeValue") String timeValue,
			@Input("DateValue") String dateValue,
			@Input("Scheme") String Scheme,		
			@Input("ReConfiguration") String reconfiguration,
			@Input("CountDown") int countdown,
	        @Input("Expected") String expected
	       
	       ){

		
		
		BCGKGBExportData bcgExportTestData = new BCGKGBExportData();
		//bcgExportTestData.setTestID(TestID);
		bcgExportTestData.setExportNodename(exportNodename);
		bcgExportTestData.setImportMo(ImportMo);
		bcgExportTestData.setModifier(modifier);
		bcgExportTestData.setNumberOfMos(numberofmos);
		bcgExportTestData.setPlanName(PlanName);
		bcgExportTestData.setid(id);
		bcgExportTestData.setExportNodeType(exportNodeType);
		bcgExportTestData.setExportNodeFdn(exportNodeFdn);
		bcgExportTestData.setNumberOfFdns(numberOfFdns);
		bcgExportTestData.setExportFileName(exportFileName);
		bcgExportTestData.setImportfilename(Importfilename);
		bcgExportTestData.setDomain(domain);
		bcgExportTestData.setVerifyMo(verifyMo);
		bcgExportTestData.setCompression(compression);
		bcgExportTestData.setPED_autoLockUnlock(PED_autoLockUnlock);
		bcgExportTestData.setTimeValue(timeValue);
		bcgExportTestData.setDateValue(dateValue);
		bcgExportTestData.setReconfiguration(reconfiguration);
		bcgExportTestData.setCountdown(countdown);
		bcgExportTestData.setScheme(Scheme);
		bcgExportTestData.setExpected(expected);
	  

	    BCGKGBdg2ImportCliOperator bCGKGBExportOperator = (BCGKGBdg2ImportCliOperator) bcgExportProvider.provide(BCGKGBdg2ImportCliOperator.class);

		boolean preActionResult;
		boolean validationoutput;	
		boolean ExportResult;
		
	    boolean prepareImportResult;
		
		
		

		LOGGER.info("Test case started");
		setTestcase(TestID,"");
		setTestInfo("Run cstest to get the nodes");
		preActionResult = bCGKGBExportOperator.preAction(bcgExportTestData);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");
		//TODO VERIFY:Nodes are not empty

		/*setTestInfo("BCG Export for nodes");
		ExportResult = bCGKGBExportOperator.BcgExport(bcgExportTestData);
		assertTrue(ExportResult);
		setTestStep("exports are succedded");*/
		//TODO VERIFY:exports are succedded
		setTestInfo("prepareImportFil for nodes");
		ExportResult = bCGKGBExportOperator.prepareImportFile(bcgExportTestData);
		assertTrue(ExportResult);
		setTestStep("prepareImportFil are succedded");
		
		 setTestInfo("Run Import for nodes");
		String output = bCGKGBExportOperator.bcgImport(bcgExportTestData);
		setTestStep("Imports are successful"); 

		setTestInfo("Doing validation");
		validationoutput = bCGKGBExportOperator.validation(bcgExportTestData);
		assertTrue(validationoutput);
		setTestStep("Validations are successful");
		//TODO VERIFY:Imports are successful

		//assertEquals(output, expected);

		bCGKGBExportOperator.postAction(bcgExportTestData);
		//BCGKGBExportData
		LOGGER.info("Test case completed");
		
		
		
		
		
	}
}

