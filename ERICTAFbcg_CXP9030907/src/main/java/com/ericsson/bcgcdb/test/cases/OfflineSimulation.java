package com.ericsson.bcgcdb.test.cases;

//mport java.util.logging.Logger;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.*;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import javax.inject.Inject;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;



public class OfflineSimulation extends TorTestCaseHelper implements TestCase{

	Logger LOGGER = Logger.getLogger(OfflineSimulation.class);
	@Inject
	private PreTestSuiteCliOperator operator;
	

	@Inject
	private OperatorRegistry<PreTestSuiteOperator> preTestSuiteProvider;

	
	BCGKGBExportData preSuiteTestData = new BCGKGBExportData();
	String networkElement= "ERBS H1220;ERBS E1239;ERBS H1120;ERBS H1440;ERBS F1100;ERBS G1301;ERBS J1220;DSC 15B-WCDMA-V1;BCE 16A-CORE-V1;M-MGw C1193;M-MGw C176;M-MGw C193-V5;M-MGw C1124;M-MGw C153;PRBS 16A-WCDMA-V1;WCG 15A-CORE-V1;WCG 15B-CORE-V6;TCU03 15B-V6;TCU03 16B-V2;TCU04 16A-V6;TCU03 16A-V7;BSC G14B-APG43L;LANSWITCH R1;RXI K190;PRBS 16B-WCDMA-V2;MSRBS-V2 15B-V9;MSRBS-V2 16A-V12;MSRBS-V2 16B-V11;MSRBS-V2 18Q1-V3;MSRBS-V2 18-Q2-V2;SBG 16A-CORE-V1;CSCF 15A-CORE-V2;DUA-S 15A-CORE-ECIM-REF;EPG-WMG 15B-V1;PGM 16A-CORE-V2;IPWORKS 16A-CORE-V6;CEE 16A-V1;MTAS 15B-CORE-ECIM-REF;MTAS 16A;RNC V12290;RNC V10442;RNC V11338;RNC V81339;RNC V53878;RNC V71202;RNC V13316;RNC V32275;RNC V91067;RNC V8696;RBS U4430;";
	
	@AfterSuite(alwaysRun = true)
	public void stopScript(){
		
	LOGGER.info("Online-Offline Simulation started");
	preSuiteTestData.setNE(networkElement);
	PreTestSuiteCliOperator preSuiteOperator = (PreTestSuiteCliOperator) preTestSuiteProvider.provide(PreTestSuiteOperator.class);
	preSuiteOperator.offlineSimulation(preSuiteTestData);

	
	}
	
}

