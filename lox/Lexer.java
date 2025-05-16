package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import lox.TokenType;

public class Lexer 
{
    private final String m_source; // source file 
    private final List<Token> m_tokens = new ArrayList<>(); // list of tokens
    private int m_start = 0; // to track start of a lexeme
    private int m_current = 0; // to track current position in lexeme
    private int m_line = 1; // to track line we are in

    private static final Map<String,TokenType> m_keywords;
    static
    {
        m_keywords = new HashMap<>();
        m_keywords.put("and",    TokenType.AND);
        m_keywords.put("class",  TokenType.CLASS);
        m_keywords.put("else",   TokenType.ELSE);
        m_keywords.put("false",  TokenType.FALSE);
        m_keywords.put("for",    TokenType.FOR);
        m_keywords.put("fun",    TokenType.FUN);
        m_keywords.put("if",     TokenType.IF);
        m_keywords.put("nil",    TokenType.NIL);
        m_keywords.put("or",     TokenType.OR);
        m_keywords.put("print",  TokenType.PRINT);
        m_keywords.put("return", TokenType.RETURN);
        m_keywords.put("super",  TokenType.SUPER);
        m_keywords.put("this",   TokenType.THIS);
        m_keywords.put("true",   TokenType.TRUE);
        m_keywords.put("var",    TokenType.VAR);
        m_keywords.put("while",  TokenType.WHILE);
    }

    Lexer(String l_source)
    {
        this.m_source = l_source;
    }
    /**
     * @return A list of tokens for parser to process
     */
    List<Token> scanTokens()
    {
        while(!isAtEnd())
        {
            m_start = m_current;
            scanToken();
        }

        m_tokens.add(new Token(TokenType.EOF,"",null,m_line));

        return m_tokens;
    }

    /**
     * Scan the cources and turns them into tokens
     */
    private void scanToken()
    {
        char c = advance(); // get the next character in source 
        switch(c)
        {
            case ' ' :
            case '\t':
            case '\r': break;
            case '\n': m_line++;break;
            case '(' : addToken(TokenType.LEFT_PAREN);break;
            case ')' : addToken(TokenType.RIGHT_PAREN);break;
            case '{' : addToken(TokenType.LEFT_BRACE);break;
            case '}' : addToken(TokenType.RIGHT_BRACE);break;
            case '+' : addToken(TokenType.PLUS);break;
            case '-' : addToken(TokenType.MINUS);break;
            case '*' : addToken(TokenType.STAR);break;
            case ';' : addToken(TokenType.SEMICOLON);break;
            case ',' : addToken(TokenType.COMMA);break;
            case '.' : addToken(TokenType.DOT);break;
            case '!' : 
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.EQUAL);
                break;
            case '<' : 
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.EQUAL);
                break;
            case '>' : 
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.EQUAL);
                break;
            case '=' : 
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '/' :
                if(match('/'))
                {
                    // comment will go untill the end of the line
                    while(peek() != '\n' && !isAtEnd()) advance();
                }
                else
                {
                    addToken(TokenType.SLASH);
                }
                break;
            case '"' : string();break;

            default : 
                if(isDigit(c))
                {
                    number();
                }
                else if(isAlpha(c))
                {
                    identifier();
                }
                else
                {Lox.error(m_line,"Unexpected character.");}
                break;
        }
    }

    /**
     * Consumes the character i.e increments m_currnet
     * @return The next charater in the source
     */
    private char advance()
    {
        m_current++;
        return m_source.charAt(m_current-1);
    }

    private void addToken(TokenType l_type)
    {
        // as you can see we are overloading by arguments
        addToken(l_type,null);
    }

    private void addToken(TokenType l_type, Object l_literal)
    {
        String l_text = m_source.substring(m_start,m_current);
        m_tokens.add(new Token(l_type,l_text,l_literal,m_line));
    }

    private boolean isAtEnd()
    {
        return m_current >= m_source.length();
    }

    /**
     * 
     * @param l_expected
     * @return true if the next charater is equal to give char
     */
    private boolean match(char l_expected)
    {
        if(isAtEnd()) return false;
        if(m_source.charAt(m_current) != l_expected) return false;

        m_current++;
        return true;
    }

    /**
     * @return The next character 
     */
    private char peek()
    {
        if(isAtEnd()) return '\0';
        return m_source.charAt(m_current);
    }

    /** 
     * Keeps consuming the string utill closing '"'
     */
    private void string()
    {
        while(peek() != '"' && !isAtEnd())
        {
            if(peek() == '\n')m_line++;
            advance();
        }

        if(isAtEnd())
        {
            Lox.error(m_line,"Unterminated string.");
            return;
        }

        // to capture the closing "
        advance();

        //now we exclude the surronunding " "
        String value = m_source.substring(m_start + 1, m_current -1);
        addToken(TokenType.STRING, value);
    }

    /**
     * check whether given character is a digit
     */
    private boolean isDigit(char c)
    {
        return (c >= '0' && c <= '9');
    }

    /**
     * Keep cosuming the number
     */
    private void number()
    {
        while(isDigit(peek())) advance();

        // we also look for the part after decimal
        if(peek() == '.' && isDigit(peekNext()))
        {
            //just consume the '.'
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER,
        Double.parseDouble(m_source.substring(m_start,m_current)));
    }

    private char peekNext()
    {
        if(m_current + 1 >= m_source.length())return '\0';
        return m_source.charAt(m_current+1);
    }

    private boolean isAlpha(char c)
    {
        return (c >= 'a' && c <= 'z')||
               (c >= 'A' && c <= 'Z')||
               c == '_';
    }

    private boolean isAlphaNumeric(char c)
    {  
        return isAlpha(c) || isDigit(c);
    }

    private void identifier()
    {
        while(isAlphaNumeric(peek())) advance();

        String text = m_source.substring(m_start,m_current);
        TokenType type = m_keywords.get(text);
        if(type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }
}
