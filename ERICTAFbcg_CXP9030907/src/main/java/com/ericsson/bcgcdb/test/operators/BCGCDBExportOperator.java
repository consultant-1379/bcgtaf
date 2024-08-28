package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGCDBExportTestData;

public interface BCGCDBExportOperator {
	
	public boolean preAction(BCGCDBExportTestData bcgTestData);
	
	public boolean BcgExport(BCGCDBExportTestData bcgTestData);

	public boolean validation(BCGCDBExportTestData bcgTestData);
	
}


