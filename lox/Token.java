package lox;

public class Token {
    final TokenType m_type; // type of the token
    final String m_lexeme; // the lexeme of the token
    final Object m_literal; // literal is the token has a literal
    final int m_line; // the line wehere we see the lexeme

    Token(TokenType l_type, String l_lexeme, Object l_literal, int l_line)
    {
        this.m_type    = l_type;
        this.m_lexeme  = l_lexeme;
        this.m_literal = l_literal;
        this.m_line    = l_line;
    }
}
