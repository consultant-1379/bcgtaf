package com.ericsson.bcgcdb.test.operators;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

public interface ImportStkpiOperator {

	String BCGTOOLFOREXPORT ="/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -e";
	String UTRANRELATION = "UtranRelation";
	String MOCFILTER = "-d :";
	String XMLFILEEXTENSION = ".xml";
	String EXPORTFILEPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	String LINE = "\n" ;
	String IMPORTFILEPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/import/";
	String BCGTOOLFORIMPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -i";
	String IMPORTLOGSPATH = "/var/opt/ericsson/nms_umts_wran_bcg/logs/import/";
	String BCGTOOLFORREMOVINGPLAN = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -rp ";
	String TEMPDIR = "/tmp/BCG_STKPI/";
	String BCGTOOLFORACTIVATINGPLAN = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -a ";

	public static final String[] TAGS ={":configData","xn:SubNetwork","xn:MeContext",
		"xn:ManagedElement","un:RncFunction","un:UtranRelation","un:attributes","un:adjacentCell"};

	/**
	 * This method will check for the value of PED Parameter: ExportNodeIfUnsynched
	 * @return boolean (Value of the PED Parameter)
	 */

	public boolean checkPEDParameterStatus();

	/**
	 * This method will set the value of the PED Parameter ExportNodeIfUnsynched value to false .
	 */

	public void setPEDParameter();
	/**
	 * This method will be performing MOC export on the server
	 * @return output (Output of the export command)
	 */

	public String performMOCExport();

	/**
	 * This method will check for the Exit Value of the command executed .
	 * @return result (integer)
	 */

	public int actualExitValue();

	/**
	 * This method will check whether the file exist on the remote location or not .
	 * @param path : Path of the file ,
	 * @param fileName : Name of the file .
	 * @return result (boolean)
	 */

	public boolean fileExists(String path , String fileName);

	/**
	 * This method will copy XML from the remote location .
	 * @return result (boolean)
	 */

	public boolean copyRemoteXmlFile();

	/**
	 * This method is to verify the size of file copied from the remote location .
	 * @param fileName : Name of the Import File .
	 * @return result (boolean)
	 */

	public boolean verifyFileSizeGreaterThanZero(String fileName);

	/**
	 * This method is to create import file on local .
	 * @param modifer : MO Modifier (create or delete) ,
	 * @param totalMOs : Total Number of MOs for which import file need to be created ,
	 * @param importFileName : Name of the Import File ,
	 * @param importType : Type of Import (Inter or Intra ).
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws IOException
	 * @return result (boolean)
	 */

	public boolean createImportFile(String modifier,int totalMOs,String importFileName,String importType) throws IOException, XMLStreamException, FactoryConfigurationError, ParserConfigurationException, TransformerException;

	/**
	 * This method is to verify import file exists on local or not .
	 * @param Name of the Import File .
	 * @return result (boolean)
	 */

	public boolean verifyImportFileExistOnLocal(String importFile);

	/**
	 * This method is to get the name of the export file name .
	 * @return String Export File Name
	 */
	public String getExportFileName();

	/**
	 * This method is to verify the total number of modifiers in the import file .
	 * @param modifer : MO Modifier (create or delete) ,
	 * @param totalMOs : Total Number of MOs for which import file need to be created ,
	 * @param importFile : Name of the Import File ,
	 * @return result (boolean)
	 */

	public boolean verifyTotalImportCommands(String importFile,String modifier,int totalMOs);

	/**
	 * This method is to transfer the import file from local to remote location .
	 * @param importFile : Name of the Import File .
	 * @return result (boolean)
	 */

	public boolean copyLocalXmlFile(String importFile);

	/**
	 * This method is to execute the import command on the server .
	 * @param importFileName : Name of the Import File ,
	 * @param planName : Name of the plan in which import need to be performed .
	 * @return String Output of import command
	 */

	public String performImport(String importFileName, String planName);

	/**
	 * This method will calculate and return the value of current MO Rate .
	 * @param importFileName : Name of the Import File ,
	 * @param totalMOs : Total Number of MOs .
	 * @return result (Integer)
	 */

	public double verifyImportMoRate(String importFileName,int totalMOs);

	/**
	 * This method will perform the clean up activities like removing the export file from local and remote
	 * location and deleting the plan .
	 * @param importFileName : Name of the Import File
	 */

	public void exitCleanUp(String importFileName);

	/**
	 * This method will create the Temp Directory for keeping intermediate
	 * files during execution of the use case.
	 */

	public void createDirectoryInTemp();

	/**
	 * This method will return the output of Plan Activation command .
	 * @return String Output of Plan Activation command .
	 */

	public String performActivation() ;


	/**
	 * This method will return the output of Plan Deletion command .
	 * @return String Output of Plan Deletion command .
	 */

	public String deletePlan();

	/**
	 * This method will convert create file to delete file .
	 * @param importFile : Name of the Import File ,
	 * @param intermediateFile : Intermediate file created for deletion .
	 */


	public void convertCreateFileToDeleteFile(String importFile,String intermediateFile) throws FileNotFoundException, IOException;

	/**
	 * This method will perform the clean up activities like removing the export file from local and remote
	 * location and also removal of intermediate import file from local and remote .
	 * @param intermediateImportFileName : Name of the Intermediate Delete Import File
	 */

	void exitCleanUpForCreation(String intermediateImportFileName);



}
