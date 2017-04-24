/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.annotations.PrimaryKey;

/**
 *
 * @author Marcos Vinícius
 */
public class ColumnKey
{

    private String columnName;
    private PrimaryKey key;

    public ColumnKey(String columnName, PrimaryKey key)
    {
        this.columnName = columnName;
        this.key = key;
    }
    
    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public PrimaryKey getKey()
    {
        return key;
    }

    public void setKey(PrimaryKey key)
    {
        this.key = key;
    }

}
