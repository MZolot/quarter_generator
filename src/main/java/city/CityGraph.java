package city;

import geometry.Point;
import geometry.Polygon;
import geometry.Randomizer;
import geometry.Segment;
import json.JSONReader;

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

    private final double MIN_EDGE_LENGTH = 1.2 * MULTIPLIER;
    private final double MAX_EDGE_LENGTH = 3 * MULTIPLIER;
    private final double EDGE_LENGTH_RANGE = (MAX_EDGE_LENGTH - MIN_EDGE_LENGTH) / 2;
    private final double AVG_EDGE_LENGTH = MAX_EDGE_LENGTH - EDGE_LENGTH_RANGE;

    private final double SIDE_LENGTH_ERROR_PERCENT = 0.07 / 2;
    private final double BORDER_TILT_PERCENT = 0.05 * MULTIPLIER;


    public CityGraph() {
    }

    public void generateCity(double startX, double startY, double length, double[][] shapeMultipliers, Map<String, Double> coloringConfig) throws IOException {
        BufferedWriter buildingsWriter = new BufferedWriter(new FileWriter("src/main/resources/buildings.json", false));

        generateCityGraph(startX, startY, length, shapeMultipliers);

        List<Quarter> quarters = cityPolygon.getQuarters();

        BufferedWriter quartersWriter = new BufferedWriter(new FileWriter("src/main/resources/quarters.json"));
        quartersWriter.write(quarters.toString().replace("start", "\"start\"").replace("end","\"end\""));
        quartersWriter.flush();

//        List<Quarter> quarters = JSONReader.readQuarters("src/main/resources/quarters.json");

        colorQuarters(quarters, coloringConfig);

        List<Building> allBuildings = new ArrayList<>();
        for (Quarter quarter : quarters) {
            List<Building> buildings = quarter.fill();
            if (buildings != null) {
                allBuildings.addAll(buildings.stream().filter(b -> !b.vertices.isEmpty()).toList());
            }
        }
        buildingsWriter.write(allBuildings.toString());
        buildingsWriter.flush();
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
                n = number;
                continue;
            }
            if (n >= number) {
                typeAmounts.put(type, 0);
                continue;
            }
            typeAmounts.put(type, amount);
            n += amount;
        }

        if (n < number) {
            String randomType = coloringConfig.keySet().stream().toList().get((int) (Math.random() * coloringConfig.size()));
            typeAmounts.put(randomType, typeAmounts.get(randomType) + (number - n));
        }

//        System.out.println(typeAmounts.keySet());

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

