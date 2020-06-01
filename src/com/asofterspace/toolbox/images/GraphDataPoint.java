/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;


/**
 * One datapoint intended for being shown in the GraphPanel
 */
public class GraphDataPoint {

	// x
	private double position;

	// y
	private double value;

	private String annotation;


	public GraphDataPoint(double position, double value) {
		this.position = position;
		this.value = value;
		this.annotation = null;
	}

	public GraphDataPoint(double position, double value, String annotation) {
		this.position = position;
		this.value = value;
		this.annotation = annotation;
	}

	public double getPosition() {
		return position;
	}

	public void setPosition(double position) {
		this.position = position;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

}
