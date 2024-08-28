package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.S_BCGTestData;


public interface S_BCGImportOperator {
	
	public boolean preAction(S_BCGTestData bcgTestData);
	
	public boolean prepareImportFile(S_BCGTestData bcgTestData);
	
	public String bcgImport(S_BCGTestData bcgTestData);
	
	public boolean validation(S_BCGTestData bcgTestData);
	
	public void postAction(S_BCGTestData bcgTestData);
	
}
