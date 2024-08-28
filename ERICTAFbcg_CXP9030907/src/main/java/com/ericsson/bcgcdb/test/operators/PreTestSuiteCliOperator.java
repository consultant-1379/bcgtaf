package com.ericsson.bcgcdb.test.operators;

import org.apache.log4j.Logger;

import com.ericsson.bcgcdb.test.cases.BCGKGBExportData;
import com.ericsson.bcgcdb.test.getters.BCGExportGetter;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.cifwk.taf.data.UserType;

@Operator(context = Context.CLI)
public class PreTestSuiteCliOperator implements PreTestSuiteOperator{

	Logger LOGGER = Logger.getLogger(PreTestSuiteCliOperator.class);
	
	Shell shell;
	Host host;
	User operUser;
	User rootUser;
	BCGExportGetter preSuiteGetter;
		
	private static final String TRIGGER_PATH = "/netsim/inst/POC/";
	private static final String FileName = "test_bcg.txt";
	
	//private static final String StartResponseFileName = "/usr/bin/grep -i Start_Response "+TRIGGER_PATH;
		
	//final OssHost ossHost = new OssHost(host);
	//User user = ossHost.getNetsimUser();
	//handler = new CLICommandHelper(host, user); 
	final Host netsimHost = HostGroup.getAllNetsims().get(0);
	final User netsimUser = new User(netsimHost.getUser(UserType.CUSTOM), netsimHost.getPass(UserType.CUSTOM), UserType.CUSTOM);
	CLICommandHelper helper = new CLICommandHelper(netsimHost,netsimUser);
	
	
	final String startScript = "triggerStart.sh";
	final String stopScript = "triggerStop.sh";
	private boolean value = false;
	
	
	
	/*
	 * (non-Javadoc)
	 * @see com.ericsson.bcgcdb.test.operators.PreTestSuiteOperator#onlineSimulation(com.ericsson.bcgcdb.test.cases.BCGKGBExportData)
	 * 
	 */
	
	@Override
	public boolean onlineSimulation(BCGKGBExportData bcgTestData){
		
		String neList[] = bcgTestData.getNE().split(";");
		
		String initialCommand = "rm -rf " + TRIGGER_PATH + FileName + " | touch " + TRIGGER_PATH + FileName;
		
		String permissionCommand = "chmod 777 " + TRIGGER_PATH + startScript;
		String stopscriptpermission = "chmod 777 " + TRIGGER_PATH + stopScript;
		
		LOGGER.info("Creating file on the path :Online  " +  TRIGGER_PATH + " \n "+ initialCommand );
//		LOGGER.info("neList[]   :::Online "+ neList[0] + neList[1]);
		
		helper.execute(initialCommand);
		helper.execute(permissionCommand);
		helper.execute(stopscriptpermission);
		
		for(String ne : neList ){
			LOGGER.info("ne :::::Online" + ne);
			LOGGER.info("echo Online" + ne + " >> " + TRIGGER_PATH + FileName);
			helper.execute("echo " + ne + " >> " + TRIGGER_PATH + FileName);
			LOGGER.info("echo Online" + ne + " >> " + TRIGGER_PATH + FileName +" after");
		}
		LOGGER.info("Waiting for the script to execute....Online");
		LOGGER.info("::::: Online" +  TRIGGER_PATH + startScript + " " + TRIGGER_PATH + FileName) ;
		
		String stopall = ". " + TRIGGER_PATH + stopScript + " " + "all";
		String result1 = helper.execute(stopall);
		LOGGER.info("Executed stop script : "+result1);
		String lsOutPutStop = helper.execute("ls -lrt /netsim/inst/POC/ | grep -i 'Stop_Response' | tail -1");
		LOGGER.info("lsOutPut for StopScript : " + lsOutPutStop);
		
		String command = ". " + TRIGGER_PATH + startScript + " " + TRIGGER_PATH + FileName;
		String result =  helper.execute(command);
		
		if(result.contains("0"))
		{
			value = true;
		}
		else
		{
			value = false;
		}

		String lsOutPut = helper.execute("ls -lrt /netsim/inst/POC/ | grep -i 'Start_Response' | tail -1");
		LOGGER.info("lsOutPut : " +lsOutPut);
		String StartFileName = lsOutPut.substring(lsOutPut.lastIndexOf(" "),lsOutPut.length());
		LOGGER.info("StartFileName : " + StartFileName);
		String outPut = helper.execute("cat "+ TRIGGER_PATH +StartFileName.trim()); 
		
		int i=1;
		while(i<6){
			try{
			Thread.sleep(300000);}
			catch(Exception e){}
			i++;
		}
		
		return value;
	}
	
	@Override
	
	public boolean offlineSimulation(BCGKGBExportData bcgTestData){
		/*
		String neList[] = bcgTestData.getNE().split(";");
		String initialCommand = "rm -rf " + TRIGGER_PATH + FileName + " | touch " + TRIGGER_PATH + FileName;
		LOGGER.info("Creating file on the path :Offline  " +  TRIGGER_PATH + " \n "+ initialCommand );
//		LOGGER.info("neList[]   :::Offline "+ neList[0] + neList[1]);
		sshExecutor.executeCommand(initialCommand);
		for(String ne : neList ){
			LOGGER.info("ne :::::Offline" + ne);
			sshExecutor.executeCommand("echo " + ne + " >> " + TRIGGER_PATH + FileName);
		}
		LOGGER.info("Waiting for the script to execute....Offline");
		LOGGER.info(":::::Offline " +  TRIGGER_PATH + stopScript + " " + TRIGGER_PATH + FileName) ;
//		String result =  sshExecutor.executeCommand(TRIGGER_PATH + stopScript + " " + TRIGGER_PATH + FileName).read();
		
//		LOGGER.info("Result from the script = " + result);
		
		while( (sshExecutor.executeCommand(TRIGGER_PATH + stopScript + " " + TRIGGER_PATH + FileName).getExitValue(3600) != 0 ) ){
			
			 value = true;
		}
		LOGGER.info("after while loop :::" +  value);
		return value;*/
		String stopscriptscriptpermission = "chmod 777 " + TRIGGER_PATH
				+ stopScript;
		String neList[] = bcgTestData.getNE().split(";");
		String initialCommand = "rm -rf " + TRIGGER_PATH + FileName + " | touch " + TRIGGER_PATH + FileName;
		LOGGER.info("Creating file on the path :Offline  " +  TRIGGER_PATH + " \n "+ initialCommand );
		helper.execute(stopscriptscriptpermission);
		helper.execute(initialCommand);
		for(String ne : neList ){
			LOGGER.info("ne :::::Offline" + ne);
			helper.execute("echo " + ne + " >> " + TRIGGER_PATH + FileName);
		}
		LOGGER.info("Waiting for the script to execute....Offline");
		LOGGER.info(":::::Offline " +  TRIGGER_PATH + stopScript + " " + TRIGGER_PATH + FileName) ;
		String result = helper.execute(TRIGGER_PATH + stopScript + " "
				+ "all");
		if (result.contains("0") || result.contains("1")) {
			return true;
		} else {
			return false;
		}
	}
	}
