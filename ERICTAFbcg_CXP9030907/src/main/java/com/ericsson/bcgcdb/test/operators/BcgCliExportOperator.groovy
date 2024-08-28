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

package com.ericsson.bcgcdb.test.operators

import groovy.util.logging.Log4j

import com.ericsson.cifwk.taf.annotations.Context
import com.ericsson.cifwk.taf.annotations.Operator
import com.ericsson.cifwk.taf.data.DataHandler
import com.ericsson.cifwk.taf.data.Host
import com.ericsson.cifwk.taf.data.HostType
import com.ericsson.cifwk.taf.data.User
import com.ericsson.cifwk.taf.data.UserType
import com.ericsson.cifwk.taf.tools.cli.CLI
import com.ericsson.cifwk.taf.tools.cli.Shell
import com.ericsson.bcgcdb.test.getters.BcgExportCliGetter

@Log4j
@Operator(context = Context.CLI)
@Singleton
class BcgCliExportOperator implements BcgExportOperator {

	static final String FILTER_STRING = " -f "
	static final String COMPARE_EQUALS = "=="
	static final String NEWLINE = "\\n"
	static final String OPEN_SQUARE_BRACKET = "["
	static final String CLOSE_SQUARE_BRACKET = "]"
	static final String COMMA = ","
	static final String COMMA_SPACE = "${COMMA}${SPACE}"
	static final String EMPTY_STRING  = ""
	static final String SPACE  = " "
	static final String BCG_EXPORT_FLAG = "-e"
	static final String BCG_EXPORT_RADIO_TRANSPORT_FLAG = "-d"
	static final String BCG_MO_LIST_FLAG = "-n"
	static final String LS_COMMAND = "ls"
	static final String FILESIZE_GREATER_THAN_ZERO_COMMAND = 'test -s '
	static final String PIPE = "|"
	static final String BCG_KEYWORD_IN_OUTPUT = "Export has succeeded"
	static final String SUBNETWORK = "SubNetwork"
	static final String SUBNETWORK_SUBNETWORK = "${SUBNETWORK},${SUBNETWORK}"
	static final String CSTEST_OUTPUT_LOWER_MO_STRING = ",ManagedElement=1,"
	static final String BCG_INSTRUMENTATION_RATE_GREP_STRING = "'MO/Sec for Export = '"

	BcgExportCliGetter bcgExportCliGetter

	CLI cli
	Shell shell
	Host rcHost
	User operUser

	public BcgCliExportOperator() {
		bcgExportCliGetter = new BcgExportCliGetter()

		rcHost = DataHandler.getHostByType(HostType.RC)
		operUser = new User(rcHost.getUser(UserType.OPER), rcHost.getPass(UserType.OPER), UserType.OPER)
		cli = new CLI(rcHost, operUser)
	}

	/**
	 * @param listToBeEvenlyDistributed A List of items to be evenly distributed
	 * @param percentage The percentage of items that should be evenly distributed
	 * @param offset The index of the even distribution
	 * @return Evenly distributed List of items of appropriate type
	 */
	public <T> List<T> filterEvenDistributionPercentage(List<T> listToBeEvenlyDistributed, double percentage, int offset=0) {
		List<T> filteredList = []
		int indexOfList = 0
		List<T> tempList = new ArrayList<T>(listToBeEvenlyDistributed)
		Collections.rotate(tempList,offset)

		for(int i=1; indexOfList<tempList.size(); i++) {
			filteredList.add(tempList.get(indexOfList))
			indexOfList = (int) Math.round((1 / (percentage / 100)) * i)
		}
		return filteredList
	}

	/**
	 * @param managedObject The ManagedObject (MeContext or SubNetwork) to list
	 * @param attribute The attribute to filter on
	 * @param value The value of attribute to filter on
	 * @return managedObjectList
	 */
	List<String> csTestListMos(String managedObject, String attribute, String value) {
		List<String> managedObjectList = []
		String lastPartOfMo = managedObject.split(COMMA)[-1]
		String csTestCommand = "${bcgExportCliGetter.getCsTestCommandHeader()} $lastPartOfMo"
		if (attribute != null && value != null &&
		!managedObject.equals(SUBNETWORK) && !managedObject.equals(SUBNETWORK_SUBNETWORK))
			csTestCommand += "$FILTER_STRING$attribute$COMPARE_EQUALS$value"
		log.debug "Executing cstest command: $csTestCommand"
		shell = cli.executeCommand(csTestCommand)
		String managedObjects = shell.read()
		String [] lines = managedObjects.split(NEWLINE)

		if (managedObject.equals(SUBNETWORK)) {
			managedObjectList.add(lines[0])
		}
		else {
			lines.each{ managedObjectList.add(it) }
			if (managedObject.contains(SUBNETWORK_SUBNETWORK)) {
				managedObjectList.remove(lines[0])
			}
		}
		return managedObjectList
	}

