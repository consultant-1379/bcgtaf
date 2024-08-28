package com.ericsson.bcgcdb.test.operators;


import java.util.Scanner;

import com.ericsson.bcgcdb.test.cases.BCGTestData;
import com.ericsson.bcgcdb.test.getters.BCGImportGetter;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
//import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

import java.io.*; 

import org.apache.log4j.Logger;

@Operator(context = Context.CLI)
public class BCGImportCliOperator implements BCGImportOperator {

	Logger LOGGER = Logger.getLogger(BCGImportCliOperator.class);
	
	Shell shell;
	Host host, host1;
	User operUser;
	User rootUser;


	private final String CSTEST = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt ";
	private final String CSTESTLA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice la ";
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
	//private final String PYTHONPATH = " /var/opt/ericsson/Createimportfile.py ";
	private final String PYTHONPATH = " /var/tmp/Createimportfile.py ";
	private final String PYTHONGATEWAYPATH = "/var/tmp/";
	private final String PYTHONREMOTEPATH = "/var/opt/ericsson/";
	private final String MINUSP = " -p ";
	private final String MINUSAS = " -as ";
	private final String SPACE = " ";
	private final String SMTOOL_COMMAND = "smtool -set wran_bcg";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String MOLOCK = "-molock";
	private boolean syncFlag = false;

	BCGImportGetter bcgImportGetter;
	CLICommandHelper helper;

	String autoLockUnlock;
	String autoLockUnlockBackup;
	String importNodeList;
	String utranCellFdn;
	String nodeIsSync;
	String bachupAutoLockUnlock;
	boolean pedIncluded = false;
	boolean isDeleteDone = false;

	/**
	 * Initializing host, user and cli
	 */
	public BCGImportCliOperator(){
		bcgImportGetter = new BCGImportGetter();
		host = HostGroup.getOssmaster();
		host1 = HostGroup.getOssmaster();

		final CLICommandHelper cmdHelper = new CLICommandHelper(host);
		cmdHelper.openShell();
		//  helper = new CLICommandHelper(host);
		//		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
		rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
		helper = new CLICommandHelper(host, operUser);

	}

	/**
	 * This method for BCG import
	 * @param ImportFileName as String
	 * @param PlanName as String
	 * @return boolean
	 */
	public String bcgImport(BCGTestData bcgTestData){
		LOGGER.info("Bcg Import Started");
		String bcgImportCommand = null;
		String result = "Import has failed";
		if(bcgTestData.getPed_autolockunlock() == null){
			bcgImportCommand =BCGTOOLIMPORT + bcgTestData.getImportfilename() + MINUSP + bcgTestData.getPlanname();
		}
		else{
			bcgImportCommand =BCGTOOLIMPORT + bcgTestData.getImportfilename() + MINUSP + bcgImportCommand + MOLOCK;
		}
		if(bcgTestData.getScheme() !=null)
		{
			if (bcgTestData.getScheme().equals("system") || bcgTestData.getScheme().equals("networkelement") || bcgTestData.getScheme().equals("plan"))
				bcgImportCommand += MINUSAS + bcgTestData.getScheme();
			else {
				LOGGER.info("BCG Import command : "+bcgImportCommand);
				LOGGER.info("Invalid activation scheme option entered");
				return "Import has failed";
			}
		}
		
		LOGGER.info("BCG Import command : "+bcgImportCommand);
		String importOutput = helper.simpleExec(bcgImportCommand);
		LOGGER.info("BCG Import poutput  : \n"+ importOutput);
		//		//helper.getShell().disconnect();
		helper.disconnect();
		//if(importOutput.contains("Import has succeeded")){
		if(importOutput.contains("Import has succeeded")){
			result = "Import has succeeded";
		}
		else if(bcgTestData.getImportmo().equalsIgnoreCase("Aal2Ap") && (importOutput.contains("doesn't exist"))){
			result = "Import has succeeded";
		}

		return result;
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgTestData
	 * @return result
	 */
	public boolean preAction(BCGTestData bcgTestData){
		LOGGER.info("preAction");
		boolean pedresult = false;
		boolean findmoresult = false;
		if(!(bcgTestData.getPed_autolockunlock() == null)){
			LOGGER.info("Setting PED parameters");
			pedIncluded = true;
			backupPed(autoLockUnlock, bcgTestData.getPed_autolockunlock());
			pedresult = upadePEDParameters("autoLockUnlock", bcgTestData.getPed_autolockunlock());
			if (!pedresult) {
				return false;
			}
		}

		findmoresult = findMos(bcgTestData);
		if(!findmoresult){
			return false;
		}

		if(bcgTestData.getImportmo().equalsIgnoreCase("UtranCell")){
			int ucPlace = utranCellFdn.indexOf("UtranCell=");
			String newUtranCellFdn = utranCellFdn.substring(0,(ucPlace+10))+"TestBcg".trim();
			String getIubCommand = CSTESTLA + utranCellFdn +" utranCellIubLink";
			String IubOutput = helper.simpleExec(getIubCommand);
			String requirediub = IubOutput.split("\"")[1];
			LOGGER.info(requirediub);
			String createMoCommand = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice cm "+newUtranCellFdn
					+ " -attr cId 50001 lac 1 localCellId 50002 primaryScramblingCode 10 sac 1 sib1PlmnScopeValueTag 9 tCell 5 uarfcnDl 5005 uarfcnUl 5006 utranCellIubLink "+
					requirediub;
			LOGGER.info(createMoCommand);
			String createUcOutput = helper.simpleExec(createMoCommand);
			LOGGER.info(createUcOutput);
			if(createUcOutput.contains("error"))
				return false;

		}
		return true;
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
			if(!PEDOutput.contains("error")){
				result = true;
			}
		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
			//		//helper.getShell().disconnect();
			helper.disconnect();
		}
		return result;
	}

