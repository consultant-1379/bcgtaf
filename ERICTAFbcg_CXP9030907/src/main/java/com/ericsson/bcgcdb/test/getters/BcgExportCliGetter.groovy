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
package com.ericsson.bcgcdb.test.getters

import groovy.util.logging.Log4j

import com.ericsson.cifwk.taf.CliGetter
import com.ericsson.cifwk.taf.data.Host

@Log4j
class BcgExportCliGetter implements CliGetter {

	static final String CSTEST_HEADER = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s"
	static final String SEG_CS = "Seg_masterservice_CS"
	static final String NAMING_SERVICE_STRING = "-ns masterservice"
	static final String LIST_TYPE_COMMAND = "lt"

	static final String BCGTOOL_HEADER = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh"
	static final String BCG_DIRECTORY = "/var/opt/ericsson/nms_umts_wran_bcg/"
	static final String BCG_EXPORT_FILES_DIRECTORY = "${BCG_DIRECTORY}files/export/"
	static final String BCG_IMPORT_FILES_DIRECTORY = "${BCG_DIRECTORY}files/import/"
	static final String BCG_EXPORT_LOGS_DIRECTORY = "${BCG_DIRECTORY}logs/export/"


	/**
	 * @return Header for cstest command
	 */
	static String getCsTestCommandHeader() {
		String csTestCommandHeader  = "$CSTEST_HEADER $SEG_CS $NAMING_SERVICE_STRING $LIST_TYPE_COMMAND"
		csTestCommandHeader
	}
}
