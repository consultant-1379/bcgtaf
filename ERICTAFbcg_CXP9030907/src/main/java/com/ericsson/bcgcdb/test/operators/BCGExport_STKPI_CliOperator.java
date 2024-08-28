package com.ericsson.bcgcdb.test.operators;

import com.ericsson.bcgcdb.test.cases.BCGExport_STKPI_TestData;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.utils.FileFinder;

import org.apache.log4j.Logger;

@Operator(context = Context.CLI)
public class BCGExport_STKPI_CliOperator implements BCGExport_STKPI_Operator {

	protected final Logger LOGGER = Logger.getLogger(BCGExport_STKPI_CliOperator.class);

	Shell shell;
	Host host;
	User operUser;
	User rootUser;

	private final String BCGTOOLEXPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -e ";
	private final String MINUSD = "-d";
	private final String MINUSN = "-n";
	private final String SPACE = " ";
	private final String FILESIZE_GREATER_THAN_ZERO_COMMAND = "du -sk ";
	private final String BCG_EXPORT_FILES_DIRECTORY = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	private final String BCG_INSTRUMENTATION_RATE_GREP_STRING = "'MO/Sec for Export = '";
	private final String BCG_EXPORT_LOGS_DIRECTORY = " /var/opt/ericsson/nms_umts_wran_bcg/logs/export/";
	private final String NECHECK = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt MeContext -f neType==";
	private final String CUSTOMFILTER = "/opt/ericsson/nms_umts_bcg_meta/dat/customfilters/";
	private final String GETNODES = " | awk -F"+","+" '{print $1"+"\","+"\""+ "$2}' | uniq -d";
	private final String SMTOOL_COMMAND = "/opt/ericsson/nms_cif_sm/bin/smtool -set wran_bcg";
	private final String CACHEOFF = "sed 's/generation.counters=true/generation.counters=false/' /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh > /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg_new.sh";
	private final String RENAMEFILE = "mv /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg_new.sh /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh;chmod 755 /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh";
	private final String CACHEON = "sed 's/generation.counters=false/generation.counters=true/' /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh > /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg_new.sh";
	private final String MCRESTART = "/opt/ericsson/nms_cif_sm/bin/smtool coldrestart wran_bcg -reason=Other -reasontext=\"Made Cache changes\"";
	private final String SMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep wran_bcg";
	private final String CACHESTATUS = "grep 'generation.counters=' /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh  | awk '{print $1}'";



	int successfulExportCount = 0;

	BCGExportGetter bCGexportGetter;

	CLICommandHelper helper;
	CLICommandHelper rootHelper;
	CLI cli;

	String nodeList;
	String bachupAutoLockUnlock;

