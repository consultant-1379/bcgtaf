package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.apache.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;

public class BcgIrp_Test extends TorTestCaseHelper implements TestCase {
	
	Logger LOGGER = Logger.getLogger(BcgIrp_Test.class);
	
	@Inject
	private OperatorRegistry<BcgIrp_ExportOperator> bcgIrpExporOperator;
	
	@Inject
	private BcgIrp_ExportCliOperator operator;
	
	@BeforeSuite
	public void preCondition(){
		try{
			operator.irathomMCOnlineOffline("Online");
			}
		catch(Exception pre){
			LOGGER.info("Pre Action failed" + pre.getMessage());
		}
	}
	
    /**
     * @DESCRIPTION BCG Export for Nodes using IRP
     * @PRE Nodes should be available on server
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS_59298_BCG_KGB", title = "BCG KGB IROTHOM Refresh use case")
    @Context(context = {Context.CLI})
    @Test(groups={"KGB"})
    @DataDriven(name = "bcgIrp")
    public void bCGIrpExport(
		@Input("TestId") String TestID,
    		@Input("subnetworkAction") String subnetworkAction,
    		@Input("fileName") String fileName,
    		@Input("ipAddress") String ipAddress,
    		@Input("bulkUserName") String bulkUserName,
    		@Input("bulkPass") String bulkPass,
    		@Input("schedule") String schedule,
    		@Input("rantype") String rantype,
    		@Input("rootmo") String rootmo,
    		@Input("fileTransfer") String fileTransfer) {
    	
    	BcgIrp_TestData bcgIrp_TestData = new BcgIrp_TestData();
    	bcgIrp_TestData.setSubnetwork(subnetworkAction);
    	bcgIrp_TestData.setFileName(fileName);
    	bcgIrp_TestData.setIpaddress(ipAddress);
    	bcgIrp_TestData.setBulkusername(bulkUserName);
    	bcgIrp_TestData.setBulkPass(bulkPass);
    	bcgIrp_TestData.setSchedule(schedule);
    	bcgIrp_TestData.setRantype(rantype);
    	bcgIrp_TestData.setRootmo(rootmo);
    	bcgIrp_TestData.setFileTransfer(fileTransfer);

        BcgIrp_ExportCliOperator bcgIrpExportCliOperator = (BcgIrp_ExportCliOperator) bcgIrpExporOperator.provide(BcgIrp_ExportOperator.class);
        
        boolean preActionResult;
        boolean irathomRefreshOutout;
        boolean validationResult;

		LOGGER.info("Test Case Started");
		setTestcase(TestID,"");
        //setTestInfo("setting username and password for IRATHOM and BULKCMIRP");
        setTestStep("setting username and password for IRATHOM and BULKCMIRP");
        preActionResult = bcgIrpExportCliOperator.preAction(bcgIrp_TestData);
		assertTrue(preActionResult);
        
		setTestStep("Run irathom refresh");
    //    setTestInfo("Run irathom refresh");
        irathomRefreshOutout = bcgIrpExportCliOperator.runIrathomRefresh(bcgIrp_TestData);
        assertTrue(irathomRefreshOutout);
        
        setTestStep("Validation for irathom refresh");
     //   setTestInfo("Validation for irathom refresh");
        validationResult = bcgIrpExportCliOperator.validation(bcgIrp_TestData);
        assertTrue(validationResult);
        LOGGER.info("Test Case Completed");
        
    }
    
    @AfterSuite
    public void postCondition(){
    	try{
			operator.irathomMCOnlineOffline("Offline");
			}
		catch(Exception post){
			LOGGER.info("Pre Action failed" + post.getMessage());
		}
    }
}
