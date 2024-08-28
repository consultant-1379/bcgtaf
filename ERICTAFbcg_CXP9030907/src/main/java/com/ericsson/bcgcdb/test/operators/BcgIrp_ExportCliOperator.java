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

import com.ericsson.bcgcdb.test.cases.BcgIrp_TestData;


@Operator(context = Context.CLI)
public class BcgIrp_ExportCliOperator implements BcgIrp_ExportOperator {

	Logger LOGGER = Logger.getLogger(BcgIrp_ExportCliOperator.class);

	private static String IRATHOMSH = "/opt/ericsson/oss_irathom_applcontainer/bin/irathom.sh";
	private final String IRATHOMSMTOOL = "/opt/ericsson/nms_cif_sm/bin/smtool -l | grep oss_irathom ";
	private final String IRATHOMMCONLINE = "/opt/ericsson/nms_cif_sm/bin/smtool online oss_irathom ";
	private final String IRATHOMMCOFFLINE = "/opt/ericsson/nms_cif_sm/bin/smtool offline oss_irathom -reason=Other -reasontext=\"IRATHOM offline\"";
	CLICommandHelper helper;

	Shell shell;
	Host host;
	User operUser;
	User rootUser;

	/**
	 * Initializing host, user and cli
	 */
	public BcgIrp_ExportCliOperator(){
		host = HostGroup.getOssmaster();
	    //helper = new CLICommandHelper(host);
//		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
             rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
//		LOGGER.info("host.getIp()"+host.getIp());
		helper = new CLICommandHelper(host, operUser);
	}

	/**
	 * This method will check all the pre-condition for the Test Case
	 * @param bcgTestData
	 * @return result
	 */
	public boolean preAction(BcgIrp_TestData bcgIrp_TestData) {
		LOGGER.info("preAction");

		boolean result = false;

		try{
			LOGGER.info("Setting login for Irathom");
			String serverFiles = helper.simpleExec("cd /home/nmsadm/;ls");
			LOGGER.info("server files : "+serverFiles);
			String settingIrathomsuser = helper.simpleExec("/opt/ericsson/bin/pwAdmin -create IRATHOM FTP "+bcgIrp_TestData.getBulkusername()+" -pw "+bcgIrp_TestData.getBulkPass());
		//	LOGGER.info(settingIrathomsuser);
			String settingBulkIrpuser = helper.simpleExec("/opt/ericsson/bin/pwAdmin -create BULKCMIRP IRP "+bcgIrp_TestData.getBulkusername()+" -pw "+bcgIrp_TestData.getBulkPass());
		//	LOGGER.info(settingBulkIrpuser);

			if((settingIrathomsuser.isEmpty() && settingBulkIrpuser.isEmpty()) ||  (settingIrathomsuser.contains("is already defined in the password") 
					|| settingBulkIrpuser.contains("is already defined in the password")) ){
				LOGGER.info("Setting login for Irathom");
				result = true;
			}
			/*			
			if(bcgIrp_TestData.getFileTransfer().equals("FTP")){
				String corbaStatus = helper.simpleExec("");
				LOGGER.info(corbaStatus);
				//if(corbaStatus.contains(""))
			}
			if(bcgIrp_TestData.getFileTransfer().equals("SFTP")){
				String settingSftp = helper.simpleExec(terminal, commands);
				LOGGER.info(settingSftp);
			}*/
		}catch(Exception e){
			LOGGER.info("Exception while connecting to server " + host.getIp() + " "+ e);
		}
		finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}

		return result;
	}


	public boolean runIrathomRefresh(BcgIrp_TestData bcgIrp_TestData){
		String irathomRefreshCommand;
		boolean result = false;

		try{
			irathomRefreshCommand = IRATHOMSH + " -"+ bcgIrp_TestData.getSubnetwork() +" -node " + bcgIrp_TestData.getFileName() +
					" -ipaddress " + bcgIrp_TestData.getIpaddress()+ " -bulkusername "+bcgIrp_TestData.getBulkusername()+
					" -schedule "+bcgIrp_TestData.getSchedule()+" -rantype "+bcgIrp_TestData.getRantype()+" -rootmo "+"\""+bcgIrp_TestData.getRootmo() +"\"" + " -ignoreduplicates ON";
			LOGGER.info(irathomRefreshCommand);
			String irathomRefreshOutput = helper.simpleExec(irathomRefreshCommand);
			LOGGER.info(irathomRefreshOutput);

			if(irathomRefreshOutput.contains("successful"))
				result = true;

		}catch(Exception e){
			LOGGER.info("Exception while irathom refresh on the server " + e);
		}
		finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}
		return result;
	}

	public boolean validation(BcgIrp_TestData bcgIrp_TestData){
		boolean result = false;

		try{
			String getRefreshStatusCommand = IRATHOMSH + " -listsubnetworkmgr -node " + bcgIrp_TestData.getFileName() + 
					" | grep " + bcgIrp_TestData.getFileName() + " | awk '{print $7}'";
			LOGGER.info(getRefreshStatusCommand);
			String getRefreshStatusOutput = helper.simpleExec(getRefreshStatusCommand);
			LOGGER.info(getRefreshStatusOutput);
			if (getRefreshStatusOutput.contains("READY"))
				result = true;

		}catch(Exception e){
			LOGGER.info("Exception while validation " + e);
		}
		finally{
//			//helper.getShell().disconnect();
			helper.disconnect();
		}

		return result;
	}
	
	public void irathomMCOnlineOffline(String command){
		
		try{
			if(command.equalsIgnoreCase("Online")){
				helper.simpleExec(IRATHOMMCONLINE);
				while(!(helper.simpleExec(IRATHOMSMTOOL)).contains("started")){
					helper.disconnect();
				}
				LOGGER.info("IRATHOM MC is up"+helper.simpleExec(IRATHOMSMTOOL));
			}
			if(command.equalsIgnoreCase("Offline")){
				helper.simpleExec(IRATHOMMCOFFLINE);
				while(!(helper.simpleExec(IRATHOMSMTOOL)).contains("offline")){
					helper.disconnect();
				}
				LOGGER.info("IRATHOM MC is down"+helper.simpleExec(IRATHOMSMTOOL));
			}
		}
		catch(Exception mc){
			LOGGER.info("MAKING IRATHOM MC OFFLINE");
			helper.simpleExec(IRATHOMMCOFFLINE);
			while(!(helper.simpleExec(IRATHOMSMTOOL)).contains("offline")){
				helper.disconnect();
			}
			LOGGER.info(mc.getMessage());
			LOGGER.info(mc);
		}finally{
			helper.disconnect();
		}
				
	}
}
