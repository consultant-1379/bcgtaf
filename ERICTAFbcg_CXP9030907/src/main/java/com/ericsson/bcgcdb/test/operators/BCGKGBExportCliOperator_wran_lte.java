package com.ericsson.bcgcdb.test.operators;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.handlers.implementation.SshRemoteCommandExecutor;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

import org.apache.log4j.Logger;
import com.ericsson.bcgcdb.test.cases.BCGKGBExportData_wran_lte;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;


@Operator(context = Context.CLI)
public class BCGKGBExportCliOperator_wran_lte implements BCGKGBExportOperator_wran_lte {

	BCGExportGetter bCGexportGetter;
	Logger LOGGER;
	CLICommandHelper helper;

	String autoLockUnlock;
	String nodeList;
	String bachupAutoLockUnlock;

	Shell shell;
	Host host;
	User operUser;

	SshRemoteCommandExecutor SSH;
	StringBuffer stringBuffer;

	private final String CSTESTLT = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt ";
	private final String CSTESTLA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice la ";
	private final String EXPORTPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	private final String BCGTOOLEXPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -e ";
	private final String PERIODICEXPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/start_rah_export.sh";
	private final String FILESIZE_GREATER_THAN_ZERO_COMMAND = "du -sk ";
	private final String BCG_EXPORT_FILES_DIRECTORY = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	private final String BCG_INSTRUMENTATION_RATE_GREP_STRING = " 'MO/Sec for Export =' ";
	private final String BCG_EXPORT_LOGS_DIRECTORY = "/var/opt/ericsson/nms_umts_wran_bcg/logs/export/";
	private final String lslrt = "ls -lrt";
	private final String SPACE = " ";
	private final String MINUSD = "-d";
	private final String MINUSN = "-n";
	private final String NECHECK = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt MeContext -f neType==";
	private final String FILECHECK = "test -e /var/opt/ericsson/nms_umts_wran_bcg/logs/export/errInfoLog_UTRAN_TOPOLOGY*.tmp && echo \"file exists\"";
	private final String CHECKSUCCESS = "grep \'Successfully completed PERIODIC_EXPORT\' /var/opt/ericsson/nms_umts_wran_bcg/logs/export/errInfoLog_UTRAN_TOPOLOGY* | tail -1";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String CUSTOMFILTER = "/opt/ericsson/nms_umts_bcg_meta/dat/customfilters/";
	private final String MECONTEXTID = "MeContextId";
	private final String SUBNETWORK = "SubNetwork";




