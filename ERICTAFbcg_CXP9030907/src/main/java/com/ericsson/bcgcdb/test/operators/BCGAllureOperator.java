package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGKGBExportData;

public interface BCGAllureOperator {
	
	public boolean preAction(BCGKGBExportData bcgTestData);
	
	public boolean BcgExport(BCGKGBExportData bcgTestData);

	public boolean validation(BCGKGBExportData bcgTestData);
	
}


