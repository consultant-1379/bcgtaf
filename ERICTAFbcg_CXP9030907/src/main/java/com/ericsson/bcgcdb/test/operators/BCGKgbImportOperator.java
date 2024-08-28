package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGKGBImportTestData;


public interface BCGKgbImportOperator {
	
	public boolean preAction(BCGKGBImportTestData bcgTestData);
	
	public boolean prepareImportFile(BCGKGBImportTestData bcgTestData);
	
	public String bcgImport(BCGKGBImportTestData bcgTestData);
	
	public boolean validation(BCGKGBImportTestData bcgTestData);
	
	public void postAction(BCGKGBImportTestData bcgTestData);
	
}


