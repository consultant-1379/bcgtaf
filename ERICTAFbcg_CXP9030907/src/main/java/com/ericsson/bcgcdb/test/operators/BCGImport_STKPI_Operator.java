package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGImport_STKPI_TestData;


public interface BCGImport_STKPI_Operator {
	
	public boolean preAction(BCGImport_STKPI_TestData bcgTestData);
	
	public boolean bcgImport(BCGImport_STKPI_TestData bcgTestData);
	
	public double validation(String Filename , String Mimrate);

	public double verifyExportRateAboveMinimum(String importFileName,
			String mimRate);
		
}
