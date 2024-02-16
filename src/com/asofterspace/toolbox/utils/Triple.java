/**
 * Unlicensed code created by A Softer Space, 2024
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.Map;


/**
 * Just a triple of values
 */
public class Triple<L, M, R> implements Map.Entry<L, R> {

	private L left;

	private M middle;

	private R right;

	public Triple(L left, M middle, R right) {
		this.left = left;
		this.middle = middle;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public M getMiddle() {
		return middle;
	}

	public void setMiddle(M middle) {
		this.middle = middle;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
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
