// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static jminusminus.TokenKind.*;

/**
 * A recursive descent parser that, given a lexical analyzer (a LookaheadScanner), parses a j--
 * compilation unit (program file), taking tokens from the LookaheadScanner, and produces an
 * abstract syntax tree (AST) for it.
 */
public class Parser {
    // The lexical analyzer with which tokens are scanned.
    private LookaheadScanner scanner;

    // Whether a parser error has been found.
    private boolean isInError;

    // Whether we have recovered from a parser error.
    private boolean isRecovered;

    /**
     * Constructs a parser from the given lexical analyzer.
     *
     * @param scanner the lexical analyzer with which tokens are scanned.
     */
    public Parser(LookaheadScanner scanner) {
        this.scanner = scanner;
        isInError = false;
        isRecovered = true;

        // Prime the pump.
        scanner.next();
    }

    /**
     * Returns true if a parser error has occurred up to now, and false otherwise.
     *
     * @return true if a parser error has occurred up to now, and false otherwise.
     */
    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * Parses a compilation unit (a program file) and returns an AST for it.
     *
     * <pre>
     *     compilationUnit ::= [ PACKAGE qualifiedIdentifier SEMI ]
     *                         { IMPORT  qualifiedIdentifier SEMI }
     *                         { typeDeclaration }
     *                         EOF
     * </pre>
     *
     * @return an AST for a compilation unit.
     */
    public JCompilationUnit compilationUnit() {
        int line = scanner.token().line();
        String fileName = scanner.fileName();
        TypeName packageName = null;
        if (have(PACKAGE)) {
            packageName = qualifiedIdentifier();
            mustBe(SEMI);
        }
        ArrayList<TypeName> imports = new ArrayList<TypeName>();
        while (have(IMPORT)) {
            imports.add(qualifiedIdentifier());
            mustBe(SEMI);
        }
        ArrayList<JAST> typeDeclarations = new ArrayList<JAST>();
        while (!see(EOF)) {
            JAST typeDeclaration = typeDeclaration();
            if (typeDeclaration != null) {
                typeDeclarations.add(typeDeclaration);
            }
        }
        mustBe(EOF);
        return new JCompilationUnit(fileName, line, packageName, imports, typeDeclarations);
    }

    /**
     *
     * Parses and returns a qualified identifier.
     *
     * <pre>
     *   qualifiedIdentifier ::= IDENTIFIER { DOT IDENTIFIER }
     * </pre>
     *
     * @return a qualified identifier.
     */
    private TypeName qualifiedIdentifier() {
        int line = scanner.token().line();
        mustBe(IDENTIFIER);
        String qualifiedIdentifier = scanner.previousToken().image();
        while (have(DOT)) {
            mustBe(IDENTIFIER);
            qualifiedIdentifier += "." + scanner.previousToken().image();
        }
        return new TypeName(line, qualifiedIdentifier);
    }

    /**
     * Parses a type declaration and returns an AST for it.
     *
     * <pre>
     *   typeDeclaration ::= modifiers ( classDeclaration | interfaceDeclaration )
     *
     * </pre>
     *
     * @return an AST for a type declaration.
     */
    private JAST typeDeclaration() {
        ArrayList<String> mods = modifiers();
        // When looking at an Interface, return an interfaceDeclaration with arguments the modifiers.
        if (see(INTERFACE)) {
            return interfaceDeclaration(mods);
        } else {
            return classDeclaration(mods);
        }
    }

    /**
     * Parses and returns a list of modifiers.
     *
     * <pre>
     *   modifiers ::= { ABSTRACT | PRIVATE | PROTECTED | PUBLIC | STATIC }
     * </pre>
     *
     * @return a list of modifiers.
     */
    private ArrayList<String> modifiers() {
        ArrayList<String> mods = new ArrayList<String>();
        boolean scannedPUBLIC = false;
        boolean scannedPROTECTED = false;
        boolean scannedPRIVATE = false;
        boolean scannedSTATIC = false;
        boolean scannedABSTRACT = false;
        boolean more = true;
        while (more) {
            if (have(ABSTRACT)) {
                mods.add("abstract");
                if (scannedABSTRACT) {
                    reportParserError("Repeated modifier: abstract");
                }
                scannedABSTRACT = true;
            } else if (have(PRIVATE)) {
                mods.add("private");
                if (scannedPRIVATE) {
                    reportParserError("Repeated modifier: private");
                }
                if (scannedPUBLIC || scannedPROTECTED) {
                    reportParserError("Access conflict in modifiers");
                }
                scannedPRIVATE = true;
            } else if (have(PROTECTED)) {
                mods.add("protected");
                if (scannedPROTECTED) {
                    reportParserError("Repeated modifier: protected");
                }
                if (scannedPUBLIC || scannedPRIVATE) {
                    reportParserError("Access conflict in modifiers");
                }
                scannedPROTECTED = true;
            } else if (have(PUBLIC)) {
                mods.add("public");
                if (scannedPUBLIC) {
                    reportParserError("Repeated modifier: public");
                }
                if (scannedPROTECTED || scannedPRIVATE) {
                    reportParserError("Access conflict in modifiers");
                }
                scannedPUBLIC = true;
            } else if (have(STATIC)) {
                mods.add("static");
                if (scannedSTATIC) {
                    reportParserError("Repeated modifier: static");
                }
                scannedSTATIC = true;
            } else if (have(ABSTRACT)) {
                mods.add("abstract");
                if (scannedABSTRACT) {
                    reportParserError("Repeated modifier: abstract");
                }
                scannedABSTRACT = true;
            } else {
                more = false;
            }
        }
        return mods;
    }

    /**
     * Parses a class declaration and returns an AST for it.
     *
     * <pre>
     *   classDeclaration ::= CLASS IDENTIFIER [ EXTENDS qualifiedIdentifier ]
     *                          [ IMPLEMENTS qualifiedIdentifier { COMMA qualifiedIdentifier } ] classBody
     * </pre>
     *
     * @param mods the class modifiers.
     * @return an AST for a class declaration.
     */
    private JClassDeclaration classDeclaration(ArrayList<String> mods) {
        int line = scanner.token().line();
        mustBe(CLASS);
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        Type superClass;
        // I am adding a list of type TypeName for the superInterfaces.
        ArrayList<TypeName> superInterfaces = new ArrayList<TypeName>();
        if (have(EXTENDS)) {
            superClass = qualifiedIdentifier();
        } else {
            superClass = Type.OBJECT;
        }
        // implementation for the IMPLEMENTS for the case of the interface
        if (have(IMPLEMENTS)) {
            do {
                // create this list with qualified identifiers if you encounter an IMPLEMENTS
                superInterfaces.add(qualifiedIdentifier());
            } while (have(COMMA));
        } else {
            // otherwise the list is going to be empty
            superInterfaces = null;
        }
        return new JClassDeclaration(line, mods, name, superClass, superInterfaces, classBody());
    }

