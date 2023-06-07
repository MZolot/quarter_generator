package city;

import geometry.Point;
import geometry.Randomizer;
import geometry.Segment;

import java.util.*;

public class Quarter {

    private final Segment[] borders;
    private String colour = "poor";

    private final List<List<Segment>> verticalWalls;
    private final List<Segment> outerVerticalWalls;
    private final List<Building> buildings;

    // parameters (configuration)
    private final double SIZE_MULTIPLIER = 30;

    private final double MIN_WALL_LENGTH = 0.4 * SIZE_MULTIPLIER;
    private final double MAX_WALL_LENGTH = 0.75 * SIZE_MULTIPLIER;
    private final double WALL_LENGTH_RANGE = (MAX_WALL_LENGTH - MIN_WALL_LENGTH) / 2;
    private final double AVG_WALL_LENGTH = MAX_WALL_LENGTH - WALL_LENGTH_RANGE;
    private final double WALL_TO_WALL_RANGE = 0.55;

    private final double MAX_BORDER_WALL_OFFSET = 0.01 * SIZE_MULTIPLIER;

    private final double MAX_LENGTH = 10 * SIZE_MULTIPLIER;

    public Quarter(Segment[] borders) {
        this.borders = borders;
        verticalWalls = new ArrayList<>();
        outerVerticalWalls = new ArrayList<>();
        buildings = new ArrayList<>();
    }

    public Quarter(List<Segment> borders) {
        this.borders = borders.toArray(new Segment[0]);
        verticalWalls = new ArrayList<>();
        outerVerticalWalls = new ArrayList<>();
        buildings = new ArrayList<>();
    }

    public Quarter(List<Segment> borders, String color) {
        this.borders = borders.toArray(new Segment[0]);
        verticalWalls = new ArrayList<>();
        outerVerticalWalls = new ArrayList<>();
        buildings = new ArrayList<>();
        this.colour = color;
    }

    public List<Building> fill() {
        generateVerticalWalls();

        if (colour.equals("square") || colour.equals("park")) {
//        if (colour.equals("square") || colour.equals("park") || verticalWalls.size() < 2) {
            List<Point> vertices = new ArrayList<>();
            for (Segment border : borders) {
                vertices.add(border.getEndPoint());
            }
            buildings.add(new Building(vertices, colour));
            return buildings;
        }

        if (verticalWalls.stream().flatMap(Collection::stream).toList().size() < 3) {
            List<Point> vertices = new ArrayList<>();
            for (Segment border : borders) {
                vertices.add(border.getEndPoint());
            }
            buildings.add(new Building(vertices, colour));
            return buildings;
        }

        generateHorizontalWalls();

        return buildings;
    }


    private void generateVerticalWalls() {
        for (int i = 0; i < borders.length; i++) {
            Segment edge = borders[i];
            if (edge.length() < MIN_WALL_LENGTH) continue;

            List<Segment> wallsForEdge = new ArrayList<>();
            double x = edge.getX1();
            double y = edge.getY1();

            while (true) {
                //double length = MIN_WALL_LENGTH + nextGaussian() * (MAX_WALL_LENGTH - MIN_WALL_LENGTH);{
                double length = Randomizer.randomAverage(AVG_WALL_LENGTH, WALL_LENGTH_RANGE);
                Segment lengthSegment = edge.getParallel(x, y, length);
                x = lengthSegment.getX2();
                y = lengthSegment.getY2();

                Segment lengthLeft = new Segment(x, y, edge.getX2(), edge.getY2());

                if (!edge.isOnSegment(x, y) || lengthLeft.length() < MIN_WALL_LENGTH) {
                    Segment nextEdge = borders[(borders.length + i + 1) % borders.length];
                    if (edge.getReversed().getAngleCos(nextEdge) < 0) {
                        Segment average = edge.getAverageSegment(nextEdge.getReversed()).getParallel(edge.getX2(), edge.getY2(), MAX_LENGTH);
                        if (!average.intersectsExtended(borders)) {
                            //average = edge.getAverageSegment(nextEdge.getReversed()).getParallel(edge.getX2(), edge.getY2(), -MAX_LENGTH);
                            break;
                        }
                        wallsForEdge.add(average);
                    }
                    break;
                }

                wallsForEdge.add(edge.getPerpendicular(x, y, MAX_LENGTH));
            }
            if (wallsForEdge.size() != 0) {
                verticalWalls.add(wallsForEdge);
            }
        }
    }


