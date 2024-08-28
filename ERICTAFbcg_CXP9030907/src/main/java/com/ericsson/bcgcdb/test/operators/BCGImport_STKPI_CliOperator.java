package com.ericsson.bcgcdb.test.operators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import com.ericsson.bcgcdb.test.cases.BCGImport_STKPI_TestData;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;

import org.apache.log4j.Logger;

@Operator(context = Context.CLI)
public class BCGImport_STKPI_CliOperator implements BCGImport_STKPI_Operator {

	Shell shell;
	Host host;
	User operUser;
	User rootUser;

	private final String CSTESTLA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice la ";
	private final String BCGTOOLIMPORT = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -i ";
	private final String SPACE = " ";
	private final String BCG_IMPORT_LOGS_DIRECTORY = "/var/opt/ericsson/nms_umts_wran_bcg/logs/import/";
	private final String MINUSP = " -p ";
	private final String IMPORTPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/import/";
	private final String MIRRORMIBSYNCHSTATUS ="mirrorMIBsynchStatus";
	private final String BCGTOOLPLANACTIVATION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -a ";
	private final String EXPORTCOMMAND ="/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -e UtranRelation.xml -d :UtranRelation ";
	private final String BCG_IMPORT_LOGS = "ls /var/opt/ericsson/nms_umts_wran_bcg/logs/import/ | grep ";
	public static final String TEMPDIR = "/tmp/BCG_STKPI/";


	BCGExportGetter bCGexportGetter;
	Logger LOGGER;
	CLICommandHelper helper;
	CLI cli;
	RemoteFileHandler remoteFileHandler;


	String autoLockUnlock;
	String nodeList;
	String bachupAutoLockUnlock;
	StringBuffer stringBuffer;



