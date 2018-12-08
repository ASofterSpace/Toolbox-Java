/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * A map optimized for having lots of these around, with only a small amount of entries each :)
 *
 * @author Moya (a softer space), 2018
 */
public class SmallMap<K, V> implements Map<K, V> {

	private List<K> keys;
	private List<V> values;
	
	public SmallMap() {
		this.keys = new ArrayList<>();
		this.values = new ArrayList<>();
	}
	
	// ideally, use this constructor to be faster if you already know the size that will be needed
	public SmallMap(int size) {
		this.keys = new ArrayList<>(size);
		this.values = new ArrayList<>(size);
	}
	
	@Override
	public void clear() {
		keys = new ArrayList<>();
		values = new ArrayList<>();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return keys.contains(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return values.contains(value);
	}

	@Override
	public Set<Map.Entry<K,V>> entrySet() {
		Set<Map.Entry<K,V>> result = new HashSet<>();
		for (int i = 0; i < keys.size(); i++) {
			result.add(new Pair<K,V>(keys.get(i), values.get(i)));
		}
		return result;
	}
	
	@Override
	public V get(Object key) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				return values.get(i);
			}
		}
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		return keys.size() < 1;
	}
	
	@Override
	public Set<K> keySet() {
		return new HashSet<>(keys);
	}
	
	// only call this if you know that the key does not exist in the map already!
	public void putFast(K key, V value) {
		keys.add(key);
		values.add(value);
	}
	
	@Override
	public V put(K key, V value) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				V prevval = values.get(i);
				values.set(i, value);
				return prevval;
			}
		}
		
		keys.add(key);
		values.add(value);
		return null;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public V remove(Object key) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				keys.remove(i);
				V prevval = values.get(i);
				values.remove(i);
				return prevval;
			}
		}
		return null;
	}
	
	@Override
	public int size() {
		return keys.size();
	}
	
	@Override
	public Collection<V> values() {
		return values;
	}
	
}
