/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcos Vinícius
 */
public class CodeFirstTableDomain
{
    private Class clazz;
    private List<ColumnProperties> properties;
    
    public CodeFirstTableDomain(Class clazz)
    {
        this.clazz = clazz;
        this.properties = new ArrayList<>();
    }
    
    public Class getDomainClass()
    {
        return this.clazz;
    }
    
    public ColumnProperties getPropertiesForColumn(String columnName)
    {
        for(ColumnProperties cp : properties)
            if(cp.getColumnName().equals(columnName))
                return cp;
        
        return null;
    }
    
    public CodeFirstTableDomain setColumnProperty(String columnName, boolean nullable, int length, int decimalDigits, Object defaultValue)
    {
        properties.add(new ColumnProperties(columnName, nullable, length, decimalDigits, defaultValue));
        return this;
    }
}
