package city;

import geometry.Point;
import geometry.Randomizer;
import geometry.Segment;
import json.JSONSerializer;

import java.util.*;

public class City {

    private final JSONSerializer jsonSerializer;

    private Graph cityGraph;
    private List<Building> allBuildings;
    private List<Segment> edges;
    private Set<Point> vertices;

    // parameters (configuration)
    private final double MULTIPLIER = 60;

    private final double MIN_EDGE_LENGTH = 1.2 * MULTIPLIER;
    private final double MAX_EDGE_LENGTH = 3 * MULTIPLIER;
    private final double EDGE_LENGTH_RANGE = (MAX_EDGE_LENGTH - MIN_EDGE_LENGTH) / 2;
    private final double AVG_EDGE_LENGTH = MAX_EDGE_LENGTH - EDGE_LENGTH_RANGE;

    private final double SIDE_LENGTH_ERROR_PERCENT = 0.07 / 2;
    private final double BORDER_TILT_PERCENT = 0.05 * MULTIPLIER;


    public City() {
        jsonSerializer = new JSONSerializer();
        allBuildings = new ArrayList<>();
    }

    public void generateCity(double startX, double startY, double length, double[][] shapeMultipliers, Map<String, Double> coloringConfig) {
        generateCityGraph(startX, startY, length, shapeMultipliers);

        List<Quarter> quarters = cityGraph.getQuarters();
        colorQuarters(quarters, coloringConfig);

        allBuildings = new ArrayList<>();
        for (Quarter quarter : quarters) {
            List<Building> buildings = quarter.fill();
            if (buildings != null) {
                allBuildings.addAll(buildings.stream().filter(b -> !b.vertexes().isEmpty()).toList());
            }
        }
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

    private void generateCityGraph(double startX, double startY, double length, double[][] shapeMultipliers) {
        List<Segment> borders = generateShapedBorders(startX, startY, length, shapeMultipliers);
        cityGraph = new Graph(borders);

        edges = cityGraph.fill();

        vertices = new HashSet<>();
        for (Segment edge : edges) {
            vertices.add(edge.getStartPoint());
            vertices.add(edge.getEndPoint());
        }
    }

    private List<Segment> generateShapedBorders(double startX, double startY, double length, double[][] shapeMultipliers) {
        double x = startX;
        double y = startY;
        List<Segment> borderSegments = new ArrayList<>();

        int multipliersNumber = shapeMultipliers.length;

        for (int i = 0; i < multipliersNumber - 1; i++) {
            List<Segment> segments = generateSide(x, y, shapeMultipliers[i][0], shapeMultipliers[i][1], length);
            x = segments.get(segments.size() - 1).getX2();
            y = segments.get(segments.size() - 1).getY2();
            borderSegments.addAll(segments);
        }

        List<Segment> segments = generateSide(x, y, shapeMultipliers[multipliersNumber - 1][0],
                shapeMultipliers[multipliersNumber - 1][1], startX, startY);
        borderSegments.addAll(segments);

        return borderSegments;
    }

    private List<Segment> generateSide(double startX, double startY, double xMultiplier, double yMultiplier, double sideLength) {
        double xOffset = Randomizer.nextGaussian() * sideLength * SIDE_LENGTH_ERROR_PERCENT;
        double endX = startX + (xMultiplier * sideLength) + xOffset;
        double yOffset = Randomizer.nextGaussian() * sideLength * SIDE_LENGTH_ERROR_PERCENT;
        double endY = startY + (yMultiplier * sideLength) + yOffset;

        return generateSide(startX, startY, xMultiplier, yMultiplier, endX, endY);
    }

    private List<Segment> generateSide(double startX, double startY, double xMultiplier, double yMultiplier, double endX, double endY) {
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
                }
            }

            segments.add(segment);
            x = newX;
            y = newY;
        }
        return segments;
    }

    public List<Building> getBuildings() {
        return allBuildings;
    }

    public List<Segment> getEdges() {
        return edges;
    }

    public Set<Point> getVertices() {
        return vertices;
    }
}
