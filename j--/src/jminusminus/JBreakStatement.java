// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.Objects;

import static jminusminus.CLConstants.*;

/**
 * An AST node for a break-statement.
 */
public class JBreakStatement extends JStatement {

    // declare an instance variable JStatement for enclosingStatement.
    JStatement enclosingStatement;

    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */
    public JBreakStatement(int line) {
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
            // I got help from Ksenia, she in fact told me that I need to cast the hasBreak to the
            // right form and I will be able to get access to the hasBreak and the breakLabel. From a
            // piazza post.
            // cast the variable enclosingStatement to the JForStatement
            JForStatement forStmt = (JForStatement) enclosingStatement;
            // Get access to the boolean variable hasBreak in the JForStatement
            // and set it to true
            forStmt.hasBreak = true;
            // Is the enclosingStatement an instance of JWhileStatement?
        } else if (enclosingStatement instanceof JWhileStatement) {
            // cast the variable enclosingStatement to the JWhileStatement
            JWhileStatement whileStmt = (JWhileStatement) enclosingStatement;
            // Get access to the boolean variable hasBreak in the JWhileStatement
            // and set it to true
            whileStmt.hasBreak = true;
            // Is the enclosingStatement an instance of JDoStatement?
        } else if (enclosingStatement instanceof JDoStatement) {
            // cast the variable enclosingStatement to the JDoStatement
            JDoStatement doStmt = (JDoStatement) enclosingStatement;
            // Get access to the boolean variable hasBreak in the JDoStatement
            // and set it to true
            doStmt.hasBreak = true;
        // Is the enclosingStatement an instance of JSwitchStatement?
        } else if (enclosingStatement instanceof JSwitchStatement) {
            // cast the variable enclosingStatement to the JSwitchStatement
            JSwitchStatement switchStmt = (JSwitchStatement) enclosingStatement;
            // Get access to the boolean variable hasBreak in the JSwitchStatement
            // and set it to true
            switchStmt.hasBreak = true;
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
            // access the breakLabel in the JForStatement, and add an unconditional jump to it
            output.addBranchInstruction(GOTO, forLabel.breakLabel);
            // Is the enclosingStatement an instance of JWhileStatement?
        } else if (enclosingStatement instanceof JWhileStatement) {
            // cast the variable enclosingStatement to the JWhileStatement
            JWhileStatement whileLabel = (JWhileStatement) enclosingStatement;
            // access the breakLabel in the JWhileStatement, and add an unconditional jump to it
            output.addBranchInstruction(GOTO, whileLabel.breakLabel);
            // Is the enclosingStatement an instance of JDoStatement?
        } else if (enclosingStatement instanceof JDoStatement) {
            // cast the variable enclosingStatement to the JDoStatement
            JDoStatement doLabel = (JDoStatement) enclosingStatement;
            // access the breakLabel in the JDoStatement, and add an unconditional jump to it
            output.addBranchInstruction(GOTO, doLabel.breakLabel);
        // Is the enclosingStatement an instance of JSwitchStatement?
        } else if (enclosingStatement instanceof JSwitchStatement) {
            // cast the variable enclosingStatement to the JSwitchStatement
            JSwitchStatement switchLabel = (JSwitchStatement) enclosingStatement;
            // access the breakLabel in the JSwitchStatement, and add an unconditional jump to it
            //System.out.println(switchLabel.breakLabel);
            output.addBranchInstruction(GOTO, switchLabel.breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}
