package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.F_BCGTestData;


public interface F_BCGImportOperator {
	
	public boolean preAction(F_BCGTestData f_bcgTestData);
	
	public boolean prepareImportFile(F_BCGTestData f_bcgTestData);
	
	public String bcgImport(F_BCGTestData f_bcgTestData);
	
	public boolean validation(F_BCGTestData f_bcgTestData);
	
	public void postAction(F_BCGTestData f_bcgTestData);
	
}


