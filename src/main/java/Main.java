import city.Building;
import geometry.Point;
import geometry.Polygon;
import geometry.Segment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

//        Segment[] edges = new Segment[6];
////        edges[0] = new Segment(0.0, 2.5, 2.5, 0);
////        edges[1] = new Segment(2.5, 0, 5.0, 1.0);
////        edges[2] = new Segment(5.0, 1.0, 4.5, 3.5);
////        edges[3] = new Segment(4.5, 3.5, 2.5, 5.5);
////        edges[4] = new Segment(2.5, 5.5, 0.0, 2.5);
//        //CityGraph mainGraph = new CityGraph(edges);
//
////        edges[0] = new Segment(0.0, 50, 50, 0);
////        edges[1] = new Segment(50, 0, 100, 20);
////        edges[2] = new Segment(100, 20, 90, 70);
////        edges[3] = new Segment(90, 70, 50, 110);
////        edges[4] = new Segment(50, 110, 0.0, 50);
//
//        double multiplier = 1;
//
////        edges[0] = new Segment(0.0 * multiplier, 2.5 * multiplier, 2.5 * multiplier, 0 * multiplier);
////        edges[1] = new Segment(2.5 * multiplier, 0 * multiplier, 5.0 * multiplier, 1.0 * multiplier);
////        edges[2] = new Segment(5.0 * multiplier, 1.0 * multiplier, 4.5 * multiplier, 3.5 * multiplier);
////        edges[3] = new Segment(4.5 * multiplier, 3.5 * multiplier, 2.5 * multiplier, 5.5 * multiplier);
////        edges[4] = new Segment(2.5 * multiplier, 5.5 * multiplier, 0.0 * multiplier, 2.5 * multiplier);
//
//        edges[0] = new Segment(1 * multiplier, 1 * multiplier, 4 * multiplier, 0.4 * multiplier);
//        edges[1] = new Segment(4 * multiplier, 0.4 * multiplier, 7 * multiplier, 2 * multiplier);
//        edges[2] = new Segment(7 * multiplier, 2 * multiplier, 6 * multiplier, 5 * multiplier);
//        edges[3] = new Segment(6 * multiplier, 5 * multiplier, 2 * multiplier, 6 * multiplier);
//        edges[4] = new Segment(2 * multiplier, 6 * multiplier, 0 * multiplier, 3 * multiplier);
//        edges[5] = new Segment(0 * multiplier, 3 * multiplier, 1 * multiplier, 1 * multiplier);
//
//        Segment segment = new Segment(-2, 0, 0, 0);
//        Segment segment2 = new Segment(0, 0, 0, 2);
//
//        //System.out.println(segment.getTiltedParallel(0 ,0, 1));
//
////        Point[] vertexes = new Point[5];
////        vertexes[0] = new Point(0.0, 2.5);
////        vertexes[1] = new Point(2.5, 0);
////        vertexes[2] = new Point(5.0, 1.0);
////        vertexes[3] = new Point(4.5, 3.5);
////        vertexes[4] = new Point(2.5, 5.5);
//
//        List<Point> vertexes = new ArrayList<>();
//        vertexes.add(new Point(0.0, 2.5));
//        vertexes.add(new Point(2.5, 0));
//        vertexes.add(new Point(5.0, 1.0));
//        vertexes.add(new Point(4.5, 3.5));
//        vertexes.add(new Point(2.5, 5.5));
//        vertexes.add(new Point(2, 3));
//
//        Polygon polygon = new Polygon(vertexes);
//
//        Point point = new Point(2.2, 3.8);
//
//        city.Quarter quarter = new city.Quarter(edges);
//        List<Building> buildings = quarter.fill();
//
//        Serializer serializer = new Serializer("buildings.json");
//        serializer.serializeBuildings(buildings);

        Random random = new Random();
        double max = 0;
        for (int i = 0; i < 100; i++) {
            double v = Math.abs(random.nextGaussian());
            if (v > max) {
                max = v;
            }
        }
        System.out.println(max);
    }
}
