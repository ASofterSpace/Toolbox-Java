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

	public void setLeft(L left) {
		this.left = left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
	}

	public L getX() {
		return left;
	}

	public void setX(L x) {
		this.left = x;
	}

	public R getY() {
		return right;
	}

	public void setY(R y) {
		this.right = y;
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

	@Override
	public String toString() {
		return "[" + this.left + ", " + this.right + "]";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object other) {

		// If the other one does not even exist, we are not the same - because we exist!
		if (other == null) {
			return false;
		}

		if (other instanceof Pair) {
			Pair otherPair = (Pair) other;

			// If our values for left are different...
			if (this.left == null) {
				if (otherPair.left != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.left.equals(otherPair.left)) {
				// ... then we are not the same!
				return false;
			}

			// If our values for right are different...
			if (this.right == null) {
				if (otherPair.right != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.right.equals(otherPair.right)) {
				// ... then we are not the same!
				return false;
			}

			// We have no reason to assume that we are not the same
			return true;
		}

		// If the other one cannot even be cast to us, then we are not the same!
		return false;
	}

	@Override
	public int hashCode() {
		int result = 0;
		if (this.left != null) {
			result += this.left.hashCode();
		}
		if (this.right != null) {
			result += this.right.hashCode();
		}
		return result;
	}

}
