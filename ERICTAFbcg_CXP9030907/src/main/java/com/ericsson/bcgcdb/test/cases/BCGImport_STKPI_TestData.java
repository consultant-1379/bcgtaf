package com.ericsson.bcgcdb.test.cases;

public class BCGImport_STKPI_TestData {
	String ImportMo;
	/**
	 * @return the importMo
	 */
	public String getImportMo() {
		return ImportMo;
	}
	/**
	 * @param importMo the importMo to set
	 */
	public void setImportMo(String importMo) {
		ImportMo = importMo;
	}
	/**
	 * @return the importType
	 */
	public String getImportType() {
		return ImportType;
	}
	/**
	 * @param importType the importType to set
	 */
	public void setImportType(String importType) {
		ImportType = importType;
	}
	/**
	 * @return the importFileName
	 */
	public String getImportFileName() {
		return ImportFileName;
	}
	/**
	 * @param importFileName the importFileName to set
	 */
	public void setImportFileName(String importFileName) {
		ImportFileName = importFileName;
	}
	/**
	 * @return the planName
	 */
	public String getPlanName() {
		return PlanName;
	}
	/**
	 * @param planName the planName to set
	 */
	public void setPlanName(String planName) {
		PlanName = planName;
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
	 * @return the mimRate
	 */
	public String getMimRate() {
		return MimRate;
	}
	/**
	 * @param mimRate the mimRate to set
	 */
	public void setMimRate(String mimRate) {
		MimRate = mimRate;
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
	 * @param type type of import
	 */
	public int getMos() {
		return Mos;
	}
	public void setMos(int mos) {
		Mos = mos;
	}


	String ImportType;
	String ImportFileName;
	String PlanName;
	String modifier;
	String MimRate;
	int Mos;
	String expected;
}
