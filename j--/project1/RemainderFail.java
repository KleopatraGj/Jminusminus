import java.lang.System;

public class RemainderFail {
    public static void main(String[] args) {
        System.out.println(42 % 5);
        System.out.println("42" % 5);
        System.out.println(42 % "5");
        System.out.println("42" % "5");
    }
}
