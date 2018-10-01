package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.TinyMap;
import com.asofterspace.toolbox.Utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;


public class XmlElement {

	private String name;
	
	private String innerText;
	
	private TinyMap attributes;
	
	private XmlElement xmlParent;
	
	private List<XmlElement> xmlChildren;

	
	public XmlElement(String name, Attributes attributes) {
	
		// internalize the node names
		this.name = name.intern();
		
		this.innerText = null;
		
		this.attributes = new TinyMap(attributes.getLength());
		
		for (int i = 0; i < attributes.getLength(); i++) {
		
			// internalize all keys
			String key = attributes.getQName(i).intern();
			
			// only internalize a FEW values, based on their keys
			String val = attributes.getValue(i);
			
			// we can use == comparison as the key has already been internalized
			if (key == "xsi:type") {
				val = val.intern();
			}
			
			this.attributes.putFast(key, val);
		}
		
		this.xmlParent = null;
		
		this.xmlChildren = new ArrayList<>();
	}
	
	protected XmlElement() {
	}
	
	public void copyTo(XmlElement other) {
		other.name = name;
		other.innerText = innerText;
		other.attributes = attributes;
		other.xmlParent = xmlParent;
		other.xmlChildren = xmlChildren;
	}
	
	public String getNodeName() {
		return name;
	}
	
	public void setNodeName(String name) {
		this.name = name;
	}
	
	public List<XmlElement> getChildNodes() {
		return xmlChildren;
	}
	
	public void removeChild(XmlElement xmlEl) {
		xmlChildren.remove(xmlEl);
	}
	
	public void addChild(XmlElement newChild) {
		this.xmlChildren.add(newChild);
		newChild.xmlParent = this;
	}
	
	public XmlElement createChild(String tagName) {
	
		XmlElement newChild = new XmlElement();
		
		newChild.name = tagName;
		
		newChild.innerText = null;
		
		newChild.attributes = new TinyMap();
		
		newChild.xmlParent = this;
		
		newChild.xmlChildren = new ArrayList<>();
		
		xmlChildren.add(newChild);
		
		return newChild;
	}
	
	public XmlElement getXmlParent() {
		return xmlParent;
	}
	
	public List<XmlElement> getElementsByTagName(String tagName) {
		List<XmlElement> result = new ArrayList<>();
		getElementsByTagName(tagName, result);
		return result;
	}
	
	private void getElementsByTagName(String tagName, List<XmlElement> outResult) {
		if (name.equals(tagName)) {
			outResult.add(this);
		}
		
		for (XmlElement child : xmlChildren) {
			child.getElementsByTagName(tagName, outResult);
		}
	}

	public String getAttribute(String key) {
		if (attributes == null) {
			return null;
		}
		return attributes.get(key);
	}
	
	public TinyMap getAttributes() {
		return attributes;
	}
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	public void removeAttribute(String key) {
		attributes.remove(key);
	}
	
	public void setInnerText(String innerText) {
		this.innerText = innerText;
	}
	
	public void writeToFile(OutputStreamWriter writer) throws IOException {
		writer.write("<" + name + attributes.toXmlAttributesStr());
		if (xmlChildren.size() < 1) {
			if ((innerText == null) || (innerText.length() < 1)) {
				writer.write("/>\n");
			} else {
				writer.write(">" + Utils.xmlEscape(innerText) + "</" + name + ">\n");
			}
		} else {
			writer.write(">\n");
			for (XmlElement child : xmlChildren) {
				child.writeToFile(writer);
			}
			writer.write("</" + name + ">\n");
		}
	}

}