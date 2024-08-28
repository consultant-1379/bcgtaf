package com.ericsson.bcgcdb.test.operators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.ericsson.bcgcdb.test.cases.S_BCGTestData;
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
public class S_BCGImportCliOperator implements S_BCGImportOperator {

	protected static final Logger LOGGER = Logger.getLogger(S_BCGImportCliOperator.class);

	Shell shell;
	Host host, host1;
	User operUser;

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
	//private final String PYTHONPATH = " /home/nmsadm/Createimportfile.py ";
	//private final String PYTHONREMOTEPATH = "/home/nmsadm/";
	private final String PYTHONPATH = " /var/tmp/Createimportfile.py ";
	private final String PYTHONGATEWAYPATH = "/var/tmp/";
	private final String PYTHONREMOTEPATH = "/var/opt/ericsson/";
	private final String MINUSP = " -p ";
	private final String MINUSAS = " -as ";
	private final String SPACE = " ";
	private final String SMTOOL_COMMAND = "smtool -set wran_bcg";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String MOLOCK = "-molock";
	private final String EXPORTPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/"; //my changes
	private final String ECHO = "echo"; //my changes - has to be /bin/echo
	private final String CAT = "cat"; //my changes - has to be /bin/cat
	private final String NECHECK = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt MeContext -f neType==";
	BCGImportGetter bCGimportGetter;
	//Logger LOGGER ;//Logger LOGGER = Logger.getLogger("com.ericsson.bcgcdb.test.operators.S_BCGImportCliOperator");
	CLICommandHelper helper;

	String autoLockUnlock;
	String autoLockUnlockBackup;
	String nodeList;
	String bachupAutoLockUnlock;
	boolean pedIncluded = false;
	boolean isDeleteDone = false;
	//my changes
	static Map<String,String> hashmap = new HashMap<String,String>();
	static Map<String,Integer> idMap = new HashMap<String,Integer>();

	/**
	 * Initializing host, user and cli
	 */
	public S_BCGImportCliOperator(){
		bCGimportGetter = new BCGImportGetter();
		host = HostGroup.getOssmaster();
		host1 = HostGroup.getOssmaster();
		final CLICommandHelper cmdHelper = new CLICommandHelper(host);
		cmdHelper.openShell();
//	    helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
//		//	cli = new CLI(host, operUser);
		helper = new CLICommandHelper(host, operUser);
	}

