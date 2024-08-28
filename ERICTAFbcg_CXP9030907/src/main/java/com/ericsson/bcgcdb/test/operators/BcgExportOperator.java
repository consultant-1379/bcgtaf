/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.bcgcdb.test.operators;

import java.util.List;

public interface BcgExportOperator {

	public <T> List<T> filterEvenDistributionPercentage(List<T> listToBeEvenlyDistributed,
			double percentage);

	public <T> List<T> filterEvenDistributionPercentage(List<T> listToBeEvenlyDistributed,
			double percentage, int offset);

	public List<String> csTestListMos(String managedObject, String attribute, String value);

	public void bcgExport(List<String> filteredManagedObjectList, String domain, String fileName);

	public boolean verifyExitCode();

	public boolean verifyFileSizeGreaterThanZero(String fileName);

	public boolean verifyKeywordInOutput();

	public List<String> getUniqueNodeList(List<String> filteredList);

	public boolean verifyExportRateAboveMinimum(String fileName, double minRate);

	public String getfileIdentifier(String managedObject, double percent, String domain,
			String fileName, String testCaseId);
}
