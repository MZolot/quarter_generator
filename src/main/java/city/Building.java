package city;

import geometry.Point;

import java.util.List;

public class Building {

    public final List<Point> vertices;
    public final String color;

    public Building(List<Point> vertices, String color) {
        this.vertices = vertices;
        this.color = color;
    }

    @Override
    public String toString() {
        return "\n{\n" +
//                "   color: \"" + color + "\",\n" +
                "   \"vertexes\": \n    " + vertices +
                "\n}";
    }
}
