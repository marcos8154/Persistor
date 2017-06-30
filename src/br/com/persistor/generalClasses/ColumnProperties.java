/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

/**
 *
 * @author Marcos Vinícius
 */
public class ColumnProperties
{

    private String columnName;
    private boolean nullable;
    private int length;
    private int decimalDigits;
    private Object defaultValue;
    private String columnType;

    public ColumnProperties(String columnName,
            boolean nullable, int length, int decimalDigits, Object defaultValue)
    {
        this.columnName = columnName;
        this.nullable = nullable;
        this.length = length;
        this.decimalDigits = decimalDigits;
        this.defaultValue = defaultValue;
    }

    public static ColumnProperties get(String columnName,
            String columnType, boolean nullable, int length, int decimalDigits,
            Object defaultValue)
    {
        ColumnProperties cp = new ColumnProperties(columnName, nullable,
                length, decimalDigits, defaultValue);
        cp.setColumnType(columnType);
        return cp;
    }

    public String getColumnDescription()
    {
        String result = columnName + " " + columnType;

        if (length > 0)
        {
            if (length > 0 && decimalDigits > 0)
                result += " (" + length + ", " + decimalDigits + ") ";
            else
                result += " (" + length + ") ";
        }

        if (!nullable)
            result += " not null ";

        if (defaultValue != null)
            if (defaultValue.toString().isEmpty())
                result += "default ''";
            else
                result += "default " + defaultValue;

        return result;
    }

    public String getColumnType()
    {
        return columnType;
    }

    public void setColumnType(String columnType)
    {
        this.columnType = columnType;
    }

    public int getDecimalDigits()
    {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits)
    {
        this.decimalDigits = decimalDigits;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public boolean isNullable()
    {
        return nullable;
    }

    public void setNullable(boolean nullable)
    {
        this.nullable = nullable;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue)
    {
        this.defaultValue = defaultValue;
    }

}
