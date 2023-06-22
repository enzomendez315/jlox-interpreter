package lox_interpreter_java;

import java.util.List;

import lox_interpreter_java.Stmt.Print;

/*
 * This class is used to evaluate expressions and produce values 
 * using the syntax trees created by the parser.
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>
{
    /*
     * Takes in a series of statements and evaluates them.
     * If a runtime error is thrown, it is caught and dealt with.
     */
    public void interpret(List<Stmt> statements)
    {
        try
        {
            for (Stmt statement : statements)
            {
                execute(statement);
            }
        } 
        catch (RuntimeError error)
        {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) 
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type)
        {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;

                if (left instanceof String && right instanceof String)
                    return (String)left + (String)right;

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) 
    {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) 
    {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) 
    {
        Object right = evaluate(expr.right);

        switch (expr.operator.type)
        {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        return null;
    }

    /*
     * Evaluates an expression.
     */
    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    /*
     * Executes a statement.
     */
    private void execute(Stmt stmt)
    {
        stmt.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt)
    {
        evaluate(stmt.expression);

        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) 
    {
       Object value = evaluate(stmt.expression);
       System.out.println(stringify(value));

       return null;
    }

    /*
     * Checks that the operand is a number on which the operator
     * can be used.
     */
    private void checkNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Double)
            return;
        
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /*
     * Checks that the operands are numbers on which the operator
     * can be used.
     */
    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Double && right instanceof Double)
            return;
        
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /*
     * Returns false if the object is false or nil. 
     * Returns true for any other object.
     */
    private boolean isTruthy(Object object)
    {
        if (object == null)
            return false;
        
        if (object instanceof Boolean)
            return (boolean)object;

        return true;
    }

    /*
     * Checks if two objects are equal.
     */
    private boolean isEqual(Object a, Object b)
    {
        if (a == null && b == null)
            return true;
        
        if (a == null)
            return false;

        return a.equals(b);
    }

    /*
     * Returns the string representation of the object.
     */
    private String stringify(Object object)
    {
        if (object == null)
            return "nil";

        if (object instanceof Double)
        {
            String text = object.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);

            return text;
        }

        return object.toString();
    }
}