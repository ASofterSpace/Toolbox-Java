/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images3d;


/**
 * A vector contains (x,y,z) coordinates like a point, but for a vector
 * we are interested only in the difference between start and end point,
 * while a point in space actually denotes one certain location.
 * Therefore, we make the distinction such that the compiler helps us if
 * we use one where we intended to use the other one.
 */
public class Vector3D extends Vector3DBase {

	public Vector3D(double x, double y, double z) {
		super(x, y, z);
	}

	public Vector3D(Vector3DBase other) {
		super(other);
	}

}
