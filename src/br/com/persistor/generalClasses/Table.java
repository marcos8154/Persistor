/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 *
 * @author marcosvinicius
 */
public class Table
{

    public static void create(Object obj)
    {
        try
        {
            String sql = "create table " + obj.getClass().getSimpleName().toLowerCase() + " \n(\n";
            for (Method method : obj.getClass().getMethods())
            {
                if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                {
                    String name = "";

                    if (method.getName().startsWith("is"))
                        name = method.getName().substring(2, method.getName().length());
                    else
                        name = method.getName().substring(3, method.getName().length());

                    name = name.toLowerCase();
                    sql += "    "+ name + " varchar(100) not null,\n";
                }
            }

            sql = sql.substring(0, sql.length() - 2);
            sql += "\n);";
            System.out.println(sql);
        }
        catch (Exception ex)
        {

        }
    }
}
