/*
 * To change this license header; choose License Headers in Project Properties.
 * To change this template file; choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

/**
 *
 * @author Marcos Vinícius
 */
public class PersistenceLog
{
    String className;
    String methodName;
    String date;
    String description;
    String query;

    public PersistenceLog(String className, String methodName, String date, String description, String query)
    {
        this.className = className;
        this.methodName = methodName;
        this.date = date;
        this.description = description;
        this.query = query;
    }
    
    public String getClassName()
    {
        return className;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public String getDate()
    {
        return date;
    }

    public String getDescription()
    {
        return description;
    }

    public String getQuery()
    {
        return query;
    }
}
