 /**
  *Class that extends and overrides methods from ANTLR generated BaseVisitor class.
  *It will be  performing bytecode operations for grammar rules
  * @author Jonathan Moreira Alsina
  * @version 1.0
  * Assignment 5
  * CS322 - Compiler Construction
  * Spring 2024
  **/
package compiler;
import lexparse.*;
import org.objectweb.asm.*;  //classes for generating bytecode
import java.util.*;
import java.io.FileOutputStream;
import java.io.IOException;


public class MyBaseVisitor extends KnightCodeBaseVisitor<Object>
{

    private ClassWriter cw;  
	private MethodVisitor main_visitor; 
	private String program_name; 
    private Map <String, Variable> symbol_Table; 
    private int memory_pointer;


    /**
     * Constructor for MyBaseListener
     * @param program_name name of the programs
     */
    public MyBaseVisitor(String program_name){
        this.program_name = program_name;
        
    }//end constructor

    /**
     * Method that will set up the ClassWriter and create the constructor
     * @param name the name of the program that will be created
     */
    public void beginClass(String name){
        
       
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,program_name, null, "java/lang/Object",null);
        
        {
			MethodVisitor mv=cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0); //load the first local variable: this
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V",false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(1,1);
			mv.visitEnd();
		}

    }

    /**
     * Ends the main method and writes the ClassWriter data into the outputFile
     */
    public void endClass(){

        main_visitor.visitInsn(Opcodes.RETURN);
        main_visitor.visitMaxs(0, 0);
        main_visitor.visitEnd();
    
        cw.visitEnd();
    
        byte[] b = cw.toByteArray();
    
        writeFile(b,this.program_name+".class");
    
        System.out.println("\nSuccessful Compilation");
        
    }

    @Override
    /**
     * Calls the beginClass method which creates the ClassWriter and constructor for the KnightCode class 
     */
    public Object visitFile(KnightCodeParser.FileContext ctx){
        System.out.println("Entering File");

        beginClass(program_name);
        return super.visitFile(ctx);
    }

    @Override
    /**
     * Once we enter DECLERE, a HashMap for the symbol table will be created and the stack memory pointer will be set to zero
     */
    public Object visitDeclare(KnightCodeParser.DeclareContext ctx){

        System.out.println("Visit Declare");
        
        symbol_Table = new HashMap<>();
        memory_pointer = 0;

        return super.visitDeclare(ctx);
    }

    @Override
    /**
     * Once we enter VARIABLE, the name and type will be used to instantiate a new Variable object
     * using the attributes from the declaration and put it into the symbol table
     */
    public Object visitVariable(KnightCodeParser.VariableContext ctx){
        
        System.out.println("Visit Variable");
        
        String type = ctx.vartype().getText();

        if (!type.equals("INTEGER") && !type.equals("STRING")){
            System.err.println("Compilation error: the entered type is not supported.");
            System.exit(1);
        }

        String name = ctx.identifier().getText();
        Variable var = new Variable(name, type, memory_pointer++);
        symbol_Table.put(name, var);

        printSymbolTable();

        return super.visitVariable(ctx);
    }//end visitVariable

    @Override
    /**
     * Method that visits the body and initializes the main method
     */
    public Object visitBody(KnightCodeParser.BodyContext ctx){
        
        System.out.println("Enter Body");
        
        main_visitor=cw.visitMethod(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        main_visitor.visitCode();

        return super.visitBody(ctx);
    }
    
    /**
     * Evaluates an expression depending on what type of context it is an instance of.
     * @param ctx the context of the expression that is to be evaluated
     */
    public void evalExpr(KnightCodeParser.ExprContext ctx){
        
        if (ctx instanceof KnightCodeParser.NumberContext){
            int value = Integer.parseInt(ctx.getText());
            
            System.out.println(value + " is on the stack");
            main_visitor.visitLdcInsn(value);
        }
        else if (ctx instanceof KnightCodeParser.IdContext){
            String id = ctx.getText();
            Variable var = symbol_Table.get(id);

            System.out.println("expr id " + id + "\nvar: " + var.toString());

            if (var.getType().equals("INTEGER"))
            {
                main_visitor.visitVarInsn(Opcodes.ILOAD, var.getMemLocation());
                System.out.println(id+ " is on stack");
            }

            else if (var.getType().equals("STRING"))
            {
                main_visitor.visitVarInsn(Opcodes.ALOAD, var.getMemLocation());
            } 
            
        }
        //Subtraction
        else if (ctx instanceof KnightCodeParser.SubtractionContext){
            
            for(KnightCodeParser.ExprContext expr : ((KnightCodeParser.SubtractionContext)ctx).expr())
            {
                evalExpr(expr);
            }
            System.out.println("subbing");
            main_visitor.visitInsn(Opcodes.ISUB);
            
        }
        //Addition
        else if (ctx instanceof KnightCodeParser.AdditionContext){
            
            for(KnightCodeParser.ExprContext expr : ((KnightCodeParser.AdditionContext)ctx).expr())
            {
                evalExpr(expr);
            }
            System.out.println("adding");
            main_visitor.visitInsn(Opcodes.IADD);
            
        }
        //Multiplication
        else if (ctx instanceof KnightCodeParser.MultiplicationContext){
            
            for(KnightCodeParser.ExprContext expr : ((KnightCodeParser.MultiplicationContext)ctx).expr())
            {
                evalExpr(expr);
            }
            System.out.println("Multiplying");
            main_visitor.visitInsn(Opcodes.IMUL);
            
        }
        //Division
        else if (ctx instanceof KnightCodeParser.DivisionContext){
            
            for(KnightCodeParser.ExprContext expr : ((KnightCodeParser.DivisionContext)ctx).expr())
            {
                evalExpr(expr);
            }
            System.out.println("dividing");
            main_visitor.visitInsn(Opcodes.IDIV);
            
        }

    }

    @Override
    /**
     * It will perform the comparison operation and : 
     * if true load 1
     * if false load 0
     */
    public Object visitComparison(KnightCodeParser.ComparisonContext ctx){
        
        Label trueLabel = new Label();
        Label endLabel = new Label();

        String op = ctx.comp().getText();

        evalExpr(ctx.expr(0));
        evalExpr(ctx.expr(1));

        switch (op) {
            case "GT":
            main_visitor.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                break;
        
            case "LT":
            main_visitor.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                break;

            case "EQ":
            main_visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                break;

            case "NE":
            main_visitor.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                break;
        }

        main_visitor.visitLdcInsn(0);
        main_visitor.visitJumpInsn(Opcodes.GOTO, endLabel);

        main_visitor.visitLabel(trueLabel);
        main_visitor.visitLdcInsn(1);

        main_visitor.visitLabel(endLabel);
        return super.visitComparison(ctx);
    }


    /**
     * Checks if a string is either a number or an identifier in the symbol table, and loads it
     * @param operand the string with the ID or value to be loaded
     */
    public void loadInteger(String operand){
        int location;
        
        //If the string is an ID of a variable
        if (symbol_Table.containsKey(operand)){
            Variable var = symbol_Table.get(operand);
            location = var.getMemLocation();
            main_visitor.visitVarInsn(Opcodes.ILOAD, location);
        }
        //If it's a number
        else {
            main_visitor.visitLdcInsn(Integer.parseInt(operand));
        }
    }
    
    @Override
    /**
     * Handles the logic of an IF THEN ELSE from a comparison using jumps
     */
    public Object visitDecision(KnightCodeParser.DecisionContext ctx){
        
        Label trueLabel = new Label();
        Label endLabel = new Label();
        
        String num1 = ctx.getChild(1).getText();
        String num2 = ctx.getChild(3).getText();

        loadInteger(num1);
        loadInteger(num2);

        String op = ctx.comp().getText();
        
        switch (op) {
            case ">":
                System.out.println("GT");
                main_visitor.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                break;
        
            case "<":
                System.out.println("LT");
                main_visitor.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                break;

            case "=":
                System.out.println("EQ");  
                main_visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                break;
            case "<>":
                System.out.println("NEQ");
                main_visitor.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                break;
            default:
                System.err.println("ERROR: PLEASE ENTER THE RIGHT OPERATOR.");
                System.exit(1);
        }

        boolean hasElse = false;
        int endLocation = 6; 

         
        while (!ctx.getChild(endLocation).getText().equals("ENDIF"))
        {
            endLocation++;
        }  

        for(int i = 0; i<ctx.children.size(); i++){
            if(ctx.getChild(i).getText().equals("ELSE")){
                hasElse = true;
                break;
            }
        }

        int elseLocation = 6;
        
        if(hasElse){
    
            
            while (!ctx.getChild(elseLocation).getText().equals("ELSE")){
                elseLocation++;
            }  
            
            for(int i = elseLocation+1; i<ctx.getChildCount(); i++){
                visit(ctx.getChild(i));
            }
        }

        main_visitor.visitJumpInsn(Opcodes.GOTO, endLabel);

        //IF comparison is true
        main_visitor.visitLabel(trueLabel);

        if(hasElse)
        {
            for (int i = 5; i< elseLocation;i++)
            {
                visit(ctx.getChild(i));
            }
        }
        else
        {
            for (int i = 5; i< endLocation;i++)
            {
                visit(ctx.getChild(i));
            }
        }

        main_visitor.visitLabel(endLabel);
        return null;

    }
    @Override
    /**
     * Defines a previously declared variable and it is entered when setVar is activated
     */
    public Object visitSetvar(KnightCodeParser.SetvarContext ctx)
    {

        String varName = ctx.ID().getText(); 
        System.out.println("Enter SetVar: " + varName);

        Variable var = symbol_Table.get(varName);
        if (var == null){
            System.err.println("ERROR: " + varName + " has not been declared yet");
            System.exit(1);
        }
        else if(ctx.expr() != null){

            evalExpr(ctx.expr());

            if (var.getType().equals("INTEGER")){
                System.out.println("Storing for " + varName);
                main_visitor.visitVarInsn(Opcodes.ISTORE, var.getMemLocation());
            }
        }
        else if (var.getType().equals("STRING") && ctx.STRING() != null){

            String str = removeFirstandLast(ctx.STRING().getText());
            main_visitor.visitLdcInsn(str);
            main_visitor.visitVarInsn(Opcodes.ASTORE, var.getMemLocation());
        } 
        
        printSymbolTable();
        return super.visitSetvar(ctx);
    }

    @Override
    /**
     * It will either print out the value of the identifier specified, or a string that is specified (Triggered whenever print is encountered)
     */
    public Object visitPrint(KnightCodeParser.PrintContext ctx){

        System.out.println("Enter Print");
        
        main_visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        if(ctx.ID() != null){   
            String varID = ctx.ID().getText();
            Variable var = symbol_Table.get(varID);
            int location = var.getMemLocation();

            if (var.getType().equals("INTEGER")){
                main_visitor.visitVarInsn(Opcodes.ILOAD, location);
                main_visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            }
            else{
                main_visitor.visitVarInsn(Opcodes.ALOAD, location);
                main_visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }
        }
        else if(ctx.STRING()!=null){
            String str = removeFirstandLast(ctx.STRING().getText());
            main_visitor.visitLdcInsn(str);
            main_visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }
        return super.visitPrint(ctx);
    }
    
    @Override
    /**
     * Reads an input from the user and store it in the variable whose identifier follows the read call 
     */
    public Object visitRead(KnightCodeParser.ReadContext ctx){

        System.out.println("Entering Read");

        Variable var = symbol_Table.get(ctx.ID().getText());
        int scanLocation = memory_pointer++;

        main_visitor.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        main_visitor.visitInsn(Opcodes.DUP); 
        main_visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        main_visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        main_visitor.visitVarInsn(Opcodes.ASTORE, scanLocation);

        if (var.getType().equals("INTEGER")){

            main_visitor.visitVarInsn(Opcodes.ALOAD, scanLocation);
            main_visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
            main_visitor.visitVarInsn(Opcodes.ISTORE, var.getMemLocation());
        }
        else if (var.getType().equals("STRING")){
            
            main_visitor.visitVarInsn(Opcodes.ALOAD, scanLocation); 
            main_visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false);
            main_visitor.visitVarInsn(Opcodes.ASTORE, var.getMemLocation()); 
        }

        return super.visitRead(ctx);
    }
    
    @Override
    /**
     * Handles the logic for a while loop
     */
    public Object visitLoop(KnightCodeParser.LoopContext ctx){
        
        Label beginLoop = new Label(); 
        Label endLoop = new Label(); 
        
        
        main_visitor.visitLabel(beginLoop);

        String num1 = ctx.getChild(1).getText();
        String num2 = ctx.getChild(3).getText();

        loadInteger(num1);
        loadInteger(num2);

        String op = ctx.comp().getText();

        switch (op) {
            case ">":
                System.out.println("LE");
                main_visitor.visitJumpInsn(Opcodes.IF_ICMPLE, endLoop);
                break;
        
            case "<":
                System.out.println("GE");
                main_visitor.visitJumpInsn(Opcodes.IF_ICMPGE, endLoop);
                break;

            case "=":
                System.out.println("NEQ");  
                main_visitor.visitJumpInsn(Opcodes.IF_ICMPNE, endLoop);
                break;
            case "<>":
                System.out.println("EQ");
                main_visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, endLoop);
                break;
            default:
                System.err.println("ERROR: Comparison operator not recongnized.");
                System.exit(1);
        }

        for(int i = 5; i<ctx.getChildCount(); i++){
            visit(ctx.getChild(i));
        }

        main_visitor.visitJumpInsn(Opcodes.GOTO, beginLoop);
        main_visitor.visitLabel(endLoop);

        return null;
    }

    /**
     * Removes the first and last characters of a string
     * @param s the string that will be modified
     * @return the modified string without the 1st and last characters
     */
    public String removeFirstandLast(String s){
        return s.substring(1, s.length() -1);
    }

    /**
     * Writes from an array to a file
     * @param bytearray to be modified 
     * @param fileName output file with information
     */
    public static void writeFile(byte[] bytearray, String fileName){

        try{
            FileOutputStream out = new FileOutputStream(fileName);
            out.write(bytearray);
            out.close();
        }
        catch(IOException e){
        System.out.println(e.getMessage());
        }
    }

    /**
     * Prints contents of the Symbol Table
     */
    public void printSymbolTable(){
        System.out.println("SymbolTable");
        for (Map.Entry<String, Variable> entry : symbol_Table.entrySet()){
            System.out.println("Key: " + entry.getKey() + " Var: " + entry.getValue().toString());
        }
    }


}
 