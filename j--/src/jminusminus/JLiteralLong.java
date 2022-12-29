// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a long literal.
 */
class JLiteralLong extends JExpression {
    // String representation of the literal.
    private String text;

    /**
     * Constructs an AST node for a long literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public JLiteralLong(int line, String text) {
        super(line);
        this.text = text;
    }

    // I saw the following method in the JLiteralInt.java file,
    // and it seems to be necessary in the case for the codegen() method.
    // I have changed the method I got from JLiteralInt from toInt() to toLong()
    /**
     * Returns the literal as a long.
     *
     * @return the literal as a long.
     */
    public long toLong() {
        // According to the professor Long.parseLong doesn't handle the terminating L, and l
        // very well, thus, I had to find a way to get rid of it. I am accomplishing that by
        // using an if statement that checks if the string text contains L or l by using the
        // method contains() which takes one argument.
        // Depending on if I find L or l, I am replacing L or l by nothing. The replacement was done
        // with the help of the method replace() which takes two arguments. The first argument
        // is the letter than needs to be replaced, and the second argument contains the letter that
        // will replace the old letter. In this case, as I already mentioned is nothing
        if (text.contains("L")) {
            text = text.replace("L", "");
        } else if (text.contains("l")) {
            text = text.replace("l", "");
        }
        return Long.parseLong(text);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // I added this line here which is very similar with
        // the JLiteralInt.java file
        type = Type.LONG;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I added the following part. the constants for long literals seem to be only 2.
        // Thus, we only have two cases, and the default case which I copied from the JLiteralInt file.
        long i = toLong();
        // since I have taken away the ending "L" and "l", I am not including it in the condition below
        if (i == 0) {
            output.addNoArgInstruction(LCONST_0);
        } else if (i == 1) {
            output.addNoArgInstruction(LCONST_1);
        } else {
            // Get the LDCInstruction
            output.addLDCInstruction(i);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JLiteralLong:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", text);
    }
}
