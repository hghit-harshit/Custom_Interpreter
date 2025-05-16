package lox;
import java.util.Map;
import java.util.HashMap;

public class Environment 
{
    final Environment enclosing; // reference to the 
    // environment that is enclosing this environment
    private final Map<String, Object> values = new HashMap<>();

    Environment()
    {
        enclosing = null;
        // only global environment's enclosing field would be null
        // as that is enclosing every other environment
        // but no one is enclosing him
    }

    Environment(Environment enclosing)
    {
        this.enclosing = enclosing;
    }

    void define(String name, Object value)
    {
        // notice we are allowing redefinition 
        // of a varialble because if we dont there would be inconsistency between 
        // REPL and scripts 
        // so var a = 1;
        // var a = 2; would work bad practice i know
        values.put(name,value);
    }

    /**
     * this well gets the value of the variable
     */
    Object get(Token name)
    {
        if(values.containsKey(name.m_lexeme))
        {
            return values.get(name.m_lexeme);
        }
        //If the variable isn’t found in this environment, we simply try the enclosing one.
        // That in turn does the same thing recursively, so this will ultimately walk the
        // entire chain.
        if(enclosing != null) return enclosing.get(name);

        // notice we made referring a undefined variale a 
        // runtime error rather than a static error
        // because If we make it a static error to mention a
        // variable before it’s been declared, it becomes much 
        //harder to define recursive
        //  functions
        throw new RuntimeError(name, "Undefined varialble"+
        name.m_lexeme + ".");
    }

    void assign(Token name, Object value)
    {
        if(values.containsKey(name.m_lexeme))
        {
            values.put(name.m_lexeme, value);
            return;
        }

        //same with assignment as we did with get we walk down the chain
        if(enclosing != null)
        {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, 
        "Undefined variable " + name.m_lexeme + ".");
    }
}
