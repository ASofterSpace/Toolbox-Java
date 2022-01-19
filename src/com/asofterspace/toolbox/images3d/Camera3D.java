/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images3d;

import com.asofterspace.toolbox.images.ColorRGBA;
import com.asofterspace.toolbox.images.Image;


/**
 * A camera looking at the scene
 */
public class Camera3D {

	private Point3D cameraPos;

	private Vector3D cameraTarget;


	public Camera3D(Point3D cameraPos, Vector3D cameraTarget) {
		this.cameraPos = cameraPos;
		this.cameraTarget = cameraTarget;
	}

	public Point3D getCameraPos() {
		return cameraPos;
	}

	public void setCameraPos(Point3D cameraPos) {
		this.cameraPos = cameraPos;
	}

	public Vector3D getCameraTarget() {
		return cameraTarget;
	}

	public void setCameraTarget(Vector3D cameraTarget) {
		this.cameraTarget = cameraTarget;
	}

	public Image takePictureOfScene(Scene3D scene) {

		int width = 200;
		int height = 200;
		int offsetX = width / 2;
		int offsetY = height / 2;

		// TODO - for now, we always take a picture of 200x200 px, with 1 unit being 100 px
		Image result = new Image(width, height);

		// TODO - for now, we just ignore the cameraTarget and assume that it is (0, 0, 1) - change that!

		ColorRGBA pointColor = new ColorRGBA(0, 0, 0);

		for (Object3D obj : scene.getObjects()) {
			for (Point3D point : obj.getPoints()) {
				if (point.getZ() > cameraPos.getZ()) {
					int x = (int) (point.getX() * 100) + offsetX;
					int y = (int) (point.getY() * 100) + offsetY;
					if ((x < 0) || (x >= width) || (y < 0) || (y >= height)) {
						continue;
					}
					result.setPixel(x, y, pointColor);
				}
			}
		}

		return result;
	}

}
