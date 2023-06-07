import city.City;
import city.CityConfig;
import city.Quarter;
import json.JSONDeserializer;
import json.JSONSerializer;

public class Main {

    private static double[][] getMultipliers(String shape) {
        double[][] squareMultipliers = {{1.0, 0.0}, {0.0, 1.0}, {-1.0, 0.0}, {0.0, -1.0}};
        double[][] rhombusMultipliers = {{-0.45, -0.9}, {0.45, -0.9}, {0.45, 0.9}, {-0.45, 0.9}};
        double[][] crossMultipliers = {
                {0.0, -1.0}, {-1.0, 0.0},
                {0.0, -1.0}, {1.0, 0},
                {0.0, -1.0}, {1.0, 0.0},
                {0.0, 1.0}, {1.0, 0.0},
                {0.0, 1.0}, {-1.0, 0.0},
                {0.0, 1.0}, {-1.0, 0.0}};

        return switch (shape) {
//            case ("square") -> squareMultipliers;
            case ("rhombus") -> rhombusMultipliers;
            case ("cross") -> crossMultipliers;
            default -> squareMultipliers;
        };
    }

    private static void generateCity(String inputFilePath, String outputPath) {
        JSONDeserializer deserializer = new JSONDeserializer();
        CityConfig config = deserializer.deserializeCityConfig(inputFilePath);

        City city = new City();
        city.generateCity(config.start.get(0), config.start.get(1), config.sideLength, getMultipliers(config.shape), config.coloring);

        JSONSerializer serializer = new JSONSerializer();
        serializer.serializeCity(city, outputPath);
    }

    private static void generateQuarter(String inputFilePath, String outputPath) {
        JSONDeserializer deserializer = new JSONDeserializer();
        Quarter quarter = deserializer.deserializeQuarter(inputFilePath);
        quarter.fill();

        JSONSerializer serializer = new JSONSerializer();
        serializer.serializeBuildings(quarter.getBuildings(), outputPath);
    }

    public static void main(String[] args) {
//        args = new String[3];
//        args[0] = "city";
//        args[1] = "city_config.json";
//        args[1] = "quarter.json";
//        args[2] = "../";

        if (args == null || args.length < 3) {
            System.out.println("INCORRECT ARGUMENTS (amount)");
            return;
        }

        if (args[0].equals("city")) {
            generateCity(args[1], args[2]);
        } else if (args[0].equals("quarter")) {
            generateQuarter(args[1], args[2]);
        } else {
            System.out.println("INCORRECT ARGUMENTS (option)");
        }
    }

}
