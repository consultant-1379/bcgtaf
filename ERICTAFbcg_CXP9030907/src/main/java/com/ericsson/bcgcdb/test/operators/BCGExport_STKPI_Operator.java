package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGExport_STKPI_TestData;


public interface BCGExport_STKPI_Operator {
	
	public boolean BcgExport(BCGExport_STKPI_TestData bcgExportTestData);
	
	public boolean preActionforexport(BCGExport_STKPI_TestData bcgExportTestData);
	
	public boolean validationforExport(BCGExport_STKPI_TestData bcgExportTestData);
	
	public double[] verifyExportRateAboveMinimum(String Filename);
	
	public boolean settingCache(String status);
	
	public double[] getMoRate(String Filename);
}
