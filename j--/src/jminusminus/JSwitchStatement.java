// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a switch-statement.
 */
public class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> stmtGroup;
    // add the boolean hasBreak and the breakLabel
    public boolean hasBreak;
    public String breakLabel;
    // This will allow me to know if I have a default label or not
    private boolean flag;
    // These are some variables given in the writeup
    int hi;
    int lo;
    int nLabels;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line      line in which the switch-statement occurs in the source file.
     * @param condition test expression.
     * @param stmtGroup list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition,
                            ArrayList<SwitchStatementGroup> stmtGroup) {
        super(line);
        this.condition = condition;
        this.stmtGroup = stmtGroup;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // TODO
        // A reference for itself is being pushed in the stack as asked from the problem with Break
        JMember.enclosingStatement.push(this);
        // Analyze the condition and make sure it's of type INT
        condition = (JExpression) condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.INT);
        // initialize the variable nLabels which is going to help
        // with counting how many labels there are. This part will also
        // help with the implementation of the snippet of code the
        //professor has given us to use.
        nLabels = 0;
        // There can be multiple cases, which means that
        // we may have to deal with multiple case labels. In this
        // case we probably need to keep track of the labels by
        // using an arraylist
        ArrayList<JLiteralInt> caseLabels = new ArrayList<JLiteralInt>();
        // For each switch statement group
        for (SwitchStatementGroup group : stmtGroup) {
            // Make a new LocalContext with context as the parent
            // and analyze the statements in each case group
            LocalContext newcontext = new LocalContext(context);
            // iterate through the arraylist JExpression of the switch labels to analyze the labels
            // for each case
            for (int i = 0; i < group.switchLabels.size(); i++) {
                // get the first label
                JExpression label = group.switchLabels.get(i);
                // if the label is not null
                if (label != null ){
                    // count the labels
                    nLabels++;
                    // make sure it's an integer literal
                    label = (JLiteralInt) label.analyze(newcontext);
                    // add that literal to the caseLabels arraylist
                    caseLabels.add((JLiteralInt) label);
                } else {
                    // in case the label is null, then that means
                    // that we are dealing with the deafult label
                    // Thus, the boolean flag gets the value of true
                    flag = true;
                }
            }
            // analyze the block of each case label
            for (int i = 0; i < group.block.size(); i++) {
                JStatement switblock = (JStatement) group.block.get(i).analyze(newcontext);
                // return back the analysis to the array, took this from the JTryStatement analysis
                // implementation as it was shown by the professor
                group.block.set(i, switblock);
            }
        }
        // create an Array list that will hold the case labels as integers in the array
        int[] caseInts = new int[caseLabels.size()];
        for (int i = 0; i < caseLabels.size(); i++) {
            caseInts[i] = caseLabels.get(i).toInt();
        }
        // Now sort the array with the case labels as integers
        Arrays.sort(caseInts);
        // Since the array is sorted now, the first element of the array is the lowest case value
        lo = caseInts[0];
        // the last element of the array is the highest case value.
        hi = caseInts[caseInts.length - 1];
        // The hi and lo variables above are needed for the given code snippet to work
        // Pop the reference to itself upon exit like asked
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // generate code for the condition
        condition.codegen(output);
        // hi and lo have been computed in the analysis as well as the nLabels variable
        long tableSpaceCost = 5 + hi - lo;
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * nLabels ;
        long lookupTimeCost = nLabels ;
        int opcode = nLabels > 0 && ( tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost ) ?
                TABLESWITCH : LOOKUPSWITCH ;
        // Find out if we have to do tableswicth or lookupswitch
        // In the case of a table switch
        if (opcode == TABLESWITCH) {
            // Following the example from the genTableSwitch, I need to create an arraylist that is of type
            // String and that will contain all the labels
            ArrayList<String> labels = new ArrayList<String>();
            // for each switch statement group
            for (SwitchStatementGroup group : stmtGroup) {
                // Go through each one of the labels
                for (int i = 0; i < group.switchLabels.size(); i++) {
                    // if the label from the switchLabels arraylist is not null
                    // then create one and add it to the labels arraylist
                    if (group.switchLabels.get(i) != null) {
                        String label = output.createLabel();
                        labels.add(label);
                    }
                }
            }
            // Create a default label that I will give as an argument to the addTABLESWITCHInstruction below
            String defaultLabel = output.createLabel();
            // if there is a break, then create its label
            if (hasBreak == true) {
                breakLabel = output.createLabel();
            }
            // This is the tableswitch instruction that I need to use for the case of a table switch
            output.addTABLESWITCHInstruction(defaultLabel, lo, hi, labels);
            // go through the switch statement groups to generate the appropriate code
            // I need the variable below because, I am getting an unable to resolve
            // jump labels.
            int j = 0;
            for (SwitchStatementGroup group: stmtGroup) {
                // for each case label
                for (int i = 0; i < group.switchLabels.size(); i++) {
                    // if the label is null, then I add a default label, otherwise
                    // I add the case labels, I saved in the labels arraylist I created earlier
                    if (group.switchLabels.get(i) == null) {
                        output.addLabel(defaultLabel);
                    } else {
                        output.addLabel(labels.get(j));
                        j++;
                    }
                }
                // generate code for each case block
                for (int i = 0; i < group.block.size(); i++) {
                    group.block.get(i).codegen(output);
                }
            }
            // If the flag is false, which means there is no default label
            // then just add one default label anyway
            if (!flag) {
                output.addLabel(defaultLabel);
            }
            // if there is a break just add the break label
            if (hasBreak == true) {
                output.addLabel(breakLabel);
            }
         // This is the case we are dealing with the lookupswitch
        } else if (opcode == LOOKUPSWITCH){
            // Following the example from the genLookupSwitch, I need to create a tree map that will
            // contain a pair of integer and string
            TreeMap<Integer, String> matchLabelPairs = new TreeMap<Integer, String>();
            // for each switch statement group
            for (SwitchStatementGroup group : stmtGroup) {
                // for each label in the switchLabels list
                for (int i = 0; i < group.switchLabels.size(); i++) {
                    // if the switchLabels list is not null
                    if (group.switchLabels.get(i) != null) {
                        // take the expression of the label
                        JExpression caseLabel = group.switchLabels.get(i);
                        // cast it to a JLiteralInt
                        caseLabel = (JLiteralInt) caseLabel;
                        // make the JLiteralInt to an int
                        int caselabel = ((JLiteralInt) caseLabel).toInt();
                        // create the case label
                        String label = output.createLabel();
                        // add the key literal and the string label in te tree map
                        matchLabelPairs.put(caselabel, label);
                    }
                }
            }
            // Create a default label that I will give as an argument to the addLOOKUPSWITCHInstruction below
            String defaultLabel = output.createLabel();
            // if there is a break, create the break label
            if (hasBreak == true) {
                breakLabel = output.createLabel();
            }
            // This is the lookupswitch instruction that I need to use for the case of the look up switch
            output.addLOOKUPSWITCHInstruction(defaultLabel, matchLabelPairs.size(), matchLabelPairs);
            // for each switch statement group
            for (SwitchStatementGroup group: stmtGroup) {
                // for each label in the switchLabels list
                for (int i = 0; i < group.switchLabels.size(); i++) {
                    // if we encounter an null label then add the default label
                    if (group.switchLabels.get(i) == null) {
                        output.addLabel(defaultLabel);
                    } else {
                        // otherwise get the expression of the case label
                        JExpression caseLabel = group.switchLabels.get(i);
                        // cast it to the JLiteralInt
                        caseLabel = (JLiteralInt) caseLabel;
                        // get the int of that JLiteralInt
                        int caselabel = ((JLiteralInt) caseLabel).toInt();
                        // add the label that we get from the tree map by using the
                        // integer we got above
                        output.addLabel(matchLabelPairs.get(caselabel));
                    }
                }
                // generate code for each case block
                for (int i = 0; i < group.block.size(); i++) {
                    group.block.get(i).codegen(output);
                }
            }
            // if there is no default label, then add one
            if (!flag) {
                output.addLabel(defaultLabel);
            }
            // and if there is a break, also add it the break label
            if (hasBreak == true) {
                output.addLabel(breakLabel);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : stmtGroup) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch statement group consists of case labels and a block of statements.
 */
class SwitchStatementGroup {
    // Case labels.
    public ArrayList<JExpression> switchLabels;

    // Block of statements.
    public ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels case labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}
