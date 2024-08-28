package com.ericsson.bcgcdb.test.operators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ericsson.bcgcdb.test.getters.TagInfo;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;

@Operator(context = Context.CLI)
public class ImportStkpiCliOperator implements ImportStkpiOperator {


	private final String EXPORTFILENAME ;
	private Host host;
	private User operUser;
	private User rootUser;
	private final static Logger LOGGER = Logger.getLogger(ImportStkpiCliOperator .class);;
	private CLICommandHelper helper;
	private RemoteFileHandler remoteFileHandler;
	private String planNameWithTimeStamp ;
	private int moIncrement = 0 ;
	private HashMap<String,ArrayList<String>> mapOfRncAndRelations = new HashMap<String,ArrayList<String>>();
	private String rncToBeUsed = null;
	private Element rncFunctionStart = null ;
	private Element utranCellStart = null ;
	private Element utranRelationStart = null;
	private Element attributeStart = null;
	private Element adjacentCellStart = null;
	private TagInfo fileHeader = null ;
	private TagInfo bulkCmConfigDataFile = null ;

	/**
	 * Initializing Host, User ,CLICommandHelper ,Logger ,RemoteFileHandler and CSTestHandler
	 */
	public ImportStkpiCliOperator(){

		EXPORTFILENAME = UTRANRELATION+System.currentTimeMillis()+XMLFILEEXTENSION;
		host = DataHandler.getHostByType(HostType.RC);
		operUser = new User(host.getUser(UserType.OPER),host.getPass(UserType.OPER),UserType.OPER);
		rootUser = new User(host.getUser(UserType.ADMIN),host.getPass(UserType.ADMIN),UserType.ADMIN);
		helper = new CLICommandHelper(host, operUser);
		remoteFileHandler = new RemoteFileHandler(host,rootUser);
	}

	@Override
	public String getExportFileName() {
		return EXPORTFILENAME;
	}

	@Override
	public void createDirectoryInTemp(){
		if(!new File(TEMPDIR).exists()){
			new File(TEMPDIR).mkdir();
			LOGGER.info("Directory created : "+TEMPDIR);
		}
	}

	@Override
	public boolean checkPEDParameterStatus(){

		String pedCommand = "/opt/ericsson/nms_cif_sm/bin/smtool -config wran_bcg ExportNodeIfUnsynched";
		String checkPEDParameterStatus = executeCommandAndCloseShell(pedCommand);
		LOGGER.info("Status of PED Parameter :"+checkPEDParameterStatus);

		return checkPEDParameterStatus.contains("true");
	}

	@Override
	public void setPEDParameter(){

		String pedCommand = "/opt/ericsson/nms_cif_sm/bin/smtool -set wran_bcg ExportNodeIfUnsynched false";
		String output = executeCommandAndCloseShell(pedCommand);
		LOGGER.info("Output of the executed command to set the value of PED Parameter "+output);
	}

	@Override
	public String performMOCExport(){

		String exportCommand =  BCGTOOLFOREXPORT+" "+EXPORTFILENAME+" "+MOCFILTER+UTRANRELATION;
		String output = executeCommandAndCloseShell(exportCommand);
		LOGGER.info("Output of the export command :"+output);
		return output;
	}

	@Override
	public int actualExitValue(){

		return helper.getCommandExitValue() ;
	}

	@Override
	public boolean fileExists(String path, String fileName){

		return remoteFileHandler.remoteFileExists(path+fileName);
	}

	@Override
	public boolean copyRemoteXmlFile() {

		return remoteFileHandler.copyRemoteFileToLocal(EXPORTFILEPATH+EXPORTFILENAME,TEMPDIR+EXPORTFILENAME);

	}

	@Override
	public boolean verifyFileSizeGreaterThanZero(String fileName){

		return new File(TEMPDIR+fileName).length()>0;
	}