    /**
     * Parses an interface declaration and returns an AST for it.
     *
     * <pre>
     *   interfaceDeclaration ::= INTERFACE IDENTIFIER [ EXTENDS qualifiedIdentifier { COMMA qualifiedIdentifier} ]
     *                              interfaceBody
     * </pre>
     *
     * @return an AST for an interface declaration.
     */
    private JInterfaceDeclaration interfaceDeclaration(ArrayList<String> mods) {
        int line = scanner.token().line();
        // Need to have first the word INTERFACE
        mustBe(INTERFACE);
        // Followed by an identifier
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        // Create a list for superInterfaces when you encounter an extends
        ArrayList<TypeName> superInterfaces = new ArrayList<TypeName>();
        // if you encounter an extends
        if (have(EXTENDS)) {
            // create a list with qualifiedIdentifiers
            do {
                superInterfaces.add(qualifiedIdentifier());
            } while (have(COMMA));
        } else {
            // otherwise the list is going to be empty
            superInterfaces = null;
        }
        // return a JInterfaceDeclaration
        return new JInterfaceDeclaration(line, mods, name, superInterfaces, interfaceBody());
    }
    /**
     * Parses a class body and returns a list of members in the body.
     *
     * <pre>
     *   classBody ::= LCURLY { modifiers memberDecl } RCURLY
     * </pre>
     *
     * @return a list of members in the class body.
     */
    private ArrayList<JMember> classBody() {
        ArrayList<JMember> members = new ArrayList<JMember>();
        mustBe(LCURLY);
        while (!see(RCURLY) && !see(EOF)) {
            ArrayList<String> mods = modifiers();
            members.add(memberDecl(mods));
        }
        mustBe(RCURLY);
        return members;
    }

    /**
     * Parses an interface body and returns a list of members in the body.
     *
     * <pre>
     *   interfaceBody ::= LCURLY { modifiers interfaceMemberDecl } RCURLY
     * </pre>
     *
     * @return a list of members in the class body.
     */
    private ArrayList<JMember> interfaceBody() {
        // This is copy-pasted from the classBody() method above. The only difference between the two is
        // the call to the interfaceMemberDecl rather than the memberDecl
        ArrayList<JMember> members = new ArrayList<JMember>();
        mustBe(LCURLY);
        while (!see(RCURLY) && !see(EOF)) {
            ArrayList<String> mods = modifiers();
            // I had added this part on a previous project, where I was implementing the parser for the
            // interface
            if (!mods.contains(ABSTRACT)) {
                mods.add(String.valueOf("abstract"));
                // in case the list with the modifiers doesn't have "public"
                // add "public" to them
                if(!mods.contains(PUBLIC)) {
                    mods.add(String.valueOf("public"));
                }
            }
            members.add(interfaceMemberDecl(mods));
        }
        mustBe(RCURLY);
        return members;
    }
    /**
     * Parses a member declaration and returns an AST for it.
     *
     * <pre>
     *   memberDecl ::= IDENTIFIER formalParameters [ THROWS qualifiedIdentifier { COMMA qualifiedIdentifier } ] block
     *                | ( VOID | type ) IDENTIFIER
     *                          formalParameters
     *                              [ THROWS qualifiedIdentifier { COMMA qualifiedIdentifier } ] ( block | SEMI )
     *                | type variableDeclarators SEMI
     * </pre>
     *
     * @param mods the class member modifiers.
     * @return an AST for a member declaration.
     */
    private JMember memberDecl(ArrayList<String> mods) {
        int line = scanner.token().line();
        JMember memberDecl = null;
        if (seeIdentLParen()) {
            // A constructor.
            mustBe(IDENTIFIER);
            String name = scanner.previousToken().image();
            ArrayList<JFormalParameter> params = formalParameters();
            // I added a list of type TypeName that will be containing the exceptions
            ArrayList<TypeName> exceptions = new ArrayList<TypeName>();
            // if you encounter THROWS then add qualifiedIdentifiers in the list
            // with the exceptions
            if (have(THROWS)) {
                // until you get a COMMA, keep adding qualified identifiers in the list of the exceptions
                do {
                    exceptions.add(qualifiedIdentifier());
                } while (have(COMMA));
            } else {
                // otherwise the list with the exceptions will be empty
                exceptions = null;
            }
            JBlock body = block();
            // change the parameter null to exceptions
            memberDecl = new JConstructorDeclaration(line, mods, name, params, exceptions, body);
        } else {
            Type type = null;
            if (have(VOID)) {
                // A void method.
                type = Type.VOID;
                mustBe(IDENTIFIER);
                String name = scanner.previousToken().image();
                ArrayList<JFormalParameter> params = formalParameters();
                // I added a list of type TypeName that will be containing the exceptions
                ArrayList<TypeName> exceptions = new ArrayList<TypeName>();
                // if you encounter THROWS then add qualifiedIdentifiers in the list
                // with the exceptions
                if (have(THROWS)) {
                    // until you get a COMMA, keep adding qualified identifiers in the list of the exceptions
                    do {
                        exceptions.add(qualifiedIdentifier());
                    } while (have(COMMA));
                } else {
                    // otherwise the exceptions list will be empty
                    exceptions = null;
                }
                JBlock body = have(SEMI) ? null : block();
                // change the parameter null to exceptions
                memberDecl = new JMethodDeclaration(line, mods, name, type, params, exceptions, body);
            } else {
                type = type();
                if (seeIdentLParen()) {
                    // A non void method.
                    mustBe(IDENTIFIER);
                    String name = scanner.previousToken().image();
                    ArrayList<JFormalParameter> params = formalParameters();
                    // I added a list of type TypeName that will be containing the exceptions
                    ArrayList<TypeName> exceptions = new ArrayList<TypeName>();
                    // if you encounter THROWS then add qualifiedIdentifiers in the list
                    // with the exceptions
                    if (have(THROWS)) {
                        // until you get a COMMA, keep adding qualified identifiers in the list of the exceptions
                        do {
                            exceptions.add(qualifiedIdentifier());
                        } while (have(COMMA));
                    } else {
                        // otherwise the list with the exceptions will be empty
                        exceptions = null;
                    }
                    JBlock body = have(SEMI) ? null : block();
                    // change the parameter null to exceptions
                    memberDecl = new JMethodDeclaration(line, mods, name, type, params, exceptions, body);
                } else {
                    // A field.
                    memberDecl = new JFieldDeclaration(line, mods, variableDeclarators(type));
                    mustBe(SEMI);
                }
            }
        }
        return memberDecl;
    }
    /**
     * Parses an interface member declaration and returns an AST for it.
     *
     * <pre>
     *   interfaceMemberDecl ::= ( VOID | type ) IDENTIFIER formalParameters
     *                                  [ THROWS qualifiedIdentifier { COMMA qualifiedIdentifier } ] SEMI
     *                          | type variableDeclarators SEMI
     * </pre>
     *
     * @param mods the interface member modifiers.
     * @return an AST for an interface member declaration.
     */
    private JMember interfaceMemberDecl(ArrayList<String> mods) {
        // I also copied a big chunk of this part of the code from the memberDecl() method above.
        int line = scanner.token().line();
        JMember interfaceMemberDecl = null;
        Type type = null;
        if (have(VOID)) {
            // A void method.
            type = Type.VOID;
            mustBe(IDENTIFIER);
            String name = scanner.previousToken().image();
            ArrayList<JFormalParameter> params = formalParameters();
            // I added a list of type TypeName that will be containing the exceptions
            ArrayList<TypeName> exceptions = new ArrayList<TypeName>();
            // if you encounter THROWS then add qualifiedIdentifiers in the list
            // with the exceptions
            if (have(THROWS)) {
                // until you get a COMMA, keep adding qualified identifiers in the list of the exceptions
                do {
                    exceptions.add(qualifiedIdentifier());
                } while (have(COMMA));
            } else {
                // otherwise the list with the exception will be empty
                exceptions = null;
            }
            // It should end with a SEMI
            mustBe(SEMI);
            // change the parameter of the body to null
            interfaceMemberDecl = new JMethodDeclaration(line, mods, name, type, params, exceptions, null);
        } else {
            type = type();
            if (seeIdentLParen()) {
                // A non void method.
                mustBe(IDENTIFIER);
                String name = scanner.previousToken().image();
                ArrayList<JFormalParameter> params = formalParameters();
                // I added a list of type TypeName that will be containing the exceptions
                ArrayList<TypeName> exceptions = new ArrayList<>();
                // if you encounter THROWS then add qualifiedIdentifiers in the list
                // with the exceptions
                if (have(THROWS)) {
                    // until you get a COMMA, keep adding qualified identifiers in the list of the exceptions
                    do {
                        exceptions.add(qualifiedIdentifier());
                    } while (have(COMMA));
                } else {
                    // otherwise the list with the exceptions will be empty
                    exceptions = null;
                }
                // It should end with a semi
                mustBe(SEMI);
                // change the parameter of the body to null
                interfaceMemberDecl = new JMethodDeclaration(line, mods, name, type, params, exceptions, null);
            } else {
                // A field.
                interfaceMemberDecl = new JFieldDeclaration(line, mods, variableDeclarators(type));
                mustBe(SEMI);
            }
        }
        return interfaceMemberDecl;
    }
    /**
     * Parses a block and returns an AST for it.
     *
     * <pre>
     *   block ::= LCURLY { blockStatement } RCURLY
     * </pre>
     *
     * @return an AST for a block.
     */
    private JBlock block() {
        int line = scanner.token().line();
        ArrayList<JStatement> statements = new ArrayList<JStatement>();
        mustBe(LCURLY);
        while (!see(RCURLY) && !see(EOF)) {
            statements.add(blockStatement());
        }
        mustBe(RCURLY);
        return new JBlock(line, statements);
    }

