import java.lang.Integer;
import java.lang.System;

interface Factorial {
    public int compute(int n);
}

class FactorialIter implements Factorial {
    public int compute(int n) {
        int result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}

class FactorialRec implements Factorial {
    public int compute(int n) {
        return n == 0 ? 1 : n * compute(n - 1);
    }
}

public class Interface {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        FactorialIter fIter = new FactorialIter();
        System.out.println("fIter(" + n + ") = " + fIter.compute(n));
        FactorialRec fRec = new FactorialRec();
        System.out.println("fRec(" + n + ")  = " + fRec.compute(n));
    }
}
