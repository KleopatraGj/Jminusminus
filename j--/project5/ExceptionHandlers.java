import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Math;
import java.lang.NumberFormatException;
import java.lang.System;

public class ExceptionHandlers {
    public static void main(String[] args) {
        try {
            double x = Double.parseDouble(args[0]);
            double result = sqrt(x);
            System.out.println(result);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("x not specified");
        } catch (NumberFormatException e) {
            System.out.println("x must be a double");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("Done!");
        }
    }

    private static double sqrt(double x) throws IllegalArgumentException {
        if (x < 0.0) {
            throw new IllegalArgumentException("x must be positve");
        }
        return Math.sqrt(x);
    }
}
