/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images3d;


/**
 * Like a cube, but smooshed and elongated ^^
 */
public class Cuboid3D {

	private double left;
	private double right;
	private double top;
	private double bottom;
	private double front;
	private double back;


	// left, top and front are negative
	// right, bottom and back are positive
	public Cuboid3D(double left, double right, double top, double bottom, double front, double back) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.front = front;
		this.back = back;
	}

	public double getLeft() {
		return left;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getRight() {
		return right;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public double getTop() {
		return top;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getBottom() {
		return bottom;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public double getFront() {
		return front;
	}

	public void setFront(double front) {
		this.front = front;
	}

	public double getBack() {
		return back;
	}

	public void setBack(double back) {
		this.back = back;
	}

}
