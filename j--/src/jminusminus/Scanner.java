// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import javax.print.DocFlavor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Hashtable;

import static jminusminus.TokenKind.*;

/**
 * A lexical analyzer for j--, that has no backtracking mechanism.
 */
class Scanner {
    // End of file character.
    public final static char EOFCH = CharReader.EOFCH;

    // Keywords in j--.
    private Hashtable<String, TokenKind> reserved;

    // Source characters.
    private CharReader input;

    // Next unscanned character.
    private char ch;

    // Whether a scanner error has been found.
    private boolean isInError;

    // Source file name.
    private String fileName;

    // Line number of current token.
    private int line;

    /**
     * Constructs a Scanner from a file name.
     *
     * @param fileName name of the source file.
     * @throws FileNotFoundException when the named file cannot be found.
     */
    public Scanner(String fileName) throws FileNotFoundException {
        this.input = new CharReader(fileName);
        this.fileName = fileName;
        isInError = false;

        // Keywords in j--
        reserved = new Hashtable<String, TokenKind>();
        reserved.put(ABSTRACT.image(), ABSTRACT);
        reserved.put(BOOLEAN.image(), BOOLEAN);
        // Here is the reserved word break
        reserved.put(BREAK.image(), BREAK);
        // Here I added the reserved word case
        reserved.put(CASE.image(), CASE);
        // Here I added the reserved word catch
        reserved.put(CATCH.image(), CATCH);
        reserved.put(CHAR.image(), CHAR);
        reserved.put(CLASS.image(), CLASS);
        // Here I added the reserved word continue
        reserved.put(CONTINUE.image(), CONTINUE);
        // Here I added the reserved word default
        reserved.put(DEFAULT.image(), DEFAULT);
        // Here I added the reserved word do
        reserved.put(DO.image(), DO);
        // Here I added the reserved word double
        reserved.put(DOUBLE.image(), DOUBLE);
        reserved.put(ELSE.image(), ELSE);
        reserved.put(EXTENDS.image(), EXTENDS);
        reserved.put(FALSE.image(), FALSE);
        // Here I added the reserved word finally
        reserved.put(FINALLY.image(), FINALLY);
        // Here I added the reserved word for
        reserved.put(FOR.image(), FOR);
        reserved.put(IF.image(), IF);
        // Here I added the reserved word implements
        reserved.put(IMPLEMENTS.image(), IMPLEMENTS);
        reserved.put(IMPORT.image(), IMPORT);
        reserved.put(INSTANCEOF.image(), INSTANCEOF);
        reserved.put(INT.image(), INT);
        // Here I added the reserved word interface
        reserved.put(INTERFACE.image(), INTERFACE);
        // Here I added the reserved word long
        reserved.put(LONG.image(), LONG);
        reserved.put(NEW.image(), NEW);
        reserved.put(NULL.image(), NULL);
        reserved.put(PACKAGE.image(), PACKAGE);
        reserved.put(PRIVATE.image(), PRIVATE);
        reserved.put(PROTECTED.image(), PROTECTED);
        reserved.put(PUBLIC.image(), PUBLIC);
        reserved.put(RETURN.image(), RETURN);
        reserved.put(STATIC.image(), STATIC);
        reserved.put(SUPER.image(), SUPER);
        // Here I added the reserved word switch
        reserved.put(SWITCH.image(), SWITCH);
        reserved.put(THIS.image(), THIS);
        // Here I added the reserved word throw
        reserved.put(THROW.image(), THROW);
        // Here I added the reserved word throws
        reserved.put(THROWS.image(), THROWS);
        // Here I added the reserved word try
        reserved.put(TRY.image(), TRY);
        reserved.put(TRUE.image(), TRUE);
        reserved.put(VOID.image(), VOID);
        reserved.put(WHILE.image(), WHILE);

        // Prime the pump.
        nextCh();
    }