    /**
     * Parses a block statement and returns an AST for it.
     *
     * <pre>
     *   blockStatement ::= localVariableDeclarationStatement
     *                    | statement
     * </pre>
     *
     * @return an AST for a block statement.
     */
    private JStatement blockStatement() {
        if (seeLocalVariableDeclaration()) {
            return localVariableDeclarationStatement();
        } else {
            return statement();
        }
    }

    /**
     * Parses a statement and returns an AST for it.
     *
     * <pre>
     *   statement ::= block
     *               | IF parExpression statement [ ELSE statement ]
     *               | FOR LPAREN [ forInit ] SEMI [ expression ] SEMI [ forUpdate ] RPAREN statement
     *               | RETURN [ expression ] SEMI
     *               | SEMI
     *               | WHILE parExpression statement
     *               | DO statement WHILE parExpression SEMI
     *               | TRY block { CATCH LPAREN formalParameter RPAREN block } [ FINALLY block ]
     *               | SWITCH parExpression LCURLY { switchBlockStatementGroup } RCURLY
     *               | THROW expression SEMI
     *               | BREAK SEMI
     *               | CONTINUE SEMI
     *               | statementExpression SEMI
     * </pre>
     *
     * @return an AST for a statement.
     */
    private JStatement statement() {
        int line = scanner.token().line();
        if (see(LCURLY)) {
            return block();
        } else if (have(IF)) {
            JExpression test = parExpression();
            JStatement consequent = statement();
            JStatement alternate = have(ELSE) ? statement() : null;
            return new JIfStatement(line, test, consequent, alternate);
        // I added the option for the FOR
        } else if (have(FOR)) {
            mustBe(LPAREN);
            // the init will be a list of type Jstatement which is initialized to null
            ArrayList<JStatement> init = null;
            // variable expr of type JExpression for the expression part of the FOR which is initialized to null
            JExpression expr = null;
            // We have to look for any potential update which is also known as step
            // the variable update is an array and is of type JStatement. It's also initialized to null
            ArrayList<JStatement> update = null;
            // look if after the LPAREN there is not a SEMI, then there is a forInit()
            if (!have(SEMI)) {
                // call the method forInit()
                init = forInit();
                // a SEMI is necessary
                mustBe(SEMI);
            }
            // If there is not a SEMI after the first semi, then there is an expression
            if (!have(SEMI)) {
                // get the expression
                expr = expression();
                // which must be followed by a semi
                mustBe(SEMI);
            }
            // if there is an update
            if (!have(RPAREN)) {
                // get the update
                update = forUpdate();
                // which must be followed by a RPAREN ")"
                mustBe(RPAREN);
            }
            // We get the statement
            JStatement statement = statement();
            // And return a JForStatement with arguments the line, the array init, the expression, the array update
            // and the statement
            return new JForStatement(line, init, expr, update, statement);
        } else if (have(WHILE)) {
            JExpression test = parExpression();
            JStatement statement = statement();
            return new JWhileStatement(line, test, statement);
        // I added the option for the DO
        } else if (have(DO)) {
            // For the statement body
            JStatement statement = statement();
            // should be followed by a WHILE
            mustBe(WHILE);
            // condition or known as parExpression
            JExpression test = parExpression();
            // should end with a semicolon
            mustBe(SEMI);
            return new JDoStatement(line, statement, test);
            // I added the option for the try catch block
        } else if (have(TRY)) {
            // get the block after TRY
            JBlock tryBlock = block();
            // Create a list of parameters
            ArrayList<JFormalParameter> params = new ArrayList<JFormalParameter>();
            // Create a list of block that is contained inside the CATCH
            ArrayList<JBlock> catchBlocks = new ArrayList<JBlock>();
            // I have initiallized the FINALLY block to null
            JBlock finallyBlock = null;
            // If you see catch
            if(see(CATCH)) {
                // As long as you have CATCH
                while (have(CATCH)) {
                    // you should have a LPAREN
                    mustBe(LPAREN);
                    // add the formalParameters to the list with the params
                    params.add(formalParameter());
                    // you should have a RPAREN
                    mustBe(RPAREN);
                    // Add blocks to the list with all the blocks in the CATCH
                    catchBlocks.add(block());
                }
                // In case you encounter a FINALLY
                if (have(FINALLY)) {
                    // get the block that follows it
                    finallyBlock = block();
                }
            // In case you don't see catch you must have a FINALLY followed by a block
            } else {
                mustBe(FINALLY);
                finallyBlock = block();
            }
            // return a new JTryStatement with arguments a line the tryblock, the list with the parameters,
            // the list with the blocks in the ccatch, and the finallyblock if it's applicable.
            return new JTryStatement(line, tryBlock, params, catchBlocks, finallyBlock);
        // I added the option for the switch
        } else if (have(SWITCH)) {
            // Get the parExpression
            JExpression test = parExpression();
            // should be followed by an LCURLY "{"
            mustBe(LCURLY);
            // create a list that will contain each switch statement group
            ArrayList<SwitchStatementGroup> stmtGroup = new ArrayList<SwitchStatementGroup>();
            // as long as you don't see a RCURLY or the EOF
            // go through the whole switch block statement group
            while (!see(RCURLY) && !see(EOF)) {
                // Create a list with all the statements in the group
                stmtGroup.add(switchBlockStatementGroup());
            }
            mustBe(RCURLY);
            // return the JSwitchStatement with arguments being a line, an expression and the switch statement group
            return new JSwitchStatement(line, test, stmtGroup);
       // I added the option for the THROW
        } else if (have(THROW)) {
            // Get an expression and save it in the test Jexpression variable
            JExpression test = expression();
            // the expression should be followed by a SEMI
            mustBe(SEMI);
            // return a new JThrowStatement with arguments the line and the expression
            return new JThrowStatement(line, test);
        // I added the option for the break;
        } else if (have(BREAK)) {
            // a BREAK should be followed by a SEMI
            mustBe(SEMI);
            // return a new JBreakStatement with an argument being a line
            return new JBreakStatement(line);
        // I added the option for the continue;
        } else if (have(CONTINUE)) {
            // A CONTINUE should be followed by a SEMI
            mustBe(SEMI);
            // return a new JContinueStatement with an argument being a line
            return new JContinueStatement(line);
        } else if (have(RETURN)) {
            if (have(SEMI)) {
                return new JReturnStatement(line, null);
            } else {
                JExpression expr = expression();
                mustBe(SEMI);
                return new JReturnStatement(line, expr);
            }
        } else if (have(SEMI)) {
            return new JEmptyStatement(line);
        } else {
            // Must be a statementExpression.
            JStatement statement = statementExpression();
            mustBe(SEMI);
            return statement;
        }
    }

