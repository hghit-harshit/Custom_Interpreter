package lox;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import lox.TokenType;

// each rule willl become a method inside this class
public class Parser 
{
    private static class ParseError extends RuntimeException{}
    
    private final List<Token> m_tokens;// list of tokens
    private int m_current = 0;

    Parser(List<Token> l_tokens)
    {
        this.m_tokens = l_tokens;
    }

    //this is the entry point of parsing the tokens
    // our descent begin from here
    List<Stmt> parse()
    {
        // try
        // {
        //     return expression();
        // }
        // catch(ParseError error)
        // {
        //     return null;
        // }

        List<Stmt> statements = new ArrayList<>();
        
        while(!isAtEnd())
        {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration()
    {
        try
        {
            if(match(TokenType.VAR)) return varDeclaration();
            //else
            return statement();
        }
        catch(ParseError error)
        {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration()
    {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if(match(TokenType.EQUAL))
        {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");

        return new Stmt.Var(name, initializer);
    }

    private Stmt statement()
    {
        if(match(TokenType.PRINT)) return printStatement();
        if(match(TokenType.IF)) return IfStatement(); 
        if(match(TokenType.FOR)) return ForStatement();
        if(match(TokenType.WHILE)) return WhileStatement();
        if(match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());

        //else
        return expressionStatement();
    }

    private Stmt printStatement()
    {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    // if (condition) stmt1 else stm2;
    // i was thiking of adding curly braces but well see to that later
    private Stmt IfStatement()
    {
        consume(TokenType.LEFT_PAREN, "Expect '(' after if");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN,"Expect ')' after if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(TokenType.ELSE))
        {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // we are goint to 'desugar' for loop to while loop
    // meaning we are not goin to write a new syntax node for 'for loop'
    // we'll just delegate its work to while loop which we have already implemented
    private Stmt ForStatement()
    {
        consume(TokenType.LEFT_PAREN,"Expect '(' after 'for'.");

        Stmt initializer;
        if(match(TokenType.SEMICOLON))
        {
            initializer = null;
        }
        else if(match(TokenType.VAR))
        {
            initializer = varDeclaration();
        }
        else
        {
            initializer = expressionStatement();
        }

        Expr condition = null;
        // we are cheking and not matching because the first semicolon
        // is already eaten up so if the next token is not 
        // a semicolon then it must be the condition and we consume the condition
        // if it is a semicolon the the condition is ommitted
        if(!check(TokenType.SEMICOLON))
        {
            condition = expression();
        }

        // now we consume the second semicolon
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");


        Expr increment = null;
        // we do the same as we did for checking if the condition is ommitted 
        // for increment
        if(!check(TokenType.LEFT_PAREN))
        {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clause.");

        // now all that is left is the body
        Stmt body = statement();

        // if in not null we then we wrap body and increment in a block
        // we are basically adding increment to the end of the body
        if(increment != null)
        {
            body = new Stmt.Block(
                Arrays.asList(body, new Stmt.Expression(increment)));
        }

        // if user skipped condition then we assuem that the condition
        // is always true
        if(condition  == null) condition = new Expr.Literal(true);
        // then we build the loop using while loop 
        body = new Stmt.While(condition,body);

        // at last if we have the initialiser 
        // then we put it before the while loop
        if(initializer != null)
        {
            body = new Stmt.Block(Arrays.asList(initializer,body));
        }
        
        return body;
    }

    private Stmt WhileStatement()
    {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private List<Stmt> block()
    {
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd())
        {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after a block");
        return statements;
    }
    private Stmt expressionStatement()
    {
        Expr expr = expression();
        consume(TokenType.SEMICOLON,"Expect ';' after the expression.");
        return new Stmt.Expression(expr);
    }
    private Expr expression()
    {
        return assignment();
    }
    // assigning a new value to a previously defined variable
    private Expr assignment()
    {
        Expr expr = or();
        // if we are reassining
        // and if the syntax is corrent this expr would be an identifier(varialbe)
        if(match(TokenType.EQUAL))
        {
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable)
            {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or()
    {
        Expr expr = and();

        while(match(TokenType.OR))
        {
            Token operator = previous();
            Expr right = and();
            
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and()
    {
        Expr expr = equality();

        while(match(TokenType.AND))
        {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }
    //equality → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality()
    {
        Expr l_expr = comparision();
        
        // here we are looping for the (...)* part
        // we loop till we keep seeign the equality operators
        while(match(TokenType.BANG_EQUAL,TokenType.EQUAL_EQUAL))
        {
            Token l_operator = previous();
            Expr l_right = comparision();
            l_expr = new Expr.Binary(l_expr, l_operator, l_right);
        }

        return l_expr;
    }

    private Expr comparision()
    {
        Expr l_expr = term();

        while(match(TokenType.GREATER,TokenType.GREATER_EQUAL,TokenType.LESS,TokenType.LESS_EQUAL))
        {
            Token l_operator = previous();
            Expr l_right = term();
            l_expr = new Expr.Binary(l_expr, l_operator, l_right);
        }

        return l_expr;  
    }

    private Expr term()
    {
        Expr l_expr = factor();
        
        while(match(TokenType.MINUS,TokenType.PLUS))
        {
            Token l_operator = previous();
            Expr l_right = factor();
            l_expr = new Expr.Binary(l_expr,l_operator,l_right);
        }

        return l_expr;
    }

    private Expr factor()
    {
        Expr l_expr = unary();

        while(match(TokenType.SLASH, TokenType.STAR))
        {
            Token l_operator = previous();
            Expr l_right = unary();
            l_expr = new Expr.Binary(l_expr, l_operator, l_right);
        }

        return l_expr;
    }

    private Expr unary()
    {
        if(match(TokenType.BANG,TokenType.MINUS))
        {
            Token l_operator = previous();
            Expr l_right = unary();
            return new Expr.Unary(l_operator,l_right);
        }

        return primary();
    }

    private Expr primary()
    {
        if(match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.NIL)) return new Expr.Literal(null);

        if(match(TokenType.NUMBER,TokenType.STRING))
        {
            return new Expr.Literal(previous().m_literal);
        }
        if(match(TokenType.IDENTIFIER))
        {
            return new Expr.Variable(previous());
        }
        if(match(TokenType.LEFT_PAREN))
        {
            Expr l_expr = expression();
            consume(TokenType.RIGHT_PAREN,"Expect ')' after the expression");
            return new Expr.Grouping(l_expr);
        }

        throw error(peek(),"Except exprssion");
    }

    /**
     * This checks to see if the current token has any of the given types. If so, it
     * consumes the token and returns true. Otherwise, it returns false and leaves
     * the current token alone
     */
    private boolean match(TokenType... l_types)
    {
        for(TokenType type : l_types)
        {
            if(check(type))
            {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * @param l_type
     * @return true if current token is of given type
     */
    private boolean check(TokenType l_type)
    {
        if(isAtEnd()) return false;
        return peek().m_type == l_type;
    }

    /**
     * consumes the current token and returns it
     * that is increase the current token counter
     */
    private Token advance()
    {
        if(!isAtEnd())m_current++;
        return previous();
    }

    private boolean isAtEnd()
    {
        return peek().m_type == TokenType.EOF;
    }

    /**
     * 
     * @return Next token
     */
    private Token peek()
    {
        return m_tokens.get(m_current);
    }

    private Token previous()
    {
        return m_tokens.get(m_current - 1);
    }

    /**
     * checks to see if the next type is of expected type
     * thows an error exception if its not
     */
    private Token consume(TokenType l_type, String l_message)
    {
        if(check(l_type)) return advance();

        throw error(peek(),l_message);
    }

    private ParseError error(Token l_token, String l_message)
    {
        Lox.error(l_token, l_message);
        return new ParseError();
    }

    private void synchronize()
    {
        advance();

        while(!isAtEnd())
        {
            if(previous().m_type == TokenType.SEMICOLON) return;
            
            switch(peek().m_type)
            {
                case TokenType.CLASS :
                case TokenType.FUN:
                case TokenType.VAR:
                case TokenType.FOR:
                case TokenType.IF:
                case TokenType.WHILE:
                case TokenType.PRINT:
                case TokenType.RETURN: 
                return;
                default:
            }

            advance();
        }
    }
} 
