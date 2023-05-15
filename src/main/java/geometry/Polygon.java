package geometry;

import city.Quarter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Polygon {
    private final List<Point> vertices;
    private final List<Segment> edges;

    private final double MULTIPLIER = 40;
    private final double MIN_EDGE_LENGTH = 0.5 * MULTIPLIER;
    private final double MAX_EDGE_LENGTH = 3 * MULTIPLIER;
    private final double EDGE_LENGTH_RANGE = (MAX_EDGE_LENGTH - MIN_EDGE_LENGTH) / 2;
    private final double AVG_EDGE_LENGTH = (MAX_EDGE_LENGTH + EDGE_LENGTH_RANGE) / 2;
    private final double ZERO_EDGE_PROBABILITY = 0.1;

    private List<Segment> verticalEdges;
    private List<Quarter> quarters;

    public Polygon(List<Point> vertices) {
        this.vertices = vertices;
        edges = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            edges.add(new Segment(vertices.get(i), vertices.get((i + 1) % vertices.size())));
        }
    }

    public List<Point> getVertices() {
        return vertices;
    }

    private int findClosestEdgeIndex(Point point) {
        int minDistanceEdgeIndex = 0;
        double minDistance = 10000;
        for (int i = 0; i < edges.size(); i++) {
            double distance = edges.get(i).getDistanceToPoint(point);
            if (distance < minDistance) {
                minDistance = distance;
                minDistanceEdgeIndex = i;
            }
        }
        return minDistanceEdgeIndex;
    }

    private boolean isInsideByNormal(Point point, Segment previousEdge, Segment edge, Segment nextEdge) {
        double pointX = point.x;
        double pointY = point.y;
        double distance1 = Math.sqrt(Math.pow((edge.getX1() - pointX), 2) + Math.pow((edge.getY1() - pointY), 2));
        double distance2 = Math.sqrt(Math.pow((edge.getX2() - pointX), 2) + Math.pow((edge.getY2() - pointY), 2));

        double distance = Math.max(distance1, distance2);

        Segment vectorToPoint;
        Segment normalToPoint;

        Segment perpendicular = edge.getPerpendicular(pointX, pointY, distance);
        if (edge.isIntersecting(perpendicular)) {
            Point intersection = edge.getIntersection(perpendicular);
            vectorToPoint = new Segment(pointX, pointY, intersection.x, intersection.y);
            normalToPoint = edge.getPerpendicular(intersection.x, intersection.y, -distance);
        } else {
            perpendicular = edge.getPerpendicular(pointX, pointY, -distance);
            if (edge.isIntersecting(perpendicular)) {
                Point intersection = edge.getIntersection(perpendicular);
                vectorToPoint = new Segment(pointX, pointY, intersection.x, intersection.y);
                normalToPoint = edge.getPerpendicular(intersection.x, intersection.y, -distance);
            } else {
                if (distance1 < distance2) {
                    vectorToPoint = new Segment(pointX, pointY, edge.getX1(), edge.getY1());
                    normalToPoint = edge.getAverageSegment(previousEdge.getReversed()).getTurnedAround();
                } else {
                    vectorToPoint = new Segment(pointX, pointY, edge.getX2(), edge.getY2());
                    normalToPoint = edge.getReversed().getAverageSegment(nextEdge).getTurnedAround();
                }
            }
        }

        return vectorToPoint.getAngleCos(normalToPoint) > 0;
    }

    private double nextGaussian() {
        Random random = new Random();
        double nextGaussian = random.nextGaussian();
        if (Math.abs(nextGaussian) <= 1) {
            return nextGaussian;
        } else return nextGaussian * 0.5;
    }

    private boolean intersectsBorders(Segment segment) {
        for (Segment edge : edges) {
            if (segment.isIntersecting(edge)) {
                return true;
            }
        }
        return false;
    }

    private List<Segment> generateVerticalEdges(List<Segment> borders) {
        List<Segment> verticalEdges = new ArrayList<>();
        Random random = new Random();

        List<Point> innerVertices = new ArrayList<>();
        for (Segment border : borders) {
            innerVertices.add(border.getEndPoint());
        }
        Polygon innerPolygon = new Polygon(innerVertices);

        for (Segment edge : borders) {
            if (edge.length() < MIN_EDGE_LENGTH) continue;
            double x = edge.getX1();
            double y = edge.getY1();

            if (Math.random() < ZERO_EDGE_PROBABILITY) {
                verticalEdges.add(new Segment(x, y, x, y));
            }

            while (true) {
                //double length = MIN_WALL_LENGTH + nextGaussian() * (MAX_WALL_LENGTH - MIN_WALL_LENGTH);{
                double length = AVG_EDGE_LENGTH + nextGaussian() * EDGE_LENGTH_RANGE;
                Segment lengthSegment = edge.getParallel(x, y, length);
                x = lengthSegment.getX2();
                y = lengthSegment.getY2();

                if (!edge.isOnSegment(x, y)) {
                    break;
                }

                lengthSegment = new Segment(x, y, edge.getX2(), edge.getY2());
                if (lengthSegment.length() < MIN_EDGE_LENGTH) {
                    break;
                }

                if (Math.random() > ZERO_EDGE_PROBABILITY) {
                    Segment newEdge = edge.getTiltedPerpendicular(x, y, Math.random() * MAX_EDGE_LENGTH, 0.4, 0.4);
                    if (!innerPolygon.intersectsBorders(newEdge)) {
                        verticalEdges.add(newEdge);
                    }
                } else {
                    verticalEdges.add(new Segment(x, y, x, y));
                }
            }
        }
        return verticalEdges;
    }

    private List<Segment> generateHorizontalEdges(List<Segment> verticalEdges) {
        List<Segment> horizontalEdges = new ArrayList<>();
        int size = verticalEdges.size();
        for (int i = 0; i < size; i++) {
            List<Segment> quarterBorders = new ArrayList<>();
            Segment firstEdge = verticalEdges.get(i);
            Segment secondEdge;
            if (i == 0) {
                secondEdge = verticalEdges.get(size - 1);
            } else {
                secondEdge = verticalEdges.get(i - 1);
            }
            Point newEdgeEnd = firstEdge.getEndPoint();
            Point newEdgeStart = secondEdge.getEndPoint();
            Segment newEdge = new Segment(newEdgeStart, newEdgeEnd);
            horizontalEdges.add(newEdge);

            quarterBorders.add(new Segment(firstEdge.getStartPoint(), firstEdge.getEndPoint()));
            quarterBorders.add(new Segment(firstEdge.getEndPoint(), secondEdge.getEndPoint()));
            quarterBorders.add(new Segment(secondEdge.getEndPoint(), secondEdge.getStartPoint()));
            quarterBorders.add(new Segment(secondEdge.getStartPoint(), firstEdge.getStartPoint()));

//            quarterBorders.add(new Segment(firstEdge.getEndPoint(), firstEdge.getStartPoint()));
//            quarterBorders.add(new Segment(firstEdge.getStartPoint(), secondEdge.getStartPoint()));
//            quarterBorders.add(new Segment(secondEdge.getStartPoint(), secondEdge.getEndPoint()));
//            quarterBorders.add(new Segment(secondEdge.getEndPoint(), firstEdge.getEndPoint()));

            quarters.add(new Quarter(quarterBorders));
        }
        return horizontalEdges;
    }

    public boolean isInsidePolygon(Point point) {
        int minDistanceEdgeIndex = this.findClosestEdgeIndex(point);
        int previousIndex = (minDistanceEdgeIndex + edges.size() - 1) % edges.size();
        int nextIndex = (minDistanceEdgeIndex + 1) % edges.size();

        return isInsideByNormal(point, edges.get(previousIndex), edges.get(minDistanceEdgeIndex), edges.get(nextIndex));
    }

    public List<Segment> fill() {
        quarters = new ArrayList<>();
        List<Segment> innerEdges = new ArrayList<>(edges);

        verticalEdges = generateVerticalEdges(edges);

        List<Segment> horizontalEdges;

        while (verticalEdges.size() > 0 && verticalEdges.stream().anyMatch(segment -> segment.length() > 0)) {
            horizontalEdges = generateHorizontalEdges(verticalEdges);
            innerEdges.addAll(horizontalEdges);
            innerEdges.addAll(verticalEdges);
            //System.out.println(innerEdges + "\n\n\n");
            verticalEdges = generateVerticalEdges(horizontalEdges);
        }

        return innerEdges;
    }

    public List<Quarter> getQuarters() {
        return quarters;
    }
}
