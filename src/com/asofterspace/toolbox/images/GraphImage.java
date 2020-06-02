/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.utils.DateUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * An image displaying a graph
 */
public class GraphImage extends Image {

	// pixels around the graph on all sides
	// (in that area, labels are allowed!)
	private int BORDER_WIDTH = 10;

	private List<GraphDataPoint> data;

	private ColorRGB backgroundColor;
	private ColorRGB foregroundColor;
	private ColorRGB dataColor;

	private Double baseXmin;
	private Double baseXmax;
	private Double baseYmin;
	private Double baseYmax;

	private boolean includeTodayInTimeData = false;

	// all of the following are calculated when calculateNumerics() is called by redraw() and
	// stay constant until the next redraw()
	private int outerWidth;
	private int outerHeight;
	private int innerWidth;
	private int innerHeight;
	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;
	private double xRange;
	private double yRange;
	private double xMultiplier;
	private double yMultiplier;
	private int prevX;
	private int prevY;
	private int minX;
	private int minY;
	private int offsetX;
	private int offsetY;


	public GraphImage(int width, int height) {
		super(width, height);
	}

	public GraphImage() {
		super();
	}

	public void setInnerWidthAndHeight(int newWidth, int newHeight) {
		init(newWidth + (2 * BORDER_WIDTH), newHeight + (2 * BORDER_WIDTH));
	}

	/**
	 * Set absolute data points
	 */
	public void setAbsoluteDataPoints(List<GraphDataPoint> newData) {

		Collections.sort(newData, new Comparator<GraphDataPoint>() {
			public int compare(GraphDataPoint a, GraphDataPoint b) {
				return (int) (a.getPosition() - b.getPosition());
			}
		});

		this.data = newData;

		redraw();
	}

	/**
	 * Set relative data points that have dates associated with them
	 * (relative meaning that each data point contains the difference to the previous one)
	 */
	public void setRelativeTimeDataPoints(List<GraphTimeDataPoint> timeData) {

		setTimeDataPoints(timeData, false);
	}

	/**
	 * Set absolute data points that have dates associated with them
	 */
	public void setAbsoluteTimeDataPoints(List<GraphTimeDataPoint> timeData) {

		setTimeDataPoints(timeData, true);
	}

