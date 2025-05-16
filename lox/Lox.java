
package lox;

import java.io.BufferedReader;
//import java.io.IO;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



public class Lox
{
    private static final Interpreter m_interpreter = new Interpreter();
    static boolean m_hadError;
    static boolean m_hadRuntimeError;
    public static void main(String[] args) throws IOException
    {
        if (args.length > 1)
        {
            System.out.println("Usage jlox [script]");
            System.exit(64);
        }
        else if (args.length == 1)
        {
            runFile(args[0]);
        }
        else
        {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(m_hadError) System.exit(65);
        if(m_hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException
    {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader   = new BufferedReader(input);

        while(true)
        {
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null)break;
            run(line);
            m_hadError = false;
        }
    }

    private static void run(String source)
    {
        Lexer scanner = new Lexer(source);
        List<Token> tokens = scanner.scanTokens();

        for(Token token : tokens)
        {
            System.out.println(token.m_type);
        }
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Just printing the tokens for now
        // System.out.println(new ASTPrinter().print(expression));
        // if(m_hadError)return;

        m_interpreter.interpret(statements);
    }

    static void error(int l_line, String l_message)
    {
        report(l_line,"",l_message);
    }

    private static void report(int l_line, String l_where, String l_message)
    {
        System.err.println(
            "line[" + l_line +"] Error" + l_where + ": " + l_message);
        m_hadError = true;
    }

    static void error(Token l_token, String l_message)
    {
        if(l_token.m_type == TokenType.EOF)
        {
            report(l_token.m_line, " at end", l_message);
        }
        else
        {
            report(l_token.m_line, " at " + l_token.m_lexeme + "'", l_message);
        }
    }

    static void runtimeError(RuntimeError l_error)
    {
        System.err.println(l_error.getMessage() + 
        "\n[line " + l_error.m_token.m_line + "]");
        m_hadRuntimeError = true;
    }
}