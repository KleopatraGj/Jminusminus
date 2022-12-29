// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a try-catch-finally statement.
 */
class JTryStatement extends JStatement {
    // The try block.
    private JBlock tryBlock;

    // The catch parameters.
    private ArrayList<JFormalParameter> parameters;

    // The catch blocks.
    private ArrayList<JBlock> catchBlocks;

    // The finally block.
    private JBlock finallyBlock;
    // the offset is needed in the codegen method.
    private int offset;

    /**
     * Constructs an AST node for a try-statement.
     *
     * @param line         line in which the while-statement occurs in the source file.
     * @param tryBlock     the try block.
     * @param parameters   the catch parameters.
     * @param catchBlocks  the catch blocks.
     * @param finallyBlock the finally block.
     */
    public JTryStatement(int line, JBlock tryBlock, ArrayList<JFormalParameter> parameters,
                         ArrayList<JBlock> catchBlocks, JBlock finallyBlock) {
        super(line);
        this.tryBlock = tryBlock;
        this.parameters = parameters;
        this.catchBlocks = catchBlocks;
        this.finallyBlock = finallyBlock;
    }

    /**
     * {@inheritDoc}
     */
    public JTryStatement analyze(Context context) {
        // TODO
        // analyze the try block
        tryBlock = (JBlock) tryBlock.analyze(context);
        // For every parameter that is found in the parameters arraylist
        for (int i=0; i < parameters.size(); i++) {
            // Create a new LocalContext with parent the context that will help in
            // the analysis of the catchBlcok
            LocalContext newcontext = new LocalContext(context);
            // get the parameter of the first catchblock
            JFormalParameter catchParam = parameters.get(i);
            // The catch parameter mas be declared in the new context
            // We are calling the setType to set the parameter in the new context
            // after we resolve its type
            catchParam.setType(catchParam.type().resolve(newcontext));
            // This part was taken from the JMethodDeclaration file
            LocalVariableDefn defn = new LocalVariableDefn(catchParam.type(), newcontext.nextOffset());
            // initialize the defn
            defn.initialize();
            // add the catch parameter to the new context by using the addEntry method
            newcontext.addEntry(catchParam.line(), catchParam.name(), defn);
            // analyze every catch block for every parameter
            // I am creating the new variable
            JBlock catchblock;
            // I am analyzing
            catchblock = (JBlock) catchBlocks.get(i).analyze(newcontext);
            // This is needed  because we need to modify the array of the catchBlocks
            // because we need it to contain the analyzed catchblocks
            catchBlocks.set(i, catchblock);
        }
        // analyze the optional finally block
        if (finallyBlock != null) {
            // Create an new LocalContext
            LocalContext newfinally = new LocalContext(context);
            // analyze the finally block in the new context
            finallyBlock.analyze(newfinally);
            // The offset is needed for the code generation
            offset = newfinally.nextOffset();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // Create the start try label
        String startTry = output.createLabel();
        // Create the end try label
        String endTry = output.createLabel();
        // Create the end finally label
        String endFinally = output.createLabel();
        // Create the start finally label
        String startFinally = output.createLabel();
        // Create the end catch label
        String endCatch = output.createLabel();
        // Create the startFinallyPlusOne label herem
        String startFinallyPlusOne = output.createLabel();
        // Add a start try label
        output.addLabel(startTry);
        // generate code for the tryBlock
        tryBlock.codegen(output);
        // generate code for the optional finally block
        if (finallyBlock != null) {
            finallyBlock.codegen(output);
        }
        // add an uncoditional jump to the end finally label
        output.addBranchInstruction(GOTO, endFinally);
        // add an end try label
        output.addLabel(endTry);
        // We can have many catch labels, and according to the
        // genExceptionHandlers file I will need a list of them
        ArrayList<String> catchLabels = new ArrayList<String>();
        // for each parameter found in the parameters arraylist
        for (int i = 0; i < parameters.size(); i++) {
            // create a new catch label for every catch and add it in the list
            String startCatch = output.createLabel();
            // add the catch label in the catchLabels list
            catchLabels.add(startCatch);
            // add a start catch label
            output.addLabel(startCatch);
            // generate code to store the catch variable, this was found in
            // the GenExceptionHAndler.java file
            output.addNoArgInstruction(ASTORE_1);
            // generate code for the catchblock
            catchBlocks.get(i).codegen(output);
            // add the endCatch label
            output.addLabel(endCatch);
            // add an exception handler with the appropriate arguments
            // In this case we have the arguments: startTry, endTry, startCatch,
            // and the name and type of the parameter for the catchblock
            output.addExceptionHandler(startTry, endTry, startCatch,
                    parameters.get(i).type().jvmName());
            // generate code for the optional finally block
            if (finallyBlock != null) {
                finallyBlock.codegen(output);
                // add an unconditional jump to the endFinally label
                output.addBranchInstruction(GOTO, endFinally);
            }
        }
        // add a start finally label for the optional finally block
        if (finallyBlock != null) {
            output.addLabel(startFinally);
            // generate an ASTORE instruction with the offset o obtained
            // from the context for the finally block
            output.addOneArgInstruction(ASTORE, offset);
            // add a start finally plus one label
            output.addLabel(startFinallyPlusOne);
            // generate code for the finally block
            finallyBlock.codegen(output);
            // generate an ALOAD instrucyion with the offset o
            output.addOneArgInstruction(ALOAD, offset);
            // generate an ATHROW instruction
            output.addNoArgInstruction(ATHROW);
            // add an end finally label
            output.addLabel(endFinally);
            // add an exception handler that has as arguments start try, end try
            // start finally, null.
            output.addExceptionHandler(startTry, endTry, startFinally, null);
        }
        // for each catch block which I represent with the catchlabels
        for (String catchlabel: catchLabels) {
            // add the exception handler with arguments catchlabel, endCatch, startFinally, and null
            output.addExceptionHandler(catchlabel, endCatch, startFinally, null);
        }
        // add an exception handler with the arguments: startFinally, startFinallyPlusOne, startFinally, and null
        output.addExceptionHandler(startFinally, startFinallyPlusOne, startFinally, null);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JTryStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("TryBlock", e1);
        tryBlock.toJSON(e1);
        if (catchBlocks != null) {
            for (int i = 0; i < catchBlocks.size(); i++) {
                JFormalParameter param = parameters.get(i);
                JBlock catchBlock = catchBlocks.get(i);
                JSONElement e2 = new JSONElement();
                e.addChild("CatchBlock", e2);
                String s = String.format("[\"%s\", \"%s\"]", param.name(), param.type() == null ?
                        "" : param.type().toString());
                e2.addAttribute("parameter", s);
                catchBlock.toJSON(e2);
            }
        }
        if (finallyBlock != null) {
            JSONElement e2 = new JSONElement();
            e.addChild("FinallyBlock", e2);
            finallyBlock.toJSON(e2);
        }
    }
}
