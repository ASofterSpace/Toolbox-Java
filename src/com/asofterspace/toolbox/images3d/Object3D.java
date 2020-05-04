/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images3d;

import java.util.ArrayList;
import java.util.List;


public class Object3D {

	private List<Point3D> points;


	public Object3D() {
		this.points = new ArrayList<>();
	}

	public Object3D(List<Point3D> points) {
		this.points = points;
	}

	public void addPoint(Point3D point) {
		points.add(point);
	}

	public List<Point3D> getPoints() {
		return points;
	}

	public void setPoints(List<Point3D> points) {
		this.points = points;
	}

}
