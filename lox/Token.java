package lox;

public class Token {
    final TokenType m_type;
    final String m_lexeme;
    final Object m_literal;
    final int m_line;

    Token(TokenType l_type, String l_lexeme, Object l_literal, int l_line)
    {
        this.m_type    = l_type;
        this.m_lexeme  = l_lexeme;
        this.m_literal = l_literal;
        this.m_line    = l_line;
    }
}
