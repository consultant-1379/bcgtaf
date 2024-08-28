package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGKGBExportData;

public interface PreTestSuiteOperator {
	
	public boolean onlineSimulation(BCGKGBExportData bcgTestData);
	
	public boolean offlineSimulation(BCGKGBExportData bcgTestData);

}