	private void setTimeDataPoints(List<GraphTimeDataPoint> timeData, boolean pointsAreAbsolute) {

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

		if (includeTodayInTimeData) {
			if (firstDate.after(new Date())) {
				firstDate = new Date();
			}
			if (lastDate.before(new Date())) {
				lastDate = new Date();
			}
		}

		List<Date> days = DateUtils.listDaysFromTo(firstDate, lastDate);

		List<GraphDataPoint> actualData = new ArrayList<>();
		int x = 1;
		double y = 0;
		int i = 0;
		for (Date day : days) {
			while ((i < timeData.size()) && DateUtils.isSameDay(day, timeData.get(i).getDateTime())) {
				if (pointsAreAbsolute) {
					y = timeData.get(i).getValue();
				} else {
					y += timeData.get(i).getValue();
				}
				i++;
			}
			actualData.add(new GraphDataPoint(x, y, DateUtils.serializeDate(day)));
			x++;
		}

		// call setDataPoints such that the sorting will be called again
		// (the datapoints are already sorted, but meh, you never know... ^^)
		setAbsoluteDataPoints(actualData);
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

	public ColorRGB getBackgroundColor() {
		if (backgroundColor == null) {
			return new ColorRGB(255, 255, 255);
		}
		return backgroundColor;
	}

	public void setBackgroundColor(ColorRGB backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public ColorRGB getForegroundColor() {
		if (foregroundColor == null) {
			return new ColorRGB(0, 0, 0);
		}
		return foregroundColor;
	}

	public void setForegroundColor(ColorRGB foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public void setDataColor(ColorRGB newColor) {
		this.dataColor = newColor;
	}

	public ColorRGB getDataColor() {
		if (dataColor == null) {
			return getForegroundColor();
		}
		return dataColor;
	}

	public Double getBaseXmin() {
		return baseXmin;
	}

	public void setBaseXmin(Double baseXmin) {
		this.baseXmin = baseXmin;
	}

	public Double getBaseXmax() {
		return baseXmax;
	}

	public void setBaseXmax(Double baseXmax) {
		this.baseXmax = baseXmax;
	}

	public Double getBaseYmin() {
		return baseYmin;
	}

	public void setBaseYmin(Double baseYmin) {
		this.baseYmin = baseYmin;
	}

	public Double getBaseYmax() {
		return baseYmax;
	}

	public void setBaseYmax(Double baseYmax) {
		this.baseYmax = baseYmax;
	}

	public boolean getIncludeTodayInTimeData() {
		return includeTodayInTimeData;
	}

	/**
	 * Set this before calling setRelativeTimeDataPoints or setAbsoluteTimeDataPoints!
	 */
	public void setIncludeTodayInTimeData(boolean includeTodayInTimeData) {
		this.includeTodayInTimeData = includeTodayInTimeData;
	}

	/**
	 * Draws a vertical line inside the graph (it will vanish again when redraw() is called!)
	 */
	public void drawVerticalLineAt(int position, ColorRGB col) {

		int newX = (int) (xMultiplier * position);

		drawLine(
			newX + offsetX,
			BORDER_WIDTH,
			newX + offsetX,
			getHeight() - BORDER_WIDTH,
			col
		);
	}

	private void calculateNumerics() {

		outerWidth = getWidth();
		outerHeight = getHeight();
		innerWidth = outerWidth - 2 * BORDER_WIDTH;
		innerHeight = outerHeight - 2 * BORDER_WIDTH;

		if (data == null) {
			return;
		}
		if (data.size() < 1) {
			return;
		}

		xMin = data.get(0).getPosition();
		if (baseXmin != null) {
			xMin = baseXmin;
		}
		xMax = xMin;
		if (baseXmax != null) {
			xMax = baseXmax;
		}
		yMin = 0;
		if (baseYmin != null) {
			yMin = baseYmin;
		}
		yMax = 0;
		if (baseYmax != null) {
			yMax = baseYmax;
		}

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

		xRange = xMax - xMin;
		yRange = yMax - yMin;

		if (xRange < 1) {
			xRange = 1;
		}
		if (yRange < 1) {
			yRange = 1;
		}

		xMultiplier = innerWidth / xRange;
		yMultiplier = innerHeight / yRange;

		prevX = (int) (xMultiplier * data.get(0).getPosition());
		prevY = (int) (yMultiplier * data.get(0).getValue());

		minX = (int) (xMultiplier * xMin);
		minY = (int) (yMultiplier * yMin);

		offsetX = BORDER_WIDTH - minX;
		offsetY = BORDER_WIDTH + innerHeight + minY;
	}

	@Override
	public void redraw() {

		super.redraw();

		calculateNumerics();

		drawRectangle(0, 0, getWidth()-1, getHeight()-1, getBackgroundColor());

		ColorRGB black = getForegroundColor();
		// y axis
		drawLine(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH + innerHeight, black);
		drawLine(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH - 4, BORDER_WIDTH + 9, black);
		drawLine(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH + 4, BORDER_WIDTH + 9, black);
		// x axis
		drawLine(BORDER_WIDTH, BORDER_WIDTH + innerHeight, BORDER_WIDTH + innerWidth, BORDER_WIDTH + innerHeight, black);
		drawLine(BORDER_WIDTH + innerWidth, BORDER_WIDTH + innerHeight, BORDER_WIDTH + innerWidth - 9, BORDER_WIDTH + innerHeight - 4, black);
		drawLine(BORDER_WIDTH + innerWidth, BORDER_WIDTH + innerHeight, BORDER_WIDTH + innerWidth - 9, BORDER_WIDTH + innerHeight + 4, black);

		if (data == null) {
			return;
		}
		if (data.size() < 1) {
			return;
		}

		ColorRGB dataColor = getDataColor();

		for (GraphDataPoint dataPoint : data) {

			int newX = (int) (xMultiplier * dataPoint.getPosition());
			int newY = (int) (yMultiplier * dataPoint.getValue());

			drawLine(
				prevX + offsetX,
				offsetY - prevY,
				newX + offsetX,
				offsetY - newY,
				dataColor
			);

			prevX = newX;
			prevY = newY;
		}
	}

}
