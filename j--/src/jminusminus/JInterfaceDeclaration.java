// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.RETURN;

/**
 * A representation of an interface declaration.
 */
class JInterfaceDeclaration extends JAST implements JTypeDecl {
    // Interface modifiers.
    private ArrayList<String> mods;

    // Interface name.
    private String name;

    // This interface type.
    private Type thisType;

    // Super class type.
    private Type superType;

    // Extended interfaces.
    private ArrayList<TypeName> superInterfaces;

    // Interface block.
    private ArrayList<JMember> interfaceBlock;

    // Context for this interface.
    private ClassContext context;

    // Interfaces don't contain constructors or instance fields, thus
    // I have not included those array lists here
    // Static (interface) fields of this class.
    private ArrayList<JFieldDeclaration> staticFieldInitializations;
    // an array that will hold the names of the superinterfaces.
    private ArrayList<String> interfaces;
    /**
     * Constructs an AST node for an interface declaration.
     *
     * @param line            line in which the interface declaration occurs in the source file.
     * @param mods            class modifiers.
     * @param name            class name.
     * @param superInterfaces super class types.
     * @param interfaceBlock  interface block.
     */
    public JInterfaceDeclaration(int line, ArrayList<String> mods, String name,
                                 ArrayList<TypeName> superInterfaces,
                                 ArrayList<JMember> interfaceBlock) {
        super(line);
        this.mods = mods;
        this.name = name;
        this.superType = Type.OBJECT;
        this.superInterfaces = superInterfaces;
        this.interfaceBlock = interfaceBlock;
        staticFieldInitializations = new ArrayList<JFieldDeclaration>();

        // An interface must have the "abstract" and "interface" modifiers.
        // These were already added here. I didn't need to add them myself.
        if (!this.mods.contains("abstract")) {
            mods.add("abstract");
        }
        this.mods.add("interface");
    }

    /**
     * {@inheritDoc}
     */
    public void declareThisType(Context context) {
        // TODO
        // I copied the entirety of the this method from the JClassDeclaration
        // file. The only change I have made, was to include the arraylist interfaces
        // as an argument in the call of the method addClass below.
        String qualifiedName = JAST.compilationUnit.packageName() == "" ?
                name : JAST.compilationUnit.packageName() + "/" + name;
        CLEmitter partial = new CLEmitter(false);
        partial.addClass(mods, qualifiedName, Type.OBJECT.jvmName(), interfaces, false);
        thisType = Type.typeFor(partial.toClass());
        context.addType(line, thisType);
    }

    /**
     * {@inheritDoc}
     */
    public void preAnalyze(Context context) {
        // TODO
        // I copied the entirety of this method from the JClassDEclaration
        // with some small changes to fit the interface declarations
        // Construct a class context.
        this.context = new ClassContext(this, context);

        // Resolve superinterface.
        superType = superType.resolve(this.context);

        // Creating a partial class in memory can result in a java.lang.VerifyError if the
        // semantics below are violated, so we can't defer these checks to analyze().
        thisType.checkAccess(line, superType);
        if (superType.isFinal()) {
            JAST.compilationUnit.reportSemanticError(line, "Cannot extend a final type: %s",
                    superType.toString());
        }

        // Create the (partial) interface.
        CLEmitter partial = new CLEmitter(false);

        // Add the class header to the partial interface
        String qualifiedName = JAST.compilationUnit.packageName() == "" ?
                name : JAST.compilationUnit.packageName() + "/" + name;
        // the superInterfaces, now have a non null argument, and more specifically they have
        // an arraylist with interfaces
        partial.addClass(mods, qualifiedName, superType.jvmName(), interfaces, false);

        // Pre-analyze the members and add them to the partial interface.
        for (JMember member : interfaceBlock) {
            member.preAnalyze(this.context, partial);
            // I deleted the explicit constructor because it is not needed for the interfaces
        }

        // Get the ClassRep for the (partial) interface and make it the representation for this type.
        Type id = this.context.lookupType(name);
        if (id != null && !JAST.compilationUnit.errorHasOccurred()) {
            id.setClassRep(partial.toClass());
        }
    }

    /**
     * {@inheritDoc}
     */
    public String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public Type superType() {
        return superType;
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<TypeName> superInterfaces() {
        return superInterfaces;
    }

    /**
     * {@inheritDoc}
     */
    public Type thisType() {
        // TODO
        //return null;
        //Copied from the JClassDeclaration file
        return thisType;
    }

    /**
     * {@inheritDoc}
     */
    public JAST analyze(Context context) {
        // TODO
        // Analyze all members.
        // I also copied all the code for this method from the JClassDeclaration
        // class with some small modifications that fit the interfaces
        // I am analyzing the members of an interface block
        for (JMember member : interfaceBlock) {
            ((JAST) member).analyze(this.context);
        }

        // Separate declared fields for purposes of initialization
        // Go through the members of the interface block
        for (JMember member : interfaceBlock) {
            if (member instanceof JFieldDeclaration) {
                JFieldDeclaration fieldDecl = (JFieldDeclaration) member;
                if (fieldDecl.mods().contains("static")) {
                    // initialize only the static field because the interface doesn't have an instance field
                    staticFieldInitializations.add(fieldDecl);
                }
            }
        }

        // Finally, ensure that a non-abstract interface has no abstract methods.
        if (!thisType.isAbstract() && thisType.abstractMethods().size() > 0) {
            String methods = "";
            for (Method method : thisType.abstractMethods()) {
                methods += "\n" + method;
            }
            JAST.compilationUnit.reportSemanticError(line,
                    "Interface must be abstract since it defines abstract methods: %s", methods);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
        // I copied most of the code in this method from the JClassDeclaration
        // with some small modifications to fit the interfaces
        String qualifiedName = JAST.compilationUnit.packageName() == "" ?
                name : JAST.compilationUnit.packageName() + "/" + name;
        output.addClass(mods, qualifiedName, superType.jvmName(), interfaces, false);

        // The members of an interface block
        for (JMember member : interfaceBlock) {
            ((JAST) member).codegen(output);
        }

        // Generate an interface initialization method?
        if (staticFieldInitializations.size() > 0) {
            codegenClassInit(output);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JInterfaceDeclaration:" + line, e);
        if (mods != null) {
            ArrayList<String> value = new ArrayList<String>();
            for (String mod : mods) {
                value.add(String.format("\"%s\"", mod));
            }
            e.addAttribute("modifiers", value);
        }
        e.addAttribute("name", name);
        e.addAttribute("super", superType == null ? "" : superType.toString());
        if (superInterfaces != null) {
            ArrayList<String> value = new ArrayList<String>();
            for (TypeName impl : superInterfaces) {
                value.add(String.format("\"%s\"", impl.toString()));
            }
            e.addAttribute("extends", value);
        }
        if (context != null) {
            context.toJSON(e);
        }
        if (interfaceBlock != null) {
            for (JMember member : interfaceBlock) {
                ((JAST) member).toJSON(e);
            }
        }
    }

    // Generates code for interface initialization (in j-- this means static field initializations.
    private void codegenClassInit(CLEmitter output) {
        ArrayList<String> mods = new ArrayList<String>();
        mods.add("public");
        mods.add("static");
        output.addMethod(mods, "<clinit>", "()V", null, false);

        // If there are static field initializations, generate code for them.
        for (JFieldDeclaration staticField : staticFieldInitializations) {
            staticField.codegenInitializations(output);
        }

        output.addNoArgInstruction(RETURN);
    }
}