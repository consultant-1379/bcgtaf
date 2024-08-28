package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCG_ATT_TR_TestData;

public interface BCG_ATT_TR_Operator {
	
	public boolean preAction(BCG_ATT_TR_TestData bcgTestData);
		
	public boolean bcgExport(BCG_ATT_TR_TestData bcgTestData);
	
	public boolean prepareImportFile(BCG_ATT_TR_TestData bcgTestData);
	
	public boolean bcgImport(BCG_ATT_TR_TestData bcgTestData);

	public boolean exportValidation(BCG_ATT_TR_TestData bcgTestData);
	
	public double verifyExportRateAboveMinimum(String fileName);
	
	public boolean importValidation(BCG_ATT_TR_TestData bcgTestData);
	
	public void postAction(BCG_ATT_TR_TestData bcgTestData);
	
	public boolean verifyImportTestCase(BCG_ATT_TR_TestData bcgTestData);
	
	public long verifyTotalImportTime(String fileName);
	
}


