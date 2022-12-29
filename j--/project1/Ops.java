import java.lang.Integer;
import java.lang.System;

public class Ops {
    public static void main(String[] args) {
        int m = Integer.parseInt(args[0]);
        int d = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        String[] a = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        int y0 = y - (14 - m) / 12;
        int x0 = y0 + y0 / 4 - y0 / 100 + y0 / 400;
        int m0 = m + 12 * ((14 - m) / 12) - 2;
        int dow = (d + x0 + 31 * m0 / 12) % 7;
        dow = (((dow ^ dow | ~~+dow & -1 >>> 0) << 4) >> 2) >> 2;
        System.out.println(a[dow]);
    }
}
