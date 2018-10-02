package com.asofterspace.toolbox.utils;

import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * A map optimized for having lots of these around, with only a small amount of entries each
 * This one is even more optimized than the SmallMap by using the fact that both keys and values
 * are actually strings :)
 *
 * @author Moya (a softer space), 2018
 */
public class TinyMap implements Map<String, String> {

	// the data and its internal size
	private String[] heap;
	private int heapsize;
	
	// the actual amount of data
	private int length;
	
	
	public TinyMap() {
		length = 0;
		heapsize = 0;
		heap = new String[heapsize];
	}
	
	// ideally, use this constructor to be faster if you already know the size that will be needed
	public TinyMap(int size) {
		length = 0;
		heapsize = length * 2;
		heap = new String[heapsize];
	}
	
	@Override
	public void clear() {
		length = 0;
	}
	
	@Override
	public boolean containsKey(Object key) {
		for (int i = 0; i < length; i++) {
			if (heap[i*2].equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean containsValue(Object value) {
		for (int i = 0; i < length; i++) {
			if (heap[(i*2)+1].equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Map.Entry<String, String>> entrySet() {
		Set<Map.Entry<String, String>> result = new HashSet<>();
		for (int i = 0; i < length; i++) {
			result.add(new Pair<String, String>(heap[i*2], heap[(i*2)+1]));
		}
		return result;
	}
	
	@Override
	public String get(Object key) {
		for (int i = 0; i < length; i++) {
			if (heap[i*2].equals(key)) {
				return heap[(i*2)+1];
			}
		}
		return null;
	}
	
	public String get(int index) {
		if (index < length) {
			return heap[(index*2)+1];
		}
		return null;
	}
	
	public String getKey(int index) {
		if (index < length) {
			return heap[index*2];
		}
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		return length < 1;
	}
	
	@Override
	public Set<String> keySet() {
		Set<String> result = new HashSet<>();
		for (int i = 0; i < length; i++) {
			result.add(heap[i*2]);
		}
		return result;
	}
	
	// only call this if you know that the key does not exist in the map already!
	public void putFast(String key, String value) {
		if (key == null) {
			return;
		}
		if (heapsize <= length*2) {
			heapsize++;
			heapsize *= 2;
			String[] newheap = new String[heapsize];
			System.arraycopy(heap, 0, newheap, 0, length*2);
			heap = newheap;
		}
		heap[length*2] = key;
		heap[(length*2)+1] = value;
		length++;
	}
	
	@Override
	public String put(String key, String value) {
		for (int i = 0; i < length; i++) {
			if (heap[i*2].equals(key)) {
				String prevval = heap[(i*2)+1];
				heap[(i*2)+1] = value;
				return prevval;
			}
		}
		
		putFast(key, value);
		
		return null;
	}
	
	@Override
	public void putAll(Map<? extends String, ? extends String> map) {
		for (Map.Entry<? extends String, ? extends String> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public String remove(Object key) {
		for (int i = 0; i < length; i++) {
			if (heap[i*2].equals(key)) {
				String prevval = heap[(i*2)+1];
				// TODO :: check if this is correct
				System.arraycopy(heap, (i*2)+2, heap, i*2, (length-i-1)*2);
				length -= 1;
				return prevval;
			}
		}
		return null;
	}
	
	@Override
	public int size() {
		return length;
	}
	
	@Override
	public Collection<String> values() {
		List<String> result = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			result.add(heap[(i*2)+1]);
		}
		return result;
	}
	
	// returns a string like foo="bar" key="val" ...
	public String toXmlAttributesStr() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			result.append(" ");
			result.append(heap[i*2]);
			result.append("=\"");
			result.append(Utils.xmlEscape(heap[(i*2)+1]));
			result.append("\"");
		}
		return result.toString();
	}
	
}
