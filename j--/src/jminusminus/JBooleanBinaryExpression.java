// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * This abstract base class is the AST node for binary expressions that return booleans.
 */
abstract class JBooleanBinaryExpression extends JBinaryExpression {
    /**
     * Constructs an AST node for a boolean binary expression.
     *
     * @param line     line in which the boolean binary expression occurs in the source file.
     * @param operator the boolean binary operator.
     * @param lhs      lhs operand.
     * @param rhs      rhs operand.
     */

    protected JBooleanBinaryExpression(int line, String operator, JExpression lhs,
                                       JExpression rhs) {
        super(line, operator, lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String falseLabel = output.createLabel();
        String trueLabel = output.createLabel();
        this.codegen(output, falseLabel, false);
        output.addNoArgInstruction(ICONST_1); // true
        output.addBranchInstruction(GOTO, trueLabel);
        output.addLabel(falseLabel);
        output.addNoArgInstruction(ICONST_0); // false
        output.addLabel(trueLabel);
    }
}

/**
 * The AST node for an equality (==) expression.
 */
class JEqualOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for an equality expression.
     *
     * @param line line number in which the equality expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */

    public JEqualOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "==", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), rhs.type());
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type().isReference()) {
            output.addBranchInstruction(onTrue ? IF_ACMPEQ : IF_ACMPNE, targetLabel);
        } else {
            output.addBranchInstruction(onTrue ? IF_ICMPEQ : IF_ICMPNE, targetLabel);
        }
    }
}

/**
 * The AST node for a logical-and (&amp;&amp;) expression.
 */
class JLogicalAndOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for a logical-and expression.
     *
     * @param line line in which the logical-and expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JLogicalAndOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "&&", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        rhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        if (onTrue) {
            String falseLabel = output.createLabel();
            lhs.codegen(output, falseLabel, false);
            rhs.codegen(output, targetLabel, true);
            output.addLabel(falseLabel);
        } else {
            lhs.codegen(output, targetLabel, false);
            rhs.codegen(output, targetLabel, false);
        }
    }
}

/**
 * The AST node for a logical-or (||) expression.
 */
class JLogicalOrOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for a logical-or expression.
     *
     * @param line line in which the logical-or expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JLogicalOrOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "||", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // I copied this part from the JLogicalAndOp above and I made no changes to it.
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        rhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        // TODO
        // I copied this from the JLogicalAndOp method above. I had to change the
        // boolean arguments and the labels. In the case of the logical or, when onTrue
        // is true, we want our labels to be the target labels and the boolean argument
        // to be true
        if (onTrue) {
            lhs.codegen(output, targetLabel, true);
            rhs.codegen(output, targetLabel, true);
            // Otherwise we are creating a false label, and we have the lhs get the false
            // label as an argument, and true as a boolean, and the rhs get the target label
            // and false as a boolean.
        } else {
            String falseLabel = output.createLabel();
            lhs.codegen(output, falseLabel, true);
            rhs.codegen(output, targetLabel, false);
            output.addLabel(falseLabel);
        }
    }
}

/**
 * The AST node for a not-equal-to (!=) expression.
 */
class JNotEqualOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for not-equal-to (!=) expression.
     *
     * @param line line number in which the not-equal-to (!=) expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */

    public JNotEqualOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "!=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // I copied this from the JEqualOp above
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), rhs.type());
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        // TODO
        // I copied this from the JEqualOp
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type().isReference()) {
            // I just changed the base case and the alternate case. I included the opposite of
            // what the JEqualOp has. First, we get the not equal and then we get the equal as
            // an alternative.
            output.addBranchInstruction(onTrue ? IF_ACMPNE : IF_ACMPEQ, targetLabel);
        } else {
            output.addBranchInstruction(onTrue ? IF_ICMPNE : IF_ICMPEQ, targetLabel);
        }
    }
}
