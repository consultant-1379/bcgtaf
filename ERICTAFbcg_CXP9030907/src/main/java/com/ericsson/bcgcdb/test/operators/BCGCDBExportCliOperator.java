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
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

import org.apache.log4j.Logger;

import com.ericsson.bcgcdb.test.cases.BCGCDBExportTestData;
import com.ericsson.bcgcdb.test.cases.BCGTestData;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;

@Operator(context = Context.CLI)
public class BCGCDBExportCliOperator implements BCGCDBExportOperator {

	Logger LOGGER = Logger.getLogger(BCGCDBExportCliOperator.class);

	BCGExportGetter bcgExportGetter;
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
	private final String BCG_EXPORTLOG_FILES_DIRECTORY = " /var/opt/ericsson/nms_umts_wran_bcg/logs/export/";
	private final String NECHECK = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt MeContext -f neType==";
	private final String CSLT = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt ";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String SPACE = " ";
	private final String MINUSD = "-d";
	private final String MINUSN = "-n";
	private final String BCGSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep wran_bcg ";
	private final String PCASMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep wran_pca ";
	private final String SEGMASTERCSSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep Seg_masterservice_CS ";
	private final String REGIONCSSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep Region_CS ";
	private final String SNADSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep cms_snad_reg ";
	private final String NEADSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep cms_nead_seg ";
	private final String MAFSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep MAF ";

	String bcgExportCommand ;
	String exportOutput = null;