    /**
     * Parses and returns a list of formal parameters.
     *
     * <pre>
     *   formalParameters ::= LPAREN [ formalParameter { COMMA formalParameter } ] RPAREN
     * </pre>
     *
     * @return a list of formal parameters.
     */
    private ArrayList<JFormalParameter> formalParameters() {
        ArrayList<JFormalParameter> parameters = new ArrayList<JFormalParameter>();
        mustBe(LPAREN);
        if (have(RPAREN)) {
            return parameters;
        }
        do {
            parameters.add(formalParameter());
        } while (have(COMMA));
        mustBe(RPAREN);
        return parameters;
    }

    /**
     * Parses a formal parameter and returns an AST for it.
     *
     * <pre>
     *   formalParameter ::= type IDENTIFIER
     * </pre>
     *
     * @return an AST for a formal parameter.
     */
    private JFormalParameter formalParameter() {
        int line = scanner.token().line();
        Type type = type();
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        return new JFormalParameter(line, name, type);
    }

    /**
     * Parses a parenthesized expression and returns an AST for it.
     *
     * <pre>
     *   parExpression ::= LPAREN expression RPAREN
     * </pre>
     *
     * @return an AST for a parenthesized expression.
     */
    private JExpression parExpression() {
        mustBe(LPAREN);
        JExpression expr = expression();
        mustBe(RPAREN);
        return expr;
    }

    /**
     * Parses and returns a list of statement expressions or a variable declarator in a form of a list
     * of statement expressions
     *
     * <pre>
     *   forInit ::= statementExpression { COMMA statementExpression }
     *              | type variableDeclarators
     * </pre>
     *
     * @return a list of statement expressions.
     */
    // I added the forInit()
    private ArrayList<JStatement> forInit() {
        // Create a new array named statements
        ArrayList<JStatement> statements = new ArrayList<JStatement>();
        // if it's not looking at a local variable Declaration
        if(!seeLocalVariableDeclaration()) {
            do {
                // add statement expression to the list statements
                statements.add(statementExpression());
            // keep adding until you don't encounter a comma anymore
            } while (have(COMMA));
            // return a list of statement expressions.
            return statements;
        }
        // get the line
        int line = scanner.token().line();
        Type type = type();
        // create a list of type declarators that will contain a single item
        ArrayList<JVariableDeclarator> vdecls = variableDeclarators(type);
        // add it to the list statements
        statements.add(new JVariableDeclaration(line, vdecls));
        // return the list statements.
        return statements;
    }

    /**
     * Parses and returns a list of statement Expressions that are specifically for the update part
     * of a for loop
     *
     * <pre>
     *   forUpdate ::= statementExpression { COMMA statementExpression }
     * </pre>
     *
     * @return a list of statement expressions.
     */
    private ArrayList<JStatement> forUpdate() {
        // Create a list to include all the statement Expressions
        ArrayList<JStatement> statements = new ArrayList<JStatement>();
        // until you encounter a COMMA
        do {
            // add statement Expressions to the list
            statements.add(statementExpression());
        } while (have(COMMA));
        return statements;
    }
    /**
     * Parses a switch block statement group and returns an AST for it.
     *
     * <pre>
     *   switchBlockStatementGroup ::= switchLabel {switchLabel} {blockStatement}
     * </pre>
     *
     * @return an AST for a switch block statement group
     */
    private SwitchStatementGroup switchBlockStatementGroup(){
        // Create a list that will contain all the switch labels
        ArrayList<JExpression> labels = new ArrayList<JExpression>();
        // Create a list that will contain all the switch statement blocks
        ArrayList<JStatement> stmtBlock = new ArrayList<JStatement>();
        // Until you see CASE or you see DEFAULT, keep adding the switch labels to the list.
        // This is happening because there may be CASE labels that do not have any statements aster
        // the COLON.
        do {
            labels.add(switchLabel());
        } while(see(CASE) || see(DEFAULT));
        // As long as you don't encounter a RCURLY, or a CASE, or a DEFAULT
        while(!see(RCURLY) && !see(CASE) && !see(DEFAULT)) {
            // add in the statement block list all the necessary statements
            stmtBlock.add(blockStatement());
        }
        // return a new SwitchStatementGroup with arguments being the list of switch labels and switch statement blocks
        return new SwitchStatementGroup(labels, stmtBlock);
    }
    /**
     * Parses a switch label and returns an AST for it.
     *
     * <pre>
     *   switchLabel ::= CASE expression :
     *                  | DEFAULT :
     * </pre>
     *
     * @return an AST for a switch label.
     */
    private JExpression switchLabel(){
        // we need to get the expression that is located between CASE and COLON,
        // or null if we have to deal with a DEFAULT
        JExpression expr = null;
        // if you encounter a CASE
        if (have(CASE)) {
            // get the expression
            expr = expression();
            // the expression should be followed by a COLON
            mustBe(COLON);
            // return the expression
            return expr;
        } else {
            // otherwise you are most likely dealing with a DEFAULT
            mustBe(DEFAULT);
            // which should be followed by a COLON
            mustBe(COLON);
            // return a null expression
            return expr;
        }
    }

    /**
     * Parses a local variable declaration statement and returns an AST for it.
     *
     * <pre>
     *   localVariableDeclarationStatement ::= type variableDeclarators SEMI
     * </pre>
     *
     * @return an AST for a local variable declaration statement.
     */
    private JVariableDeclaration localVariableDeclarationStatement() {
        int line = scanner.token().line();
        Type type = type();
        ArrayList<JVariableDeclarator> vdecls = variableDeclarators(type);
        mustBe(SEMI);
        return new JVariableDeclaration(line, vdecls);
    }

    /**
     * Parses and returns a list of variable declarators.
     *
     * <pre>
     *   variableDeclarators ::= variableDeclarator { COMMA variableDeclarator }
     * </pre>
     *
     * @param type type of the variables.
     * @return a list of variable declarators.
     */
    private ArrayList<JVariableDeclarator> variableDeclarators(Type type) {
        ArrayList<JVariableDeclarator> variableDeclarators = new ArrayList<JVariableDeclarator>();
        do {
            variableDeclarators.add(variableDeclarator(type));
        } while (have(COMMA));
        return variableDeclarators;
    }

