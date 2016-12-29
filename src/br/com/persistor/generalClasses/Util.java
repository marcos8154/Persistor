package br.com.persistor.generalClasses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;

public class Util
{

    public static boolean extendsEntity(Class cls)
    {
        try
        {
            for (Field field : cls.getFields())
            {
                if (field.getName() == "saved")
                {
                    return true;
                }
            }

        }
        catch (Exception ex)
        {

        }
        return false;
    }

    public static void runPresentation()
    {
        JOptionPane.showMessageDialog(null, "Persistor - " + br.com.persistor.generalClasses.Util.getVersion(), "Version", 1);
    }

    public static String getVersion()
    {
        return "2.2.2 - Build 161229";
    }

    /**
     * Default format is yyyy-MM-dd
     *
     * @param dateFormat
     * @return
     */
    public static Date getDateFromFormat(String dateFormat)
    {
        try
        {
            Date d = new Date();
            DateFormat sdf1 = DateFormat.getDateTimeInstance();
            Date resultDate = sdf1.parse(sdf1.format(d));
            return resultDate;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getDateTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void closeStatement(Statement statement)
    {
        try
        {
            if (statement != null)
            {
                if (!statement.isClosed())
                    statement.close();

            }
        }
        catch (Exception ex)
        {
            // System.err.println("Persistor: error at: \n" + ex.getMessage());
        }
    }

    public static void closeResultSet(ResultSet resultSet)
    {
        try
        {
            if (resultSet != null)
            {
                if (!resultSet.isClosed())
                {
                    resultSet.close();
                }
            }
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: internal error at: \n" + ex.getMessage());
        }
    }

    public static void closePreparedStatement(PreparedStatement preparedStatement)
    {
        try
        {
            if (preparedStatement != null)
            {
                if (!preparedStatement.isClosed())
                {
                    preparedStatement.close();
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    public static String getFullStackTrace(Exception ex)
    {
        String result = ex.getMessage() + "\n";
        for (StackTraceElement stackTrace : ex.getStackTrace())
        {
            result += stackTrace + "\n";
        }
        return result;
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
