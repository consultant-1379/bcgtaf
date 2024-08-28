package com.ericsson.bcgcdb.test.operators;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.implementation.SshRemoteCommandExecutor;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

import org.apache.log4j.Logger;

import com.ericsson.bcgcdb.test.cases.BCGExportTestData;
import com.ericsson.bcgcdb.test.cases.BCGTestData;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;

import java.util.Map;

@Operator(context = Context.CLI)
public class BCGExportCliOperator implements BCGExportOperator {
	
	Logger LOGGER = Logger.getLogger(BCGExportCliOperator.class);

	BCGExportGetter bCGexportGetter;
	CLICommandHelper helper;

	String autoLockUnlock;
	String nodeList;
	String bachupAutoLockUnlock;

	Shell shell;
	Host host;
	User operUser;
	private final String CSTESTLA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice la ";
	private final String EXPORTPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	private final String BCGTOOLEXPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -e ";
	private final String FILESIZE_GREATER_THAN_ZERO_COMMAND = "du -sk ";
	private final String BCG_EXPORT_FILES_DIRECTORY = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	private final String NECHECK = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt MeContext -f neType==";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String SPACE = " ";
	private final String MINUSD = "-d";
	private final String MINUSN = "-n";



	/**
	 * Initializing host, user and cli
	 */
	public BCGExportCliOperator(){
		bCGexportGetter = new BCGExportGetter();
		host = HostGroup.getOssmaster();
	   // helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
	operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
//		LOGGER.info("host.getIp()"+host.getIp());
	helper = new CLICommandHelper(host, operUser);
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgTestData
	 * @return result
	 */
	public boolean preAction(BCGExportTestData bcgExportTestData) {
		LOGGER.info("*preAction*");
		try{
			LOGGER.info("Checking server connectivity");
			String serverFiles = helper.simpleExec("cd /home/nmsadm/;ls");
			LOGGER.info("server files : "+serverFiles);
		}catch(Exception e){
			LOGGER.info("Exception while connecting to server " + host.getIp() + " "+ e);
		}
		if(!findMos(bcgExportTestData))
			return false;
		return true;
	}

	/**
	 * This method will check the server for the required Mos
	 * @param importmo
	 * @param fdnpath
	 * @return result
	 */
	private boolean findMos(BCGExportTestData bcgExportTestData) {
		LOGGER.info("*findMos*");
		String allNodes = null;
		try{
			String getAllNodes = NECHECK+bcgExportTestData.getExportNodeType();
			LOGGER.info("All nodes list command");
			allNodes = helper.simpleExec(getAllNodes);
			LOGGER.info("List of Nodes: " + allNodes);
			if(!allNodes.isEmpty()){
				String[] fdns = allNodes.split("\n");
				if(fdns.length > 0){
					for(int i = 0 ; i < fdns.length; i++){
						if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("4")){
							String[] fdn = fdns[i].split(",");
							if(bcgExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
								if(fdn.length >=3){
									if(fdn[0].contains("SubNetwork") && fdn[1].contains("SubNetwork") && fdn[2].contains("MeContext") ){
										if(isNodeSynchronized(fdns[i])){
											nodeList = fdns[i];
											break;
										}
									}else{
										if(fdn[0].contains("SubNetwork") && fdn[1].contains("MeContext") ){
											if(isNodeSynchronized(fdns[i])){
												nodeList = fdns[i];
												break;
											}
										}
									}
								}
							}
						}else{
							if(isNodeSynchronized(fdns[i])){
								nodeList = fdns[i];
								break;
							}
						}
					}
				}
			}
		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
			// helper.disconnect();
		}

		if(!nodeList.isEmpty()){
			LOGGER.info("Available node : "+nodeList);
			return true;
		}
		else{
			LOGGER.info("Nodes are NOT available for "+ bcgExportTestData.getExportNodeName());
			return false;
		}
	}
	/**
	 * This method will check the required nodes is sync or not
	 * @param fdn as String
	 * @return result as boolean
	 */
	private boolean isNodeSynchronized(String fdn){
		LOGGER.info("*isNodeSynchronized*");
		String csTestCommand = CSTESTLA + SPACE + fdn + SPACE + MIRRORMIBSYNCHSTATUS ;
		LOGGER.info("Checking Node Status : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		if(csOutput.contains("5") || csOutput.contains("3")){
			LOGGER.info(fdn + " Node is sync");
			return true;
		}else{
			LOGGER.info(fdn + " Node is Unsync");
			return false;
		}
	}

