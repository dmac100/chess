package ui;

import java.util.ArrayList;

class Turtle {
	private ArrayList<Integer> points = new ArrayList<Integer>();

	private double x;
	private double y;
	private double angle;

	public Turtle(double x, double y) {
		this.x = x;
		this.y = y;
		addPoint();
	}

	public void rotate(double a) {
		angle += (a / 180.0) * Math.PI;
	}

	public void forward(double d) {
		x += d * Math.sin(angle);
		y += d * Math.cos(angle);
		addPoint();
	}

	private void addPoint() {
		points.add((int)x);
		points.add((int)y);
	}

	public int[] getPoints() {
		int[] pointsArray = new int[points.size()];
		int i = 0;
		for(Integer x:points) {
			pointsArray[i++] = x;
		}
		return pointsArray;
	}

	public int getX() {
		return (int)x;
	}

	public int getY() {
		return (int)y;
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
