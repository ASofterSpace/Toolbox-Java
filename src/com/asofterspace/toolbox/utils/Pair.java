/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.Map;


/**
 * Just a pair of values
 *
 * @author Moya (a softer space), 2018
 */
public class Pair<L, R> implements Map.Entry<L, R> {

	private L left;
	
	private R right;
	
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	public L getLeft() {
		return left;
	}
	
	public R getRight() {
		return right;
	}
	
	@Override
	public L getKey() {
		return left;
	}
	
	@Override
	public R getValue() {
		return right;
	}
	
	public L setKey(L key) {
		L old = left;
		left = key;
		return old;
	}
	
	@Override
	public R setValue(R value) {
		R old = right;
		right = value;
		return old;
	}
}
