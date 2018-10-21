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

	private String name = null;
	
	private String innerText;
	
	private TinyMap attributes;
	
	private XmlElement xmlParent;
	
	private List<XmlElement> xmlChildren;

	
	public XmlElement(String name, Attributes attributes) {

		TinyMap attrMap = new TinyMap(attributes.getLength());
		
		for (int i = 0; i < attributes.getLength(); i++) {
		
			// internalize all keys
			String key = attributes.getQName(i).intern();
			
			// only internalize a FEW values, based on their keys
			String val = attributes.getValue(i);
			
			// we can use == comparison as the key has already been internalized
			if (key == "xsi:type") {
				val = val.intern();
			}
			
			attrMap.putFast(key, val);
		}

		construct(name, attrMap);
	}

	public XmlElement(String name, TinyMap attributes) {

		construct(name, attributes);
	}

	private void construct(String name, TinyMap attributes) {
	
		// internalize the node names
		if (name != null) {
			this.name = name.intern();
		}
		
		this.innerText = null;
		
		this.attributes = attributes;

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
	
	/**
	 * Get one child with the given tag name, if any such child exists
	 */
	public XmlElement getChild(String tagName) {
		for (XmlElement child : xmlChildren) {
			if (tagName.equals(child.name)) {
				return child;
			}
		}
		return null;
	}
	
	/**
	 * Recursively get elements by any of several tag names
	 */
	public List<XmlElement> getElementsByTagNames(String[] tagNames) {
		List<XmlElement> result = new ArrayList<>();
		getElementsByTagNames(tagNames, result);
		return result;
	}
	
	private void getElementsByTagNames(String[] tagNames, List<XmlElement> outResult) {
		for (String tagName : tagNames) {
			if (name.equals(tagName)) {
				outResult.add(this);
				break;
			}
		}
		
		for (XmlElement child : xmlChildren) {
			child.getElementsByTagNames(tagNames, outResult);
		}
	}

	/**
	 * Recursively get elements by one tag name
	 */
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

	/**
	 * Get elements by an exact path of tag names (e.g. call with ["foo"] to
	 * get all elements with tag name foo inside this element, but not its children;
	 * call with ["foo", "bar"] to get all elements with tag name bar inside elements
	 * with tag name foo inside this element, but again on exactly those levels - not
	 * with further intermediate elements in between)
	 */
	public List<XmlElement> getElementsByTagNameHierarchy(String... tagNameHierarchy) {
		
		if (tagNameHierarchy == null) {
			return null;
		}
		
		if (tagNameHierarchy.length < 1) {
			return null;
		}
		
		List<XmlElement> result = new ArrayList<>();
		
		getElementsByTagNameHierarchy(tagNameHierarchy, result, 0);
		
		return result;
	}
	
	private void getElementsByTagNameHierarchy(String[] tagNameHierarchy, List<XmlElement> outResult, int hierarchyLevel) {
		
		// check that this element has the correct name in the hierarchy
		if (!name.equals(tagNameHierarchy[hierarchyLevel])) {
			return;
		}
		
		// check if we need to go any deeper
		if (hierarchyLevel < tagNameHierarchy.length - 1) {
		
			// increase the hierarchy level when looking through children
			hierarchyLevel++;
		
			for (XmlElement child : xmlChildren) {
				child.getElementsByTagNameHierarchy(tagNameHierarchy, outResult, hierarchyLevel);
			}
			
		} else {
			
			// we do not need to go deeper - we are found!
			outResult.add(this);
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
	
	public String getInnerText() {
		return innerText;
	}
	
	public void setInnerText(String innerText) {
		this.innerText = innerText;
	}

	/**
	 * Assuming that we have <element attrOrChildName="_bla"/> or
	 * <element><attrOrChildName href="_bla"/></element>, this function
	 * returns _bla (or null if it finds neither)
	 */
	public String getLinkFromAttrOrChild(String attrOrChildName) {

		String elAttr = getAttribute(attrOrChildName);

		if (elAttr != null) {
			return elAttr;
		}

		// if we did not find an attrOrChildName as attribute, maybe we can find one as child?
		List<XmlElement> children = getChildNodes();
		for (XmlElement child : children) {
			if (attrOrChildName.equals(child.getNodeName())) {
				String href = child.getAttribute("href");

				if (href != null) {
					return href;
				}
			}
		}

		return null;
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
