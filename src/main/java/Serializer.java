import city.Building;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import geometry.Polygon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class Serializer {

    private final Gson gson;
    private final String path;


    public Serializer(String path) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.path = path;
    }

    public void serializeBuildings(List<Building> buildings) {
        File file = new File(path);
        Writer writer;
        try {
            writer = new FileWriter(file, false);
            gson.toJson(buildings, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Exception thrown while writing to file " + path);
            e.printStackTrace();
        }

    }
}
