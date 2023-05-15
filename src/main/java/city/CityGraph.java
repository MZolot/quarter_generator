package city;

import geometry.Point;
import geometry.Polygon;
import geometry.Segment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CityGraph {

    private List<Segment> borders;
    private List<Point> vertices;
    private Polygon cityPolygon;

    // parameters (configuration)
    private final double MULTIPLIER = 60;

    private final double MIN_QUARTER_WIDTH = 0.5;
    private final double MIN_ANGLE_COS = 0.5236;

    private final double MIN_EDGE_LENGTH = 0.5 * MULTIPLIER;
    private final double MAX_EDGE_LENGTH = 3 * MULTIPLIER;
    private final double EDGE_LENGTH_RANGE = (MAX_EDGE_LENGTH - MIN_EDGE_LENGTH) / 2;
    private final double AVG_EDGE_LENGTH = MAX_EDGE_LENGTH - EDGE_LENGTH_RANGE;

    private final double SIDE_LENGTH_ERROR_PERCENT = 0.05;
    private final double BORDER_TILT = 0.1 * MULTIPLIER;


    public CityGraph() {
    }

    public void generateCity(double startX, double startY, double length, double[][] shapeMultipliers, Map<String, Double> coloringConfig) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("buildings.txt"));

        generateCityGraph(startX, startY, length, shapeMultipliers);
        List<Quarter> quarters = cityPolygon.getQuarters();
        colorQuarters(quarters, coloringConfig);

        List<Building> allBuildings = new ArrayList<>();
        for (Quarter quarter : quarters) {
            //List<Building> buildings = quarter.fill();
            //System.out.println(buildings);
            //writer.append(buildings.toString());
            List<Building> buildings = quarter.fill();
            if (buildings != null) {
                allBuildings.addAll(buildings);
            }
        }
        writer.write(allBuildings.toString());
        writer.flush();
    }

    private void colorQuarters(List<Quarter> quarters, Map<String, Double> coloringConfig) {
        //quarter names: "park" "market" "square" "industrial" "poor" "middle" "rich"
        int number = quarters.size();
        String[] special = {"park", "market", "square"};
        String[] regular = {"industrial", "poor", "middle", "rich"};

        Map<String, Integer> typeAmounts = new HashMap<>();
        int n = 0;
        for (String type : coloringConfig.keySet()) {
            int amount = (int) Math.round(coloringConfig.get(type) * number);
            if (n + amount > number) {
                typeAmounts.put(type, number - n);
                continue;
            }
            if (n >= number) {
                typeAmounts.put(type, 0);
            }
            typeAmounts.put(type, amount);
            n += amount;
        }

        if (n < number) {
            String randomType = coloringConfig.keySet().stream().toList().get((int) (Math.random() * coloringConfig.size()));
            typeAmounts.put(randomType, typeAmounts.get(randomType) + (number - n));
        }

        System.out.println(typeAmounts.keySet());

        List<Quarter> uncolored = new ArrayList<>(quarters);
        for (String type : special) {
            int amount = typeAmounts.get(type);
            for (int i = 0; i < amount; i++) {
                int qNumber = (int) (Math.random() * uncolored.size());
                Quarter quarter = uncolored.get(qNumber);
                if (quarters.stream().anyMatch(s -> (quarter.isNeighbour(s) && type.equals(s.getColour())))) {
                    i--;
                    continue;
                }
                quarter.setColour(type);
                uncolored.remove(qNumber);
            }
        }


        for (String type : regular) {
            int amount = typeAmounts.get(type);
            for (int i = 0; i < amount; i++) {
                int qNumber = (int) (Math.random() * uncolored.size());
                Quarter quarter = uncolored.get(qNumber);
                quarter.setColour(type);
                uncolored.remove(qNumber);
            }
        }

        System.out.println(quarters);
    }

    public void generateCityGraph(double startX, double startY, double length, double[][] shapeMultipliers) throws IOException {
        borders = generateShapedBorders(startX, startY, length, shapeMultipliers);
        vertices = new ArrayList<>();
        for (Segment border : borders) {
            vertices.add(new Point(border.getX1(), border.getY1()));
        }
        cityPolygon = new Polygon(vertices);
        List<Segment> edges = cityPolygon.fill();

        BufferedWriter writer = new BufferedWriter(new FileWriter("graph.txt"));
        writer.write(edges.toString());
        writer.flush();

        System.out.println(edges);

        System.out.println("\n\n");

        vertices = new ArrayList<>();
        for (Segment edge : edges) {
            vertices.add(edge.getEndPoint());
        }
        //System.out.println(borders);
    }

    private List<Segment> generateShapedBorders(double startX, double startY, double length, double[][] shapeMultipliers) {
        double x = startX;
        double y = startY;
        List<Segment> borderSegments = new ArrayList<>();

        int multipliersNumber = shapeMultipliers.length;

        for (int i = 0; i < multipliersNumber - 1; i++) {
            List<Segment> segments = generateStraightSideSegments(x, y, shapeMultipliers[i][0], shapeMultipliers[i][1], length);
            x = segments.get(segments.size() - 1).getX2();
            y = segments.get(segments.size() - 1).getY2();
            borderSegments.addAll(segments);
        }
        List<Segment> segments = generateStraightSideSegments(x, y, shapeMultipliers[multipliersNumber - 1][0],
                shapeMultipliers[multipliersNumber - 1][1], length, startX, startY);
        borderSegments.addAll(segments);

        return borderSegments;
    }

    private List<Segment> generateStraightSideSegments(double startX, double startY, double XMultiplier, double YMultiplier, double sideLength) {
        double xOffset = nextGaussian() * sideLength * SIDE_LENGTH_ERROR_PERCENT;
        double endX = startX + (XMultiplier * sideLength) + xOffset;
        double yOffset = nextGaussian() * sideLength * SIDE_LENGTH_ERROR_PERCENT;
        double endY = startY + (YMultiplier * sideLength) + yOffset;
        //System.out.println((new Segment(startX, startY, endX, endY)));

        return generateStraightSideSegments(startX, startY, XMultiplier, YMultiplier, sideLength, endX, endY);
    }

    private List<Segment> generateStraightSideSegments(double startX, double startY, double XMultiplier, double YMultiplier, double sideLength, double endX, double endY) {
        List<Segment> segments = new ArrayList<>();
        double length = 0;
        double x = startX;
        double y = startY;

        double newX;
        double newY;

        double minSideLength = sideLength * (1 - SIDE_LENGTH_ERROR_PERCENT);
        double maxSideLength = sideLength * (1 + SIDE_LENGTH_ERROR_PERCENT);

        while (length <= minSideLength) {
            double xDeviation = nextGaussian() * EDGE_LENGTH_RANGE;
            double xLength = XMultiplier * (AVG_EDGE_LENGTH + xDeviation);
            newX = x + xLength + nextGaussian() * BORDER_TILT;

            double yDeviation = nextGaussian() * EDGE_LENGTH_RANGE;
            double yLength = YMultiplier * (AVG_EDGE_LENGTH + yDeviation);
            newY = y + yLength + nextGaussian() * BORDER_TILT;

            Segment segment = new Segment(x, y, newX, newY);
            double lengthLeft = sideLength - length - segment.length();

            length += segment.length();

            if (length >= minSideLength && (lengthLeft > 0 && lengthLeft < MIN_EDGE_LENGTH)) {
                if (segment.length() + lengthLeft <= MAX_EDGE_LENGTH) {
                    newX = endX;
                    newY = endY;
                    segment = new Segment(x, y, endX, endY);
                } else {
                    newX = x + (XMultiplier * (lengthLeft / 2) + (XMultiplier * 0.5 * nextGaussian()));
                    newY = y + (YMultiplier * (lengthLeft / 2) + (YMultiplier * 0.5 * nextGaussian()));
                    segment = new Segment(x, y, newX, newY);
                }
            }

            if (length > maxSideLength) {
                newX = endX;
                newY = endY;
                segment = new Segment(x, y, endX, endY);
            }

            segments.add(segment);
            x = newX;
            y = newY;
        }

        return segments;
    }

    private double nextGaussian() {
        Random random = new Random();
        double nextGaussian = random.nextGaussian();
        if (Math.abs(nextGaussian) <= 1) {
            return nextGaussian;
        } else return nextGaussian * 0.5;
    }

    private List<Segment> findPreviousEdges(Point vertex) {
        return borders.stream().filter(segment -> segment.isEndPoint(vertex)).collect(Collectors.toList());
    }

    private List<Segment> findNextEdges(Point vertex) {
        return borders.stream().filter(segment -> segment.isStartPoint(vertex)).collect(Collectors.toList());
    }

    private void generateQuarters() {
        List<Point> uncheckedVertices = new ArrayList<>();
        uncheckedVertices.addAll(vertices);
        for (Point vertex : uncheckedVertices) {
            Point firstVertex = (findNextEdges(vertex) == null || findNextEdges(vertex).size() == 0) ?
                    vertex : findNextEdges(vertex).get(0).getEndPoint();

        }
    }

    public static void main(String[] args) throws IOException {
        CityGraph cityGraph = new CityGraph();
        double[][] rectangleMultipliers = {{1.0, 0.0}, {0.0, 1.0}, {-1.0, 0.0}, {0.0, -1.0}};
        double[][] rhombusMultipliers = {{0.45, -0.9}, {0.45, 0.9}, {-0.45, 0.9}, {-0.45, -0.9}};
        double[][] crossMultipliers = {
                {1.0, 0.0}, {0.0, 1.0},
                {1.0, 0.0}, {0.0, 1.0},
                {-1.0, 0.0}, {0.0, 1.0},
                {-1.0, 0.0}, {0.0, -1.0},
                {-1.0, 0.0}, {0.0, -1.0},
                {1.0, 0}, {0.0, -1.0}};

        Map<String, Double> coloringConfig = new HashMap<>();
        coloringConfig.put("park", 0.15);
        coloringConfig.put("poor", 0.3);
        coloringConfig.put("middle", 0.1);
        coloringConfig.put("industrial", 0.2);
        coloringConfig.put("market", 0.1);
        coloringConfig.put("square", 0.1);
        coloringConfig.put("rich", 0.05);

        cityGraph.generateCity(80, 80, 300, rectangleMultipliers, coloringConfig);
    }

}
