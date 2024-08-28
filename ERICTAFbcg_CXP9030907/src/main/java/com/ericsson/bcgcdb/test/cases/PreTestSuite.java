package com.ericsson.bcgcdb.test.cases;
import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

public class PreTestSuite extends TorTestCaseHelper implements TestCase {
	
	Logger LOGGER = Logger.getLogger(PreTestSuite.class);

	@Inject
	private OperatorRegistry<PreTestSuiteOperator> preTestSuiteProvider;

	@Inject
	private BCGExportGetter bcgExportGetter;

	@Inject
	private PreTestSuiteCliOperator operator;

	/**
	 * @DESCRIPTION BCG Export for Nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	@TestId(id = "Online_Offline_Simulation", title = "Online_Offline_Simulation")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB","CDB"})
	@DataDriven(name = "Online_Offline_Simulation")
	public void preSuite(
			@Input("TestID") String TestID,
			@Input("NE") String NE){
		
		BCGKGBExportData preSuiteTestData = new BCGKGBExportData();
		preSuiteTestData.setNE(NE);
		
		PreTestSuiteCliOperator preSuiteOperator = (PreTestSuiteCliOperator) preTestSuiteProvider.provide(PreTestSuiteOperator.class);
		
		/*
		 * 
		 * LOGGER.info("Test case started");
		setTestcase(TestID,"");
		setTestInfo("Run cstest to get the nodes");
		preActionResult = bCGKGBExportOperator.preAction(bcgExportTestData);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");
		//TODO VERIFY:Nodes are not empty
		 */
		boolean isSimulationsAreOnline = false;
		boolean isSimulationsAreOffline = false;
		
		LOGGER.info("Online-Offline Simulation started");
		setTestcase(TestID, "");
		setTestInfo("Online the Simulations");
		isSimulationsAreOnline = preSuiteOperator.onlineSimulation(preSuiteTestData);
		assertTrue(isSimulationsAreOnline);
		setTestStep("Simulations are Online");
		
		/*LOGGER.info("Online-Offline Simulation started");
		setTestcase(TestID, "");
		setTestInfo("Offline the Simulations");
		isSimulationsAreOffline = preSuiteOperator.offlineSimulation(preSuiteTestData);
		assertTrue(isSimulationsAreOffline);
		setTestStep("Simulations are Offline");*/
		
	}
	
}
