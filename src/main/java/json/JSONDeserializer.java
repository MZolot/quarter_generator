package json;

import city.CityConfig;
import city.Quarter;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import geometry.Segment;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JSONDeserializer {

    private final Gson gson;

    public JSONDeserializer() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Segment.class, new SegmentDeserializer())
                .registerTypeAdapter(Quarter.class, new QuarterDeserializer())
                .create();
    }

    private static class QuarterDeserializer implements JsonDeserializer<Quarter>
    {
        @Override
        public Quarter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            String color = jsonObject.get("color").getAsString();
            JsonArray jsonBorders = jsonObject.getAsJsonArray("borders");
            List<Segment> borders = new ArrayList<>();
            for (JsonElement jsonBorder : jsonBorders) {
                borders.add(context.deserialize(jsonBorder, Segment.class));
            }
            return new Quarter(borders, color);
        }
    }

    private static class SegmentDeserializer implements JsonDeserializer<Segment>
    {
        @Override
        public Segment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonArray start = jsonObject.get("start").getAsJsonArray();
            JsonArray end = jsonObject.get("end").getAsJsonArray();
            return new Segment(start.get(0).getAsDouble(), start.get(1).getAsDouble(), end.get(0).getAsDouble(), end.get(1).getAsDouble());
        }
    }

    public Quarter deserializeQuarter(String fileName) {
        Quarter quarter;
        try {
            JsonReader reader = new JsonReader(new FileReader(fileName));
            quarter = gson.fromJson(reader, Quarter.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return quarter;
    }

    public CityConfig deserializeCityConfig(String fileName) {
        CityConfig config;
        try {
            JsonReader reader = new JsonReader(new FileReader(fileName));
            config = gson.fromJson(reader, CityConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return config;

    }
}