    /**
     * Parses a variable declarator and returns an AST for it.
     *
     * <pre>
     *   variableDeclarator ::= IDENTIFIER [ ASSIGN variableInitializer ]
     * </pre>
     *
     * @param type type of the variable.
     * @return an AST for a variable declarator.
     */
    private JVariableDeclarator variableDeclarator(Type type) {
        int line = scanner.token().line();
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        JExpression initial = have(ASSIGN) ? variableInitializer(type) : null;
        return new JVariableDeclarator(line, name, type, initial);
    }

    /**
     * Parses a variable initializer and returns an AST for it.
     *
     * <pre>
     *   variableInitializer ::= arrayInitializer | expression
     * </pre>
     *
     * @param type type of the variable.
     * @return an AST for a variable initializer.
     */
    private JExpression variableInitializer(Type type) {
        if (see(LCURLY)) {
            return arrayInitializer(type);
        }
        return expression();
    }

    /**
     * Parses an array initializer and returns an AST for it.
     *
     * <pre>
     *   arrayInitializer ::= LCURLY [ variableInitializer { COMMA variableInitializer }
     *                                 [ COMMA ] ] RCURLY
     * </pre>
     *
     * @param type type of the array.
     * @return an AST for an array initializer.
     */
    private JArrayInitializer arrayInitializer(Type type) {
        int line = scanner.token().line();
        ArrayList<JExpression> initials = new ArrayList<JExpression>();
        mustBe(LCURLY);
        if (have(RCURLY)) {
            return new JArrayInitializer(line, type, initials);
        }
        initials.add(variableInitializer(type.componentType()));
        while (have(COMMA)) {
            initials.add(see(RCURLY) ? null : variableInitializer(type.componentType()));
        }
        mustBe(RCURLY);
        return new JArrayInitializer(line, type, initials);
    }

    /**
     * Parses and returns a list of arguments.
     *
     * <pre>
     *   arguments ::= LPAREN [ expression { COMMA expression } ] RPAREN
     * </pre>
     *
     * @return a list of arguments.
     */
    private ArrayList<JExpression> arguments() {
        ArrayList<JExpression> args = new ArrayList<JExpression>();
        mustBe(LPAREN);
        if (have(RPAREN)) {
            return args;
        }
        do {
            args.add(expression());
        } while (have(COMMA));
        mustBe(RPAREN);
        return args;
    }

    /**
     * Parses and returns a type.
     *
     * <pre>
     *   type ::= referenceType | basicType
     * </pre>
     *
     * @return a type.
     */
    private Type type() {
        if (seeReferenceType()) {
            return referenceType();
        }
        return basicType();
    }


    /**
     * Parses and returns a basic type.
     *
     * <pre>
     *   basicType ::= BOOLEAN | DOUBLE | CHAR | INT | LONG
     * </pre>
     *
     * @return a basic type.
     */
    private Type basicType() {
        if (have(BOOLEAN)) {
            return Type.BOOLEAN;
        } else if (have(CHAR)) {
            return Type.CHAR;
        } else if (have(INT)) {
            return Type.INT;
        // I added the LONG basic Type
        } else if (have(LONG)) {
            return Type.LONG;
        // I added the DOUBLE basic Type
        } else if (have(DOUBLE)) {
            return Type.DOUBLE;
        } else {
            reportParserError("Type sought where %s found", scanner.token().image());
            return Type.ANY;
        }
    }

    /**
     * Parses and returns a reference type.
     *
     * <pre>
     *   referenceType ::= basicType LBRACK RBRACK { LBRACK RBRACK }
     *                   | qualifiedIdentifier { LBRACK RBRACK }
     * </pre>
     *
     * @return a reference type.
     */
    private Type referenceType() {
        Type type = null;
        if (!see(IDENTIFIER)) {
            type = basicType();
            mustBe(LBRACK);
            mustBe(RBRACK);
            type = new ArrayTypeName(type);
        } else {
            type = qualifiedIdentifier();
        }
        while (seeDims()) {
            mustBe(LBRACK);
            mustBe(RBRACK);
            type = new ArrayTypeName(type);
        }
        return type;
    }

    /**
     * Parses a statement expression and returns an AST for it.
     *
     * <pre>
     *   statementExpression ::= expression
     * </pre>
     *
     * @return an AST for a statement expression.
     */
    private JStatement statementExpression() {
        int line = scanner.token().line();
        JExpression expr = expression();
        if (expr instanceof JAssignment
                || expr instanceof JPreIncrementOp
                || expr instanceof JPostDecrementOp
                || expr instanceof JMessageExpression
                || expr instanceof JSuperConstruction
                || expr instanceof JThisConstruction
                || expr instanceof JNewOp
                || expr instanceof JNewArrayOp
                // I added an instance of the JStatement for the FOR
                || expr instanceof JStatement) {
            // So as not to save on stack.
            expr.isStatementExpression = true;
        } else {
            reportParserError("Invalid statement expression; it does not have a side-effect");
        }
        return new JStatementExpression(line, expr);
    }

    /**
     * Parses an expression and returns an AST for it.
     *
     * <pre>
     *   expression ::= assignmentExpression
     * </pre>
     *
     * @return an AST for an expression.
     */
    private JExpression expression() {
        return assignmentExpression();
    }

    /**
     * Parses an assignment expression and returns an AST for it.
     *
     * <pre>
     *   assignmentExpression ::= conditionalExpression
     *                                [ ( ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | STAR_ASSIGN | DIV_ASSIGN |
     *                                REM_ASSIGN | ARSHIFT_ASSIGN | LRSHIFT_ASSIGN | ALSHIFT_ASSIGN |
     *                                BXOR_ASSIGN | BOR_ASSIGN | BAND_ASSIGN) assignmentExpression ]
     * </pre>
     *
     * @return an AST for an assignment expression.
     */
    private JExpression assignmentExpression() {
        int line = scanner.token().line();
        JExpression lhs = conditionalExpression();
        if (have(ASSIGN)) {
            return new JAssignOp(line, lhs, assignmentExpression());
        } else if (have(PLUS_ASSIGN)) {
            return new JPlusAssignOp(line, lhs, assignmentExpression());
        // I added the option for the MINUS_ASSIGN "-="
        } else if (have(MINUS_ASSIGN)) {
            return new JMinusAssignOp(line, lhs, assignmentExpression());
        // I added the option for the STAR_ASSIGN "*="
        } else if (have(STAR_ASSIGN)) {
            return new JStarAssignOp(line, lhs, assignmentExpression());
        // I added the option for the DIV_ASSIGN "/="
        } else if (have(DIV_ASSIGN)) {
            return new JDivAssignOp(line, lhs, assignmentExpression());
        // I added the option for the REM_ASSIGN "%="
        } else if (have(REM_ASSIGN)) {
            return new JRemAssignOp(line, lhs, assignmentExpression());
        // I added the option for the ARSHIFT_ASSIGN ">>="
        } else if (have(ARSHIFT_ASSIGN)) {
            return new JARightShiftAssignOp(line, lhs, assignmentExpression());
        // I added the option for the LRSHIFT_ASSIGN ">>>="
        } else if (have(LRSHIFT_ASSIGN)) {
            return new JLRightShiftAssignOp(line, lhs, assignmentExpression());
        // I added the option for the ALSHIFT_ASSIGN "<<="
        } else if (have(ALSHIFT_ASSIGN)) {
            return new JALeftShiftAssignOp(line, lhs, assignmentExpression());
        // I added the option for the BXOR_ASSIGN "^="
        } else if (have(BXOR_ASSIGN)) {
            return new JXorAssignOp(line, lhs, assignmentExpression());
        // I added the option for the BOR_ASSIGN "|="
        } else if (have(BOR_ASSIGN)) {
            return new JOrAssignOp(line, lhs, assignmentExpression());
        // I added the option for the BAND_ASSIGN "&="
        } else if (have(BAND_ASSIGN)) {
            return new JAndAssignOp(line, lhs, assignmentExpression());
        } else {
            return lhs;
        }
    }
    /**
     * Parses a conditional expression and returns an AST for it.
     *
     * <pre>
     *   conditionalExpression ::= conditionalOrExpression
     *                                  [? assignmentExpression : conditionalExpression]
     * </pre>
     *
     * @return an AST for a conditional expression.
     */
    // I added the conditional expression option
    private JExpression conditionalExpression() {
        int line = scanner.token().line();
        // This is for the condition according to te JConditionalExpression
        JExpression lhs = conditionalOrExpression();
        // Look if the next token is a question "?"
        if (have(QUESTION)) {
            // if it is, include in the thenPart expression
            JExpression thenPart = assignmentExpression();
            // after then thenPart we need to have a colon ":"
            mustBe(COLON);
            // after the colon we get the elsePart
            JExpression elsePart = conditionalExpression();
            // Create the JConditionalExpression with the 4 arguments:
            // the line, the condition lhs, the thenPart, and the elsePart
            lhs = new JConditionalExpression(line, lhs, thenPart, elsePart);
        }
        return lhs;
    }

