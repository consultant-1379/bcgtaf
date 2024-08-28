

package com.ericsson.bcgcdb.test.operators;

import org.apache.log4j.Logger;

import com.ericsson.bcgcdb.test.cases.BCGKGBImportTestData;
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

@Operator(context = Context.CLI)
public class BCGKGBImportCliOperator implements BCGKgbImportOperator {

	Logger LOGGER = Logger.getLogger(BCGKGBImportCliOperator.class);
	Shell shell;
	Host host,host1;
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
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String VSDATA = " -d :vsData";
	private final String PYTHON = "python";
	private final String PYTHONPATH = " /var/tmp/Createimportfile.py ";
	private final String PYTHONGATEWAYPATH = "/var/tmp/";
	private final String PYTHONREMOTEPATH = "/var/opt/ericsson/";
//	private final String PYTHONPATH = " /home/nmsadm/Createimportfile.py ";
//	private final String PYTHONREMOTEPATH = "/home/nmsadm/";
	private final String MINUSP = " -p ";
	private final String SPACE = " ";
	private final String SMTOOL_COMMAND = "smtool -set wran_bcg";
	private final String SYNCHRONISATIONSTATUS = "synchronisationProgress";
	private final String MOLOCK = " -molock";

	BCGImportGetter bCGimportGetter;
	CLICommandHelper helper;
	String autoLockUnlock;
	String autoLockUnlockBackup;
	String nodeList;
	String bachupAutoLockUnlock;
	boolean pedIncluded = false;
	boolean isDeleteDone = false;

	/**
	 * Initializing host, user and cli
	 */
	public BCGKGBImportCliOperator(){
		bCGimportGetter = new BCGImportGetter();
		host = HostGroup.getOssmaster();
		
		host1 = HostGroup.getOssmaster();

		final CLICommandHelper cmdHelper = new CLICommandHelper(host);
		cmdHelper.openShell();
//	    helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
        rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
//		//	cli = new CLI(host, operUser);
		helper = new CLICommandHelper(host, operUser);
	}

