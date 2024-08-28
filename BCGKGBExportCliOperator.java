package com.ericsson.bcgcdb.test.operators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ericsson.bcgcdb.test.cases.BCGKGBExportData;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

@Operator(context = Context.CLI)
public class BCGKGBExportCliOperator implements BCGKGBExportOperator {

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
	private final String PERIODICEXPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/start_rah_export.sh";
	private final String FILESIZE_GREATER_THAN_ZERO_COMMAND = "du -sk ";
	private final String BCG_EXPORT_FILES_DIRECTORY = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	private final String SPACE = " ";
	private final String MINUSD = "-d";
	private final String MINUSN = "-n";
	private final String NECHECK = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt MeContext -f neType==";
	private final String FILECHECK = "test -e /var/opt/ericsson/nms_umts_wran_bcg/logs/export/errInfoLog_UTRAN_TOPOLOGY*.tmp && echo \"file exists\"";
	private final String CHECKSUCCESS = "grep \'Successfully completed PERIODIC_EXPORT\' /var/opt/ericsson/nms_umts_wran_bcg/logs/export/errInfoLog_UTRAN_TOPOLOGY* | tail -1";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String MECONTEXTID = "MeContextId";
	private final String SUBNETWORK = "SubNetwork";
	private final String GREP = " | grep ";
	private final String MECONTEXT = "MeContext";
	private final String MINUSI = "-i";
	private final String MINUSIV = "-iv";
	private final String JUNIPER = "juniper";
	private final String EXPORTFILES = " cd /var/opt/ericsson/nms_umts_wran_bcg/files/export/ ; rm -rf *.xml *.bck *.gz *.txt ";
	private final String IMPORTFILES = " cd /var/opt/ericsson/nms_umts_wran_bcg/files/import/ ; rm -rf * ";
	private final String CSFILTERFILES = " cd /var/opt/ericsson/nms_umts_wran_bcg/files/export/csfilters/ ; rm -rf * ";
	private final String BCRFILES = " cd /var/opt/ericsson/nms_umts_wran_bcg/files/export/cache/lastexport_bcr/ ; rm -rf * ";
	private final String BCTFILES = " cd /var/opt/ericsson/nms_umts_wran_bcg/files/export/cache/lastexport_bct/ ; rm -rf * ";
	private final String BOTHFILES = " cd /var/opt/ericsson/nms_umts_wran_bcg/files/export/cache/lastexport_both/ ; rm -rf * ";
	private final String USERFILTERS = " cd /var/opt/ericsson/nms_umts_wran_bcg/files/export/userfilters/ ; rm -rf * ";
	private final String PCAMCRESTART = "/opt/ericsson/nms_cif_sm/bin/smtool coldrestart wran_pca -reason=Other -reasontext=\" \"";
	private final String SEGMASTERCSMCRESTART = "/opt/ericsson/nms_cif_sm/bin/smtool coldrestart Seg_masterservice_CS -reason=Other -reasontext=\" \"";
	private final String REGIONCSMCRESTART = "/opt/ericsson/nms_cif_sm/bin/smtool coldrestart Region_CS -reason=Other -reasontext=\" \"";
	private final String SNADMCRESTART = "/opt/ericsson/nms_cif_sm/bin/smtool coldrestart cms_snad_reg -reason=Other -reasontext=\" \"";
	private final String NEADMCRESTART = "/opt/ericsson/nms_cif_sm/bin/smtool coldrestart cms_nead_seg -reason=Other -reasontext=\" \"";
	private final String PCASMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep wran_pca ";
	private final String SEGMASTERCSSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep Seg_masterservice_CS ";
	private final String REGIONCSSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep Region_CS ";
	private final String SNADSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep cms_snad_reg ";
	private final String NEADSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep cms_nead_seg ";
	private final String BCGSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep wran_bcg ";
	private final String BCGMCRESTART = "/opt/ericsson/nms_cif_sm/bin/smtool coldrestart wran_bcg -reason=Other -reasontext=\"Made Cache changes\"";
	
	
	//Global variables defined in sprint 14.2.2
	private final String SMTOOL_COMMAND = "/opt/ericsson/nms_cif_sm/bin/smtool -set wran_bcg";
	private final String CREATEDXMLPATH="/var/opt/ericsson/nms_umts_wran_bcg/files/export/";
	String exportNodeList;
	String customFilterPath = "/opt/ericsson/nms_umts_bcg_meta/dat/customfilters/";
	boolean syncFlag = true;
	private final String BCGTOOLEXPORTGOPTION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -g";
	String fileNamePath;
	String allRNCNodes=null;
	private final String MINUSDB = "-d b";
	/**
	 * Initializing host, user and cli
	 */
	public BCGKGBExportCliOperator(){
		bcgEexportGetter = new BCGExportGetter();
		host = HostGroup.getOssmaster();
	  //  helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
             rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
		helper = new CLICommandHelper(host, operUser);
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgTestData
	 * @return result
	 */
	public boolean preAction(BCGKGBExportData bcgExportTestData) {
		LOGGER.info("*preAction*");
		if(!findMos(bcgExportTestData)){
			return false;
		}
		return true;
	}

	/**
	 * This method will check the server for the required Mos
	 * @param importmo
	 * @param fdnpath
	 * @return result
	 */
	private boolean findMos(BCGKGBExportData bcgExportTestData) {
		LOGGER.info("*findMos*");
		String allNodes = null;
		boolean pedresult = false;
		//boolean syncFlag = true;
		try{
			if(bcgExportTestData.getExportNodename().equalsIgnoreCase("SubNetwork") || bcgExportTestData.getExportNodename().equalsIgnoreCase("periodic export")){
				String getSubnetwork = CSTESTLT + SUBNETWORK;
				allNodes = helper.simpleExec(getSubnetwork);
				LOGGER.info("List of available nodes : "+allNodes);
				if(!allNodes.isEmpty()){
					String[] fdns = allNodes.split("\n");
					if(fdns.length > 0){
						nodeList = fdns[0];
					}
				}
			}
			
			/*Code changes in BCG_O14B_Sprint16 starts here
			Export of Unsynch node by setting ExportNodeIfUnsynched PED parameter to true starts here*/
			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("MeContext")){
				LOGGER.info("Inside Mecontext");
				String getSubnetwork = NECHECK + bcgExportTestData.getExportNodeType();
				LOGGER.info("Command is :::"+getSubnetwork);
				allNodes = helper.simpleExec(getSubnetwork);
				LOGGER.info("List of available nodes : "+allNodes);
				if(!allNodes.isEmpty()){
					String[] fdns = allNodes.split("\n");
					LOGGER.info("Length of node  : "+fdns.length);
					if(fdns.length > 0){
						for(int i =1; i<fdns.length; i++){
							exportNodeList="";
							exportNodeList=fdns[i];
									if(!isNodeSynchronized(exportNodeList)){
										LOGGER.info("Unsynch node is :::::"+fdns[i]+" "+exportNodeList);
										nodeList = exportNodeList;
										try{
											pedresult = upadePEDParameters("ExportNodeIfUnsynched","true");
											LOGGER.info("PED parameter after updating :::: "+pedresult);
											if(pedresult){
												LOGGER.info("PED Parameter ExportNodeIfUnsynched updated to true");
												syncFlag = false;
												break;
											}
										}catch(Exception e){
											pedresult = false;
										}
									}
									else{
			
										nodeList = exportNodeList;
									}
								}
						}
						if(syncFlag){
							LOGGER.info("All Node are Synch");
							
						}
					}
			}
			/*Code changes in BCG_O14B_Sprint16 ends here
			Export of Unsynch node by setting ExportNodeIfUnsynched PED parameter to true ends here*/

			else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("EPG") ){
				String getFdn = CSTESTLT +SPACE + MECONTEXT + SPACE +GREP + SPACE + MINUSI + SPACE + bcgExportTestData.getExportNodename() + SPACE + GREP + SPACE + MINUSIV + SPACE + JUNIPER;
				LOGGER.info("EPG grep lt command ::: "+ getFdn);
				allNodes = helper.simpleExec(getFdn);
				LOGGER.info("List of available nodes : "+allNodes);
				if(!allNodes.isEmpty()){
					String[] fdns = allNodes.split("\n");
					if(fdns.length > 0){
						for(int i = 0 ; i < fdns.length; i++){
							if (isNodeSynchronized(fdns[i])){
								nodeList = fdns[i];
								LOGGER.info("node for EPG :::: "+ nodeList);
								break;
							}
						}
					}
				}
			}else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("DG2") ){
				String getFdn = CSTESTLT +SPACE + MECONTEXT + SPACE +GREP + SPACE + MINUSI + SPACE + bcgExportTestData.getExportNodename() + SPACE + GREP + SPACE + MINUSIV + SPACE + JUNIPER;
				LOGGER.info("EPG grep lt command ::: "+ getFdn);
				allNodes = helper.simpleExec(getFdn);
				LOGGER.info("List of available nodes : "+allNodes);
				if(!allNodes.isEmpty()){
					String[] fdns = allNodes.split("\n");
					if(fdns.length > 0){
						for(int i = 0 ; i < fdns.length; i++){
							if (isNodeSynchronized(fdns[i])){
								nodeList = fdns[i];
								LOGGER.info("node for DG2 :::: "+ nodeList);
								break;
							}
						}
					}
				}
			}else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("MSRBS") ){
				String getFdn = CSTESTLT +SPACE + MECONTEXT + SPACE +GREP + SPACE + MINUSI + SPACE + bcgExportTestData.getExportNodename() + SPACE + GREP + SPACE + MINUSIV + SPACE + JUNIPER;
				LOGGER.info("EPG grep lt command ::: "+ getFdn);
				allNodes = helper.simpleExec(getFdn);
				LOGGER.info("List of available nodes : "+allNodes);
				if(!allNodes.isEmpty()){
					String[] fdns = allNodes.split("\n");
					if(fdns.length > 0){
						for(int i = 0 ; i < fdns.length; i++){
							if (isNodeSynchronized(fdns[i])){
								nodeList = fdns[i];
								LOGGER.info("node for MSRBS :::: "+ nodeList);
								break;
							}
						}
					}
				}
			}else if((bcgExportTestData.getExportNodename().equalsIgnoreCase("SSR")) ){
				String getFdn = CSTESTLT +SPACE + MECONTEXT + SPACE + GREP + SPACE + MINUSI + SPACE + bcgExportTestData.getExportNodename();
				LOGGER.info("EPG grep lt command ::: "+ getFdn);
				allNodes = helper.simpleExec(getFdn);
				LOGGER.info("List of available nodes : "+allNodes);
				if(!allNodes.isEmpty()){
					String[] fdns = allNodes.split("\n");
					if(fdns.length > 0){
						for(int i = 0 ; i < fdns.length; i++){
							if (isNodeSynchronized(fdns[i])){
								nodeList = fdns[i];
								LOGGER.info("node for SSR :::: "+ nodeList);
								break;
							}
						}
					}
				}
			}else{
				LOGGER.info("Inside last else");
				String getAllNodes = NECHECK+bcgExportTestData.getExportNodeType();
				allRNCNodes = helper.simpleExec(getAllNodes);
				allNodes = helper.simpleExec(getAllNodes);
				LOGGER.info("List of available nodes : "+allNodes);
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
							}else if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("15")){
								String getStn = CSTESTLA +SPACE + fdns[i] + MECONTEXTID;
								allNodes = helper.simpleExec(getStn);
								LOGGER.info("List of available nodes : "+allNodes);
								if(bcgExportTestData.getExportNodename().equalsIgnoreCase("SIU")){
									if(allNodes.contains("SIU") && isNodeSynchronized(fdns[i])){
										nodeList = fdns[i];
										break;
									}
								}if(bcgExportTestData.getExportNodename().equalsIgnoreCase("TCU")){
									if(allNodes.contains("TCU") && isNodeSynchronized(fdns[i])){
										nodeList = fdns[i];
										break;
									}
								}
							}else if(bcgExportTestData.getExportNodeType().equalsIgnoreCase("14")){
								LOGGER.info("Inside 14");
								String[] fdn = fdns[i].split(",");
								if(bcgExportTestData.getExportNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
									if(fdn.length > 0){
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
							}
							/*else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("34")){
								nodeList = fdns[fdns.length - 1];
								break;
							}*/

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
		}catch(Exception e){
			LOGGER.info("Caught Exception while finding the MO's"+e.getMessage()+"\n"+e);
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

	/**
	 * This method will export for the node
	 * @return result as boolean
	 * @throws InterruptedException 
	 */
	public boolean BcgExport(BCGKGBExportData bcgExportTestData){
		LOGGER.info("*BcgExport*");
		String bcgExportCommand = "";
		String exportOutput = null;
		String exedate ;
		try{
		if(bcgExportTestData.getExportNodename().equalsIgnoreCase("periodic export")){
			if(bcgExportTestData.getDomain() == null){
				bcgExportCommand =  PERIODICEXPORT;
			}
			else{
				if(bcgExportTestData.getTimeValue() != null){
					if(bcgExportTestData.getCompression() != null){
						bcgExportCommand =  PERIODICEXPORT + SPACE + bcgExportTestData.getDomain() + SPACE + bcgExportTestData.getExportFileName() + SPACE + bcgExportTestData.getTimeValue() + SPACE + bcgExportTestData.getCompression();
					}
					else{
						bcgExportCommand =  PERIODICEXPORT + SPACE + bcgExportTestData.getDomain() + SPACE + bcgExportTestData.getExportFileName() + SPACE + bcgExportTestData.getTimeValue();	
					}
				}
				else{
					bcgExportCommand =  PERIODICEXPORT + SPACE + bcgExportTestData.getDomain() + SPACE + bcgExportTestData.getExportFileName();
				}
			}
		}
		else if(bcgExportTestData.getExportNodename().equalsIgnoreCase("SubNetwork")){
			/*Code changes in BCG_O14B_Sprint16 starts here
				Full network export with -c starts starts here*/
			if(bcgExportTestData.getCompression() != null){
				LOGGER.info("With -c option Export");
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName()+ SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + bcgExportTestData.getCompression();	
			}
			else{
				LOGGER.info("Without -c option Export");
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName()+ SPACE + MINUSD + SPACE + bcgExportTestData.getDomain();
			}
			/*Code changes in BCG_O14B_Sprint16 ends here
			Full network export with -c starts ends here*/
		}
		else if(bcgExportTestData.getCompression() != null)
		{
			LOGGER.info("***********************************Inside compression*****************************");
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName()+ SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgExportTestData.getCompression();
		}
		else if((bcgExportTestData.getDateValue() != null))
		{
			String mydate = "date '+%Y-%m-%d" + SPACE + "%H:%M:%S'";
			String exe = helper.simpleExec(mydate);
			exe = exe.replace("[", "");
			exe = exe.replace("]", "");
			exedate = exe;
			LOGGER.info("exedate :" + exedate);
			String AttributeChange = "sa" + SPACE + nodeList + SPACE + "latitude" + SPACE + "12345";
			LOGGER.info("value :" + AttributeChange);
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList  + SPACE +  "-time date" + SPACE + exedate;
		}
		else if(bcgExportTestData.getTimeValue() != null)
		{
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgExportTestData.getTimeValue();
		}
		else if(bcgExportTestData.getDomain() != null){
			/*Code changes in BCG_O14B_Sprint16 starts here
			MOC Filter export with -c starts starts here*/
			if(bcgExportTestData.getCompression() != null){
				LOGGER.info("Inside with compress");
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList + SPACE + bcgExportTestData.getCompression();
			}
			else{
				LOGGER.info("Inside without compress");
				bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
			}
			/*Code changes in BCG_O14B_Sprint16 ends here
			MOC Filter export with -c ends here*/	
		}
		
		/*Code changes in BCG_O14B_Sprint16 starts here
		Export with -g starts starts here*/
		else if(bcgExportTestData.getExportFileName() == null){
			LOGGER.info("Inside -g option");
			fileNamePath=CreateFileName();
			if(fileNamePath!=""){
				bcgExportCommand =  BCGTOOLEXPORTGOPTION + SPACE +fileNamePath + "bcgTeam11.xml";
			}
		}
		else{
			bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgExportTestData.getExportFileName() + SPACE + MINUSD + SPACE + bcgExportTestData.getDomain();
		}
		/*Code changes in BCG_O14B_Sprint16 ends here
		Export with -c option ends here*/
		LOGGER.info("BCG Export command: "+bcgExportCommand);
		exportOutput = helper.simpleExec(bcgExportCommand);
		LOGGER.info("BCG export output : "+exportOutput);
		if(bcgExportTestData.getExportNodename().equalsIgnoreCase("Subnetwork") && bcgExportTestData.getDomain().equals("r")){
			helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/radioMoCount.txt");
		}if(bcgExportTestData.getExportNodename().equalsIgnoreCase("Subnetwork") && bcgExportTestData.getDomain().equals("t")){
			helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/trnsportMoCount.txt");
		}if(bcgExportTestData.getExportNodename().equalsIgnoreCase("Subnetwork") && bcgExportTestData.getDomain().equals("b")){
			helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/bothMoCount.txt");
		}if(bcgExportTestData.getExportNodename().equalsIgnoreCase("Subnetwork") && bcgExportTestData.getDomain().equals("BCR.xml")){
			helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/bcrMoCount.txt");
		}if(bcgExportTestData.getExportNodename().equalsIgnoreCase("Subnetwork") && bcgExportTestData.getDomain().equals("BCT.xml")){
			helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/bctMoCount.txt");
		}if(bcgExportTestData.getExportNodename().equalsIgnoreCase("Subnetwork") && bcgExportTestData.getDomain().equals("BCR_BCT.xml")){
			helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/bcrbctMoCount.txt");
		}
		
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

	/**
	 * This method will check the MO is there in export file or not
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean verifyMo(BCGKGBExportData bcgExportTestData) {
		LOGGER.info("*verifyMo*");
		String grepCommand = "grep "+bcgExportTestData.getVerifyMo()+SPACE + EXPORTPATH +bcgExportTestData.getExportFileName();
		String moFound = helper.simpleExec(grepCommand);
		LOGGER.info("MO in Export File : "+moFound);
		//helper.getShell().disconnect();
		helper.disconnect();
		if(!moFound.contains(bcgExportTestData.getVerifyMo())){
			return false;
		}
		return true;
	}

	/**
	 * This method will check the validation of the bcg export test case
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean validation(BCGKGBExportData bcgExportTestData) {
		LOGGER.info("*validation*");
		int fileSize = 0;
		String bcgfilesizecommand = null;
		String exportoutputfilesize;
		boolean exitFlag = false;
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
				if(bcgExportTestData.getCompression() != null && bcgExportTestData.getCompression().contains("-c")){
					bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgExportTestData.getExportFileName()+".gz | cut -c 0-1";
				}
				else if(bcgExportTestData.getExportFileName() == null){
					bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + "bcgTeam11.xml" + " | cut -c 0-1";
				}
				else{
					bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgExportTestData.getExportFileName()+ " | cut -c 0-1";
				}
			}
			LOGGER.info("BCG file size command : "+bcgfilesizecommand);
			exportoutputfilesize = helper.simpleExec(bcgfilesizecommand);
			fileSize = Integer.parseInt(exportoutputfilesize.trim());
			LOGGER.info("BCG Export filesize : "+fileSize);
			if(syncFlag==false){
				LOGGER.info("Updating PED parameter back to false");
				boolean pedresultFlag=false;
				pedresultFlag = upadePEDParameters("ExportNodeIfUnsynched","false");
				if(pedresultFlag){
					LOGGER.info("PED Parameter ExportNodeIfUnsynched is set to false");
				}
			}
		}catch(Exception e){
			LOGGER.info("Caught Exception while validating exported TestCase"+e.getMessage()+"\n"+e);
		}finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
		//code to handle exitcode starts here
		exitFlag = verifyExitCode();
		if(fileSize > 0){
			if(exitFlag){
				LOGGER.info("Export has finished successfully");
				return true;
			}
			if(bcgExportTestData.getVerifyMo() != null){
				return verifyMo(bcgExportTestData);
			}
			//return true;
		}
		//code to handle exitcode ends here
		return false;
	}
	
	/*Impplementation of export of unsynch node by updating the PED parameter starts here*/
	private boolean upadePEDParameters(String pedName, String pedValue) {
		LOGGER.info("upadePEDParameters method called ");
		boolean result = false;
		try{
			LOGGER.info("setting PED parameters: ");
			String updatepedcommand = SMTOOL_COMMAND + SPACE + pedName + SPACE + pedValue;
			LOGGER.info("BCG set PED parameter command : "+updatepedcommand);
			LOGGER.info(updatepedcommand);
			String PEDOutput = helper.simpleExec(updatepedcommand);
			LOGGER.info("update PED parameter output :::"+PEDOutput);
			if(!PEDOutput.contains("error")){
				result = true;
			}
		}catch(Exception e){
			LOGGER.info("Exception while setting PED parameter for export of unsynch Node"+e.getMessage()+"\n"+e);
		}finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
		return result;
	}
	
	/*Impplementation of export of unsynch node by updating the PED parameter ends here*/
	
	/*Implentation of -g options starts here for sprint 14.2.2 starts here*/
	private String CreateFileName(){
		LOGGER.info("Inside file creation");
		String remoteFileLocation="";
		try {
			LOGGER.info("File Creation all RNC nodes ::::"+allRNCNodes);
			allRNCNodes.replace("\n"," ");
			String string1=BCGTOOLEXPORT + SPACE +"exportGFile.xml";
			String FinalString = string1 + "\n" + MINUSDB + "\n"+ MINUSN + SPACE + allRNCNodes;
			File file = new File("bcgTeam11.xml");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
				
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(FinalString);
			bw.close();
			
			RemoteFileHandler remote = new RemoteFileHandler(host,rootUser);
			String pythonscr = FileFinder.findFile("bcgTeam11.xml").get(0);
			String localFileLocation = pythonscr;
			LOGGER.info("localFileLocation :: "+localFileLocation);
			remoteFileLocation = CREATEDXMLPATH;
			LOGGER.info("remoteFileLocation before :: "+remoteFileLocation);
			remote.copyLocalFileToRemote(localFileLocation ,remoteFileLocation);
			LOGGER.info("remoteFileLocation after :: "+remoteFileLocation);
			LOGGER.info("Done");
 
		} catch (IOException e) {
			LOGGER.info("Exception while creating file for -g testCase "+e.getMessage()+"\n"+e);
		}
		finally{
			
		}
		return remoteFileLocation;
	}
	
	/*Implentation of -g options ends here for sprint 14.2.2 starts here*/
	boolean verifyExitCode() {
		int exitCode =helper.getCommandExitValue();
		LOGGER.info("Exit code of export command is exitCode");
		return exitCode == 0;

	}
	
	public boolean cleanUpFiles() {
		
		try{
			helper.simpleExec(EXPORTFILES);
			helper.simpleExec(IMPORTFILES);
			helper.simpleExec(CSFILTERFILES);
			helper.simpleExec(USERFILTERS);
			helper.simpleExec(BCRFILES);
			helper.simpleExec(BCTFILES);
			helper.simpleExec(BOTHFILES);
		}
		catch(Exception clean){
			LOGGER.info(clean.getMessage());
			LOGGER.info(clean);
		}
		finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
		return true;
		
	}
	
	public void restartMCS(){
		
		try{
			helper.simpleExec(SEGMASTERCSMCRESTART);
			while(!(helper.simpleExec(SEGMASTERCSSMTOOL)).contains("started")){
				helper.disconnect();
			}
			helper.simpleExec(REGIONCSMCRESTART);
			while(!(helper.simpleExec(REGIONCSSMTOOL)).contains("started")){
				helper.disconnect();
			}
			helper.simpleExec(SNADMCRESTART);
			while(!(helper.simpleExec(SNADSMTOOL)).contains("started")){
				helper.disconnect();
			}
			helper.simpleExec(NEADMCRESTART);
			while(!(helper.simpleExec(NEADSMTOOL)).contains("started")){
				helper.disconnect();
			}
			helper.simpleExec(BCGMCRESTART);
			while(!(helper.simpleExec(BCGSMTOOL)).contains("started")){
				helper.disconnect();
			}
			helper.simpleExec(PCAMCRESTART);
			while(!(helper.simpleExec(PCASMTOOL)).contains("started")){
				helper.disconnect();
			}
		}
		catch(Exception mcRestart){
			LOGGER.info(mcRestart.getMessage());
			LOGGER.info(mcRestart);
		}
		finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
		
	}
}

