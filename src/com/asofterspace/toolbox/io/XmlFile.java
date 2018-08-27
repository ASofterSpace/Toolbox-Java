package com.asofterspace.toolbox.io;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * An xml file object describes a single xml file and enables simple access to
 * its contents.
 */
public class XmlFile extends File {

	// ideally, this class should have no own members (they should all be in File,
	// and here we should just have additional methods for working on it), or at
	// least the own members should be fine as null that are later lazy-loaded
	// if this ever changes, classes that extend this one (such as CdmFile in
	// the CdmScriptEditor) need to get their constructors adapted!
	
	Document xmlcontents = null;

	/**
	 * You can construct an XmlFile instance by basing it on an existing file object.
	 */
	public XmlFile(File regularFile) {
	
		super();
		
		regularFile.copyTo(this);
	}

	private void loadXmlContents() {
	
		try {
			
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			
			xmlcontents = documentBuilder.parse(this.getJavaFile());
			
			xmlcontents.getDocumentElement().normalize();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public Node getRoot() {

		if (xmlcontents == null) {
			loadXmlContents();
		}
		
		return xmlcontents.getDocumentElement();
	}
   
	/**
	 * Gives back a string representation of the xml file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.XmlFile: " + filename + " (root element: " + this.getRoot().getNodeName() + ")";
	}
	
}
