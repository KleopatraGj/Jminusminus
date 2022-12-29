import java.lang.System;

public class LRightShiftFail {
    public static void main(String[] args) {
        System.out.println(16 >>> 2);
        System.out.println("16" >>> 2);
        System.out.println(16 >>> "2");
        System.out.println("16" >>> "2");
    }
}
