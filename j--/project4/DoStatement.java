import java.lang.Integer;
import java.lang.System;

public class DoStatement {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int i = 0, sum = 0;
        do {
            sum += i++;
        } while(i <= n);
        System.out.println(sum);
    }
}
