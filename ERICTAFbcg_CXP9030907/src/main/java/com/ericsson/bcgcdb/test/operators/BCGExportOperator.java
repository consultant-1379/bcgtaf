package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGExportTestData;

public interface BCGExportOperator {
	
	public boolean preAction(BCGExportTestData bcgTestData);
	
	public boolean BcgExport(BCGExportTestData bcgTestData);

	public boolean validation(BCGExportTestData bcgTestData);
	
}