	/**
	 * Initializing host, user and cli
	 */
	public BCGImport_STKPI_CliOperator(){

		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
		rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
		helper = new CLICommandHelper(host, operUser);
		LOGGER = Logger.getLogger(BCGImport_STKPI_CliOperator.class);
		remoteFileHandler = new RemoteFileHandler(host, rootUser);
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgTestData
	 * @return result
	 */
	public boolean preAction(BCGImport_STKPI_TestData bcgExportTestData) {
	
	String	bcgExport ="";
	createDirectoryInTemp();
	if(bcgExportTestData.getModifier().equalsIgnoreCase("delete")) 
	{
		bcgExport = helper.simpleExec(EXPORTCOMMAND);
	}else{
		LOGGER.info("Import of Create, Export is not required");
		return true;
	}
	 
	 if(bcgExport.contains("Export has succeeded")){
			LOGGER.info("Export Succeeded");
			boolean success = remoteFileHandler.copyRemoteFileToLocal("/var/opt/ericsson/nms_umts_wran_bcg/files/export/UtranRelation.xml",TEMPDIR+"UtranRelation.xml");
			if(success)
			{
				LOGGER.info("Export File successfully transferred to Local");  
				return true;
			}
			else{
				LOGGER.info("Export File transfer to Local failed "); 
			    return false;
			}
		}
		else{
			LOGGER.info("Export Failed");
			return false;
		}
	}

	/**
	 * This method will export for the node
	 * @return result as boolean
	 */
	public boolean bcgImport(BCGImport_STKPI_TestData bcgExportTestData){
		
		boolean BCGImport = false ,  PCAActivation = false ;
		try {
			boolean parsed = false ;
			if(bcgExportTestData.getModifier().equalsIgnoreCase("delete")){
			 parsed = parseExportXMLForUtranRelationDeletion(bcgExportTestData);
            }else {
             parsed = parseExportXMLForUtranRelationCreation(bcgExportTestData);
            }
			if (!parsed){
              	LOGGER.info("Node may be Unsynchronised");
 		     	return false;
            }
		} catch (IOException e) {
			LOGGER.info("IO Exception caught "+e.getMessage());
			return false;
		}
	
		boolean success = remoteFileHandler.copyLocalFileToRemote(bcgExportTestData.getImportFileName(),IMPORTPATH+bcgExportTestData.getImportFileName(),TEMPDIR);
        if(success){
        	LOGGER.info("Transfer successfull ");
        }else{
        	LOGGER.info("Transfer unsuccessfull ");
        	return false ;
        }
        
        String planName = bcgExportTestData.getPlanName()+System.currentTimeMillis();
	    String BCGimportcommand =BCGTOOLIMPORT + bcgExportTestData.getImportFileName() + MINUSP + planName ;
	    LOGGER.info("Import Command :"+BCGimportcommand);	
		String importOutput = helper.simpleExec(BCGimportcommand);
		LOGGER.info("Import Command Output is :"+importOutput);
		if(importOutput.contains("Import has succeeded"))
	    {
			  LOGGER.info("Import has succeeded ,proceeding to Activation ");
			  BCGImport = true ;
		    }
		else{
			LOGGER.info("Import unsuccessfull , not proceeding to Activation ");
			return false;
		}
		String planActivationCommand =BCGTOOLPLANACTIVATION + planName;
		LOGGER.info("Plan Activation CLI command : "+planActivationCommand);
		String planAvtivationOutput = helper.simpleExec(planActivationCommand);
		LOGGER.info("Plan Activation CLI command Output : "+planAvtivationOutput);

		if(planAvtivationOutput.contains("Activation SUCCESSFUL for plan"))
		{
			  LOGGER.info("PCA Activation successfull for the plan :"+planName);
			  PCAActivation = true ;
			}
		else{
			LOGGER.info("PCA Activation failed for the plan :"+planName);
			return false;
		}
		if(BCGImport && PCAActivation){
			LOGGER.info("Import and PCA Activation successfull :"+planName);
			return true ;
		}else{
			LOGGER.info("Either import failed or PCA Activation failed :"+planName);
			return false;
		}
	}
	
	
	public boolean parseExportXMLForUtranRelationDeletion(BCGImport_STKPI_TestData bcgExportTestData) throws IOException{
		
		LOGGER.info("Parsing the Export File ");
		String line ;
		String modifier =bcgExportTestData.getModifier();
		int totalMOs = bcgExportTestData.getMos();
		int moIncrement = 1 ;
		ArrayList<String> MeContext = new ArrayList<String>();
		String[] tags ={"<?xml","<bulkCmConfigDataFile","xmlns:gn","xmlns:un","<fileHeader","<configData","<xn:MeContext",
				"<xn:ManagedElement","<un:RncFunction","<un:UtranCell","</un:UtranCell>","</un:RncFunction>","</xn:ManagedElement>",
				"</xn:MeContext>","<xn:SubNetwork","</xn:SubNetwork>","</configData>","<fileFooter","</bulkCmConfigDataFile>"};

		FileWriter writer = new FileWriter(TEMPDIR+bcgExportTestData.getImportFileName());
		final BufferedReader buffRead = new BufferedReader(new FileReader(TEMPDIR+"UtranRelation.xml"));
		String MeContextId ="" ;	
		String rootSubNetWorkid = "";
		int rootSubNetWorkidcounter = 0;
		
	    while ( (line = buffRead.readLine()) != null) {
	    	 for(String tag : tags){
	    		 if(line.contains(tag)){
	    			  if(line.contains("<fileFooter")){
	    				     writer.write("                        <fileFooter dateTime=\"" +getTime()+"\"/>"+ "\n"); 
	    			     }else{
	    			    	 writer.write(line + "\n");
	    			     }
	    			 break;
	    		 }
	    	 }
	    	 
	    	 if (line.contains("<xn:MeContext id")){
			     MeContextId = line.trim().substring("<xn:MeContext id=\"".length(),line.trim().length() -"\">".length());
		       }
	    	 if (line.contains("<xn:SubNetwork id=") && rootSubNetWorkidcounter<1 ){
	    		 rootSubNetWorkid = line.trim().substring("<xn:SubNetwork id=\"".length(),line.trim().length() -"\">".length());
	    		 rootSubNetWorkidcounter++;
		       }
	    	 if(MeContextId!=null && !MeContextId.equalsIgnoreCase("") && rootSubNetWorkid!=null && !rootSubNetWorkid.equalsIgnoreCase("") 
	    			 && !MeContext.contains(MeContextId))
	    	  { 
	    		 MeContext.add(MeContextId);
	    		 boolean synchronization = isNodeSynchronized(MeContextId,rootSubNetWorkid);
	            if(!synchronization)
	            {
	             	 LOGGER.info("Node "+MeContextId+ "is Unsynchronized"); 
	            	 return false;
	            }
	    	  } 
	         
	         if (line.contains("<un:UtranRelation id") ){
	        	 
	        	    String MeContextValueId = "";
	        	    String adjacentCell = "";
			        String UtranRelationId = line.trim().substring("<un:UtranRelation id=\"".length(),line.trim().length() -"\">".length());
			        
			        	
			        String line1 = buffRead.readLine();
			      
			        String line2 = buffRead.readLine();
		
			        adjacentCell = line2.substring(line2.indexOf("<un:adjacentCell>"),line2.lastIndexOf("</un:adjacentCell>")).replace("<un:adjacentCell>", "");
						       
			        if(!adjacentCell.contains("ExternalUtranCell") && !adjacentCell.equals("")){
			         String[] fdnMOs = adjacentCell.split(",");
			         MeContextValueId = fdnMOs[2].split("=")[1];
			        } else{
			        	continue;
			        }
			        if(bcgExportTestData.getImportType().equalsIgnoreCase("intra") && !MeContextValueId.contains(MeContextId)){
			        	continue;
			        }else if(bcgExportTestData.getImportType().equalsIgnoreCase("inter") && MeContextValueId.contains(MeContextId)){
			        	continue;
			        }
			        else{
				         writer.write("                                <un:UtranRelation id=\""+UtranRelationId+"\"  "+"modifier=\""+modifier+"\">" + "\n");
				         writer.write(line1 + "\n");
				         writer.write(line2 + "\n");
				         line = buffRead.readLine();

				         writer.write(line+ "\n");
				         writer.write( "                                </un:UtranRelation>"+ "\n");
				         
				         moIncrement++;
					        if(moIncrement>totalMOs) {
					        	completeXML(writer);
					           	break;
					           } 
				         continue;
				         
		            
			        }
		 }
	   }
	  writer.close();
	  buffRead.close();
	  return true ;
   }
	
	public boolean parseExportXMLForUtranRelationCreation(BCGImport_STKPI_TestData bcgExportTestData) throws IOException{
		
		LOGGER.info("Parsing the Delete import XML for Create Import XML Creation"); 
		
		FileWriter writer = new FileWriter(TEMPDIR+bcgExportTestData.getImportFileName());
		LOGGER.info("writer ::: " + writer + "Import File name :::" + bcgExportTestData.getImportFileName());
		final BufferedReader buffRead = new BufferedReader(new FileReader(TEMPDIR+bcgExportTestData.getImportFileName().replace("Create", "Delete")));
		LOGGER.info("buffRead ::::::" +buffRead);
		String line ;
		String MeContextId ="" ;	
		String rootSubNetWorkid = "";
		int rootSubNetWorkidcounter = 0;
		ArrayList<String> MeContext = new ArrayList<String>();
		
		while ( (line = buffRead.readLine()) != null) {
			LOGGER.info("reading buffRead ::::");
	    	 if (line.contains("<xn:MeContext id")){
			     MeContextId = line.trim().substring("<xn:MeContext id=\"".length(),line.trim().length() -"\">".length());
		       }
	    	 if (line.contains("<xn:SubNetwork id=") && rootSubNetWorkidcounter<1 ){
	    		 rootSubNetWorkid = line.trim().substring("<xn:SubNetwork id=\"".length(),line.trim().length() -"\">".length());
	    		 rootSubNetWorkidcounter++;
		       }
	    	 if(MeContextId!=null && !MeContextId.equalsIgnoreCase("") && rootSubNetWorkid!=null && !rootSubNetWorkid.equalsIgnoreCase("") 
	    			 && !MeContext.contains(MeContextId))
	    	  { 
	    		 MeContext.add(MeContextId);
	    		 boolean synchronization = isNodeSynchronized(MeContextId,rootSubNetWorkid);
	            if(!synchronization)
	            {
	             	 LOGGER.info("Node "+MeContextId+ "is Unsynchronized"); 
	            	 return false;
	            }
	    	  } 
	         
			
			if(line.contains("delete")){
			   writer.write(line.replace("delete", "create") + "\n");
			}else{
			   writer.write(line + "\n");	
			}
		}
	writer.close();
	buffRead.close();
	return true;
 }
	
	public static void completeXML(FileWriter writer) throws IOException{
		
		 writer.write("                            </un:UtranCell>" + "\n");
		 writer.write("                        </un:RncFunction>" + "\n");
		 writer.write("                    </xn:ManagedElement>" + "\n");
		 writer.write("                </xn:MeContext>" + "\n");
		 writer.write("            </xn:SubNetwork>" + "\n");
		 writer.write("         </xn:SubNetwork>" + "\n");
		 writer.write("      </configData>" + "\n");
		 writer.write("  <fileFooter dateTime=\"" +getTime()+"\"/>"+ "\n");
		 writer.write("</bulkCmConfigDataFile>" + "\n");
	}
	
	public static String getTime(){
		
		final Date tempDate = new Date();
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		final String currentTime = formatter.format(tempDate);
		
		String time = currentTime.substring(0, 4);//year
		   time += "-"+ currentTime.substring(4, 6);//month
		   time += "-"+currentTime.substring(6, 8);//day
		   time += "T"+ currentTime.substring(8, 10);//hour
		   time += ":"+currentTime.substring(10, 12);//minute
		   time += ":"+currentTime.substring(12, 14)+"Z";//sec
		   
		   return time ;
	}
	
	/**
	 * This method will check the required nodes is sync or not
	 * @param fdn as String
	 * @return result as boolean
	 */
	private boolean isNodeSynchronized(String MeContext,String rootSubNetworkid){
		boolean result = false;
		
		String nodeFdn= "SubNetwork="+rootSubNetworkid+",SubNetwork="+MeContext+",MeContext="+MeContext;
		String csTestCommand = CSTESTLA + SPACE + nodeFdn + SPACE + MIRRORMIBSYNCHSTATUS ;
		LOGGER.info("CHecking Node Status : "+csTestCommand);
		String csOutput = helper.simpleExec(csTestCommand);
		LOGGER.info("Node Status : "+csOutput);
		
		if(csOutput.contains("5") || csOutput.contains("3"))
			result = true;
		return result;
	}

	/**
	 * @param fileName Name of Bcg Export File
	 * @param minRate Minimum acceptable rate per second for the export
	 * @return Whether MO rate per second is above minRate
	 */
	public double verifyExportRateAboveMinimum(String Filename , String Mimrate) {

		String resultTime = ""; 
        String resultNumber = "";
        double actualRate;
        double minRate = Double.parseDouble(Mimrate);
        String commandToGetLogs = BCG_IMPORT_LOGS + Filename.replace(".xml", "_instrument.txt");
        String resultLogs = helper.simpleExec(commandToGetLogs.toString()); 
        String instrumentationFileName = fileWriter(resultLogs,Filename.replace(".xml", "_instrument.txt"));
        LOGGER.info("Instrumentation File name :"+instrumentationFileName);
        
        boolean success = remoteFileHandler.copyRemoteFileToLocal(BCG_IMPORT_LOGS_DIRECTORY+instrumentationFileName,TEMPDIR+instrumentationFileName);
        if(success)
        LOGGER.info("Import Log Transferred Successfully");

        try {
                BufferedReader buffRead = new BufferedReader(new FileReader(TEMPDIR+instrumentationFileName));
            String line ;               
                
                        while ( (line = buffRead.readLine()) != null) {
                                
                                if(line.contains("Total time for BCG Import")){
                                        resultTime = line.split("=")[1].trim();
                                }
                                if(line.contains("Total Number of Commands")){
                                        resultNumber = line.split("=")[1].trim();
                                }
                        }
                        
                buffRead.close();
                        
                } catch (IOException e) {
                        LOGGER.info("IO Exception caught during parsing Import Log "+e.getMessage());
                }
        
        double actualTime = Double.parseDouble(resultTime);
        double actualNumber = Double.parseDouble(resultNumber);
        LOGGER.info("Actual Time :" + actualTime);
        LOGGER.info("Actual Number :" + actualNumber);
        actualRate = (actualNumber/actualTime)*1000;
        LOGGER.info("Actual rate value :" + actualRate);
        LOGGER.info("Expected mimrate :" + Mimrate);
        return actualRate ;

	}

	private String fileWriter(String text,String filename)
	{
		
		String logName ="";
		TreeSet<Integer> logsCounter = new TreeSet<Integer>();
		BufferedWriter writer = null;
		try
		{
		    writer = new BufferedWriter( new FileWriter(TEMPDIR+"listoflogs.txt"));
		    writer.write(text);
		    writer.close( );
		    
		    final BufferedReader buffRead = new BufferedReader(new FileReader(TEMPDIR+"listoflogs.txt"));
            String line ;		
			while ( (line = buffRead.readLine()) != null) {
				if(!line.endsWith(".txt")){
				int logCounter = Integer.parseInt(line.substring(line.lastIndexOf(".")+1, line.length()));
				logsCounter.add(logCounter);}
			}
			
			if(logsCounter.size()==0){
				logName = filename;
			}else{
				logName = filename+"."+logsCounter.last();
			}
			buffRead.close();
		}
		catch ( IOException e)
		{
			LOGGER.info("IO Exception caught while finding Import Logs :"+e.getMessage());
		}
	
		return logName;
	}
	
	/**
	 * @param exitCode from method getExitValue()
	 * @return Whether Exit code of command is 0 or not
	 */
	boolean verifyExitCode() {
		int exitCode = shell.getExitValue(-1);
		LOGGER.info("Exit code of export command is exitCode");
		return exitCode == 0;

	}
	
	public void createDirectoryInTemp(){
		if(!new File(TEMPDIR).exists()){
			new File(TEMPDIR).mkdir();
			LOGGER.info("Directory created : "+TEMPDIR);
		}
	}
	
	/**
	 * This method will check the validation of the bcg import test case
	 * @param bcgTestData as BCGTestData
	 * @return result as boolean
	 */
	public double validation(String Filename , String Mimrate){
	   return verifyExportRateAboveMinimum(Filename , Mimrate);
	}
}