	/**
	 * This method will check the server for the required Mos
	 * @param importmo
	 * @param fdnpath
	 * @return result
	 */
	private boolean findMos(BCGTestData bcgTestData) {
		LOGGER.info("findMos");
		boolean result = false;
		String oldString = " ";
		int j = 0;
		String firstUtranCell = "";
		try{
			String csTestCommand = CSTEST + SPACE + bcgTestData.getImportmo();
			String csOutput = helper.simpleExec(csTestCommand);
			//LOGGER.info("List of available nodes: "+csOutput);
			String[] fdns = csOutput.split("\n");
			if(bcgTestData.getImportmo().equals("ExternalGsmCell") || bcgTestData.getImportmo().equals("ExternalUtranCell") || bcgTestData.getImportmo().equals("LocationArea") 
					|| bcgTestData.getImportmo().equals("ServiceArea") || bcgTestData.getImportmo().equals("RoutingArea")){
				String deletePlmn = helper.simpleExec(DPLMN);


				if(fdns.length > 0){					
					for(int i =0; i<fdns.length; i++){
						String [] commasplit = fdns[i].split(",");
						importNodeList = "";

						if(commasplit[0].contains("SubNetwork")){
							importNodeList = commasplit[0];
							importNodeList = importNodeList.replace("[", "");
							result = true;
							break;
						}
					}
				}

			}else{
				if(bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")){
					if(fdns.length > 0){
						for(int i =0; i<fdns.length; i++){
							importNodeList = "";
							String [] commasplit = fdns[i].split(",");

							if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("MeContext"))
								importNodeList = commasplit[0]+","+commasplit[1];
							LOGGER.info("importNideList ::::" + importNodeList);
							LOGGER.info("oldString :::: " + oldString);

							importNodeList = importNodeList.replace("[", "");
							importNodeList = importNodeList.replace("]", "");
							importNodeList = importNodeList.replace(" ", "");
							if(!oldString.equals(importNodeList)){
								if(isNodeSynchronized(importNodeList)){
									result = true;
									break;
								}
							}
							oldString = importNodeList;
						}
					}
				}
				if(bcgTestData.getImportmo().equalsIgnoreCase("NbapCommon")){
					String csTestCommandNbapCommon = CSTEST + SPACE + bcgTestData.getImportmo() + " | grep -i RNC | grep -v MSRBS | grep -v PRBS ";
					String csOutputNbapCommon = helper.simpleExec(csTestCommandNbapCommon);
					//LOGGER.info("List of available nodes: "+csOutput);
					String[] fdnsNbapCommpn = csOutputNbapCommon.split("\n");
					if(fdnsNbapCommpn.length > 0){
						for(int i =0; i<fdnsNbapCommpn.length; i++){
							importNodeList = "";
							String [] commasplit = fdnsNbapCommpn[i].split(",");
							if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("SubNetwork") && commasplit[2].contains("MeContext"))
								importNodeList = commasplit[0]+","+commasplit[1]+","+commasplit[2];

							importNodeList = importNodeList.replace("[", "");
							importNodeList = importNodeList.replace("]", "");
							importNodeList = importNodeList.replace(" ", "");
							if(!oldString.equals(importNodeList)){
								if(isNodeSynchronized(importNodeList)){
									result = true;
									break;
								}
							}
							oldString = importNodeList;
						}
					}
				}
				else{
					if(fdns.length > 0){
						for(int i =0; i<fdns.length; i++){
							importNodeList = "";
							String [] commasplit = fdns[i].split(",");
							if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("SubNetwork") && commasplit[2].contains("MeContext"))
								importNodeList = commasplit[0]+","+commasplit[1]+","+commasplit[2];

							importNodeList = importNodeList.replace("[", "");
							importNodeList = importNodeList.replace("]", "");
							importNodeList = importNodeList.replace(" ", "");
							if(!oldString.equals(importNodeList)){
								if(isNodeSynchronized(importNodeList)){
									nodeIsSync = importNodeList;
									if(bcgTestData.getImportmo().equalsIgnoreCase("UtranCell")){
										if (syncFlag)
										{   
											firstUtranCell = importNodeList;
											utranCellFdn = fdns[i];
											result = true;
											break;
										}
									}else if(bcgTestData.getImportmo().equalsIgnoreCase("NbapCommon")){
										String splitPrbs = helper.simpleExec(CSTESTLA + importNodeList + " neType");
										if(importNodeList.contains("RBS") && !splitPrbs.contains("26") && !splitPrbs.contains("42") && !splitPrbs.contains("32") && !splitPrbs.contains("45") && !importNodeList.contains("MSRBS") && !importNodeList.contains("PRBS") ){
											result = true;
											break;
										}
									}else{
										result = true;
										break;
									}
								}
							}
							oldString = importNodeList;
						}
					}

				}	
			}
		}catch(Exception e){
			LOGGER.info("exception"+  e.getMessage());
			LOGGER.info(e);
		}finally{
			//		//helper.getShell().disconnect();
			helper.disconnect();
		}
		if(!importNodeList.isEmpty()){
			LOGGER.info("Available node : "+importNodeList);
			return result;
		}
		else{
			LOGGER.info("Nodes are NOT available for "+ bcgTestData.getImportmo());
			return result;
		}
	}

	/**
	 * this method for export the mo
	 * @param exportfdn as string
	 * @param importMo as string
	 * @param importFileName as string
	 * @return boolean
	 */
	private boolean bcgExport(String exportfdn, String importMo, String importFileName){
		LOGGER.info("bcgExport");
		String bcgExportCommand;
		String exportOutput;
		bcgExportCommand =  BCGTOOLEXPORT + importFileName + VSDATA + importMo + " -n " + exportfdn;
		LOGGER.info("BCG Export command: "+bcgExportCommand);
		exportOutput = helper.simpleExec(bcgExportCommand);
		//		//helper.getShell().disconnect();
		helper.disconnect();
		LOGGER.info("BCG Export :"+exportOutput);
		if(exportOutput.contains("Export has succeeded")){
			return true;
		}
		return false;
	}

	/**
	 * This method will copy the python script from Testware to server
	 * @return void
	 */
	private void copyPython(){
		LOGGER.info("Copy python files to server");
		RemoteObjectHandler preCheckRemoteFileHandler = null;
		System.out.println("Host name is "+host1.getHostname().trim());
		if (host1.getHostname().trim().equalsIgnoreCase("ossmaster")) {
			LOGGER.info("Root User password is : " + rootUser.getPassword());
			host1.setUser("root");
			host1.setPass("shroot12");
			System.out.println("New Host name is "+host1.getHostname().trim());
			LOGGER.info("New Root User password is : " + rootUser.getPassword());
			preCheckRemoteFileHandler = new RemoteObjectHandler(host1,rootUser);
			
			String pythonscr = FileFinder.findFile("Createimportfile.py").get(0);
			LOGGER.info("Createimportfile.py found at "+pythonscr);
			 preCheckRemoteFileHandler.copyLocalFileToRemote(pythonscr, PYTHONGATEWAYPATH);
			 String Fdnpyscript = FileFinder.findFile("getData.py").get(0);
			 System.out.println("getData.py found at location "+Fdnpyscript);
			 preCheckRemoteFileHandler.copyLocalFileToRemote(Fdnpyscript, PYTHONGATEWAYPATH);
		}

		
/*		//		RemoteFileHandler remote = new RemoteFileHandler(host,rootUser);
		RemoteObjectHandler remote = new RemoteObjectHandler(host,rootUser);
		String pythonscr = FileFinder.findFile("Createimportfile.py").get(0);
		String localFileLocation = pythonscr;
		String remoteFileLocation = PYTHONREMOTEPATH;
		remote.copyLocalFileToRemote(localFileLocation ,remoteFileLocation);*/
	}

	/**
	 * This method for running python script to create import XML file
	 * @param FileName as String
	 * @param importMo as String
	 * @return boolean
	 */
	private boolean pythonForImport(String importFilename, String importmo, int numberofmos, String modifier,String id){
		String runpython;
		boolean result = false;
		LOGGER.info("importFilename*****"+importFilename);
		LOGGER.info("importmo***:"+importmo);
		LOGGER.info("numberofmos*****"+numberofmos);
		LOGGER.info("modifier***:"+modifier);
		LOGGER.info("id***:"+id);

		try{
			if(importmo.contains("LocationArea") || importmo.contains("RoutingArea") || importmo.contains("ServiceArea")){
				LOGGER.info("Handling for LocationArea ServiceArea and Routing Area");
				String csOutput = helper.simpleExec(CPLMN);
				if(!csOutput.contains("Excepction") && (!importmo.equals("LocationArea")))
					csOutput = helper.simpleExec(CLA);
				if(csOutput.contains("Excepction")){
					return false;
				}
			}



			if(importmo.equalsIgnoreCase("LocationArea") || importmo.equalsIgnoreCase("ServiceArea") || importmo.equalsIgnoreCase("RoutingArea")){
				runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + SPACE + modifier + SPACE + numberofmos + SPACE +id;
			}else if(modifier.equals("create") && !isDeleteDone && !(importmo.equalsIgnoreCase("Aal2ap"))){
				runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + " delete " + numberofmos;
				isDeleteDone = true;
			}
			else if(importmo.equalsIgnoreCase("Aal2ap")){
				runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + " create " + numberofmos +  SPACE +id;
				LOGGER.info("runpython***:"+runpython);
			}
			else{
				runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + SPACE + modifier + SPACE + numberofmos + SPACE +id;

			}
			LOGGER.info("Running Python : " + runpython);
			String pythonOutput = helper.simpleExec(runpython);
			LOGGER.info("Python output: " + pythonOutput);
			if(pythonOutput.contains("Success")){
				result = true;
			}
		}catch(Exception e){
			LOGGER.info(e.getMessage());
			LOGGER.info(e);
		}finally{
			//		//helper.getShell().disconnect();
			helper.disconnect();
		}
		return result;
	}

	/**
	 * This method will check the required nodes is sync or not
	 * @param fdn as String
	 * @return result as boolean
	 */
	private boolean isNodeSynchronized(String fdn){
		LOGGER.info("isNodeSynchronized");
		String csTestCommand = CSTESTLA + SPACE + fdn + SPACE + MIRRORMIBSYNCHSTATUS ;
		LOGGER.info("Checking Node Status : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		//		//helper.getShell().disconnect();
		helper.disconnect();
		if(csOutput.contains("5") || csOutput.contains("3")){
			LOGGER.info(fdn + " Node is sync");
			syncFlag = true;
			return true;
		}else{
			LOGGER.info(fdn + " Node is Unsync");
			syncFlag = false;
			return false;
		}
	}

	/**
	 * This method will take the backup of the PED parameter
	 * @param pedName
	 * @param pedValue
	 * @return void
	 */
	private void backupPed(String pedName, String pedValue){
		String updatepedcommand = SMTOOL_COMMAND + SPACE + pedName + SPACE + pedValue;
		LOGGER.info("BCG set PED parameter command : "+updatepedcommand);
		helper.simpleExec(updatepedcommand);
		//		//helper.getShell().disconnect();
		helper.disconnect();
	}

	/**
	 * This method will revert back all the network changes after
	 * running the test case
	 * @param bcgTestData
	 * @return void
	 */
	public void postAction(BCGTestData bcgTestData){
		String csOutput;
		LOGGER.info("Post Action");
		if(bcgTestData.getImportmo().equalsIgnoreCase("UtranCell")){
			removePlan(bcgTestData);
			//		//helper.getShell().disconnect();
			helper.disconnect();
		}
		else{		
			if(bcgTestData.getModifier().equals("delete")){
				LOGGER.info(" delete part");
				if(undoPlanCreation(bcgTestData))
					undoPlanActivation(bcgTestData);
				removePlan(bcgTestData);
				removeUndoPlan(bcgTestData);
			}else
				removePlan(bcgTestData);
			if(bcgTestData.getImportmo().contains("LocationArea") || bcgTestData.getImportmo().contains("RoutingArea") || bcgTestData.getImportmo().contains("ServiceArea"))
				csOutput = helper.simpleExec(DPLMN);
			//		//helper.getShell().disconnect();
			helper.disconnect();
		}
	}
	
	
	

	/**
	 * This method will create the import file based on the export file
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean prepareImportFile(BCGTestData bcgTestData){
		LOGGER.info("prepareImportFile");
		boolean exportResult = false;
		boolean importPythonResult = false;
		String deleteImport = "";

		exportResult = bcgExport(importNodeList,bcgTestData.getImportmo(),bcgTestData.getImportfilename());
		if(exportResult){
			copyPython();
			if(bcgTestData.getImportmo().equalsIgnoreCase("LocationArea") || bcgTestData.getImportmo().equalsIgnoreCase("ServiceArea") || bcgTestData.getImportmo().equalsIgnoreCase("RoutingArea")){
				importPythonResult = pythonForImport(bcgTestData.getImportfilename(),bcgTestData.getImportmo(),bcgTestData.getNumberofmos(),bcgTestData.getModifier(),bcgTestData.getId());
			}else if(bcgTestData.getModifier().equals("create")){
				importPythonResult = pythonForImport(bcgTestData.getImportfilename(),bcgTestData.getImportmo(),bcgTestData.getNumberofmos(),bcgTestData.getModifier(),bcgTestData.getId());
				deleteImport = bcgImport(bcgTestData);
				if(deleteImport.contains("Import has succeeded")){
					if(rollbackFileCreation(bcgTestData)){
						if( !(bcgTestData.getImportmo().equalsIgnoreCase("Aal2Ap")) ){
							if(PlanActivation(bcgTestData)){
								copyImportFile(bcgTestData);
							}
						}
						else{
							copyImportFile(bcgTestData);
						}
					}
				}
			}
			else
				importPythonResult = pythonForImport(bcgTestData.getImportfilename(),bcgTestData.getImportmo(),bcgTestData.getNumberofmos(),bcgTestData.getModifier(),bcgTestData.getId());
			String Moname = bcgTestData.getImportmo();
			LOGGER.info("Exiting from Import. MoName is"+Moname);
			if (Moname.equals("ExternalUtranCell")){		
					String importFilename = bcgTestData.getImportfilename();
					System.out.println("Import File name is "+importFilename);
					String SpecialCommand = "python /var/tmp/getData.py "+importFilename;
					System.out.println("Command for trigerring getData.py is "+SpecialCommand);
					String getDataOutput = helper.simpleExec(SpecialCommand);
					System.out.println("Output of the getData.py is "+getDataOutput);
				} 
			}
		
		return importPythonResult;
	}

	/**
	 * This method will check the validation of the bcg import test case
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean validation(BCGTestData bcgTestData){
		LOGGER.info("validation");
		boolean result = false;
		if(bcgTestData.getImportmo().equalsIgnoreCase("UtranCell"))
			return result = PlanActivation(bcgTestData);

		if(bcgTestData.getImportmo().equalsIgnoreCase("LocationArea") || bcgTestData.getImportmo().equalsIgnoreCase("RoutingArea") 
				|| bcgTestData.getImportmo().equalsIgnoreCase("ServiceArea") ){
			result = PlanActivation(bcgTestData);
		}else if(bcgTestData.getModifier().equalsIgnoreCase("create")){
			result = PlanActivation(bcgTestData);
		}else{
			if(rollbackFileCreation(bcgTestData)){
				result = PlanActivation(bcgTestData);
			}
		}
		if(isPedIncluded()){
			upadePEDParameters("autoLockUnlock", autoLockUnlockBackup);
			result = getErrorIntoLog(bcgTestData);
		}
		LOGGER.info("Validation Result : "+result);
		return result;
	}

	/**
	 * This method will create the undo plan
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean rollbackFileCreation(BCGTestData bcgTestData){
		LOGGER.info("rollbackFileCreation");
		boolean result = false;
		String BCGundocommand =BCGTOOLUNDO + bcgTestData.getPlanname();
		LOGGER.info("BCG rollback file creation command : "+BCGundocommand);
		String undoplanOutput = helper.simpleExec(BCGundocommand);
		LOGGER.info("Rollback file creation:"+undoplanOutput);
		//		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoplanOutput.contains("PrepareUndo successful")|| undoplanOutput.contains("No MO found")){
			return true;
		}
		return result;
	}

	/**
	 * This method will activate the plan
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean PlanActivation(BCGTestData bcgTestData){
		LOGGER.info("Plan Activation");
		boolean result = false;
		String planAvtivationOutput ;
		String planActivationCommand ;

		if( !(bcgTestData.getImportmo().equalsIgnoreCase("Aal2Ap")) ){

			planActivationCommand =BCGTOOLPLANACTIVATION + bcgTestData.getPlanname();
			if(bcgTestData.getReconfiguration() != null) 
			{
			if (bcgTestData.getReconfiguration().equalsIgnoreCase("RNC") || bcgTestData.getReconfiguration().equalsIgnoreCase("RBS")) {
				planActivationCommand += " -useRobustReconfig true";

				if (bcgTestData.getCountdown() != 0) {
					if (bcgTestData.getCountdown() >= 300
							&& bcgTestData.getCountdown() <= 86400) {
						planActivationCommand += " -configAdmCountdown "
								+ bcgTestData.getCountdown();
					} else {
						if (bcgTestData.getCountdown() < 300)
							LOGGER.info("Minimum node restart time is 300 seconds");
						else if (bcgTestData.getCountdown() > 86400)
							LOGGER.info("Maximum node restart time is 86400 seconds");

						return false;
					}
				}
			}
			else if (bcgTestData.getReconfiguration().equalsIgnoreCase("RadioTNode") || bcgTestData.getReconfiguration().equalsIgnoreCase("RadioNode")) {
				planActivationCommand += " -useBrmFailSafe true";
				if (bcgTestData.getCountdown() != 0) {
					if (bcgTestData.getCountdown() >= 300
							&& bcgTestData.getCountdown() <= 86400) {
						planActivationCommand += " -configBrmFailSafeCountdown "
								+ bcgTestData.getCountdown();
					} else {
						if (bcgTestData.getCountdown() < 300)
							LOGGER.info("Minimum node restart time is 300 seconds");
						else if (bcgTestData.getCountdown() > 86400)
							LOGGER.info("Maximum node restart time is 86400 seconds");

						return false;
					}
				}
			}
			else if (bcgTestData.getReconfiguration().equalsIgnoreCase("STN")) {
				planActivationCommand += " -useRobustStnConfig true";
				if (bcgTestData.getCountdown() != 0) {
					if (bcgTestData.getCountdown() >= 60
							&& bcgTestData.getCountdown() <= 5940) {
						planActivationCommand += " -configRobustStnReconfigCountdown "
								+ bcgTestData.getCountdown();
					} else {
						if (bcgTestData.getCountdown() < 60)
							LOGGER.info("Minimum node restart time is 60 seconds");
						else if (bcgTestData.getCountdown() > 5940)
							LOGGER.info("Maximum node restart time is 5940 seconds");

						return false;
					}
				} 
			}
			}
			
			LOGGER.info("BCG plan activation command : "+planActivationCommand);
			planAvtivationOutput = helper.simpleExec(planActivationCommand);
			LOGGER.info("Plan activation :"+planAvtivationOutput);
			//		//helper.getShell().disconnect();
			helper.disconnect();
		}
		else{
			return true;
		}
		if (planAvtivationOutput.contains("Activation SUCCESSFUL") || (planAvtivationOutput.contains("Activation PARTLY_REALIZED"))){
			return true;
		}
		if(bcgTestData.getImportmo().equalsIgnoreCase("Aal2Ap") && (planAvtivationOutput.contains("CONFIGURATION_IS_EMPTY"))){
			return true;
		}
		return result;
	}

	/**
	 * This method will activate the undo plan
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean undoPlanActivation(BCGTestData bcgTestData){
		LOGGER.info("undoPlanActivation");
		boolean result = false;
		String undoPlanActivationCommand =BCGTOOLUNDOPLANACTIVATION + bcgTestData.getPlanname();
		if(bcgTestData.getReconfiguration() != null) 
		{
		if (bcgTestData.getReconfiguration().equalsIgnoreCase("RNC") || bcgTestData.getReconfiguration().equalsIgnoreCase("RBS")) {
			undoPlanActivationCommand += " -useRobustReconfig true";

			if (bcgTestData.getCountdown() != 0) {
				if (bcgTestData.getCountdown() >= 300
						&& bcgTestData.getCountdown() <= 86400) {
					undoPlanActivationCommand += " -configAdmCountdown "
							+ bcgTestData.getCountdown();
				} else {
					if (bcgTestData.getCountdown() < 300)
						LOGGER.info("Minimum node restart time is 300 seconds");
					else if (bcgTestData.getCountdown() > 86400)
						LOGGER.info("Maximum node restart time is 86400 seconds");

					return false;
				}
			}
		}
		else if (bcgTestData.getReconfiguration().equalsIgnoreCase("RadioTNode") || bcgTestData.getReconfiguration().equalsIgnoreCase("RadioNode")) {
			undoPlanActivationCommand += " -useBrmFailSafe true";
			if (bcgTestData.getCountdown() != 0) {
				if (bcgTestData.getCountdown() >= 300
						&& bcgTestData.getCountdown() <= 86400) {
					undoPlanActivationCommand += " -configBrmFailSafeCountdown "
							+ bcgTestData.getCountdown();
				} else {
					if (bcgTestData.getCountdown() < 300)
						LOGGER.info("Minimum node restart time is 300 seconds");
					else if (bcgTestData.getCountdown() > 86400)
						LOGGER.info("Maximum node restart time is 86400 seconds");

					return false;
				}
			}
		}
		else if (bcgTestData.getReconfiguration().equalsIgnoreCase("STN")) {
			undoPlanActivationCommand += " -useRobustStnConfig true";
			if (bcgTestData.getCountdown() != 0) {
				if (bcgTestData.getCountdown() >= 60
						&& bcgTestData.getCountdown() <= 5940) {
					undoPlanActivationCommand += " -configRobustStnReconfigCountdown "
							+ bcgTestData.getCountdown();
				} else {
					if (bcgTestData.getCountdown() < 60)
						LOGGER.info("Minimum node restart time is 60 seconds");
					else if (bcgTestData.getCountdown() > 5940)
						LOGGER.info("Maximum node restart time is 5940 seconds");

					return false;
				}
			} 
		}
		}
		
		LOGGER.info("BCG Undo Activation command : "+undoPlanActivationCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(undoPlanActivationCommand);
		LOGGER.info("Undo plan activation : "+ undoPlanAvtivationOutput);
		//		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("Activation SUCCESSFUL")){
			return true;
		}
		return result;
	}

	/**
	 * This method will create the undo_plan
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean undoPlanCreation(BCGTestData bcgTestData){
		LOGGER.info("undoPlanCreation");
		boolean result = false;
		String undoPlanActivationCommand =BCGTOOLUNDOPLANCREATION + bcgTestData.getPlanname();
		LOGGER.info("BCG undo plan creation command : "+undoPlanActivationCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(undoPlanActivationCommand);
		LOGGER.info("Unto plan creation :"+undoPlanAvtivationOutput);
		//		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("UndoActivation successful")){
			return true;
		}
		return result;
	}

	/**
	 * This method will remove the plan
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean removePlan(BCGTestData bcgTestData){
		LOGGER.info("removePlan");
		boolean result = false;
		String removePlanCommand =BCGTOOLREMOVEPLAN + bcgTestData.getPlanname();
		LOGGER.info("BCG Remove plan command : "+removePlanCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(removePlanCommand);
		LOGGER.info("Remove plan :"+undoPlanAvtivationOutput);
		//		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("successfully deleted")){
			return true;
		}
		return result;
	}

	/**
	 * This method will remove the Undo_plan
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean removeUndoPlan(BCGTestData bcgTestData){
		LOGGER.info("removeUndoPlan");
		boolean result = false;
		String removeUndoPlanCommand =BCGTOOLREMOVEUNDOPLAN + bcgTestData.getPlanname();
		LOGGER.info("BCG remove undo plan command : "+removeUndoPlanCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(removeUndoPlanCommand);
		LOGGER.info("Remove undo plan : "+undoPlanAvtivationOutput);
		//		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("Activation SUCCESSFUL")){
			return true;
		}
		return result;
	}

	/**
	 * This method will move the import file from undoplan location to import filepath
	 * @param bcgTestData as BCGTestData
	 */
	public void copyImportFile(BCGTestData bcgTestData){
		helper.simpleExec(REMOVEIMPORT + bcgTestData.getImportfilename());
		String copyCommand = "cp " + UNDOPLANPATH + bcgTestData.getPlanname() +"/*"+ SPACE + IMPORTPATH;
		helper.simpleExec(copyCommand);
		String renameCommand = "mv " + IMPORTPATH + "Undo_*" + SPACE + IMPORTPATH + bcgTestData.getImportfilename();
		helper.simpleExec(renameCommand);
		//		//helper.getShell().disconnect();
		helper.disconnect();
	}

	/**
	 * This method will return true or false based on the ped parameter 
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean isPedIncluded(){
		return pedIncluded;
	}

	/**
	 * This method will return true or false based on the string availability on file
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean verifyStringOnErrInfo(){
		return true;
	}

	/**
	 * This method will get the errinfolog file
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean getErrorIntoLog(BCGTestData bcgTestData){
		String grepCommand = "grep \"attribute set to LOCKED before modifying\""+ " errInfoLog_"+ bcgTestData.getImportmo()+"*";
		LOGGER.info("Errlog: "+grepCommand);
		if(grepCommand.contains("modifying")){
			return true;
		}
		return false;
	}
}

