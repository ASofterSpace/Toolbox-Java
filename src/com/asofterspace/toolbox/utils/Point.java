/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;


/**
 * Just an arbitrary point
 */
public class Point<X, Y> extends Pair<X, Y> {

	public Point(X x, Y y) {
		super(x, y);
	}

	public X getX() {
		return getLeft();
	}

	public void setX(X x) {
		setLeft(x);
	}

	public Y getY() {
		return getRight();
	}

	public void setY(Y y) {
		setRight(y);
	}
}
