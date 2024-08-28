package com.ericsson.bcgcdb.test.cases ;

import java.text.DecimalFormat;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.bcgcdb.test.getters.BCGExportGetter;
import com.ericsson.bcgcdb.test.operators.BCG_ATT_TR_CliOperator;
import com.ericsson.bcgcdb.test.operators.BCG_ATT_TR_Operator;
import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;

public class BCG_ATT_TR_FunctionalTest extends TorTestCaseHelper implements TestCase {
	
	Logger LOGGER = Logger.getLogger(BCG_ATT_TR_FunctionalTest.class);

    @Inject
    private OperatorRegistry<BCG_ATT_TR_Operator> bcgProvider;
    
    
    @Inject
    private BCGExportGetter bCGExportGetter;
 
    /**
     * @DESCRIPTION BCG ATT TR Verification
     * @PRE Nodes should be available
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS_76082_BCG_KGB", title = "BCG ATT TR Automation")
    @Context(context = {Context.CLI})
    @Test(groups={"KGB","CDB"})
    @DataDriven(name = "bcg_att_tr_functionaltest")
    public void bCGExport(
		@Input("TestID") String testID,
		@Input("MOName") String moName,
		@Input("Nodename") String nodeName,
		@Input("NodeType") String nodeType,
		@Input("NodeFdn") String nodeFdn,
		@Input("FileName") String fileName,
		@Input("Domain") String domain,
		@Input("VerifyTestCase") String verifyTestCase,
		@Input("ImportTime") String importTime,
		@Input("MimRate") String mimRate,
		@Input("VarianceAllowed") String varianceAllowed,
		@Input("Compression") String compression,
		@Input("TimeValue") String timeValue,
		@Input("DateValue") String dateValue,
		@Input("NumberOfMos") String numberOfMos,
		@Input("PlanName") String planName,
		@Input("PED_autoLockUnlock") String pedAutoLockUnlock,
		@Input("AdminState") String adminState,
		@Input("Modifier") String modifier,
		@Input("UpdateFields") String updateField,
		@Input("ReConfiguration") String reconfiguration,
		@Input("CountDown") int countdown,
		@Input("Scheme") String Scheme,	
		@Input("Expected") String expected) {
    	
    	BCG_ATT_TR_TestData bcgTestData = new BCG_ATT_TR_TestData();
    	bcgTestData.setMoName(moName);
    	bcgTestData.setNodeName(nodeName);
    	bcgTestData.setNodeType(nodeType);
    	bcgTestData.setNodeFdn(nodeFdn);
    	bcgTestData.setFileName(fileName);
    	bcgTestData.setDomain(domain);
    	bcgTestData.setVerifyTestCase(verifyTestCase);
    	bcgTestData.setImportTime(importTime);
    	bcgTestData.setMimRate(mimRate);
    	bcgTestData.setCompression(compression);
    	bcgTestData.setTimeValue(timeValue);
    	bcgTestData.setDateValue(dateValue);
    	bcgTestData.setNumberOfMOs(numberOfMos);
    	bcgTestData.setPlanName(planName);
    	bcgTestData.setPedAutoLockUnlock(pedAutoLockUnlock);
    	bcgTestData.setAdminState(adminState);
    	bcgTestData.setModifier(modifier);
    	bcgTestData.setUpdateField(updateField);
    	bcgTestData.setReconfiguration(reconfiguration);
    	bcgTestData.setCountdown(countdown);
    	bcgTestData.setScheme(Scheme);
    	bcgTestData.setExpected(expected);

        BCG_ATT_TR_CliOperator bcgCliOperator = (BCG_ATT_TR_CliOperator) bcgProvider.provide(BCG_ATT_TR_Operator.class);

        boolean preActionResult;
		boolean exportValidationResult;
		boolean prepareImportResult;
		boolean importResult;
		boolean exportResult;
		boolean importValidationResult ;
		boolean importVerificationResult;
		double actualRate;
		double exportVariance;
		
		String pattern = "#.#";
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		
		LOGGER.info("Test Case Started");
		setTestcase(testID,"");
        setTestInfo("Run cstest to get the nodes");
        preActionResult = bcgCliOperator.preAction(bcgTestData);
		assertTrue(preActionResult);
        setTestStep("Nodes are not empty");
        //TODO VERIFY:Nodes are not empty
        
        setTestInfo("BCG Export for nodes");
    	exportResult = bcgCliOperator.bcgExport(bcgTestData);
    	assertTrue(exportResult);
        setTestStep("exports are succedded");
        //TODO VERIFY:Exports are succeeded

        setTestInfo("Validate Export XML file");
        exportValidationResult = bcgCliOperator.exportValidation(bcgTestData);
        assertTrue(exportValidationResult);
        setTestStep("XML should not be empty");
        //TODO VERIFY:Export XML should not be empty
        
        if(bcgTestData.getModifier() != null){
        	setTestInfo("Create Import File");
        	prepareImportResult = bcgCliOperator.prepareImportFile(bcgTestData);
    		assertTrue(prepareImportResult);
    		setTestStep("Import file created successfully");
    		//TODO VERIFY:Import file created successfully
        	
        	setTestInfo("Run Import for nodes");
    		importResult = bcgCliOperator.bcgImport(bcgTestData);
    		assertTrue(importResult);
    		setTestStep("Imports are successful");
    		//TODO VERIFY:Imports are successful
    		
    		if( !(bcgTestData.getExpected().equalsIgnoreCase("Import has failed") || bcgTestData.getExpected().equalsIgnoreCase("CLI operation failed")) ){
	    		setTestInfo("Import Validation");
	    		importValidationResult = bcgCliOperator.importValidation(bcgTestData);
	    		assertTrue(importValidationResult);
	    		setTestStep("Imports are successful");
	    		//TODO VERIFY:Validation is successful
	    		
	    		setTestInfo("Post Action");
	    		bcgCliOperator.postAction(bcgTestData);
	    		setTestStep("Post Action is successful");
	    		//TODO VERIFY:Post Action is successful
    		}

    		if(bcgTestData.getVerifyTestCase() != null){

	    		setTestInfo("Verify Import Test Case");
	    		importVerificationResult = bcgCliOperator.verifyImportTestCase(bcgTestData);
	    		assertTrue(importVerificationResult);
	    		setTestStep("Import Verification is successful");
	    		//TODO VERIFY:Import test case verification is successful
    		}
        }
        
        if (bcgTestData.getMimRate() != null ){
	        setTestStep("MO rate for Export");	
			setTestInfo("Calculating MO rate for Export ");
			double mRate = Double.parseDouble(mimRate);
			double varience = Double.parseDouble(varianceAllowed);
			actualRate = bcgCliOperator.verifyExportRateAboveMinimum(fileName);
			exportVariance = ((mRate - actualRate)*100)/mRate;
			
			String convertedActualRate = decimalFormat.format(actualRate);
			String convertedExpectedRate = decimalFormat.format(mimRate);
			String convertedVariance = decimalFormat.format(exportVariance);
			
			setAdditionalResultInfo("Actual MO rate = "+convertedActualRate +" MO/Sec");
			setAdditionalResultInfo("Expected MO rate = "+convertedExpectedRate + " MO/Sec");
			setAdditionalResultInfo("Actual variance = "+convertedVariance+"%");
			setAdditionalResultInfo("Variance Allowed <= "+varianceAllowed +"%");
			
			assertTrue((exportVariance <= (varience)));
        }
        
        
        LOGGER.info("Test Case Completed");
//        bcgCliOperator.cleanUpFiles();
//        bcgCliOperator.restartMCS();
    }
}

