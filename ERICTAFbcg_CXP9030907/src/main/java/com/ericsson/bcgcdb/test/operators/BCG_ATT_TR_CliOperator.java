package com.ericsson.bcgcdb.test.operators;
    
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
   


 import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

 import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

 import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

 import com.ericsson.bcgcdb.test.cases.BCG_ATT_TR_TestData;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 
 @Operator(context = Context.CLI)
 public class BCG_ATT_TR_CliOperator implements BCG_ATT_TR_Operator {
 
         Logger LOGGER = Logger.getLogger(BCG_ATT_TR_CliOperator.class);
 
         BCGExportGetter bcgExportGetter;
         CLICommandHelper helper;
 
         Shell shell;
         Host host,host1;
         User operUser;
         User rootUser;
 
         private final String BCG_EXPORTLOG_FILES_DIRECTORY = " /var/opt/ericsson/nms_umts_wran_bcg/logs/export/";
         private final String CSTESTLT = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt ";
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
         private final String CSTESTSA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice sa ";
         private final String IMPORTPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/import/";
         private final String BCGTOOLIMPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -i ";
         private final String BCGTOOLUNDO = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -u ";
         private final String BCGTOOLPLANACTIVATION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -a ";
         private final String BCGTOOLUNDOPLANCREATION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -au ";
         private final String BCGTOOLUNDOPLANACTIVATION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -a Undo_";
         private final String BCGTOOLREMOVEPLAN = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -rp ";
         private final String BCGTOOLREMOVEUNDOPLAN = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -rp Undo_";
         private final String VSDATA = " -d :vsData";
         private final String MINUSP = " -p ";
         private final String MOLOCK = "-molock";
         private final String SUBNETWORK = "SubNetwork";
         private final String BCG_INSTRUMENTATION_RATE_GREP_STRING = "'MO/Sec for Export = '";
         private final String BCG_INSTRUMENTATION_IMPORT_TIME_GREP_STRING = "'Total time for BCG Import (ms) = '";
         private final String BCG_EXPORT_LOGS_DIRECTORY = " /var/opt/ericsson/nms_umts_wran_bcg/logs/export/";
         private final String BCG_IMPORT_LOGS_DIRECTORY = " /var/opt/ericsson/nms_umts_wran_bcg/logs/import/";
         private final String CUSTOMFILTER = "/opt/ericsson/nms_umts_bcg_meta/dat/customfilters/";
         private final String SMTOOL_COMMAND = "/opt/ericsson/nms_cif_sm/bin/smtool -set wran_bcg";
         private final String CACHEOFF = "sed 's/generation.counters=true/generation.counters=false/' /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh > /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg_new.sh";
         private final String RENAMEFILE = "mv /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg_new.sh /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh;chmod 750 /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh";
         private final String CACHEON = "sed 's/generation.counters=false/generation.counters=true/' /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg.sh > /opt/ericsson/nms_umts_wran_bcg/bin/start_wran_bcg_new.sh";
         private final String BCGMCRESTART = "/opt/ericsson/nms_cif_sm/bin/smtool coldrestart wran_bcg -reason=Other -reasontext=\"Made Cache changes\"";
         private final String AMMCOFFLINE = "/opt/ericsson/nms_cif_sm/bin/smtool offline ActivityManager -reason=Other -reasontext=\"AM offline\"";
         private final String AMMCONLINE = "/opt/ericsson/nms_cif_sm/bin/smtool online ActivityManager ";
         private final String BCGSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep wran_bcg ";
         private final String AMSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep ActivityManager ";
 
 
         int successfulExportCount = 0;
         String adminStateFdn;
         static Map<String,String> hashmap = new HashMap<String,String>();
         boolean isExportResult ;
         private String modifierString;
         String autoLockUnlock;
         String nodeList;
         String bachupAutoLockUnlock;
         String[] updateFieldValue;
         String updateFieldFdn;
         String adminStateOutput;
         String admState;
         boolean isPEDIncluded = false;
         boolean isDeleteDone = false;
         boolean isAdminStateIncluded = false;
         String backupAutoLockUnlock;
         String PEDOutput;
         String autoLockUnlockBackup;
         String modifiedUseIdValue;
         
 
         /**
          * Initializing host, user and cli
          */
         public BCG_ATT_TR_CliOperator(){
                 bcgExportGetter = new BCGExportGetter();
                 host = HostGroup.getOssmaster();
                 
                 host1 = HostGroup.getOssmaster();
                 final CLICommandHelper cmdHelper = new CLICommandHelper(host);
                 cmdHelper.openShell();
            // helper = new CLICommandHelper(host);
 //              host = DataHandler.getHostByType(HostType.RC);
         operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
         rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
 //              LOGGER.info("host.getIp()"+host.getIp());
         helper = new CLICommandHelper(host, operUser);
         }
 
         /**
          * This method will check all the pre-condition for the Test Case
          * @param bcgTestData
          * @return result as boolean
          */
         public boolean preAction(BCG_ATT_TR_TestData bcgTestData) {
                 LOGGER.info("*preAction*");
                 boolean adminStateResult = false;
                 boolean pedresult = false;
                 
                 try{
                         LOGGER.info("Checking server connectivity");
 //                      String serverFiles = helper.simpleExec("cd /home/nmsadm/;ls");
                 }catch(Exception e){
                         LOGGER.info("Exception while connecting to server " + host.getIp() + " "+ e);
                 }
                 finally{
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                 }
                 if(!findMos(bcgTestData)){
                         return false;
                 }
                 if(!(bcgTestData.getPedAutoLockUnlock() == null)){
                         LOGGER.info("###Setting PED parameters");
                         isPEDIncluded = true;
                         backupPed(autoLockUnlock, bcgTestData.getPedAutoLockUnlock());
                         pedresult = upadePEDParameters("autoLockUnlock", bcgTestData.getPedAutoLockUnlock());
                         LOGGER.info("pedResult     "+pedresult);
                         if (!pedresult) {
                                 return false;
                         }
                 }
                 if(bcgTestData.getMoName() != null){
                         if(bcgTestData.getMoName().equalsIgnoreCase("Aal2PathVccTp")){
                                 if(((bcgTestData.getAdminState() != null))){
                                         LOGGER.info("###Setting AdminState parameter");
                                         admState  = bcgTestData.getAdminState();
                                         adminStateResult = updateStateParameter("administrativeState", bcgTestData.getAdminState(),bcgTestData);
                                         LOGGER.info("adminState setting result    "+adminStateResult);
                                         if (!adminStateResult) {
                                                 return false;
                                         }
                                 }
                         }
                 }
                 if(bcgTestData.getNodeType().equalsIgnoreCase("1")){
                         copyFiles();
                 }
                 return true;
         }
 
         /**
          * This method will update the adminState parameter values
          * @param bcgTestData 
          * @param adminState
          * @param adminStateValue
          * @return result as boolean
          */
         private boolean updateStateParameter(String adminState, String adminStateValue, BCG_ATT_TR_TestData bcgTestData) {
 
                 boolean isAdministrativeStateUpdated = false;
                 String updatePEDCommand = "";
                 updatePEDCommand = CSTESTSA + SPACE + adminStateFdn+ SPACE + adminState + SPACE + adminStateValue;
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
          * This method will update the PED parameter values
          * @param ped_name
          * @param ped_value
          * @return result as boolean
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
         }
         
         /**
          * This method will check the server for the required Mos
          * @param bcgTestData
          * @return result as boolean
          */
         private boolean findMos(BCG_ATT_TR_TestData bcgTestData) {
                 LOGGER.info("*findMos*");
                 String allNodes = null;
                 String getAllNodes = "";
                 try{
                         if(bcgTestData.getMoName() != null){
                                 if(bcgTestData.getMoName().equalsIgnoreCase("Aal2PathVccTp") || bcgTestData.getMoName().equalsIgnoreCase("EUtranCellFDD")
                                                 || (bcgTestData.getMoName().equalsIgnoreCase("UtranCell")) || bcgTestData.getMoName().equalsIgnoreCase("EUtranFreqRelation")){
                                         String csTestCommand = CSTESTLT + SPACE + bcgTestData.getMoName();
                                         LOGGER.info("BCG Find MO call command : "+csTestCommand);
                                         String csOutput = helper.simpleExec(csTestCommand);
         
                                         String[] fdns = csOutput.split("\n");
                                         LOGGER.info("nodes from CS : "+fdns);
                                         if(fdns.length > 0){
                                                 for(int i =0; i<fdns.length; i++){
                                                         String [] commasplit = fdns[i].split(",");
                                                         nodeList = "";
                                                         if(commasplit[0].contains("SubNetwork") && commasplit[1].contains("SubNetwork") && commasplit[2].contains("MeContext")){
                                                                 nodeList = commasplit[0]+","+commasplit[1]+","+commasplit[2];
                                                                 if(isNodeSynchronized(nodeList)){
                                                                         updateFieldFdn = commasplit[1].substring(SUBNETWORK.length()+1,commasplit[1].length());
                                                                         adminStateFdn = fdns[i];
                                                                         LOGGER.info("\n updatefieldfdn ::::: " + updateFieldFdn);
                                                                         break;
                                                                 }
                                                         }
                                                 }
                                         }
                                         
                                 }
                         }
                         else if((!bcgTestData.getNodeName().isEmpty()) &&(bcgTestData.getNodeName().equalsIgnoreCase("SubNetwork")) ){
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
                         else {
                                 getAllNodes = NECHECK+bcgTestData.getNodeType();
                                 allNodes = helper.simpleExec(getAllNodes);
                                 LOGGER.info("List of available nodes: "+allNodes);
                                 if(!allNodes.isEmpty()){
                                         String[] fdns = allNodes.split("\n");
                                         if(fdns.length > 0){
                                                 for(int i = 0 ; i < fdns.length; i++){
                                                         nodeList = "";
                                                         if(bcgTestData.getNodeType().equalsIgnoreCase("4")){
                                                                 String[] fdn = fdns[i].split(",");
                                                                 if(bcgTestData.getNodeFdn().equalsIgnoreCase("SubNetwork,SubNetwork,MeContext")){
                                                                         if(fdn.length >=3){
                                                                                 if(fdn[0].contains("SubNetwork") && fdn[1].contains("SubNetwork") && fdn[2].contains("MeContext") ){
                                                                                         nodeList = fdn[0] + "," + fdn[1] + "," + fdn[2];
                                                                                         if(isNodeSynchronized(nodeList))
                                                                                                 nodeList = fdns[i];
                                                                                         break;
                                                                                 }else{
                                                                                         if(fdn[0].contains("SubNetwork") && fdn[1].contains("MeContext") ){
                                                                                                 nodeList = fdn[0] + "," + fdn[1];
                                                                                                 if(isNodeSynchronized(nodeList)){
                                                                                                         nodeList = fdns[i];
                                                                                                         break;
                                                                                                 }
                                                                                         }
                                                                                 }
                                                                         }
                                                                 }
                                                         }else{
                                                                 String[] fdn = fdns[i].split(",");
                                                                 if(fdn[0].contains("SubNetwork") && fdn[1].contains("SubNetwork") && fdn[2].contains("MeContext") ){
                                                                         nodeList = fdn[0] + "," + fdn[1] + "," + fdn[2];
                                                                         if(isNodeSynchronized(nodeList)){
                                                                                 nodeList = fdns[i];
                                                                                 break;
                                                                         }
                                                                 }
                                                         }
                                                 }
                                         }
                                 }
                         }
                 }catch(Exception e){
                         LOGGER.info(e.getMessage());
                         LOGGER.info(e);
                 }finally{
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                 }
 
                 if(!nodeList.isEmpty()){
                         if(bcgTestData.getVerifyTestCase() != null){
                                 if(bcgTestData.getVerifyTestCase().equalsIgnoreCase("vclTp")){
                                         String csTestupdateFieldMO = CSTESTLT + SPACE + bcgTestData.getVerifyTestCase() + SPACE +" | grep " + SPACE + updateFieldFdn;
                                         LOGGER.info("BCG Find MO call command : "+csTestupdateFieldMO);
                                         String updateFieldMO = helper.simpleExec(csTestupdateFieldMO);
                                         updateFieldValue = updateFieldMO.split("\n");
                                         LOGGER.info(updateFieldValue[0] +"\n"+updateFieldValue[1]);
                                 }
                         }
                         else{
                                 LOGGER.info("Available node : "+nodeList);
                         }
                         
                         return true;
                 }
                 else{
                         LOGGER.info("Nodes are NOT available for "+ bcgTestData.getNodeName());
                         return false;
                 }
 
         }
         
         /**
          * This method will check the required node is sync or not
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
         public boolean bcgExport(BCG_ATT_TR_TestData bcgTestData){
                 LOGGER.info("*BcgExport*");
                 String bcgExportCommand = null;
                 String exportOutput = null;
 
                 try{
                         if(bcgTestData.getMoName() !=  null){
                                                 bcgExportCommand = BCGTOOLEXPORT + SPACE + bcgTestData.getFileName() + SPACE + VSDATA + bcgTestData.getMoName() + SPACE + MINUSN + SPACE + nodeList;
                         }
                         else if( (bcgTestData.getNodeName().equalsIgnoreCase("SubNetwork"))){
                                 if(bcgTestData.getMimRate() != null ){
                                         if(settingCache("CACHEOFF")){
                                                 bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgTestData.getFileName();
                                         }
                                 }
                                 else{
                                         bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgTestData.getFileName()+ SPACE + MINUSD + SPACE + bcgTestData.getDomain();
                                 }
                         }
                         else if(bcgTestData.getDomain() != null){
                                 bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgTestData.getFileName() + SPACE + MINUSD + SPACE + bcgTestData.getDomain() + SPACE + MINUSN + SPACE + nodeList;
                         }
                         else{
                                 bcgExportCommand =  BCGTOOLEXPORT + SPACE + bcgTestData.getFileName();
                         }
 
                         LOGGER.info("BCG Export command: "+bcgExportCommand);
                         exportOutput = helper.simpleExec(bcgExportCommand);
                         helper.simpleExec("echo " + "\"" + exportOutput +"\"" + " > " + "/var/opt/ericsson/nms_umts_wran_bcg/files/export/export_console.txt");
                         LOGGER.info("BCG Export output ::: \n" + exportOutput);
                 }catch(Exception e){
                         LOGGER.info(e.getMessage());
                         LOGGER.info(e);
                 }finally{
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                 }
 
                 if(exportOutput.contains("Export has succeeded")){
                         if(bcgTestData.getMimRate() != null){
                                 settingCache("CACHEON");
                         }
                         return isExportResult = true;
                 }
                 else{
                         return isExportResult = false;
                 }
         }
 
         /**
          * This method will check the MO is there in export file or not
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         public boolean verifyTestCase(BCG_ATT_TR_TestData bcgTestData) {
                 LOGGER.info("*verify Export Test Case*");
                 String grepCommand ;
                 String stringFound = null ;
                 String stringFound1 = null;     
                 
                 int fileNameLength = bcgTestData.getFileName().length();
                 String errLogFileName = "errLog_"+bcgTestData.getFileName().substring(0, (fileNameLength-4));
                 String errInfoLogFileName = "errInfoLog_"+bcgTestData.getFileName().substring(0, (fileNameLength-4));
                 
                 grepCommand = "ls -lt "+ SPACE+ BCG_EXPORT_LOGS_DIRECTORY+"*"+SPACE+" | grep"+SPACE+errInfoLogFileName;
                 LOGGER.info("files grep command ::: "+ grepCommand);
                 String outputInfo = helper.simpleExec(grepCommand);
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 
                 String[] filesInfo = outputInfo.split("\n");
                 int startIndexInfo = filesInfo[0].indexOf("/");
                 String errorInfoFile = filesInfo[0].substring(startIndexInfo);          
                 int info = errorInfoFile.indexOf("err");
                 String errInfoLogFile = errorInfoFile.substring(info);
                 
                 grepCommand = "ls -lt "+ SPACE+ BCG_EXPORT_LOGS_DIRECTORY+"*"+SPACE+" | grep"+SPACE+errLogFileName;
                 LOGGER.info("files grep command ::: "+ grepCommand);
                 String output = helper.simpleExec(grepCommand);
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 
                 String[] files = output.split("\n");
                 int start = files[0].indexOf("/");
                 String errorLogFile = files[0].substring(start);                
                 int f = errorLogFile.indexOf("err");
                 String errLogFile = errorLogFile.substring(f);
                                 
                 if(!(bcgTestData.getVerifyTestCase().isEmpty())){
                         if(bcgTestData.getVerifyTestCase().equalsIgnoreCase("uraList")){
                                 grepCommand = "grep "+bcgTestData.getVerifyTestCase()+SPACE + EXPORTPATH +bcgTestData.getFileName();
                                 LOGGER.info("GREP Command :::    " +   grepCommand);
                                 stringFound = helper.simpleExec(grepCommand);
                                 LOGGER.info("Attribute is present in Export File : "+stringFound);
         //                      //helper.getShell().disconnect();
                                 helper.disconnect();
                                 
                                 if(!stringFound.contains(bcgTestData.getVerifyTestCase())){
                                         return false;
                                 }
                         }
                         else if((bcgTestData.getVerifyTestCase().equalsIgnoreCase("PersistenceManager"))){
                                 
                                 grepCommand = "grep"+SPACE+bcgTestData.getVerifyTestCase()+SPACE +BCG_EXPORT_LOGS_DIRECTORY+errInfoLogFile.trim();
                                 LOGGER.info("GREP Command :::    " +   grepCommand);
                                 stringFound = helper.simpleExec(grepCommand);
                                 LOGGER.info("Attribute is present in Export File : "+stringFound);
         //                      //helper.getShell().disconnect();
                                 helper.disconnect();
                                 LOGGER.info("moFound ::::"+ stringFound);
                                 
                                 grepCommand = "grep"+SPACE+bcgTestData.getVerifyTestCase()+SPACE +BCG_EXPORT_LOGS_DIRECTORY+errLogFile.trim();
                                 LOGGER.info("GREP Command :::    " +   grepCommand);
                                 stringFound1 = helper.simpleExec(grepCommand);
                                 LOGGER.info("Attribute is present in Export File : "+stringFound1);
         //                      //helper.getShell().disconnect();
                                 helper.disconnect();
                                 LOGGER.info("moFound ::::"+ stringFound1);
                                 if((stringFound.contains(bcgTestData.getVerifyTestCase())) || (stringFound1.contains(bcgTestData.getVerifyTestCase())) ){
                                         return false;
                                 }
                         }
                         else if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("temp")){
                                 grepCommand = " ls -lrt " + SPACE + EXPORTPATH + "temp/tmp ";
                                 LOGGER.info("GREP command  :: "+ grepCommand);
                                 stringFound = helper.simpleExec(grepCommand);
                                 LOGGER.info("output ::: "+ stringFound);
         //                      //helper.getShell().disconnect();
                                 helper.disconnect();
                                 
                                 if(!stringFound.toString().contains("total 0")){
                                         return false;
                                 }
                         }
                         else if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("SubNetwork")){
                                 final String SUB_NETWORK_START_TAG = "<xn:SubNetwork id=";
                                 final String SUB_NETWORK_END_TAG = "</xn:SubNetwork>";
                                 BufferedReader br = null;
                                 String subNetworkStartTag = "";
                                 String subNetworkEndTag = "";
                                 String[] subNetworkStart = null;
                                 String[] subNetworkEnd = null;
                                 
                                 try{
                                         /*String exportFileContents = helper.simpleExec("cat "+ EXPORTPATH + bcgTestData.getFileName());
                 //                      //helper.getShell().disconnect();
                                         helper.disconnect();
                                         br = new BufferedReader(new StringReader(exportFileContents));
                                         String str = new String() ;
                                         while((str = br.readLine()) != null)
                                         {
                                                 if(str.contains(SUB_NETWORK_START_TAG)){
                                                         subNetworkStartTag += str + "\n";
 //                                                      LOGGER.info("vsdataContainer :::: "+ vsDataContainer);
                                                 }
                                                 if(str.contains(SUB_NETWORK_END_TAG)){
                                                         subNetworkEndTag += str + "\n";
                                                 }
                                         }
                                         br.close();     
                                 subNetworkStart = subNetworkStartTag.trim().split("\n");
                                 subNetworkEnd = subNetworkEndTag.trim().split("\n");*/
 
                                 subNetworkStartTag = helper.simpleExec("find " + SPACE + EXPORTPATH + SPACE +  " -name " + SPACE  + "\"" + bcgTestData.getFileName() + "\"" + SPACE + " -exec grep -ic " + SPACE + "\"" + SUB_NETWORK_START_TAG.trim() + "\"" + SPACE + " {} " + SPACE + "\\; ");
                                 subNetworkEndTag = helper.simpleExec("find " + SPACE + EXPORTPATH + SPACE +  " -name " + SPACE  + "\"" + bcgTestData.getFileName() + "\"" + SPACE + " -exec grep -ic " + SPACE + "\"" + SUB_NETWORK_END_TAG.trim() + "\"" + SPACE + " {} " + SPACE + "\\;");
                                 
                                 LOGGER.info("subNetworkStart length == " +   subNetworkStartTag.trim());
                                 LOGGER.info("subNetworkEnd length == " +   subNetworkEndTag.trim());
                                 
                                 }
                                 catch(Exception ex){
                                         ex.getMessage();
                                 }
                                 finally{
                 //                      //helper.getShell().disconnect();
                                         helper.disconnect();
                                 }
                                 
                                 if(!(subNetworkStartTag.equalsIgnoreCase(subNetworkEndTag))){
                                         return false;
                                 }
                         }
                 }
                 return true;
         }
 
         /**
          * This method will check the validation of the bcg export test case
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         public boolean exportValidation(BCG_ATT_TR_TestData bcgTestData) {
                 LOGGER.info("*Validation*");
                 String bcgfilesizecommand = "";
                 String exportoutputfilesize = "";
                 boolean isVerifyExitCode = false;
                 int fileSize = 0;
                 int num1 = 0;
         		 int num = 0;
         		 String msg = "" ;
                 try{
                         if(bcgTestData.getCompression() != null){
                                 bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgTestData.getFileName()+".gz | cut -c 0-1";
                         }
                         else{
                                 bcgfilesizecommand = FILESIZE_GREATER_THAN_ZERO_COMMAND + BCG_EXPORT_FILES_DIRECTORY + bcgTestData.getFileName()+ " | cut -c 0-1";
                         }
                         
                         LOGGER.info(bcgfilesizecommand);
                         exportoutputfilesize = helper.simpleExec(bcgfilesizecommand);
                         fileSize = Integer.parseInt(exportoutputfilesize.trim());
                         isVerifyExitCode = verifyExitCode();
                         LOGGER.info("BCG Export filesize : "+fileSize);
                         
                         String error = "Error Info log location" ;
                         String filename = bcgTestData.getFileName();
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
             			String export = "were  successfully exported";
             			String cmd = "grep -i " + "\"" + export+"\""+ BCG_EXPORTLOG_FILES_DIRECTORY +errName;
             			
             			if(fileSize!=0){
             			LOGGER.info(cmd);
             			if ((bcgTestData.getCompression()!="-c")&&(bcgTestData.getCompression()==null)){
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
             			String cmd1 = "grep -i "+ "\""+export2+"\"" +" /var/opt/ericsson/nms_umts_wran_bcg/files/export/"+bcgTestData.getFileName()+" | wc -l";
             			LOGGER.info(cmd1);
             			String cmd2 = "grep -i "+ "\""+export3+"\"" +" /var/opt/ericsson/nms_umts_wran_bcg/files/export/"+bcgTestData.getFileName()+" | wc -l";
            			LOGGER.info(cmd2);
             			String msg1 = helper.simpleExec(cmd1);
             			String msg2 = helper.simpleExec(cmd2);
             			int n = 0;
             			int m = 0;
             			n = Integer.parseInt(msg1.trim());
             			m = Integer.parseInt(msg2.trim());
             			LOGGER.info(bcgTestData.getNodeName());
             			num1 = n+m ;
             			/*if(bcgTestData.getNodeName().equalsIgnoreCase("SubNetwork")){
             			num1 = n + 1 ;
             			}
             			else {
             				num1 = n;
             			}*/
             			LOGGER.info("No of Nodes in xml: "+ num1);
             			}
             			}  
                 }catch(Exception e){
                         LOGGER.info(e.getMessage());
                         LOGGER.info(e);
                 }finally{
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                 }
                 if(fileSize > 0){
                	    if(!(msg.contains("can't open"))){
             			if(num != num1){
             				LOGGER.info("No of nodes are not Equal");
             				return false;
             			}
             			}
                         if(isVerifyExitCode){
                                 if(bcgTestData.getVerifyTestCase() != null){
                                         return verifyTestCase(bcgTestData);
                                 }
                                 return true;
                         }
                 }
                 return false;
         }
         
         public boolean verifyImport(BCG_ATT_TR_TestData bcgTestData){
                 LOGGER.info("*verify Import Test Case*");
                 String grepCommand ;
                 String verifyExportCommand;
                 String verifyExportOutput;
                 String[] updatedField;
                 String moFound = null ;
                 
                 int fileNameLength = bcgTestData.getFileName().length();
                 String errInfoLogFileName = "errInfoLog_"+bcgTestData.getFileName().substring(0, (fileNameLength-4));
                 grepCommand = "ls -lt "+ SPACE+ BCG_IMPORT_LOGS_DIRECTORY+"*"+SPACE+" | grep"+SPACE+errInfoLogFileName;
                 LOGGER.info("files grep command ::: "+ grepCommand);
                 String outputInfoFile = helper.simpleExec(grepCommand);
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 
                 String[] filesErrorInfo = outputInfoFile.split("\n");
                 int startIndexErrorInfo = filesErrorInfo[0].indexOf("/");
                 String importErrorInfoFile = filesErrorInfo[0].substring(startIndexErrorInfo);          
                 int errorInfo = importErrorInfoFile.indexOf("err");
                 String importErrInfoLogFile = importErrorInfoFile.substring(errorInfo);
 
                 if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("administrativeState")){
                         
                         grepCommand = "grep" + SPACE + bcgTestData.getVerifyTestCase() + SPACE + BCG_IMPORT_LOGS_DIRECTORY + importErrInfoLogFile.trim();
                         LOGGER.info("GREP Command :::    " +   grepCommand);
                         moFound = helper.simpleExec(grepCommand);
                         LOGGER.info("Attribute is present in Export File : "+moFound);
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                         if(!moFound.contains(bcgTestData.getVerifyTestCase())){
                                 return false;
                         }
                 }
                 else if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("es:useId")){
                         
                         verifyExportCommand = BCGTOOLEXPORT + SPACE + bcgTestData.getFileName() + SPACE + VSDATA + bcgTestData.getMoName() + SPACE + MINUSN + SPACE + nodeList;
                         LOGGER.info("Export Command :::    " +   verifyExportCommand);
                         verifyExportOutput = helper.simpleExec(verifyExportCommand);
                         LOGGER.info("export out put ::: \n" +verifyExportOutput);
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                         if(verifyExportOutput.contains("Export has succeeded")){        
                                 grepCommand = "grep" + SPACE + bcgTestData.getVerifyTestCase() + SPACE + BCG_EXPORT_FILES_DIRECTORY +bcgTestData.getFileName();
                                 moFound = helper.simpleExec(grepCommand);
                                 updatedField = moFound.split("\n");
         //                      //helper.getShell().disconnect();
                                 helper.disconnect();
                                 if(!(updatedField[0].trim().contentEquals(modifiedUseIdValue.trim())) ){
                                         return false;
                                 }
                         }
                 }
                 else if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("succeeded")){
                         
                         grepCommand = "grep" + SPACE + bcgTestData.getVerifyTestCase() + SPACE + BCG_IMPORT_LOGS_DIRECTORY + importErrInfoLogFile.trim() + SPACE + " | awk '{print $8}'";
                         LOGGER.info("GREP Command :::    " +   grepCommand);
                         moFound = helper.simpleExec(grepCommand);
                         LOGGER.info("Attribute is present in Export File : "+moFound);
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                         if(!moFound.trim().equalsIgnoreCase("succeeded")){
                                 return false;
                         }
                 }
                 else if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("does not exist")){
                         
                         grepCommand = "grep" + SPACE + "\"" +bcgTestData.getVerifyTestCase() +"\"" + SPACE + BCG_IMPORT_LOGS_DIRECTORY + importErrInfoLogFile.trim() ;
                         LOGGER.info("GREP Command :::    " +   grepCommand);
                         moFound = helper.simpleExec(grepCommand);
                         LOGGER.info("Attribute is present in Export File : "+moFound);
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                         if(!moFound.contains(bcgTestData.getVerifyTestCase())){
                                 return false;
                         }
                 }
                 else if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("error count")){
                         
                         grepCommand = "grep -ic" + SPACE + "\"" + "does not exist" +"\"" + SPACE + BCG_IMPORT_LOGS_DIRECTORY + importErrInfoLogFile.trim() ;
                         LOGGER.info("GREP Command :::    " +   grepCommand);
                         moFound = helper.simpleExec(grepCommand);
                         int mos = Integer.parseInt(bcgTestData.getNumberOfMOs().trim());
                         String moCount = Integer.toString((mos * 2) );
                         LOGGER.info("Attribute is present in Export File : "+moFound + moCount);
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                         if( !moFound.trim().equals(moCount) ){
                                 return false;
                         }
                 }
                 else if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("ATTRIBUTE_NUMBER_OUT_OF_RANGE")){
                         
                         grepCommand = "grep " + SPACE + "\"" + "ATTRIBUTE_NUMBER_OUT_OF_RANGE" +"\"" + SPACE + BCG_IMPORT_LOGS_DIRECTORY + importErrInfoLogFile.trim() ;
                         LOGGER.info("GREP Command :::    " +   grepCommand);
                         moFound = helper.simpleExec(grepCommand);
                         LOGGER.info("Attribute is present in Export File : "+moFound );
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                         if( !moFound.contains(bcgTestData.getVerifyTestCase()) ){
                                 return false;
                         }
                 }
                 else if (bcgTestData.getVerifyTestCase().equalsIgnoreCase("es:useId")){
                         
                         verifyExportCommand = BCGTOOLEXPORT + SPACE + bcgTestData.getFileName() + SPACE + VSDATA + bcgTestData.getMoName() + SPACE + MINUSN + SPACE + nodeList;
                         LOGGER.info("Export Command :::    " +   verifyExportCommand);
                         verifyExportOutput = helper.simpleExec(verifyExportCommand);
                         LOGGER.info("export out put ::: \n" +verifyExportOutput);
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                         if(verifyExportOutput.contains("Export has succeeded")){        
                                 grepCommand = "grep" + SPACE + bcgTestData.getVerifyTestCase() + SPACE + BCG_EXPORT_FILES_DIRECTORY +bcgTestData.getFileName();
                                 moFound = helper.simpleExec(grepCommand);
                                 updatedField = moFound.split("\n");
         //                      //helper.getShell().disconnect();
                                 helper.disconnect();
                                 if(!(updatedField[0].trim().contentEquals(modifiedUseIdValue.trim())) ){
                                         return false;
                                 }
                         }
                 }
                 return true;
                 
         }
         
         /**
          * @param fileName Name of Bcg Export File
          * @param minRate Minimum acceptable rate per second for the export
          * @return Whether MO rate per second is above minRate
          */
         public double verifyExportRateAboveMinimum(String Filename) {
 
                 StringBuilder command;
                 double moPerSecond = 0.0;
                 String instrumentationFileName = Filename.replace(".xml", "") + "_instrument.txt"; 
                 String getFileName = "ls -t /var/opt/ericsson/nms_umts_wran_bcg/logs/export | grep " + instrumentationFileName ;
                 String fileName = helper.simpleExec(getFileName);
                 LOGGER.info("File Name : "+ fileName);
                 if(!fileName.isEmpty()){
                         String[] oneFileName = fileName.split("\n");
                         LOGGER.info("oneFileName ::: " + oneFileName[0]);
                         command = new StringBuilder("grep ").append(BCG_INSTRUMENTATION_RATE_GREP_STRING).append(BCG_EXPORT_LOGS_DIRECTORY).append(oneFileName[0].trim()).append(" | awk '{print $5}'");
                         String moRate = helper.simpleExec(command.toString());
                         LOGGER.info("MO/Sec for Export ::: "+moRate);
                         moPerSecond = moPerSecond + Double.parseDouble(moRate);
                 }
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 
                 return moPerSecond;
         }
 
         public double[] getMoRate(String Filename){
                 
                 double [] allMoRate = new double[successfulExportCount];
                 StringBuilder getMoRateCommand;
                 String instrumentationFileName = Filename.replace(".xml", "") + "_instrument.txt"; 
                 String getFileName = "ls -t /var/opt/ericsson/nms_umts_wran_bcg/logs/export | grep " + instrumentationFileName;
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
          * @param fileName Name of Bcg Import File
          * @param time taken for import
          * @return Whether time taken for import is above Import Time
          */
         public long verifyTotalImportTime(String fileName) {
 
                 StringBuilder command;
                 long totalImportTime = 0 ;
                 String instrumentationFileName = fileName.replace(".xml", "") + "_instrument.txt"; 
                 String getFileName = "ls -t /var/opt/ericsson/nms_umts_wran_bcg/logs/import | grep " + instrumentationFileName ;
                 String fileName1 = helper.simpleExec(getFileName);
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 
                 LOGGER.info("File Name : "+ fileName1);
                 if(!fileName1.isEmpty()){
                         String[] oneFileName = fileName1.split("\n");
                         LOGGER.info("oneFileName ::: " + oneFileName[0]);
                         command = new StringBuilder("grep ").append(BCG_INSTRUMENTATION_IMPORT_TIME_GREP_STRING).append(BCG_IMPORT_LOGS_DIRECTORY).append(oneFileName[0].trim()).append(" | awk '{print $8}'");
                         LOGGER.info("grep command :: \n " + command.toString() );
                         String importTime = helper.simpleExec(command.toString());
                         LOGGER.info("Time taken for Import ::: "+importTime);
                         importTime = importTime.trim().toString();
                         totalImportTime = totalImportTime + Long.parseLong(importTime.trim().toString());
                         LOGGER.info("total import time ::: "+ totalImportTime);
                 }
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 
                 return totalImportTime;
         }
         
         /**
          * @param exitCode from method getExitValue()
          * @return Whether Exit code of command is 0 or not
          */
         boolean verifyExitCode() {
                 int exitCode = helper.getCommandExitValue();
                 return exitCode == 0;
 
         }
         
         @Override
         public boolean prepareImportFile(BCG_ATT_TR_TestData bcgTestData) {             
                 boolean createImportFileResult = false;
                 
                 if(isExportResult){
                         String Aal2PathVccTp_ModF[] = {bcgTestData.getUpdateField()};
                         String EUtranCellFDD[] = {bcgTestData.getUpdateField()};
                         String EUtranFreqRelation[] = {bcgTestData.getUpdateField()};
                         String UtranCell[] = {bcgTestData.getUpdateField()};
                         
                         if(bcgTestData.getMoName().equalsIgnoreCase("Aal2PathVccTp")){
                                 createImportFileResult = createImportFile(Aal2PathVccTp_ModF,bcgTestData.getModifier(),bcgTestData.getFileName(),bcgTestData.getMoName(),bcgTestData);
                         }
                         else if(bcgTestData.getMoName().equalsIgnoreCase("EUtranCellFDD")){
                                 createImportFileResult = createImportFile(EUtranCellFDD,bcgTestData.getModifier(),bcgTestData.getFileName(),bcgTestData.getMoName(),bcgTestData);
                         }
                         else if(bcgTestData.getMoName().equalsIgnoreCase("UtranCell")){
                                 createImportFileResult = createImportFile(UtranCell,bcgTestData.getModifier(),bcgTestData.getFileName(),bcgTestData.getMoName(),bcgTestData);
                         }
                         else if(bcgTestData.getMoName().equalsIgnoreCase("EUtranFreqRelation")){
                                 createImportFileResult = createImportFile(EUtranFreqRelation,bcgTestData.getModifier(),bcgTestData.getFileName(),bcgTestData.getMoName(),bcgTestData);
                         }
                         if(createImportFileResult){
                                 return true;
                         }
                 }
                 return createImportFileResult;
         }
         
         public boolean createImportFile(String[] mandatoryFields, String operation, String filename, String tagName,BCG_ATT_TR_TestData bcgTestData)
         {
                 LOGGER.info("create import file  ::");
                 final String START_TAGS[] = {"<es:vsData"+tagName+">", "<es:vsData"+tagName+"/>"};
                 final String END_TAGS[] = {"<es:vsData"+tagName+"/>", "</es:vsData"+tagName+">"};
                 final String VS_DATA_CONTAINER = "<xn:VsDataContainer id=";
                 final String UTRANCELL_START_TAG = "<un:"+tagName + " id=";
                 final String ATTRIBUTE_TAGS[] = {"<un:attributes>" , "</un:attributes>"};
                 final String CID_ATTRIBUTE[] = {"<un:cId>" , "</un:cId>" ,"<un:cId>"+"1230000"+"</un:cId>\n" };
                 
                 
                 int count = 0;
                 BufferedReader br = null;
                 String strS1 = "";
                 
                 try{
                         String exportFileContents = helper.simpleExec("cat "+ EXPORTPATH + filename);
                         
                         br = new BufferedReader(new StringReader(exportFileContents));
                         String str = new String() ;
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
                         String sub1 = "";
                 String subReplace = "";
                 String subReplace1 = "";
                 String utranCellAttributes = "" ;
                 count = 0;
                 count = 0;
                 String subTag = "";
                 String element = "";
                 
                         if(strS1.indexOf(START_TAGS[count]) > -1 || strS1.indexOf(START_TAGS[++count]) > -1){
                                 sub = strS1.substring(0, strS1.indexOf(START_TAGS[count]));
                                 sub1 = strS1.substring(0, strS1.indexOf(START_TAGS[count]));
                                 if(sub.lastIndexOf(VS_DATA_CONTAINER) >  -1)
                                 {
                                         if(bcgTestData.getMoName().equalsIgnoreCase("UtranCell") ){
                                                 if((bcgTestData.getModifier().equalsIgnoreCase("delete"))){
                                                         sub = sub.substring(sub.lastIndexOf(VS_DATA_CONTAINER), sub.length());
                                                         sub = sub.substring(0, sub.indexOf(">") + 1);
                                                         modifierString = sub.substring(0, sub.indexOf("=") + 1);
                                                         subReplace = modifierString.concat("\""+"66667"+"\""+"   modifier="+"\""+operation+"\">");
                                                         modifierString = modifierString.concat(" modifier="+"\""+operation+"\">");
                                                         strS1 = strS1.replace(sub, subReplace);
                                                 }
                                                 /*if((bcgTestData.getModifier().equalsIgnoreCase("update"))){
                                                         sub = sub.substring(sub.lastIndexOf(VS_DATA_CONTAINER), sub.length());
                                                         sub = sub.substring(0, sub.indexOf(">") + 1);
                                                         modifierString = sub.substring(0, sub.indexOf("=") + 1);
                                                         subReplace = modifierString.concat("\""+"66667"+"\""+"   modifier="+"\""+operation+"\">");
                                                         modifierString = modifierString.concat(" modifier="+"\""+operation+"\">");
                                                         strS1 = strS1.replace(sub, subReplace);
                                                 }*/
                                         }
                                         if(bcgTestData.getMoName().equalsIgnoreCase("UtranCell") && (bcgTestData.getModifier().equalsIgnoreCase("create"))){
                                                 sub = sub.substring(sub.indexOf(UTRANCELL_START_TAG), sub.indexOf(ATTRIBUTE_TAGS[0]));
                                                 LOGGER.info("sub  ::::" + sub);
                                                 sub = sub.trim().substring(0, sub.indexOf(">") +1 );
                                                 LOGGER.info("second sub  :::: " + sub);
                                                 modifierString = sub.substring(0, sub.indexOf("=") + 1);
                                                 subReplace = modifierString.concat("\""+"RNC-01-1-10002"+"\""+"   modifier="+"\""+operation+"\">");
                                                 modifierString = modifierString.concat(" modifier="+"\""+operation+"\">");
                                                 strS1 = strS1.replace(sub, subReplace);
                                                 
                                                 sub1 = sub1.substring(sub1.lastIndexOf(VS_DATA_CONTAINER), sub1.length());
                                                 sub1 = sub1.substring(0, sub1.indexOf(">") + 1);
                                                 modifierString = sub1.substring(0, (sub1.length()-1));
                                                 subReplace1 = modifierString.concat(" modifier="+"\""+operation+"\">");
                                                 modifierString = modifierString.concat(" modifier="+"\""+operation+"\">");
                                                 strS1 = strS1.replace(sub1, subReplace1);
                                                 utranCellAttributes = strS1.substring(strS1.indexOf(CID_ATTRIBUTE[0]) , (strS1.indexOf(CID_ATTRIBUTE[1] ))+CID_ATTRIBUTE[1].length());
                                                 LOGGER.info("utranCellAttributes  ::: " +utranCellAttributes);
                                                 strS1 = strS1.replace(utranCellAttributes, CID_ATTRIBUTE[2]);
                                                 
                                                 
                                         }
                                         else{
                                                 sub = sub.substring(sub.lastIndexOf(VS_DATA_CONTAINER), sub.length());
                                                 sub = sub.substring(0, sub.indexOf(">") + 1);
                                                 modifierString = sub.substring(0, (sub.length()-1));
                                                 subReplace = modifierString.concat(" modifier="+"\""+operation+"\">");
                                                 modifierString = modifierString.concat(" modifier="+"\""+operation+"\">");
                                                 strS1 = strS1.replace(sub, subReplace);
                                                 if(bcgTestData.getMoName().equalsIgnoreCase("EUtranFreqRelation")){
                                                         strS1 = strS1.replaceFirst(subReplace, sub);
                                                 }
                                         }
                                                 
                                 }
                         }
                                 if((mandatoryFields != null) ){
                                         element = strS1.substring(strS1.indexOf(START_TAGS[0]) +START_TAGS[0].length() + 1 ,strS1.indexOf(END_TAGS[1]));
                                         for(int i = 0 ; i < mandatoryFields.length; i++){
                                                 String sTag = "<"+mandatoryFields[i]+">";
                                                 String eTag = "</"+mandatoryFields[i]+">";
                                                 String nTag = "<"+mandatoryFields[i]+"/>";
                                                 if(element.contains(sTag)){
                                                         String tag = element.substring(element.indexOf(sTag), element.indexOf(eTag) +eTag.length()) +"\n";
                                                         if(tag.contains("<es:timerCu>"))
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
                                                         if(tag.contains("<es:vclTpId>"))
                                                         {
                                                                 String newTag = element.substring(element.indexOf(sTag), element.indexOf(eTag)+eTag.length());
                                                                 if(newTag.equalsIgnoreCase(updateFieldValue[0])){
                                                                         tag = "<es:vclTpId>"+updateFieldValue[1]+"</es:vclTpId>\n";
                                                                 }
                                                                 else{
                                                                         tag = "<es:vclTpId>"+updateFieldValue[0]+"</es:vclTpId>\n";
                                                                 }
                                                         }
                                                         if(tag.contains("<es:useId>"))
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
                                                         if(tag.contains("<es:userLabel>"))
                                                         {
                                                                 tag = "<es:userLabel>"+"OSS-66667"+"</es:userLabel>\n";
                                                         }
                                                         if(tag.contains("<es:locRegAcb>"))
                                                         {
                                                                 tag = "<es:locRegAcb>"+"1"+"</es:locRegAcb>\n";
                                                         }
                                                         if(tag.contains(","))
                                                         {
                                                                 tag = tag.replace(",", "||");
                                                         }
                                                         subTag += tag;
                                                         modifiedUseIdValue = tag;
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
                                 LOGGER.info("\n subTag    :::: "+strS1);
                                 if(!formatXMLFile(strS1, operation, filename)){
                                         return false;
                                 }
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
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                 }
                 
                 return true;
                 
         }
         
         /**
          * This method for BCG import
          * @param ImportFileName as String
          * @param PlanName as String
          * @return boolean
          */
         public boolean bcgImport(BCG_ATT_TR_TestData bcgTestData){
                 LOGGER.info("BCGIMPORT");
                 String bcgImportcommand = null;
                 if(bcgTestData.getPedAutoLockUnlock() != null){
                         bcgImportcommand =BCGTOOLIMPORT + bcgTestData.getFileName() + MINUSP + bcgTestData.getPlanName();
                 }
                 else{
                         LOGGER.info("verify test case::: "+ bcgTestData.getVerifyTestCase());
                         if(( (bcgTestData.getVerifyTestCase() != null) && (bcgTestData.getVerifyTestCase().equalsIgnoreCase("CLI operation failed")) )){
                                 if(mcOfflineOnline("AM Offline")){
                                         bcgImportcommand =BCGTOOLIMPORT + bcgTestData.getFileName() + MINUSP + bcgTestData.getPlanName() +  SPACE + MOLOCK;
                                 }
                         }else{
                                 bcgImportcommand =BCGTOOLIMPORT + bcgTestData.getFileName() + MINUSP + bcgTestData.getPlanName() +  SPACE + MOLOCK;
                         }
                 }
                 LOGGER.info("BCG Import command : "+bcgImportcommand);
                 String importOutput = helper.simpleExec(bcgImportcommand);
                 LOGGER.info("BCG Import output :::::\n"+ importOutput);
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 if(( (bcgTestData.getVerifyTestCase() != null) && (bcgTestData.getVerifyTestCase().equalsIgnoreCase("CLI operation failed")) )){
                         mcOfflineOnline("AM Online");
                 }
                 if( importOutput.contains(bcgTestData.getExpected()) ){
                         return true;
                 }
                 else{
                         LOGGER.info("Import has failed   :::::::");
                         return false;
                 }
         }
         
         /**
          * This method will check the validation of the bcg import test case
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         public boolean importValidation(BCG_ATT_TR_TestData bcgTestData){
                 boolean isValidated = false;
                 
                 LOGGER.info("Import Validation");
                 if(rollbackFileCreation(bcgTestData)){
                         isValidated = PlanActivation(bcgTestData);
                 }
                 LOGGER.info("Validation Result : "+isValidated);
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 return isValidated;
         }
         
         /**
          * This method will check the verification of the bcg import test case
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         public boolean verifyImportTestCase(BCG_ATT_TR_TestData bcgTestData){
                 LOGGER.info("Verification od Import Test Case");
                 
                 boolean isImportTestCaseVerified = false;
                 if(bcgTestData.getVerifyTestCase() != null){
                         if(isPedIncluded()){
                                 upadePEDParameters("autoLockUnlock", autoLockUnlockBackup);
                                 isImportTestCaseVerified = verifyImport(bcgTestData);
                                 LOGGER.info("Verification of Import Test Case   :::: "  +isImportTestCaseVerified);
                         }
                         else{
                                 if(!bcgTestData.getVerifyTestCase().equalsIgnoreCase("CLI operation failed")){
                                         isImportTestCaseVerified = verifyImport(bcgTestData);
                                 }
                                 if(bcgTestData.getVerifyTestCase().equalsIgnoreCase("CLI operation failed")){
                                         isImportTestCaseVerified = true;
                                 }
                                 LOGGER.info("Verification of Import Test Case   :::: "  +isImportTestCaseVerified);
                         }
                 }
                 return isImportTestCaseVerified;
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
          * This method will revert back all the network changes after
          * running the test case
          * @param bcgTestData
          * @return void
          */
         public void postAction(BCG_ATT_TR_TestData bcgTestData){
                 LOGGER.info("Post Actrion: ");
                 if(bcgTestData.getModifier().equals("delete")){
                         LOGGER.info(" delete part");
                         if(undoPlanCreation(bcgTestData)){
                                 LOGGER.info("undoPlanActivation  -- postAction  :::: " + undoPlanActivation(bcgTestData));
                         }
                         LOGGER.info("removePlan  -- postAction  :::: " + removePlan(bcgTestData));
                         LOGGER.info("removeUndoPlan  -- postAction  :::: " + removeUndoPlan(bcgTestData));
                 }else{
                         LOGGER.info("removePlan  -- postAction-- for update  :::: " + removePlan(bcgTestData));
                 }
                 //helper.getShell().disconnect();
                 helper.disconnect();
         }
         
         /**
          * This method will create the undo plan
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         private boolean rollbackFileCreation(BCG_ATT_TR_TestData bcgTestData){
                 boolean isRollBackFileCreated = false;
                 String BCGundocommand =BCGTOOLUNDO + bcgTestData.getPlanName();
                 LOGGER.info("Rollback command : "+BCGundocommand);
                 String undoplanOutput = helper.simpleExec(BCGundocommand);
                 LOGGER.info("rollback file creation output ::::"  +undoplanOutput);
                 if (undoplanOutput.contains("PrepareUndo successful") || undoplanOutput.contains("No MO found")){ 
                         return true;
                 }
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 return isRollBackFileCreated;
         }
 
         /**
          * This method will activate the plan
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         private boolean PlanActivation(BCG_ATT_TR_TestData bcgTestData){
                 boolean isPlanActivated = false;
                 LOGGER.info("Plan Activation");
                 String planActivationCommand =BCGTOOLPLANACTIVATION + bcgTestData.getPlanName();
                 LOGGER.info("BCG Undo command : "+planActivationCommand); 
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
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         private boolean undoPlanActivation(BCG_ATT_TR_TestData bcgTestData){
                 boolean isUndoPlanActivated = false;
                 String undoPlanActivationCommand =BCGTOOLUNDOPLANACTIVATION + bcgTestData.getPlanName();
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
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         private boolean undoPlanCreation(BCG_ATT_TR_TestData bcgTestData){
                 boolean isUndoPlanCreated = false;
                 String undoPlanActivationCommand =BCGTOOLUNDOPLANCREATION + bcgTestData.getPlanName();
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
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         private boolean removePlan(BCG_ATT_TR_TestData bcgTestData){
                 boolean isPlanRemoved = false;
                 String removePlanCommand =BCGTOOLREMOVEPLAN + bcgTestData.getPlanName();
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
          * @param bcgTestData as BCGTestData
          * @return result as boolean
          */
         private boolean removeUndoPlan(BCG_ATT_TR_TestData bcgTestData){
                 boolean isUndoPlanRemoved = false;
                 String removeUndoPlanCommand =BCGTOOLREMOVEUNDOPLAN + bcgTestData.getPlanName();
                 LOGGER.info("BCG Remove Undo Plan Command : "+removeUndoPlanCommand);
                 String undoPlanAvtivationOutput = helper.simpleExec(removeUndoPlanCommand);
                 LOGGER.info("Remove Undo Plan output :::\n"+undoPlanAvtivationOutput);
                 if (undoPlanAvtivationOutput.contains("successfully deleted")) {                //# nagu changes
                         LOGGER.info("BCG Undo Plans Successfully Deleted  ::::::");
                         return true;
                 }
                 //helper.getShell().disconnect();
                 helper.disconnect();
                 return isUndoPlanRemoved;
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
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                 }
                 return true;
           }
         /**
          * This method will copy the Filer File from Testware to server
          * @return void
          */
         private void copyFiles(){
                 
                 RemoteObjectHandler preCheckRemoteFileHandler = null;
                 if (host1.getHostname().trim().equalsIgnoreCase("ossmaster")) {
                         //host1.setUser("root");
                         //host1.setPass("shroot12");
                         LOGGER.info("Root USer password is : " + rootUser.getPassword());
                         preCheckRemoteFileHandler = new RemoteObjectHandler(host1,rootUser);
                         
                         String pythonscr = FileFinder.findFile("Filter_File.xml").get(0);
                         String remoteFileLocation = CUSTOMFILTER;
                          preCheckRemoteFileHandler.copyLocalFileToRemote(pythonscr, remoteFileLocation);
                 }
 //              RemoteFileHandler remote = new RemoteFileHandler(host,rootUser);
                 /*RemoteObjectHandler remote = new RemoteObjectHandler(host,rootUser);
                 String pythonscr = FileFinder.findFile("Filter_File.xml").get(0);
                 String localFileLocation = pythonscr;
                 String remoteFileLocation = CUSTOMFILTER;
                 LOGGER.info("Copying filter file from local to server ::: "+remote.copyLocalFileToRemote(localFileLocation ,remoteFileLocation));
 */      }
         
 
         public boolean settingCache(String command){
                 boolean result = false;
                 String cacheOutput = "";
                 try{
                         if(command.equalsIgnoreCase("CACHEOFF")){
                                 LOGGER.info("setting Cache OFF ");
                                 LOGGER.info(CACHEOFF);
                                 cacheOutput = helper.simpleExec(CACHEOFF);
                                 LOGGER.info(cacheOutput);
                         }else{
                                 LOGGER.info("setting Cache ON ");
                                 cacheOutput = helper.simpleExec(CACHEON);
                         }
                         String fileRenameOutput= helper.simpleExec(RENAMEFILE);
 
                         if(!(cacheOutput.contains("error") && !(fileRenameOutput.contains("error")))){
                                 helper.simpleExec(BCGMCRESTART);
                                 while(!(helper.simpleExec(BCGSMTOOL)).contains("started")){
                                         helper.disconnect();
                                 }
                                 LOGGER.info("BCG MC is up"+helper.simpleExec(BCGSMTOOL));
                                 result = true;
                         }
 
                 }catch(Exception e){
                         LOGGER.info("Rollback Cache ON ");
                         helper.simpleExec(CACHEON);
                         helper.simpleExec(RENAMEFILE);
                         helper.simpleExec(BCGMCRESTART);
                         LOGGER.info(e.getMessage());
                         LOGGER.info(e);
                 }finally{
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                 }
                 return result;
         }
         
         public boolean mcOfflineOnline(String command){
                 boolean isMCOfflineOrOnline = false;
                 
                 try{
                         if(command.equalsIgnoreCase("AM Offline")){
                                 helper.simpleExec(AMMCOFFLINE);
                                 while(!(helper.simpleExec(AMSMTOOL)).contains("offline")){
                                         helper.disconnect();
                                 }
                                 LOGGER.info("AM MC is down"+helper.simpleExec(AMSMTOOL));
                                 isMCOfflineOrOnline = true;
                         }
                         if(command.equalsIgnoreCase("AM Online")){
                                 helper.simpleExec(AMMCONLINE);
                                 while(!(helper.simpleExec(AMSMTOOL)).contains("started")){
                                         helper.disconnect();
                                 }
                                 LOGGER.info("AM MC is up"+helper.simpleExec(AMSMTOOL));
                                 isMCOfflineOrOnline = true;
                         }
                 }
                 catch(Exception mc){
                         LOGGER.info("Bringing up MC ");
                         helper.simpleExec(AMMCONLINE);
                         LOGGER.info(mc.getMessage());
                         LOGGER.info(mc);
                 }finally{
 //                      //helper.getShell().disconnect();
                         helper.disconnect();
                 }
                 
                 return isMCOfflineOrOnline ;
                 
         }
         
 
         
 }