/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images3d;

import java.util.ArrayList;
import java.util.List;


/**
 * A scene contains objects and effects which are displayed
 */
public class Scene3D {

	private List<Object3D> objects;


	public Scene3D() {
		this.objects = new ArrayList<>();
	}

	public void add(Object3D object) {
		objects.add(object);
	}

	public List<Object3D> getObjects() {
		return objects;
	}
}
