package com.ericsson.bcgcdb.test.cases;

public class BCG_ATT_TR_TestData {
	
	private String moName;
	private String nodeName;
	private String nodeType;
	private String nodeFdn;
	private String fileName;
	private String domain;
	private String verifyTestCase;
	private String mimRate;
	private String compression;
	private String exportMo;
	private String timeValue;
	private String dateValue;
	private String numberOfMOs;
	private String planName;
	private String pedAutoLockUnlock;
	private String adminState;
	private String modifier;
	private String updateField;
	private String Scheme;
	private String reconfiguration;
	private int countdown;
	private String expected;
	private String importTime;


	/**
	 * @param nodeType the exportNodeType to set
	 */
	public void setNodeType(String nodeType) {
		this.setExportMo(nodeType);
		this.nodeType = nodeType;
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
	 * @return the moName
	 */
	public String getMoName() {
		return moName;
	}

	/**
	 * @param moName the moName to set
	 */
	public void setMoName(String moName) {
		this.moName = moName;
	}

	/**
	 * @return the nodeName
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * @param nodeName the nodeName to set
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * @return the nodeFdn
	 */
	public String getNodeFdn() {
		return nodeFdn;
	}

	/**
	 * @param nodeFdn the nodeFdn to set
	 */
	public void setNodeFdn(String nodeFdn) {
		this.nodeFdn = nodeFdn;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
	public String getVerifyTestCase() {
		return verifyTestCase;
	}

	/**
	 * @param verifyTestCase the verifyMo to set
	 */
	public void setVerifyTestCase(String verifyTestCase) {
		this.verifyTestCase = verifyTestCase;
	}

	/**
	 * @return the compression
	 */
	public String getCompression() {
		return compression;
	}

	/**
	 * @param compression the compression to set
	 */
	public void setCompression(String compression) {
		this.compression = compression;
	}

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

	/**
	 * @return the numberOfMOs
	 */
	public String getNumberOfMOs() {
		return numberOfMOs;
	}

	/**
	 * @param numberOfMOs the numberOfMOs to set
	 */
	public void setNumberOfMOs(String numberOfMOs) {
		this.numberOfMOs = numberOfMOs;
	}

	/**
	 * @return the planName
	 */
	public String getPlanName() {
		return planName;
	}

	/**
	 * @param planName the planName to set
	 */
	public void setPlanName(String planName) {
		this.planName = planName;
	}

	/**
	 * @return the pedAutoLockUnlock
	 */
	public String getPedAutoLockUnlock() {
		return pedAutoLockUnlock;
	}

	/**
	 * @param pedAutoLockUnlock the pedAutoLockUnlock to set
	 */
	public void setPedAutoLockUnlock(String pedAutoLockUnlock) {
		this.pedAutoLockUnlock = pedAutoLockUnlock;
	}

	/**
	 * @return the adminState
	 */
	public String getAdminState() {
		return adminState;
	}

	/**
	 * @param adminState the adminState to set
	 */
	public void setAdminState(String adminState) {
		this.adminState = adminState;
	}

	/**
	 * @return the modifier
	 */
	public String getModifier() {
		return modifier;
	}

	/**
	 * @param modifier the modifier to set
	 */
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	/**
	 * @return the updateField
	 */
	public String getUpdateField() {
		return updateField;
	}

	/**
	 * @param updateField the updateField to set
	 */
	public void setUpdateField(String updateField) {
		this.updateField = updateField;
	}

	public String getScheme() {
		return Scheme;
	}

	public void setScheme(String scheme) {
		Scheme = scheme;
	}

	public String getReconfiguration() {
		return reconfiguration;
	}

	public void setReconfiguration(String reconfiguration) {
		this.reconfiguration = reconfiguration;
	}

	public int getCountdown() {
		return countdown;
	}

	public void setCountdown(int countdown) {
		this.countdown = countdown;
	}

	/**
	 * @return the expected
	 */
	public String getExpected() {
		return expected;
	}

	/**
	 * @param expected the expected to set
	 */
	public void setExpected(String expected) {
		this.expected = expected;
	}

	/**
	 * @return the nodeType
	 */
	public String getNodeType() {
		return nodeType;
	}

	/**
	 * @return the exportMo
	 */
	public String getExportMo() {
		return exportMo;
	}

	/**
	 * @return the mimRate
	 */
	public String getMimRate() {
		return mimRate;
	}

	/**
	 * @param mimRate the mimRate to set
	 */
	public void setMimRate(String mimRate) {
		this.mimRate = mimRate;
	}

	/**
	 * @return the importTime
	 */
	public String getImportTime() {
		return importTime;
	}

	/**
	 * @param importTime the importTime to set
	 */
	public void setImportTime(String importTime) {
		this.importTime = importTime;
	}	
}
