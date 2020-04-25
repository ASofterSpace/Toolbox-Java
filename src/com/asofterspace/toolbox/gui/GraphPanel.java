/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import com.asofterspace.toolbox.utils.DateUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;


/**
 * This is a JPanel that, instead of just showing a boring background color,
 * actually shows a fancy graph! :D
 */
public class GraphPanel extends JPanel {

	public static final long serialVersionUID = 3458397457249723l;

	// pixels around the graph on all sides
	// (in that area, labels are allowed!)
	private int BORDER_WIDTH = 10;

	private List<GraphDataPoint> data;

	private Integer ourMinimumHeight = null;

	private Color dataColor;


	public GraphPanel() {
		super();
	}

	public void setDataPoints(List<GraphDataPoint> newData) {

		Collections.sort(newData, new Comparator<GraphDataPoint>() {
			public int compare(GraphDataPoint a, GraphDataPoint b) {
				return (int) (a.getPosition() - b.getPosition());
			}
		});

		this.data = newData;
	}

	public void setTimeDataPoints(List<GraphTimeDataPoint> timeData) {

		if (timeData.size() < 1) {
			this.data = new ArrayList<>();
			return;
		}

		Collections.sort(timeData, new Comparator<GraphTimeDataPoint>() {
			public int compare(GraphTimeDataPoint a, GraphTimeDataPoint b) {
				return a.getDateTime().compareTo(b.getDateTime());
			}
		});

		Date firstDate = timeData.get(0).getDateTime();
		Date lastDate = timeData.get(timeData.size() - 1).getDateTime();

		List<Date> days = DateUtils.listDaysFromTo(firstDate, lastDate);

		List<GraphDataPoint> actualData = new ArrayList<>();
		int x = 1;
		double y = 0;
		int i = 0;
		for (Date day : days) {
			while ((i < timeData.size()) && DateUtils.isSameDay(day, timeData.get(i).getDateTime())) {
				y += timeData.get(i).getValue();
				i++;
			}
			actualData.add(new GraphDataPoint(x, y, DateUtils.serializeDate(day)));
			x++;
		}

		// call setDataPoints such that the sorting will be called again
		// (the datapoints are already sorted, but meh, you never know... ^^)
		setDataPoints(actualData);
	}

	public void setMinimumHeight(int height) {
		this.ourMinimumHeight = height;
	}

	public double getMinimumValue() {

		if ((data == null) || (data.size() < 1)) {
			return 0;
		}

		double min = data.get(0).getValue();

		for (GraphDataPoint dataPoint : data) {
			if (dataPoint.getValue() < min) {
				min = dataPoint.getValue();
			}
		}

		return min;
	}

	public void shiftValues(double shiftBy) {

		if ((data == null) || (data.size() < 1)) {
			return;
		}

		for (GraphDataPoint dataPoint : data) {
			dataPoint.setValue(dataPoint.getValue() + shiftBy);
		}
	}

	public void setDataColor(Color newColor) {
		this.dataColor = newColor;
	}

	public Color getDataColor() {
		if (dataColor == null) {
			return getForeground();
		}
		return dataColor;
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();
		if (ourMinimumHeight == null) {
			return result;
		}
		result.height = ourMinimumHeight;
		return result;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (ourMinimumHeight == null) {
			return result;
		}
		result.height = ourMinimumHeight;
		return result;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		int outerWidth = getWidth();
		int outerHeight = getHeight();
		int innerWidth = outerWidth - 2 * BORDER_WIDTH;
		int innerHeight = outerHeight - 2 * BORDER_WIDTH;

		g.setColor(getForeground());
		// y axis
		g.drawLine(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH + innerHeight);
		g.drawLine(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH - 4, BORDER_WIDTH + 9);
		g.drawLine(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH + 4, BORDER_WIDTH + 9);
		// x axis
		g.drawLine(BORDER_WIDTH, BORDER_WIDTH + innerHeight, BORDER_WIDTH + innerWidth, BORDER_WIDTH + innerHeight);
		g.drawLine(BORDER_WIDTH + innerWidth, BORDER_WIDTH + innerHeight, BORDER_WIDTH + innerWidth - 9, BORDER_WIDTH + innerHeight - 4);
		g.drawLine(BORDER_WIDTH + innerWidth, BORDER_WIDTH + innerHeight, BORDER_WIDTH + innerWidth - 9, BORDER_WIDTH + innerHeight + 4);

		if (data == null) {
			return;
		}
		if (data.size() < 1) {
			return;
		}

		g.setColor(getDataColor());

		double xMin = data.get(0).getPosition();
		double xMax = xMin;
		double yMin = 0;
		double yMax = 0;

		for (GraphDataPoint dataPoint : data) {
			if (dataPoint.getPosition() < xMin) {
				xMin = dataPoint.getPosition();
			}
			if (dataPoint.getPosition() > xMax) {
				xMax = dataPoint.getPosition();
			}
			if (dataPoint.getValue() < yMin) {
				yMin = dataPoint.getValue();
			}
			if (dataPoint.getValue() > yMax) {
				yMax = dataPoint.getValue();
			}
		}

		double xRange = xMax - xMin;
		double yRange = yMax - yMin;

		if (xRange < 1) {
			xRange = 1;
		}
		if (yRange < 1) {
			yRange = 1;
		}

		double xMultiplier = innerWidth / xRange;
		double yMultiplier = innerHeight / yRange;

		int prevX = (int) (xMultiplier * data.get(0).getPosition());
		int prevY = (int) (yMultiplier * data.get(0).getValue());

		int minX = (int) (xMultiplier * xMin);
		int minY = (int) (yMultiplier * yMin);

		int offsetX = BORDER_WIDTH - minX;
		int offsetY = BORDER_WIDTH + innerHeight + minY;

		for (GraphDataPoint dataPoint : data) {

			int newX = (int) (xMultiplier * dataPoint.getPosition());
			int newY = (int) (yMultiplier * dataPoint.getValue());

			g.drawLine(
				prevX + offsetX,
				offsetY - prevY,
				newX + offsetX,
				offsetY - newY
			);

			prevX = newX;
			prevY = newY;
		}
	}

}