	@Override
	public boolean createImportFile(String modifier,int totalMOs,
			String importFileName,String importType) throws XMLStreamException, FileNotFoundException,
			FactoryConfigurationError, ParserConfigurationException, TransformerException{



		String tagContent = null;

		String meContextId = null;
		String utranCellId = null;
		String utranRelationId = null;
		boolean meContextFound = false ;
		boolean meContextCheck = false ;

		Set<String> meContext = new LinkedHashSet<String>();

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader( new FileReader(TEMPDIR+EXPORTFILENAME));

		try{
			while(reader.hasNext()){

				if(meContextFound){
					LOGGER.info("Parsing of the other RNC's not required, more than one RNC has already been found .");
					break ;
				}

				if(meContext.size()==1){
					meContextCheck = true ;
				}

				int event = reader.next();

				switch(event){

				case XMLStreamConstants.START_ELEMENT:
					if ("MeContext".equals(reader.getLocalName()) && meContext.size()==0){
						meContextId = reader.getAttributeValue(0);
						meContext.add(meContextId);

					}else if ("MeContext".equals(reader.getLocalName()) && meContext.size()>0 && meContextCheck){
						meContextFound = true;

					}else if ("UtranCell".equals(reader.getLocalName())){
						utranCellId = reader.getAttributeValue(0);

					}else if ("UtranRelation".equals(reader.getLocalName())){
						utranRelationId = reader.getAttributeValue(0);

					}else if ("fileHeader".equals(reader.getLocalName())){
						fileHeader  = new TagInfo(reader,"Start");
						fileHeader.populateAttributeMapFromXML();

					}else if ("bulkCmConfigDataFile".equals(reader.getLocalName())){
						bulkCmConfigDataFile  = new TagInfo(reader,"Start");
						bulkCmConfigDataFile.populateAttributeMapFromXML();
						bulkCmConfigDataFile.populateNameSpaceQueueFromXML();

					}
					break;

				case XMLStreamConstants.CHARACTERS:
					tagContent = reader.getText().trim();
					break;

				case XMLStreamConstants.END_ELEMENT:
					if(reader.getLocalName().equalsIgnoreCase("adjacentCell")){
						seperateUtranRelations(meContextId,utranCellId,tagContent,utranRelationId,importType);
					}
					break;
				}
			}
		}
		finally{
			reader.close();
		}

		rncToBeUsed = findRNC(totalMOs);
		LOGGER.info("RNC to be used is:"+rncToBeUsed);

		boolean importFileCreationSuccess = writeXML(importType,meContextId,modifier,totalMOs,importFileName);
		return importFileCreationSuccess ;
	}

	private String findRNC(int totalMOs){

		for (String rncName : mapOfRncAndRelations.keySet()){
			if(mapOfRncAndRelations.get(rncName).size()>= totalMOs){
				return rncName ;
			}
		}
		LOGGER.error("Two RNC not found having :"+totalMOs +"Utran Relations");
		return null ;
	}

	private void seperateUtranRelations(String meContextId,String utranCellId,String adjacentCell,
			String utranRelationId,String importType){

		String meContextValueId = null ;

		if(!adjacentCell.isEmpty() && !adjacentCell.contains("ExternalUtranCell")){
			String[] fdnMOs = adjacentCell.split(",");
			meContextValueId = fdnMOs[2].split("=")[1];

		} else{
			return;
		}

		if(importType.equalsIgnoreCase("intra") && !meContextValueId.contains(meContextId)){
			return;

		}else if(importType.equalsIgnoreCase("inter") && meContextValueId.contains(meContextId)){
			return;

		}
		else{
			ArrayList<String> relationsList = mapOfRncAndRelations.get(meContextValueId);
			if (relationsList == null)
			{
				relationsList = new ArrayList<String>();
			}
			relationsList.add(utranCellId+utranRelationId+adjacentCell);
			mapOfRncAndRelations.put(meContextValueId, relationsList);
		}
	}

	private boolean writeXML(String importType,String meContextId,String importModifier,int totalMOs,String importFileName) throws XMLStreamException,
	FileNotFoundException, FactoryConfigurationError, ParserConfigurationException, TransformerException{

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader( new FileReader(TEMPDIR+EXPORTFILENAME));

		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		documentFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element rootElement = null ;

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");

		try {

			rootElement = createRootAppendChild(document ,rootElement) ;
			while(reader.hasNext()){

				int event = reader.next() ;

				if(event==XMLStreamConstants.START_ELEMENT && (reader.getPrefix()+":"+reader.getLocalName()).equals("un:UtranCell")){
					rootElement = writeUtranRelation(reader,importType,meContextId,importModifier,totalMOs,rootElement,document);
					continue;
				}

				if(event==XMLStreamConstants.START_ELEMENT && checkTag(reader.getPrefix()+":"+reader.getLocalName()) && moIncrement<totalMOs){
					TagInfo startTag = new TagInfo(reader,"Start");
					startTag.populateAttributeMapFromXML();
					rootElement = createElement(document,startTag,rootElement,null,importModifier);

				}
			}

			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(new File(TEMPDIR+importFileName));
			transformer.transform(domSource, streamResult);
		}
		finally{
			reader.close();
		}
		return true ;
	}


