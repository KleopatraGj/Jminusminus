// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a while-statement.
 */
class JWhileStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // Body.
    private JStatement body;

    // add the boolean hasBreak and the breakLabel
    public boolean hasBreak;
    public String breakLabel;

    // Add the boolean for the hasContinue and the continueLabel
    public boolean hasContinue;
    public String continueLabel;
    /**
     * Constructs an AST node for a while-statement.
     *
     * @param line      line in which the while-statement occurs in the source file.
     * @param condition test expression.
     * @param body      the body.
     */
    public JWhileStatement(int line, JExpression condition, JStatement body) {
        super(line);
        this.condition = condition;
        this.body = body;
    }

    /**
     * {@inheritDoc}
     */
    public JWhileStatement analyze(Context context) {
        // A reference for itself is being pushed in the stack as asked from the problem with Break
        JMember.enclosingStatement.push(this);

        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);
        // Pop the reference to itself upon exit like asked
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String test = output.createLabel();
        String out = output.createLabel();

        output.addLabel(test);
        // check if there is a Continue, create a continue label if the
        // condition is true
        if (hasContinue == true) {
            continueLabel = output.createLabel();
            //output.addLabel(continueLabel); <-- Not needed
        }

        // check if there is a break, initialize the break label with
        // the out label, because when we encounter break, we are getting
        // out of a loop
        if (hasBreak == true) {
            breakLabel = out;
            //output.addLabel(breakLabel); <-- Not needed
        }
        condition.codegen(output, out, false);
        // I add a label for the continue
        if (hasContinue == true) {
            output.addLabel(continueLabel);
        }
        body.codegen(output);
        output.addBranchInstruction(GOTO, test);
        output.addLabel(out);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JWhileStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Body", e2);
        body.toJSON(e2);
    }
}
