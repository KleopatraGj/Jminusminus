// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * An enum of token kinds. Each entry in this enum represents the kind of a token along with its
 * image (string representation).
 */
enum TokenKind {
    // End of file.
    EOF(""),

    // Reserved words.
    // In the reserved words below, I added the new reserved words in the alphabetical order as shown in the book
    // appendix. The new reserved words that have been added are the following:
    // break, case, catch, continue, default, do, double, finally, for, implements, interface, long, switch, throw,
    // throws, and try.
    ABSTRACT("abstract"), BOOLEAN("boolean"), BREAK("break"), CASE("case"),
    CATCH("catch"), CHAR("char"), CLASS("class"), CONTINUE("continue"),
    DEFAULT("default"), DO("do"), DOUBLE("double"), ELSE("else"), EXTENDS("extends"),
    FINALLY("finally"), FOR("for"), IF("if"), IMPLEMENTS("implements"), IMPORT("import"),
    INSTANCEOF("instanceof"), INT("int"), INTERFACE("interface"), LONG("long"),
    NEW("new"), PACKAGE("package"), PRIVATE("private"), PROTECTED("protected"),
    PUBLIC("public"), RETURN("return"), STATIC("static"), SUPER("super"),
    SWITCH("switch"),  THIS("this"), THROW("throw"), THROWS("throws"), TRY("try"),
    VOID("void"), WHILE("while"),

    // Operators.
    // For project 2 I added all the necessary operators in the order that is shown in the book.
    // I added the question and colon operator, I also added almost all the assign operators, and some more.
    QUESTION("?"), COLON(":"), ASSIGN("="), EQUAL("=="), LNOT("!"), BNOT("~"),
    NOT_EQUAL("!="), DIV("/"), DIV_ASSIGN("/="), PLUS("+"), PLUS_ASSIGN("+="),
    INC("++"), MINUS("-"), MINUS_ASSIGN("-="), DEC("--"), STAR("*"),
    STAR_ASSIGN("*="), REM("%"), REM_ASSIGN("%="), ARSHIFT(">>"), ARSHIFT_ASSIGN(">>="),
    LRSHIFT(">>>"), LRSHIFT_ASSIGN(">>>="), GE(">="), GT(">"), ALSHIFT("<<"),
    ALSHIFT_ASSIGN("<<="), LE("<="), LT("<"), BXOR("^"), BXOR_ASSIGN("^="),
    BOR("|"), BOR_ASSIGN("|="), LOR("||"), BAND("&"), BAND_ASSIGN("&="),
    LAND("&&"),
    // I added the operators DIV and REM on the line above, I also added the complement, the inclusive OR, the exclusive
    // OR and the and bitwise operators

    // Separators.
    COMMA(","), DOT("."), LBRACK("["), LCURLY("{"), LPAREN("("), RBRACK("]"),
    RCURLY("}"),
    RPAREN(")"), SEMI(";"),

    // Identifiers.
    IDENTIFIER("<IDENTIFIER>"),

    // Literals.
    // I added in the literals, below, the long and the double literal with an alphabetical order.
    CHAR_LITERAL("<CHAR_LITERAL>"), DOUBLE_LITERAL("<DOUBLE_LITERAL>"), FALSE("false"),
    INT_LITERAL("<INT_LITERAL>"), LONG_LITERAL("<LONG_LITERAL>"), NULL("null"),
    STRING_LITERAL("<STRING_LITERAL>"), TRUE("true");

    // The token kind's string representation.
    private String image;

    /**
     * Constructs an instance of TokenKind given its string representation.
     *
     * @param image string representation of the token kind.
     */
    private TokenKind(String image) {
        this.image = image;
    }

    /**
     * Returns the token kind's string representation.
     *
     * @return the token kind's string representation.
     */
    public String tokenRep() {
        if (this == EOF) {
            return "<EOF>";
        }
        if (image.startsWith("<") && image.endsWith(">")) {
            return image;
        }
        return "\"" + image + "\"";
    }

    /**
     * Returns the token kind's image.
     *
     * @return the token kind's image.
     */
    public String image() {
        return image;
    }
}

/**
 * A representation of tokens returned by the Scanner method getNextToken(). A token has a kind
 * identifying what kind of token it is, an image for providing any semantic text, and the line in
 * which it occurred in the source file.
 */
public class TokenInfo {
    // Token kind.
    private TokenKind kind;

    // Semantic text (if any). For example, the identifier name when the token kind is IDENTIFIER
    // . For tokens without a semantic text, it is simply its string representation. For example,
    // "+=" when the token kind is PLUS_ASSIGN.
    private String image;

    // Line in which the token occurs in the source file.
    private int line;

    /**
     * Constructs a TokenInfo object given its kind, the semantic text forming the token, and its
     * line number.
     *
     * @param kind  the token's kind.
     * @param image the semantic text forming the token.
     * @param line  the line in which the token occurs in the source file.
     */
    public TokenInfo(TokenKind kind, String image, int line) {
        this.kind = kind;
        this.image = image;
        this.line = line;
    }

    /**
     * Constructs a TokenInfo object given its kind and its line number. Its image is simply the
     * token kind's string representation.
     *
     * @param kind the token's identifying number.
     * @param line the line in which the token occurs in the source file.
     */
    public TokenInfo(TokenKind kind, int line) {
        this(kind, kind.image(), line);
    }

    /**
     * Returns the token's kind.
     *
     * @return the token's kind.
     */
    public TokenKind kind() {
        return kind;
    }

    /**
     * Returns the line number associated with the token.
     *
     * @return the line number associated with the token.
     */
    public int line() {
        return line;
    }

    /**
     * Returns the token's string representation.
     *
     * @return the token's string representation.
     */
    public String tokenRep() {
        return kind.tokenRep();
    }

    /**
     * Returns the token's image.
     *
     * @return the token's image.
     */
    public String image() {
        return image;
    }
}
