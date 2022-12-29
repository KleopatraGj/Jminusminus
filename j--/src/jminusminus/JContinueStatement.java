// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a continue-statement.
 */
public class JContinueStatement extends JStatement {
    // declare an instance variable JStatement for enclosingStatement
    JStatement enclosingStatement;

    /**
     * Constructs an AST node for a continue-statement.
     *
     * @param line line in which the continue-statement occurs in the source file.
     */
    public JContinueStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // TODO
        // set the instance variable enclosingStatement to what the value on top of the stack is
        enclosingStatement = JMember.enclosingStatement.peek();
        // check what kind of instance is the enclosingStatement.
        // Is the enclosingStatement an instance of JForStatement?
        if (enclosingStatement instanceof JForStatement) {
            // cast the variable enclosingStatement to the JForStatement
            JForStatement forStmt = (JForStatement) enclosingStatement;
            // Get access to the boolean variable hasContinue in the JForStatement
            // and set it to true
            forStmt.hasContinue = true;
            // Is the enclosingStatement an instance of JWhileStatement?
        } else if (enclosingStatement instanceof JWhileStatement) {
            // cast the variable enclosingStatement to the JWhileStatement
            JWhileStatement whileStmt = (JWhileStatement) enclosingStatement;
            // Get access to the boolean variable hasContinue in the JWhileStatement
            // and set it to true
            whileStmt.hasContinue = true;
            // Is the enclosingStatement an instance of JDoStatement?
        } else if (enclosingStatement instanceof JDoStatement) {
            // cast the variable enclosingStatement to the JDoStatement
            JDoStatement doStmt = (JDoStatement) enclosingStatement;
            // Get access to the boolean variable hasContinue in the JDoStatement
            // and set it to true
            doStmt.hasContinue = true;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // check what kind of instance is the enclosingStatement.
        // Is the enclosingStatement an instance of JForStatement?
        if (enclosingStatement instanceof JForStatement) {
            // cast the variable enclosingStatement to the JForStatement
            JForStatement forLabel = (JForStatement) enclosingStatement;
            // access the continueLabel in the JForStatement, and add an unconditional jump to it
            output.addBranchInstruction(GOTO, forLabel.continueLabel);
            // Is the enclosingStatement an instance of JWhileStatement?
        } else if (enclosingStatement instanceof JWhileStatement) {
            // cast the variable enclosingStatement to the JWhileStatement
            JWhileStatement whileLabel = (JWhileStatement) enclosingStatement;
            // access the continueLabel in the JWhileStatement, and add an unconditional jump to it
            output.addBranchInstruction(GOTO, whileLabel.continueLabel);
            // Is the enclosingStatement an instance of JDoStatement?
        } else if (enclosingStatement instanceof JDoStatement) {
            // cast the variable enclosingStatement to the JDoStatement
            JDoStatement doLabel = (JDoStatement) enclosingStatement;
            // access the continueLabel in the JDoStatement, and add an unconditional jump to it
            output.addBranchInstruction(GOTO, doLabel.continueLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JContinueStatement:" + line, e);
    }
}
