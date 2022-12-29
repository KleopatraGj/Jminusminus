import java.lang.Integer;
import java.lang.System;

public class BitwiseNot {
    public static void main(String[] args) {
        int a = Integer.parseInt(args[0]);
        int c = ~a;
        System.out.println(Integer.toBinaryString(c));
    }
}
