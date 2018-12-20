/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;


/**
 * Allows converting a Collection with its Iterator into an Enumeration in case we are using
 * Collections internally (as they are fun!) but legacy APIs require being fed Enumerations...
 *
 * @author Moya (a softer space), 2018
 */
public class EnumeratedCollection implements Enumeration<Object> {

	private Iterator<? extends Object> baseIterator;


	public EnumeratedCollection(Collection<? extends Object> baseCollection) {
		this.baseIterator = baseCollection.iterator();
	}
	
	public boolean hasMoreElements() {
		return baseIterator.hasNext();
	}
	
	public Object nextElement() {
		return baseIterator.next();
	}
	
}