    /**
     * Scans and returns the next token from input.
     *
     * @return the next scanned token.
     */
    public TokenInfo getNextToken() {
        StringBuffer buffer;
        boolean moreWhiteSpace = true;
        while (moreWhiteSpace) {
            while (isWhitespace(ch)) {
                nextCh();
            }
            if (ch == '/') {
                nextCh();
                if (ch == '/') {
                    // CharReader maps all new lines to '\n'.
                    while (ch != '\n' && ch != EOFCH) {
                        nextCh();
                    }
                // This is for the multiline comment.
                // If the character after the first '/' is a '*'
                // then we are dealing with a multiline comment
                } else if (ch == '*') {
                    // As long as we don't encounter another '/',
                    // we are still dealing with a comment.
                    while (ch != EOFCH) {
                        // keep looking for the next character that needs to be ignored
                        nextCh();
                        // if the next character is a '*'
                        if (ch == '*') {
                            // get the next character
                            nextCh();
                            // if that character is a '/', then you know the Multiline
                            // comment is over.
                            if (ch == '/') {
                                // get the next character
                                nextCh();
                                // and get out of the loop because, as I mentioned,
                                // the comment is over
                                break;
                            }
                        }
                    }
                // If after the single slash we have an equal sign, then we are dealing with a
                // DIV_ASSIGN or '/='
                } else if (ch == '=') {
                    nextCh();
                    return new TokenInfo(DIV_ASSIGN, line);
                } else {
                    // Make the single slash be handled as division
                    return new TokenInfo(DIV, line);
                }
            } else {
                moreWhiteSpace = false;
            }
        }
        line = input.line();
        switch (ch) {
            case ',':
                nextCh();
                return new TokenInfo(COMMA, line);
            case '.':
                nextCh();
                // This part of the code here is about getting a double literal.
                // First, I check if after the dot there is a digit
                if (isDigit(ch)) {
                    // I am creating a StringBuffer to put all the digits that are following
                    // the dot
                    buffer = new StringBuffer();
                    // I add as the first element in the buffer the '.' itself.
                    buffer.append('.');
                    // I am adding the next character, which is a digit in the buffer, as well.
                    buffer.append(ch);
                    // I am getting the next character.
                    nextCh();
                    // As long as the next character is a Digit,
                    // append that character in the buffer.
                    while (isDigit(ch)) {
                        buffer.append(ch);
                        // Get the next character
                        nextCh();
                    }
                    // When the digits are done, check if the next character is a 'D' or 'd'
                    if (ch == 'D' || ch == 'd') {
                        // If the next character is 'D' or 'd'
                        // put them in the buffer
                        buffer.append(ch);
                        // get the next character
                        nextCh();
                        // return a TokenInfo with the double literal, the buffer, and the line
                        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                    } else if (ch == 'E' || ch == 'e') {
                        // if the character is an 'E' or 'e', call the helper method
                        // isDoubleWithExponent that is of type TokenInfo and
                        // takes as arguments a buffer, and a character. More
                        // information about this helper method can be found
                        // in the definition of the helper method below.
                        isDoubleWithExponent(buffer, ch);
                    }
                    // if there are no letters in the way just return the double literal.
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                } else {
                    return new TokenInfo(DOT, line);
                }
            case '[':
                nextCh();
                return new TokenInfo(LBRACK, line);
            case '{':
                nextCh();
                return new TokenInfo(LCURLY, line);
            case '(':
                nextCh();
                return new TokenInfo(LPAREN, line);
            case ']':
                nextCh();
                return new TokenInfo(RBRACK, line);
            case '}':
                nextCh();
                return new TokenInfo(RCURLY, line);
            case ')':
                nextCh();
                return new TokenInfo(RPAREN, line);
            case ';':
                nextCh();
                return new TokenInfo(SEMI, line);
            // I added the colon case here
            case ':':
                nextCh();
                return new TokenInfo(COLON, line);
            case '*':
                nextCh();
                // Check if after a star there is an '=' for the operator *=,
                // which I named as STAR_ASSIGN
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(STAR_ASSIGN, line);
                // Otherwise just get the start sign normally for multiplications
                } else {
                    return new TokenInfo(STAR, line);
                }
            // Adding the case for the remainder
            case '%':
                nextCh();
                // Check if after a remainder/percent sign there is an '=' for the operator %=,
                // which I named as REM_ASSIGN
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(REM_ASSIGN, line);
                // Otherwise just get the rem sign
                } else {
                    return new TokenInfo(REM, line);
                }
            case '+':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(PLUS_ASSIGN, line);
                } else if (ch == '+') {
                    nextCh();
                    return new TokenInfo(INC, line);
                } else {
                    return new TokenInfo(PLUS, line);
                }
            case '-':
                nextCh();
                if (ch == '-') {
                    nextCh();
                    return new TokenInfo(DEC, line);
               // Check if after a minus there is an '=' for the operator -=
               // which I named as MINUS_ASSIGN
                } else if (ch == '='){
                    nextCh();
                    return new TokenInfo(MINUS_ASSIGN, line);
                } else {
                    return new TokenInfo(MINUS, line);
                }
            case '=':
                nextCh();
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(EQUAL, line);
                } else {
                    return new TokenInfo(ASSIGN, line);
                }
            case '>':
                nextCh();
                if (ch == '>') {
                    nextCh();
                    if (ch == '>') {
                        nextCh();
                        // Check if after >>> there is an '=' for the operator >>>=,
                        // which I named as LRSHIFT_ASSIGN
                        if (ch == '=') {
                            nextCh();
                            // Get the LRSHIFT_ASSIGN
                            return new TokenInfo(LRSHIFT_ASSIGN, line);
                        // Otherwise get the LRSHIFT (>>>)
                        } else {
                            // Added the logical right shift
                            return new TokenInfo(LRSHIFT, line);
                        }
                    // Check if after >> there is an '=' for the operator >>=
                    // which I named ARSHIFT_ASSIGN
                    } else if (ch == '=') {
                        nextCh();
                        // Get the ARSHIFT_ASSIGN
                        return new TokenInfo(ARSHIFT_ASSIGN, line);
                    // Otherwise get the ARSHIFT (>>)
                    } else {
                        // Added the arithmetic right shift
                        return new TokenInfo(ARSHIFT, line);
                    }
                // Check if after > there is a '=' for the operator >=
                // which I named GE
                } else if (ch == '='){
                    nextCh();
                    // Get the GE (>=)
                    return new TokenInfo(GE, line);
                // otherwise get GT (>)
                } else {
                    return new TokenInfo(GT, line);
                }
            case '<':
                nextCh();
                // if directly after the '<', there is an '=',
                // get the TokenInfo for '<=' or named as LE
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(LE, line);
                // Check if after <, there is a '<'
                } else if (ch == '<') {
                    // move to the next character
                    nextCh();
                    // check if the next character is an '=', and if it is
                    // get the TokenInfo ALSHIFT_ASSIGN (<<=)
                    if (ch == '=') {
                        nextCh();
                        return new TokenInfo(ALSHIFT_ASSIGN, line);
                    // otherwise just get the ALSHIFT (<<)
                    } else {
                        // Added the arithmetic left shift
                        return new TokenInfo(ALSHIFT, line);
                        // commented out the error that was shown before
                        //reportScannerError("Operator < is not supported in j--");
                        //return getNextToken();
                    }
                // Here is the option for the '<' operator.
                } else {
                    return new TokenInfo(LT, line);
                }
            case '!':
                nextCh();
                // Check if the next character after the '!' is an equal and get
                // the TokenInfo '!=' or named as NOT_EQUAL
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(NOT_EQUAL, line);
                // Otherwise, the TokenInfo will be just '!' or named as LNOT
                } else {
                    return new TokenInfo(LNOT, line);
                }
            // Adding the complement option
            case '~':
                nextCh();
                return new TokenInfo(BNOT, line);
            case '&':
                nextCh();
                if (ch == '&') {
                    nextCh();
                    return new TokenInfo(LAND, line);
                // Check if the next character after the '&' is an equal and get
                // the TokenInfo '&=' or named as BAND_ASSIGN
                } else if (ch == '=') {
                    nextCh();
                    return new TokenInfo(BAND_ASSIGN, line);
                } else {
                    // Adding the AND option
                    return new TokenInfo(BAND, line);
                    // commented out the error that was here before
                    // reportScannerError("Operator & is not supported in j--");
                    // return getNextToken();
                }
            // Adding the inclusive or option
            case '|':
                nextCh();
                // Check if the next character after the '|' is an equal and get
                // the TokenInfo '|=' or named as BOR_ASSIGN
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(BOR_ASSIGN, line);
                // Check if the next character after the '|' is another '|' and get
                // the TokenInfo '||' or named as LOR
                } else if (ch == '|') {
                    nextCh();
                    return new TokenInfo(LOR, line);
                // Otherwise, we get the BOR (Bitwise OR)
                } else {
                    return new TokenInfo(BOR, line);
                }
            // Adding the exclusive or option
            case '^':
                nextCh();
                // Check if the next character after the '^' is an equal and get
                // the TokenInfo '^=' or named as BXOR_ASSIGN
                if (ch == '=') {
                    nextCh();
                    return new TokenInfo(BXOR_ASSIGN, line);
                // Otherwise, we get the BXOR (Bitwise Exclusive OR)
                } else {
                    return new TokenInfo(BXOR, line);
                }
            // This is the conditional question mark case
            case '?':
                nextCh();
                return new TokenInfo(QUESTION, line);
            case '\'':
                buffer = new StringBuffer();
                buffer.append('\'');
                nextCh();
                if (ch == '\\') {
                    nextCh();
                    buffer.append(escape());
                } else {
                    buffer.append(ch);
                    nextCh();
                }
                if (ch == '\'') {
                    buffer.append('\'');
                    nextCh();
                    return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
                } else {
                    // Expected a ' ; report error and try to recover.
                    reportScannerError(ch + " found by scanner where closing ' was expected");
                    while (ch != '\'' && ch != ';' && ch != '\n') {
                        nextCh();
                    }
                    return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
                }
            case '"':
                buffer = new StringBuffer();
                buffer.append("\"");
                nextCh();
                while (ch != '"' && ch != '\n' && ch != EOFCH) {
                    if (ch == '\\') {
                        nextCh();
                        buffer.append(escape());
                    } else {
                        buffer.append(ch);
                        nextCh();
                    }
                }
                if (ch == '\n') {
                    reportScannerError("Unexpected end of line found in string");
                } else if (ch == EOFCH) {
                    reportScannerError("Unexpected end of file found in string");
                } else {
                    // Scan the closing "
                    nextCh();
                    buffer.append("\"");
                }
                return new TokenInfo(STRING_LITERAL, buffer.toString(), line);
            case EOFCH:
                return new TokenInfo(EOF, line);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                buffer = new StringBuffer();
                while (isDigit(ch)) {
                    buffer.append(ch);
                    nextCh();
                }
                // When the digits are done, look if the next character is
                // an upper or lower case l ('L' or 'l')
                if (ch == 'l' || ch == 'L') {
                    // If the character is either 'L' or 'l', then put it in the
                    // buffer and get the next character
                    buffer.append(ch);
                    nextCh();
                    // return the TokenInfo with arguments the long literal,
                    // the buffer and the line.
                    return new TokenInfo(LONG_LITERAL, buffer.toString(), line);
                // If after a single digit or a series of digits the next character
                // is a dot, then there might be some potential of dealing with a
                // double or some nonsense
                } else if (ch == '.') {
                    // include the '.' in the buffer
                    buffer.append(ch);
                    // get the next character
                    nextCh();
                    // As long as the next character is a digit,
                    // append that character in the buffer
                    while (isDigit(ch)) {
                        buffer.append(ch);
                        // Get the next character
                        nextCh();
                    }
                    // after the dot, and after a single digit or a series of digits,
                    // if we encounter a character that is a 'D' or a 'd'. Then,
                    // I am appending that character in the buffer because we are most likely
                    // dealing with a double literal
                    if (ch == 'D' || ch == 'd') {
                        buffer.append(ch);
                        // Get the next character
                        nextCh();
                        // return a token info that contains the double literal,
                        // the buffer, and the line
                        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                    // Or the may be a chance that you could encounter an exponent instead
                    // Check if the next character is a 'E' or 'e' for exponent, and call
                    // the helper method I created which takes as argument a buffer and a character
                    // and its of type is TokenInfo
                    } else if (ch == 'E' || ch == 'e') {
                        isDoubleWithExponent(buffer, ch);
                    }
                    // There may be the case were a 'D' or 'd' or 'E' or 'e' has not been seen.
                    // But we still may be dealing with a double literal since there is a dot.
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                // If after a single digit or series of digits there is a character 'D' or 'd'
                // then append that character in the buffer, get the next character, and return
                // the double literal, with the buffer and the line. This choice, here, is for
                // the case were we have for example: 1234567D or 123456d.
                } else if (ch == 'D' || ch == 'd') {
                    buffer.append(ch);
                    nextCh();
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                // Check if after a single digit or a series of digits there is a character 'E' or 'e'
                // that denotes a potential exponent. If there is such a character look for the exponent case
                // This choice looks for a case were we may have for example: 9e9, or 0e9d, or 0e9D, 246e13
                // or 246e13d, or 0e-9d, or 9e+0d, and many more example.
                } else if (ch == 'E' || ch == 'e') {
                    // Call the helper method I created to check for the exponent option
                    // The arguments for this method are a buffer and a character
                    isDoubleWithExponent(buffer, ch);
                    // return the Double literal along with the buffer and the line
                    return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
                } else {
                    return new TokenInfo(INT_LITERAL, buffer.toString(), line);
                }
            default:
                if (isIdentifierStart(ch)) {
                    buffer = new StringBuffer();
                    while (isIdentifierPart(ch)) {
                        buffer.append(ch);
                        nextCh();
                    }
                    String identifier = buffer.toString();
                    if (reserved.containsKey(identifier)) {
                        return new TokenInfo(reserved.get(identifier), line);
                    } else {
                        return new TokenInfo(IDENTIFIER, identifier, line);
                    }
                } else {
                    reportScannerError("Unidentified input token: '%c'", ch);
                    nextCh();
                    return getNextToken();
                }
        }
    }

    /**
     * Returns true if an error has occurred, and false otherwise.
     *
     * @return true if an error has occurred, and false otherwise.
     */
    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * Returns the name of the source file.
     *
     * @return the name of the source file.
     */
    public String fileName() {
        return fileName;
    }

    // Scans and returns an escaped character.
    private String escape() {
        switch (ch) {
            case 'b':
                nextCh();
                return "\\b";
            case 't':
                nextCh();
                return "\\t";
            case 'n':
                nextCh();
                return "\\n";
            case 'f':
                nextCh();
                return "\\f";
            case 'r':
                nextCh();
                return "\\r";
            case '"':
                nextCh();
                return "\\\"";
            case '\'':
                nextCh();
                return "\\'";
            case '\\':
                nextCh();
                return "\\\\";
            default:
                reportScannerError("Badly formed escape: \\%c", ch);
                nextCh();
                return "";
        }
    }

    // Advances ch to the next character from input, and updates the line number.
    private void nextCh() {
        line = input.line();
        try {
            ch = input.nextChar();
        } catch (Exception e) {
            reportScannerError("Unable to read characters from input");
        }
    }

    // Reports a lexical error and records the fact that an error has occurred. This fact can be
    // ascertained from the Scanner by sending it an errorHasOccurred message.
    private void reportScannerError(String message, Object... args) {
        isInError = true;
        System.err.printf("%s:%d: error: ", fileName, line);
        System.err.printf(message, args);
        System.err.println();
    }

    // Returns true if the specified character is a digit (0-9), and false otherwise.
    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    // Returns true if the specified character is a whitespace, and false otherwise.
    private boolean isWhitespace(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\f');
    }

    // Returns true if the specified character can start an identifier name, and false otherwise.
    private boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '$');
    }

    // Returns true if the specified character can be part of an identifier name, and false
    // otherwise.
    private boolean isIdentifierPart(char c) {
        return (isIdentifierStart(c) || isDigit(c));
    }

    // Created this helper method that takes as arguments a Stringbuffer, and a char.
    // I created this helper method because I saw it showing up a few times in the scanner
    // There is more code that is showing up as well but, as many times as I have tried to
    // create other helper methods my code is breaking. Thus, I will only use the
    // following method
    private TokenInfo isDoubleWithExponent(StringBuffer buffer, char c) {
        // Add the given character to the buffer, because the checking has already
        // happen above in the '.' case and in the 9 case.
        buffer.append(c);
        // Get the next character
        nextCh();
        // Check if the next character is a '-' or a '+'
        if (ch == '-' || ch == '+') {
            // if it is added to the buffer
            buffer.append(ch);
            // Get the next character
            nextCh();
        }
        // As long as the next character is a digit,
        // append that character in the buffer
        while (isDigit(ch)) {
            buffer.append(ch);
            // Get the next Character
            nextCh();
        }
        // If the next character is either a 'D' or a 'd',
        // append it in the buffer
        if (ch == 'D' || ch == 'd' ) {
            buffer.append(ch);
            // Get the next character
            nextCh();
            // Return a TokenInfo with arguments, the double literal,
            // the buffer we created, and the line
            return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
        }
        // In case there isn't and 'D' or 'd', then we aer probably still working
        // with a double literal. Thus, return a TokenInfo with it.
        return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
    }
}