	private Element createElement(Document document ,TagInfo tag,Element element,Queue<TagInfo> queue,String modifier){


		Element childElement = null ;

		if(tag.getLocalName().equals("RncFunction") && tag.getTagType().equals("Start"))
		{
			rncFunctionStart = document.createElement(tag.getTagPrefix()+":"+tag.getLocalName());
			addAttributes ( document , tag, rncFunctionStart);
			element.appendChild(rncFunctionStart);
			return element;
		}
		if(tag.getLocalName().equals("UtranCell") && tag.getTagType().equals("Start")){

			utranCellStart = document.createElement(tag.getTagPrefix()+":"+tag.getLocalName());
			addAttributes ( document , tag, utranCellStart);
			return element;
		}

		if(tag.getLocalName().equals("UtranRelation") && tag.getTagType().equals("Start")){

			utranRelationStart = document.createElement(tag.getTagPrefix()+":"+tag.getLocalName());
			addAttributes ( document , tag, utranRelationStart);
			Attr attribute = document.createAttribute("modifier");
			attribute.setValue(modifier);
			utranRelationStart.setAttributeNode(attribute);
			return element;
		}

		if(tag.getLocalName().equals("attributes") && tag.getTagType().equals("Start")){

			attributeStart = document.createElement(tag.getTagPrefix()+":"+tag.getLocalName());
			addAttributes ( document , tag, attributeStart);

			return element;
		}

		if(tag.getLocalName().equals("adjacentCell") && tag.getTagType().equals("Start")){

			adjacentCellStart = document.createElement(tag.getTagPrefix()+":"+tag.getLocalName());
			adjacentCellStart.appendChild(document.createTextNode(queue.poll().getCharacterContent()));
			attributeStart.appendChild(adjacentCellStart);
			utranRelationStart.appendChild(attributeStart);
			utranCellStart.appendChild(utranRelationStart);
			return element;
		}

		if(tag.getLocalName().equals("UtranCell") && tag.getTagType().equals("End")){

			rncFunctionStart.appendChild(utranCellStart);
			return rncFunctionStart;
		}

		if(!tag.getTagPrefix().isEmpty()){
			childElement = document.createElement(tag.getTagPrefix()+":"+tag.getLocalName());
		}else {
			childElement = document.createElement(tag.getLocalName());
		}

		element.appendChild(childElement);
		addAttributes (document ,tag,childElement);
		if(tag.getLocalName().equals("configData")){

			Element fileFooter = document.createElement("fileFooter");
			fileFooter.setAttribute("dateTime", getTime());
			element.appendChild(fileFooter);
		}

		return childElement;

	}

	private Element createRootAppendChild(Document document ,Element childElement){

		childElement = document.createElement(bulkCmConfigDataFile.getLocalName());
		childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:un", bulkCmConfigDataFile.getNameSpaceQueue().poll());
		childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:es", bulkCmConfigDataFile.getNameSpaceQueue().poll());
		childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xn",bulkCmConfigDataFile.getNameSpaceQueue().poll());
		childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:gn", bulkCmConfigDataFile.getNameSpaceQueue().poll());
		childElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", bulkCmConfigDataFile.getNameSpaceQueue().poll());
		document.appendChild(childElement);
		Element fileHeaderTag = document.createElement("fileHeader");
		fileHeaderTag.setAttribute("fileFormatVersion", fileHeader.getAttributeMap().get("fileFormatVersion"));
		fileHeaderTag.setAttribute("vendorName", "Ericsson");
		childElement.appendChild(fileHeaderTag);

		return childElement;

	}

	private void addAttributes (Document document ,TagInfo tag,Element childElement){

		Set<String> attributeNames = tag.getAttributeMap().keySet();
		for(String attributeName : attributeNames ){
			Attr attribute = document.createAttribute(attributeName);
			attribute.setValue(tag.getAttributeMap().get(attributeName));
			childElement.setAttributeNode(attribute);
		}

	}

