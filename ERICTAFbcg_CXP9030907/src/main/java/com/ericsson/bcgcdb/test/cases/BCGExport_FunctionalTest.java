package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

public class BCGExport_FunctionalTest extends TorTestCaseHelper implements TestCase {
	
	Logger LOGGER = Logger.getLogger(BCGExport_FunctionalTest.class);

    @Inject
    private OperatorRegistry<BCGCDBExportOperator> bCGExportProvider;

    @Inject
    private BCGExportGetter bCGExportGetter;
 
    /**
     * @DESCRIPTION BCG Export for Nodes
     * @PRE Nodes should be available
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS_32698_BCG_CDB", title = "BCG CDB Export")
    @Context(context = {Context.CLI})
    @Test(groups={"KGB","CDB"})
    @DataDriven(name = "bcgexport_functionaltest")
    public void bCGExport(
		@Input("TestID") String TestID,
    		@Input("exportNodename") String exportNodename,
    		@Input("exportNodeType") String exportNodeType,
    		@Input("exportNodeFdn") String exportNodeFdn,
    		@Input("exportFileName") String exportFileName,
    		@Input("domain") String domain,
    		@Input("verifyMo") String verifyMo,
    		@Input("Compression") String compression,
    		@Input("TimeValue") String timeValue,
    		@Input("DateValue") String dateValue,
    		@Input("Expected") String expected) {
    	
    	BCGCDBExportTestData bcgcdbExportTestData = new BCGCDBExportTestData();
    	bcgcdbExportTestData.setExportNodeName(exportNodename);
    	bcgcdbExportTestData.setExportNodeType(exportNodeType);
    	bcgcdbExportTestData.setExportNodeFdn(exportNodeFdn);
    	bcgcdbExportTestData.setExportFileName(exportFileName);
    	bcgcdbExportTestData.setDomain(domain);
    	bcgcdbExportTestData.setVerifyMo(verifyMo);
    	bcgcdbExportTestData.setCompression(compression);
    	bcgcdbExportTestData.setTimeValue(timeValue);
    	bcgcdbExportTestData.setDateValue(dateValue);
    	bcgcdbExportTestData.setExpected(expected);

        BCGCDBExportCliOperator bcgcdbExportCliOperator = (BCGCDBExportCliOperator) bCGExportProvider.provide(BCGCDBExportOperator.class);
        
        boolean preActionResult;
		boolean validationoutput;
		boolean ExportResult;

		LOGGER.info("Test Case Started");
	setTestcase(TestID,"");
        setTestInfo("Run cstest to get the nodes");
        preActionResult = bcgcdbExportCliOperator.preAction(bcgcdbExportTestData);
		assertTrue(preActionResult);
        setTestStep("Nodes are not empty");
        //TODO VERIFY:Nodes are not empty
        
        setTestInfo("BCG Export for nodes");
    	ExportResult = bcgcdbExportCliOperator.BcgExport(bcgcdbExportTestData);
    	assertTrue(ExportResult);
        setTestStep("exports are succedded");
        //TODO VERIFY:exports are succedded

        setTestInfo("Validate Export XML file");
        validationoutput = bcgcdbExportCliOperator.validation(bcgcdbExportTestData);
        assertTrue(validationoutput);
        setTestStep("XML should not be empty");
        //TODO VERIFY:XML should not be empty
        
        LOGGER.info("Test Case Completed");
    }
}
