/**
 * Main class that starts up the compiler and excepts two command line arguments: 
 * 1.name of the .kcc file
 * 2.name of the output file
 * (File paths must be included)
 * @author Jonathan Moreira Alsina
 * @version 1.0
 * Assignment 5
 * CS322 - Compiler Construction
 * Spring 2024
 **/
package compiler;

import lexparse.*;
import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.Trees;

public class kcc {

    public static void main(String[] args){
        
        CharStream input;
        KnightCodeLexer lexer;
        CommonTokenStream tokens; 
        KnightCodeParser parser;
        String output; 
      
        // Handles if args are not entered properly
        if (args.length < 2) {
            System.err.println("Run as: java compiler/kcc <pathToIp/input.kcc> <pathToOp/output>\nReplace pathToIp with the directory of the input file and pathToOp with the desired output location");
            return;
        }
      
        try{
            input = CharStreams.fromFileName(args[0]); 
            
            lexer = new KnightCodeLexer(input); 
            tokens = new CommonTokenStream(lexer); 
            parser = new KnightCodeParser(tokens); 
            output = args[1]; //get the output

            ParseTree tree = parser.file(); 
             
            Trees.inspect(tree, parser); 
            
            MyBaseVisitor visitor = new MyBaseVisitor(output);
            
         
            visitor.visit(tree);

           
            visitor.endClass();

        }
        catch(IOException e){
            System.out.println("Incorrect file paths. Please try again.");
        }
    
    }
}