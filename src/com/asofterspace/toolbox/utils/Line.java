/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;


/**
 * A line from one point to another
 */
public class Line<T> {

	private Point<T, T> from;
	private Point<T, T> to;


	public Line(Point<T, T> from, Point<T, T> to) {
		this.from = from;
		this.to = to;
	}

	public Point<T, T> getFrom() {
		return from;
	}

	public void setFrom(Point<T, T> from) {
		this.from = from;
	}

	public Point<T, T> getTo() {
		return to;
	}

	public void setTo(Point<T, T> to) {
		this.to = to;
	}
}
