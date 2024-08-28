/*
 * The purpose of this class is to retain the information of the XML tags .
 *
 * */

package com.ericsson.bcgcdb.test.getters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.stream.XMLStreamReader;


public class TagInfo {

	private String localName = "" ;
	private String tagPrefix = "";
	private String namespaceURI = "" ;
	private XMLStreamReader reader = null ;
	private String characterContent = ""  ;
	private String tagType = "" ;
	private HashMap<String,String> attributeMap = new  HashMap<String,String>()  ;
	private Queue<String> nameSpaceQueue = new  LinkedList<String>()  ;

	public TagInfo(XMLStreamReader reader,String tagType) {
		this.localName = reader.getLocalName();
		this.tagPrefix = reader.getPrefix();
		this.namespaceURI = reader.getNamespaceURI();
		this.reader = reader ;
		this.tagType = tagType;
	}

	public TagInfo(XMLStreamReader reader) {
		this.characterContent = reader.getText();
	}

	public String getLocalName() {
		return localName;
	}

	public String getTagPrefix() {
		return tagPrefix.trim();
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public String getCharacterContent() {
		return characterContent;
	}

	public void populateAttributeMapFromXML() {
		int count = reader.getAttributeCount();
		while (count>=0){
			if(reader.getAttributeLocalName(count)!=null)
			{
				attributeMap.put(reader.getAttributeLocalName(count), reader.getAttributeValue(count));
			}
			count--;
		}
	}

	public void populateNameSpaceQueueFromXML() {
		int count = 0 ;
		while (count<=reader.getNamespaceCount()){
			if(reader.getNamespaceURI(count)!=null)
			{
				nameSpaceQueue.add(reader.getNamespaceURI(count));
			}
			count++;
		}
	}

	public Queue<String> getNameSpaceQueue() {
		return nameSpaceQueue;
	}

	public HashMap<String, String> getAttributeMap() {
		return attributeMap;
	}

	public String getTagType() {
		return tagType;
	}

}

