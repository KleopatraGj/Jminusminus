import java.util.ArrayList;

import jminusminus.CLEmitter;

import static jminusminus.CLConstants.*;

/**
 * This class programmatically generates the class file for the following Java application:
 * 
 * <pre>
 * public class IsPrime {
 *     // Entry point.
 *     public static void main(String[] args) {
 *         int n = Integer.parseInt(args[0]);
 *         boolean result = isPrime(n);
 *         if (result) {
 *             System.out.println(n + " is a prime number");
 *         } else {
 *             System.out.println(n + " is not a prime number");
 *         }
 *     }
 *
 *     // Returns true if n is prime, and false otherwise.
 *     private static boolean isPrime(int n) {
 *         if (n < 2) {
 *             return false;
 *         }
 *         for (int i = 2; i <= n / i; i++) {
 *             if (n % i == 0) {
 *                 return false;
 *             }
 *         }
 *         return true;
 *     }
 * }
 * </pre>
 */
public class GenIsPrime {
    public static void main(String[] args) {
        CLEmitter e = new CLEmitter(true);
        
        // I believe I will need an ArrayList to store the modifiers
        // like in the GenFactorial.java file
        ArrayList<String> modifiers = new ArrayList<String>();

        // For public class IsPrime {
        modifiers.add("public");
        e.addClass(modifiers, "IsPrime", "java/lang/Object", null, true);

        // For the public static void main(String[] args) {
        modifiers.clear();
        modifiers.add("public");
        modifiers.add("static");
        e.addMethod(modifiers, "main", "([Ljava/lang/String;)V", null, true);

        // For the int n = Integer.parseInt(args[0]);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt",
                "(Ljava/lang/String;)I");
        e.addNoArgInstruction(ISTORE_1);

        // For the boolean result = isPrime(n)
        e.addNoArgInstruction(ILOAD_1);
        // using Z for boolean
        e.addMemberAccessInstruction(INVOKESTATIC, "IsPrime", "isPrime", "(I)Z");
        e.addNoArgInstruction(ISTORE_2);

        //if (result) branch to a number being a prime number
        e.addNoArgInstruction(ILOAD_2);
        e.addBranchInstruction(IFEQ, "NOTPRIME");

        // Base case: System.out.println(n + " is a prime number");
        // Get System.out in the stack
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        // Creating an instance (sb) of StringBuffer on stack for the string concatenations
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        // create a duplicate of the instance
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");

        // sb.append(n);
        e.addNoArgInstruction(ILOAD_1);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(I)Ljava/lang/StringBuffer;");
        // sb.append(" is a prime number");
        e.addLDCInstruction(" is a prime number");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        //System.out.println(sb.toString());
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "toString", "()Ljava/lang/String;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                "(Ljava/lang/String;)V");
        // return;
        e.addNoArgInstruction(RETURN);

        // NOTPRIME case: System.out.println(n + " is not a prime number");
        e.addLabel("NOTPRIME");
        // Get System.out in the stack
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        // Creating an instance (sb) of StringBuffer on stack for the string concatenations
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        // create a duplicate of the instance
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");

        // sb.append(n);
        e.addNoArgInstruction(ILOAD_1);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(I)Ljava/lang/StringBuffer;");
        // sb.append(" is not a prime number");
        e.addLDCInstruction(" is not a prime number");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        //System.out.println(sb.toString());
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "toString", "()Ljava/lang/String;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                "(Ljava/lang/String;)V");
        // return;
        e.addNoArgInstruction(RETURN);


        // For the private static boolean isPrime(int n) {
        modifiers.clear();
        modifiers.add("private");
        modifiers.add("static");
        e.addMethod(modifiers, "isPrime", "(I)Z", null, true);

        // if (n >= 2) {
        // ILOAD_0 loads n, pushes n into the stack
        e.addNoArgInstruction(ILOAD_0);
        // Use the integer ICONST_2 to represent the number 2 for the condition in
        // the if statement
        e.addNoArgInstruction(ICONST_2);
        // IE2 = "i = 2"
        // check if the if condition is correct
        e.addBranchInstruction(IF_ICMPGE, "IE2");

        // return false
        // false is being represented by 0
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(IRETURN);

        // I think I will treat this for loop as a while loop
        // I will first initialize the integer i which is being initialiazed
        // inside the parenthesis of the for loop and then do the rest as of
        // it being a while loop.
        e.addLabel("IE2");
        // int i = 2;
        e.addNoArgInstruction(ICONST_2);
        e.addNoArgInstruction(ISTORE_1);

        // start the for loop: for (int i = 2; i <= n / i; i++) {
        e.addLabel("FORLOOP");
        // load i
        e.addNoArgInstruction(ILOAD_1);
        // load n
        e.addNoArgInstruction(ILOAD_0);
        // load i
        e.addNoArgInstruction(ILOAD_1);
        // do the division
        e.addNoArgInstruction(IDIV);
        // check the condition
        e.addBranchInstruction(IF_ICMPGT, "ENDFORLOOP");

        // Inside the for loop
        // if ( n % i == 0) {
        // load n
        e.addNoArgInstruction(ILOAD_0);
        //load i
        e.addNoArgInstruction(ILOAD_1);
        // find the remainder between n and i
        e.addNoArgInstruction(IREM);
        // check the condition
        e.addBranchInstruction(IFNE, "OUTOFIF");

        //BASE CASE: return false;
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(IRETURN);

        e.addLabel("OUTOFIF");
        // continue in the for loop
        e.addIINCInstruction(1, 1);
        e.addBranchInstruction(GOTO, "FORLOOP");

        // end of the for loop
        e.addLabel("ENDFORLOOP");
        // return true;
        e.addNoArgInstruction(ICONST_1);
        e.addNoArgInstruction(IRETURN);

        e.write();
    }
}
