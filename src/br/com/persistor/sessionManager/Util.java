/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Marcos Vinícius
 */
public class Util
{

    public static String getFullStackTrace(Exception ex)
    {
        String result = ex.getMessage() + "\n";
        for (StackTraceElement stackTrace : ex.getStackTrace())
        {
            result += stackTrace + "\n";
        }
        return result;
    }

    public static String getDateTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static boolean isNumber(Method method)
    {
        if (method.getReturnType() == int.class)
        {
            return true;
        }
        if (method.getReturnType() == double.class)
        {
            return true;
        }
        if (method.getReturnType() == float.class)
        {
            return true;
        }
        if (method.getReturnType() == short.class)
        {
            return true;
        }
        if (method.getReturnType() == long.class)
        {
            return true;
        }

        return false;
    }

    public static boolean extendsEntity(Class cls)
    {
        for (Field field : cls.getFields())
        {
            if (field.getName() == "saved")
            {
                return true;
            }
        }

        return false;
    }

    public static boolean methodHasValue(Object obj, String methodName) throws Exception
    {
        try
        {
            Method method;
            if (methodName.startsWith("is"))
            {
                methodName = "is" + methodName.substring(2, methodName.length());
                method = obj.getClass().getMethod(methodName);
            }
            else
            {
                methodName = "get" + methodName.substring(3, methodName.length());
                method = obj.getClass().getMethod(methodName);
            }

            Object value = method.invoke(obj);

            if (value != null && (int) value != 0)
            {
                return true;
            }

        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            throw new Exception(ex.getMessage());
        }

        return false;
    }
}