//        System.out.println(quarters);
    }

    public void generateCityGraph(double startX, double startY, double length, double[][] shapeMultipliers) throws IOException {
        borders = generateShapedBorders(startX, startY, length, shapeMultipliers);
        //cityPolygon = new Polygon(vertices);
        cityPolygon = new Polygon(borders);

//        vertices = new ArrayList<>();
//        for (Segment border : borders) {
//            vertices.add(new Point(border.getX1(), border.getY1()));
//        }


        BufferedWriter bordersWriter = new BufferedWriter(new FileWriter("src/main/resources/borders.txt"));
        bordersWriter.write(borders.toString());
        bordersWriter.flush();

//        BufferedWriter verticesWriter = new BufferedWriter(new FileWriter("src/main/resources/vertex.txt"));
//        verticesWriter.write(vertices.toString());
//        verticesWriter.flush();

        List<Segment> edges = cityPolygon.fill();

        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/graph.txt"));
        writer.write(edges.toString());
        writer.flush();
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
                shapeMultipliers[multipliersNumber - 1][1], startX, startY);
        borderSegments.addAll(segments);

        return borderSegments;
    }

    private List<Segment> generateStraightSideSegments(double startX, double startY, double xMultiplier, double yMultiplier, double sideLength) {
        double xOffset = Randomizer.nextGaussian() * sideLength * SIDE_LENGTH_ERROR_PERCENT;
        double endX = startX + (xMultiplier * sideLength) + xOffset;
        double yOffset = Randomizer.nextGaussian() * sideLength * SIDE_LENGTH_ERROR_PERCENT;
        double endY = startY + (yMultiplier * sideLength) + yOffset;
        //System.out.println((new Segment(startX, startY, endX, endY)));

        return generateStraightSideSegments(startX, startY, xMultiplier, yMultiplier, endX, endY);
    }

    private List<Segment> generateStraightSideSegments(double startX, double startY, double xMultiplier, double yMultiplier, double endX, double endY) {
        List<Segment> segments = new ArrayList<>();
        double length = 0;
        double x = startX;
        double y = startY;

        double newX;
        double newY;

        double sideLength = (new Point(startX, startY)).distance(endX, endY);

        while (true) {
            double xLengthDeviation = Randomizer.nextGaussian() * EDGE_LENGTH_RANGE;
            double xLength = xMultiplier * (AVG_EDGE_LENGTH + xLengthDeviation);
            double xDeviation = Randomizer.nextGaussian() * BORDER_TILT_PERCENT * yMultiplier;
            newX = x + xLength + xDeviation;

            double yLengthDeviation = Randomizer.nextGaussian() * EDGE_LENGTH_RANGE;
            double yLength = yMultiplier * (AVG_EDGE_LENGTH + yLengthDeviation);
            double yDeviation = Randomizer.nextGaussian() * BORDER_TILT_PERCENT * xMultiplier;
            newY = y + yLength + yDeviation;
            //newY = y + yMultiplier * (AVG_EDGE_LENGTH + Randomizer.nextGaussian() * EDGE_LENGTH_RANGE) + Randomizer.nextGaussian() * BORDER_TILT_PERCENT * MULTIPLIER;

            Segment segment = new Segment(x, y, newX, newY);
            double s = segment.length();
            length += s;
            double lengthLeft = sideLength - length;

            if (lengthLeft < MIN_EDGE_LENGTH) {
                if ((lengthLeft >= 0 && segment.length() + lengthLeft <= MAX_EDGE_LENGTH) ||
                        (lengthLeft < 0 && segment.length() + lengthLeft >= MIN_EDGE_LENGTH) ||
                        (new Point(x, y).distance(endX, endY) >= MIN_EDGE_LENGTH)) {
                    segment = new Segment(x, y, endX, endY);
                    segments.add(segment);
                    break;
                } else if (lengthLeft > 0) {
                    newX = x + (xMultiplier * (lengthLeft + segment.length() / 2) + (xMultiplier * 0.5 * Randomizer.nextGaussian()));
                    newY = y + (yMultiplier * (lengthLeft + segment.length() / 2) + (yMultiplier * 0.5 * Randomizer.nextGaussian()));
                    segment = new Segment(x, y, newX, newY);
                } else {
                    System.out.println("AAAAAAAAAAAAAAA");
                }
            }

            segments.add(segment);
            x = newX;
            y = newY;
        }
        return segments;
    }

    private List<Segment> findPreviousEdges(Point vertex) {
        return borders.stream().filter(segment -> segment.isEndPoint(vertex)).collect(Collectors.toList());
    }

    private List<Segment> findNextEdges(Point vertex) {
        return borders.stream().filter(segment -> segment.isStartPoint(vertex)).collect(Collectors.toList());
    }

    private void generateQuarters() {
        List<Point> uncheckedVertices = new ArrayList<>(vertices);
        for (Point vertex : uncheckedVertices) {
            Point firstVertex = (findNextEdges(vertex) == null || findNextEdges(vertex).size() == 0) ?
                    vertex : findNextEdges(vertex).get(0).getEndPoint();

        }
    }

    public static void main(String[] args) throws IOException {
        CityGraph cityGraph = new CityGraph();
        double[][] sqareMultipliers = {{1.0, 0.0}, {0.0, 1.0}, {-1.0, 0.0}, {0.0, -1.0}};
        double[][] rhombusMultipliers = {{-0.45, -0.9}, {0.45, -0.9}, {0.45, 0.9}, {-0.45, 0.9}};
        double[][] crossMultipliers = {
                {0.0, -1.0}, {-1.0, 0.0},
                {0.0, -1.0}, {1.0, 0},
                {0.0, -1.0}, {1.0, 0.0},
                {0.0, 1.0}, {1.0, 0.0},
                {0.0, 1.0}, {-1.0, 0.0},
                {0.0, 1.0}, {-1.0, 0.0}};

        Map<String, Double> coloringConfig = new HashMap<>();
        coloringConfig.put("park", 0.15);
        coloringConfig.put("poor", 0.3);
        coloringConfig.put("middle", 0.1);
        coloringConfig.put("industrial", 0.2);
        coloringConfig.put("market", 0.1);
        coloringConfig.put("square", 0.1);
        coloringConfig.put("rich", 0.05);

        cityGraph.generateCity(80, 80, 300, sqareMultipliers, coloringConfig);
    }

}
