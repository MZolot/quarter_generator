package geometry;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Segment {

    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    public Segment(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Segment(Point point1, Point point2) {
        this.x1 = point1.x;
        this.y1 = point1.y;
        this.x2 = point2.x;
        this.y2 = point2.y;
    }

    public double length() {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    public Segment getPerpendicular(double x, double y) {
        double newX = x - (y2 - y1) / this.length();
        double newY = y + (x2 - x1) / this.length();
        return new Segment(x, y, newX, newY);
    }

    public Segment getPerpendicular(double x, double y, double length) {
        double newX = x - (y2 - y1) / this.length() * length;
        double newY = y + (x2 - x1) / this.length() * length;
        return new Segment(x, y, newX, newY);
    }

    public Segment getTiltedPerpendicular(double x, double y, double length, double yTiltPercent, double xTiltPercent) {
        double perpendicularX = x - (y2 - y1) / this.length() * length;
        double perpendicularY = y + (x2 - x1) / this.length() * length;

        double newX = perpendicularX + ((Math.random() - 0.5) * xTiltPercent * 2 * length);
        double newY = perpendicularY + ((Math.random() - 0.5) * yTiltPercent * 2 * length);

        return new Segment(x, y, newX, newY);
    }

    public Segment getTranslation(double x, double y) {
        double newX = x2 - (x1 - x);
        double newY = y2 - (y1 - y);
        return new Segment(x, y, newX, newY);
    }

    public Segment getParallel(double x, double y) {
        double newX = (x2 - (x1 - x) - x) / this.length() + x;
        double newY = (y2 - (y1 - y) - y) / this.length() + y;
        return new Segment(x, y, newX, newY);
    }

    public Segment getParallel(double x, double y, double length) {
        double newX = (x2 - (x1 - x) - x) / this.length() * length + x;
        double newY = (y2 - (y1 - y) - y) / this.length() * length + y;
        return new Segment(x, y, newX, newY);
    }

//    public Segment getTiltedParallel(double x, double y, double length, double yTiltPercent, double xTiltPercent) {
//        double parallelX = (x2 - (x1 - x) - x) / this.length() * length + x;
//        double parallelY = (y2 - (y1 - y) - y) / this.length() * length + y;
//
//        double newX = parallelX + ((Math.random() - 0.5) * xTiltPercent * 2 * length);
//        double newY = parallelY + ((Math.random() - 0.5) * yTiltPercent * 2 * length);
//
//        return new Segment(x, y, newX, newY);
//    }

    public Segment getNormal() {
        return this.getParallel(0, 0, 1);
    }

    public Segment getReversed() {
        return new Segment(x2, y2, x1, y1);
    }

    public Segment getTurnedAround() {
        return getParallel(x1, y1, -this.length());
    }

    public boolean intersects(Segment segment) {
        return getIntersection(segment) != null;
    }

    public boolean intersects(List<Segment> segments) {
        for (Segment segment : segments) {
            if (this.intersects(segment)) {
                return true;
            }
        }
        return false;
    }

    public boolean intersectsExtended(Segment segment) {
        if (this.intersects(segment)) {
            if (this.isStartPoint(segment.getStartPoint()) && this.isEndPoint(segment.getEndPoint())) {
                return false;
            }
            Point intersection = this.getIntersection(segment);
            return  !this.isStartPoint(intersection) && !this.isEndPoint(intersection) &&
                    !segment.isStartPoint(intersection) && !segment.isEndPoint(intersection);
        }
        return false;
    }

    public boolean intersectsExtended(List<Segment> segments) {
        if (segments == null) {
            return false;
        }
        for (Segment segment : segments) {
            if (this.intersects(segment)) {
                Point intersection = this.getIntersection(segment);
                if (this.isStartPoint(intersection) || this.isEndPoint(intersection) ||
                        segment.isStartPoint(intersection) || segment.isStartPoint(intersection)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    public boolean intersectsExtended(Segment[] segments) {
        return this.intersectsExtended(Arrays.stream(segments).toList());
    }

    public Segment getIntersectedExtendedSegment(List<Segment> segments) {
        if (segments == null) {
            return null;
        }
        for (Segment segment : segments) {
            if (this.intersectsExtended(segment)) {
                return segment;
            }
        }
        return null;
    }

    public Point getIntersection(Segment segment) {
        double v = x2 - x1;
        double w = y2 - y1;
        double v2 = segment.x2 - segment.x1;
        double w2 = segment.y2 - segment.y1;

        double t2 = (-w * segment.x1 + w * x1 + v * segment.y1 - v * y1) / (w * v2 - v * w2);
        double t = (segment.x1 - x1 + v2 * t2) / v;

        if (t < 0 || t > 1 || t2 < 0 || t2 > 1) return null;

        return new Point(segment.x1 + v2 * t2, segment.y1 + w2 * t2);
    }

    public boolean isOnSegment(double x, double y) {
        double distance1 = Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
        double distance2 = Math.sqrt(Math.pow((x2 - x), 2) + Math.pow((y2 - y), 2));
        return distance1 + distance2 - length() <= 0.05;
    }

    public boolean isOnSegment(Point point) {
        double distance1 = Math.sqrt(Math.pow((point.x - x1), 2) + Math.pow((point.y - y1), 2));
        double distance2 = Math.sqrt(Math.pow((x2 - point.x), 2) + Math.pow((y2 - point.y), 2));
        return distance1 + distance2 - length() <= 0.05;
    }

    public double getDistanceToPoint(Point point) {
        return getDistanceToPoint(point.x, point.y);
    }

    public double getDistanceToPoint(double pointX, double pointY) {
        double distance1 = Math.sqrt(Math.pow((x1 - pointX), 2) + Math.pow((y1 - pointY), 2));
        double distance2 = Math.sqrt(Math.pow((x2 - pointX), 2) + Math.pow((y2 - pointY), 2));

        double distance = Math.max(distance1, distance2);
        Segment perpendicular = this.getPerpendicular(pointX, pointY, distance);
        if (this.intersects(perpendicular)) {
            Point intersection = getIntersection(perpendicular);
            return Math.sqrt(Math.pow((pointX - intersection.x), 2) + Math.pow((pointY - intersection.y), 2));
        }

        perpendicular = this.getPerpendicular(pointX, pointY, -distance);
        if (this.intersects(perpendicular)) {
            Point intersection = getIntersection(perpendicular);
            return Math.sqrt(Math.pow((pointX - intersection.x), 2) + Math.pow((pointY - intersection.y), 2));
        }

        return Math.min(distance1, distance2);
    }

    public double getAngleCos(Segment segment) {
        Segment thisNormalized = this.getTranslation(0, 0);
        Segment normalized = segment.getTranslation(0, 0);
        double k = thisNormalized.getX2() * normalized.getX2() + thisNormalized.getY2() * normalized.getY2();
        return k / (thisNormalized.length() * normalized.length());
    }

    public Segment getAverageSegment(Segment segment) {
        double newX1 = (this.x1 + segment.x1) / 2;
        double newY1 = (this.y1 + segment.y1) / 2;

        Segment thisNormal = this.getNormal();
        Segment segmentNormal = segment.getNormal();

        double newX2 = (thisNormal.x2 + segmentNormal.x2) / 2;
        double newY2 = (thisNormal.y2 + segmentNormal.y2) / 2;

        return new Segment(0, 0, newX2, newY2).getParallel(newX1, newY1, 1);
    }

    public boolean isStartPoint(Point point) {
        return (Math.abs(point.x - x1) < 0.001) && (Math.abs(point.y - y1) < 0.001);
    }

    public boolean isEndPoint(Point point) {
        return (Math.abs(point.x - x2) < 0.001) && (Math.abs(point.y - y2) < 0.001);
    }

    public Point getStartPoint() {
        return new Point(x1, y1);
    }

    public Point getEndPoint() {
        return new Point(x2, y2);
    }

    public Point getPointOnSegment(double length) {
        Segment lengthSegment = getParallel(x1, y1, length);
        return new Point(lengthSegment.getX2(), lengthSegment.getY2());
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

//    @Override
//    public String toString() {
//        return String.format("Segment{(%.2f, %.2f), (%.2f, %.2f)}", x1, y1, x2, y2);
//    }

    public boolean equals(Segment segment) {
        return Math.abs(this.x1 - segment.x1) < 0.1 &&
                Math.abs(this.x2 - segment.x2) < 0.1 &&
                Math.abs(this.y1 - segment.y1) < 0.1 &&
                Math.abs(this.y2 - segment.y2) < 0.1;
    }

    @Override
    public String toString() {
        // for js
        return String.format(Locale.US, "{\nstart: [%.2f, %.2f],\nend: [%.2f, %.2f]\n}", x1, y1, x2, y2);
    }
}
