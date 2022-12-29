// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;

    // Update.
    private ArrayList<JStatement> update;

    // The body.
    private JStatement body;
    // add the boolean hasBreak and the breakLabel
    public boolean hasBreak;
    public String breakLabel;
    // add the boolean hasContinue and the continueLabel
    public boolean hasContinue;
    public String continueLabel;

    /**
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition,
                         ArrayList<JStatement> update, JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    /**
     * {@inheritDoc}
     */
    public JForStatement analyze(Context context) {
        // TODO
        // A reference for itself is being pushed in the stack as asked from the problem with Break
        JMember.enclosingStatement.push(this);
        // Create a new LocalContext with context being the parent
        LocalContext localContext = new LocalContext(context);
        // because init is an arraylist, I am adding a for loop
        // to go through each of the inits in the arraylist and
        // analyze it in the new context.
        if (init != null) {
            for (JStatement someinit : init) {
                someinit.analyze(localContext);
            }
        }
        // Now, I am checking if the condition is null and move on to the next part.
        // I am doing the same for the update.
        if (condition != null) {
            // analyze condition in the new context
            condition = (JExpression) condition.analyze(localContext);
            // make sure condition is of type Boolean
            condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        }
        if (update != null) {
            // update is an arraylist, so, I am doing what I did for init
            // to analyze update for the new context
            for (JStatement someupdate : update) {
                someupdate.analyze(localContext);
            }
        }
        // analyze the body of the for loop
        body = (JStatement) body.analyze(localContext);

        // Pop the reference to itself upon exit like asked
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // generate code for the arraylist init
        if (init != null) {
            for (JStatement someinit : init) {
                someinit.codegen(output);
            }
        }
        // I remember that I had treated before the for loop as a while
        // loop. I think this happened on the first project we had on the
        // first problem. Thus, I copied some of the following lines from
        // the JWhileStatement file.

        // create labels that will allow looping and one that is
        // for ending the loop
        String test = output.createLabel();
        String out = output.createLabel();
        // add the test label, which is for looping
        output.addLabel(test);
        // After going to office hours, create the continue label
        if (hasContinue == true) {
            continueLabel = output.createLabel();
            //output.addLabel(continueLabel);
        }
        // leave the for loop when the condition is false
        if (condition != null) {
            condition.codegen(output, out, false);
        }
        // check if there is a break, initialize the break label with
        // the out label, because when we encounter break, we are getting
        // out of a loop
        if (hasBreak == true) {
            breakLabel = out;
            //output.addLabel(breakLabel);
        }
        // if the condition is not false get the body
        body.codegen(output);
        // add the continue label
        if (hasContinue == true) {
            output.addLabel(continueLabel);
        }
        // time to update to the next index of the arraylist
        if (update != null) {
            for (JStatement someupdate : update) {
                someupdate.codegen(output);
            }
        }
        // go back to the beginning of the loop
        output.addBranchInstruction(GOTO, test);
        // this is the label when the loop is done and we want out
        output.addLabel(out);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
