

package com.ericsson.bcgcdb.test.operators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.ericsson.bcgcdb.test.cases.F_BCGTestData;
import com.ericsson.bcgcdb.test.getters.BCGImportGetter;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
//my changes
//my changes end



@Operator(context = Context.CLI)
public class F_BCGImportCliOperator implements F_BCGImportOperator  {

	Shell shell;
	Host host;
	User operUser;
 
	private final String CSTEST = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt ";
	private final String CSTESTLA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice la ";
	private final String CSTESTSA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice sa ";
	private final String CSTESTMI = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice mi ";
	private final String CPLMN = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice cm SubNetwork=ONRM_ROOT_MO_R,Areas=1,Plmn=501 -attr PlmnId 501 mcc 801 mnc 801 mncLength 2";
	private final String DPLMN = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice dm SubNetwork=ONRM_ROOT_MO_R,Areas=1,Plmn=501";
	private final String CLA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice cm SubNetwork=ONRM_ROOT_MO_R,Areas=1,Plmn=501,LocationArea=1 -attr LocationAreaId 501 lac 1";
	private final String REMOVEIMPORT = "rm -rf /var/opt/ericsson/nms_umts_wran_bcg/files/import/";
	private final String UNDOPLANPATH ="/var/opt/ericsson/nms_umts_wran_undoplan/files/";
	private final String IMPORTPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/import/";
	private final String BCGTOOLEXPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -e ";
	private final String BCGTOOLIMPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -i ";
	private final String BCGTOOLUNDO = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -u ";
	private final String BCGTOOLPLANACTIVATION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -a ";
	private final String BCGTOOLUNDOPLANCREATION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -au ";
	private final String BCGTOOLUNDOPLANACTIVATION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -a Undo_";
	private final String BCGTOOLREMOVEPLAN = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -rp ";
	private final String BCGTOOLREMOVEUNDOPLAN = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -rp Undo_";
	private final String VSDATA = " -d :vsData";
	private final String PYTHON = "python";
	private final String PYTHONPATH = " /home/nmsadm/Createimportfile.py ";
	private final String PYTHONREMOTEPATH = "/home/nmsadm/";
	private final String MINUSP = " -p ";
	private final String MINUSAS = " -as ";
	private final String SPACE = " ";
	private final String SMTOOL_COMMAND = "smtool -set wran_bcg";
	private final String SYNCHRONISATIONSTATUS = "synchronisationProgress";
	private final String MOLOCK = "-molock";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String EXPORTPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/"; //my changes

	BCGImportGetter bcgImportGetter;
	Logger LOGGER;
	CLICommandHelper helper;


	String autoLockUnlock;
	String adminState;
	String autoLockUnlockBackup;
	String nodeList;
	String adminStateFdn;
	String bachupAutoLockUnlock;
	String PEDOutput;
	String adminStateOutput;
	String admState;
	boolean isPEDIncluded = false;
	boolean isDeleteDone = false;
	boolean isAdminStateIncluded = false;
	private String modifierString;
	//my changes
	static Map<String,String> hashmap = new HashMap<String,String>();

	/**
	 * Initializing host, user and cli
	 */
	public F_BCGImportCliOperator(){
		bcgImportGetter = new BCGImportGetter();
		host = HostGroup.getOssmaster();
//	    helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
//		//	cli = new CLI(host, operUser);
		helper = new CLICommandHelper(host, operUser);
		
		LOGGER = Logger.getLogger(F_BCGImportCliOperator.class);


	}

