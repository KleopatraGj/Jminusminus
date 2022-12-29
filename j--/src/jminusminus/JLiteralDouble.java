// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a double literal.
 */
class JLiteralDouble extends JExpression {
    // String representation of the literal.
    private String text;

    /**
     * Constructs an AST node for a double literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public JLiteralDouble(int line, String text) {
        super(line);
        this.text = text;
    }
    // I saw the following method in the JLiteralInt.java file,
    // and it seems to be necessary in the case for the codegen() method.
    // I have changed the method I got from JLiteralInt from toInt() to toDouble()
    /**
     * Returns the literal as a double.
     *
     * @return the literal as a double.
     */
    public double toDouble() {
        return Double.parseDouble(text);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // I added this line here which is very similar with
        // the JLiteralInt.java file
        type = Type.DOUBLE;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I added the following part. The constants for double literals seem to be only 2.
        // Thus, we only have two cases, and the default case which I copied from the JLiteralInt file
        double i = toDouble();
        if (i == 0.0) {
            output.addNoArgInstruction(DCONST_0);
        } else if (i == 1.0) {
            output.addNoArgInstruction(DCONST_1);
        } else {
            output.addLDCInstruction(i);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JLiteralDouble:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", text);
    }
}
