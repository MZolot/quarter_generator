package city;

import geometry.Point;
import geometry.Randomizer;
import geometry.Segment;

import java.util.*;

public class Quarter {

    private final Segment[] borders;
    private String colour = "poor";

    private final List<List<Segment>> verticalWalls;
    private final List<Building> buildings;

    // parameters (configuration)
    private final double SIZE_MULTIPLIER = 30;

    private final double MIN_WALL_LENGTH = 0.3 * SIZE_MULTIPLIER;
    private final double MAX_WALL_LENGTH = 0.7 * SIZE_MULTIPLIER;
    private final double WALL_LENGTH_RANGE = (MAX_WALL_LENGTH - MIN_WALL_LENGTH) / 2;
    private final double AVG_WALL_LENGTH = MAX_WALL_LENGTH - WALL_LENGTH_RANGE;
    private final double WALL_TO_WALL_RANGE = 0.55;

    private final double MAX_BORDER_WALL_OFFSET = 0.01 * SIZE_MULTIPLIER;

    private final double MAX_LENGTH = 10 * SIZE_MULTIPLIER;

    public Quarter(Segment[] borders) {
        this.borders = borders;
        verticalWalls = new ArrayList<>();
        buildings = new ArrayList<>();
    }

    public Quarter(List<Segment> borders) {
        this.borders = borders.toArray(new Segment[0]);
        verticalWalls = new ArrayList<>();
        buildings = new ArrayList<>();
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
                double length = Randomizer.randomFromAverage(AVG_WALL_LENGTH, WALL_LENGTH_RANGE);
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
//                    while (previousBorderWalls.size() == 0) { //такого вроде не должно быть, но нужно добавить проверку, что не попали на ту же сторону
//                        k--;
//                        if (k < 0) break;
//                        previousBorderWalls = verticalWalls.get(k);
//                    }
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
            generateCornerNotRectangular(wall, previousWall); //тут чуть другой алгоритм нужен
            return;
        }
        Segment lengthWall1 = new Segment(wall.getX1(), wall.getY1(), vertex1.x, vertex1.y);
        Segment lengthWall2 = new Segment(previousWall.getX1(), previousWall.getY1(), vertex1.x, vertex1.y);
        if (lengthWall1.length() > MAX_WALL_LENGTH || lengthWall2.length() > MAX_WALL_LENGTH) {
            generateCornerPentagon(wall, previousWall);
            return;
        }

        Point vertex2 = previousWall.getPointOnSegment(Math.random() * (MAX_BORDER_WALL_OFFSET));
        //double length = AVG_WALL_LENGTH + nextGaussian() * WALL_LENGTH_RANGE;
        Segment offset = previousWall.getPerpendicular(vertex2.x, vertex2.y, -MAX_LENGTH);
        Point vertex4 = wall.getPointOnSegment(Math.random() * MAX_BORDER_WALL_OFFSET);
        Segment offsetPrevious = wall.getPerpendicular(vertex4.x, vertex4.y, MAX_LENGTH);
        Point vertex3 = offset.getIntersection(offsetPrevious);

        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);

        if (vertexes.stream().anyMatch(Objects::isNull)) return;

        buildings.add(new Building(vertexes, colour));
    }

    private void generateCornerNotRectangular(Segment wall, Segment previousWall) {
        Segment lengthWall;

        double offset = Math.random() * MAX_BORDER_WALL_OFFSET;

        lengthWall = wall.getParallel(wall.getX1(), wall.getY1(), offset);
        Point vertex4 = new Point(lengthWall.getX2(), lengthWall.getY2());

        lengthWall = previousWall.getParallel(previousWall.getX1(), previousWall.getY1(), offset);
        Point vertex3 = new Point(lengthWall.getX2(), lengthWall.getY2());

        double width = vertex3.distance(vertex4);
        double wallLength = Randomizer.randomFromAverageWithMinMax(width, width * WALL_TO_WALL_RANGE, MIN_WALL_LENGTH, MAX_WALL_LENGTH);

        lengthWall = wall.getParallel(wall.getX1(), wall.getY1(), wallLength);
        Point vertex1 = new Point(lengthWall.getX2(), lengthWall.getY2());

        lengthWall = previousWall.getParallel(previousWall.getX1(), previousWall.getY1(), wallLength);
        Point vertex2 = new Point(lengthWall.getX2(), lengthWall.getY2());


        List<Point> vertexes = new ArrayList<>();

        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);

        if (vertexes.stream().anyMatch(Objects::isNull)) return;

        buildings.add(new Building(vertexes, colour));
    }

    private void generateCornerPentagon(Segment wall, Segment previousWall) {
        List<Point> vertexes = new ArrayList<>();

//        double maxLength = WALL_LENGTH_RANGE * 0.75;

        Segment lengthWall1 = wall.getParallel(wall.getX1(), wall.getY1(), Randomizer.randomFromAverage(AVG_WALL_LENGTH, WALL_LENGTH_RANGE));
        Point vertex1 = new Point(lengthWall1.getX2(), lengthWall1.getY2());

        Segment lengthWall2 = previousWall.getParallel(previousWall.getX1(), previousWall.getY1(), Randomizer.randomFromAverage(AVG_WALL_LENGTH, WALL_LENGTH_RANGE));
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

        if (vertexes.stream().anyMatch(Objects::isNull)) return;

        buildings.add(new Building(vertexes, colour));
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

        lengthWall = wall.getParallel(wall.getX1(), wall.getY1(), Randomizer.randomFromAverageWithMinMax(width, width * WALL_TO_WALL_RANGE, MIN_WALL_LENGTH, MAX_WALL_LENGTH));
        Point vertex1 = new Point(lengthWall.getX2(), lengthWall.getY2());

        perpendicular = wall.getPerpendicular(lengthWall.getX2(), lengthWall.getY2(), MAX_WALL_LENGTH);
        Point vertex2 = perpendicular.getIntersection(previousWall);

        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);

        if (vertexes.stream().anyMatch(Objects::isNull)) return;

        buildings.add(new Building(vertexes, colour));
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

    public String toStringBorders() {
        return Arrays.stream(borders).toList().toString();
    }
}
