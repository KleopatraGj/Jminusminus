import java.lang.System;

public class ARightShiftFail {
    public static void main(String[] args) {
        System.out.println(16 >> 2);
        System.out.println("16" >> 2);
        System.out.println(16 >> "2");
        System.out.println("16" >> "2");
    }
}
