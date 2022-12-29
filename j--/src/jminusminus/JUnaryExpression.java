// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * This abstract base class is the AST node for an unary expression --- an expression with a
 * single operand.
 */
abstract class JUnaryExpression extends JExpression {
    /**
     * The unary operator.
     */
    protected String operator;

    /**
     * The operand.
     */
    protected JExpression operand;

    /**
     * Constructs an AST node for an unary expression.
     *
     * @param line     line in which the unary expression occurs in the source file.
     * @param operator the unary operator.
     * @param operand  the operand.
     */
    protected JUnaryExpression(int line, String operator, JExpression operand) {
        super(line);
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JUnaryExpression:" + line, e);
        e.addAttribute("operator", operator);
        e.addAttribute("type", type == null ? "" : type.toString());
        JSONElement e1 = new JSONElement();
        e.addChild("Operand", e1);
        operand.toJSON(e1);
    }
}

/**
 * The AST node for a logical NOT (!) expression.
 */
class JLogicalNotOp extends JUnaryExpression {
    /**
     * Constructs an AST for a logical NOT expression.
     *
     * @param line line in which the logical NOT expression occurs in the source file.
     * @param arg  the operand.
     */
    public JLogicalNotOp(int line, JExpression arg) {
        super(line, "!", arg);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        operand = (JExpression) operand.analyze(context);
        operand.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
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

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        operand.codegen(output, targetLabel, !onTrue);
    }
}

/**
 * The AST node for a unary negation (-) expression.
 */
class JNegateOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a negation expression.
     *
     * @param line    line in which the negation expression occurs in the source file.
     * @param operand the operand.
     */
    public JNegateOp(int line, JExpression operand) {
        super(line, "-", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        operand = operand.analyze(context);
        //operand.type().mustMatchExpected(line(), Type.INT);
        //type = Type.INT;

        // Here, I am checking what kind of type can the operand be, and then, I am updating the type
        // variable
        // At first, I check if the operand is of type INT
        if (operand.type().equals(Type.INT)) {
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        // Otherwise, I check if the operand is of type LONG
        } else if (operand.type().equals(Type.LONG)) {
            operand.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
        // Otherwise, I check if the operand is of type DOUBLE
        } else if (operand.type().equals(Type.DOUBLE)) {
            operand.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        operand.codegen(output);
        // I am checking the different types of the operand, and I am doing the necessary action
        // depending on the type
        // If the type is LONG, I do LNEG
        if (operand.type() == Type.LONG) {
            output.addNoArgInstruction(LNEG);
            // If the type is DOUBLE, I do DNEG
        } else if (operand.type() == Type.DOUBLE) {
            output.addNoArgInstruction(DNEG);
            // If the type is INT, I do INEG
        } else if (operand.type() == Type.INT) {
            output.addNoArgInstruction(INEG);
        }
    }
}

/**
 * The AST node for a post-decrement (--) expression.
 */
class JPostDecrementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a post-decrement expression.
     *
     * @param line    line in which the expression occurs in the source file.
     * @param operand the operand.
     */
    public JPostDecrementOp(int line, JExpression operand) {
        super(line, "-- (post)", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(operand instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line, "Operand to -- must have an LValue.");
            type = Type.ANY;
        } else {
            operand = (JExpression) operand.analyze(context);
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (operand instanceof JVariable) {
            // A local variable; otherwise analyze() would have replaced it with an explicit
            // field selection.
            int offset = ((LocalVariableDefn) ((JVariable) operand).iDefn()).offset();
            if (!isStatementExpression) {
                // Loading its original rvalue.
                operand.codegen(output);
            }
            output.addIINCInstruction(offset, -1);
        } else {
            ((JLhs) operand).codegenLoadLhsLvalue(output);
            ((JLhs) operand).codegenLoadLhsRvalue(output);
            if (!isStatementExpression) {
                // Loading its original rvalue.
                ((JLhs) operand).codegenDuplicateRvalue(output);
            }
            output.addNoArgInstruction(ICONST_1);
            output.addNoArgInstruction(ISUB);
            ((JLhs) operand).codegenStore(output);
        }
    }
}

/**
 * The AST node for pre-increment (++) expression.
 */
class JPreIncrementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a pre-increment expression.
     *
     * @param line    line in which the expression occurs in the source file.
     * @param operand the operand.
     */
    public JPreIncrementOp(int line, JExpression operand) {
        super(line, "++ (pre)", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        if (!(operand instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line, "Operand to ++ must have an LValue.");
            type = Type.ANY;
        } else {
            operand = (JExpression) operand.analyze(context);
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (operand instanceof JVariable) {
            // A local variable; otherwise analyze() would have replaced it with an explicit
            // field selection.
            int offset = ((LocalVariableDefn) ((JVariable) operand).iDefn()).offset();
            output.addIINCInstruction(offset, 1);
            if (!isStatementExpression) {
                // Loading its original rvalue.
                operand.codegen(output);
            }
        } else {
            ((JLhs) operand).codegenLoadLhsLvalue(output);
            ((JLhs) operand).codegenLoadLhsRvalue(output);
            output.addNoArgInstruction(ICONST_1);
            output.addNoArgInstruction(IADD);
            if (!isStatementExpression) {
                // Loading its original rvalue.
                ((JLhs) operand).codegenDuplicateRvalue(output);
            }
            ((JLhs) operand).codegenStore(output);
        }
    }
}

/**
 * The AST node for a unary plus (+) expression.
 */
class JUnaryPlusOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a unary plus expression.
     *
     * @param line    line in which the unary plus expression occurs in the source file.
     * @param operand the operand.
     */
    public JUnaryPlusOp(int line, JExpression operand) {
        super(line, "+", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // I thought that this is going to be the same with the JNegateOp above.
        operand = operand.analyze(context);
        //operand.type().mustMatchExpected(line(), Type.INT);
        //type = Type.INT;

        // I check what type the operand is and according to the result
        // I give the right type to the variable type.
        // First, I am checking for INT, and I give INT
        if (operand.type().equals(Type.INT)) {
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
            // Otherwise, I check for LONG and I give LONG
        } else if (operand.type().equals(Type.LONG)) {
            operand.type().mustMatchExpected(line(), Type.LONG);
            type = Type.LONG;
            // Otherwise, I check for DOUBLE, and I give DOUBLE
        } else if (operand.type().equals(Type.DOUBLE)) {
            operand.type().mustMatchExpected(line(), Type.DOUBLE);
            type = Type.DOUBLE;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I thought that this is going to be the same with the JNegateOp above
        operand.codegen(output);
        // The line below is not needed because after the previous
        // line, when I tested my code, I got an error saying "Unable to pop operand of
        // an empty stack," which I thought it would mean that the output is empty
        // which would mean that I could not call any methods on it (aka the
        // addNoArgInstruction(IADD); )
        //output.addNoArgInstruction(IADD);
    }
}

/**
 * The AST node for a unary complement (~) expression.
 */
class JComplementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a unary complement expression.
     *
     * @param line    line in which the unary complement expression occurs in the source file.
     * @param operand the operand.
     */
    public JComplementOp(int line, JExpression operand) {
        super(line, "~", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO'
        // from the LNOT option above
        operand = (JExpression) operand.analyze(context);
        operand.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }
    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // used the constant -1 and the IXOR operator to make the BNOT work
        operand.codegen(output);
        output.addNoArgInstruction(ICONST_M1);
        output.addNoArgInstruction(IXOR);

    }
}

/**
 * The AST node for post-increment (++) expression.
 */
class JPostIncrementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a post-increment expression.
     *
     * @param line    line in which the expression occurs in the source file.
     * @param operand the operand.
     */
    public JPostIncrementOp(int line, JExpression operand) {
        super(line, "++ (post)", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // I have copy-pasted the code from the JPostDecrementOp() method. I have only changed the --
        // in the error message to a ++
        if (!(operand instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line, "Operand to ++ must have an LValue.");
            type = Type.ANY;
        } else {
            operand = (JExpression) operand.analyze(context);
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I have also copied this part of the code from the JPostDecrementOp method above
        if (operand instanceof JVariable) {
            // A local variable; otherwise analyze() would have replaced it with an explicit
            // field selection.
            int offset = ((LocalVariableDefn) ((JVariable) operand).iDefn()).offset();
            if (!isStatementExpression) {
                // Loading its original rvalue.
                operand.codegen(output);
            }
            // because we are dealing with PostIncrement in this method I changed the constVal below
            // from -1 to 1.
            output.addIINCInstruction(offset, 1);
        } else {
            ((JLhs) operand).codegenLoadLhsLvalue(output);
            ((JLhs) operand).codegenLoadLhsRvalue(output);
            if (!isStatementExpression) {
                // Loading its original rvalue.
                ((JLhs) operand).codegenDuplicateRvalue(output);
            }
            output.addNoArgInstruction(ICONST_1);
            // The PostIncrement is about adding not subtracting, thus, I changed the ISUB to IADD
            output.addNoArgInstruction(IADD);
            ((JLhs) operand).codegenStore(output);
        }
    }
}

/**
 * The AST node for a pre-decrement (--) expression.
 */
class JPreDecrementOp extends JUnaryExpression {
    /**
     * Constructs an AST node for a pre-decrement expression.
     *
     * @param line    line in which the expression occurs in the source file.
     * @param operand the operand.
     */
    public JPreDecrementOp(int line, JExpression operand) {
        super(line, "-- (pre)", operand);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // I copied the following lines of code from the JPreIncrementOp method above, and I made a small
        // change in the error message
        if (!(operand instanceof JLhs)) {
            JAST.compilationUnit.reportSemanticError(line, "Operand to -- must have an LValue.");
            type = Type.ANY;
        } else {
            operand = (JExpression) operand.analyze(context);
            operand.type().mustMatchExpected(line(), Type.INT);
            type = Type.INT;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I copied the following lines of code from the JPreIncrementOp method above, and I made some small
        // changes which I will mentioned in some comments below
        if (operand instanceof JVariable) {
            // A local variable; otherwise analyze() would have replaced it with an explicit
            // field selection.
            int offset = ((LocalVariableDefn) ((JVariable) operand).iDefn()).offset();
            // Because we are working for PreDecrementOp we are looking to add a negative number
            // this I changed the constVal from 1 to -1
            output.addIINCInstruction(offset, -1);
            if (!isStatementExpression) {
                // Loading its original rvalue.
                operand.codegen(output);
            }
        } else {
            ((JLhs) operand).codegenLoadLhsLvalue(output);
            ((JLhs) operand).codegenLoadLhsRvalue(output);
            output.addNoArgInstruction(ICONST_1);
            // This is a JPreDecrementOp method, thus we need subtraction and not addition. I changed
            // IADD to ISUB.
            output.addNoArgInstruction(ISUB);
            if (!isStatementExpression) {
                // Loading its original rvalue.
                ((JLhs) operand).codegenDuplicateRvalue(output);
            }
            ((JLhs) operand).codegenStore(output);
        }
    }
}
