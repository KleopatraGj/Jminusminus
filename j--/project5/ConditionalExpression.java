import java.lang.System;
import java.util.Random;

public class ConditionalExpression {
    public static void main(String[] args) {
        Random rng = new Random();
        String result = rng.nextBoolean() ? "Heads" : "Tails";
        System.out.println(result);
    }
}
