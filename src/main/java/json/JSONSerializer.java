package json;

import city.Building;
import com.google.gson.*;
import geometry.Point;
import geometry.Segment;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

public class JSONSerializer {

    private final Gson gson;

    public JSONSerializer() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Segment.class, new EdgeSerializer())
                .create();
    }

    private static class EdgeSerializer implements JsonSerializer<Segment> {
        @Override
        public JsonElement serialize(Segment src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();

            JsonArray start = new JsonArray();
            start.add(src.getX1());
            start.add(src.getY1());
            object.add("start", start);

            JsonArray end = new JsonArray();
            end.add(src.getX2());
            end.add(src.getY2());
            object.add("end", end);

            return object;
        }
    }

    public void serializeGraph(Collection<Segment> edges, Collection<Point> vertices) {
        try {
            FileWriter edgesWriter = new FileWriter("graph_edges.json");
            gson.toJson(edges, edgesWriter);
            edgesWriter.flush();
            edgesWriter.close();

            FileWriter verticesWriter = new FileWriter("graph_vertices.json");
            gson.toJson(vertices, verticesWriter);
            verticesWriter.flush();
            verticesWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void serializeBuildings(Collection<Building> buildings) {

        try {
            FileWriter buildingsWriter = new FileWriter("buildings.json");
            gson.toJson(buildings, buildingsWriter);
            buildingsWriter.flush();
            buildingsWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
