import java.lang.Double;
import java.lang.Math;
import java.lang.System;

public class Stats {
    public static double mean(double[] a) {
        int n = a.length;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += a[i];
        }
        return sum / (double) n;
    }

    public static double stddev(double[] a) {
        int n = a.length;
        double mean = mean(a);
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += (a[i] - mean) * (a[i] - mean);
        }
        return Math.sqrt(sum / (double) n);
    }

    public static void main(String[] args) {
        double[] a = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        System.out.println("Mean   = " + Stats.mean(a));
        System.out.println("Stddev = " + Stats.stddev(a));
    }
}
