/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import br.com.persistor.annotations.OneToMany;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.connectionManager.DataSource;
import br.com.persistor.enums.RESULT_TYPE;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author Marcos Vinícius
 */
public class FieldHandled
{

    private FieldHandled()
    {

    }

    static String toCamelCase(String string)
    {
        StringBuffer sb = new StringBuffer(string);
        sb.replace(0, 1, string.substring(0, 1).toUpperCase());
        return sb.toString();

    }

    public Object oneToOneLoad(Object obj, String fieldName)
    {
        SessionImpl session = null;
        try
        {
            session = new SessionImpl(DataSource.getInstanceByDefaultDbConfig().getConnection(),
            DataSource.defaultDbConfig);

            Method method = obj.getClass().getMethod("get" + FieldHandled.toCamelCase(fieldName));
            Class entityToLoadClass = method.getReturnType();

            OneToOne oneToOne = method.getAnnotation(OneToOne.class);
            Field field = obj.getClass().getDeclaredField(oneToOne.source());
            field.setAccessible(true);
            int id = (int) field.get(obj);

            Object entity = session.onID(entityToLoadClass, id);
            session.close();
            return entity;

        }
        catch (Exception ex)
        {
            if(session != null)
                session.close();
            ex.printStackTrace();
            return null;
        }
    }

    public Object oneToManyLoad(Object obj, String fieldName)
    {
        SessionImpl session = null;
        try
        {
            session = new SessionImpl(DataSource.getInstanceByDefaultDbConfig().getConnection(),
            DataSource.defaultDbConfig);

            Method method = obj.getClass().getMethod("get" + FieldHandled.toCamelCase(fieldName));
            Class entityToLoadClass = method.getReturnType();

            OneToMany oneToMany = (OneToMany) method.getAnnotation(OneToMany.class);
            
            Field fieldSource = obj.getClass().getDeclaredField(oneToMany.source());
            fieldSource.setAccessible(true);
            
            Object valueSource = fieldSource.get(obj);
            
            String queryCommand = "SELECT * FROM " + entityToLoadClass.getSimpleName().toLowerCase() + " where " + oneToMany.target() + " = " + valueSource;
            
            Object entityToLoad = entityToLoadClass.newInstance();
            Query query = session.createQuery(entityToLoad, queryCommand);
            query.setResult_type(RESULT_TYPE.MULTIPLE);
            query.execute();
            session.close();
            
            return entityToLoad;
        }
        catch (Exception ex)
        {
            if(session != null)
                session.close();
            ex.printStackTrace();
            return null;
        }
    }

    public static Object readObject(Object obj, String fieldName)
    {
        try
        {
            Method method = obj.getClass().getMethod("get" + toCamelCase(fieldName));
            if (method.isAnnotationPresent(OneToOne.class))
                return new FieldHandled().oneToOneLoad(obj, fieldName);
            if (method.isAnnotationPresent(OneToMany.class))
                return new FieldHandled().oneToManyLoad(obj, fieldName);
        }
        catch (Exception ex)
        {

        }

        return null;
    }
}
