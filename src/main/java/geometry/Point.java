package geometry;

import java.util.Locale;

public class Point {
    public Double x;
    public Double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }


    public double distance(Double pointX, double pointY) {
        return Math.sqrt(Math.pow((x - pointX), 2) + Math.pow((y - pointY), 2));
    }

    public double distance(Point point) {
        return Math.sqrt(Math.pow((x - point.x), 2) + Math.pow((y - point.y), 2));
    }

    public Point round() {
        double newX = x - (x % 0.001);
        double newY = y - (y % 0.001);
        return new Point(newX, newY);
    }

    public void roundThis() {
        x = x - (x % 0.001);
        y = y - (y % 0.001);
    }

    public boolean isNan() {
        return x.isNaN() || y.isNaN();
    }

    public boolean equals(Point point) {
        return Math.abs((this.x - point.x)) < 0.01 && Math.abs((this.y - point.y)) < 0.01;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "\n{\"x\": %.2f, \"y\": %.2f}", x, y);
    }

}
