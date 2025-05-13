package lox;


public class Interpreter implements Expr.Visitor<Object> 
{
    void interpret(Expr expression)
    {
        try
        {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        }
        catch(RuntimeError error)
        {
            Lox.runtimeError(error);
        }
    }
    @Override
    public Object visitLiteralExpr(Expr.Literal expr)
    {
        return expr.value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.m_type)
        {
            case TokenType.MINUS :
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case TokenType.PLUS :
                if(left instanceof Double && right instanceof Double)
                { return (double)left + (double)right; }
                if(left instanceof String && right instanceof String)
                { return (String)left + (String)right; } 
                // if they are neither then we throw the exception
                throw new RuntimeError(expr.operator, 
                "Operands must be numbers or two strings");
            case TokenType.SLASH :
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case TokenType.STAR :
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case TokenType.GREATER :
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case TokenType.GREATER_EQUAL :
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case TokenType.LESS :
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case TokenType.LESS_EQUAL :
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case TokenType.BANG_EQUAL :
                return !isEqual(left,right);
                // we dont have to check these two because if the 
                // are not number then fuck it we just return false
            case TokenType.EQUAL_EQUAL :
                return isEqual(left,right);
            
            default:
                return 0; // again just a placeholder for now
        }
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Object right = evaluate(expr.right);

        switch(expr.operator.m_type)
        {
            case TokenType.BANG:
                return !isTruthy(right);
            case TokenType.MINUS:
                chekcNumberOperand(expr.operator, right);
                return -(double)right;
            default : return 0; // for now only later we'll raise an error
        }

        //return null; // this is unreachable
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr)
    {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object)
    {
        // null and false are false and everything else it true
        if(object  == null) return false;
        if(object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b)
    {
        if(a == null && b == null) return true;
        if(a == null) return false;

        return a.equals(b);
    }

    private void chekcNumberOperand(Token operator, Object operand)
    {
        if(operand instanceof Double) return;
        throw new RuntimeError(operator,"Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if(left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers");
    }

    private String stringify(Object object)
    {
        if(object == "null") return "nil";

        if(object instanceof Double)
        {
            String text = object.toString();
            if(text.endsWith(".0"))
            {
                text = text.substring(0,text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}


