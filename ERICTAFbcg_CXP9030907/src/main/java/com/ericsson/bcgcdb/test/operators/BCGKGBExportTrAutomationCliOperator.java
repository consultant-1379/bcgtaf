package com.ericsson.bcgcdb.test.operators;

import org.apache.log4j.Logger;

import com.ericsson.bcgcdb.test.cases.BCGKGBExportData;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

@Operator(context = Context.CLI)
public class BCGKGBExportTrAutomationCliOperator implements BCGKGBTRExportOperator{
	Logger LOGGER = Logger.getLogger(BCGKGBExportCliOperator.class);
	BCGExportGetter bcgEexportGetter;
	CLICommandHelper helper;
	String autoLockUnlock;
	String nodeList;
	String bachupAutoLockUnlock;
	Shell shell;
	Host host;
	User operUser;
	User rootUser;
	StringBuffer stringBuffer;
	
	
	private final String CSTESTLT = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt ";
	private final String CSTESTLA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice la ";
	private final String EXPORTPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	private final String BCGTOOLEXPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -e";
	private final String FILESIZE_GREATER_THAN_ZERO_COMMAND = "du -sk ";
	private final String BCG_EXPORT_FILES_DIRECTORY = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	private final String BCG_EXPORTLOG_FILES_DIRECTORY = " /var/opt/ericsson/nms_umts_wran_bcg/logs/export/";
	private final String SPACE = " ";
	private final String MINUSD = "-d";
	private final String MINUSN = "-n";
	private final String NECHECK = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt MeContext -f neType==";
	private final String FILECHECK = "test -e /var/opt/ericsson/nms_umts_wran_bcg/logs/export/errInfoLog_UTRAN_TOPOLOGY*.tmp && echo \"file exists\"";
	private final String CHECKSUCCESS = "grep \'Successfully completed PERIODIC_EXPORT\' /var/opt/ericsson/nms_umts_wran_bcg/logs/export/errInfoLog_UTRAN_TOPOLOGY* | tail -1";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String SUBNETWORK = "SubNetwork";
	private final String EGREP = "egrep";
	String exportOutputboth = "";
	String exportOutputBCRBCT = "";
	String exportOutputTemp = null;
	/**
	 * Initializing host, user and cli
	 */
	public BCGKGBExportTrAutomationCliOperator(){
		bcgEexportGetter = new BCGExportGetter();
		host = HostGroup.getOssmaster();
	  //  helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
		rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
		helper = new CLICommandHelper(host, operUser);
	}
	
	@Override
	public boolean preAction(BCGKGBExportData bcgExportTestData) {
		LOGGER.info("*preAction*");
		if(!findMos(bcgExportTestData)){
			return false;
		}
		return true;
	}

