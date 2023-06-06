package city;

import geometry.Point;

import java.util.List;

public record Building(List<Point> vertices, String color) {

    @Override
    public String toString() {
        return "\n{\n" +
                "   color: \"" + color + "\",\n" +
                "   \"vertexes\": \n    " + vertices +
                "\n}";
    }
}
