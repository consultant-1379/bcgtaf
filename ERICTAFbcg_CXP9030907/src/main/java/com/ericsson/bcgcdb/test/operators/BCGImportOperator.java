package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGTestData;


public interface BCGImportOperator {
	
	public boolean preAction(BCGTestData bcgTestData);
	
	public boolean prepareImportFile(BCGTestData bcgTestData);
	
	public String bcgImport(BCGTestData bcgTestData);
	
	public boolean validation(BCGTestData bcgTestData);
	
	public void postAction(BCGTestData bcgTestData);
	
}


