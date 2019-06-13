/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

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

	private TinyXmlMap attributes;

	private XmlElement xmlParent;

	protected List<XmlElement> xmlChildren;

	// an object that represents this XmlElement, but is (ideally the uppermost) extending class of it
	private XmlElement extendingObject;


	public XmlElement(String name) {

		construct(name, new TinyXmlMap());
	}

	public XmlElement(String name, Attributes attributes) {

		TinyXmlMap attrMap = new TinyXmlMap(attributes.getLength());

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

	public XmlElement(String name, TinyXmlMap attributes) {

		construct(name, attributes);
	}

	public XmlElement(Record other) {

		if (other instanceof XML) {
			XML otherXML = (XML) other;
			construct(otherXML.getName(), otherXML.getAttributes());
		} else {
			construct(null, new TinyXmlMap());
		}

		switch (other.getKind()) {

			case ARRAY:
				List<Record> values = other.getValues();
				for (Record val : values) {
					this.xmlChildren.add(new XmlElement(val));
				}
				break;

			case OBJECT:
				Map<String, Record> valueMap = other.getValueMap();
				for (Map.Entry<String, Record> entry : valueMap.entrySet()) {
					XmlElement xmlElement = new XmlElement(entry.getValue());
					xmlElement.name = entry.getKey();
					this.xmlChildren.add(xmlElement);
				}
				break;

			default:
				innerText = other.asString();
		}
	}

	private void construct(String name, TinyXmlMap attributes) {

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

	public void setExtendingObject(XmlElement extObj) {
		this.extendingObject = extObj;
	}

	public XmlElement getExtendingObject() {
		if (extendingObject == null) {
			return this;
		}
		return extendingObject.getExtendingObject();
	}

	public void copyTo(XmlElement other) {
		other.name = name;
		other.innerText = innerText;
		other.attributes = attributes;
		other.xmlParent = xmlParent;
		other.xmlChildren = xmlChildren;
	}

	public String getTagName() {
		return name;
	}

	public void setTagName(String name) {
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

	/**
	 * This takes all the children of the other parent
	 * and makes them children of this element instead
	 * - therefore effectively stealing them!
	 * (They will NOT any longer be children of the
	 * other parent!)
	 */
	public void addChildrenOf(XmlElement otherParent) {

		for (XmlElement child : otherParent.xmlChildren) {
			child.xmlParent = this;
		}

		this.xmlChildren.addAll(otherParent.xmlChildren);

		otherParent.xmlChildren.clear();
	}

	public void addChildrenOf(Record otherParentRecord) {
		addChildrenOf(new XmlElement(otherParentRecord));
	}

	/**
	 * Does the same as addChildrenOf, but if a child with the same
	 * tag name already exists, its content is updated instead of
	 * a second one with the same tag name being added
	 */
	public void addOrUpdateChildrenOf(XmlElement otherParent) {

		for (XmlElement child : otherParent.xmlChildren) {
			child.xmlParent = this;
		}

		for (XmlElement child : otherParent.xmlChildren) {

			boolean added = false;

			for (XmlElement ourChild : this.xmlChildren) {
				if (child.name.equals(ourChild.name)) {
					child.copyTo(ourChild);
					added = true;
					break;
				}
			}

			if (!added) {
				this.xmlChildren.add(child);
			}
		}

		otherParent.xmlChildren.clear();
	}

	public void addOrUpdateChildrenOf(Record otherParentRecord) {
		addOrUpdateChildrenOf(new XmlElement(otherParentRecord));
	}

	public XmlElement createChild(String tagName) {

		XmlElement newChild = new XmlElement();

		newChild.name = tagName;

		newChild.innerText = null;

		newChild.attributes = new TinyXmlMap();

		newChild.xmlParent = this;

		newChild.xmlChildren = new ArrayList<>();

		xmlChildren.add(newChild);

		return newChild;
	}

	public XmlElement getXmlParent() {
		return xmlParent;
	}

	public void setXmlParent(XmlElement newParent) {
		xmlParent = newParent;
	}

	/**
	 * Get one direct child with the given tag name, if any such child exists
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
	 * Get one direct child with the given tag name and attribute key / value pair,
	 * if any such child exists
	 */
	public XmlElement getChild(String tagName, String attrKey, String attrValue) {
		for (XmlElement child : xmlChildren) {
			if (tagName.equals(child.name)) {
				if (attrValue.equals(child.getAttribute(attrKey))) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * Get one direct child with the given tag name that itself has a child with
	 * the given tag name and inner text, if any such child exists
	 */
	public XmlElement getChildWithChild(String tagName, String childChildTagName, String childChildInnerText) {
		for (XmlElement child : xmlChildren) {
			if (tagName.equals(child.name)) {
				for (XmlElement childchild : child.xmlChildren) {
					if (childChildTagName.equals(childchild.name)) {
						if (childChildInnerText.equals(childchild.innerText)) {
							return child;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get all direct children with the given tag name, but without recursion
	 * (if you want recursion, check getElementsByTagNames())
	 */
	public List<XmlElement> getChildren(String tagName) {

		List<XmlElement> result = new ArrayList<>();

		for (XmlElement child : xmlChildren) {
			if (tagName.equals(child.name)) {
				result.add(child);
			}
		}

		return result;
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

	public TinyXmlMap getAttributes() {
		return attributes;
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	public String getInnerText() {

		if (innerText == null) {
			return "";
		}

		return innerText;
	}

	public void appendInnerText(String furtherInnerText) {

		if (this.innerText == null) {
			this.innerText = furtherInnerText;
		} else {
			this.innerText += furtherInnerText;
		}
	}

	public void setInnerText(String innerText) {
		this.innerText = innerText;
	}

	public void setInnerText(int innerText) {
		this.innerText = Integer.toString(innerText);
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
			if (attrOrChildName.equals(child.getTagName())) {
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
				writer.write(">" + XML.escapeXMLstr(innerText) + "</" + name + ">\n");
			}
		} else {
			writer.write(">\n");
			for (XmlElement child : xmlChildren) {
				child.writeToFile(writer);
			}
			writer.write("</" + name + ">\n");
		}
	}

	private void toString(StringBuilder result) {
		result.append("<" + name + attributes.toXmlAttributesStr());
		if (xmlChildren.size() < 1) {
			if ((innerText == null) || (innerText.length() < 1)) {
				result.append("/>\n");
			} else {
				result.append(">" + XML.escapeXMLstr(innerText) + "</" + name + ">\n");
			}
		} else {
			result.append(">\n");
			for (XmlElement child : xmlChildren) {
				child.toString(result);
			}
			result.append("</" + name + ">\n");
		}
	}

	@Override
	public String toString() {

		StringBuilder result = new StringBuilder();

		toString(result);

		return result.toString();
	}

	/**
	 * Removes this element, unless it is the root (the root cannot be removed, as it has no parent that it could be removed from...)
	 */
	public void remove() {
		if (xmlParent != null) {
			xmlParent.removeChild(this);
		}
	}

}
