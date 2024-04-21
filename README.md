# KnightCodeSkeleton

This project creates a compiler that uses a basic programming language named KnightCode. Said language only uses and supports two datatypes: INTEGER and STRING. Additinally, it can perform operations like: Addition, Subtraction, Multiplication, and Division, as well as printing out values to the cmd. Furthermore, it can use simple conditionals like if else, and while loops.

In order to use the compiler, you need two libraries ASM Bytecode Library and ANTLR Library. Then, you will need to build the grammar by running the following in the cmd:

ant build-grammar
ant compile-grammar
ant compile

This will generate the lexparse folder containing the lexers, parser, and other files needed from ANTLR. 
Afterwards, you will need to compile the kcc.java file with two command line arguments. The first one is the path to the .kc file(this is the file with the program written in KnightCode). The second one is the path to the folder you wish to be the location of the output file.
 
 Example:
 java kcc program1.kc output

 Example# 2 (Files in directories):
 java compiler/kcc tests/program1.kc output/output

Finally, if you want to run the output file, you can use the following command in the cmd:
java output/output


