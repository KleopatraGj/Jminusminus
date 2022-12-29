// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a conditional expression.
 */
class JConditionalExpression extends JExpression {
    // Test expression.
    private JExpression condition;

    // Then part.
    private JExpression thenPart;

    // Else part.
    private JExpression elsePart;

    /**
     * Constructs an AST node for a conditional expression.
     *
     * @param line      line in which the conditional expression occurs in the source file.
     * @param condition test expression.
     * @param thenPart  then part.
     * @param elsePart  else part.
     */
    public JConditionalExpression(int line, JExpression condition, JExpression thenPart,
                                  JExpression elsePart) {
        super(line);
        this.condition = condition;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        // Analyze the condition and make sure that it's a boolean
        // we call the method analyze() with the argument context
        // in order to analyze it
        condition = (JExpression) condition.analyze(context);
        // Then we are making sure that the condition is of type BOOLEAN
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        // I am doing the same analysis for the thenPart and the elsePArt
        thenPart = (JExpression) thenPart.analyze(context);
        elsePart = (JExpression) elsePart.analyze(context);
        // make sure that the thenPart and elsePart are the same type
        elsePart.type().mustMatchExpected(line ,thenPart.type());
        // After making sure the thenPart and the elsePart have the same type
        // I am setting the type to the type of the thenPart
        type = thenPart.type();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I got some of the following information by observing codegen in the JIfStatement.
        // In fact for the conditional expression there always a thenPart and an elsePart, thus
        // I don't need to check if the elsePart is null or not. So I will no be including any if statements
        // to check for that
        // Create the elseLabel, and the endLabel like in the JIfStatement
        String elseLabel = output.createLabel();
        String endLabel = output.createLabel();
        // generate code for the condition
        condition.codegen(output, elseLabel, false);
        // generate code for the then part
        thenPart.codegen(output);
        // if the then part has happened just jump to the end
        // don't allow the else part to happen
        output.addBranchInstruction(GOTO, endLabel);
        // otherwise add the elseLabel
        output.addLabel(elseLabel);
        // generate code for the else part
        elsePart.codegen(output);
        // add the endLabels which leads to leaving the
        // conditional expression
        output.addLabel(endLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JConditionalExpression:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("ThenPart", e2);
        thenPart.toJSON(e2);
        JSONElement e3 = new JSONElement();
        e.addChild("ElsePart", e3);
        elsePart.toJSON(e3);
    }
}
