package com.asofterspace.toolbox.io;


public class XlsxSheet {

	private XmlFile xml;
	
	private String title;
	
	private String content;
	

	public XlsxSheet(String title, XmlFile sheetFile) {

		this.title = title;
		
		this.xml = sheetFile;
	}
	
	public String getCellContent(String name) {
		return content;
	}
	
	public String getTitle() {
		return title;
	}

}