/**
 * A buffered character reader, which abstracts out differences between platforms, mapping all new
 * lines to '\n', and also keeps track of line numbers.
 */
class CharReader {
    // Representation of the end of file as a character.
    public final static char EOFCH = (char) -1;

    // The underlying reader records line numbers.
    private LineNumberReader lineNumberReader;

    // Name of the file that is being read.
    private String fileName;

    /**
     * Constructs a CharReader from a file name.
     *
     * @param fileName the name of the input file.
     * @throws FileNotFoundException if the file is not found.
     */
    public CharReader(String fileName) throws FileNotFoundException {
        lineNumberReader = new LineNumberReader(new FileReader(fileName));
        this.fileName = fileName;
    }

    /**
     * Scans and returns the next character.
     *
     * @return the character scanned.
     * @throws IOException if an I/O error occurs.
     */
    public char nextChar() throws IOException {
        return (char) lineNumberReader.read();
    }

    /**
     * Returns the current line number in the source file.
     *
     * @return the current line number in the source file.
     */
    public int line() {
        return lineNumberReader.getLineNumber() + 1; // LineNumberReader counts lines from 0
    }

    /**
     * Returns the file name.
     *
     * @return the file name.
     */
    public String fileName() {
        return fileName;
    }

    /**
     * Closes the file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        lineNumberReader.close();
    }
}