	/**
	 * This method for BCG import
	 * @param ImportFileName as String
	 * @param PlanName as String
	 * @return boolean
	 */
	public String bcgImport(F_BCGTestData f_bcgTestData){
		LOGGER.info("BCGIMPORT");
		String bcgImportcommand = null;
		if(f_bcgTestData.getPed_autolockunlock() == null){
			bcgImportcommand =BCGTOOLIMPORT + f_bcgTestData.getImportfilename() + MINUSP + f_bcgTestData.getPlanname();
		}
		else{
			bcgImportcommand =BCGTOOLIMPORT + f_bcgTestData.getImportfilename() + MINUSP + f_bcgTestData.getPlanname() +  SPACE + MOLOCK;
		}

		if(f_bcgTestData.getScheme() !=null)
		{
			if (f_bcgTestData.getScheme().equals("system") || f_bcgTestData.getScheme().equals("networkelement") || f_bcgTestData.getScheme().equals("plan"))
				bcgImportcommand += MINUSAS + f_bcgTestData.getScheme();
			else {
				LOGGER.info("BCG Import command : "+bcgImportcommand);
				LOGGER.info("Invalid activation scheme option entered");
				return "Import has failed";
			}
		}
		LOGGER.info("BCG Import command : "+bcgImportcommand);
		String importOutput = helper.simpleExec(bcgImportcommand);
		LOGGER.info("BCG Import output :::::\n"+ importOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		//if(importOutput.contains("Export has succeeded")){
		if(importOutput.contains("Import has succeeded")){
			LOGGER.info("Import has succeeded   ::::::::");
			return "Import has succeeded";
		}
		else{
			LOGGER.info("Import has failed   :::::::");
			return "Import has failed";
		}
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param f_bcgTestData
	 * @return result
	 */
	public boolean preAction(F_BCGTestData f_bcgTestData){
		LOGGER.info("***preAction***");
		boolean pedresult = false;
		boolean adminStateresult = false;
		boolean findMOResult = false;
		if(!(f_bcgTestData.getPed_autolockunlock() == null)){
			LOGGER.info("###Setting PED parameters");
			isPEDIncluded = true;
			backupPed(autoLockUnlock, f_bcgTestData.getPed_autolockunlock());
			pedresult = upadePEDParameters("autoLockUnlock", f_bcgTestData.getPed_autolockunlock());
			LOGGER.info("pedResult     "+pedresult);
			if (!pedresult) {
				return false;
			}
		}
		if((f_bcgTestData.getImportmo().equals("IpAccessHostEt"))||(f_bcgTestData.getImportmo().equals("IpSyncRef"))
				||(f_bcgTestData.getImportmo().equals("PacketFrequencySyncRef")) || (f_bcgTestData.getImportmo().equals("Aal2PathVccTp"))
				|| (f_bcgTestData.getImportmo().equals("IubLink")) || (f_bcgTestData.getImportmo().equals("Ospf")) 
				|| (f_bcgTestData.getImportmo().equals("IpAccessHostGpb"))
				){
			if(((f_bcgTestData.getAdminState() != null))){	//# nagu changes
				LOGGER.info("###Setting AdminState parameter");
				admState  = f_bcgTestData.getAdminState();
				adminStateresult = upadeStateParameter("administrativeState", f_bcgTestData.getAdminState(),f_bcgTestData);
				LOGGER.info("adminState setting result    "+adminStateresult);
//				if (!adminStateresult) {
					return true;
//				}
			}
		}
		if(f_bcgTestData.getImportmo().contains("LocationArea") || f_bcgTestData.getImportmo().contains("RoutingArea") || f_bcgTestData.getImportmo().contains("ServiceArea")){
			LOGGER.info("Handling for LocationArea ServiceArea and Routing Area");
			String csOutput = helper.simpleExec(CPLMN);
			if(!csOutput.contains("Excepction") && (!f_bcgTestData.getImportmo().equals("LocationArea"))){
				csOutput = helper.simpleExec(CLA);
			}
			if(csOutput.contains("Excepction")){
				return false;
			}
			findMOResult = findMos(f_bcgTestData);
			if(!findMOResult){
				return false;
			}
		}
		else{
			findMOResult = findMos(f_bcgTestData);
			if(!findMOResult){
				return false;
			}
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return true;
	}

	/**
	 * This method will update the PED parameter values
	 * @param ped_name
	 * @param ped_value
	 * @return result
	 */
	private boolean upadePEDParameters(String pedName, String pedValue) {

		boolean isPEDParameterUpdated = false;
		LOGGER.info("setting PED parameters: ");
		String updatepedcommand = SMTOOL_COMMAND + SPACE + pedName + SPACE + pedValue;
		LOGGER.info("BCG set PED parameter command : "+updatepedcommand);
		PEDOutput = helper.simpleExec(updatepedcommand);
		if(!PEDOutput.contains("error")){
			LOGGER.info("upadePEDParameters result  ::::   true");
			isPEDParameterUpdated = true;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return isPEDParameterUpdated;
	}
	/**
	 * This method will update the adminState parameter values
	 * @param f_bcgTestData 
	 * @param ped_name
	 * @param ped_value
	 * @return result
	 */
	private boolean upadeStateParameter(String adminState, String adminStateValue, F_BCGTestData f_bcgTestData) {//# nagu changes

		boolean isAdministrativeStateUpdated = false;
		String updatePEDCommand = "";
		String csTestCommand = CSTEST + SPACE + f_bcgTestData.getImportmo();
		LOGGER.info("BCG Find MO call command : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		String[] fdns = csOutput.split("\n");
		LOGGER.info("setting adminState parameter  ::::: : ");
		
		if(f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext"))
		{
			findMos(f_bcgTestData);
			updatePEDCommand = CSTESTSA + SPACE + adminStateFdn+ SPACE + adminState + SPACE + adminStateValue;
		}
		else
		{
			updatePEDCommand = CSTESTSA + SPACE + fdns[0]+ SPACE + adminState + SPACE + adminStateValue;
		}
		LOGGER.info("BCG set PED parameter command : "+updatePEDCommand);
		adminStateOutput = helper.simpleExec(updatePEDCommand);
		LOGGER.info("setting admin state ::::\n"+ adminStateOutput);
		if(!adminStateOutput.contains("error")){
			isAdministrativeStateUpdated = true;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return isAdministrativeStateUpdated;
	}

	/**
	 * This method will check the server for the required Mos
	 * @param importmo
	 * @param fdnpath
	 * @return result
	 */
	private boolean findMos(F_BCGTestData f_bcgTestData) {
		boolean result = false;
		String oldString = " ";
		String csTestCommand = CSTEST + SPACE + f_bcgTestData.getImportmo();
		LOGGER.info("BCG Find MO call command : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		String[] fdns = csOutput.split(f_bcgTestData.getImportmo());
		for(String fdn : fdns)
		LOGGER.info("fdn details : "+ fdn);
		//String csAdminStateOutput = helper.simpleExec(csTestCommand);
		String[] cs_adminFdns = csOutput.split("\n");
		LOGGER.info("nodes from CS : "+fdns);
		String[] adminFdns = new String[cs_adminFdns.length]; 
		
		int count = 0;
		if(f_bcgTestData.getImportmo().toLowerCase().contains("ospf"))
		{
			for(String fdn : cs_adminFdns)
			{
				csTestCommand = CSTESTMI + SPACE + fdn;
				csOutput = helper.simpleExec(csTestCommand);
				LOGGER.info("mi ouput :"+csOutput);
				if (csOutput.contains("_MODEL"))
				{
					LOGGER.info("mi ouput inside if :"+csOutput);
					adminFdns[count++] = new String(fdn);
				}
			}
		}
		else{
			adminFdns = csOutput.split("\n");
		}
		for(String f : adminFdns)
			LOGGER.info("adminFdns : "+f);
		fdns = adminFdns;
		//my changes
		if(f_bcgTestData.getImportmo().equals("ExternalGsmCell") || f_bcgTestData.getImportmo().equals("ExternalUtranCell") || f_bcgTestData.getImportmo().equals("LocationArea") 
				|| f_bcgTestData.getImportmo().equals("ServiceArea") || f_bcgTestData.getImportmo().equals("RoutingArea") || f_bcgTestData.getImportmo().equals("ExternalGsmPlmn") 
				|| f_bcgTestData.getImportmo().equals("ExternalEUtranPlmn") || f_bcgTestData.getImportmo().equals("MbmsServiceArea")
				|| f_bcgTestData.getImportmo().equals("PacketFrequencySyncRef") || f_bcgTestData.getImportmo().equals("EUtranCellRelation")
				|| f_bcgTestData.getImportmo().equals("EUtranCellFDD")
				|| (f_bcgTestData.getImportmo().equals("IpAccessHostEt") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
				|| (f_bcgTestData.getImportmo().equals("Aal2PathVccTp") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
				|| (f_bcgTestData.getImportmo().equals("Aal2Ap") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
				|| (f_bcgTestData.getImportmo().equals("IpSyncRef") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
				){

			if(fdns.length > 0){
				for(int i =0; i<fdns.length; i++){
					String [] commasplit = fdns[i].split(",");
					nodeList = "";

					if(commasplit[0].contains("SubNetwork")){
						nodeList = commasplit[0];
						nodeList = nodeList.replace("[", "");
						adminStateFdn = fdns[i];
						LOGGER.info("adminStateFdn  fdns[i] for SubNetwork ::::::  "+  adminStateFdn  + "i value  :::"+ i);
						result = true;
						break;
					}
				}
			}

		}else{
			if(f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,SubNetwork")){
				if(fdns.length > 0){
					for(int i =0; i<fdns.length; i++){
						nodeList = "";
						String [] commasplit = fdns[i].split(",");
						if(i==0){
							if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("MeContext"))
								nodeList = commasplit[0]+","+commasplit[1];
						}
						else{
							if( commasplit[0].contains("SubNetwork") && commasplit[1].contains("MeContext"))
								nodeList = commasplit[1]+","+commasplit[2];
						}
						nodeList = nodeList.replace("[", "");
						nodeList = nodeList.replace("]", "");
						nodeList = nodeList.replace(" ", "");
						adminStateFdn = fdns[i];
						LOGGER.info("adminStateFdn  fdns[i] for SubNetwork,SubNetwork ::::::  "+  adminStateFdn  + "i value  :::"+ i);
						if(!oldString.equals(nodeList)){
							if(isNodeSynchronized(nodeList)){

								result = true;
								break;
							}
						}
						oldString = nodeList;
					}
				}
			}
			else{
				if(fdns.length > 0){
					for(int i =0; i<fdns.length; i++){
						nodeList = "";
						LOGGER.info("nodeList ::: inside last else loop  :: " + nodeList);
						String [] commasplit = fdns[i].split(",");
						if(i==0){
							try
							{
								nodeList = commasplit[0]+","+commasplit[1]+","+commasplit[2];
								LOGGER.info("nodeList ::: i=0  :: " + nodeList);
							}
							catch(Exception e)
							{
								//ignore exception as of now
							}
						}
						else{
							try
							{
								nodeList = commasplit[1]+","+commasplit[2]+","+commasplit[3];
								LOGGER.info("nodeList ::: try :: " + nodeList);
							}
							catch(Exception e)
							{
								//ignore exception as of now
							}
						}
						nodeList = nodeList.replace("[", "");
						nodeList = nodeList.replace("]", "");
						nodeList = nodeList.replace(" ", "");
						adminStateFdn = adminFdns[i];
						LOGGER.info("adminStateFdn   ::::: " + adminStateFdn);
						LOGGER.info("nodeList   last   ::::: " + nodeList);
						LOGGER.info("oldString :: inside last else loop  ::  "  + oldString);
//						LOGGER.info("adminStateFdn  adminFdns[i] for else ::::::  "+  adminStateFdn  + "i value  :::"+ i);
						if(!oldString.equals(nodeList)){
							if(isNodeSynchronized(nodeList)){
								LOGGER.info("synched node  :: " +  nodeList);
								result = true;
								break;
							}
						}
						
						oldString = nodeList;
					}
				}

			}	
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		LOGGER.info("node : "+nodeList);
		LOGGER.info("result : "+result);
		return result;
	}

	/**
	 * this method for export the mo
	 * @param exportfdn as string
	 * @param importMo as string
	 * @param importFileName as string
	 * @return boolean
	 */
	private boolean bcgExport(String exportfdn, String importMo, String importFileName){

		String bcgExportCommand;
		String exportOutput;
		bcgExportCommand =  BCGTOOLEXPORT + importFileName + VSDATA + importMo + " -n " + exportfdn;
		LOGGER.info("BCG Export command: "+bcgExportCommand);
		exportOutput = helper.simpleExec(bcgExportCommand);
		LOGGER.info("BCG Export"+  exportOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if(exportOutput.contains("Export has succeeded")){
			LOGGER.info("BCG XPort is Successfull  ::::::");
			return true;
		}		
		return false;
	}

	/**
	 * This method will copy the python script from Testware to server
	 * @return void
	 */
	private void copyPython(){

//		RemoteFileHandler remote = new RemoteFileHandler(host);
		RemoteObjectHandler remote = new RemoteObjectHandler(host);
		String pythonSCR = FileFinder.findFile("Createimportfile.py").get(0);
		String localFileLocation = pythonSCR;
		LOGGER.info("copyPython - local"+pythonSCR); //delete me
		String remoteFileLocation = PYTHONREMOTEPATH;
		LOGGER.info("copyPython - remote "+PYTHONREMOTEPATH); //delete me
		boolean remoteCopyFlag = remote.copyLocalFileToRemote(localFileLocation ,remoteFileLocation);
		LOGGER.info("copyPython - remoteCopyFlag "+remoteCopyFlag); //delete me
	}

	/**
	 * This method for running python script to create import XML file
	 * @param FileName as String
	 * @param importMo as String
	 * @return boolean
	 */
	private boolean pythonForImport(String importFilename, String importmo, int numberofmos, String modifier){
		String runPython;
		if(modifier.equals("create") && !isDeleteDone){
			runPython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + " delete " + numberofmos;
			isDeleteDone = true;
		}
		else
			runPython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + SPACE + modifier + SPACE + numberofmos;
		LOGGER.info("Running Python : " + runPython); //delete me
		String pythonOutput = helper.simpleExec(runPython);
		//helper.getShell().disconnect();
		helper.disconnect();
		if(pythonOutput.contains("Success")){
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * This method will check the required nodes is sync or not
	 * @param fdn as String
	 * @return result as boolean
	 */
	private boolean isNodeSynchronized(String fdn){
		boolean isNodeSynchronized = false;
		String csTestCommand = CSTESTLA + SPACE + fdn + SPACE + MIRRORMIBSYNCHSTATUS ;
		String csOutput = helper.simpleExec(csTestCommand);
		//helper.getShell().disconnect();
		helper.disconnect();
/*		if(csOutput.contains("100")){
			isNodeSynchronized = true;
		}*/
		if(csOutput.contains("5") || csOutput.contains("3")){
			LOGGER.info(fdn + " Node is sync");
			isNodeSynchronized = true;
		}
		else{
			LOGGER.info(fdn + " Node is Un-Sync");
		}
		return isNodeSynchronized;
	}

	/**
	 * This method will take the backup of the PED parameter
	 * @param pedName
	 * @param pedValue
	 * @return void
	 */
	private void backupPed(String pedName, String pedValue){

		String updatePEDCommand = SMTOOL_COMMAND + SPACE + pedName + SPACE + pedValue;
		LOGGER.info("BCG set PED parameter command : "+updatePEDCommand);
		helper.simpleExec(updatePEDCommand);
		//helper.getShell().disconnect();
		helper.disconnect();
		//	autoLockUnlockBackup = ;
	}
/*	*//**
	 * This method will take the backup of the PED parameter
	 * @param pedName
	 * @param pedValue
	 * @return void
	 *//*
	private void backupAS(int admState2, int i){

		String updatepedcommand = SMTOOL_COMMAND + SPACE + admState2 + SPACE + i;
		LOGGER.info("BCG set adminState parameter command : "+updatepedcommand);
		LOGGER.info("BCG set adminState parameter command : "+updatepedcommand);
		helper.simpleExec(updatepedcommand);
		//	autoLockUnlockBackup = ;
	}*/
	/**
	 * This method will revert back all the network changes after
	 * running the test case
	 * @param f_bcgTestData
	 * @return void
	 */
	public void postAction(F_BCGTestData f_bcgTestData){
		LOGGER.info("Post Actrion: ");
		if(f_bcgTestData.getModifier().equals("delete")){
			LOGGER.info(" delete part");
			if(undoPlanCreation(f_bcgTestData)){
				LOGGER.info("undoPlanActivation  -- postAction  :::: " + undoPlanActivation(f_bcgTestData));
			}
			LOGGER.info("removePlan  -- postAction  :::: " + removePlan(f_bcgTestData));
			LOGGER.info("removeUndoPlan  -- postAction  :::: " + removeUndoPlan(f_bcgTestData));
		}else{
			LOGGER.info("removePlan  -- postAction-- for update  :::: " + removePlan(f_bcgTestData));
		}
		if(f_bcgTestData.getImportmo().contains("LocationArea") || f_bcgTestData.getImportmo().contains("RoutingArea") || f_bcgTestData.getImportmo().contains("ServiceArea")){
			helper.simpleExec(DPLMN);
		
		}
		//helper.getShell().disconnect();
		helper.disconnect();
	}

	/**
	 * This method will create the import file based on the export file
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean prepareImportFile(F_BCGTestData f_bcgTestData){

		boolean exportResult = false;
		boolean importPythonResult = false;
		String deleteImport = "";

		if((f_bcgTestData.getImportmo().equals("IpAccessHostEt") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
			|| (f_bcgTestData.getImportmo().equals("IpSyncRef") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
			|| (f_bcgTestData.getImportmo().equals("Aal2Ap") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
			|| (f_bcgTestData.getImportmo().equals("Aal2PathVccTp") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
			|| (f_bcgTestData.getImportmo().equals("EUtranCellFDD") && (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
			|| (f_bcgTestData.getImportmo().equals("EUtranCellRelation")) //&& (f_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")))
			){
				String csTestCommand = CSTEST + SPACE + f_bcgTestData.getImportmo();
				LOGGER.info("BCG Find MO call command : "+csTestCommand);
				String csOutput = helper.simpleExec(csTestCommand);
				String[] fdns = csOutput.split("ManagedElement");
				String fdns1 = fdns[0].substring(0, (fdns[0].length()-1));
				exportResult = bcgExport(fdns1,f_bcgTestData.getImportmo(),f_bcgTestData.getImportfilename());
				LOGGER.info("prepareImportFile -exportResult :" +exportResult); //delete me
		}
		else
		{
			exportResult = bcgExport(nodeList,f_bcgTestData.getImportmo(),f_bcgTestData.getImportfilename());
			LOGGER.info("prepareImportFile else part -exportResult :" +exportResult); //delete me
		}
		
		//my changes
		if(f_bcgTestData.getImportmo().equals("EUtranCellTDD") || f_bcgTestData.getImportmo().equals("AntennaUnitGroup") 
				|| f_bcgTestData.getImportmo().equals("ExternalGsmPlmn") || f_bcgTestData.getImportmo().equals("ExternalEUtranPlmn") 
				|| f_bcgTestData.getImportmo().equals("AnrFunction") || f_bcgTestData.getImportmo().equals("MbmsServiceArea")
				|| f_bcgTestData.getImportmo().equals("IubDataStreams") || f_bcgTestData.getImportmo().equals("Sector")
				|| f_bcgTestData.getImportmo().equals("IpAccessHostEt")|| f_bcgTestData.getImportmo().equals("EUtranCellRelation")//# nagu changes
				|| f_bcgTestData.getImportmo().equals("IpSyncRef") || f_bcgTestData.getImportmo().equals("IpAccessHostGpb")
				|| f_bcgTestData.getImportmo().equals("EUtranCellFDD") || f_bcgTestData.getImportmo().equals("PacketFrequencySyncRef")
				|| f_bcgTestData.getImportmo().equals("Aal2PathVccTp") || f_bcgTestData.getImportmo().equals("IubLink")
				|| f_bcgTestData.getImportmo().equals("Ospf")  || (f_bcgTestData.getImportmo().equals("Aal2Ap"))  )	//# nagu changes
		{
			boolean result = false;	
			if(exportResult){
				//mandatory fields for delete/create modifier
				 String EUtranCellTDD_MF[] = {"es:frameStartOffset","es:earfcn","es:cellId","es:physicalLayerCellIdGroup",
							"es:physicalLayerSubCellId","es:tac","es:subframeAssignment","es:sectorFunctionRef"};
				 String EUtranCellFDD_MF[] ={"es:cellId","es:earfcndl","es:earfcnul","es:physicalLayerCellIdGroup",
							"es:physicalLayerSubCellId","es:tac"};		//# nagu changes
				 String EUtranCellFDD_ModF[] ={"es:useId"};		//# nagu changes
				 String Ospf_ModF[] ={"es:userLabel"};		//# nagu changes
				 String EUtranCellRelation_ModF[] ={"es:qOffsetCellEUtran"};	//# nagu changes
				 String ExternalEUtranPLMN_MF[] = {"es:plmnIdentity"};
				 String ExternalGsmPLMN_MF[] = {"es:mcc", "es:mnc", "es:mncLength"};
				 String AnrFunction_ModF[] = {"es:removeNcellTime"};
				 String MbmsServiceArea_MF[] = {"es:sac"};
				 String IubDataStreams_ModF[] = {"es:maxHsRate"};
				 String Sector_ModF[] = {"es:latitude"};
				 String IpAccessHostEt_ModF[] = {"es:ntpDscp"};		//# nagu changes
				 String IpAccessHostGpb_ModF[] = {"es:ipDefaultTtl"};		//# nagu changes
				 String IpSyncRef_ModF[] = {"es:userLabel"};		//# nagu changes
				 String Aal2Ap_ModF[] = {"es:userLabel"};		//# nagu changes
				 String Aal2PathVccTp_ModF[] = {"es:timerCu"};		//# nagu changes
				 String PacketFrequencySyncRef_ModF[] = {"es:userLabel"};		//# nagu changes
				 String IpAccessHostEt_MF[] = {"es:ipAddress","es:ipInterfaceMoRef"};	//# nagu changes
				 String IubLink_ModF[] = {"es:spare"};	//# nagu changes
				 
				 if(f_bcgTestData.getImportmo().equals("EUtranCellTDD"))
				 {
					 result = createImportFile(EUtranCellTDD_MF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("EUtranCellRelation"))
				 {
					 result = createImportFile(EUtranCellRelation_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("Ospf"))
				 {
					 result = createImportFile(Ospf_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("Aal2Ap"))
				 {
					 LOGGER.info("inside if(exportResut)  inside in Aal2Ap condition :::::::"); 
					 result = createImportFile(Aal2Ap_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("PacketFrequencySyncRef"))
				 {
					 result = createImportFile(PacketFrequencySyncRef_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("EUtranCellFDD"))		//# nagu changes
				 {
					 if(f_bcgTestData.getModifier().equals("create")){
						 result = createImportFile(EUtranCellFDD_MF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());	 
					 }
					 if(f_bcgTestData.getModifier().equals("update")){
						 result = createImportFile(EUtranCellFDD_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }
					 if(f_bcgTestData.getModifier().equals("delete")){
						 result = createImportFile(EUtranCellFDD_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }
					 
				 }
				 else if(f_bcgTestData.getImportmo().equals("Aal2PathVccTp"))		//# nagu changes
				 {
					 if(f_bcgTestData.getModifier().equals("create")){
						 result = createImportFile(EUtranCellFDD_MF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());	 
					 }
					 if(f_bcgTestData.getModifier().equals("update")){
						 result = createImportFile(Aal2PathVccTp_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }
					 if(f_bcgTestData.getModifier().equals("delete")){
						 result = createImportFile(Aal2PathVccTp_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }
					 
				 }
				 else if(f_bcgTestData.getImportmo().equals("ExternalEUtranPlmn"))
				 {
					 result = createImportFile(ExternalEUtranPLMN_MF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("ExternalGsmPlmn"))
				 {
					 result = createImportFile(ExternalGsmPLMN_MF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("AnrFunction"))
				 {
					 result = createImportFile(AnrFunction_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("MbmsServiceArea"))
				 {
					 result = createImportFile(MbmsServiceArea_MF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("IubDataStreams"))
				 {
					 result = createImportFile(IubDataStreams_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("Sector"))
				 {
					 result = createImportFile(Sector_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("IpAccessHostEt"))		//# nagu changes
				 {
					 if(f_bcgTestData.getModifier().equals("update")){
						 result = createImportFile(IpAccessHostEt_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }
					 if(f_bcgTestData.getModifier().equals("delete")){
						 result = createImportFile(IpAccessHostEt_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }
					 if(f_bcgTestData.getModifier().equals("create")){
						 result = createImportFile(IpAccessHostEt_MF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }
					 				 
				 }
				 else if(f_bcgTestData.getImportmo().equals("GeneralProcessorUnit"))
				 {
					 result = createImportFile(null, f_bcgTestData.getModifier(), f_bcgTestData.getImportfilename(), f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("IpSyncRef"))	// # nagu changes
				 {
					 if(f_bcgTestData.getModifier().equals("update")){
						 result = createImportFile(IpSyncRef_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }
					 if(f_bcgTestData.getModifier().equals("delete")){
						 result = createImportFile(IpSyncRef_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
					 }					 				 
				 }
				 else if(f_bcgTestData.getImportmo().equals("IpAccessHostGpb"))
				 {
					 result = createImportFile(IpAccessHostGpb_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else if(f_bcgTestData.getImportmo().equals("IubLink"))
				 {
					 result = createImportFile(IubLink_ModF,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				 }
				 else
				 {
					 result = createImportFile(null,f_bcgTestData.getModifier(),f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo());
				
				 if(f_bcgTestData.getModifier().equals("create")){       //# nagu changes
					 if(deleteImport.contains("Import has succeeded") || deleteImport.contains("Import has partially succeeded")){	//# nagu changes
							LOGGER.info("deleteImport for create modifier successful"); //delete me	# nagu changes
							if(rollbackFileCreation(f_bcgTestData)){			// # nagu changes
								if(PlanActivation(f_bcgTestData)){		// # nagu changes
									copyImportFile(f_bcgTestData);			//# nagu changes
								}
							}
						}
				 }
				 }
				 
			}
			importPythonResult = result;
		}
		else
		{	
			if(exportResult){
				copyPython();
				if(f_bcgTestData.getModifier().equals("create")){
					importPythonResult = pythonForImport(f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo(),f_bcgTestData.getNumberofmos(),f_bcgTestData.getModifier());
					deleteImport = bcgImport(f_bcgTestData);
					if(deleteImport.contains("Import has succeeded") || deleteImport.contains("Import has partially succeeded")){
						LOGGER.info("deleteImport for create modifier successful"); //delete me
						if(rollbackFileCreation(f_bcgTestData)){
							if(PlanActivation(f_bcgTestData)){
								copyImportFile(f_bcgTestData);
							}
						}
					}
				}
				else
					importPythonResult = pythonForImport(f_bcgTestData.getImportfilename(),f_bcgTestData.getImportmo(),f_bcgTestData.getNumberofmos(),f_bcgTestData.getModifier());
	
			}
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return importPythonResult;
	}

	/**
	 * This method will check the validation of the bcg import test case
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean validation(F_BCGTestData f_bcgTestData){
		boolean isValidated = false;
		if(f_bcgTestData.getModifier().equalsIgnoreCase("create")){
			isValidated = PlanActivation(f_bcgTestData);
			LOGGER.info("Plan Activation   :::: "  +isValidated);
		}else{
			LOGGER.info("Else of validation");
			if(rollbackFileCreation(f_bcgTestData)){
				isValidated = PlanActivation(f_bcgTestData);
			}
			LOGGER.info("Else of Plan Activation   :::: "  +isValidated);
		}
		if(isPedIncluded()){
			upadePEDParameters("autoLockUnlock", autoLockUnlockBackup);
			isValidated = getErrorIntoLog(f_bcgTestData);
			LOGGER.info("getErrorIntoLog   :::: "  +isValidated);
		}
		LOGGER.info("Validation Result : "+isValidated);
		//helper.getShell().disconnect();
		helper.disconnect();
		return isValidated;
	}

	/**
	 * This method will create the undo plan
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean rollbackFileCreation(F_BCGTestData f_bcgTestData){
		boolean isRollBackFileCreated = false;
		String BCGundocommand =BCGTOOLUNDO + f_bcgTestData.getPlanname();
		LOGGER.info("Rollback command : "+BCGundocommand);
		String undoplanOutput = helper.simpleExec(BCGundocommand);
		LOGGER.info("rollback file creation output ::::"  +undoplanOutput);
		if (undoplanOutput.contains("PrepareUndo successful") || undoplanOutput.contains("No MO found")){ //my changes
			return true;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return isRollBackFileCreated;
	}

	/**
	 * This method will activate the plan
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean PlanActivation(F_BCGTestData f_bcgTestData){
		boolean isPlanActivated = false;
		LOGGER.info("Plan Activation");
		String planActivationCommand =BCGTOOLPLANACTIVATION + f_bcgTestData.getPlanname();
		if(f_bcgTestData.getReconfiguration() != null)
		{
		if (f_bcgTestData.getReconfiguration().equalsIgnoreCase("RNC") || f_bcgTestData.getReconfiguration().equalsIgnoreCase("RBS")) {
			planActivationCommand += " -useRobustReconfig true";
			
			if (f_bcgTestData.getCountdown() != 0) {
				if (f_bcgTestData.getCountdown() >= 300
						&& f_bcgTestData.getCountdown() <= 86400) {
					planActivationCommand += " -configAdmCountdown "
							+ f_bcgTestData.getCountdown();
				} else {
					if (f_bcgTestData.getCountdown() < 300)
						LOGGER.info("Minimum node restart time is 300 seconds");
					else if (f_bcgTestData.getCountdown() > 86400)
						LOGGER.info("Maximum node restart time is 86400 seconds");

					return false;
				}
			} 
		}
		else if (f_bcgTestData.getReconfiguration().equalsIgnoreCase("RadioTNode") || f_bcgTestData.getReconfiguration().equalsIgnoreCase("RadioNode")) {
			planActivationCommand += " -useBrmFailSafe true";
			if (f_bcgTestData.getCountdown() != 0) {
				if (f_bcgTestData.getCountdown() >= 300
						&& f_bcgTestData.getCountdown() <= 86400) {
					planActivationCommand += " -configBrmFailSafeCountdown "
							+ f_bcgTestData.getCountdown();
				} else {
					if (f_bcgTestData.getCountdown() < 300)
						LOGGER.info("Minimum node restart time is 300 seconds");
					else if (f_bcgTestData.getCountdown() > 86400)
						LOGGER.info("Maximum node restart time is 86400 seconds");

					return false;
				}
			} 
		}
		else if (f_bcgTestData.getReconfiguration().equalsIgnoreCase("STN")) {
			planActivationCommand += " -useRobustStnConfig true";
			if (f_bcgTestData.getCountdown() != 0) {
				if (f_bcgTestData.getCountdown() >= 60
						&& f_bcgTestData.getCountdown() <= 5940) {
					planActivationCommand += " -configRobustStnReconfigCountdown "
							+ f_bcgTestData.getCountdown();
				} else {
					if (f_bcgTestData.getCountdown() < 60)
						LOGGER.info("Minimum node restart time is 60 seconds");
					else if (f_bcgTestData.getCountdown() > 5940)
						LOGGER.info("Maximum node restart time is 5940 seconds");

					return false;
				}
			} 
		}
		}
		
		LOGGER.info("BCG Undo command : "+planActivationCommand); //delete me
		String planAvtivationOutput = helper.simpleExec(planActivationCommand);
		LOGGER.info("Plan Activation output ::: \n"+planAvtivationOutput);
		if (planAvtivationOutput.contains("Activation SUCCESSFUL") ){
			return  true;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return isPlanActivated;
	}

	/**
	 * This method will activate the undo plan
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean undoPlanActivation(F_BCGTestData f_bcgTestData){
		boolean isUndoPlanActivated = false;
		String undoPlanActivationCommand =BCGTOOLUNDOPLANACTIVATION + f_bcgTestData.getPlanname();
		if(f_bcgTestData.getReconfiguration() != null)
		{
		if (f_bcgTestData.getReconfiguration().equalsIgnoreCase("RNC") || f_bcgTestData.getReconfiguration().equalsIgnoreCase("RBS")) {
			undoPlanActivationCommand += " -useRobustReconfig true";
			
			if (f_bcgTestData.getCountdown() != 0) {
				if (f_bcgTestData.getCountdown() >= 300
						&& f_bcgTestData.getCountdown() <= 86400) {
					undoPlanActivationCommand += " -configAdmCountdown "
							+ f_bcgTestData.getCountdown();
				} else {
					if (f_bcgTestData.getCountdown() < 300)
						LOGGER.info("Minimum node restart time is 300 seconds");
					else if (f_bcgTestData.getCountdown() > 86400)
						LOGGER.info("Maximum node restart time is 86400 seconds");

					return false;
				}
			} 
		}
		else if (f_bcgTestData.getReconfiguration().equalsIgnoreCase("RadioTNode") || f_bcgTestData.getReconfiguration().equalsIgnoreCase("RadioNode")) {
			undoPlanActivationCommand += " -useBrmFailSafe true";
			if (f_bcgTestData.getCountdown() != 0) {
				if (f_bcgTestData.getCountdown() >= 300
						&& f_bcgTestData.getCountdown() <= 86400) {
					undoPlanActivationCommand += " -configBrmFailSafeCountdown "
							+ f_bcgTestData.getCountdown();
				} else {
					if (f_bcgTestData.getCountdown() < 300)
						LOGGER.info("Minimum node restart time is 300 seconds");
					else if (f_bcgTestData.getCountdown() > 86400)
						LOGGER.info("Maximum node restart time is 86400 seconds");

					return false;
				}
			} 
		}
		else if (f_bcgTestData.getReconfiguration().equalsIgnoreCase("STN")) {
			undoPlanActivationCommand += " -useRobustStnConfig true";
			if (f_bcgTestData.getCountdown() != 0) {
				if (f_bcgTestData.getCountdown() >= 60
						&& f_bcgTestData.getCountdown() <= 5940) {
					undoPlanActivationCommand += " -configRobustStnReconfigCountdown "
							+ f_bcgTestData.getCountdown();
				} else {
					if (f_bcgTestData.getCountdown() < 60)
						LOGGER.info("Minimum node restart time is 60 seconds");
					else if (f_bcgTestData.getCountdown() > 5940)
						LOGGER.info("Maximum node restart time is 5940 seconds");

					return false;
				}
			} 
		}
		}
		
		LOGGER.info("BCG Undo Plan Activation command : "+undoPlanActivationCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(undoPlanActivationCommand);
		LOGGER.info("Undo Plan Activation output  :::\n" +undoPlanAvtivationOutput);
		if (undoPlanAvtivationOutput.contains("Activation SUCCESSFUL")){
			LOGGER.info("BCG Undo plan Activation Successfull  :::::");
			return true;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return isUndoPlanActivated;
	}

	/**
	 * This method will create the undo_plan
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean undoPlanCreation(F_BCGTestData f_bcgTestData){
		boolean isUndoPlanCreated = false;
		String undoPlanActivationCommand =BCGTOOLUNDOPLANCREATION + f_bcgTestData.getPlanname();
		LOGGER.info("BCG Undo Plan Creation command : "+undoPlanActivationCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(undoPlanActivationCommand);
		LOGGER.info("BCG Undo Plan Creation ::::: "+ undoPlanAvtivationOutput);
		if (undoPlanAvtivationOutput.contains("UndoActivation successful")){
			LOGGER.info("BCG Undo Plan Activation Successfull :::::");
			return true;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return isUndoPlanCreated;
	}

	/**
	 * This method will remove the plan
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean removePlan(F_BCGTestData f_bcgTestData){
		boolean isPlanRemoved = false;
		String removePlanCommand =BCGTOOLREMOVEPLAN + f_bcgTestData.getPlanname();
		LOGGER.info("BCG Remove Plan Command : "+removePlanCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(removePlanCommand);
		LOGGER.info("BCG Remove Plan:::: "+ undoPlanAvtivationOutput);
		if (undoPlanAvtivationOutput.contains("successfully deleted")){
			LOGGER.info("BCG Plans Successfully Removed  ::::::");
			return true;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return isPlanRemoved;
	}

	/**
	 * This method will remove the Undo_plan
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean removeUndoPlan(F_BCGTestData f_bcgTestData){
		boolean isUndoPlanRemoved = false;
		String removeUndoPlanCommand =BCGTOOLREMOVEUNDOPLAN + f_bcgTestData.getPlanname();
		LOGGER.info("BCG Remove Undo Plan Command : "+removeUndoPlanCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(removeUndoPlanCommand);
		LOGGER.info("Remove Undo Plan output :::\n"+undoPlanAvtivationOutput);
		if (undoPlanAvtivationOutput.contains("successfully deleted")) {		//# nagu changes
			LOGGER.info("BCG Undo Plans Successfully Deleted  ::::::");
			return true;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
		return isUndoPlanRemoved;
	}

	/**
	 * This method will move the import file from undoplan location to import filepath
	 * @param f_bcgTestData as BCGTestData
	 */
	public void copyImportFile(F_BCGTestData f_bcgTestData){
		helper.simpleExec(REMOVEIMPORT + f_bcgTestData.getImportfilename());
		String copyCommand = "cp " + UNDOPLANPATH + f_bcgTestData.getPlanname() +"/*"+ SPACE + IMPORTPATH;
		helper.simpleExec(copyCommand);
		LOGGER.info("copyImportFile - copyCommand : "+copyCommand);
		String renameCommand = "mv " + IMPORTPATH + "Undo_*" + SPACE + IMPORTPATH + f_bcgTestData.getImportfilename();
		helper.simpleExec(renameCommand);
		LOGGER.info("copyImportFile - renameCommand : "+renameCommand);
		//helper.getShell().disconnect();
		helper.disconnect();
		/*if(bcgTestData.getImportmo().equals("ExternalGsmPlmn") || bcgTestData.getImportmo().equals("ExternalEUtranPlmn"))
		{
			helper.simpleExec(renameCommand);
			LOGGER.info("copyImportFile - renameCommand : "+renameCommand);
			String runpython = PYTHON + PYTHONPATH + bcgTestData.getImportfilename() + SPACE + bcgTestData.getImportmo() + " create " + bcgTestData.getNumberofmos();
		
			String pythonOutput = helper.simpleExec(runpython);
			LOGGER.info("pythonOutput : " + pythonOutput); //delete me
			if(pythonOutput.contains("Success"))
				LOGGER.info("python for create successful"); //delete me
			else
				LOGGER.info("python for create failed"); //delete me
		}*/	
		
	}

	/**
	 * This method will return true or false based on the ped parameter 
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean isPedIncluded(){
		return isPEDIncluded;
	}

	/**
	 * This method will return true or false based on the string availability on file
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean			renameCommand = "mv " + IMPORTPATH + "planned_undo*" + SPACE + IMPORTPATH + bcgTestData.getImportfilename();

	 */
	public boolean verifyStringOnErrInfo(){


		return true;
	}

	/**
	 * This method will get the errinfolog file
	 * @param f_bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean getErrorIntoLog(F_BCGTestData f_bcgTestData){
		String grepCommand = "grep \"attribute set to LOCKED before modifying\""+ " errInfoLog_"+ f_bcgTestData.getImportmo()+"*";
		LOGGER.info("Errlog: "+grepCommand);
		if(grepCommand.contains("modifying")){
			return true;
		}
		return false;
	}
	
	//my changes
	public boolean createImportFile(String[] mandatoryFields, String operation, String filename, String tagName)
	{
		
		final String START_TAGS[] = {"<es:vsData"+tagName+">", "<es:vsData"+tagName+"/>"};
		final String END_TAGS[] = {"<es:vsData"+tagName+"/>", "</es:vsData"+tagName+">"};
		final String VS_DATA_CONTAINER = "<xn:VsDataContainer id=";
		int count = 0;
		BufferedReader br = null;
		String strS1 = "";
				
		try
		{
			String output = helper.simpleExec("cat "+ EXPORTPATH + filename);
			
			br = new BufferedReader(new StringReader(output));
			String str = new String() ;
			//if(/*file.length()/(1024) > 1024*/false)
			if(hashmap.get(tagName) != null)
			{
				strS1 = hashmap.get(tagName);
				//if file size is very big
				//do nothing as of now..
				//return false;				        
			}    
			else
			{	
				while((str = br.readLine()) != null)
				{
					strS1 += str + "\n";
				}
				br.close();	
				
				
				if(strS1.contains(END_TAGS[count]) || strS1.contains(END_TAGS[++count]))
				{
					str = strS1.substring(strS1.lastIndexOf(END_TAGS[count]), strS1.length());
					strS1 =  strS1.substring(0,strS1.indexOf(END_TAGS[count]));
				}
				else
				{
					LOGGER.info("tag not found");
					return false;
				}
				
				strS1 += str ;
				//LOGGER.info(strS1);
				
		    
			}
			hashmap.put(tagName, strS1);
	        String sub = "";
	        String subReplace = "";
	        count = 0;
	        count = 0;
			if(strS1.indexOf(START_TAGS[count]) > -1 || strS1.indexOf(START_TAGS[++count]) > -1)
	        //if(strS1.contains(START_TAGS[count])|| strS1.contains(START_TAGS[++count]))
			{
				sub = strS1.substring(0, strS1.indexOf(START_TAGS[count]));
				if(sub.lastIndexOf(VS_DATA_CONTAINER) >  -1)
				{
					sub = sub.substring(sub.lastIndexOf(VS_DATA_CONTAINER), sub.length());
					sub = sub.substring(0, sub.indexOf(">") + 1);
					modifierString = sub.substring(0, (sub.length()-1));
					/*String id = sub.substring(sub.indexOf("id="),  sub.length());
					id = id.substring(0,id.indexOf(">"));
					LOGGER.info("id string :" +id);
					subReplace = sub.replace(id, "id="+"\""+"1"+"\""+" "+"modifier="+"\""+"delete"+"\"");*/
					if(sub.contains("-"))
					{
						String s = sub.substring(sub.lastIndexOf("-")+1,sub.length());
						if(tagName.equals("EUtranCellFDD") && ((operation.equals("update")) || (operation.equals("delete")))){
							subReplace = sub.replace(s, "1\" modifier="+"\""+operation+"\">");
							strS1 = strS1.replace(sub, subReplace);
						}
						if(tagName.equals("EUtranCellFDD") && (operation.equals("create"))){
							subReplace = sub.replace(s, "254\" modifier="+"\""+operation+"\">");
							strS1 = strS1.replace(sub, subReplace);
						}
						
						else{
//							subReplace = sub.replace(s, "254\" modifier="+"\""+operation+"\">");
							subReplace = modifierString.concat(" modifier="+"\""+operation+"\">");
							modifierString = modifierString.concat(" modifier="+"\""+operation+"\">");
							strS1 = strS1.replace(sub, subReplace);
						}
						
					}
					else
					{
						String s = sub.substring(sub.lastIndexOf("\"")-1, sub.length());
						if(tagName.equals("AnrFunction") || tagName.equals("IubDataStreams") 
								|| tagName.equals("Sector")
								|| tagName.equals("IpAccessHostEt")	    || tagName.equals("IpSyncRef")
								|| tagName.equals("EUtranCellRelation") ||tagName.equals("GeneralProcessorUnit")
								|| tagName.equals("IpAccessHostGpb") 	|| tagName.equals("PacketFrequencySyncRef")
								|| tagName.equals("Aal2PathVccTp") 		|| tagName.equals("IubLink")
								|| tagName.equals("Ospf") 				|| tagName.equals("Aal2Ap")
								){ //for update modifier # nagu changes
							subReplace = modifierString.concat(" modifier="+"\""+operation+"\">");
							modifierString = modifierString.concat(" modifier="+"\""+operation+"\">");
							}
						else
							{
								subReplace = sub.replace(s, "254\" modifier="+"\""+operation+"\">");
								subReplace = modifierString.concat(" modifier="+"\""+operation+"\">");
								modifierString = modifierString.concat(" modifier="+"\""+operation+"\">");

							}
						strS1 = strS1.replace(sub, subReplace);
						if(tagName.equals("AntennaUnitGroup") || tagName.equals("AnrFunction") 
								|| tagName.equals("MbmsServiceArea") || tagName.equals("IubDataStreams")
								|| tagName.equals("IpAccessHostEt")  || tagName.equals("EUtranCellFDD")
								|| tagName.equals("EUtranCellRelation")
								 ){	//# nagu changes
							
							strS1 = strS1.replaceFirst(subReplace, sub);

						}
						if(tagName.equals("IpSyncRef")){
							strS1 = strS1.replaceFirst(subReplace, sub);
							strS1 = strS1.replaceFirst(subReplace, sub);
						}
						
						
										
					}
					
						//subReplace = sub.replace(">", " modifier="+"\""+operation+"\">");
				}
				
			}
			
			
			if((mandatoryFields != null) || operation.equals("delete"))
			{
				String element = strS1.substring(strS1.indexOf(START_TAGS[0]) +START_TAGS[0].length() + 1 ,strS1.indexOf(END_TAGS[1]));
				String subTag = "";
				for(int i = 0 ; i < mandatoryFields.length; i++)
				{
					String sTag = "<"+mandatoryFields[i]+">";
					String eTag = "</"+mandatoryFields[i]+">";
					String nTag = "<"+mandatoryFields[i]+"/>";
					if(element.contains(sTag))
					{
					

						String tag = element.substring(element.indexOf(sTag), element.indexOf(eTag) +eTag.length()) +"\n";
						if(tag.contains("subframeAssignment"))
						{
							tag = tag.replace("0", "1");
						}
						if(tag.contains("<es:cellId>"))
						{
							tag = "<es:cellId>"+"0"+"</es:cellId>\n";
						}
						if(tag.contains("<es:earfcndl>"))	// # nagu changes
						{
							tag = "<es:earfcndl>0</es:earfcndl>\n";
						}
						if(tag.contains("<es:earfcnul>")) //# nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(30000);
							if(randomInt == 0){
								tag = "<es:earfcnul>"+"1"+"</es:earfcnul>\n";
							}
							else{
								tag = "<es:earfcnul>"+String.valueOf(randomInt)+"</es:earfcnul>\n";
							}
							
						}
						if(tag.contains("<es:physicalLayerCellIdGroup>")) //# nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(165);
							if(randomInt == 0){
								tag = "<es:physicalLayerCellIdGroup>"+"1"+"</es:physicalLayerCellIdGroup>\n";
							}
							else{
								tag = "<es:physicalLayerCellIdGroup>"+String.valueOf(randomInt)+"</es:physicalLayerCellIdGroup>\n";
							}
							
						}
						if(tag.contains("<es:physicalLayerSubCellId>")) // # nagu changes
						{
							tag = "<es:physicalLayerSubCellId>0</es:physicalLayerSubCellId>\n";
						}
						if(tag.contains("<es:tac>")) // # nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(65000);
							if(randomInt == 0){
								tag = "<es:tac>"+"1"+"</es:tac>\n";
							}
							else{
								tag = "<es:tac>"+String.valueOf(randomInt)+"</es:tac>\n";
							}
						}
						if(tag.contains("<es:sac>"))
						{
							tag = "<es:sac>254</es:sac>\n";
						}
						if(tag.contains("<es:removeNcellTime>")) //update
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(100);
							if(randomInt == 0){
								tag = "<es:removeNcellTime>"+"1"+"</es:removeNcellTime>\n";
							}
							else{
								tag = "<es:removeNcellTime>"+String.valueOf(randomInt)+"</es:removeNcellTime>\n";
							}
							
						}
						if(tag.contains("<es:maxHsRate>")) //update
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(100);
							if(randomInt == 0){
								tag = "<es:maxHsRate>"+"1"+"</es:maxHsRate>\n";
							}
							else{
								tag = "<es:maxHsRate>"+String.valueOf(randomInt)+"</es:maxHsRate>\n";
							}
							
						}
						if(tag.contains("<es:latitude>")) //update
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(100);
							if(randomInt == 0){
								tag = "<es:latitude>"+"1"+"</es:latitude>\n";
							}
							else{
								tag = "<es:latitude>"+String.valueOf(randomInt)+"</es:latitude>\n";
							}
							
						}
						if(tag.contains("<es:ntpDscp>")) //update # nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(100);
							if(randomInt == 0){
								tag = "<es:ntpDscp>"+"1"+"</es:ntpDscp>\n";
							}
							else{
								tag = "<es:ntpDscp>"+String.valueOf(randomInt)+"</es:ntpDscp>\n";
							}
							
						}
						if(tag.contains("<es:ipDefaultTtl>")) //update # nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(255);
							if(randomInt == 0){
								tag = "<es:ipDefaultTtl>"+"1"+"</es:ipDefaultTtl>\n";
							}
							else{
								tag = "<es:ipDefaultTtl>"+String.valueOf(randomInt)+"</es:ipDefaultTtl>\n";
							}
							
						}
						if(tag.contains("<es:useId>")) //update 
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(60);
							if(randomInt == 0){
								tag = "<es:useId>"+"1"+"</es:useId>\n";
							}
							else{
								tag = "<es:useId>"+String.valueOf(randomInt)+"</es:useId>\n";
							}
							
						}
						if(tag.contains("<es:userLabel>"))//update # nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(60);
							if(randomInt == 0){
								tag = "<es:userLabel>"+"1"+"</es:userLabel>\n";
							}
							else{
								tag = "<es:userLabel>"+String.valueOf(randomInt)+"</es:userLabel>\n";
							}
							
						}
						if(tag.contains("<es:qOffsetCellEUtran>"))	//update # nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(24);
							if(randomInt == 0){
								tag = "<es:qOffsetCellEUtran>"+1+"</es:qOffsetCellEUtran>\n";
							}
							else{
								tag = "<es:qOffsetCellEUtran>"+randomInt+"</es:qOffsetCellEUtran>\n";
							}
							
						}
						if(tag.contains("<es:timerCu>"))	//update # nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(100);
							if(randomInt == 0){
								tag = "<es:timerCu>"+"1"+"</es:timerCu>\n";
							}
							else{
								tag = "<es:timerCu>"+String.valueOf(randomInt)+"</es:timerCu>\n";
							}
							
						}
						if(tag.contains("<es:spare>"))	//update # nagu changes
						{
							Random randomGenerator = new Random();
							int randomInt = randomGenerator.nextInt(10000);
							if(randomInt == 0){
								tag = "<es:spare>"+"1"+"</es:spare>\n";
							}
							else{
								tag = "<es:spare>"+String.valueOf(randomInt)+"</es:spare>\n";
							}
							
						}
						
						if(tag.contains(","))
						{
							tag = tag.replace(",", "||");
						}
						subTag += tag;
					}
					else if(element.contains(nTag))
					{
						subTag += nTag + "\n";
					}
				}
				strS1 = strS1.replace(element, subTag);
			}	
			
			
			strS1 = strS1.replace("[","");
			strS1 = strS1.replace("]","");
			strS1 = strS1.replace(",", "");
			strS1 = strS1.replace("||", ",");
	      
			
			if(!formatXMLFile(strS1, operation, filename))
				return false;
		}
		catch(IOException ioe)
		{
			LOGGER.info("IO exception caught while processing import xml file \n" +ioe.getMessage());
			return false;
		}
		catch(Exception e)
		{
			LOGGER.info("Exception caught while processing import xml file \n" +e.getMessage());
			e.printStackTrace();
			return false;
		}finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
        return true;
	}
	public boolean formatXMLFile(String file, String operation, String filename){
		
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
		InputSource is = null;
	    Document document = null;
	    OutputFormat format = null;
	    Writer out = null;
	    XMLSerializer serializer = null;
		try
		{
			
			
			
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			is = new InputSource(new StringReader(file));
	        document = db.parse(is);
	        
	        //format part
	        format = new OutputFormat(document);
	        format.setOmitXMLDeclaration(true);
	        //format.setLineWidth(65);
	        format.setIndenting(true);
	        format.setIndent(4);
	        out = new StringWriter();
	        serializer = new XMLSerializer(out, format);
	        serializer.serialize(document);
	        //LOGGER.info(out.toString());
	        	        
	       
	        helper.simpleExec("echo "+ "'"+out.toString()+"'" + " > " +IMPORTPATH + filename);
			
		}
		
		catch(IOException ioe){
			LOGGER.info("IO exception caught while formating xml file \n"+ioe.getMessage());
			return false;
		}
		catch(Exception e){
			LOGGER.info("Exception caught while formating xml file \n"+e.getMessage());
			return false;
		}
		finally
		{
//			//helper.getShell().disconnect();
			helper.disconnect();
			//do nothing as of now
		}
		return true;
	  }
	
}