	/**
	 * Initializing host, user and cli
	 */
	public BCGKGBExportCliOperator_wran_lte(){
		bCGexportGetter = new BCGExportGetter();
		host = HostGroup.getOssmaster();
	 //   helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
	helper = new CLICommandHelper(host, operUser);
//		SSH = new SshRemoteCommandExecutor(host);
		LOGGER = Logger.getLogger(BCGKGBExportCliOperator_wran_lte.class);
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgTestData
	 * @return result
	 */
	public boolean preAction(BCGKGBExportData_wran_lte bcgExportTestData_wran_lte) {
		System.out.println("*****preAction*****");
		if(!findMos(bcgExportTestData_wran_lte))
			return false;
		return true;
	}

	/**
	 * This method will check the server for the required Mos
	 * @param importmo
	 * @param fdnpath
	 * @return result
	 * 
	 */
	private boolean findMos(BCGKGBExportData_wran_lte bcgkgbExportData_wran_lte) {
		System.out.println("*****findMos*****");
		String allNodes = null;
		if(bcgkgbExportData_wran_lte.getExportNodename().equalsIgnoreCase("SubNetwork") || bcgkgbExportData_wran_lte.getExportNodename().equalsIgnoreCase("periodic export")){
			String getSubnetwork = CSTESTLT + SUBNETWORK;
			allNodes = helper.simpleExec(getSubnetwork);
			if(!allNodes.isEmpty()){
				String[] fdns = allNodes.split("\n");
				if(fdns.length > 0)
					nodeList = fdns[0];
			}
		}else{
			String getAllNodes = NECHECK+bcgkgbExportData_wran_lte.getExportNodeType();
			allNodes = helper.simpleExec(getAllNodes);
			LOGGER.info("List of Nodes: " + allNodes);
			if(!allNodes.isEmpty()){
				String[] fdns = allNodes.split("\n");
				if(fdns.length > 0){
					for(int i = 0 ; i < fdns.length; i++){
						if(bcgkgbExportData_wran_lte.getExportNodeType().equalsIgnoreCase("4")){
							String[] fdn = fdns[i].split(",");
							if(bcgkgbExportData_wran_lte.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
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
						}else if(bcgkgbExportData_wran_lte.getExportNodeType().equalsIgnoreCase("15")){
							String getStn = CSTESTLA +SPACE + fdns[i] + MECONTEXTID;
							allNodes = helper.simpleExec(getStn);
							if(bcgkgbExportData_wran_lte.getExportNodename().equalsIgnoreCase("SIU")){
								if(allNodes.contains("SIU") && isNodeSynchronized(fdns[i])){
									nodeList = fdns[i];
									break;
								}
							}if(bcgkgbExportData_wran_lte.getExportNodename().equalsIgnoreCase("TCU")){
								if(allNodes.contains("TCU") && isNodeSynchronized(fdns[i])){
									nodeList = fdns[i];
									break;
								}
							}
						}
						else{
							if(isNodeSynchronized(fdns[i])){
								nodeList = fdns[i];
								break;
							}
						}
					}
				}
			}
		}
		System.out.println("nodeList : "+nodeList);
		if(!nodeList.isEmpty())
			return true;
		return false;

	}
	/**
	 * This method will check the required nodes is sync or not
	 * @param fdn as String
	 * @return result as boolean
	 */
	private boolean isNodeSynchronized(String fdn){
		System.out.println("******isNodeSynchronized******");
		boolean result = false;
		String csTestCommand = CSTESTLA + SPACE + fdn + SPACE + MIRRORMIBSYNCHSTATUS ;
		LOGGER.info("CHecking Node Status : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		if(csOutput.contains("5") || csOutput.contains("3"))
			result = true;
		
		if(!result)
			System.out.println(fdn+" Node Is unsync");
		return result;
	}

	/**
	 * This method will export for the node
	 * @return result as boolean
	 * @throws InterruptedException 
	 */
	public boolean BcgExport(BCGKGBExportData_wran_lte bcgkgbExportData_wran_lte){
		System.out.println("*****BCGExport*****");
		String bcgExportCommand = "";
		String exportOutput;
		String exedate ;
		if(bcgkgbExportData_wran_lte.getExportNodename().equalsIgnoreCase("periodic export")){
			if(bcgkgbExportData_wran_lte.getDomain() == null)
				bcgExportCommand =  PERIODICEXPORT;
			else
				bcgExportCommand =  PERIODICEXPORT + SPACE + bcgkgbExportData_wran_lte.getDomain() + SPACE + bcgkgbExportData_wran_lte.getExportFileName() ;
		}
		else if(bcgkgbExportData_wran_lte.getCompression() != null)
		{
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgkgbExportData_wran_lte.getExportFileName()+ SPACE + MINUSD + SPACE + bcgkgbExportData_wran_lte.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgkgbExportData_wran_lte.getCompression();
		}
		else if((bcgkgbExportData_wran_lte.getDateValue() != null))
		{
			String mydate = "date '+%Y-%m-%d" + SPACE + "%H:%M:%S'";
			String exe = helper.simpleExec(mydate);
			exe = exe.replace("[", "");
			exe = exe.replace("]", "");
			exedate = exe;
			System.out.println("exedate :" + exedate);
			String AttributeChange = "sa" + SPACE + nodeList + SPACE + "latitude" + SPACE + "12345";
			System.out.println("value :" + AttributeChange);
			String AttributeCommand = helper.simpleExec(AttributeChange);
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgkgbExportData_wran_lte.getExportFileName() + SPACE + MINUSD + SPACE + bcgkgbExportData_wran_lte.getDomain() + SPACE + MINUSN + SPACE + nodeList  + SPACE +  "-time date" + SPACE + exedate;
		}
		else if(bcgkgbExportData_wran_lte.getTimeValue() != null)
		{
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgkgbExportData_wran_lte.getExportFileName() + SPACE + MINUSD + SPACE + bcgkgbExportData_wran_lte.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgkgbExportData_wran_lte.getTimeValue();
		}
		else if(bcgkgbExportData_wran_lte.getDomain() != null)
		{
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgkgbExportData_wran_lte.getExportFileName() + SPACE + MINUSD + SPACE + bcgkgbExportData_wran_lte.getDomain() + SPACE + MINUSN + SPACE + nodeList;
		}
		
		else
		{
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgkgbExportData_wran_lte.getExportFileName() + SPACE + MINUSD + SPACE + bcgkgbExportData_wran_lte.getDomain();
		}
		System.out.println(bcgExportCommand);
		LOGGER.info("BCG Export command: "+bcgExportCommand);
		exportOutput = helper.simpleExec(bcgExportCommand);
		System.out.println("exportOutput : "+exportOutput);
		if(exportOutput.contains("Export has succeeded") || exportOutput.contains("periodic export started successfully"))
			return true;
		else
			return false;

	}

	/**
	 * This method will check the MO is there in export file or not
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean verifyMo(BCGKGBExportData_wran_lte bcgkgbExportData_wran_lte) {
		System.out.println("*****verifyMo*****");
		String grepCommand = "grep "+bcgkgbExportData_wran_lte.getVerifyMo()+SPACE + EXPORTPATH +bcgkgbExportData_wran_lte.getExportFileName();
		System.out.println("grepCommand : "+grepCommand);
		String verifyOutput = helper.simpleExec(grepCommand);
		if(!verifyOutput.contains(bcgkgbExportData_wran_lte.getVerifyMo()))
			return false;
		return true;
	}

	/**
	 * This method will check the validation of the bcg export test case
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean validation(BCGKGBExportData_wran_lte bcgkgbExportData_wran_lte) {
		System.out.println("*****validation*****");
		int fileSize = 0;
		String bcgfilesizecommand = null;
		String exportoutputfilesize;

		if(bcgkgbExportData_wran_lte.getExportNodename().contains("periodic")){
			boolean flag = true;
			while(flag){
				String checkFile = helper.simpleExec(FILECHECK);
				if (!checkFile.contains("file exists"))
					break;
			}
			String checkSuccess = helper.simpleExec(CHECKSUCCESS);
			if(checkSuccess.contains("Successfully completed PERIODIC_EXPORT")){
				if(bcgkgbExportData_wran_lte.getCompression() != null)
					bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + "UTRAN_TOPOLOGY.xml.gz | cut -c 0-1";
				else
					bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + "UTRAN_TOPOLOGY.xml | cut -c 0-1";
			}

		}else{
			if(bcgkgbExportData_wran_lte.getCompression() != null)
				bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgkgbExportData_wran_lte.getExportFileName()+".gz | cut -c 0-1";
			else
				bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgkgbExportData_wran_lte.getExportFileName()+ " | cut -c 0-1";
		}
		System.out.println("bcgfilesizecommand : "+bcgfilesizecommand);
		exportoutputfilesize = helper.simpleExec(bcgfilesizecommand);
		System.out.println("exportoutputfilesize "+exportoutputfilesize);
		fileSize = Integer.parseInt(exportoutputfilesize.trim());
		LOGGER.info("BCG Export filesize command: "+exportoutputfilesize);

		if(fileSize > 0){
			if(bcgkgbExportData_wran_lte.getVerifyMo() != null)
				return verifyMo(bcgkgbExportData_wran_lte);
			return true;
		}
		return false;
	}
}

