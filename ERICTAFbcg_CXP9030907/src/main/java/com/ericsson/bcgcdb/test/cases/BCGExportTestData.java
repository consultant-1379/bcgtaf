package com.ericsson.bcgcdb.test.cases;

import com.ericsson.cifwk.taf.annotations.Input;

public class BCGExportTestData {
	
	private String exportNodeType;
	private String exportNodeFdn;
	private String exportFileName;
	private String domain;
	private String verifyMo;
	private String Compression;
	private String Expected;
	private String exportMo;
	private String timeValue;
	/**
	 * @return the exportNodeName
	 */
	public String getExportNodeName() {
		return exportNodeName;
	}
	/**
	 * @param exportNodeName the exportNodeName to set
	 */
	public void setExportNodeName(String exportNodeName) {
		this.exportNodeName = exportNodeName;
	}

	private String exportNodeName;
	
	
	/**
	 * @return the timeValue
	 */
	public String getTimeValue() {
		return timeValue;
	}
	/**
	 * @param timeValue the timeValue to set
	 */
	public void setTimeValue(String timeValue) {
		this.timeValue = timeValue;
	}
	/**
	 * @return the dateValue
	 */
	public String getDateValue() {
		return dateValue;
	}
	/**
	 * @param dateValue the dateValue to set
	 */
	public void setDateValue(String dateValue) {
		this.dateValue = dateValue;
	}

	private String dateValue;
	
	/**
	 * @return the exportNodeType
	 */
	public String getExportNodeType() {
		return exportNodeType;
	}
	/**
	 * @param exportNodeType the exportNodeType to set
	 */
	public void setExportNodeType(String exportNodeType) {
		this.setExportMo(exportNodeType);
		this.exportNodeType = exportNodeType;
	}
	/**
	 * @return the exportNodeFdn
	 */
	public String getExportNodeFdn() {
		return exportNodeFdn;
	}
	/**
	 * @param exportNodeFdn the exportNodeFdn to set
	 */
	public void setExportNodeFdn(String exportNodeFdn) {
		this.exportNodeFdn = exportNodeFdn;
	}
	
	/**
	 * @return the exportFileName
	 */
	public String getExportFileName() {
		return exportFileName;
	}
	/**
	 * @param exportFileName the exportFileName to set
	 */
	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}
	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	/**
	 * @return the verifyMo
	 */
	public String getVerifyMo() {
		return verifyMo;
	}
	/**
	 * @param verifyMo the verifyMo to set
	 */
	public void setVerifyMo(String verifyMo) {
		this.verifyMo = verifyMo;
	}
	
	/**
	 * @return the compression
	 */
	public String getCompression() {
		return Compression;
	}
	/**
	 * @param compression the compression to set
	 */
	public void setCompression(String compression) {
		Compression = compression;
	}
	/**
	 * @return the expected
	 */
	public String getExpected() {
		return Expected;
	}
	/**
	 * @param expected the expected to set
	 */
	public void setExpected(String expected) {
		Expected = expected;
	}

	/**
	 * @param exportMo the exportMo to set
	 */
	public void setExportMo(String exportMo){
		if(exportMo.equalsIgnoreCase("RNC"))
			this.exportMo = "UtranCell";
		else if(exportMo.equalsIgnoreCase("RBS"))
			this.exportMo = "RbsLocalCell";
		else if(exportMo.equalsIgnoreCase("ERBS"))
			this.exportMo = "EUtranCellFDD";
		else if(exportMo.equalsIgnoreCase("MGW"))
			this.exportMo = "InteractiveMessaging";
		else if(exportMo.equalsIgnoreCase("SGSN"))
			this.exportMo = "SgsnFunction";
		else
			this.exportMo = "SubNetwork";
	}
	
	/**
	 * @return the exportMo
	 */
	public String getExportMo() {
		return exportMo;
	}
}
