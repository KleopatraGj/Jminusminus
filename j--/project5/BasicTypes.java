import java.lang.Double;
import java.lang.Integer;
import java.lang.Math;
import java.lang.System;

public class BasicTypes {
    private static void compute(double a, double b, double c, int n) {
        quadratic(a, b, c);
        System.out.println("fibonacci(" + n + ") = " + fibonacci(n));
    }
    
    private static void quadratic(double a, double b, double c) {
        double discriminant = b * b - 4.0 * a * c;
        double root1 = (-b + Math.sqrt(discriminant)) / (2.0 * a);
        double root2 = (-b - Math.sqrt(discriminant)) / (2.0 * a);
        System.out.print("Roots of " + a + "x^2 + " + b + "x + " + c + " = 0: ");
        System.out.print(root1 + ", ");
        System.out.println(root2);
    }

    private static long fibonacci(int n) {
        long a = 1L, b = 1L;
        int i = 3;
        while (i <= n) {
            long temp = a;
            a = b;
            b += temp;
            ++i;
        }
        return b;
    }
    
    public static void main(String[] args) {
        double a = Double.parseDouble(args[0]);
        double b = Double.parseDouble(args[1]);
        double c = Double.parseDouble(args[2]);
        int n = Integer.parseInt(args[3]);
        compute(a, b, c, n);
    }
}
