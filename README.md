# Jminusminus
This project was part of a course that introduced how a compiler is organized and implemented. In this project, the compiler is implemented in Java. To be more precise, this compiler is focused on the source language jminusminus(j--) which is a subset of the known programming language Java. The actual implementation is using Java's object orientation. This project was separated into parts: the lexical analysis, the syntax, and the semantics of certain constructs.

# The Implementation of the Jminusminus Compiler

## Lexixal Analysis

I implemented the Scanner.java that deals with the lexicals analysis of the Java language. The scanner tokenizes the input stream of characters, in other words, it breaks the input stream of characters into tokens. In this file, you will see how an identifier, or a int literal, or a double literal, or operators, etc are found and tokenized. To get a better understanding of the Scanner, I needed to learn about transition diagrams, Regular Expressions, Deterministic and Non-deterniministic finite-state automata (DFAs and NFAs).

## Parsing 

I implemented the Parser.java that deals with the syntactic triats of the Java language and more specifically the j-- language. In this phase, the compiler produces the abstract syntax tree (AST). In this phase, the compiler prints all the syntactic errors a programmer has made when implementing a code.

## Type Checking and Code Generation

I implemented the semantic analysis of the compiler which includes the implementations of the methods pre-analyze(), analuze(), and codegen(). Those methods are the core part of the semantics in any languages, since it allows for analysis and code generation of the differenent constructs. It is important to mention that the semantic analysis, and more specifically the type checking part of it, includes and is not limited to "declaring names in a symbol table, looking up names to determine their types, assigning types, etc". Once the type checking is completed, code generation is needed. Technically, I implemenented code that is necessary to generate the Java Virtual Machine (JVM) code that builds the class file for a program. To implement the type checking and the code generation, I needed to implement or edit multiple files. Those files are shown in the table below:

First Header  | Second Header
------------- | -------------
Content Cell  | Content Cell
Content Cell  | Content Cell

# Acknowledgements
