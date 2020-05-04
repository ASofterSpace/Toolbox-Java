/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images3d;


/**
 * The common base for vectors and points.
 */
public class Vector3DBase {

	private double x;
	private double y;
	private double z;


	public Vector3DBase(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3DBase(Vector3DBase other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

}
