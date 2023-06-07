package city;

import geometry.Point;

import java.util.List;

public record Building(int quarterID, String color, List<Point> vertexes) {

    @Override
    public String toString() {
        return "Building{" +
                "vertexes=" + vertexes +
                ", color='" + color + '\'' +
                ", quarterID=" + quarterID +
                '}';
    }
}
