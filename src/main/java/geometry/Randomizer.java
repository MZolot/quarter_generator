package geometry;

import java.util.Random;

public class Randomizer {
    public static double nextGaussian() {
        Random random = new Random();
        double nextGaussian = random.nextGaussian();
        if (Math.abs(nextGaussian) > 1) {
            while (Math.abs(nextGaussian) > 1) {
                nextGaussian *= 0.7;
            }
        }
        return nextGaussian;
    }

    public static double randomFromAverage(double average, double range) {
        return average + nextGaussian() * range;
    }

    public static double randomFromMinMax(double min, double max) {
        return min + Math.random() * (max - min);
    }


    public static double randomFromAverageWithMinMax(double average, double range, double min, double max) {
        return Math.min(Math.max(average + nextGaussian() * range, min), max);
    }
}
