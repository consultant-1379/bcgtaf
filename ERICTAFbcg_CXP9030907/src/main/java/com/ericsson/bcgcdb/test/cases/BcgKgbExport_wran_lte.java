package com.ericsson.bcgcdb.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.exceptions.*;
import com.ericsson.cifwk.taf.guice.*;
import com.ericsson.cifwk.taf.tools.cli.*;

import org.testng.annotations.Test;
import javax.inject.Inject;
import java.util.*;

import com.ericsson.bcgcdb.test.operators.*;
import com.ericsson.bcgcdb.test.getters.*;

public class BcgKgbExport_wran_lte extends TorTestCaseHelper implements TestCase {

	@Inject
	private OperatorRegistry<BCGKGBExportOperator_wran_lte> bCGExportProvider;

	@Inject
	private BCGExportGetter bCGExportGetter;

	/**
	 * @DESCRIPTION BCG Export for Nodes
	 * @PRE Nodes should be available
	 * @PRIORITY HIGH
	 */
	@TestId(id = "OSS-32698_Func_3", title = "BCG Export")
	@Context(context = {Context.CLI})
	@Test(groups={"KGB","CDB"})
	@DataDriven(name = "bcg_KGB_export_wran_lte")
	public void bCGExport(
			@Input("TestID") String TestID,
			@Input("exportNodename") String exportNodename,
			@Input("exportNodeType") String exportNodeType,
			@Input("exportNodeFdn") String exportNodeFdn,
			@Input("numberOfFdns") int numberOfFdns,
			@Input("exportFileName") String exportFileName,
			@Input("domain") String domain,
			@Input("verifyMo") String verifyMo,
			@Input("Compression") String compression,
			@Input("PED_autoLockUnlock") String PED_autoLockUnlock,
			@Input("TimeValue") String timeValue,
			@Input("DateValue") String dateValue,
			@Input("Expected") String expected) {

		BCGKGBExportData_wran_lte bcgExportKgbwranlte = new BCGKGBExportData_wran_lte();
		bcgExportKgbwranlte.setExportNodename(exportNodename);
		bcgExportKgbwranlte.setExportNodeType(exportNodeType);
		bcgExportKgbwranlte.setExportNodeFdn(exportNodeFdn);
		bcgExportKgbwranlte.setNumberOfFdns(numberOfFdns);
		bcgExportKgbwranlte.setExportFileName(exportFileName);
		bcgExportKgbwranlte.setDomain(domain);
		bcgExportKgbwranlte.setVerifyMo(verifyMo);
		bcgExportKgbwranlte.setCompression(compression);
		bcgExportKgbwranlte.setPED_autoLockUnlock(PED_autoLockUnlock);
		bcgExportKgbwranlte.setTimeValue(timeValue);
		bcgExportKgbwranlte.setDateValue(dateValue);
		bcgExportKgbwranlte.setExpected(expected);

		BCGKGBExportCliOperator_wran_lte bcgkgbExportCliOperator_wran_lte = (BCGKGBExportCliOperator_wran_lte) bCGExportProvider.provide(BCGKGBExportOperator_wran_lte.class);

		boolean preActionResult;
		boolean validationoutput;	
		boolean ExportResult;

		setTestcase(TestID,"");
		setTestInfo("Run cstest to get the nodes");
		preActionResult = bcgkgbExportCliOperator_wran_lte.preAction(bcgExportKgbwranlte);
		assertTrue(preActionResult);
		setTestStep("Nodes are not empty");
		//TODO VERIFY:Nodes are not empty

		setTestInfo("BCG Export for nodes");
		ExportResult = bcgkgbExportCliOperator_wran_lte.BcgExport(bcgExportKgbwranlte);
		assertTrue(ExportResult);
		setTestStep("exports are succedded");
		//TODO VERIFY:exports are succedded

		setTestInfo("Validate Export XML file");
		validationoutput = bcgkgbExportCliOperator_wran_lte.validation(bcgExportKgbwranlte);
		assertTrue(validationoutput);
		setTestStep("XML should not be empty");
		//TODO VERIFY:XML should not be empty


	}
}