	private Element writeUtranRelation(XMLStreamReader reader,String importType,String meContextId,
			String modifier,int totalMOs,Element rootElement,Document document) throws XMLStreamException {

		Queue<TagInfo> queue = new LinkedList<TagInfo>();
		String tagName = null;
		String adjacentCell = null ;
		boolean relationCheck = false;
		boolean utranCellCheck = true;

		TagInfo utranCellStartTag = new TagInfo(reader,"Start");
		utranCellStartTag.populateAttributeMapFromXML();

		TagInfo utranRelationStart = null;
		TagInfo attributeStart = null;
		TagInfo adjacentCellStart = null;
		TagInfo adjacentCellContent = null ;

		while (reader.hasNext()) {

			int event = reader.next();
			if(reader.isWhiteSpace()){
				continue;
			}

			switch(event){

			case XMLStreamConstants.START_ELEMENT:

				tagName = reader.getPrefix()+":"+reader.getLocalName();
				if(tagName.equals("un:UtranRelation")){
					utranRelationStart = new TagInfo(reader,"Start");
					utranRelationStart.populateAttributeMapFromXML();
					queue.add(utranRelationStart);
				}else if(tagName.equals("un:attributes")){
					attributeStart =  new TagInfo(reader,"Start");
					queue.add(attributeStart);
				}else if(tagName.equals("un:adjacentCell")){
					adjacentCellStart =  new TagInfo(reader,"Start");
					queue.add(adjacentCellStart);
				}

				break ;

			case XMLStreamConstants.CHARACTERS:
				if(tagName != null && tagName.equals("un:adjacentCell")){
					adjacentCell = reader.getText();
					adjacentCellContent =  new TagInfo(reader);
					queue.add(adjacentCellContent);
				}
				break;

			case XMLStreamConstants.END_ELEMENT:

				tagName = reader.getPrefix()+":"+reader.getLocalName();

				if(tagName.equals("un:UtranRelation")){
					relationCheck = checkRelation(adjacentCell,importType,meContextId);

					if(relationCheck && moIncrement<totalMOs){

						if(utranCellCheck){
							rootElement = createElement(document ,utranCellStartTag,rootElement,null,modifier);
							utranCellCheck = false;
						}

						while (queue.size()>0){
							rootElement = createElement(document ,queue.poll(),rootElement,queue,modifier);
						}
						moIncrement++;
						adjacentCell = "";
					}
					if(!relationCheck){queue.clear();}
				}else if(tagName.equals("un:UtranCell")){
					if(!utranCellCheck){
						rootElement = createElement(document ,new TagInfo(reader,"End"),rootElement,null,modifier);
					}
					return rootElement ;
				}
				break ;
			}
		}
		return rootElement;
	}

	private boolean checkRelation(String adjacentCell,String importType,String meContextId){

		String meContextValueId = "";

		if(adjacentCell != null && !adjacentCell.isEmpty() && !adjacentCell.contains("ExternalUtranCell")){
			String[] fdnMOs = adjacentCell.split(",");
			meContextValueId = fdnMOs[2].split("=")[1];
		} else{
			return false;
		}

		if(importType != null && importType.equalsIgnoreCase("intra") && !meContextValueId.contains(meContextId)){
			return false;
		}else if(importType != null && importType.equalsIgnoreCase("inter") && meContextValueId.contains(meContextId)){
			return false;
		}else if (meContextValueId.equalsIgnoreCase(rncToBeUsed)){
			return true;
		}
		return false;
	}

	private boolean checkTag(String tagName){

		boolean tagFound = false;
		for(String tag : TAGS){
			if(tagName.contains(tag)){
				return true ;
			}
		}
		return tagFound;
	}

	private String getTime(){

		final Date tempDate = new Date();
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		final String currentTime = formatter.format(tempDate);
		return currentTime ;

	}

	@Override
	public boolean verifyImportFileExistOnLocal(String importFile){

		return new File(TEMPDIR+importFile).exists();
	}

	@Override
	public boolean verifyTotalImportCommands(String importFile,String modifier,int totalMOs){

		String totalCommands = executeCommandAndCloseShell(String.format("grep -i '%s' %s | wc -l", modifier, IMPORTFILEPATH+importFile)).split("\\n")[0].trim();
		int importCommands = Integer.parseInt(totalCommands);
		return  importCommands == totalMOs ;
	}

	@Override
	public boolean copyLocalXmlFile(String importFile) {

		return remoteFileHandler.copyLocalFileToRemote(importFile,IMPORTFILEPATH+importFile,TEMPDIR);
	}

