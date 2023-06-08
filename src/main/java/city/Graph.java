package city;

import geometry.Point;
import geometry.Randomizer;
import geometry.Segment;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final List<Segment> edges;

    private final double MULTIPLIER = 60;
    private final double MIN_EDGE_LENGTH = 0.8 * MULTIPLIER;
    private final double MAX_EDGE_LENGTH = 1.5 * MULTIPLIER;

    private List<Segment> innerEdges;
    private List<Quarter> quarters;

    public Graph(List<Segment> edges) {
        this.edges = edges;
    }

    private List<Segment> generateVerticalEdges(List<Segment> borders, double maxLengthMultiplier, double minLengthMultiplier) {
        List<Segment> verticalEdges = new ArrayList<>();
        double maxLength = maxLengthMultiplier * MAX_EDGE_LENGTH;
        double minLength = minLengthMultiplier * MIN_EDGE_LENGTH;

        for (int i = 0; i < borders.size(); i++) {
            Segment edge = borders.get(i);
            if (edge.length() < minLength) continue; //добавить проверку на тупой угол?
            double x = edge.getX1();
            double y = edge.getY1();

            Segment nextEdge = borders.get((i + 1) % borders.size());

            while (true) {
                double length = Randomizer.randomMinMax(minLength, maxLength);
                Segment lengthSegment = edge.getParallel(x, y, length);
                x = lengthSegment.getX2();
                y = lengthSegment.getY2();

                double lengthLeft = new Point(x, y).distance(edge.getEndPoint());
                if (!edge.isOnSegment(x, y) || lengthLeft < minLength) {
                    x = edge.getX2();
                    y = edge.getY2();
                    Segment newEdge = edge.getTiltedPerpendicular(x, y, Randomizer.randomMinMax(minLength, maxLength), 0.4, 0.4);
                    if (!newEdge.intersectsExtended(borders) && !newEdge.intersectsExtended(verticalEdges)) {
                        double angle = newEdge.getAngleCos(nextEdge);
                        if (angle > 0.75) {
                            break;
                        }
                        verticalEdges.add(newEdge);
                    }
                    break;
                }

                Segment newEdge = edge.getTiltedPerpendicular(x, y, Randomizer.randomMinMax(minLength, maxLength), 0.4, 0.4);
                if (!newEdge.intersectsExtended(borders) && !newEdge.intersects(verticalEdges)) {
                    verticalEdges.add(newEdge);
                }
            }
        }
        return verticalEdges;
    }

    private List<Segment> generateHorizontalEdges(List<Segment> verticalEdges, List<Segment> innerBorders) {
        List<Segment> horizontalEdges = new ArrayList<>();
        int size = verticalEdges.size();
        for (int i = 0; i < size; i++) {
            Segment firstEdge = verticalEdges.get(i);
            int secondEdgeIndex = (verticalEdges.size() + i - 1) % verticalEdges.size();
            Segment secondEdge = verticalEdges.get(secondEdgeIndex);

            Point newEdgeEnd = firstEdge.getEndPoint();
            Point newEdgeStart = secondEdge.getEndPoint();
            Segment newEdge = new Segment(newEdgeStart, newEdgeEnd);

            double firstEdgeCos = firstEdge.getAngleCos(newEdge);
            double secondEdgeCos = secondEdge.getAngleCos(newEdge.getReversed());

            if (newEdge.intersectsExtended(verticalEdges) || newEdge.intersects(innerEdges) ||
                    firstEdgeCos > 0.65 || secondEdgeCos > 0.65) { // добавить проверку на угол с соседним вертикальным ребром?
                verticalEdges.remove(secondEdgeIndex);
                if (!horizontalEdges.isEmpty()) {
                    horizontalEdges.remove(horizontalEdges.size() - 1);
                    quarters.remove(quarters.size() - 1);
                }
                if (i > 0) {
                    i -= 2;
                } else {
                    i--;
                }
                size--;
                if (size == 1) {
                    verticalEdges.remove(0);
                    quarters.add(new Quarter(horizontalEdges, quarters.size()));
                    break;
                }
                continue;
            }

            horizontalEdges.add(newEdge);

            List<Point> quarterVertices = new ArrayList<>();
            quarterVertices.add(firstEdge.getStartPoint());
            quarterVertices.add(firstEdge.getEndPoint());
            quarterVertices.add(secondEdge.getEndPoint());
            quarterVertices.add(secondEdge.getStartPoint());

            addQuarter(quarterVertices, innerBorders);
        }
        return horizontalEdges;
    }

    private void addQuarter(List<Point> quarterVertices, List<Segment> innerBorders) {
        List<Segment> quarterBorders = new ArrayList<>();
        for (int i = 0; i < quarterVertices.size() - 1; i++) {
            quarterBorders.add(new Segment(quarterVertices.get(i), quarterVertices.get(i + 1)));
        }

        int firstEdgeBaseIndex = -1;
        int secondEdgeBaseIndex = -1;
        for (int i = 0; i < innerBorders.size(); i++) {
            Segment innerBorder = innerBorders.get(i);
            if (innerBorder.isOnSegment(quarterVertices.get(0)) && firstEdgeBaseIndex == -1) {
                firstEdgeBaseIndex = i;
            }
            if (innerBorder.isOnSegment(quarterVertices.get(3))) {
                secondEdgeBaseIndex = i;
            }
        }

        if (firstEdgeBaseIndex == -1 || secondEdgeBaseIndex == -1) {
            System.out.println("WHAT");
            quarterBorders.add(new Segment(quarterVertices.get(3), quarterVertices.get(0)));
        } else if (firstEdgeBaseIndex == secondEdgeBaseIndex) {
            quarterBorders.add(new Segment(quarterVertices.get(3), quarterVertices.get(0)));
        } else {
            quarterBorders.add(new Segment(quarterVertices.get(3), innerBorders.get(secondEdgeBaseIndex).getEndPoint()));
            int size = innerBorders.size();
            int i = (secondEdgeBaseIndex + 1) % size;
            while (i != firstEdgeBaseIndex) {
                quarterBorders.add(innerBorders.get(i));
                i = (i + 1) % size;
            }
            quarterBorders.add(new Segment(innerBorders.get(firstEdgeBaseIndex).getStartPoint(), quarterVertices.get(0)));
        }

        quarters.add(new Quarter(quarterBorders, quarters.size()));
    }

    public List<Segment> fill() {
        quarters = new ArrayList<>();
        innerEdges = new ArrayList<>(edges);

        double maxLengthMultiplier = 1;
        double minLengthMultiplier = 1;

        List<Segment> verticalEdges = generateVerticalEdges(edges, maxLengthMultiplier, minLengthMultiplier);

        List<Segment> horizontalEdges = new ArrayList<>(edges);
        List<Segment> innerPolygon = new ArrayList<>(edges);

        while (verticalEdges.size() > 1 && verticalEdges.stream().anyMatch(segment -> segment.length() > 0)) {
            horizontalEdges = generateHorizontalEdges(verticalEdges, horizontalEdges);
            if (!horizontalEdges.isEmpty()) {
                innerPolygon = new ArrayList<>(horizontalEdges);
            }
            innerEdges.addAll(verticalEdges);
            innerEdges.addAll(horizontalEdges);

            maxLengthMultiplier *= 0.8;
            minLengthMultiplier *= 0.95;

            verticalEdges = generateVerticalEdges(horizontalEdges, maxLengthMultiplier, minLengthMultiplier);
        }
        innerEdges.addAll(verticalEdges);
        quarters.add(new Quarter(innerPolygon, quarters.size()));

        return innerEdges;
    }

    public List<Quarter> getQuarters() {
        return quarters;
    }
}