	/**
	 * This method for BCG import
	 * @param ImportFileName as String
	 * @param PlanName as String
	 * @return boolean
	 */
	public String bcgImport(S_BCGTestData s_bcgTestData){
		System.out.println("BCGIMPORT");
		String BCGimportcommand = null;
		if(s_bcgTestData.getPed_autolockunlock() == null){
			BCGimportcommand =BCGTOOLIMPORT + s_bcgTestData.getImportfilename() + MINUSP + s_bcgTestData.getPlanname();
		}
		else{
			BCGimportcommand =BCGTOOLIMPORT + s_bcgTestData.getImportfilename() + MINUSP + s_bcgTestData.getPlanname() + MOLOCK;
		}
		if(s_bcgTestData.getScheme() !=null)
		{
			if (s_bcgTestData.getScheme().equals("system") || s_bcgTestData.getScheme().equals("networkelement") || s_bcgTestData.getScheme().equals("plan"))
				BCGimportcommand += MINUSAS + s_bcgTestData.getScheme();
			else {
				LOGGER.info("BCG Import command : "+BCGimportcommand);
				LOGGER.info("Invalid activation scheme option entered");
				return "Import has failed";
			}
		}
		LOGGER.info("BCG Import command : "+BCGimportcommand);
		String importOutput = helper.simpleExec(BCGimportcommand); //actual code
		//my code
		//String importOutput = null;
		/*if(s_bcgTestData.getMimName() != null)
		{
			BCGimportcommand += " -as plan";
			importOutput = helper.simpleExec(BCGimportcommand); 
		}
		else
		{
			BCGimportcommand += " -as plan";
			importOutput = helper.simpleExec(BCGimportcommand);
		}*/
		//ends
		LOGGER.info("Import Command : "+BCGimportcommand);
		LOGGER.info("Import Output : "+importOutput); //delete me
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
	 * @param s_bcgTestData
	 * @return result
	 */
	public boolean preAction(S_BCGTestData s_bcgTestData){
		LOGGER.info("***preAction***");
		boolean pedresult = false;
		boolean findmoresult = false;
		if(!(s_bcgTestData.getPed_autolockunlock() == null)){
			LOGGER.info("###Setting PED parameters");
			pedIncluded = true;
			backupPed(autoLockUnlock, s_bcgTestData.getPed_autolockunlock());
			pedresult = upadePEDParameters("autoLockUnlock", s_bcgTestData.getPed_autolockunlock());
			if (!pedresult) {
				return false;
			}
		}
		if(s_bcgTestData.getImportmo().contains("LocationArea") || s_bcgTestData.getImportmo().contains("RoutingArea") || s_bcgTestData.getImportmo().contains("ServiceArea")){
			LOGGER.info("Handling for LocationArea ServiceArea and Routing Area");
			String csOutput = helper.simpleExec(CPLMN);
			if(!csOutput.contains("Excepction") && (!s_bcgTestData.getImportmo().equals("LocationArea"))){
				csOutput = helper.simpleExec(CLA);
			}
//			//helper.getShell().disconnect();
			helper.disconnect();
			if(csOutput.contains("Excepction")){
				return false;
			}
			findmoresult = findMos(s_bcgTestData);
			if(!findmoresult){
				return false;
			}
		}
		else{
			findmoresult = findMos(s_bcgTestData);
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
	private boolean findMos(S_BCGTestData s_bcgTestData) {
		boolean result = false;
		String oldString = " ";
		String[] fdns;
		//ne type check
		if(s_bcgTestData.getImportnodetype() != null){
			String csTestNECommand = NECHECK + s_bcgTestData.getImportnodetype();
			LOGGER.info("BCG Find MO call command : "+csTestNECommand.trim());
			String csOutput = helper.simpleExec(csTestNECommand.trim());
			LOGGER.info("Necheck csOutput is "+csOutput);
			fdns = csOutput.split("\n");
			LOGGER.info("nodes from CS : "+fdns);
		}else{
			String csTestCommand = CSTEST + SPACE + s_bcgTestData.getImportmo();
			LOGGER.info("BCG Find MO call command : "+csTestCommand.trim());
			String csOutput = helper.simpleExec(csTestCommand.trim());
			LOGGER.info("csOutput is "+csOutput);
			fdns = csOutput.split("\n");
			LOGGER.info("nodes from CS : "+fdns);

		}
		//my changes
		if(s_bcgTestData.getImportmo().equals("ExternalGsmCell") || s_bcgTestData.getImportmo().equals("ExternalUtranCell") 
				|| s_bcgTestData.getImportmo().equals("LocationArea") || s_bcgTestData.getImportmo().equals("ServiceArea") || s_bcgTestData.getImportmo().equals("RoutingArea") 
				|| s_bcgTestData.getImportmo().equals("ExternalGsmPlmn") || s_bcgTestData.getImportmo().equals("ExternalEUtranPlmn")
				|| s_bcgTestData.getImportmo().equals("MbmsServiceArea") || s_bcgTestData.getImportmo().equals("ExternalEUtranCellTDD") 
				|| s_bcgTestData.getImportmo().equals("Schema") || s_bcgTestData.getImportmo().equals("FileM") 
				|| s_bcgTestData.getImportmo().equals("SecM") || s_bcgTestData.getImportmo().equals("SwM") 
				|| s_bcgTestData.getImportmo().equals("PmJob") || s_bcgTestData.getImportmo().equals("BrM") 
				|| s_bcgTestData.getImportmo().equals("ExternalEUtranCellFDD") || s_bcgTestData.getImportmo().equals("Fm") 
				|| s_bcgTestData.getImportmo().equals("Pm") || s_bcgTestData.getImportmo().equals("CertM")
				|| s_bcgTestData.getImportmo().equals("FtpTls") || s_bcgTestData.getImportmo().equals("HwIM") 
				|| s_bcgTestData.getImportmo().equals("Lm") || s_bcgTestData.getImportmo().equals("SwInventory") 
				|| s_bcgTestData.getImportmo().equals("SystemFunctions") || s_bcgTestData.getImportmo().equals("PmEventM")
				|| s_bcgTestData.getImportmo().equals("BfdProfile") || s_bcgTestData.getImportmo().equals("BfdSessionIPv4")
				|| s_bcgTestData.getImportmo().equals("EthernetPort") || s_bcgTestData.getImportmo().equals("Host")
				|| s_bcgTestData.getImportmo().equals("InterfaceIPv4") || s_bcgTestData.getImportmo().equals("Router")
				|| s_bcgTestData.getImportmo().equals("Shaper") || s_bcgTestData.getImportmo().equals("PeerIPv4")
				|| s_bcgTestData.getImportmo().equals("RouteTableIPv4Static") || s_bcgTestData.getImportmo().equals("Xossx_MtasFunction")
				|| s_bcgTestData.getImportmo().equals("HSS-Function")){

			if(fdns.length > 0){
				if(s_bcgTestData.getImportnodetype() != null){
					LOGGER.info("Inside NE type");
					nodeList = "";
					nodeList = fdns[0];
					result = true;
				}else{
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
			}

		}else{
			if(s_bcgTestData.getImportnodefdn().equalsIgnoreCase("SubNetwork,MeContext")){
				if(fdns.length > 0){
					for(int i =0; i<fdns.length; i++){
						nodeList = "";
						String [] commasplit = fdns[i].split(",");
						if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("MeContext"))
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
			else{
				if(fdns.length > 0){
					for(int i =0; i<fdns.length; i++){
						nodeList = "";
						String [] commasplit = fdns[i].split(",");
						if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("SubNetwork") && commasplit[2].contains("MeContext"))
							nodeList = commasplit[0]+","+commasplit[1]+","+commasplit[2];

						nodeList = nodeList.replace("[", "");
						nodeList = nodeList.replace("]", "");
						nodeList = nodeList.replace(" ", "");
						if(!oldString.equals(nodeList)){
							if(isNodeSynchronized(nodeList)){
								if(s_bcgTestData.getImportmo().equalsIgnoreCase("IubDataStreams")){
									String splitPrbs = helper.simpleExec(CSTESTLA + nodeList + " neType");
									if(nodeList.contains("RBS") && !splitPrbs.contains("26")){
										result = true;
										break;
									}
								}else{
									result = true;
									break;
								}						
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
		if(importMo.equals("HSS-Function")){
			bcgExportCommand =  BCGTOOLEXPORT + importFileName + VSDATA + "HSS_Function" + " -n " + exportfdn;
		}else{
			bcgExportCommand =  BCGTOOLEXPORT + importFileName + VSDATA + importMo + " -n " + exportfdn;
		}		
		LOGGER.info("BCG Export command: "+bcgExportCommand);
		exportOutput = helper.simpleExec(bcgExportCommand);
		LOGGER.info("BCG Export output - "+exportOutput);
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
				
		LOGGER.info("Copying Python files");
//		RemoteFileHandler remote = new RemoteFileHandler(host);
		RemoteObjectHandler remote = new RemoteObjectHandler(host);
		String pythonscr = FileFinder.findFile("Createimportfile.py").get(0);
		String localFileLocation = pythonscr;
		String remoteFileLocation = PYTHONGATEWAYPATH;
		remote.copyLocalFileToRemote(localFileLocation ,remoteFileLocation);
	}

	/**
	 * This method for running python script to create import XML file
	 * @param FileName as String
	 * @param importMo as String
	 * @return boolean
	 */
	private boolean pythonForImport(String importFilename, String importmo, int numberofmos, String modifier){
		String runpython;
		if(modifier.equals("create") && !isDeleteDone){
			runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + " delete " + numberofmos;
			isDeleteDone = true;
		}
		else{
			runpython = PYTHON + PYTHONPATH + importFilename + SPACE + importmo + SPACE + modifier + SPACE + numberofmos;
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
		LOGGER.info("*Checking sysnc status*");
		boolean result = false;
		String csTestCommand = CSTESTLA + SPACE + fdn + SPACE + MIRRORMIBSYNCHSTATUS ;
		LOGGER.info("Checking Node Status : "+csTestCommand);
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
	 * @param s_bcgTestData
	 * @return void
	 */
	public void postAction(S_BCGTestData s_bcgTestData){
		String csOutput;
		LOGGER.info("*Post ACtion of test case");
		if(s_bcgTestData.getModifier().equals("delete")){
			LOGGER.info(" delete part");
			if(undoPlanCreation(s_bcgTestData))
				undoPlanActivation(s_bcgTestData);
			removePlan(s_bcgTestData);
			removeUndoPlan(s_bcgTestData);
		}else
			removePlan(s_bcgTestData);
		if(s_bcgTestData.getImportmo().contains("LocationArea") || s_bcgTestData.getImportmo().contains("RoutingArea") || s_bcgTestData.getImportmo().contains("ServiceArea"))
			csOutput = helper.simpleExec(DPLMN);
	}

	/**
	 * This method will create the import file based on the export file
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	public boolean prepareImportFile(S_BCGTestData s_bcgTestData){

		boolean exportResult = false;
		boolean importPythonResult = false;
		String deleteImport = "";

		exportResult = bcgExport(nodeList,s_bcgTestData.getImportmo(),s_bcgTestData.getImportfilename());

		//my changes
		if(s_bcgTestData.getImportmo().equals("EUtranCellTDD") || s_bcgTestData.getImportmo().equals("AntennaUnitGroup") 
				|| s_bcgTestData.getImportmo().equals("ExternalGsmPlmn") || s_bcgTestData.getImportmo().equals("ExternalEUtranPlmn") 
				|| s_bcgTestData.getImportmo().equals("AnrFunction") || s_bcgTestData.getImportmo().equals("MbmsServiceArea")
				|| s_bcgTestData.getImportmo().equals("IubDataStreams") || s_bcgTestData.getImportmo().equals("Sector") 
				|| s_bcgTestData.getImportmo().equals("ExternalEUtranCellTDD") || s_bcgTestData.getImportmo().equals("ExternalEUtranCellFDD")
				|| s_bcgTestData.getImportmo().equals("IpSystem") || s_bcgTestData.getImportmo().equals("GeneralProcessorUnit") 
				|| /*s_bcgTestData.getImportmo().equals("EUtranCellFDD") ||*/ s_bcgTestData.getImportmo().equals("Schema") 
				||  s_bcgTestData.getImportmo().equals("Equipment") || s_bcgTestData.getImportmo().equals("SwM")
				|| s_bcgTestData.getImportmo().equals("BrM") ||  s_bcgTestData.getImportmo().equals("FileM") 
				|| s_bcgTestData.getImportmo().equals("SecM")|| s_bcgTestData.getImportmo().equals("PmJob")
				|| s_bcgTestData.getImportmo().equals("EUtranCellRelation") || s_bcgTestData.getImportmo().equals("Fm") 
				|| s_bcgTestData.getImportmo().equals("Pm") || s_bcgTestData.getImportmo().equals("CertM")
				|| s_bcgTestData.getImportmo().equals("FtpTls") || s_bcgTestData.getImportmo().equals("HwIM") 
				|| s_bcgTestData.getImportmo().equals("Lm") || s_bcgTestData.getImportmo().equals("SwInventory") 
				|| s_bcgTestData.getImportmo().equals("SystemFunctions") || s_bcgTestData.getImportmo().equals("PmEventM")
				|| s_bcgTestData.getImportmo().equals("Snmp") || s_bcgTestData.getImportmo().equals("TimeM")
				|| s_bcgTestData.getImportmo().equals("BfdProfile") || s_bcgTestData.getImportmo().equals("BfdSessionIPv4")
				|| s_bcgTestData.getImportmo().equals("EthernetPort") || s_bcgTestData.getImportmo().equals("Host")
				|| s_bcgTestData.getImportmo().equals("InterfaceIPv4") || s_bcgTestData.getImportmo().equals("Router")
				|| s_bcgTestData.getImportmo().equals("Shaper") || s_bcgTestData.getImportmo().equals("PeerIPv4")
				|| s_bcgTestData.getImportmo().equals("RouteTableIPv4Static") || s_bcgTestData.getImportmo().equals("Xossx_MtasFunction")
				|| s_bcgTestData.getImportmo().equals("HSS-Function"))

		{
			boolean result = false;	
			if(exportResult){
				//mandatory fields for delete/create modifier
				String EUtranCellTDD_MF[] = {"es:frameStartOffset","es:earfcn","es:cellId","es:physicalLayerCellIdGroup",
						"es:physicalLayerSubCellId","es:tac","es:subframeAssignment","es:sectorFunctionRef"};
				String ExternalEUtranPLMN_MF[] = {"es:plmnIdentity"};
				String MbmsServiceArea_MF[] = {"es:sac"};
				String EUtranCellRelation_MF[] ={"es:qOffsetCellEUtran"};
				String ExternalEUtranCellTDD_MF[] = {"es:localCellId", "es:physicalLayerCellIdGroup", "es:physicalLayerSubCellId", "es:tac",  "es:earfcn"};
				String ExternalEUtranCellFDD_MF[] = {"es:localCellId", "es:physicalLayerCellIdGroup", "es:physicalLayerSubCellId", "es:tac",  "es:activePlmnList", "es:earfcndl"};
				LOGGER.info("UPDATE MODIFIER FIELDS = "+s_bcgTestData.getUpdateFields());	
				if(s_bcgTestData.getImportmo().equals("EUtranCellTDD"))
				{
					result = createImportFile(EUtranCellTDD_MF, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("ExternalEUtranPlmn"))
				{
					result = createImportFile(ExternalEUtranPLMN_MF, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("ExternalGsmPlmn"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("EUtranCellRelation"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("AnrFunction"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("MbmsServiceArea"))
				{
					result = createImportFile(MbmsServiceArea_MF, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("IubDataStreams"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Sector"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("ExternalEUtranCellTDD"))
				{
					result = createImportFile(ExternalEUtranCellTDD_MF, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("ExternalEUtranCellFDD"))
				{
					result = createImportFile(ExternalEUtranCellFDD_MF, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("IpSystem"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("GeneralProcessorUnit"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Schema"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("SecM"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("SwM"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("BrM"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("FileM"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("PmJob"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Equipment"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				//nagu changes
				/*else if(s_bcgTestData.getImportmo().equals("EUtranCellFDD"))
				 {
					 result = createImportFile(null, s_bcgTestData);
				 }*/
				//ends
				else if(s_bcgTestData.getImportmo().equals("Equipment"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				//16.0.1 code changes starts here
				else if(s_bcgTestData.getImportmo().equals("Fm"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Pm"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("CertM"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("FtpTls"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("HwIM"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Lm"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("SwInventory"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("SystemFunctions"))
				{
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("PmEventM")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Snmp")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("TimeM")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("BfdProfile")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("BfdSessionIPv4")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("EthernetPort")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Host")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("InterfaceIPv4")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Router")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Shaper")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("PeerIPv4")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("RouteTableIPv4Static")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("Xossx_MtasFunction")){
					result = createImportFile(null, s_bcgTestData);
				}
				else if(s_bcgTestData.getImportmo().equals("HSS-Function")){
					result = createImportFile(null, s_bcgTestData);
				}
				//16.0.1 code changes ends here
				else //for AntennaUnitGroup
				{
					result = createImportFile(null, s_bcgTestData);
				}
			}
			importPythonResult = result;
		}
		else //python
		{	
			if(exportResult){
				copyPython();
				if(s_bcgTestData.getModifier().equals("create")){
					importPythonResult = pythonForImport(s_bcgTestData.getImportfilename(),s_bcgTestData.getImportmo(),s_bcgTestData.getNumberofmos(),s_bcgTestData.getModifier());
					deleteImport = bcgImport(s_bcgTestData);
					if(deleteImport.contains("Import has succeeded") || deleteImport.contains("Import has partially succeeded")){
						if(rollbackFileCreation(s_bcgTestData)){
							if(PlanActivation(s_bcgTestData)){
								copyImportFile(s_bcgTestData);
							}
						}
					}
				}
				else
					importPythonResult = pythonForImport(s_bcgTestData.getImportfilename(),s_bcgTestData.getImportmo(),s_bcgTestData.getNumberofmos(),s_bcgTestData.getModifier());

			}
		}
		/*if(s_bcgTestData.getImportmo().equals("AntennaUnitGroup") || s_bcgTestData.getImportmo().equals("GeneralProcessorUnit")) // for delete modifier
		{
			rollbackFileCreation(s_bcgTestData);
			undoPlanActivation(s_bcgTestData);
			undoPlanCreation(s_bcgTestData);	
		}*/
		return importPythonResult;
	}

	/**
	 * This method will check the validation of the bcg import test case
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	public boolean validation(S_BCGTestData s_bcgTestData){
		boolean result = false;
		if(s_bcgTestData.getModifier().equalsIgnoreCase("create")){
			result = PlanActivation(s_bcgTestData);
		}else{
			if(rollbackFileCreation(s_bcgTestData)){
				result = PlanActivation(s_bcgTestData);
			}
		}
		if(isPedIncluded()){
			upadePEDParameters("autoLockUnlock", autoLockUnlockBackup);
			result = getErrorIntoLog(s_bcgTestData);
		}
		LOGGER.info("Validation Result : "+result);
		return result;
	}

	/**
	 * This method will create the undo plan
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	private boolean rollbackFileCreation(S_BCGTestData s_bcgTestData){
		boolean result = false;
		String BCGundocommand =BCGTOOLUNDO + s_bcgTestData.getPlanname();
		LOGGER.info("Rollback file creation command : "+BCGundocommand);
		String undoplanOutput = helper.simpleExec(BCGundocommand);
		LOGGER.info(undoplanOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoplanOutput.contains("PrepareUndo successful") || undoplanOutput.contains("No MO found")){ //my changes
			return true;
		}
		return result;
	}

	/**
	 * This method will activate the plan
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	private boolean PlanActivation(S_BCGTestData s_bcgTestData){
		boolean result = false;
		LOGGER.info("Plan Activation");
		String planActivationCommand =BCGTOOLPLANACTIVATION + s_bcgTestData.getPlanname();
		if(s_bcgTestData.getReconfiguration() != null)
		{
		if (s_bcgTestData.getReconfiguration().equalsIgnoreCase("RNC") || s_bcgTestData.getReconfiguration().equalsIgnoreCase("RBS")) {
			planActivationCommand += " -useRobustReconfig true";

			if (s_bcgTestData.getCountdown() != 0) {
				if (s_bcgTestData.getCountdown() >= 300	&& s_bcgTestData.getCountdown() <= 86400) {
					planActivationCommand += " -configAdmCountdown " + s_bcgTestData.getCountdown();
				} else {
					LOGGER.info("BCG plan activation command : " + planActivationCommand + " -configAdmCountdown "+ s_bcgTestData.getCountdown());
					if (s_bcgTestData.getCountdown() < 300)
						LOGGER.info("Minimum node restart time is 300 seconds");
					else if (s_bcgTestData.getCountdown() > 86400)
						LOGGER.info("Maximum node restart time is 86400 seconds");

					return result;
				}
			} 
		}
		 else if (s_bcgTestData.getReconfiguration().equalsIgnoreCase("RadioTNode")|| s_bcgTestData.getReconfiguration().equalsIgnoreCase("RadioNode")) {
					planActivationCommand += " -useBrmFailSafe true";
					if (s_bcgTestData.getCountdown() != 0) {
						if (s_bcgTestData.getCountdown() >= 300 && s_bcgTestData.getCountdown() <= 86400) {
							planActivationCommand += " -configBrmFailSafeCountdown " + s_bcgTestData.getCountdown();
						} else {
							LOGGER.info("BCG plan activation command : "+ planActivationCommand + " -configBrmFailSafeCountdown "+ s_bcgTestData.getCountdown());
							if (s_bcgTestData.getCountdown() < 300)
								LOGGER.info("Minimum node restart time is 300 seconds");
							else if (s_bcgTestData.getCountdown() > 86400)
								LOGGER.info("Maximum node restart time is 86400 seconds");
		
							return result;
						}
					}
				}
			 else if (s_bcgTestData.getReconfiguration().equalsIgnoreCase("STN")) {
						planActivationCommand += " -useRobustStnConfig true";
						if (s_bcgTestData.getCountdown() != 0) {
							if (s_bcgTestData.getCountdown() >= 60
									&& s_bcgTestData.getCountdown() <= 5940) {
								planActivationCommand += " -configRobustStnReconfigCountdown " + s_bcgTestData.getCountdown();
							} else {
								LOGGER.info("BCG plan activation command : "+ planActivationCommand + " -configRobustStnReconfigCountdown "	+ s_bcgTestData.getCountdown());
								if (s_bcgTestData.getCountdown() < 60)
									LOGGER.info("Minimum node restart time is 60 seconds");
								else if (s_bcgTestData.getCountdown() > 5940)
									LOGGER.info("Maximum node restart time is 5940 seconds");
			
								return result;
							}
						}
					}
		}
		
		LOGGER.info("BCG plan activation command : "+planActivationCommand);
		String planAvtivationOutput = helper.simpleExec(planActivationCommand); //de-comment later
		LOGGER.info("planAvtivationOutput "+planAvtivationOutput);//delete me
		//helper.getShell().disconnect();
		helper.disconnect();
		if (planAvtivationOutput.contains("Activation SUCCESSFUL")/* || planAvtivationOutput.contains("Activation PARTLY_REALIZED")*/){
			return true;
		}
		return result;
	}

	/**
	 * This method will activate the undo plan
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	private boolean undoPlanActivation(S_BCGTestData s_bcgTestData){
		boolean result = false;
		String undoPlanActivationCommand =BCGTOOLUNDOPLANACTIVATION + s_bcgTestData.getPlanname();
		if(s_bcgTestData.getReconfiguration() != null)
		{
		if (s_bcgTestData.getReconfiguration().equalsIgnoreCase("RNC") || s_bcgTestData.getReconfiguration().equalsIgnoreCase("RBS")) {
			undoPlanActivationCommand += " -useRobustReconfig true";

			if (s_bcgTestData.getCountdown() != 0) {
				if (s_bcgTestData.getCountdown() >= 300	&& s_bcgTestData.getCountdown() <= 86400) {
					undoPlanActivationCommand += " -configAdmCountdown " + s_bcgTestData.getCountdown();
				} else {
					LOGGER.info("BCG plan activation command : " + undoPlanActivationCommand + " -configAdmCountdown "+ s_bcgTestData.getCountdown());
					if (s_bcgTestData.getCountdown() < 300)
						LOGGER.info("Minimum node restart time is 300 seconds");
					else if (s_bcgTestData.getCountdown() > 86400)
						LOGGER.info("Maximum node restart time is 86400 seconds");

					return result;
				}
			} 
		}
		 else if (s_bcgTestData.getReconfiguration().equalsIgnoreCase("RadioTNode")|| s_bcgTestData.getReconfiguration().equalsIgnoreCase("RadioNode")) {
			 undoPlanActivationCommand += " -useBrmFailSafe true";
					if (s_bcgTestData.getCountdown() != 0) {
						if (s_bcgTestData.getCountdown() >= 300 && s_bcgTestData.getCountdown() <= 86400) {
							undoPlanActivationCommand += " -configBrmFailSafeCountdown " + s_bcgTestData.getCountdown();
						} else {
							LOGGER.info("BCG plan activation command : "+ undoPlanActivationCommand + " -configBrmFailSafeCountdown "+ s_bcgTestData.getCountdown());
							if (s_bcgTestData.getCountdown() < 300)
								LOGGER.info("Minimum node restart time is 300 seconds");
							else if (s_bcgTestData.getCountdown() > 86400)
								LOGGER.info("Maximum node restart time is 86400 seconds");
		
							return result;
						}
					}
				}
			 else if (s_bcgTestData.getReconfiguration().equalsIgnoreCase("STN")) {
				 undoPlanActivationCommand += " -useRobustStnConfig true";
						if (s_bcgTestData.getCountdown() != 0) {
							if (s_bcgTestData.getCountdown() >= 60
									&& s_bcgTestData.getCountdown() <= 5940) {
								undoPlanActivationCommand += " -configRobustStnReconfigCountdown " + s_bcgTestData.getCountdown();
							} else {
								LOGGER.info("BCG plan activation command : "+ undoPlanActivationCommand + " -configRobustStnReconfigCountdown "	+ s_bcgTestData.getCountdown());
								if (s_bcgTestData.getCountdown() < 60)
									LOGGER.info("Minimum node restart time is 60 seconds");
								else if (s_bcgTestData.getCountdown() > 5940)
									LOGGER.info("Maximum node restart time is 5940 seconds");
			
								return result;
							}
						}
					}
		}
		
		LOGGER.info("BCG Undo Activation command : "+undoPlanActivationCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(undoPlanActivationCommand);
		LOGGER.info(undoPlanAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("Activation SUCCESSFUL")){
			return true;
		}
		return result;
	}

	/**
	 * This method will create the undo_plan
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	private boolean undoPlanCreation(S_BCGTestData s_bcgTestData){
		boolean result = false;
		String undoPlanActivationCommand =BCGTOOLUNDOPLANCREATION + s_bcgTestData.getPlanname();
		LOGGER.info("BCG Undo plan creation command : "+undoPlanActivationCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(undoPlanActivationCommand);
		LOGGER.info(undoPlanAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("UndoActivation successful")){
			return true;
		}
		return result;
	}

	/**
	 * This method will remove the plan
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	private boolean removePlan(S_BCGTestData s_bcgTestData){
		boolean result = false;
		String removePlanCommand =BCGTOOLREMOVEPLAN + s_bcgTestData.getPlanname();
		LOGGER.info("BCG Remove plan command : "+removePlanCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(removePlanCommand);
		LOGGER.info(undoPlanAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("successfully deleted")){
			return true;
		}
		return result;
	}

	/**
	 * This method will remove the Undo_plan
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	private boolean removeUndoPlan(S_BCGTestData s_bcgTestData){
		boolean result = false;
		String removeUndoPlanCommand =BCGTOOLREMOVEUNDOPLAN + s_bcgTestData.getPlanname();
		LOGGER.info("BCG Remove Undo plan command : "+removeUndoPlanCommand);
		String undoPlanAvtivationOutput = helper.simpleExec(removeUndoPlanCommand);
		LOGGER.info(undoPlanAvtivationOutput);
		//helper.getShell().disconnect();
		helper.disconnect();
		if (undoPlanAvtivationOutput.contains("Activation SUCCESSFUL")){
			return true;
		}
		return result;
	}

	/**
	 * This method will move the import file from undoplan location to import filepath
	 * @param s_bcgTestData as S_BCGTestData
	 */
	public void copyImportFile(S_BCGTestData s_bcgTestData){
		helper.simpleExec(REMOVEIMPORT + s_bcgTestData.getImportfilename());
		String copyCommand = "cp " + UNDOPLANPATH + s_bcgTestData.getPlanname() +"/*"+ SPACE + IMPORTPATH;
		helper.simpleExec(copyCommand);
		LOGGER.info("copyImportFile - copyCommand : "+copyCommand);
		String renameCommand = "mv " + IMPORTPATH + "Undo_*" + SPACE + IMPORTPATH + s_bcgTestData.getImportfilename();
		helper.simpleExec(renameCommand);
		LOGGER.info("copyImportFile - renameCommand : "+renameCommand);
		//helper.getShell().disconnect();
		helper.disconnect();
		//for handling ExternalGsmPlmn and ExternalEUtranPlmn using python = rite now its being handled with java

		/*if(s_bcgTestData.getImportmo().equals("ExternalGsmPlmn") || s_bcgTestData.getImportmo().equals("ExternalEUtranPlmn"))
		{
			renameCommand = "mv " + IMPORTPATH + "planned_undo*" + SPACE + IMPORTPATH + s_bcgTestData.getImportfilename();
			helper.simpleExec(renameCommand);
			LOGGER.info("copyImportFile - renameCommand : "+renameCommand);
			String runpython = PYTHON + PYTHONPATH + s_bcgTestData.getImportfilename() + SPACE + s_bcgTestData.getImportmo() + " create " + s_bcgTestData.getNumberofmos();

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
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	public boolean isPedIncluded(){
		return pedIncluded;
	}

	/**
	 * This method will return true or false based on the string availability on file
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	public boolean verifyStringOnErrInfo(){
		return true;
	}

	/**
	 * This method will get the errinfolog file
	 * @param s_bcgTestData as S_BCGTestData
	 * @return result as boolean
	 */
	public boolean getErrorIntoLog(S_BCGTestData s_bcgTestData){
		String grepCommand = "grep \"attribute set to LOCKED before modifying\""+ " errInfoLog_"+ s_bcgTestData.getImportmo()+"*";
		LOGGER.info("Errlog: "+grepCommand);
		if(grepCommand.contains("modifying")){
			return true;
		}
		return false;
	}

	/**
	 * This method will edit the import file using string operations
	 * @param s_bcgTestData as S_BCGTestData
	 * * @param mandatoryFields as String[]
	 * @return result as boolean
	 */
	public boolean createImportFile(String[] mandatoryFields, S_BCGTestData s_bcgTestData /*String operation, String filename, String tagName, String updateFields*/)
	{
		String tmp = s_bcgTestData.getImportmo();
		if(tmp.contains("Xossx")){
			tmp = tmp.substring(6,tmp.length());
			s_bcgTestData.setImportmo(tmp);
		}		
		////// S_BCGTestData
		final String operation = s_bcgTestData.getModifier();
		final String filename = s_bcgTestData.getImportfilename();
		final String tagName = s_bcgTestData.getImportmo();
		final String updateFields = s_bcgTestData.getUpdateFields();
		final String mimName = s_bcgTestData.getMimName();
		LOGGER.info("in createImportFile..");//delete me
		/////
		final String START_TAGS[] = {"<es:vsData"+tagName+">", "<es:vsData"+tagName+"/>"};
		final String END_TAGS[] = {"<es:vsData"+tagName+"/>", "</es:vsData"+tagName+">"};
		final String VS_DATA_CONTAINER = "<xn:VsDataContainer id=";
		final String MANAGED_ELEMENT = "<xn:ManagedElement id=";
		final String TAG_END = "</xn:attributes>";
		int count = 0;
		BufferedReader br = null;
		String strS1 = "";
		//int idAttrib = 254;
		int idAttrib = 45;
		String endTag = "";
		boolean hashmapFlag = false;

		try
		{
			String output = helper.simpleExec(CAT+" "+ EXPORTPATH + filename);
			//LOGGER.info("CAT OUTPUT -------------- :"+output +"-------------------------");//delete me
			br = new BufferedReader(new StringReader(output));
			String str = new String() ;
			LOGGER.info("hashmap.get(tagName) : "+ hashmap.get(tagName));

			//if(/*file.length()/(1024) > 1024*/false) //do no use hasmap content as of now - use it in future
			/*if(hashmap.get(tagName) != null)
			{
				LOGGER.info("CHECKME - use hasmap");
				strS1 = hashmap.get(tagName);
				if(strS1.contains("create"))
				{
					strS1 = strS1.replace("create","delete");
					hashmapFlag = true;
				}
				else
				{
					strS1 = strS1.replace("delete","create");
					hashmapFlag = true;	
				}
				hashmap.clear();
				//if file size is very big
				//do nothing as of now..
				//return false;				        
			}  
			else*/
			{	
				hashmapFlag = false;
				while((str = br.readLine()) != null)
				{
					strS1 += str + "\n";
				}
				br.close();	
				if(idMap.get(tagName) != null)
				{
					LOGGER.info("in idMap condition");
					idAttrib = idMap.get(tagName);
					idMap.clear();
				}
				else
				{	
					//for(int i = idAttrib; i > 200; i--)
					for(int i = idAttrib; i > 20; i--)
					{	
						if(strS1.contains("id="+"\""+String.valueOf(i)+"\"") || strS1.contains("-"+String.valueOf(i)+"\""))
						{
							idAttrib--;
							LOGGER.info("in id= condition deleteAttrib "+idAttrib);
							idMap.put(tagName,idAttrib);
						}
					}	
				}

				/*if(strS1.contains(END_TAGS[count]) || strS1.contains(END_TAGS[++count]))
				{
					str = strS1.substring(strS1.lastIndexOf(END_TAGS[count]), strS1.length());
					strS1 =  strS1.substring(0,strS1.indexOf(END_TAGS[count]));
				}*/

				if(strS1.contains(END_TAGS[0]) && strS1.contains(END_TAGS[1]))
				{
					if(strS1.indexOf(END_TAGS[0]) < strS1.indexOf(END_TAGS[1]))
					{
						str = strS1.substring(strS1.lastIndexOf(TAG_END), strS1.length());
						strS1 =  strS1.substring(0, strS1.indexOf(END_TAGS[0]) + END_TAGS[0].length());
						endTag = END_TAGS[0];
						LOGGER.info("END_TAGS[0] : "+END_TAGS[0]);
					}
					else if(strS1.indexOf(END_TAGS[1]) < strS1.indexOf(END_TAGS[0]))
					{
						str = strS1.substring(strS1.lastIndexOf(TAG_END), strS1.length());
						strS1 =  strS1.substring(0, strS1.indexOf(END_TAGS[1]) + END_TAGS[1].length());
						endTag = END_TAGS[1];
						LOGGER.info("END_TAGS[1] : "+END_TAGS[1]);
					}
				}
				else if(strS1.contains(END_TAGS[0]))
				{
					str = strS1.substring(strS1.lastIndexOf(TAG_END), strS1.length());
					strS1 =  strS1.substring(0, strS1.indexOf(END_TAGS[0]) + END_TAGS[0].length());
					endTag = END_TAGS[0];
					LOGGER.info("END_TAGS[0] : "+END_TAGS[0]);
				}
				else if(strS1.contains(END_TAGS[1]))
				{
					str = strS1.substring(strS1.lastIndexOf(TAG_END), strS1.length());
					strS1 =  strS1.substring(0, strS1.indexOf(END_TAGS[1]) + END_TAGS[1].length());
					endTag = END_TAGS[1];
					LOGGER.info("END_TAGS[1] : "+END_TAGS[1]);

				}
				else
				{
					LOGGER.info("tag not found");
					return false;
				}

				strS1 += str ;



			}

			String sub = "";
			String subReplace = "";
			String subReplaceWithMim = "";
			String subME = "";
			String subME_Replace = "";
			count = 0;
			String startTag = "";
			boolean flag = false;
			//LOGGER.info("--------------TAGNAME" +START_TAGS[count] +"--------------");
			//LOGGER.info("--------------TAGNAME" +START_TAGS[++count] +"--------------");
			count = 0;
			//new changes
			if(strS1.contains(START_TAGS[0]) && strS1.contains(START_TAGS[1]))
			{
				if(strS1.indexOf(START_TAGS[0]) < strS1.indexOf(START_TAGS[1]))
				{
					sub = strS1.substring(0, strS1.indexOf(START_TAGS[0]));
					LOGGER.info("START_TAGS[0] : "+START_TAGS[0]);
					startTag = START_TAGS[0];
					flag = true;
				}
				else if(strS1.indexOf(START_TAGS[1]) < strS1.indexOf(START_TAGS[0]))
				{
					sub = strS1.substring(0, strS1.indexOf(START_TAGS[1]));
					LOGGER.info("START_TAGS[1] : "+START_TAGS[1]);
					startTag = START_TAGS[1];
					flag = true;
				}
			}
			else if(strS1.contains(START_TAGS[0]))
			{
				sub = strS1.substring(0, strS1.indexOf(START_TAGS[0]));
				LOGGER.info("START_TAGS[0] : "+START_TAGS[0]);
				startTag = START_TAGS[0];
				flag = true;
			}
			else if(strS1.contains(START_TAGS[1]))
			{
				sub = strS1.substring(0, strS1.indexOf(START_TAGS[1]));
				LOGGER.info("START_TAGS[1] : "+START_TAGS[1]);
				startTag = START_TAGS[1];
				flag = true;

			}
			else
			{
				LOGGER.info("tag not found");
				return false;
			}
			//ends
			//if(strS1.indexOf(START_TAGS[count]) > -1 || strS1.indexOf(START_TAGS[++count]) > -1)
			if(flag && !hashmapFlag)
			{
				//sub = strS1.substring(0, strS1.indexOf(START_TAGS[count]));
				flag = false;
				if(mimName != null && mimName.equals("ECIM_TOP"))
				{
					subME = sub.substring(sub.indexOf(MANAGED_ELEMENT), sub.length());
					subME = subME.substring(0, subME.indexOf(">") + 1);
					subME_Replace = subME.replace(">", " mimName="+"\""+mimName+"\">");
					strS1 = strS1.replaceFirst(subME, subME_Replace);
					LOGGER.info("--------------"+MANAGED_ELEMENT+" replaced"+"--------------");
				}
				//LOGGER.info("sub string CHECKME:" +sub);
				if(sub.contains(VS_DATA_CONTAINER) && sub.lastIndexOf(VS_DATA_CONTAINER) >  -1)	
				{
					LOGGER.info("--------------"+VS_DATA_CONTAINER+" found"+"--------------");
					sub = sub.substring(sub.lastIndexOf(VS_DATA_CONTAINER), sub.length());
					sub = sub.substring(0, sub.indexOf(">") + 1);
					LOGGER.info("vsDataContainer SUB STRING :" +sub);

					if(sub.contains("-"))
					{
						String s = sub.substring(sub.lastIndexOf("-")+1,sub.indexOf(">") + 1);
						if(operation.equals("update") || operation.equals("delete"))//delete for antenna group
						{
							//subReplace = sub.replace(s, "1\" modifier="+"\""+operation+"\">");
							s = sub.substring(0, sub.indexOf(">"));
							subReplace = s.concat(" modifier="+"\""+operation+"\">");	
						}
						else
						{
							subReplace = sub.replace(s, idAttrib+"\" modifier="+"\""+operation+"\">");
						}
						strS1 = strS1.replace(sub, subReplace);	

					}
					else
					{
						String s = sub.substring(sub.lastIndexOf("\"")-1, sub.indexOf(">") + 1);
						if(operation.equals("update") || operation.equals("delete"))//delete for antenna group
						{
							//subReplace = sub.replace(s, "1\" modifier="+"\""+operation+"\">");
							s = sub.substring(0, sub.indexOf(">"));
							subReplace = s.concat(" modifier="+"\""+operation+"\">");	
						}
						else
						{
							subReplace = sub.replace(s, idAttrib+"\" modifier="+"\""+operation+"\">");
						}

						//subReplace = sub.substring(0, sub.indexOf(">") + 1);
						//subReplace = subReplace.replace(">"," modifier="+"\""+operation+"\">");

						strS1 = strS1.replace(sub, subReplace);
						if(tagName.equals("AntennaUnitGroup") || tagName.equals("AnrFunction") 
								|| tagName.equals("MbmsServiceArea") || tagName.equals("IubDataStreams") /*|| tagName.equals("EUtranCellFDD")*/)
						{
							strS1 = strS1.replaceFirst(subReplace, sub);
						}
						if(tagName.equals("GeneralProcessorUnit"))
						{
							strS1 = strS1.replaceFirst(subReplace, sub);
							strS1 = strS1.replaceFirst(subReplace, sub);
							strS1 = strS1.replaceFirst(subReplace, sub);
							strS1 = strS1.replaceFirst(subReplace, sub);
						}
						if(tagName.equals("EUtranCellRelation"))
						{
							strS1 = strS1.replaceFirst(subReplace, sub);
							strS1 = strS1.replaceFirst(subReplace, sub);
						}

						if(tagName.equals("Equipment"))
						{
							subReplaceWithMim = subReplace.substring(0,subReplace.indexOf(">"));
							subReplaceWithMim = subReplaceWithMim.concat(" mimName="+"\""+mimName+"\">");
							LOGGER.info("--------------subReplaceWithMim Equipment-------------"+subReplaceWithMim);
							strS1 = strS1.replaceFirst(subReplace, subReplaceWithMim);
						}
						if(tagName.equals("MtasFunction") || tagName.equals("HSS-Function"))
						{
							subReplaceWithMim = subReplace.substring(0,subReplace.indexOf(">"));
							subReplaceWithMim = subReplaceWithMim.concat(" mimName="+"\""+mimName+"\">");
							LOGGER.info("--------------subReplaceWithMim Equipment-------------"+subReplaceWithMim);
							strS1 = strS1.replaceFirst(subReplace, subReplaceWithMim);
						}
						
						if(tagName.equals("Schema") || tagName.equals("FileM") 
								|| tagName.equals("SwM") || tagName.equals("PmJob") 
								|| tagName.equals("SecM") || tagName.equals("BrM") 
								|| tagName.equals("Fm") || tagName.equals("Pm")
								|| tagName.equals("CertM") || tagName.equals("FtpTls") 
								|| tagName.equals("HwIM") || tagName.equals("Lm") 
								|| tagName.equals("SwInventory") || tagName.equals("PmEventM")
								|| tagName.equals("Snmp") || tagName.equals("TimeM")
								|| tagName.equals("BfdProfile") || tagName.equals("BfdSessionIPv4")
								|| tagName.equals("EthernetPort") || tagName.equals("Host")
								|| tagName.equals("InterfaceIPv4") || tagName.equals("Router")
								|| tagName.equals("Shaper") || tagName.equals("PeerIPv4")
								|| tagName.equals("RouteTableIPv4Static"))
						{
							subReplaceWithMim = subReplace.substring(0,subReplace.indexOf("modifier")-1);
							subReplaceWithMim = subReplaceWithMim.concat(" mimName="+"\""+mimName+"\">");
							LOGGER.info("--------------subReplaceWithMim-------------"+subReplaceWithMim);
							strS1 = strS1.replaceFirst(subReplace, subReplaceWithMim);
						}


					}

					LOGGER.info("FINAL REPLACE STRING  :" +subReplace);
				}

			}
			String ufs[] = null;
			String element = "";
			String subTag = "";
			Map<String, String> modMap = new HashMap<String,String>();
			/*if(strS1.contains(START_TAGS[0]))
			{
				element = strS1.substring(strS1.indexOf(START_TAGS[0]) +START_TAGS[0].length() + 1 ,strS1.indexOf(END_TAGS[1]));
				LOGGER.info("element :"+element);
			}*/
			if(startTag.equals(START_TAGS[0]))
			{
				element = strS1.substring(strS1.indexOf(START_TAGS[0]) +START_TAGS[0].length() + 1 ,strS1.indexOf(END_TAGS[1]));
				LOGGER.info("element :"+element+"\n");
			}


			if(operation.equals("update") && updateFields != null )
			{
				if(updateFields.contains(";"))
				{
					ufs = updateFields.split(";");
					for(String uf: ufs)
					{
						String[] fields = uf.split("=");
						try
						{
							modMap.put(fields[0], fields[1]);
						}
						catch(Exception e)
						{
							LOGGER.info("Exception caught while split operation in update modifier code");
							return false;
						}
					}

				}
				else
				{

					String[] fields = updateFields.split("=");
					try
					{
						modMap.put(fields[0], fields[1]);
					}
					catch(Exception e)
					{
						LOGGER.info("Exception caught while split operation in update modifier code");
						return false;
					}
				}

				//element = strS1.substring(strS1.indexOf(START_TAGS[0]) +START_TAGS[0].length() + 1 ,strS1.indexOf(END_TAGS[1]));

				Iterator<String> iter = modMap.keySet().iterator(); 
				while(iter.hasNext())
				{

					String iterValue = iter.next();
					String sTag = "<"+iterValue+">";
					String value = modMap.get(iterValue);
					String eTag = "</"+iterValue+">";
					subTag += sTag+value+eTag+"\n";
				}
				LOGGER.info("update modifier subTag STRING  :" +subTag);
			}

			if(mandatoryFields != null && !operation.equals("update"))
			{
				//element = strS1.substring(strS1.indexOf(START_TAGS[0]) +START_TAGS[0].length() + 1 ,strS1.indexOf(END_TAGS[1]));

				for(int i = 0 ; i < mandatoryFields.length; i++)
				{
					String sTag = "<"+mandatoryFields[i]+">";
					String eTag = "</"+mandatoryFields[i]+">";
					String nTag = "<"+mandatoryFields[i]+"/>";
					if(element.contains(sTag))
					{
						String tag = element.substring(element.indexOf(sTag), element.indexOf(eTag) +eTag.length()) +"\n";
						LOGGER.info("tag:"+tag+"\n");
						tag = tag.replace(">,",">"); //handle cat output
						if(tag.contains("<es:subframeAssignment>"))
						{
							tag = tag.replace("0", "1");
						}
						if(tag.contains("<es:cellId>"))
						{
							tag = "<es:cellId>"+idAttrib+"</es:cellId>\n";
						}
						if(tag.contains("<es:sac>"))
						{
							tag = "<es:sac>"+idAttrib+"</es:sac>\n";
						}
						if(tag.contains("<es:localCellId>"))
						{
							tag = "<es:localCellId>"+idAttrib+"</es:localCellId>\n";
						}
						if(tag.contains(","))
						{
							tag = tag.replace(",", "||");
							LOGGER.info("Contains ,. Hence replacing with ||");
						}
						subTag += tag;
					}
					else if(element.contains(nTag))
					{
						subTag += nTag + "\n";
					}
				}

			}	
			strS1 = strS1.replace(element, subTag);

			strS1 = strS1.replace("[",""); //handle cat output
			strS1 = strS1.replace("]",""); //handle cat output
			//strS1 = strS1.replace(">,",">"); //handle cat output
			strS1 = strS1.replace(",",""); //handle cat output
			strS1 = strS1.replace("||", ",");
			strS1 = strS1.replace("><<", "><");
			LOGGER.info("FINAL strS1 :"+strS1);	
			hashmap.put(tagName, strS1);

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
			return false;
		}
		//helper.getShell().disconnect();
		helper.disconnect();
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
			format.setIndenting(true);
			format.setIndent(4);
			out = new StringWriter();
			serializer = new XMLSerializer(out, format);
			serializer.serialize(document);

			String output = helper.simpleExec(ECHO +" "+ "'"+out.toString()+"'" + " > " +IMPORTPATH + filename);
			//LOGGER.info("output from echo :"+output);

		}

		catch(IOException ioe){
			LOGGER.info("IO exception caught while formating xml file \n"+ioe.getMessage());
			return false;
		}
		catch(Exception e){
			LOGGER.info("Exception caught while formating xml file \n"+e.getMessage());
			if(e.getMessage().contains("xn:SubNetwork"))
			{	
				System.out.println("in if block");
				int ix = file.indexOf("</xn:SubNetwork>") + "</xn:SubNetwork>".length();
				String tail = file.substring(ix,file.length());
				file = file.substring(0, ix);
				file = file.concat("</xn:SubNetwork>");
				file += tail;

				System.out.println("file : "+file);
				try
				{
					is = new InputSource(new StringReader(file));
					document = db.parse(is);

					//format part
					format = new OutputFormat(document);
					format.setOmitXMLDeclaration(true);
					format.setIndenting(true);
					format.setIndent(4);
					out = new StringWriter();
					serializer = new XMLSerializer(out, format);
					serializer.serialize(document);

					String output = helper.simpleExec(ECHO +" "+ "'"+out.toString()+"'" + " > " +IMPORTPATH + filename);

				}
				catch(Exception ee)
				{
					LOGGER.info("Exception caught while formating xml file(appending the subnetwork closing tag)\n"+ee.getMessage());
					return false;
				}
				return true;

			}
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