    private void generateHorizontalWalls() {
        int size = verticalWalls.size();
        for (int i = 0; i < size; i++) {
            List<Segment> borderWalls = verticalWalls.get(i);
            for (int j = 0; j < borderWalls.size(); j++) {
                Segment wall = borderWalls.get(j);
                Segment previousWall;
                if (j == 0) {
                    int k = (size + i - 1) % size;
                    List<Segment> previousBorderWalls = verticalWalls.get(k);
                    if (k == i) return;
                    previousWall = previousBorderWalls.get(previousBorderWalls.size() - 1);
                    generateCornerBuilding(wall, previousWall);
                } else if (j == borderWalls.size() - 1) {
                    previousWall = borderWalls.get(j - 1);
                    generateCornerNotRectangular(wall, previousWall);
                } else {
                    previousWall = borderWalls.get(j - 1);
                    generateCentralBuilding(wall, previousWall);
                }
            }
        }
    }


    private void generateCornerBuilding(Segment wall, Segment previousWall) {
        List<Point> vertexes = new ArrayList<>();

        Point vertex1 = wall.getIntersection(previousWall);
        if (vertex1 == null) {
            generateCornerNotRectangular(wall, previousWall);
            return;
        }
        Segment lengthWall1 = new Segment(wall.getX1(), wall.getY1(), vertex1.x, vertex1.y);
        Segment lengthWall2 = new Segment(previousWall.getX1(), previousWall.getY1(), vertex1.x, vertex1.y);
        if (lengthWall1.length() > MAX_WALL_LENGTH || lengthWall2.length() > MAX_WALL_LENGTH) {
            generateCornerPentagon(wall, previousWall, lengthWall1.length(), lengthWall2.length());
            return;
        }

        Point vertex2 = previousWall.getPointOnSegment(Math.random() * (MAX_BORDER_WALL_OFFSET));
        Segment offset = previousWall.getPerpendicular(vertex2.x, vertex2.y, -MAX_LENGTH);
        Point vertex4 = wall.getPointOnSegment(Math.random() * MAX_BORDER_WALL_OFFSET);
        Segment offsetPrevious = wall.getPerpendicular(vertex4.x, vertex4.y, MAX_LENGTH);
        Point vertex3 = offset.getIntersection(offsetPrevious);

        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);

        vertexes = vertexes.stream().filter(v -> !v.isNan()).toList();

