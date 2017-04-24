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

    public ColumnProperties(String columnName, boolean nullable, int length, int decimalDigits, Object defaultValue)
    {
        this.columnName = columnName;
        this.nullable = nullable;
        this.length = length;
        this.decimalDigits = decimalDigits;
        this.defaultValue = defaultValue;
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