	/**
	 * Initializing host, user and cli
	 */
	public BCGCDBExportCliOperator(){
		bcgExportGetter = new BCGExportGetter();
		host = HostGroup.getOssmaster();
	   // helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
	operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
		LOGGER.info("host.getIp()"+host.getIp());
	helper = new CLICommandHelper(host, operUser);
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgTestData
	 * @return result
	 */
	public boolean preAction(BCGCDBExportTestData bcgcdbExportTestData) {
		LOGGER.info("*preAction*");
		boolean isNodeAvailable = false;
		try{
			LOGGER.info("Checking server connectivity");
			String serverFiles = helper.simpleExec("cd /home/nmsadm/;ls");
			LOGGER.info("server files : "+serverFiles);
		}catch(Exception e){
			LOGGER.info("Exception while connecting to server " + host.getIp() + " "+ e);
		}
		finally{
	//		//helper.getShell().disconnect();
			helper.disconnect();
		}
		isNodeAvailable = findNode(bcgcdbExportTestData);
		if (isNodeAvailable) {
			if (!findMos(bcgcdbExportTestData)) {
				return false;
			}
		}
		else{
			LOGGER.info(bcgcdbExportTestData.getExportNodeName()  +  " Nodes are not available in the server ::");
			return false;
		}
		return true;
	}

	private boolean findNode(BCGCDBExportTestData bcgcdbExportTestData) {
		LOGGER.info("*find Nodes ::: *");
		boolean isNodeAvailable = false;
		String availableNodes = "";
		String checkNodeAvailability = "";
		
		availableNodes = NECHECK + bcgcdbExportTestData.getExportNodeType() + " | grep -i " + bcgcdbExportTestData.getExportNodeName();
		LOGGER.info("nodeAvailable :: " + availableNodes);
		checkNodeAvailability = helper.simpleExec(availableNodes);
		if( checkNodeAvailability.contains(bcgcdbExportTestData.getExportNodeName()) ){
			LOGGER.info("isNodeAvailable before:: "  +  isNodeAvailable);
			isNodeAvailable = true;
			LOGGER.info("isNodeAvailable after:: "  +  isNodeAvailable);
		}
		
		return isNodeAvailable;
	}

	/**
	 * This method will check the server for the required Mos
	 * @param importmo
	 * @param fdnpath
	 * @return result
	 */
	private boolean findMos(BCGCDBExportTestData bcgcdbExportTestData) {
		LOGGER.info("*findMos*");
		String allNodes = null;
		String getAllNodes = "";
		try{
			if(bcgcdbExportTestData.getDomain().contains(":vsdata")){
				String moName = bcgcdbExportTestData.getDomain();
				moName = bcgcdbExportTestData.getDomain().replace(":vsdata","");
				getAllNodes = CSLT+moName;
			}
			else
				getAllNodes = NECHECK+bcgcdbExportTestData.getExportNodeType();

			allNodes = helper.simpleExec(getAllNodes);
			LOGGER.info("List of available nodes: "+allNodes);
			if(!allNodes.isEmpty()){
				String[] fdns = allNodes.split("\n");
				if(fdns.length > 0){
					for(int i = 0 ; i < fdns.length; i++){
						nodeList = "";
						if(bcgcdbExportTestData.getExportNodeType().equalsIgnoreCase("4")){
							String[] fdn = fdns[i].split(",");
							if(bcgcdbExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
								if(fdn.length >=3){
									if(fdn[0].contains("SubNetwork") && fdn[1].contains("SubNetwork") && fdn[2].contains("MeContext") ){
										nodeList = fdn[0] + "," + fdn[1] + "," + fdn[2];
										if(isNodeSynchronized(nodeList))
//											nodeList = fdns[i];
										break;
									}else{
										if(fdn[0].contains("SubNetwork") && fdn[1].contains("MeContext") ){
											nodeList = fdn[0] + "," + fdn[1];
											if(isNodeSynchronized(nodeList))
												break;
										}
									}
								}
							}
						}else{
							String[] fdn = fdns[i].split(",");
							if(fdn[0].contains("SubNetwork") && fdn[1].contains("SubNetwork") && fdn[2].contains("MeContext") ){
								nodeList = fdn[0] + "," + fdn[1] + "," + fdn[2];
								if(isNodeSynchronized(nodeList))
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
	//		//helper.getShell().disconnect();
			helper.disconnect();
		}

		if(!nodeList.isEmpty()){
			LOGGER.info("Available node : "+nodeList);
			return true;
		}
		else{
			LOGGER.info("Nodes are NOT available for "+ bcgcdbExportTestData.getExportNodeName());
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
//		//helper.getShell().disconnect();
		helper.disconnect();
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
	public boolean BcgExport(BCGCDBExportTestData bcgcdbExportTestData){
		LOGGER.info("*BcgExport*");
		String exedate ;

		try{
			if(bcgcdbExportTestData.getCompression() != null){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgcdbExportTestData.getExportFileName()+ SPACE + MINUSD + SPACE + bcgcdbExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgcdbExportTestData.getCompression();
			}
			else if(bcgcdbExportTestData.getTimeValue() != null)
			{
				String AttributeChange = "sa" + SPACE + nodeList + SPACE + "latitude" + SPACE + "12345";
				LOGGER.info("value :" + AttributeChange);
				String AttributeCommand = helper.simpleExec(AttributeChange);
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgcdbExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgcdbExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE +  bcgcdbExportTestData.getTimeValue();
			}
			else if((bcgcdbExportTestData.getDateValue() != null))
			{
				String mydate = "date '+%Y-%m-%d" + SPACE + "%H:%M:%S'";
				String exe = helper.simpleExec(mydate);
				exe = exe.replace("[", "");
				exe = exe.replace("]", "");
				exedate = exe;
				String AttributeChange = "sa" + SPACE + nodeList + SPACE + "latitude" + SPACE + "12345";
				String AttributeCommand = helper.simpleExec(AttributeChange);
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgcdbExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgcdbExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList  + SPACE +  "-time date" + SPACE + exedate;
			}
			else if(bcgcdbExportTestData.getDomain() != null){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgcdbExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgcdbExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
			}
			else{
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgcdbExportTestData.getExportFileName();
			}
			if(bcgcdbExportTestData.getExportNodeName().equalsIgnoreCase("RNC")){
				Thread.sleep(100000);
				if(statusOfMCS()){
//					Thread.sleep(100000);
					LOGGER.info("BCG Export command: "+bcgExportCommand);
					exportOutput = helper.simpleExec(bcgExportCommand);
					LOGGER.debug(exportOutput);
				}
			}
			else{
				LOGGER.info("BCG Export command: "+bcgExportCommand);
				exportOutput = helper.simpleExec(bcgExportCommand);
				LOGGER.debug(exportOutput);
			}
		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
	//		//helper.getShell().disconnect();
			helper.disconnect();
		}

		if(exportOutput.contains("Export has succeeded")){
			return true;
		}
		else{
			if(bcgcdbExportTestData.getExportNodeName().equalsIgnoreCase("RNC")){
				try {
					Thread.sleep(100000);
					if(statusOfMCS()){
						LOGGER.info("BCG Export command 2: "+bcgExportCommand);
						exportOutput = helper.simpleExec(bcgExportCommand);
						LOGGER.debug(exportOutput);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					LOGGER.info(e);
				}
				if(exportOutput.contains("Export has succeeded")){
					return true;
				}
				else{
					if(bcgcdbExportTestData.getExportNodeName().equalsIgnoreCase("RNC")){
						try {
							Thread.sleep(100000);
							if(statusOfMCS()){
								LOGGER.info("BCG Export command 3: "+bcgExportCommand);
								exportOutput = helper.simpleExec(bcgExportCommand);
								LOGGER.debug(exportOutput);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
							LOGGER.info(e);
						}
						if(exportOutput.contains("Export has succeeded")){
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * This method will check the MO is there in export file or not
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean verifyMo(BCGCDBExportTestData bcgcdbExportTestData) {
		LOGGER.info("*verifyMo*");
		String grepCommand = "grep "+bcgcdbExportTestData.getVerifyMo()+SPACE + EXPORTPATH +bcgcdbExportTestData.getExportFileName();
		String moFound = helper.simpleExec(grepCommand);
		LOGGER.info("MO in Export File : "+moFound);
//		//helper.getShell().disconnect();
		helper.disconnect();
		if(!moFound.contains(bcgcdbExportTestData.getVerifyMo())){
			return false;
		}
		return true;
	}

	/**
	 * This method will check the validation of the bcg export test case
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean validation(BCGCDBExportTestData bcgcdbExportTestData) {
		LOGGER.info("*Validation*");
		String bcgfilesizecommand = "";
		String exportoutputfilesize = "";
		int fileSize = 0;
		int num1 = 0;
		int num = 0;
		String msg = "";
		try{
			if(bcgcdbExportTestData.getCompression() != null){
				bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgcdbExportTestData.getExportFileName()+".gz | cut -c 0-1";
			}
			else{
				bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgcdbExportTestData.getExportFileName()+ " | cut -c 0-1";
			}
			
			//LOGGER.info("Domain : "+bcgcdbExportTestData.getDomain());
			
				
			String error = "Error Info log location" ;
			helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/export_console.txt");
			String error_name = helper.simpleExec("grep -i " + "\"" + error +"\"" +" /var/opt/ericsson/nms_umts_wran_bcg/files/export/export_console.txt");
			String filename = bcgcdbExportTestData.getExportFileName();
			//String errName = filename.substring(0,filename.lastIndexOf('.'));
			String errName = "";
	        String[] a = error_name.split(" ");
			for(int j=0;j<a.length;j++){
				if(a[j].contains("/var/")){
					String ne = a[j];
					String[] x = ne.split("/");
							int i = x.length ;
							errName=x[i-1];
							LOGGER.info(errName);
				}
			}	
			String export = "were  successfully exported";
			String cmd = "grep -i " + "\"" + export+"\""+ BCG_EXPORTLOG_FILES_DIRECTORY +errName;
			LOGGER.info(cmd);
			if ((bcgcdbExportTestData.getCompression()!="-c")&&(bcgcdbExportTestData.getCompression()==null)){
			msg = helper.simpleExec(cmd);
			LOGGER.info(msg);
			if(msg.contains("MO(s) were  successfully exported from")){
				String[] y = msg.split(" ");
				for(int j=0;j<y.length;j++){
					if(y[j].contains("successfully")){
						//LOGGER.info("y.length : "+ y.length);
						num = Integer.parseInt(y[j+3].trim());
					}
				}
				LOGGER.info("No of Nodes: " +num);
			}
			if(msg.contains("node(s) were  successfully exported")){
				String[] y1 = msg.split(" ");
				for(int j=0;j<y1.length;j++){
					if(y1[j].contains("successfully")){
						//LOGGER.info("y.length : "+ y1.length);
						num = Integer.parseInt(y1[j-6].trim());
					}
				}
				LOGGER.info("No of Nodes: " +num);
			}
			
			String export2 = ":MeContext id" ;
			String export3 = ":ManagementNode id" ;
			String cmd1 = "grep -i "+ "\""+export2+"\"" +" /var/opt/ericsson/nms_umts_wran_bcg/files/export/"+bcgcdbExportTestData.getExportFileName()+" | wc -l";
			LOGGER.info(cmd1);
			String cmd2 = "grep -i "+ "\""+export3+"\"" +" /var/opt/ericsson/nms_umts_wran_bcg/files/export/"+bcgcdbExportTestData.getExportFileName()+" | wc -l";
			LOGGER.info(cmd2);
			String msg1 = helper.simpleExec(cmd1);
			String msg2 = helper.simpleExec(cmd2);
			int n = 0;
			int m = 0;
			n = Integer.parseInt(msg1.trim());
			m = Integer.parseInt(msg2.trim());
			LOGGER.info(bcgcdbExportTestData.getExportNodeName());
			num1 = n+m;
			/*if(bcgcdbExportTestData.getExportNodeName().equalsIgnoreCase("SubNetwork")){
			num1 = n + 1 ;
			}
			else {
				num1 = n;
			}*/
			LOGGER.info("No of Nodes in xml: "+ num1);
			}
			LOGGER.info(bcgfilesizecommand);
			exportoutputfilesize = helper.simpleExec(bcgfilesizecommand);
			fileSize = Integer.parseInt(exportoutputfilesize.trim());
			LOGGER.info("BCG Export filesize : "+fileSize);
		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
	//		//helper.getShell().disconnect();
			helper.disconnect();
		}
		if(fileSize > 0){
			if(!(msg.contains("can't open"))){
			if(num != num1){
				LOGGER.info("No of nodes are not Equal");
				return false;
			}
			}
			if(bcgcdbExportTestData.getVerifyMo() != null){
				return verifyMo(bcgcdbExportTestData);
			}
			return true;
		}
		return false;
	}
	
	public boolean statusOfMCS(){
		boolean allMCSAreStarted = false;
		try{
			while(!(helper.simpleExec(MAFSMTOOL)).contains("started")){
				helper.disconnect();
				allMCSAreStarted = true;
			}
			while(!(helper.simpleExec(BCGSMTOOL)).contains("started")){
				helper.disconnect();
				allMCSAreStarted = true;
			}
			while(!(helper.simpleExec(PCASMTOOL)).contains("started")){
				helper.disconnect();
				allMCSAreStarted = true;
			}
			while(!(helper.simpleExec(SEGMASTERCSSMTOOL)).contains("started")){
				helper.disconnect();
				allMCSAreStarted = true;
			}
			while(!(helper.simpleExec(REGIONCSSMTOOL)).contains("started")){
				helper.disconnect();
				allMCSAreStarted = true;
			}
			while(!(helper.simpleExec(SNADSMTOOL)).contains("started")){
				helper.disconnect();
				allMCSAreStarted = true;
			}
			while(!(helper.simpleExec(NEADSMTOOL)).contains("started")){
				helper.disconnect();
				allMCSAreStarted = true;
			}
			LOGGER.info("MCs are online");
			allMCSAreStarted = true;
		}
		catch(Exception mcRestart){
			LOGGER.info(mcRestart.getMessage());
			LOGGER.info(mcRestart);
			allMCSAreStarted = false;
		}
		finally{
	//		//helper.getShell().disconnect();
			helper.disconnect();
		}
		return allMCSAreStarted;
		
	}
	
}
