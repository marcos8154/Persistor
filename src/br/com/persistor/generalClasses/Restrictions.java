package br.com.persistor.generalClasses;

import java.util.Date;

import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.MATCH_MODE;
import br.com.persistor.enums.ORDER_MODE;

public class Restrictions
{

    /**
     * Represents the equals expression, "="
     */
    public static Expressions eq(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " WHERE ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        if (value instanceof Number)
        {
            baseCondition += column + " = " + value + " ";
        }
        if (value instanceof String)
        {
            baseCondition += column + " = '" + value + "' ";
        }

        return new Expressions(baseCondition);
    }

    /**
     * Representes Not Equals expression, "<>"
     */
    public static Expressions ne(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " WHERE ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        if (value instanceof Number)
        {
            baseCondition += column + " <> " + value + " ";
        }
        if (value instanceof String)
        {
            baseCondition += column + " <> '" + value + "' ";
        }

        return new Expressions(baseCondition);
    }

    /**
     * Represents the Is Null expression, "Is Null"
     */
    public static Expressions isNull(FILTER_TYPE filter_type, String column)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " WHERE ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        return new Expressions(baseCondition + column + " IS NULL ");
    }

    public static Expressions in(FILTER_TYPE filter_type, String column, String[] values)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " WHERE ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        String in = "";
        for (int i = 0; i < values.length; i++)
        {
            in += values[i] + ", ";
        }
        
        in = in.substring(0, in.length() - 2);
        return new Expressions(baseCondition + column + " IN (" + in + ")");
    }

    /**
     * Represents the Is Not Null expression, "Is Not Null"
     */
    public static Expressions isNotNull(FILTER_TYPE filter_type, String column)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " WHERE ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        return new Expressions(baseCondition + column + " IS NOT NULL ");
    }

    /**
     * Represents the Exact Like expression, "LIKE 'term'"
     */
    public static Expressions like(FILTER_TYPE filter_type, String column, String value)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " WHERE ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        baseCondition += column + " LIKE '" + value + "' ";

        return new Expressions(baseCondition);
    }

    /**
     * Represents the Like expression. It can be customized with the parameter
     * enum MatchMode
     */
    public static Expressions like(FILTER_TYPE filter_type, String column, String value, MATCH_MODE matchMode)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " WHERE ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        switch (matchMode)
        {
            case ANYWHERE:

                value = "%" + value + "%";
                break;

            case START:

                value += "%";
                break;

            case END:

                value = "%" + value;
                break;
        }

        baseCondition += column + " LIKE '" + value + "' ";

        return new Expressions(baseCondition);
    }

    /**
     * Represents the Between expression, "Between 50 AND 60"
     */
    public static Expressions between(FILTER_TYPE filter_type, String column, Object value1, Object value2)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " WHERE ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        if (value2 instanceof Date || value1 instanceof Date || value1 instanceof String || value2 instanceof String)
        {
            baseCondition += column + " BETWEEN '" + value1 + "' AND '" + value2 + "' ";
        }
        else
        {
            baseCondition += column + " BETWEEN " + value1 + " AND " + value2 + " ";
        }

        return new Expressions(baseCondition);
    }

    /**
     * Represents More Than expression. ">"
     */
    public static Expressions gt(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        if (value instanceof Number)
        {
            switch (filter_type)
            {
                case WHERE:

                    baseCondition = " WHERE ";
                    break;

                case AND:

                    baseCondition = " AND ";
                    break;

                case OR:

                    baseCondition = " OR ";
                    break;
            }

            baseCondition += column + " > " + value + " ";

        }
        else
        {
            System.err.println("Persistor: \n Restrictions.gt: only numeric types are allowed");
            return null;
        }

        return new Expressions(baseCondition);
    }

    /**
     * Represents Less Than expression. "<"
     */
    public static Expressions it(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        if (value instanceof Number)
        {
            switch (filter_type)
            {
                case WHERE:

                    baseCondition = " WHERE ";
                    break;

                case AND:

                    baseCondition = " AND ";
                    break;

                case OR:

                    baseCondition = " OR ";
                    break;
            }

            baseCondition += column + " < " + value + " ";

        }
        else
        {
            System.err.println("Persistor: \n Expressions.it: only numeric types are allowed");
            return null;
        }

        return new Expressions(baseCondition);
    }

    /**
     * Represents greater than or equal to expression. ">="
     */
    public static Expressions ge(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        if (value instanceof Number)
        {
            switch (filter_type)
            {
                case WHERE:

                    baseCondition = " WHERE ";
                    break;

                case AND:

                    baseCondition = " AND ";
                    break;

                case OR:

                    baseCondition = " OR ";
                    break;
            }

            baseCondition += column + " >= " + value + " ";
        }
        else
        {
            System.err.println("Persistor: \n Expressions.ge: only numeric types are allowed");
        }

        return new Expressions(baseCondition);
    }

    /**
     * Represents less than or equal to expression. "<="
     */
    public static Expressions le(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        if (value instanceof Number)
        {
            switch (filter_type)
            {
                case WHERE:

                    baseCondition = " WHERE ";
                    break;

                case AND:

                    baseCondition = " AND ";
                    break;

                case OR:

                    baseCondition = " OR ";
                    break;
            }

            baseCondition += column + " <= " + value + " ";
        }
        else
        {
            System.err.println("Persistor: \n Expressions.le: only numeric types are allowed");
            return null;
        }

        return new Expressions(baseCondition);
    }

    /**
     * Represents the Order By expression. It can be customized with the
     * parameter enum ORDER_MODE
     */
    public static Expressions OrderBy(String column, ORDER_MODE order_mode)
    {
        String baseCondition = "";
        switch (order_mode)
        {
            case ASC:

                baseCondition = " ORDER BY " + column + " ASC ";
                break;

            case DESC:

                baseCondition = " ORDER BY " + column + " DESC ";
                break;
        }

        return new Expressions(baseCondition);
    }
}
