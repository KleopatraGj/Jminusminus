import java.lang.System;

public class ALeftShiftFail {
    public static void main(String[] args) {
        System.out.println(16 << 2);
        System.out.println("16" << 2);
        System.out.println(16 << "2");
        System.out.println("16" << "2");
    }
}
