// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * This abstract base class is the AST node for a binary expression --- an expression with a binary
 * operator and two operands: lhs and rhs.
 */
abstract class JBinaryExpression extends JExpression {
    /**
     * The binary operator.
     */
    protected String operator;

    /**
     * The lhs operand.
     */
    protected JExpression lhs;

    /**
     * The rhs operand.
     */
    protected JExpression rhs;

    /**
     * Constructs an AST node for a binary expression.
     *
     * @param line     line in which the binary expression occurs in the source file.
     * @param operator the binary operator.
     * @param lhs      the lhs operand.
     * @param rhs      the rhs operand.
     */
    protected JBinaryExpression(int line, String operator, JExpression lhs, JExpression rhs) {
        super(line);
        this.operator = operator;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBinaryExpression:" + line, e);
        e.addAttribute("operator", operator);
        e.addAttribute("type", type == null ? "" : type.toString());
        JSONElement e1 = new JSONElement();
        e.addChild("Operand1", e1);
        lhs.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Operand2", e2);
        rhs.toJSON(e2);
    }
}

/**
 * The AST node for a multiplication (*) expression.
 */
class JMultiplyOp extends JBinaryExpression {
    /**
     * Constructs an AST for a multiplication expression.
     *
     * @param line line in which the multiplication expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JMultiplyOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "*", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        // I copied parts of the code below from the JPlusOp. I just didn't
        // include the case of having a String.
        // Check for the different Types of lhs and rhs.
        // Those can be INT, LONG, DOUBLE
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            type = Type.INT;
        // I added the type LONG
        } else if (lhs.type() == Type.LONG && rhs.type == Type.LONG) {
            type = Type.LONG;
        // I added the type DOUBLE
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            type = Type.DOUBLE;
        } else {
            type = Type.ANY;
            // I changed the + in the message to a *
            JAST.compilationUnit.reportSemanticError(line(), "Invalid operand types for *");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        // I added an if statement to check what kind of type are the
        // lhs and the rhs. According to the type, I do the proper multiplication
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            output.addNoArgInstruction(IMUL);
        } else if (lhs.type() == Type.LONG && rhs.type() == Type.LONG) {
            output.addNoArgInstruction(LMUL);
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            output.addNoArgInstruction(DMUL);
        }
    }
}

/**
 * The AST node for a plus (+) expression. In j--, as in Java, + is overloaded to denote addition
 * for numbers and concatenation for Strings.
 */
class JPlusOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an addition expression.
     *
     * @param line line in which the addition expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JPlusOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "+", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        if (lhs.type() == Type.STRING || rhs.type() == Type.STRING) {
            return (new JStringConcatenationOp(line, lhs, rhs)).analyze(context);
        } else if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            type = Type.INT;
            // I added the type LONG
        } else if (lhs.type() == Type.LONG && rhs.type == Type.LONG) {
            type = Type.LONG;
            // I added the type DOUBLE
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            type = Type.DOUBLE;
        } else {
            type = Type.ANY;
            JAST.compilationUnit.reportSemanticError(line(), "Invalid operand types for +");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        // I added an if statement to check what kind of type are the
        // lhs and the rhs. According to the type, I do the proper addition
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            output.addNoArgInstruction(IADD);
        } else if (lhs.type() == Type.LONG && rhs.type() == Type.LONG) {
            output.addNoArgInstruction(LADD);
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            output.addNoArgInstruction(DADD);
        }
    }
}

/**
 * The AST node for a subtraction (-) expression.
 */
class JSubtractOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a subtraction expression.
     *
     * @param line line in which the subtraction expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JSubtractOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "-", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        //lhs.type().mustMatchExpected(line(), Type.INT);
        //rhs.type().mustMatchExpected(line(), Type.INT);
        //type = Type.INT;

        // check for the different Types of lhs and rhs.
        // Those can be INT, LONG, DOUBLE
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            type = Type.INT;
            // I added the type LONG
        } else if (lhs.type() == Type.LONG && rhs.type == Type.LONG) {
            type = Type.LONG;
            // I added the type DOUBLE
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            type = Type.DOUBLE;
        } else {
            type = Type.ANY;
            JAST.compilationUnit.reportSemanticError(line(), "Invalid operand types for -");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        //output.addNoArgInstruction(ISUB);
        // I added an if statement to check what kind of type are the
        // lhs and the rhs. According to the type, I do the proper multiplication
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            output.addNoArgInstruction(ISUB);
        } else if (lhs.type() == Type.LONG && rhs.type() == Type.LONG) {
            output.addNoArgInstruction(LSUB);
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            output.addNoArgInstruction(DSUB);
        }
    }
}

/**
 * The AST node for a division (/) expression.
 */
class JDivideOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a division expression.
     *
     * @param line line in which the division expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JDivideOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "/", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // This was given on the lecture slides of Compilation
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        //lhs.type().mustMatchExpected(line(), Type.INT);
        //rhs.type().mustMatchExpected(line(), Type.INT);
        //type = Type.INT;

        // check for the different Types of lhs and rhs.
        // Those can be INT, LONG, DOUBLE
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            type = Type.INT;
            // I added the type LONG
        } else if (lhs.type() == Type.LONG && rhs.type == Type.LONG) {
            type = Type.LONG;
            // I added the type DOUBLE
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            type = Type.DOUBLE;
        } else {
            type = Type.ANY;
            JAST.compilationUnit.reportSemanticError(line(), "Invalid operand types for /");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // This was given on the lecture slides of Compilation
        lhs.codegen(output);
        rhs.codegen(output);
        //output.addNoArgInstruction(IDIV);
        // I added an if statement to check what kind of type are the
        // lhs and the rhs. According to the type, I do the proper multiplication
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            output.addNoArgInstruction(IDIV);
        } else if (lhs.type() == Type.LONG && rhs.type() == Type.LONG) {
            output.addNoArgInstruction(LDIV);
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            output.addNoArgInstruction(DDIV);
        }
    }
}

/**
 * The AST node for a remainder (%) expression.
 */
class JRemainderOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a remainder expression.
     *
     * @param line line in which the division expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JRemainderOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "%", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // I copied this part from the JDivideOp for the analyze() method
        // I thought that since the division is the same with the multiplication
        // and remainder is also similar to both multiplication and division, then
        // the analyze() method would be the same
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        //lhs.type().mustMatchExpected(line(), Type.INT);
        //rhs.type().mustMatchExpected(line(), Type.INT);
        //type = Type.INT;

        // check for the different Types of lhs and rhs.
        // Those can be INT, LONG, DOUBLE
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            type = Type.INT;
            // I added the type LONG
        } else if (lhs.type() == Type.LONG && rhs.type == Type.LONG) {
            type = Type.LONG;
            // I added the type DOUBLE
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            type = Type.DOUBLE;
        } else {
            type = Type.ANY;
            JAST.compilationUnit.reportSemanticError(line(), "Invalid operand types for %");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I copied this part from the JDivideOp for the codegen() method
        // I thought that since the division is the same with the multiplication
        // and remainder is also similar to both multiplication and division, then
        // the codegen() method would be the same
        lhs.codegen(output);
        rhs.codegen(output);
        //output.addNoArgInstruction(IREM);
        // I added an if statement to check what kind of type are the
        // lhs and the rhs. According to the type, I do the proper multiplication
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            output.addNoArgInstruction(IREM);
        } else if (lhs.type() == Type.LONG && rhs.type() == Type.LONG) {
            output.addNoArgInstruction(LREM);
        } else if (lhs.type() == Type.DOUBLE && rhs.type() == Type.DOUBLE) {
            output.addNoArgInstruction(DREM);
        }
    }
}

/**
 * The AST node for an inclusive or (|) expression.
 */
class JOrOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an inclusive or expression.
     *
     * @param line line in which the inclusive or expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JOrOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "|", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // This was implemented similarly with the division and the remainder above
        // Make sure that you are working with doubles
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // Use of the IOR operator
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IOR);
    }
}

/**
 * The AST node for an exclusive or (^) expression.
 */
class JXorOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an exclusive or expression.
     *
     * @param line line in which the exclusive or expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JXorOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "^", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // Similar implementation with the JOrOp method above both
        // for the analyze() and the codegen()
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // Use of the IXOR operator
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IXOR);
    }
}

/**
 * The AST node for an and (&amp;) expression.
 */
class JAndOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an and expression.
     *
     * @param line line in which the and expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JAndOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "&", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // Similar implementation with the JXorOp and JOrOp methods
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // Use of the IAND operator
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IAND);
    }
}

/**
 * The AST node for an arithmetic left shift (&lt;&lt;) expression.
 */
class JALeftShiftOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an arithmetic left shift expression.
     *
     * @param line line in which the arithmetic left shift expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JALeftShiftOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "<<", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // Similar implementation with the previous methods
        // Making sure that I am working with integers
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // Use of the ISHL operator for left shifting
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(ISHL);
    }
}

/**
 * The AST node for an arithmetic right shift (&rt;&rt;) expression.
 */
class JARightShiftOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an arithmetic right shift expression.
     *
     * @param line line in which the arithmetic right shift expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JARightShiftOp(int line, JExpression lhs, JExpression rhs) {
        super(line, ">>", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // Another implementation like most of the above
        // making sure that I am working with integers
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // use of the ISHR operator for right shifting
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(ISHR);
    }
}

/**
 * The AST node for a logical right shift (&rt;&rt;&rt;) expression.
 */
class JLRightShiftOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a logical right shift expression.
     *
     * @param line line in which the logical right shift expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JLRightShiftOp(int line, JExpression lhs, JExpression rhs) {
        super(line, ">>>", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // Another implementation like the ones above
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // use of the IUSHR operator for the logical right shifting
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IUSHR);
    }
}
