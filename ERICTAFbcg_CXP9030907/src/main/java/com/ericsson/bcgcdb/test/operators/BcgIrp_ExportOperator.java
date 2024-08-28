package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BcgIrp_TestData;;


public interface BcgIrp_ExportOperator {
	
	public boolean preAction(BcgIrp_TestData bcgIrp_TestData);
	
	public boolean runIrathomRefresh(BcgIrp_TestData bcgIrp_TestData);
	
	public boolean validation(BcgIrp_TestData bcgIrp_TestData);
	
}


