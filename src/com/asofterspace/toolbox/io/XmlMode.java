package com.asofterspace.toolbox.io;

/**
 * An XML mode describes what kind of file the XML file object has actually ended up loading 
 */
public enum XmlMode {

	// in the end, nothing was loaded (yet, or an exception occurred)
	NONE_LOADED,

	// an XML file has been loaded
	XML_LOADED,

	// an EMF binary file has been loaded
	EMF_LOADED
}
