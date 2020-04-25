/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.util.Date;


/**
 * One datapoint intended for being shown in the GraphPanel,
 * but using a date time as key
 */
public class GraphTimeDataPoint {

	// x
	private Date dateTime;

	// y
	private double value;


	public GraphTimeDataPoint(Date dateTime, double value) {
		this.dateTime = dateTime;
		this.value = value;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