	/**
	 * @param filteredManagedObjectList The list of Managed Objects to use in the export
	 * @param filter Type of output - Radio, transport or both
	 * @param fileName The export fileName
	 */
	void bcgExport(List<String> filteredManagedObjectList, String domain, String fileName) {
		String filteredManagedObjectListString = filteredManagedObjectList.join(SPACE)
		String bcgExportCommand = "${bcgExportCliGetter.BCGTOOL_HEADER}$SPACE$BCG_EXPORT_FLAG$SPACE$fileName$SPACE$BCG_EXPORT_RADIO_TRANSPORT_FLAG$SPACE$domain$SPACE$BCG_MO_LIST_FLAG$SPACE$filteredManagedObjectListString"
		log.debug "BCG Export Command is $bcgExportCommand"
		shell = cli.executeCommand(bcgExportCommand)
	}

	/**
	 * @param exitCode from method getExitValue()
	 * @return Whether Exit code of command is 0 or not
	 */
	boolean verifyExitCode() {
		int exitCode = shell.getExitValue(-1)
		log.debug "Exit code of export command is ${exitCode}"
		return exitCode == 0

	}

	/**
	 * @param The file to check size greater than zero
	 * @return File size greater than zero or not
	 */
	boolean verifyFileSizeGreaterThanZero(String fileName) {
		String command = "${FILESIZE_GREATER_THAN_ZERO_COMMAND}${bcgExportCliGetter.BCG_EXPORT_FILES_DIRECTORY}$fileName"
		shell = cli.executeCommand(command)
		int exitCode = shell.getExitValue()
		log.debug "Exit code of BCG export file existing check is ${exitCode}"
		return exitCode == 0
	}

	/**
	 * @param localShell The shell used to execute a command
	 * @return Whether keyword exists in export command output
	 */
	boolean verifyKeywordInOutput(){
		log.debug "Checking that the keyword $BCG_KEYWORD_IN_OUTPUT is contained in output"
		boolean found = false
		String result
		try {
			result = shell.expect("Export has succeeded", -1)
			found = true
		}
		catch (RuntimeException e) {
			found = false
		}
		return found
	}

	/**
	 * @param filteredList List of Mos returned from cstest
	 * @return A set of unique Mos based on filtered list
	 */
	public List<String> getUniqueNodeList(List<String> filteredList) {
		log.debug "MO List (${filteredList.size()}) before checking for duplicates is $filteredList"
		Set<String> uniqueNodeSet = []
		filteredList.each{ node ->
			if (!node.contains(CSTEST_OUTPUT_LOWER_MO_STRING)) {
				uniqueNodeSet.add(node)
			} else {
				uniqueNodeSet.add(node.split(CSTEST_OUTPUT_LOWER_MO_STRING)[0])
			}
		}
		log.debug "MO List (${uniqueNodeSet.size()}) after removal of duplicates is ${uniqueNodeSet as String}"
		return uniqueNodeSet as List<String>
	}

	/**
	 * @param fileName Name of Bcg Export File
	 * @param minRate Minimum acceptable rate per second for the export
	 * @return Whether MO rate per second is above minRate
	 */
	boolean verifyExportRateAboveMinimum(String fileName, double minRate) {
		String instrumentationFileName = fileName.replace(".xml", "") + "_instrument.txt"
		String command = "grep ${BCG_INSTRUMENTATION_RATE_GREP_STRING} \$(ls -tr ${bcgExportCliGetter.BCG_EXPORT_LOGS_DIRECTORY}${instrumentationFileName}* | tail -1)  | awk '{print \$5}'"
		Shell localShell = cli.executeCommand(command)
		String result = localShell.read()
		double actualRate = result as double
		log.debug "Rate of export is $actualRate"
		return actualRate >= minRate
	}

	/**
	 * @param managedObject MO passed to the earlier export
	 * @param percent Percentage of MOs passed to the earlier export
	 * @param domain Domain passed to the earlier export
	 * @param fileName FileName passed to the earlier export
	 * @param testCaseId ID of the export test case
	 * @return fileName with xml extension
	 */
	String getfileIdentifier(String managedObject, double percent, String domain, String fileName, String testCaseId){
		String lastPartOfMo = managedObject.split(COMMA)[-1]
		String fileNameHeader = "${testCaseId}_${lastPartOfMo}_${percent}_${domain}_${fileName}"
		return "${fileNameHeader}.xml"
	}
}
