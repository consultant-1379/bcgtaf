/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.bcgcdb.test.cases

import javax.inject.Inject

import org.testng.annotations.Test

import com.ericsson.cifwk.taf.TestCase
import com.ericsson.cifwk.taf.TorTestCaseHelper
import com.ericsson.cifwk.taf.annotations.Context
import com.ericsson.cifwk.taf.annotations.DataDriven
import com.ericsson.cifwk.taf.annotations.Input
import com.ericsson.cifwk.taf.annotations.TestId
import com.ericsson.cifwk.taf.guice.OperatorRegistry
import com.ericsson.bcgcdb.test.operators.BcgExportOperator

class BcgExport extends TorTestCaseHelper implements TestCase {

	@Inject
	OperatorRegistry<BcgExportOperator> operatorRegistry

	/**
	 * BCG Export Test
	 * @DESCRIPTION BCG Export of defined list of Mos
	 * @PRIORITY HIGH
	 */
	@TestId(id="OSS-9872_Perf_1",title="BCG Export Test")
	@Context(context=[Context.CLI])
	@DataDriven(name = "BcgExport")
	@Test(groups=["Acceptance"])
	public void bcgExportTest(
			@Input("exportMo") String managedObject,
			@Input("attribute") String attribute,
			@Input("value") String value,
			@Input("percentage") double percent,
			@Input("domain") String domain,
			@Input("fileIdentifier") String fileIdentifier,
			@Input("min_rate") double minRate) {

		BcgExportOperator bcgOperator = operatorRegistry.provide(BcgExportOperator.class)

		String moAttributeAndValueFilter = (managedObject != null && attribute != null) ? "with filter on Mo $managedObject and attribute $attribute with value $value" : ""
		setTestStep("Run cstest command $moAttributeAndValueFilter to get relevant MO list")
		List<String> managedObjectList = bcgOperator.csTestListMos(managedObject, attribute, value)

		setTestStep("Filter list for the appropriate ${percent}% of Mos, removing any duplicate Mos")
		List<String> filteredManagedObjectList = bcgOperator.getUniqueNodeList(managedObjectList)
		filteredManagedObjectList = bcgOperator.filterEvenDistributionPercentage(filteredManagedObjectList, percent)

		setTestStep("Run BCG Export on the filtered list, creating export file")
		String fileNameWithUniqueIdentifier = bcgOperator.getfileIdentifier(managedObject, percent, domain, fileIdentifier, getTestId())
		bcgOperator.bcgExport(filteredManagedObjectList, domain, fileNameWithUniqueIdentifier)

		setTestStep("Verify relevant keyword is in output")
		assertTrue bcgOperator.verifyKeywordInOutput()

		setTestStep("Verify exit code of export command is 0")
		assertTrue bcgOperator.verifyExitCode()

		setTestStep("Verify export file is created and size is greater than 0")
		assertTrue bcgOperator.verifyFileSizeGreaterThanZero(fileNameWithUniqueIdentifier)

		setTestStep("Verify MO export rate is above minimum rate (${minRate})")
		assertTrue bcgOperator.verifyExportRateAboveMinimum(fileNameWithUniqueIdentifier, minRate)
	}
}
