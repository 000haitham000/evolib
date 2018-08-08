/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.Random;

/**
 * This class is a wrapper around the default Java random number generator. Any
 * other random number generator gan be used here instead.
 *
 * @author Haitham Seada
 */
public class RandomNumberGenerator {

    private static Random rand = new Random();

    /* Fetch a single random number between 0.0 and 1.0 */
    public static double next() {
        return rand.nextDouble();
    }

    /**
     * Restarts the random number generator using a new seed. Notice that a seed
     * should be a real number in the range [0, 1] and this number is then
     * translated to an integer number in the range [0, MAX_LONG] in order to
     * fit Java's random number generator.
     *
     * @param seed the seed to set
     */
    public static void setSeed(double seed) {
        rand = new Random((long) (seed * Long.MAX_VALUE));
    }

    /**
     * Fetch a single random integer between low and high including the bounds.
     *
     * @param low minimum limit
     * @param high maximum limit
     * @return a random integer number between the two limits inclusively
     */
    public static int nextInt(int low, int high) {
        int res;
        if (low >= high) {
            res = low;
        } else {
            res = (int) (low + (RandomNumberGenerator.next()
                    * (high - low + 1)));
            if (res > high) {
                res = high;
            }
        }
        return (res);
    }

    /**
     * Fetch a single random real between low and high including the bounds.
     *
     * @param low minimum limit
     * @param high maximum limit
     * @return a random real number between the two limits inclusively
     */
    public static double next(double low, double high) {
        return (low + (high - low) * RandomNumberGenerator.next());
    }
}