        buildings.add(new Building(vertexes, colour));
        outerVerticalWalls.add(new Segment(previousWall.getStartPoint(), vertex1));
        outerVerticalWalls.add(new Segment(wall.getStartPoint(), vertex1));
    }

    private void generateCornerNotRectangular(Segment wall, Segment previousWall) {
        Segment lengthWall;

        double offset = Math.random() * MAX_BORDER_WALL_OFFSET;

        lengthWall = wall.getParallel(wall.getX1(), wall.getY1(), offset);
        Point vertex4 = new Point(lengthWall.getX2(), lengthWall.getY2());

        lengthWall = previousWall.getParallel(previousWall.getX1(), previousWall.getY1(), offset);
        Point vertex3 = new Point(lengthWall.getX2(), lengthWall.getY2());

        Segment offsetWall = new Segment(vertex3, vertex4);
        double width = vertex3.distance(vertex4);
        double wallLength = Randomizer.randomAverageMinMax(width, width * WALL_TO_WALL_RANGE, MIN_WALL_LENGTH, MAX_WALL_LENGTH);

        List<Point> vertexes = new ArrayList<>();
        Point vertex1;
        Point vertex2;

        lengthWall = previousWall.getParallel(previousWall.getX1(), previousWall.getY1(), wallLength);

        Segment intersectedWall = lengthWall.getIntersectedExtendedSegment(outerVerticalWalls);
        if (intersectedWall != null && !Double.isNaN(lengthWall.getIntersection(intersectedWall).x)) {
            vertex2 = lengthWall.getIntersection(intersectedWall);

            vertex1 = intersectedWall.getEndPoint();
            double length = offsetWall.getDistanceToPoint(vertex1);
            lengthWall = wall.getParallel(wall.getX1(), wall.getY1(), length);
            Point vertex5 = lengthWall.getEndPoint();
            vertexes.add(vertex5);
        } else {
            vertex2 = new Point(lengthWall.getX2(), lengthWall.getY2());
            vertex1 = wall.getParallel(wall.getX1(), wall.getY1(), wallLength).getEndPoint();
        }

        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);

        vertexes = vertexes.stream().filter(v -> !v.isNan()).toList();

        buildings.add(new Building(vertexes, colour));
        outerVerticalWalls.add(new Segment(wall.getStartPoint(), vertex1));
        outerVerticalWalls.add(new Segment(previousWall.getStartPoint(), vertex2));
    }

    private void generateCornerPentagon(Segment wall, Segment previousWall, double lengthToIntersection1, double lengthToIntersection2) {
        List<Point> vertexes = new ArrayList<>();

//        double maxLength = WALL_LENGTH_RANGE * 0.75;

        double length1 = Randomizer.randomAverageMinMax(AVG_WALL_LENGTH, WALL_LENGTH_RANGE, MIN_WALL_LENGTH,
                Math.min(MAX_WALL_LENGTH, lengthToIntersection1));
        Segment lengthWall1 = wall.getParallel(wall.getX1(), wall.getY1(), length1);
        Point vertex1 = new Point(lengthWall1.getX2(), lengthWall1.getY2());

        double length2 = Randomizer.randomAverageMinMax(AVG_WALL_LENGTH, WALL_LENGTH_RANGE, MIN_WALL_LENGTH,
                Math.min(MAX_WALL_LENGTH, lengthToIntersection2));
        Segment lengthWall2 = previousWall.getParallel(previousWall.getX1(), previousWall.getY1(), length2);
        Point vertex2 = new Point(lengthWall2.getX2(), lengthWall2.getY2());

        Point vertex3 = previousWall.getPointOnSegment(Math.random() * MAX_BORDER_WALL_OFFSET);
        Segment offset = previousWall.getPerpendicular(vertex3.x, vertex3.y, -MAX_LENGTH);
        Point vertex5 = wall.getPointOnSegment(Math.random() * MAX_BORDER_WALL_OFFSET);
        Segment offsetPrevious = wall.getPerpendicular(vertex5.x, vertex5.y, MAX_LENGTH);
        Point vertex4 = offset.getIntersection(offsetPrevious);

        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);
        vertexes.add(vertex5);

        vertexes = vertexes.stream().filter(v -> !v.isNan()).toList();

        buildings.add(new Building(vertexes, colour));
        outerVerticalWalls.add(new Segment(wall.getStartPoint(), vertex1));
        outerVerticalWalls.add(new Segment(previousWall.getStartPoint(), vertex2));
    }

    private void generateCentralBuilding(Segment wall, Segment previousWall) {
        List<Point> vertexes = new ArrayList<>();

        Segment lengthWall;
        Segment perpendicular;

        lengthWall = wall.getParallel(wall.getX1(), wall.getY1(), Math.random() * MAX_BORDER_WALL_OFFSET);
        Point vertex4 = new Point(lengthWall.getX2(), lengthWall.getY2());

        perpendicular = lengthWall.getPerpendicular(lengthWall.getX2(), lengthWall.getY2(), MAX_LENGTH);
        Point vertex3 = perpendicular.getIntersection(previousWall);

        double width = vertex3.distance(vertex4);

        Point vertex1;
        Point vertex2;

        lengthWall = previousWall.getParallel(previousWall.getX1(), previousWall.getY1(),
                Randomizer.randomAverageMinMax(width, width * WALL_TO_WALL_RANGE, MIN_WALL_LENGTH, MAX_WALL_LENGTH));

        Segment intersectedWall = lengthWall.getIntersectedExtendedSegment(outerVerticalWalls);
        if (intersectedWall != null) {
            vertex2 = lengthWall.getIntersection(intersectedWall);

            vertex1 = intersectedWall.getEndPoint();
            double length = perpendicular.getDistanceToPoint(vertex1);
            lengthWall = wall.getParallel(wall.getX1(), wall.getY1(), length);
            Point vertex5 = lengthWall.getEndPoint();
            vertexes.add(vertex5);
        } else {
            vertex2 = new Point(lengthWall.getX2(), lengthWall.getY2());
            perpendicular = previousWall.getPerpendicular(lengthWall.getX2(), lengthWall.getY2(), -MAX_LENGTH);
            vertex1 = perpendicular.getIntersection(wall);
        }

        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);

        vertexes = vertexes.stream().filter(v -> !v.isNan()).toList();

        buildings.add(new Building(vertexes, colour));
        outerVerticalWalls.add(new Segment(wall.getStartPoint(), vertex1));
        outerVerticalWalls.add(new Segment(previousWall.getStartPoint(), vertex2));
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public Segment[] getBorders() {
        return borders;
    }

    public boolean isNeighbour(Quarter quarter) {
        boolean res = false;
        for (Segment border : this.borders) {
            res |= Arrays.stream(quarter.getBorders()).anyMatch(s -> s.equals(border));
        }
        return res;
    }

    @Override
    public String toString() {
        return "{" +
                "\"borders\": " + Arrays.toString(borders) +
                ",\n\"colour\": \"" + colour + '\"' +
                "}\n";
    }
}
