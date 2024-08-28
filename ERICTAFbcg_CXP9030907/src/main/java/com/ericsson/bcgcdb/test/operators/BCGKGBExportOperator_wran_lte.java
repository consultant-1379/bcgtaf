package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGKGBExportData_wran_lte;;

public interface BCGKGBExportOperator_wran_lte {
	
	public boolean preAction(BCGKGBExportData_wran_lte bcgTestData);
	
	public boolean BcgExport(BCGKGBExportData_wran_lte bcgTestData);

	public boolean validation(BCGKGBExportData_wran_lte bcgTestData);
	
}