	private boolean findMos(BCGKGBExportData bcgExportTestData) {
		LOGGER.info("*findMos*");
		String allNodes = null;
		String getSubnetwork = null;
		try{
			if(bcgExportTestData.getExportNodename().equalsIgnoreCase("SubNetwork")){
				LOGGER.info("Inside SubNetwork");
				try{
				if( bcgExportTestData.getVerifyMo() != null && bcgExportTestData.getVerifyMo().contains("RNC|ERB")){
					LOGGER.info("Inside RNC|ERB");
					getSubnetwork = CSTESTLT + SUBNETWORK + '|' + EGREP + SPACE + bcgExportTestData.getVerifyMo().trim();
					LOGGER.info("getSubnetwork :::"+getSubnetwork);
					allNodes = helper.simpleExec(getSubnetwork);
					LOGGER.info("List of available nodes : "+allNodes);
					if(!allNodes.isEmpty()){
						String[] fdns = allNodes.split("\n");
						if(fdns.length > 0){
							nodeList = fdns[0].trim() + SPACE + fdns[fdns.length-1];
						}
					}
				}else{
					LOGGER.info("Inside esle block ok");
					getSubnetwork = CSTESTLT + SUBNETWORK;
					allNodes = helper.simpleExec(getSubnetwork);
					LOGGER.info("List of available nodes : "+allNodes);
					if(!allNodes.isEmpty()){
						String[] fdns = allNodes.split("\n");
						if(fdns.length > 0){
							nodeList = fdns[0].trim();
						}
					}
				}
			}catch(Exception e){
			 	
			}
				/*allNodes = helper.simpleExec(getSubnetwork);
				LOGGER.info("List of available nodes : "+allNodes);*/
				/*if(!allNodes.isEmpty()){
					String[] fdns = allNodes.split("\n");
					if(fdns.length > 0){
						nodeList = fdns[0].trim();
					}
				}*/
			}
			else{
				String getAllNodes = NECHECK+bcgExportTestData.getExportNodeType();
				allNodes = helper.simpleExec(getAllNodes);
				LOGGER.info("List of available nodes : "+allNodes);
				if(!allNodes.isEmpty()){
					String[] fdns = allNodes.split("\n");
					if(fdns.length > 0){
						for(int i = 0 ; i < fdns.length; i++){
							if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("4")){
								String[] fdn = fdns[i].split(",");
								if(bcgExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
									if(fdn.length >=2){
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
							}else if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("2")){
								String[] fdn = fdns[i].split(",");
								if(bcgExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
									if(fdn.length >=3){
										if(fdn[0].contains("SubNetwork") && fdn[1].contains("SubNetwork") && fdn[2].contains("MeContext") ){
											if(isNodeSynchronized(fdns[i])){
												if(bcgExportTestData.getExportNodename().equalsIgnoreCase("RNC")){
												String nodeListfdn = fdn[0] +"," + fdn[1] +" "+fdn[0]+","+fdn[1]+","+fdn[2];
												nodeList = nodeListfdn;
												break;
												}
												else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("RNCRBS")){
													String nodeListfdn = fdns[0]+" "+fdns[1]+" "+fdns[2];
													nodeList = nodeListfdn;
												}
											}
										}	
								}
							}
						}else if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("30")){
							LOGGER.info("Inside 30");
							String[] fdn = fdns[i].split(",");
							if(bcgExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
								//if(fdn.length >=3){
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
								//}
							}
						}else if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("46")){
							LOGGER.info("Inside 46");
							String[] fdn = fdns[i].split(",");
							if(bcgExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
								//if(fdn.length >=3){
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
								//}
							}
						}else if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("26")){
							LOGGER.info("Inside 26");
							String[] fdn = fdns[i].split(",");
							if(bcgExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
								//if(fdn.length >=3){
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
								//}
							}
						}else if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("15")){
							LOGGER.info("Inside 15");
							String[] fdn = fdns[i].split(",");
							if(bcgExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
								//if(fdn.length >=3){
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
								//}
							}
						}
							else{
							}
						}
					}	
				}
			}
		}catch(Exception e ){
			LOGGER.info("Caught Exception while finding the fdn's "+e.getMessage()+"\n"+e);
		}finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
		try{
			if(!nodeList.isEmpty()){
				LOGGER.info("Available node : "+nodeList);
				return true;
			}
			else{
				LOGGER.info("Nodes are NOT available for "+ bcgExportTestData.getExportNodename());
				return false;
			}
		}
		catch(Exception n){
			LOGGER.info(n.getMessage());
			LOGGER.info(n);
			LOGGER.info("Nodes are NOT available for "+ bcgExportTestData.getExportNodename());
			return false;
		}	
	}
	
	
	private boolean isNodeSynchronized(String fdn){
		LOGGER.info("*isNodeSynchronized*");
		String csTestCommand = CSTESTLA + SPACE + fdn + SPACE + MIRRORMIBSYNCHSTATUS ;
		LOGGER.info("Checking Node Status : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		//helper.getShell().disconnect();
		helper.disconnect();
		if(csOutput.contains("5") || csOutput.contains("3")){
			LOGGER.info(fdn + " Node is sync");
			LOGGER.info("----------------------------");
			return true;
		}else{
			LOGGER.info(fdn + " Node is Unsync");
			LOGGER.info("++++++++++++++++++++++++++++");
			return false;
		}
	}

	@Override
	public boolean BcgExport(BCGKGBExportData bcgExportTestData) {
		LOGGER.info("*BcgExport*");
		String bcgExportCommand = "";
		String exportOutput = null;
		try{
			 if(bcgExportTestData.getExportNodename().equalsIgnoreCase("SubNetwork")){
				 if(bcgExportTestData.getTimeValue() != null){
					 if(bcgExportTestData.getCompression() != null){
						 bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgExportTestData.getTimeValue() + SPACE + bcgExportTestData.getCompression();
						 LOGGER.info("BCG Export command: "+bcgExportCommand);
						 exportOutput = helper.simpleExec(bcgExportCommand);
					 }
					 else{
						 bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgExportTestData.getTimeValue();
						 LOGGER.info("BCG Export command: "+bcgExportCommand);
						 exportOutput = helper.simpleExec(bcgExportCommand);
					 }
				} else if(bcgExportTestData.getVerifyMo().contains("RNC|ERB")){
					LOGGER.info("Inside RNC|ERB bcgexport");
					 bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
					 LOGGER.info("BCG Export command: "+bcgExportCommand);
					 exportOutput = helper.simpleExec(bcgExportCommand);
				 }
				 else{
					 bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain();
					 LOGGER.info("BCG Export command: "+bcgExportCommand);
					 exportOutput = helper.simpleExec(bcgExportCommand);
				 }
			}
			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("ERBS")){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
				LOGGER.info("BCG Export command: "+bcgExportCommand);
				exportOutput = helper.simpleExec(bcgExportCommand);
			}
			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("RNC")){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
				LOGGER.info("BCG Export command: "+bcgExportCommand);
				exportOutput = helper.simpleExec(bcgExportCommand);
			}
			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("RNCRBS")){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
				LOGGER.info("BCG Export command: "+bcgExportCommand);
				exportOutput = helper.simpleExec(bcgExportCommand);
			}
			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("WCG")){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
				LOGGER.info("BCG Export command: "+bcgExportCommand);
				exportOutput = helper.simpleExec(bcgExportCommand);
			}
			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("TCU")){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
				LOGGER.info("BCG Export command: "+bcgExportCommand);
				exportOutput = helper.simpleExec(bcgExportCommand);
			}
			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("RNCPRBS")){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
				LOGGER.info("BCG Export command: "+bcgExportCommand);
				exportOutput = helper.simpleExec(bcgExportCommand);
			}
			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("STN")){
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
				LOGGER.info("BCG Export command: "+bcgExportCommand);
				exportOutput = helper.simpleExec(bcgExportCommand);
				exportOutputTemp = exportOutput;
			}
			else{
				
			}
			/*LOGGER.info("BCG Export command: "+bcgExportCommand);
			exportOutput = helper.simpleExec(bcgExportCommand);
			exportOutputTemp = exportOutput;*/
			 helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/export_console.txt");
			LOGGER.info("BCG export output : "+exportOutput);
			
		}catch(Exception e){
			LOGGER.info("Caught Exception While exporting the TestCase"+e.getMessage()+"\n"+e);
			LOGGER.info(e);
		}finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
		if(exportOutput.contains("Export has succeeded") || exportOutput.contains("export started successfully")){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public boolean validation(BCGKGBExportData bcgExportTestData) {
		LOGGER.info("*validation*");
		int fileSize = 0;
		String bcgfilesizecommand = null;
		String exportoutputfilesize;
		boolean exitFlag = false;
		boolean verifyMoFlag = false;
		int num1 = 0;
		int num = 0;
		String msg = "" ;
		try{
			if(bcgExportTestData.getExportNodename().contains("periodic")){
				boolean flag = true;
				while(flag){
					String checkFile = helper.simpleExec(FILECHECK);
					if (!checkFile.contains("file exists")){
						break;
					}
					////helper.getShell().disconnect();
					helper.disconnect();
				}
				String checkSuccess = helper.simpleExec(CHECKSUCCESS);
				if(checkSuccess.contains("Successfully completed PERIODIC_EXPORT")){
					LOGGER.info("Successfully completed PERIODIC_EXPORT \n");
					if(bcgExportTestData.getCompression() != null){
						bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + "UTRAN_TOPOLOGY.xml.gz | cut -c 0-1";
					}
					else{
						bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + "UTRAN_TOPOLOGY.xml | cut -c 0-1";
					}
				}
			}else{
				if(bcgExportTestData.getCompression() != null){
					bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgExportTestData.getExportFileName()+".gz | cut -c 0-1";
				}
				else{
					bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgExportTestData.getExportFileName()+ " | cut -c 0-1";
				}
			}
			//LOGGER.info("Domain : "+bcgExportTestData.getDomain());
			
			String error = "Error Info log location" ;
			String filename = bcgExportTestData.getExportFileName();
			//String errName = filename.substring(0,filename.lastIndexOf('.'));
			String error_name = helper.simpleExec("grep -i " + "\"" + error +"\"" +" /var/opt/ericsson/nms_umts_wran_bcg/files/export/export_console.txt");
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
			if(bcgExportTestData.getExportNodename().contains("periodic")){
				errName = "errInfoLog_UTRAN_TOPOLOGY" ;
			}
			String export = "were  successfully exported";
			String cmd = "grep -i " + "\"" + export+"\""+ BCG_EXPORTLOG_FILES_DIRECTORY +errName;
			LOGGER.info(cmd);
			if ((bcgExportTestData.getCompression()!="-c")&&(bcgExportTestData.getCompression()==null)){
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
			String cmd1 = "grep -i "+ "\""+export2+"\"" +" /var/opt/ericsson/nms_umts_wran_bcg/files/export/"+bcgExportTestData.getExportFileName()+" | wc -l";
			LOGGER.info(cmd1);
			String cmd2 = "grep -i "+ "\""+export3+"\"" +" /var/opt/ericsson/nms_umts_wran_bcg/files/export/"+bcgExportTestData.getExportFileName()+" | wc -l";
			LOGGER.info(cmd2);
			String msg1 = helper.simpleExec(cmd1);
			String msg2 = helper.simpleExec(cmd2);
			int n = 0;
			int m = 0 ;
			n = Integer.parseInt(msg1.trim());
			m = Integer.parseInt(msg2.trim());
			if((m==0)&&(bcgExportTestData.getExportNodename().equalsIgnoreCase("SubNetwork"))){
				m=m+1;
			} 
			LOGGER.info(bcgExportTestData.getExportNodename());
			num1 = n+m;
			/*if(bcgExportTestData.getExportNodename().equalsIgnoreCase("SubNetwork")){
			num1 = n + 1 ;
			}
			else {
				num1 = n;
			}*/
			LOGGER.info("No of Nodes in xml: "+ num1);
			}
			LOGGER.info("BCG file size command : "+bcgfilesizecommand);
			exportoutputfilesize = helper.simpleExec(bcgfilesizecommand);
			fileSize = Integer.parseInt(exportoutputfilesize.trim());
			LOGGER.info("BCG Export filesize : "+fileSize);
			
		}catch(Exception e){
			LOGGER.info("Caught Exception while validating exported TestCase"+e.getMessage()+"\n"+e);
		}finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
		exitFlag = verifyExitCode();
		if(fileSize > 0){
			//if(exitFlag){
			if(!(msg.contains("can't open"))){
			if(num != num1){
				LOGGER.info("No of nodes are not Equal");
				return false;
			}
			}
				if(bcgExportTestData.getVerifyMo() != null){
					verifyMoFlag = verifyMo(bcgExportTestData);
					if(verifyMoFlag){
						LOGGER.info("Export has finished successfully.");
						return true;
					}
				}
				else if(exitFlag){
					LOGGER.info("Export has finished successfully");
					return true;
				}
				//LOGGER.info("Export has finished successfully");
				//return true;
			//}
			/*if(bcgExportTestData.getVerifyMo() != null){
				return verifyMo(bcgExportTestData);
			}*/
		}
		return false;
	}
	boolean verifyExitCode() {
		int exitCode =helper.getCommandExitValue();
		LOGGER.info("Exit code of export command is exitCode");
		return exitCode == 0;
	}
	public boolean verifyMo(BCGKGBExportData bcgExportTestData) {
		String grepCommand = null;
		String moCount = null;
		LOGGER.info("*verifyMo*");
		//String grepCommand = "grep "+bcgExportTestData.getVerifyMo()+SPACE + EXPORTPATH +bcgExportTestData.getExportFileName();
		try{
			if(bcgExportTestData.getVerifyMo().contains("SgsnFunction")){
				grepCommand = "grep "+bcgExportTestData.getVerifyMo()+SPACE + EXPORTPATH +"standardBCRBCT.xml";
				LOGGER.info("grepCommand is "+grepCommand);
				String moFound = helper.simpleExec(grepCommand);
				LOGGER.info("MO in Export File : "+moFound);
				if(!moFound.contains(bcgExportTestData.getVerifyMo())){
					return false;
				}
			}
			else if(bcgExportTestData.getVerifyMo().contains("ExternalGSMcell")){
				grepCommand = "grep "+bcgExportTestData.getVerifyMo()+SPACE + EXPORTPATH +"FullBoth.xml";
				LOGGER.info("grepCommand is "+grepCommand);
				String moFound = helper.simpleExec(grepCommand);
				LOGGER.info("MO in Export File : "+moFound);
				if(!moFound.contains(bcgExportTestData.getVerifyMo())){
					return false;
				}
			}
			else if(bcgExportTestData.getVerifyMo().contains("RNC|ERB")){
				grepCommand = "grep "+"RNC"+SPACE + EXPORTPATH + bcgExportTestData.getExportFileName();
				LOGGER.info("grepCommand is "+grepCommand);
				String moFound = helper.simpleExec(grepCommand);
				LOGGER.info("MO in Export File : "+moFound);
				if(!moFound.contains("RNC")){
					return false;
				}
			}
			else if(bcgExportTestData.getVerifyMo().contains("vsDataSTN")){
				LOGGER.info("Inside vsDataSTN");
				helper.simpleExec("echo " + "\"" + exportOutputTemp +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/stnmoCount.txt");
				moCount = helper.simpleExec("grep " + "'The export operation has succeeded ' " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/stnmoCount.txt" + "| " + "awk '{print $10}'");
				LOGGER.info("No. of Mo's Exported "+moCount);
				if(moCount == null){
					return false;
				}
			}
			else if(bcgExportTestData.getVerifyMo().contains("radioMocount")){
				String MoCountradio = helper.simpleExec("grep " + "'The export operation has succeeded ' " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/radioMoCount.txt" + "| " + "awk '{print $10}'");
				LOGGER.info("No. of Mo's for Radio export is "+MoCountradio);
				
				String MoCountBCR = helper.simpleExec("grep " + "'The export operation has succeeded ' " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/bcrMoCount.txt" + "| " + "awk '{print $10}'");
				LOGGER.info("No. of Mo's for advanced BCR export is "+MoCountBCR);
				
				if(!MoCountradio.equals(MoCountBCR)){
					return false;
				}
				
			}
			else if(bcgExportTestData.getVerifyMo().contains("transportMocount")){
				String MoCounttransport = helper.simpleExec("grep " + "'The export operation has succeeded ' " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/trnsportMoCount.txt" + "| " + "awk '{print $10}'");
				LOGGER.info("No. of Mo's for transport export is "+MoCounttransport);
				
				String MoCountBCT = helper.simpleExec("grep " + "'The export operation has succeeded ' " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/bctMoCount.txt" + "| " + "awk '{print $10}'");
				LOGGER.info("No. of Mo's for BCT export is "+MoCountBCT);
				
				if(!MoCounttransport.equals(MoCountBCT)){
					return false;
				}

			}
			else if(bcgExportTestData.getVerifyMo().contains("bothMocount")){
				String MoCountboth = helper.simpleExec("grep " + "'The export operation has succeeded ' " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/bothMoCount.txt" + "| " + "awk '{print $10}'");
				LOGGER.info("No. of Mo's for both export is "+MoCountboth);
				
				String MoCountBCRBCTExport = helper.simpleExec("grep " + "'The export operation has succeeded ' " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/bcrbctMoCount.txt" + "| " + "awk '{print $10}'");
				LOGGER.info("No. of Mo's for BCR_BCT export is "+MoCountBCRBCTExport);
				
				if(!MoCountboth.equals(MoCountBCRBCTExport)){
					return false;
				}	
			}else if(bcgExportTestData.getVerifyMo().contains("vsDataUtranCell")){
				grepCommand = "grep "+bcgExportTestData.getVerifyMo()+SPACE + EXPORTPATH +bcgExportTestData.getExportFileName();
				LOGGER.info("grepCommand is "+grepCommand);
				String moFound = helper.simpleExec(grepCommand);
				LOGGER.info("MO in Export File : "+moFound);
				if(!moFound.contains(bcgExportTestData.getVerifyMo())){
					return false;
				}
			}
			else{
				grepCommand = "grep "+bcgExportTestData.getVerifyMo()+SPACE + EXPORTPATH +bcgExportTestData.getExportFileName();
				LOGGER.info("grepCommand is "+grepCommand);
				String moFound = helper.simpleExec(grepCommand);
				LOGGER.info("MO in Export File : "+moFound);
				if(!moFound.contains(bcgExportTestData.getVerifyMo())){
					return false;
				}
			}
		}catch(Exception e){
			LOGGER.info("Caught exception while verifying the MO in files"+e+" "+e.getMessage());
			return false;
		}
		/*LOGGER.info("grepCommand is "+grepCommand);
		String moFound = helper.simpleExec(grepCommand);
		LOGGER.info("MO in Export File : "+moFound);*/
		//helper.getShell().disconnect();
		helper.disconnect();
		/*if(!moFound.contains(bcgExportTestData.getVerifyMo())){
			return false;
		}*/
		return true;
	}

}