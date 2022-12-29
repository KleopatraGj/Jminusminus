// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.Stack;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a do-statement.
 */
public class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;

    // Declare the two instance variables boolean hasBreak
    // and String breakLabel. This for the problem dealing with break
    public boolean hasBreak;
    public String breakLabel;
    // Declare the two instance variables boolean hasContinue
    // and String continueLabel. This for the problem dealing with continue
    public boolean hasContinue;
    public String continueLabel;
    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // TODO
        // A reference for itself is being pushed in the stack as asked from the problem with Break
        JMember.enclosingStatement.push(this);

        // Analyze the condition
        condition = (JExpression) condition.analyze(context);
        // Make sure that the condition is of type BOOLEAN
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        // Analyze the body
        body = (JStatement) body.analyze(context);

        // Pop the reference to itself upon exit like asked in the break problem
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I am creating the topLabel as illustrated in the book p. 179
        String topLabel = output.createLabel();
        // I am placing the label as the first instruction
        output.addLabel(topLabel);
        // If there is a continue, create a continue label
        if (hasContinue == true) {
            // make a label for continue
            continueLabel = output.createLabel();
        }
        // check if there is a break. If there is, then create the
        // break label, and add it
        if (hasBreak == true) {
            breakLabel = output.createLabel();
            output.addLabel(breakLabel);
        }
        // generate code for the body
        body.codegen(output);
        // I added the continue label here. I added this here,
        // because we want the body of the do-while loop to happen
        // first before we continue looping, because we need our
        // variables to update in case they are being updated.
        if (hasContinue == true) {
            output.addLabel(continueLabel);
        }
        // Going to the topLabel when the condition is True
        // technically generating code for the condition
        condition.codegen(output, topLabel, true);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
