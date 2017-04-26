package br.com.persistor.generalClasses;

import java.util.Date;

import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.MATCH_MODE;
import br.com.persistor.enums.ORDER_MODE;
import java.beans.Expression;

public class Restrictions
{

    /**
     * (...) column = value (...)
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param value espected value
     * @return
     */
    public static Expressions eq(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        if (value instanceof String)
            baseCondition += column + " = '" + value + "' ";
        else
            baseCondition += column + " = " + value + " ";
        
        return new Expressions(baseCondition);
    }

    /**
     * (...) column != value (...)
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param value espected value
     * @return
     */
    public static Expressions ne(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        if (value instanceof String)
            baseCondition += column + " <> '" + value + "' ";
        else
            baseCondition += column + " <> " + value + " ";
        
        return new Expressions(baseCondition);
    }

    /**
     * (...) column = is null (...)
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @return
     */
    public static Expressions isNull(FILTER_TYPE filter_type, String column)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
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

    /**
     * (...) column in (1, 3, 5) (...)
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param values in values
     * @return
     */
    public static Expressions in(FILTER_TYPE filter_type, String column, String[] values)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
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
     * (...) column not in (1, 3, 5) (...)
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param values not in values
     * @return
     */
    public static Expressions notIn(FILTER_TYPE filter_type, String column, String[] values)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
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
        return new Expressions(baseCondition + column + " NOT IN (" + in + ")");
    }
    
    /**
     * (...) column is not null (...)
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @return
     */
    public static Expressions isNotNull(FILTER_TYPE filter_type, String column)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
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
     * This result in a EXACT like expression
     *
     * (...) column like 'search term'
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param value espected value
     * @return
     */
    public static Expressions like(FILTER_TYPE filter_type, String column, String value)
    {
        value = value.replace("'", "");
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
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
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param value espected value
     * @param matchMode Used to specify how to match specified values
     *
     * Avainable Match Modes:
     *
     * ANYWHERE: Any part in String. Ex.: '%search term%'
     *   Ex.: '%search term' 
     * EXACT: Exact match. 
     *   Ex.: 'search term'
     * START: 
     *  start. 
     *   Ex.: 'search term%'
     *
     * @return
     */
    public static Expressions like(FILTER_TYPE filter_type, String column, String value, MATCH_MODE matchMode)
    {
        value = value.replace("'", "");
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
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
     * (...) column between value1 and value2 (...)
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param value1 initial value
     * @param value2 final value
     * @return
     */
    public static Expressions between(FILTER_TYPE filter_type, String column, Object value1, Object value2)
    {
        String baseCondition = "";

        switch (filter_type)
        {
            case WHERE:

                baseCondition = " where ";
                break;

            case AND:

                baseCondition = " AND ";
                break;

            case OR:

                baseCondition = " OR ";
                break;
        }

        if (value2 instanceof Date || value1 instanceof Date || value1 instanceof String || value2 instanceof String)
            baseCondition += column + " BETWEEN '" + value1 + "' AND '" + value2 + "' ";
        else
            baseCondition += column + " BETWEEN " + value1 + " AND " + value2 + " ";

        return new Expressions(baseCondition);
    }

    /**
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param value espected value
     * @return
     */
    public static Expressions gt(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        if (value instanceof Number)
        {
            switch (filter_type)
            {
                case WHERE:

                    baseCondition = " where ";
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
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param value espected value
     * @return
     */
    public static Expressions it(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        if (value instanceof Number)
        {
            switch (filter_type)
            {
                case WHERE:

                    baseCondition = " where ";
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
     *
     * @param filter_type Enum WHERE / OR / AND
     * @param column column in table
     * @param value espected value
     * @return
     */
    public static Expressions ge(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        if (value instanceof Number)
        {
            switch (filter_type)
            {
                case WHERE:

                    baseCondition = " where ";
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
     *
     * filter_type Enum WHERE / OR / AND @param column column in table @param
     * value espected value
     */
    public static Expressions le(FILTER_TYPE filter_type, String column, Object value)
    {
        String baseCondition = "";

        if (value instanceof Number)
        {
            switch (filter_type)
            {
                case WHERE:

                    baseCondition = " where ";
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
     * (...) order by column ASC / DESC (...)
     *
     * @param column column in table
     * @param order_mode type or ordenation (ASC / DESC)
     * @return
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