	/**
	 * This method for BCG import
	 * @param ImportFileName as String
	 * @param PlanName as String
	 * @return boolean
	 */
	public String bcgImport(BCGKGBImportTestData bcgKgbImportTestData){
		LOGGER.info("BCGIMPORT");
		String BCGimportcommand = null;
		if(bcgKgbImportTestData.getPed_autolockunlock() == null){
			BCGimportcommand =BCGTOOLIMPORT + bcgKgbImportTestData.getImportfilename() + MINUSP + bcgKgbImportTestData.getPlanname();
		}
		else{
			BCGimportcommand =BCGTOOLIMPORT + bcgKgbImportTestData.getImportfilename() + MINUSP + bcgKgbImportTestData.getPlanname() + MOLOCK;
		}
		LOGGER.info("BCG Import command : "+BCGimportcommand);
		LOGGER.info("Import Command : "+BCGimportcommand);
		String importOutput = helper.simpleExec(BCGimportcommand);
		LOGGER.info("BCG Import output   ::: \n" +importOutput);
		//if(importOutput.contains("Export has succeeded")){
		//helper.getShell().disconnect();
		helper.disconnect();
		if(importOutput.contains("Import has succeeded") || importOutput.contains("Import has partially succeeded")){
			return "Import has succeeded";
		}
		else{
			return "Import has failed";
		}
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgKgbImportTestDatat
	 * @return result
	 */
	public boolean preAction(BCGKGBImportTestData bcgKgbImportTestDatat){
		LOGGER.info("***preAction***");
		boolean pedresult = false;
		boolean findmoresult = false;
		if(!(bcgKgbImportTestDatat.getPed_autolockunlock() == null)){
			LOGGER.info("###Setting PED parameters");
			pedIncluded = true;
			backupPed(autoLockUnlock, bcgKgbImportTestDatat.getPed_autolockunlock());
			pedresult = upadePEDParameters("autoLockUnlock", bcgKgbImportTestDatat.getPed_autolockunlock());
			if (!pedresult) {
				return false;
			}
		}
		if(bcgKgbImportTestDatat.getImportmo().contains("LocationArea") || bcgKgbImportTestDatat.getImportmo().contains("RoutingArea") || bcgKgbImportTestDatat.getImportmo().contains("ServiceArea")){
			LOGGER.info("Handling for LocationArea ServiceArea and Routing Area");
			String csOutput = helper.simpleExec(CPLMN);
			if(!csOutput.contains("Excepction") && ( (bcgKgbImportTestDatat.getImportmo().equalsIgnoreCase("ServiceArea") || 
					                                  bcgKgbImportTestDatat.getImportmo().equalsIgnoreCase("RoutingArea") ) )){
				csOutput = helper.simpleExec(CLA);
			}
			LOGGER.info(CLA);
//			//helper.getShell().disconnect();
			helper.disconnect();
			if(csOutput.contains("Excepction")){
				return false;
			}
			findmoresult = findMos(bcgKgbImportTestDatat);
			if(!findmoresult){
				return false;
			}
		}
		else{
			findmoresult = findMos(bcgKgbImportTestDatat);
			if(!findmoresult){
				return false;
			}
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
		LOGGER.info("setting PED parameters: ");
		String updatepedcommand = SMTOOL_COMMAND + SPACE + pedName + SPACE + pedValue;
		LOGGER.info("BCG set PED parameter command : "+updatepedcommand);
		LOGGER.info(updatepedcommand);
		String PEDOutput = helper.simpleExec(updatepedcommand);
		LOGGER.info("BCG set PED parameter command : "+PEDOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if(!PEDOutput.contains("error")){
			result = true;
		}
		return result;
	}

	/**
	 * This method will check the server for the required Mos
	 * @param importmo
	 * @param fdnpath
	 * @return result
	 */
	private boolean findMos(BCGKGBImportTestData bcgkgbImportTestData) {
		boolean result = false;
		String oldString = " ";
		String csTestCommand = CSTEST + SPACE + bcgkgbImportTestData.getImportmo();
		LOGGER.info("BCG Find MO call command : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		String[] fdns = csOutput.split("\n");
		if(bcgkgbImportTestData.getImportmo().equals("ExternalGsmCell") || bcgkgbImportTestData.getImportmo().equals("ExternalUtranCell") || bcgkgbImportTestData.getImportmo().equals("LocationArea") 
				|| bcgkgbImportTestData.getImportmo().equals("ServiceArea") || bcgkgbImportTestData.getImportmo().equals("RoutingArea")){

			if(fdns.length > 0){
				for(int i =0; i<fdns.length; i++){
					String [] commasplit = fdns[i].split(",");
					nodeList = "";

					if(commasplit[0].contains("SubNetwork")){
						nodeList = commasplit[0];
						nodeList = nodeList.replace("[", "");
						result = true;
						break;
					}
				}
			}

		}else{
			if(bcgkgbImportTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
				if(fdns.length > 0){
					for(int i =0; i<fdns.length; i++){
						nodeList = "";
						String [] commasplit = fdns[i].split(",");

							if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("SubNetwork") && commasplit[2].contains("MeContext")){
								nodeList = commasplit[0]+","+commasplit[1]+","+commasplit[2];
								nodeList = nodeList.replace("[", "");
								nodeList = nodeList.replace("]", "");
								nodeList = nodeList.replace(" ", "");
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
			}
			else{
				if(fdns.length > 0){
					for(int i =0; i<fdns.length; i++){
						nodeList = "";
						String [] commasplit = fdns[i].split(",");
						if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("MeContext")){
							nodeList = commasplit[0]+","+commasplit[1];
							nodeList = nodeList.replace("[", "");
							nodeList = nodeList.replace("]", "");
							nodeList = nodeList.replace(" ", "");
							
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
			}	
		}
		LOGGER.info("node : "+nodeList);
		LOGGER.info("result : "+result);
		//helper.getShell().disconnect();
		helper.disconnect();
		if(nodeList.isEmpty()){
			LOGGER.info("No Nodes are available");
		}
		else{
			LOGGER.info("Available Node : "+nodeList);
		}
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
		LOGGER.info("BCG Export  output :: \n "+exportOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
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
		
		RemoteObjectHandler preCheckRemoteFileHandler = null;
		if (host1.getHostname().trim().equalsIgnoreCase("ossmaster")) {
			//host1.setUser("root");
			//host1.setPass("shroot12");
			LOGGER.info("Root USer password is : " + rootUser.getPassword());
			preCheckRemoteFileHandler = new RemoteObjectHandler(host1,rootUser);
			
			String pythonscr = FileFinder.findFile("Createimportfile.py").get(0);

			 preCheckRemoteFileHandler.copyLocalFileToRemote(pythonscr, PYTHONGATEWAYPATH);
		}

		
		
	}
/*
//		RemoteFileHandler remote = new RemoteFileHandler(host,rootUser);
		RemoteObjectHandler remote = new RemoteObjectHandler(host,rootUser);
		String pythonscr = FileFinder.findFile("Createimportfile.py").get(0);
		String localFileLocation = pythonscr;
		String remoteFileLocation = PYTHONREMOTEPATH;
		remote.copyLocalFileToRemote(localFileLocation ,remoteFileLocation);
	}*/

	/**
	 * This method for running python script to create import XML file
	 * @param FileName as String
	 * @param importMo as String
	 * @return boolean
	 */
	private boolean pythonForImport(String importFilename, String importmo, int numberofmos, String modifier,String id){
		String runpython = null;
		if(importmo.equalsIgnoreCase("LocationArea") || importmo.equalsIgnoreCase("ServiceArea") || importmo.equalsIgnoreCase("RoutingArea")){
			runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + SPACE + modifier + SPACE + numberofmos + SPACE + id;
		}else if(modifier.equals("create") && !isDeleteDone){
			runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + SPACE + modifier + SPACE + numberofmos + SPACE + id;
			isDeleteDone = true;
		}
		else{
			runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + SPACE + modifier + SPACE + numberofmos + SPACE + id;
		}
		LOGGER.info("Running Python : " + runpython);
		String pythonOutput = helper.simpleExec(runpython);
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
		LOGGER.info("******isNodeSynchronized******");
		boolean result = false;
		String csTestCommand = CSTESTLA + SPACE + fdn + SPACE + MIRRORMIBSYNCHSTATUS ;
		LOGGER.info("CHecking Node Status : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		//helper.getShell().disconnect();
		helper.disconnect();
		if(csOutput.contains("5") || csOutput.contains("3")){
			result = true;
		}
		if(!result){
			LOGGER.info(fdn+" Node Is unsync");
		}
		return result;
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
		//helper.getShell().disconnect();
		helper.disconnect();
		//	autoLockUnlockBackup = ;
	}

	/**
	 * This method will revert back all the network changes after
	 * running the test case
	 * @param bcgkgbImportTestData
	 * @return void
	 */
	public void postAction(BCGKGBImportTestData bcgkgbImportTestData){
		String csOutput;
		LOGGER.info("Post Actrion: ");
		if(bcgkgbImportTestData.getModifier().equals("delete")){
			LOGGER.info(" delete part");
			if(undoPlanCreation(bcgkgbImportTestData))
				undoPlanActivation(bcgkgbImportTestData);
			removePlan(bcgkgbImportTestData);
			removeUndoPlan(bcgkgbImportTestData);
		}else
			removePlan(bcgkgbImportTestData);
		if(bcgkgbImportTestData.getImportmo().contains("LocationArea") || bcgkgbImportTestData.getImportmo().contains("RoutingArea") || bcgkgbImportTestData.getImportmo().contains("ServiceArea"))
			csOutput = helper.simpleExec(DPLMN);
	}

	/**
	 * This method will create the import file based on the export file
	 * @param bcgkgbImportTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean prepareImportFile(BCGKGBImportTestData bcgkgbImportTestData){

		boolean exportResult = false;
		boolean importPythonResult = false;
		String deleteImport = "";

		exportResult = bcgExport(nodeList,bcgkgbImportTestData.getImportmo(),bcgkgbImportTestData.getImportfilename());
		if(exportResult){
			copyPython();
			if(bcgkgbImportTestData.getImportmo().equalsIgnoreCase("LocationArea") || bcgkgbImportTestData.getImportmo().equalsIgnoreCase("ServiceArea") || bcgkgbImportTestData.getImportmo().equalsIgnoreCase("RoutingArea")){
				importPythonResult = pythonForImport(bcgkgbImportTestData.getImportfilename(),bcgkgbImportTestData.getImportmo(),bcgkgbImportTestData.getNumberofmos(),bcgkgbImportTestData.getModifier(),bcgkgbImportTestData.getid());
			}else if(bcgkgbImportTestData.getModifier().equals("create")){
				importPythonResult = pythonForImport(bcgkgbImportTestData.getImportfilename(),bcgkgbImportTestData.getImportmo(),bcgkgbImportTestData.getNumberofmos(),bcgkgbImportTestData.getModifier(),bcgkgbImportTestData.getid());
				deleteImport = bcgImport(bcgkgbImportTestData);
				if(deleteImport.contains("Import has succeeded") || deleteImport.contains("Import has partially succeeded")){
					if(rollbackFileCreation(bcgkgbImportTestData)){
						if(PlanActivation(bcgkgbImportTestData)){
							copyImportFile(bcgkgbImportTestData);
						}
					}
				}
			}
			else{
				importPythonResult = pythonForImport(bcgkgbImportTestData.getImportfilename(),bcgkgbImportTestData.getImportmo(),bcgkgbImportTestData.getNumberofmos(),bcgkgbImportTestData.getModifier(),bcgkgbImportTestData.getid());
			}
		}
		return importPythonResult;
	}

	/**
	 * This method will check the validation of the bcg import test case
	 * @param bcgkgbImportTestData as BCGTestData
	 * @return result as boolean
	 */
	public boolean validation(BCGKGBImportTestData bcgkgbImportTestData){
		boolean result = false;
		if(bcgkgbImportTestData.getModifier().equalsIgnoreCase("create")){
			result = PlanActivation(bcgkgbImportTestData);
		}else{
			LOGGER.info("Else of validation");
			if(rollbackFileCreation(bcgkgbImportTestData)){
				result = PlanActivation(bcgkgbImportTestData);
			}
		}
		if(isPedIncluded()){
			upadePEDParameters("autoLockUnlock", autoLockUnlockBackup);
			result = getErrorIntoLog(bcgkgbImportTestData);
		}
		LOGGER.info("Validation Result : "+result);
		return result;
	}

	/**
	 * This method will create the undo plan
	 * @param bcgkgbImportTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean rollbackFileCreation(BCGKGBImportTestData bcgkgbImportTestData){
		boolean result = false;
		String BCGundocommand =BCGTOOLUNDO + bcgkgbImportTestData.getPlanname();
		LOGGER.info("BCG Undo command : "+BCGundocommand);
		LOGGER.info("Rollback command : "+BCGundocommand);
		String undoplanOutput = helper.simpleExec(BCGundocommand);
		LOGGER.info("rollback file creation ::: \n "+undoplanOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoplanOutput.contains("PrepareUndo successful") || undoplanOutput.contains("No MO found")) {
			return true;
		}
		return result;
	}

	/**
	 * This method will activate the plan
	 * @param bcgkgbImportTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean PlanActivation(BCGKGBImportTestData bcgkgbImportTestData){
		boolean result = false;
		LOGGER.info("Plan Activation");
		String planActivationCommand =BCGTOOLPLANACTIVATION + bcgkgbImportTestData.getPlanname();
		LOGGER.info("BCG Undo command : "+planActivationCommand);
		String planAvtivationOutput = helper.simpleExec(planActivationCommand);
		LOGGER.info("Plan Activation  ::: \n"+ planAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (planAvtivationOutput.contains("Activation SUCCESSFUL")){
			return true;
		}
		return result;
	}

	/**
	 * This method will activate the undo plan
	 * @param bcgkgbImportTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean undoPlanActivation(BCGKGBImportTestData bcgkgbImportTestData){
		boolean result = false;
		String undoPlanActivationCommand =BCGTOOLUNDOPLANACTIVATION + bcgkgbImportTestData.getPlanname();
		LOGGER.info("BCG Undo Activation command : "+undoPlanActivationCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(undoPlanActivationCommand);
		LOGGER.info("undo Plan Activation  :::" +undoPlanAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("Activation SUCCESSFUL")){
			return true;
		}
		return result;
	}

	/**
	 * This method will create the undo_plan
	 * @param bcgkgbImportTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean undoPlanCreation(BCGKGBImportTestData bcgkgbImportTestData){
		boolean result = false;
		String undoPlanActivationCommand =BCGTOOLUNDOPLANCREATION + bcgkgbImportTestData.getPlanname();
		LOGGER.info("BCG Undo Activation command : "+undoPlanActivationCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(undoPlanActivationCommand);
		LOGGER.info("undo plan creation :::: \n"  +undoPlanAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("UndoActivation successful")){
			return true;
		}
		return result;
	}

	/**
	 * This method will remove the plan
	 * @param bcgkgbImportTestData as TestData
	 * @return result as boolean
	 */
	private boolean removePlan(BCGKGBImportTestData bcgkgbImportTestData){
		boolean result = false;
		String removePlanCommand =BCGTOOLREMOVEPLAN + bcgkgbImportTestData.getPlanname();
		LOGGER.info("BCG Remove plan command : "+removePlanCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(removePlanCommand);
		LOGGER.info("remove plan   :: "+ undoPlanAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("successfully deleted")){
			return true;
		}
		return result;
	}

	/**
	 * This method will remove the Undo_plan
	 * @param bcgkgbImportTestData as BCGTestData
	 * @return result as boolean
	 */
	private boolean removeUndoPlan(BCGKGBImportTestData bcgkgbImportTestData){
		boolean result = false;
		String removeUndoPlanCommand =BCGTOOLREMOVEUNDOPLAN + bcgkgbImportTestData.getPlanname();
		LOGGER.info("BCG Remove plan command : "+removeUndoPlanCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(removeUndoPlanCommand);
		LOGGER.info("remove undo plan  ::: "+ undoPlanAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("successfully deleted")){
			return true;
		}
		return result;
	}

	/**
	 * This method will move the import file from undoplan location to import filepath
	 * @param bcgkgbImportTestData as BCGTestData
	 */
	public void copyImportFile(BCGKGBImportTestData bcgkgbImportTestData){
		helper.simpleExec(REMOVEIMPORT + bcgkgbImportTestData.getImportfilename());
		String copyCommand = "cp " + UNDOPLANPATH + bcgkgbImportTestData.getPlanname() +"/*"+ SPACE + IMPORTPATH;
		helper.simpleExec(copyCommand);
		String renameCommand = "mv " + IMPORTPATH + "Undo_*" + SPACE + IMPORTPATH + bcgkgbImportTestData.getImportfilename();
		helper.simpleExec(renameCommand);
		//helper.getShell().disconnect();
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
	 * @param bcgkgbImportTestDatae as BCGTestData
	 * @return result as boolean
	 */
	public boolean getErrorIntoLog(BCGKGBImportTestData bcgkgbImportTestDatae){
		String grepCommand = "grep \"attribute set to LOCKED before modifying\""+ " errInfoLog_"+ bcgkgbImportTestDatae.getImportmo()+"*";
		LOGGER.info("Errlog: "+grepCommand);
		if(grepCommand.contains("modifying")){
			return true;
		}
		return false;
	}
}


