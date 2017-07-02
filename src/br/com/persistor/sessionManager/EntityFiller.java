/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.interfaces.Session;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;

/**
 *
 * @author Marcos Vinícius
 */
public class EntityFiller
{

    public void fillEntity(Method method, ResultSet resultSet,
            String methodSetName, String columnName,
            Object baseEntity, Object targetEntity, Session iSession) throws Exception
    {
        if (method.getReturnType() == char.class)
        {
            String str = resultSet.getString(columnName);
            if (str == null)
                return;
            if (str.length() > 0)
            {
                Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, char.class);
                invokeMethod.invoke(targetEntity, str.charAt(0));
            }
        }

        if (method.getReturnType() == boolean.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, boolean.class);
            invokeMethod.invoke(targetEntity, resultSet.getBoolean(columnName));
        }

        if (method.getReturnType() == int.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, int.class);
            invokeMethod.invoke(targetEntity, resultSet.getInt(columnName));
        }

        if (method.getReturnType() == double.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, double.class);
            invokeMethod.invoke(targetEntity, resultSet.getDouble(columnName));
        }

        if (method.getReturnType() == float.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, float.class);
            invokeMethod.invoke(targetEntity, resultSet.getFloat(columnName));
        }

        if (method.getReturnType() == short.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, short.class);
            invokeMethod.invoke(targetEntity, resultSet.getShort(columnName));
        }

        if (method.getReturnType() == long.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, long.class);
            invokeMethod.invoke(targetEntity, resultSet.getLong(columnName));
        }

        if (method.getReturnType() == String.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, String.class);
            invokeMethod.invoke(targetEntity, resultSet.getString(columnName));
        }

        if (method.getReturnType() == java.util.Date.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, java.util.Date.class);
            invokeMethod.invoke(targetEntity, resultSet.getDate(columnName));
        }

        if (method.getReturnType() == byte.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, byte.class);
            invokeMethod.invoke(targetEntity, resultSet.getByte(columnName));
        }

        if (method.getReturnType() == BigDecimal.class)
        {
            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, BigDecimal.class);
            invokeMethod.invoke(targetEntity, resultSet.getBigDecimal(columnName));
        }

        if (method.getReturnType() == InputStream.class)
        {
            InputStream is;
            if (iSession.getConfig().getDb_type() == DB_TYPE.SQLServer)
                is = resultSet.getBlob(columnName).getBinaryStream();
            else
                is = resultSet.getBinaryStream(columnName);

            Method invokeMethod = baseEntity.getClass().getMethod(methodSetName, InputStream.class);
            invokeMethod.invoke(targetEntity, is);
        }
    }
}
