import java.lang.Integer;
import java.lang.System;

public class Operators2 {
    public static void main(String[] args) {
        int a = Integer.parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        System.out.println(a != b);
        System.out.println(a /= b);
        System.out.println(a -= b);
        System.out.println(a *= b);
        System.out.println(a %= b);
        System.out.println(a >>= b);
        System.out.println(a >>>= b);
        System.out.println(a >= b);
        System.out.println(a <<= b);
        System.out.println(a < b);
        System.out.println(a ^= b);
        System.out.println(a |= b);
        System.out.println(a == b || b == a);
        System.out.println(a &= b);
        System.out.println(a++);
        System.out.println(--b);
        System.out.println(a / b);
        System.out.println(a % b);
        System.out.println(a << b);
        System.out.println(a >> b);
        System.out.println(a >>> b);
        System.out.println(~a);
        System.out.println(a | b);
        System.out.println(a ^ b);
        System.out.println(a & b);
        System.out.println(+a);
    }
}