	@Override
	public String performImport(String importFileName, String planName){

		planNameWithTimeStamp = planName+System.currentTimeMillis();
		String importCommand =  BCGTOOLFORIMPORT+" "+importFileName+" "+"-p"+planNameWithTimeStamp;
		String output = executeCommandAndCloseShell(importCommand);
		LOGGER.info("Executed the Import command :"+output);
		return output;
	}


	@Override
	public double verifyImportMoRate(String importFileName,int totalMOs){

		String getLogFileName ="/var"+ executeCommandAndCloseShell(String.format("ls -ltr %s_instrument.txt* | tail -1",IMPORTLOGSPATH+importFileName.replace(".xml", ""))).split("\\n")[0].trim().split("var")[1];
		LOGGER.info("Import Log File Name :"+getLogFileName);

		String getTotalTimeInMilliSec = executeCommandAndCloseShell(String.format("grep -i 'Total time for BCG Import (ms) = ' %s  | awk '{print $8}'",getLogFileName)).split("\\n")[0].trim();
		LOGGER.info("Total Time taken in Import :"+getTotalTimeInMilliSec);

		String getTotalSuccess = executeCommandAndCloseShell(String.format("grep -i 'Total Number of Commands = ' %s  | awk '{print $6}'",getLogFileName)).split("\\n")[0].trim();
		LOGGER.info("Total successful commands in Import :"+getTotalSuccess);

		double totalTimeInSec = Double.parseDouble(getTotalTimeInMilliSec)/1000;
		double totalsuccess = Double.parseDouble(getTotalSuccess);

		if(totalsuccess != totalMOs){
			LOGGER.error("Total Successful commands: "+ totalsuccess+"is not equal to Total Number of MOs from CSV :"+totalMOs);
			return -1;
		}

		return totalsuccess/totalTimeInSec;
	}

	private String executeCommandAndCloseShell(String commandToExecute){
		String commandOutput = helper.simpleExec(commandToExecute);
		//helper.getShell().disconnect();
		helper.disconnect();
		return commandOutput;
	}

	@Override
	public void exitCleanUp(String importFileName){

		LOGGER.info("Deletion of Export File on Remote Location is "+(remoteFileHandler.deleteRemoteFile(EXPORTFILEPATH+EXPORTFILENAME) ? "Successful":"UnSuccessful"));
		LOGGER.info("Deletion of Import File on Remote Location is "+(remoteFileHandler.deleteRemoteFile(IMPORTFILEPATH+importFileName) ? "Successful":"UnSuccessful"));
		new File(TEMPDIR+EXPORTFILENAME).delete();

	}

	@Override
	public String performActivation(){
		String importCommand =  BCGTOOLFORACTIVATINGPLAN+" "+planNameWithTimeStamp;
		String output = executeCommandAndCloseShell(importCommand);
		LOGGER.info("Executed the Import command :"+output);
		return output;
	}

	@Override
	public String deletePlan(){
		String output = executeCommandAndCloseShell(BCGTOOLFORREMOVINGPLAN+planNameWithTimeStamp);
		LOGGER.info("Deletion of the plan  :"+output);
		return output;
	}

	@Override
	public void convertCreateFileToDeleteFile(String importFile, String intermediateFile) throws IOException{

		String line ;
		BufferedReader bufferReader = null ;
		FileWriter writer = null ;

		try {

			bufferReader = new BufferedReader(new FileReader(TEMPDIR+importFile));
			writer = new FileWriter(TEMPDIR+intermediateFile);

			while ( (line = bufferReader.readLine()) != null) {

				if(line.contains("modifier=\"create\"")){
					writer.write(line.replaceAll("create", "delete")+ "\n");
				}else{
					writer.write(line + "\n");
				}
			}
		}
		finally{
			bufferReader.close();
			writer.close();
		}
	}

	@Override
	public void exitCleanUpForCreation(String intermediateImportFileName){

		LOGGER.info("Deletion of Export File on Remote Location is "+(remoteFileHandler.deleteRemoteFile(EXPORTFILEPATH+EXPORTFILENAME) ? "Successful":"UnSuccessful"));
		LOGGER.info("Deletion of Intermediate Delete Import File on Remote Location is "+(remoteFileHandler.deleteRemoteFile(IMPORTFILEPATH+intermediateImportFileName) ? "Successful":"UnSuccessful"));
		new File(TEMPDIR+EXPORTFILENAME).delete();
		new File(TEMPDIR+intermediateImportFileName).delete();

	}

}