	/**
	 * Initializing host, user and cli
	 */
	public BCGExport_STKPI_CliOperator(){
		bCGexportGetter = new BCGExportGetter();
		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
		rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
		LOGGER.info("host.getIp()"+host.getIp());
		helper = new CLICommandHelper(host, operUser);
		LOGGER.info("Root user and pass : "+rootUser.getUsername()+" and "+rootUser.getPassword());
		rootHelper = new CLICommandHelper(host, rootUser);
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgTestData
	 * @return result
	 */
	public boolean preActionforexport(BCGExport_STKPI_TestData bcgExportTestData) {
		try{
			LOGGER.info("Checking server connectivity");
			String serverFiles = helper.simpleExec("cd /home/nmsadm/;ls");
			LOGGER.info("server files : "+serverFiles);
		}catch(Exception e){
			LOGGER.info("Exception while connecting to server " + host.getIp() + " "+ e);
		}

		String isNodeEmpty;
		if(!upadePEDParameters("ExportNodeIfUnsynched","true")){
			return false;
		}
		String neCheck = NECHECK+bcgExportTestData.getValue();
		LOGGER.info("Checking Node Availability");
		isNodeEmpty = helper.simpleExec(neCheck);
		if(bcgExportTestData.getValue().equalsIgnoreCase("1"))
			copyFiles();
		if(!isNodeEmpty.isEmpty())
			return findMo(bcgExportTestData);
		helper.disconnect();
		return false;
	}


	/**
	 * this method for export the mo
	 * @param exportfdn as string
	 * @param ExportMo as string
	 * @param ExportFileName as string
	 * @return boolean
	 */
	public boolean findMo(BCGExport_STKPI_TestData bcgExportTestData){
		String allNodes = null;
		StringBuffer stringBuffer = new StringBuffer();

		try{
			if(bcgExportTestData.getNodeType().equalsIgnoreCase("RNC")){
				
				String getAllNodes = NECHECK+bcgExportTestData.getValue();
				allNodes = helper.simpleExec(getAllNodes);
				String[] fdns = allNodes.split("\\n");
				if(fdns.length > 0){
					for(int i = 0 ; i < fdns.length; i++){
						stringBuffer.append(" ");
						stringBuffer.append(fdns[i].trim());
					}
				}

			}
			if(bcgExportTestData.getNodeType().equalsIgnoreCase("SGSN")){
				String allNodeType = bcgExportTestData.getValue();
				String nodeTypes[] = allNodeType.split(",");
				if(nodeTypes.length > 0){
					for ( int j = 0 ; j < nodeTypes.length ; j++ ){
						String getAllNodes = NECHECK + nodeTypes[j];
						allNodes = helper.simpleExec(getAllNodes);
						String[] fdns = allNodes.split("\\n");
						if(fdns.length > 0){
							for(int i = 0 ; i < fdns.length; i++){
								stringBuffer.append(" ");
								stringBuffer.append(fdns[i].trim());
							}
						}
					}
				}

			}if(bcgExportTestData.getNodeType().equalsIgnoreCase("ERBS")){
				String allNodeType = bcgExportTestData.getValue();
				String nodeTypes[] = allNodeType.split(",");
				if(nodeTypes.length > 0){
					for ( int j = 0 ; j < nodeTypes.length ; j++ ){
						String getAllNodes = NECHECK + nodeTypes[j] + " | grep -i lte  " + " " + GETNODES;
						allNodes = helper.simpleExec(getAllNodes);
		
						String[] fdns = allNodes.split("\\n");
						if(fdns.length > 0){
							for(int i = 0 ; i < fdns.length; i++){
								stringBuffer.append(" ");
								stringBuffer.append(fdns[i].trim());
							}
						}
					}
				}
			}else{
				String getAllNodes = NECHECK+bcgExportTestData.getValue()+" "+GETNODES;
				allNodes = helper.simpleExec(getAllNodes);

				String[] fdns = allNodes.split("\\n");
				if(fdns.length > 0){
					for(int i = 0 ; i < fdns.length; i++){
						stringBuffer.append(" ");
						stringBuffer.append(fdns[i].trim());
					}
				}
			}

		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
			helper.disconnect();
		}

		nodeList = stringBuffer.toString().trim();
		if(!nodeList.isEmpty()){
			LOGGER.info("Available node : "+nodeList);
			return true;
		}
		else{
			LOGGER.info("Nodes are NOT available for "+ bcgExportTestData.getNodeType());
			return false;
		}
	}

	/**
	 * This method will export for the node
	 * @return result as boolean
	 */
	public boolean BcgExport(BCGExport_STKPI_TestData bcgExportTestData){
		String bcgExport = null;
		for(int i = 0; i < bcgExportTestData.getItenation(); i++){
			try{
				LOGGER.info(bcgExportTestData.getNodeType() + " Export iteration : " + (i+1));
				if(bcgExportTestData.getValue().equalsIgnoreCase("1")){
					String bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getFilename() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain();
					bcgExport = helper.simpleExec(bcgExportCommand);
				}else{
					String bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getFilename() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
					bcgExport = helper.simpleExec(bcgExportCommand);
				}

			}catch(Exception e){
				LOGGER.info(e.getMessage());
				LOGGER.info(e);
			}finally{
				helper.disconnect();
			}
			LOGGER.info(bcgExport);
			if(bcgExport.contains("Export has succeeded")){
				LOGGER.info("Export Succeeded");
				if(validationforExport(bcgExportTestData)){
					successfulExportCount++;
				}
			}
			else{
				LOGGER.info("Export Failed");
			}
		}

		if(successfulExportCount > 0){
			LOGGER.info(successfulExportCount + " Exports successfull");
			return true;
		}
		else
			return false;
	}

	/**
	 * This method will check the validation of the bcg export test case
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean validationforExport(BCGExport_STKPI_TestData bcgExportTestData) {
		String bcgfilesizecommand = "";
		String exportoutputfilesize = "";
		int fileSize = 0;
		try{
			if(bcgExportTestData.getCompression() != null)
				bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgExportTestData.getFilename()+".gz | awk '{print $1}'";
			else
				bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgExportTestData.getFilename()+ " | awk '{print $1}'";
			exportoutputfilesize = helper.simpleExec(bcgfilesizecommand);
			fileSize = Integer.parseInt(exportoutputfilesize.trim());
			LOGGER.info("BCG Export filesize : "+fileSize);
		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
			helper.disconnect();
		}
		if(fileSize > 0)
			return true;
		else
			return false;
	}

	/**
	 * @param fileName Name of Bcg Export File
	 * @param minRate Minimum acceptable rate per second for the export
	 * @return Whether MO rate per second is above minRate
	 */
	public double[] verifyExportRateAboveMinimum(String Filename) {

		StringBuilder command;
		double moPerSecond = 0.0;
		double averageMoPerSecount = 0.0;
		double[] exportData = new double[3];
		String nodeCount = null;
		String moCount = null;
		String instrumentationFileName = Filename.replace(".xml", "") + "_instrument.txt"; 
		String getFileName = "ls -tr /var/opt/ericsson/nms_umts_wran_bcg/logs/export | grep " + instrumentationFileName + " | tail -"+successfulExportCount;
		String fileName = helper.simpleExec(getFileName);
		LOGGER.info("File Name : "+ fileName);
		if(!fileName.isEmpty()){
			String[] oneFileName = fileName.split("\n");
			for (int i = 0 ; i < successfulExportCount; i++){
				command = new StringBuilder("grep ").append(BCG_INSTRUMENTATION_RATE_GREP_STRING).append(BCG_EXPORT_LOGS_DIRECTORY).append(oneFileName[i].trim()).append(" | awk '{print $5}'");
				String moRate = helper.simpleExec(command.toString());
				LOGGER.info("MO/Sec for Export "+(i+1)+" : "+moRate);
				moPerSecond = moPerSecond + Double.parseDouble(moRate);
				command = null;
			}
			String getNumberOfNodes = "grep 'Number of Nodes from where MO(s) successfully exported' /var/opt/ericsson/nms_umts_wran_bcg/logs/export/"+oneFileName[successfulExportCount-1].trim()+" | awk '{print $10}'";
			nodeCount = helper.simpleExec(getNumberOfNodes);
			String getNumberOfMos = "grep 'Total Number of MOs exported' /var/opt/ericsson/nms_umts_wran_bcg/logs/export/"+oneFileName[successfulExportCount-1].trim()+" | awk '{print $7}'";
			moCount = helper.simpleExec(getNumberOfMos);
		}
		helper.disconnect();
		averageMoPerSecount = moPerSecond / successfulExportCount;

		exportData[0] = averageMoPerSecount;
		exportData[1] = Double.parseDouble(nodeCount);
		exportData[2] = Double.parseDouble(moCount);

		return exportData;
	}

	public double[] getMoRate(String Filename){

		double [] allMoRate = new double[successfulExportCount];
		StringBuilder getMoRateCommand;
		String instrumentationFileName = Filename.replace(".xml", "") + "_instrument.txt"; 
		String getFileName = "ls -tr /var/opt/ericsson/nms_umts_wran_bcg/logs/export | grep " + instrumentationFileName + " | tail -"+successfulExportCount;
		String fileName = helper.simpleExec(getFileName);
		if(!fileName.isEmpty()){
			String[] oneFileName = fileName.split("\n");
			for (int i = 0 ; i < successfulExportCount; i++){
				getMoRateCommand = new StringBuilder("grep ").append(BCG_INSTRUMENTATION_RATE_GREP_STRING).append(BCG_EXPORT_LOGS_DIRECTORY).append(oneFileName[i].trim()).append(" | awk '{print $5}'");
				String moRate = helper.simpleExec(getMoRateCommand.toString());
				allMoRate[i] = Double.parseDouble(moRate);
				getMoRateCommand = null;
			}
		}
		return allMoRate;
	}
	/**
	 * @param exitCode from method getExitValue()
	 * @return Whether Exit code of command is 0 or not
	 */
	boolean verifyExitCode() {
		int exitCode = shell.getExitValue(-1);
		LOGGER.info("Exit code of export command is exitCode");
		return exitCode == 0;

	}
	/**
	 * This method will copy the python script from Testware to server
	 * @return void
	 */
	private void copyFiles(){
		RemoteFileHandler remote = new RemoteFileHandler(host,rootUser);
		String pythonscr = FileFinder.findFile("WCDMA_Filter.xml").get(0);
		String localFileLocation = pythonscr;
		String remoteFileLocation = CUSTOMFILTER;
		remote.copyLocalFileToRemote(localFileLocation ,remoteFileLocation);
	}

	/**
	 * This method will update the PED parameter values
	 * @param ped_name
	 * @param ped_value
	 * @return result
	 */
	private boolean upadePEDParameters(String pedName, String pedValue) {

		boolean result = false;
		try{
			LOGGER.info("setting PED parameters: ");
			String updatepedcommand = SMTOOL_COMMAND + SPACE + pedName + SPACE + pedValue;
			LOGGER.info("BCG set PED parameter command : "+updatepedcommand);
			LOGGER.info(updatepedcommand);
			String PEDOutput = helper.simpleExec(updatepedcommand);
			if(!PEDOutput.contains("error"))
				result = true;
		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
			helper.disconnect();
		}
		return result;
	}

	public boolean settingCache(String command){

		String cacheOutput = "";
		String initialCacheStatus = "";
		String updatedCacheStatus = "";
		boolean result = false;
		boolean cacheStatus = false;

		try{
			initialCacheStatus = rootHelper.simpleExec(CACHESTATUS);
			if(command.equalsIgnoreCase("CACHEOFF")){
				LOGGER.info("setting Cache OFF ");
				if(initialCacheStatus.contains("true"))
					cacheOutput = rootHelper.simpleExec(CACHEOFF);
				else
					return true;

				String fileRenameOutput= rootHelper.simpleExec(RENAMEFILE);
				updatedCacheStatus = rootHelper.simpleExec(CACHESTATUS);
				if(updatedCacheStatus.contains("false"))
					cacheStatus = true;
			}else{
				LOGGER.info("setting Cache ON ");
				if(initialCacheStatus.contains("false"))
					cacheOutput = rootHelper.simpleExec(CACHEON);
				else
					return true;
				String fileRenameOutput= rootHelper.simpleExec(RENAMEFILE);
				updatedCacheStatus = rootHelper.simpleExec(CACHESTATUS);
				if(updatedCacheStatus.contains("true"))
					cacheStatus = true;
			}
			if(cacheStatus){
				LOGGER.info("MC Restart ");
				rootHelper.simpleExec(MCRESTART);
				while(!((rootHelper.simpleExec(SMTOOL)).contains("started")) && !((rootHelper.simpleExec(SMTOOL)).contains("failed")) 
						&& !((rootHelper.simpleExec(SMTOOL)).contains("offline")) ){
					rootHelper.disconnect();
				}
				LOGGER.info("BCG MC is up"+rootHelper.simpleExec(SMTOOL));
				if(rootHelper.simpleExec(SMTOOL).contains("started"))
					result = true;
			}
			else
				LOGGER.info("Cache not set properly");

		}catch(Exception e){
			LOGGER.info("Rollback Cache ON ");
			if(!(initialCacheStatus.equalsIgnoreCase(updatedCacheStatus))){
				if(initialCacheStatus.contains("true"))
					rootHelper.simpleExec(CACHEON);
				else
					rootHelper.simpleExec(CACHEOFF);
				rootHelper.simpleExec(RENAMEFILE);
				rootHelper.simpleExec(MCRESTART);
				LOGGER.info(e.getMessage());
				LOGGER.info(e);
			}
		}finally{
			rootHelper.disconnect();
		}
		return result;
	}
}
