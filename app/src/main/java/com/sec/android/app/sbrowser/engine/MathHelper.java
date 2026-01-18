package com.sec.android.app.sbrowser.engine;

public class MathHelper {

    /**
     * @param min include min value.
     * @param max include max value.
     * @return
     */
    public static long randomRange(long min, long max) {
        if (min > max) {
            long temp = max;
            max = min;
            min = temp;
        }

        long range = max - min;

        if (range < 0) {
            range -= 1;
        } else {
            range += 1;
        }

        return (long)(Math.random() * range) + min;
    }

    /**
     * @param min include min value.
     * @param max include max value.
     * @return
     */
    public static double randomRange(double min, double max) {
        if (min > max) {
            double temp = max;
            max = min;
            min = temp;
        }

        double range = max - min;

        return (Math.random() * range) + min;
    }
}
