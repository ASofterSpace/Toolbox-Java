/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.TinyMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A map optimized for having lots of these around, with only a small amount of entries each
 * This one is even more optimized than the SmallMap by using the fact that both keys and values
 * are actually strings :)
 *
 * @author Moya (a softer space), 2019
 */
public class TinyXmlMap extends TinyMap {

	public TinyXmlMap() {
		length = 0;
		heapsize = 0;
		heap = new String[heapsize];
	}

	// ideally, use this constructor to be faster if you already know the size that will be needed
	public TinyXmlMap(int size) {
		length = 0;
		heapsize = length * 2;
		heap = new String[heapsize];
	}

	// returns a string like foo="bar" key="val" ...
	public String toXmlAttributesStr() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			result.append(" ");
			result.append(heap[i*2]);
			result.append("=\"");
			result.append(XML.escapeXMLstr(heap[(i*2)+1]));
			result.append("\"");
		}
		return result.toString();
	}

}
