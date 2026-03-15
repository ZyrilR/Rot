package utils;

import java.util.Random;

public class RandomUtil {
    private static final Random rand = new Random();

    /**
     * Returns true with the given percentage chance.
     * @param percent Chance in percentage (0-100)
     */
    public static boolean chance(double percent) {
        return rand.nextDouble() * 100 < percent;
    }

    /**
     * Returns a random integer between min and max inclusive.
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Random int in [min, max]
     */
    public static int range(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min cannot be greater than max");
        return min + rand.nextInt(max - min + 1);
    }
}
