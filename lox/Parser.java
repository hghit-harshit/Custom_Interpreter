package lox;

import java.util.List;


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
    Expr parse()
    {
        try
        {
            return expression();
        }
        catch(ParseError error)
        {
            return null;
        }
    }
    private Expr expression()
    {
        return equality();
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

} 
