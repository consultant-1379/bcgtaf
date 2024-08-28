package com.ericsson.bcgcdb.test.cases;

public class BCGExport_STKPI_TestData {

	
	public boolean getExpected() {
		return expected;
	}
	public void setExpected(boolean Expected) {
		this.expected = Expected;
	}
	public String getExportNodeFdn() {
		return exportNodeFdn;
	}
	public void setExportNodeFdn(String ExportNodeFdn) {
		this.exportNodeFdn = ExportNodeFdn;
	}
	public String getNodeType(){
		return nodeType;
	}
	public void setNodeType(String NodeType){
		this.nodeType = NodeType;
	}
	public String getValue(){
		return value;
	}
	public void setValue(String Value){
		this.value = Value;
	}
	public String getFilename(){
		return filename;
	}
	public void setFilename(String Filename){
		this.filename = Filename;
	}
	public String getDomain(){
		return domain;
	}
	public void setDomain(String Domain){
		this.domain = Domain;
	}
	public double getMimrate(){
		return mimRate;
	}
	public void setMimrate(double Mimrate){
		this.mimRate = Mimrate;
	}
	public String getCompression(){
		return compression;
	}
	public void setCompression(String Compression)
	{
		this.compression = Compression;
	}
	
	public int getItenation(){
		return iteration;
	}
	public void setItenation(int iteration)
	{
		this.iteration = iteration;
	}
	
	private boolean expected;
	private String exportNodeFdn;
	private String nodeType;
	private String value;
	private String filename;
	private String domain;
	private double mimRate;
	private String compression;
	private int iteration;

}
