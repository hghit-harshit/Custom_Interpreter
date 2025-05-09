package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;


/**
 * A class to generater out expr class
 */
public class GenerateAST 
{
    public static void main(String[] args) throws IOException
    {
        if(args.length != 1)
        {
            System.err.println("Usage : generate_ast <output directory>" );
            System.exit(64);
        }

        String outputDir = args[0];
        defineAST(outputDir, "Expr", Arrays.asList(
         "Binary : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right"
        ));
    }
    
    private static void defineAST(
        String outputDir, String baseName, List<String> types)
        throws IOException
    {
        String path = outputDir + "/" + baseName +".java";
        PrintWriter writer = new PrintWriter(path,"UTF-8");

        writer.println("package lox");
        writer.println();
        writer.println("import java.utils.List");
        writer.println();
        writer.println("abstract class " + baseName + "{");

        for(String type : types)
        {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer,baseName,className,fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(
        PrintWriter writer, String baseName, String className, String fieldList)
    {
        writer.println(" static class " + className + " extends " + baseName + "{");

        //Constructor
        writer.println("    " + className + "(" + fieldList + ") {");

        //Store parameters in fields
        String[] fields = fieldList.split(",");
        
        for(String field : fields)
        {
            System.out.println(field);
            String name = field.trim().split(" ")[1];
            System.out.println(name);
            writer.println("    this." + name + " = " + name + ";");
            
        }

        writer.println("    }");

        //now we define the field in the class
        writer.println();
        for(String field : fields)
        {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }
}
