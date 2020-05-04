/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images3d;

import java.util.ArrayList;
import java.util.List;


public class Object3DFactory {

	public static Object3D createRandomObject(int amountOfPoints, Cuboid3D containedIn) {

		List<Point3D> points = new ArrayList<>();

		for (int i = 0; i < amountOfPoints; i++) {
			points.add(createRandomPoint(containedIn));
		}

		return new Object3D(points);
	}

	public static Point3D createRandomPoint(Cuboid3D containedIn) {

		double x = containedIn.getLeft() + Math.random() * (containedIn.getRight() - containedIn.getLeft());
		double y = containedIn.getBottom() + Math.random() * (containedIn.getTop() - containedIn.getBottom());
		double z = containedIn.getBack() + Math.random() * (containedIn.getFront() - containedIn.getBack());

		return new Point3D(x, y, z);
	}
}