    /**
     * Parses a conditional-or expression and returns an AST for it.
     *
     * <pre>
     *   conditionalOrExpression ::= conditionalAndExpression { LOR conditionalAndExpression }
     * </pre>
     *
     * @return an AST for a conditional-or expression.
     */
    // I added the conditional or option
    private JExpression conditionalOrExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = conditionalAndExpression();
        while (more) {
            if (have(LOR)) {
                lhs = new JLogicalOrOp(line, lhs, conditionalAndExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }
    /**
     * Parses a conditional-and expression and returns an AST for it.
     *
     * <pre>
     *   conditionalAndExpression ::= inclusiveOrExpression { LAND inclusiveOrExpression }
     * </pre>
     *
     * @return an AST for a conditional-and expression.
     */
    private JExpression conditionalAndExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = inclusiveOrExpression();
        while (more) {
            if (have(LAND)) {
                lhs = new JLogicalAndOp(line, lhs, inclusiveOrExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }
    /**
     * Parses an inclusive or expression and returns an AST for it.
     *
     * <pre>
     *   inclusiveOrExpression ::= exclusiveOrExpression { BOR exclusiveOrExpression }
     * </pre>
     *
     * @return an AST for an inclusive or expression.
     */
    private JExpression inclusiveOrExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = exclusiveOrExpression();
        while (more) {
            // Here is the inclusiveOrExpression option
            if (have(BOR)) {
                lhs = new JOrOp(line, lhs, exclusiveOrExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses an exclusive or expression and returns an AST for it.
     *
     * <pre>
     *   exlusiveOrExpression ::= andExpression { BXOR andExpression }
     * </pre>
     *
     * @return an AST for an exclusive or expression.
     */
    private JExpression exclusiveOrExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = andExpression();
        while (more) {
            // This is the exclusive or expression option
            if (have(BXOR)) {
                lhs = new JXorOp(line, lhs, andExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses an and expression and returns an AST for it.
     *
     * <pre>
     *   andExpression ::= equalityExpression { BAND equalityExpression }
     * </pre>
     *
     * @return an AST for an equality expression.
     */
    private JExpression andExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = equalityExpression();
        while (more) {
            // This is the and expression option
            if (have(BAND)) {
                lhs = new JAndOp(line, lhs, equalityExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }
    /**
     * Parses an equality expression and returns an AST for it.
     *
     * <pre>
     *   equalityExpression ::= relationalExpression { EQUAL | NOT_EQUAL relationalExpression }
     * </pre>
     *
     * @return an AST for an equality expression.
     */
    private JExpression equalityExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = relationalExpression();
        while (more) {
            if (have(EQUAL)) {
                lhs = new JEqualOp(line, lhs, relationalExpression());
            // I added the option for the operator not equal
            } else if (have(NOT_EQUAL)) {
                lhs = new JNotEqualOp(line, lhs, relationalExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses a relational expression and returns an AST for it.
     *
     * <pre>
     *   relationalExpression ::= additiveExpression [ ( GT | LT | LE | GE ) additiveExpression
     *                                               | INSTANCEOF referenceType ]
     * </pre>
     *
     * @return an AST for a relational expression.
     */
    private JExpression relationalExpression() {
        int line = scanner.token().line();
        JExpression lhs = shiftExpression();
        if (have(GT)) {
            return new JGreaterThanOp(line, lhs, shiftExpression());
        // I added the option for the LT "<"
        } else if (have(LT)) {
            return new JLessThanOp(line, lhs, shiftExpression());
        } else if (have(LE)) {
            return new JLessEqualOp(line, lhs, shiftExpression());
        // I added the option for the GE ">="
        } else if (have(GE)) {
            return new JGreaterEqualOp(line, lhs, shiftExpression());
        } else if (have(INSTANCEOF)) {
            return new JInstanceOfOp(line, lhs, referenceType());
        } else {
            return lhs;
        }
    }

    /**
     * Parses a shift expression and returns an AST for it.
     *
     * <pre>
     *   shiftExpression ::= additiveExpression { ( ALSHIFT | ARSHIFT | LRSHIFT ) additiveExpression }
     * </pre>
     *
     * @return an AST for a shift expression.
     */
    private JExpression shiftExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = additiveExpression();
        while (more) {
            // add the Arithmetic Left shifting
            if (have(ALSHIFT)) {
                lhs = new JALeftShiftOp(line, lhs, additiveExpression());
            // add the Arithmetic Right shifting
            } else if (have(ARSHIFT)) {
                lhs = new JARightShiftOp(line, lhs, additiveExpression());
            // add the Logical Right shifting
            } else if (have(LRSHIFT)) {
                lhs = new JLRightShiftOp(line, lhs, additiveExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }
    /**
     * Parses an additive expression and returns an AST for it.
     *
     * <pre>
     *   additiveExpression ::= multiplicativeExpression
     *                              { ( MINUS | PLUS ) multiplicativeExpression }
     * </pre>
     *
     * @return an AST for an additive expression.
     */
    private JExpression additiveExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = multiplicativeExpression();
        while (more) {
            if (have(MINUS)) {
                lhs = new JSubtractOp(line, lhs, multiplicativeExpression());
            } else if (have(PLUS)) {
                lhs = new JPlusOp(line, lhs, multiplicativeExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses a multiplicative expression and returns an AST for it.
     *
     * <pre>
     *   multiplicativeExpression ::= unaryExpression { STAR | DIV | REM unaryExpression }
     * </pre>
     *
     * @return an AST for a multiplicative expression.
     */
    private JExpression multiplicativeExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = unaryExpression();
        while (more) {
            if (have(STAR)) {
                lhs = new JMultiplyOp(line, lhs, unaryExpression());
            // added an else if for the DIV (Division) operator
            } else if (have(DIV)) {
                lhs = new JDivideOp(line, lhs, unaryExpression());
            // added an else if for the REM (remainder) operator
            } else if (have(REM)) {
                lhs = new JRemainderOp(line, lhs, unaryExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parses an unary expression and returns an AST for it.
     *
     * <pre>
     *   unaryExpression ::= INC unaryExpression
     *                     | DEC unaryExpression
     *                     | ( MINUS | PLUS)  unaryExpression
     *                     | simpleUnaryExpression
     * </pre>
     *
     * @return an AST for an unary expression.
     */
    private JExpression unaryExpression() {
        int line = scanner.token().line();
        if (have(INC)) {
            return new JPreIncrementOp(line, unaryExpression());
        // I added the option for the decrement operator "--"
        } else if (have(DEC)) {
            return new JPreDecrementOp(line, unaryExpression());
        } else if (have(MINUS)) {
            return new JNegateOp(line, unaryExpression());
        // adding an else if for the unary plus option
        } else if (have(PLUS)) {
            return new JUnaryPlusOp(line, unaryExpression());
        } else {
            return simpleUnaryExpression();
        }
    }

    /**
     * Parses a simple unary expression and returns an AST for it.
     *
     * <pre>
     *   simpleUnaryExpression ::= BNOT unaryExpression
     *                           | LNOT unaryExpression
     *                           | LPAREN basicType RPAREN unaryExpression
     *                           | LPAREN referenceType RPAREN simpleUnaryExpression
     *                           | postfixExpression
     * </pre>
     *
     * @return an AST for a simple unary expression.
     */
    private JExpression simpleUnaryExpression() {
        int line = scanner.token().line();
        if (have(LNOT)) {
            return new JLogicalNotOp(line, unaryExpression());
        } else if (seeCast()) {
            mustBe(LPAREN);
            boolean isBasicType = seeBasicType();
            Type type = type();
            mustBe(RPAREN);
            JExpression expr = isBasicType ? unaryExpression() : simpleUnaryExpression();
            return new JCastOp(line, type, expr);
        // Here is the complement option
        } else if (have(BNOT)) {
            return new JComplementOp(line, unaryExpression());
        } else {
            return postfixExpression();
        }
    }

    /**
     * Parses a postfix expression and returns an AST for it.
     *
     * <pre>
     *   postfixExpression ::= primary { selector } { INC | DEC }
     * </pre>
     *
     * @return an AST for a postfix expression.
     */
    private JExpression postfixExpression() {
        int line = scanner.token().line();
        JExpression primaryExpr = primary();
        while (see(DOT) || see(LBRACK)) {
            primaryExpr = selector(primaryExpr);
        }
        // I added the post increment option here
        while (have(INC)) {
            primaryExpr = new JPostIncrementOp(line, primaryExpr);
        }
        while (have(DEC)) {
            primaryExpr = new JPostDecrementOp(line, primaryExpr);
        }
        return primaryExpr;
    }

    /**
     * Parses a selector and returns an AST for it.
     *
     * <pre>
     *   selector ::= DOT qualifiedIdentifier [ arguments ]
     *              | LBRACK expression RBRACK
     * </pre>
     *
     * @param target the target expression for this selector.
     * @return an AST for a selector.
     */
    private JExpression selector(JExpression target) {
        int line = scanner.token().line();
        if (have(DOT)) {
            // target.selector.
            mustBe(IDENTIFIER);
            String name = scanner.previousToken().image();
            if (see(LPAREN)) {
                ArrayList<JExpression> args = arguments();
                return new JMessageExpression(line, target, name, args);
            } else {
                return new JFieldSelection(line, target, name);
            }
        } else {
            mustBe(LBRACK);
            JExpression index = expression();
            mustBe(RBRACK);
            return new JArrayExpression(line, target, index);
        }
    }

    /**
     * Parses a primary expression and returns an AST for it.
     *
     * <pre>
     *   primary ::= parExpression
     *             | NEW creator
     *             | THIS [ arguments ]
     *             | SUPER ( arguments | DOT IDENTIFIER [ arguments ] )
     *             | qualifiedIdentifier [ arguments ]
     *             | literal
     * </pre>
     *
     * @return an AST for a primary expression.
     */
    private JExpression primary() {
        int line = scanner.token().line();
        if (see(LPAREN)) {
            return parExpression();
        } else if (have(NEW)) {
            return creator();
        } else if (have(THIS)) {
            if (see(LPAREN)) {
                ArrayList<JExpression> args = arguments();
                return new JThisConstruction(line, args);
            } else {
                return new JThis(line);
            }
        } else if (have(SUPER)) {
            if (!have(DOT)) {
                ArrayList<JExpression> args = arguments();
                return new JSuperConstruction(line, args);
            } else {
                mustBe(IDENTIFIER);
                String name = scanner.previousToken().image();
                JExpression newTarget = new JSuper(line);
                if (see(LPAREN)) {
                    ArrayList<JExpression> args = arguments();
                    return new JMessageExpression(line, newTarget, null, name, args);
                } else {
                    return new JFieldSelection(line, newTarget, name);
                }
            }
        } else if (see(IDENTIFIER)) {
            TypeName id = qualifiedIdentifier();
            if (see(LPAREN)) {
                // ambiguousPart.messageName(...).
                ArrayList<JExpression> args = arguments();
                return new JMessageExpression(line, null, ambiguousPart(id), id.simpleName(), args);
            } else if (ambiguousPart(id) == null) {
                // A simple name.
                return new JVariable(line, id.simpleName());
            } else {
                // ambiguousPart.fieldName.
                return new JFieldSelection(line, ambiguousPart(id), null, id.simpleName());
            }
        } else {
            return literal();
        }
    }

    /**
     * Parses a creator and returns an AST for it.
     *
     * <pre>
     *   creator ::= ( basicType | qualifiedIdentifier )
     *                   ( arguments
     *                   | LBRACK RBRACK { LBRACK RBRACK } [ arrayInitializer ]
     *                   | newArrayDeclarator
     *                   )
     * </pre>
     *
     * @return an AST for a creator.
     */
    private JExpression creator() {
        int line = scanner.token().line();
        Type type = seeBasicType() ? basicType() : qualifiedIdentifier();
        if (see(LPAREN)) {
            ArrayList<JExpression> args = arguments();
            return new JNewOp(line, type, args);
        } else if (see(LBRACK)) {
            if (seeDims()) {
                Type expected = type;
                while (have(LBRACK)) {
                    mustBe(RBRACK);
                    expected = new ArrayTypeName(expected);
                }
                return arrayInitializer(expected);
            } else {
                return newArrayDeclarator(line, type);
            }
        } else {
            reportParserError("( or [ sought where %s found", scanner.token().image());
            return new JWildExpression(line);
        }
    }

    /**
     * Parses a new array declarator and returns an AST for it.
     *
     * <pre>
     *   newArrayDeclarator ::= LBRACK expression RBRACK
     *                              { LBRACK expression RBRACK } { LBRACK RBRACK }
     * </pre>
     *
     * @param line line in which the declarator occurred.
     * @param type type of the array.
     * @return an AST for a new array declarator.
     */
    private JNewArrayOp newArrayDeclarator(int line, Type type) {
        ArrayList<JExpression> dimensions = new ArrayList<JExpression>();
        mustBe(LBRACK);
        dimensions.add(expression());
        mustBe(RBRACK);
        type = new ArrayTypeName(type);
        while (have(LBRACK)) {
            if (have(RBRACK)) {
                // We're done with dimension expressions.
                type = new ArrayTypeName(type);
                while (have(LBRACK)) {
                    mustBe(RBRACK);
                    type = new ArrayTypeName(type);
                }
                return new JNewArrayOp(line, type, dimensions);
            } else {
                dimensions.add(expression());
                type = new ArrayTypeName(type);
                mustBe(RBRACK);
            }
        }
        return new JNewArrayOp(line, type, dimensions);
    }

    /**
     * Parses a literal and returns an AST for it.
     *
     * <pre>
     *   literal ::= CHAR_LITERAL | DOUBLE_LITERAL | FALSE | INT_LITERAL | LONG_LITERAL | NULL | STRING_LITERAL | TRUE
     * </pre>
     *
     * @return an AST for a literal.
     */
    private JExpression literal() {
        int line = scanner.token().line();
        if (have(CHAR_LITERAL)) {
            return new JLiteralChar(line, scanner.previousToken().image());
        } else if (have(FALSE)) {
            return new JLiteralBoolean(line, scanner.previousToken().image());
        } else if (have(INT_LITERAL)) {
            return new JLiteralInt(line, scanner.previousToken().image());
        } else if (have(NULL)) {
            return new JLiteralNull(line);
        } else if (have(STRING_LITERAL)) {
            return new JLiteralString(line, scanner.previousToken().image());
        // Add the option for the JLiteralLong
        } else if (have(LONG_LITERAL)) {
            return new JLiteralLong(line, scanner.previousToken().image());
        // Add the option for the JliteralDouble
        } else if (have(DOUBLE_LITERAL)) {
            return new JLiteralDouble(line, scanner.previousToken().image());
        } else if (have(TRUE)) {
            return new JLiteralBoolean(line, scanner.previousToken().image());
        } else {
            reportParserError("Literal sought where %s found", scanner.token().image());
            return new JWildExpression(line);
        }
    }

    //////////////////////////////////////////////////
    // Parsing Support
    // ////////////////////////////////////////////////

    // Returns true if the current token equals sought, and false otherwise.
    private boolean see(TokenKind sought) {
        return (sought == scanner.token().kind());
    }

    // If the current token equals sought, scans it and returns true. Otherwise, returns false
    // without scanning the token.
    private boolean have(TokenKind sought) {
        if (see(sought)) {
            scanner.next();
            return true;
        } else {
            return false;
        }
    }

    // Attempts to match a token we're looking for with the current input token. On success,
    // scans the token and goes into a "Recovered" state. On failure, what happens next depends
    // on whether or not the parser is currently in a "Recovered" state: if so, it reports the
    // error and goes into an "Unrecovered" state; if not, it repeatedly scans tokens until it
    // finds the one it is looking for (or EOF) and then returns to a "Recovered" state. This
    // gives us a kind of poor man's syntactic error recovery, a strategy due to David Turner and
    // Ron Morrison.
    private void mustBe(TokenKind sought) {
        if (scanner.token().kind() == sought) {
            scanner.next();
            isRecovered = true;
        } else if (isRecovered) {
            isRecovered = false;
            reportParserError("%s found where %s sought", scanner.token().image(), sought.image());
        } else {
            // Do not report the (possibly spurious) error, but rather attempt to recover by
            // forcing a match.
            while (!see(sought) && !see(EOF)) {
                scanner.next();
            }
            if (see(sought)) {
                scanner.next();
                isRecovered = true;
            }
        }
    }

    // Pulls out and returns the ambiguous part of a name.
    private AmbiguousName ambiguousPart(TypeName name) {
        String qualifiedName = name.toString();
        int i = qualifiedName.lastIndexOf('.');
        return i == -1 ? null : new AmbiguousName(name.line(), qualifiedName.substring(0, i));
    }

    // Reports a syntax error.
    private void reportParserError(String message, Object... args) {
        isInError = true;
        isRecovered = false;
        System.err.printf("%s:%d: error: ", scanner.fileName(), scanner.token().line());
        System.err.printf(message, args);
        System.err.println();
    }

    //////////////////////////////////////////////////
    // Lookahead Methods
    //////////////////////////////////////////////////

    // Returns true if we are looking at an IDENTIFIER followed by a LPAREN, and false otherwise.
    private boolean seeIdentLParen() {
        scanner.recordPosition();
        boolean result = have(IDENTIFIER) && see(LPAREN);
        scanner.returnToPosition();
        return result;
    }

    // Returns true if we are looking at a cast (basic or reference), and false otherwise.
    private boolean seeCast() {
        scanner.recordPosition();
        if (!have(LPAREN)) {
            scanner.returnToPosition();
            return false;
        }
        if (seeBasicType()) {
            scanner.returnToPosition();
            return true;
        }
        if (!see(IDENTIFIER)) {
            scanner.returnToPosition();
            return false;
        } else {
            scanner.next();
            // A qualified identifier is ok.
            while (have(DOT)) {
                if (!have(IDENTIFIER)) {
                    scanner.returnToPosition();
                    return false;
                }
            }
        }
        while (have(LBRACK)) {
            if (!have(RBRACK)) {
                scanner.returnToPosition();
                return false;
            }
        }
        if (!have(RPAREN)) {
            scanner.returnToPosition();
            return false;
        }
        scanner.returnToPosition();
        return true;
    }

    // Returns true if we are looking at a local variable declaration, and false otherwise.
    private boolean seeLocalVariableDeclaration() {
        scanner.recordPosition();
        if (have(IDENTIFIER)) {
            // A qualified identifier is ok.
            while (have(DOT)) {
                if (!have(IDENTIFIER)) {
                    scanner.returnToPosition();
                    return false;
                }
            }
        } else if (seeBasicType()) {
            scanner.next();
        } else {
            scanner.returnToPosition();
            return false;
        }
        while (have(LBRACK)) {
            if (!have(RBRACK)) {
                scanner.returnToPosition();
                return false;
            }
        }
        if (!have(IDENTIFIER)) {
            scanner.returnToPosition();
            return false;
        }
        while (have(LBRACK)) {
            if (!have(RBRACK)) {
                scanner.returnToPosition();
                return false;
            }
        }
        scanner.returnToPosition();
        return true;
    }

    // Returns true if we are looking at a basic type, and false otherwise.
    // I added the DOUBLE and LONG basic types
    private boolean seeBasicType() {
        return (see(BOOLEAN) || see(CHAR) || see(INT) || see(LONG) || see(DOUBLE));
    }

    // Returns true if we are looking at a reference type, and false otherwise.
    private boolean seeReferenceType() {
        if (see(IDENTIFIER)) {
            return true;
        } else {
            scanner.recordPosition();
            if (have(BOOLEAN) || have(CHAR) || have(INT) || have(LONG) || have(DOUBLE)) {
                if (have(LBRACK) && see(RBRACK)) {
                    scanner.returnToPosition();
                    return true;
                }
            }
            scanner.returnToPosition();
        }
        return false;
    }

    // Returns true if we are looking at a [] pair, and false otherwise.
    private boolean seeDims() {
        scanner.recordPosition();
        boolean result = have(LBRACK) && see(RBRACK);
        scanner.returnToPosition();
        return result;
    }
}
