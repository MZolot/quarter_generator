package city;

import geometry.Point;

import java.util.List;

public record Building(List<Point> vertexes, String color) {

    @Override
    public String toString() {
        return "\n{\n" +
                "   color: \"" + color + "\",\n" +
                "   \"vertexes\": \n    " + vertexes +
                "\n}";
    }
}
