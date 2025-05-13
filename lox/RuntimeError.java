package lox;

public class RuntimeError extends RuntimeException
{
    final Token m_token;

    RuntimeError(Token l_token, String l_message)
    {
        super(l_message);
        this.m_token = l_token;
    }
}