	/**
	 * This method will export for the node
	 * @return result as boolean
	 */
	public boolean BcgExport(BCGExportTestData bcgExportTestData){
		LOGGER.info("*BcgExport*");
		String bcgExportCommand ;
		String exportOutput = null;
		String exedate ;
		try{
			if(bcgExportTestData.getCompression() != null)
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName()+ SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgExportTestData.getCompression();
			else if(bcgExportTestData.getTimeValue() != null)
			{
				String AttributeChange = "sa" + SPACE + nodeList + SPACE + "latitude" + SPACE + "12345";
				LOGGER.info("value :" + AttributeChange);
				String AttributeCommand = helper.simpleExec(AttributeChange);
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE +  bcgExportTestData.getTimeValue();
			}
			else if((bcgExportTestData.getDateValue() != null))
			{
				String mydate = "date '+%Y-%m-%d" + SPACE + "%H:%M:%S'";
				String exe = helper.simpleExec(mydate);
				exe = exe.replace("[", "");
				exe = exe.replace("]", "");
				exedate = exe;
				String AttributeChange = "sa" + SPACE + nodeList + SPACE + "latitude" + SPACE + "12345";
				String AttributeCommand = helper.simpleExec(AttributeChange);
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList  + SPACE +  "-time date" + SPACE + exedate;
			}
			else if(bcgExportTestData.getDomain() != null)
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
			else
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName();
			LOGGER.info("BCG Export Command : "+bcgExportCommand);
			exportOutput = helper.simpleExec(bcgExportCommand);
			LOGGER.info("BCG Export : "+exportOutput);

		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
			// helper.disconnect();
		}

		if(exportOutput.contains("Export has succeeded"))
			return true;
		else
			return false;

	}

	/**
	 * This method will check the MO is there in export file or not
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean verifyMo(BCGExportTestData bcgExportTestData) {
		LOGGER.info("*verifyMo*");
		String grepCommand = "grep "+bcgExportTestData.getVerifyMo()+SPACE + EXPORTPATH +bcgExportTestData.getExportFileName();
		String moFound = helper.simpleExec(grepCommand);
		LOGGER.info("MO in Export File : "+moFound);
		// helper.disconnect();
		if(!moFound.contains(bcgExportTestData.getVerifyMo()))
			return false;
		return true;
	}

	/**
	 * This method will check the validation of the bcg export test case
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean validation(BCGExportTestData bcgExportTestData) {

		LOGGER.info("*Validation*");
		String bcgfilesizecommand = "";
		String exportoutputfilesize = "";
		int fileSize = 0;
		try{
			if(bcgExportTestData.getCompression() != null)
				bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgExportTestData.getExportFileName()+".gz | cut -c 0-1";
			else
				bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgExportTestData.getExportFileName()+ " | cut -c 0-1";
			LOGGER.info(bcgfilesizecommand);
			exportoutputfilesize = helper.simpleExec(bcgfilesizecommand);
			fileSize = Integer.parseInt(exportoutputfilesize.trim());
			LOGGER.info("BCG Export filesize : "+fileSize);
		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
			// helper.disconnect();
		}
		if(fileSize > 0){
			if(bcgExportTestData.getVerifyMo() != null)
				return verifyMo(bcgExportTestData);
			return true;
		}
		return false;
	
	}
}
