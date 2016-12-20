/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import br.com.persistor.annotations.Column;
import br.com.persistor.annotations.NamedQuery;
import br.com.persistor.annotations.NamedQueryes;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.enums.COMMIT_MODE;
import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.PersistenceLog;
import br.com.persistor.generalClasses.Util;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import br.com.persistor.interfaces.Session;
import java.sql.Date;

/**
 *
 * @author marcosvinicius
 */
public class Query
{

    private PreparedStatement preparedStatement;
    private Session iSession;
    String query;

    private RESULT_TYPE result_type;
    private COMMIT_MODE commit_mode = COMMIT_MODE.AUTO;
    private boolean closeSessionAfterExecute;

    public boolean isCloseSessionAfterExecute()
    {
        return closeSessionAfterExecute;
    }

    public void setCloseSessionAfterExecute(boolean closeSessionAfterExecute)
    {
        this.closeSessionAfterExecute = closeSessionAfterExecute;
    }

    public COMMIT_MODE getCommit_mode()
    {
        return commit_mode;
    }

    public void setCommit_mode(COMMIT_MODE commit_mode)
    {
        this.commit_mode = commit_mode;
    }

    public RESULT_TYPE getResult_type()
    {
        return result_type;
    }

    public void setResult_type(RESULT_TYPE result_type)
    {
        this.result_type = result_type;
    }

    private Class cls;
    Object obj;

    public void createQuery(Session isession, Object obj, String query)
    {
        //if "query" starts with "@", is an NamedQuery.
        //Find in Class "cls" the NamedQuery
        this.cls = obj.getClass();
        this.obj = obj;
        this.iSession = isession;

        try
        {
            boolean isNamedQuery = false;
            if (cls.isAnnotationPresent(NamedQuery.class))
            {
                for (Annotation annotation : cls.getAnnotations())
                {
                    if (annotation instanceof NamedQuery)
                    {
                        NamedQuery namedQuery = (NamedQuery) annotation;
                        if (namedQuery.queryName().equals(query.replace("@", "")))
                        {
                            this.query = namedQuery.queryValue();
                            this.result_type = namedQuery.result_type();
                            this.closeSessionAfterExecute = namedQuery.closeAfterExecute();
                            isNamedQuery = true;
                            break;
                        }
                    }
                }
            }

            if (cls.isAnnotationPresent(NamedQueryes.class))
            {
                for (Annotation annotation : cls.getAnnotations())
                {
                    if (annotation instanceof NamedQueryes)
                    {
                        NamedQueryes namedQueryes = (NamedQueryes) annotation;

                        for (NamedQuery namedQuery : namedQueryes.value())
                        {
                            if (namedQuery.queryName().equals(query.replace("@", "")))
                            {
                                this.query = namedQuery.queryValue();
                                isNamedQuery = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!isNamedQuery)
                this.query = query;

            this.preparedStatement = isession.getActiveConnection().prepareStatement(this.query);
        }
        catch (Exception ex)
        {
            this.iSession.getPersistenceLogger().newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "void createQuery(Session isession, Object obj, String query)",
                            Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
    }

    public void setParameter(int parameter_index, Object value)
    {
        try
        {
            if (value == null)
            {
                preparedStatement.setObject(parameter_index, null);
            }

            if (value instanceof String)
            {
                preparedStatement.setString(parameter_index, String.valueOf(value));
            }
            if (value instanceof Integer)
            {
                preparedStatement.setInt(parameter_index, (int) value);
            }
            if (value instanceof Double)
            {
                preparedStatement.setDouble(parameter_index, (double) value);
            }
            if (value instanceof Float)
            {
                preparedStatement.setFloat(parameter_index, (float) value);
            }
            if (value instanceof BigDecimal)
            {
                preparedStatement.setBigDecimal(parameter_index, (BigDecimal) value);
            }
            if (value instanceof Boolean)
            {
                preparedStatement.setBoolean(parameter_index, (boolean) value);
            }
            if (value instanceof InputStream)
            {
                preparedStatement.setBinaryStream(parameter_index, (InputStream) value);
            }
            if (value instanceof Short)
            {
                preparedStatement.setShort(parameter_index, (short) value);
            }
            if (value instanceof Character)
            {
                preparedStatement.setString(parameter_index, String.valueOf(value));
            }
            if (value instanceof Long)
            {
                preparedStatement.setLong(parameter_index, (long) value);
            }

            if (value instanceof java.util.Date)
            {
                preparedStatement.setDate(parameter_index, (Date) value);
            }
        }
        catch (Exception ex)
        {
            this.iSession.getPersistenceLogger().newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "void setParameter(int parameter_index, Object value)",
                            Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
    }

    /**
     * If this method will go be invoked for execute INSERT / UPDATE or DELETE
     * more of once, is recommended use "setCommit_mode(COMMIT_MODE.MANUAL)"
     */
    public void execute()
    {
        try
        {
            String tmpQuery = this.query.toLowerCase();
            if (tmpQuery.startsWith("select") || tmpQuery.startsWith("desc") || tmpQuery.startsWith("show"))
            {
                if (this.getResult_type() == null)
                {
                    throw new Exception("Persistor: error on pre-execute query at: RESULT_TYPE cannot be null !");
                }
                executeSelect(cls, this.getResult_type());
            }

            if (tmpQuery.startsWith("update") || tmpQuery.startsWith("insert"))
            {
                executeInsertOrUpdate(cls);
            }

        }
        catch (Exception ex)
        {
            this.iSession.getPersistenceLogger().newNofication(
                    new PersistenceLog(this.getClass().getName(), "void execute()", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
    }

    private void executeSelect(Class clss, RESULT_TYPE resultType) throws Exception
    {
        ResultSet resultSet = null;
        try
        {
            this.query = query.toLowerCase();

            if (!br.com.persistor.generalClasses.Util.extendsEntity(cls))
                throw new Exception("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");

            Field fieldMQ = clss.getField("mountedQuery");
            fieldMQ.set(obj, query);

            List<Object> resList = new ArrayList<>();
            Object ob = obj;
            Class cls = ob.getClass();

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                if (resultType == RESULT_TYPE.MULTIPLE)
                {
                    Constructor ctor = cls.getConstructor();
                    ob = ctor.newInstance();
                }

                for (Method method : cls.getMethods())
                {
                    if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        String columnName;
                        String fieldName;

                        if (method.getName().startsWith("is"))
                        {
                            columnName = method.isAnnotationPresent(Column.class)
                                    ? ((Column) method.getAnnotation(Column.class)).name()
                                    : (method.getName().substring(2, method.getName().length())).toLowerCase();
                            fieldName = "set" + method.getName().substring(2, method.getName().length());
                        }
                        else
                        {
                            columnName = method.isAnnotationPresent(Column.class)
                                    ? ((Column) method.getAnnotation(Column.class)).name()
                                    : (method.getName().substring(3, method.getName().length())).toLowerCase();
                            fieldName = "set" + method.getName().substring(3, method.getName().length());
                        }

                        if (method.isAnnotationPresent(OneToOne.class));

                        if (method.getReturnType() == boolean.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, boolean.class);
                            invokeMethod.invoke(ob, resultSet.getBoolean(columnName));
                            continue;
                        }

                        if (method.getReturnType() == int.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, int.class);
                            invokeMethod.invoke(ob, resultSet.getInt(columnName));
                            continue;
                        }

                        if (method.getReturnType() == double.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, double.class);
                            invokeMethod.invoke(ob, resultSet.getDouble(columnName));
                            continue;
                        }

                        if (method.getReturnType() == float.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, float.class);
                            invokeMethod.invoke(ob, resultSet.getFloat(columnName));
                            continue;
                        }

                        if (method.getReturnType() == short.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, short.class);
                            invokeMethod.invoke(ob, resultSet.getShort(columnName));
                            continue;
                        }

                        if (method.getReturnType() == long.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, long.class);
                            invokeMethod.invoke(ob, resultSet.getLong(columnName));
                            continue;
                        }

                        if (method.getReturnType() == String.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, String.class);
                            invokeMethod.invoke(ob, resultSet.getString(columnName));
                            continue;
                        }

                        if (method.getReturnType() == java.util.Date.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, java.util.Date.class);
                            invokeMethod.invoke(ob, resultSet.getDate(columnName));
                            continue;
                        }

                        if (method.getReturnType() == byte.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, byte.class);
                            invokeMethod.invoke(ob, resultSet.getByte(columnName));
                            continue;
                        }

                        if (method.getReturnType() == BigDecimal.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, BigDecimal.class);
                            invokeMethod.invoke(ob, resultSet.getBigDecimal(columnName));
                            continue;
                        }

                        if (method.getReturnType() == InputStream.class)
                        {
                            InputStream is;
                            if (iSession.getConfig().getDb_type() == DB_TYPE.SQLServer)
                                is = resultSet.getBlob(columnName).getBinaryStream();
                            else
                                is = resultSet.getBinaryStream(columnName);
                            
                            Method invokeMethod = obj.getClass().getMethod(fieldName, InputStream.class);
                            invokeMethod.invoke(obj, is);
                            continue;
                        }
                    }
                }
                if (result_type == RESULT_TYPE.MULTIPLE)
                    resList.add(ob);
                else
                    break;
            }

            System.out.println("Persistor: \n " + query);

            if (resList.size() > 0)
            {
                Field f = clss.getField("ResultList");
                f.set(obj, resList);
            }

        }
        catch (Exception ex)
        {
            this.iSession.getPersistenceLogger().newNofication(
                    new PersistenceLog(this.getClass().getName(), "void executeSelect()", Util.getDateTime(), Util.getFullStackTrace(ex), query));
        }

        finally
        {
            if (resultSet != null)
                Util.closeResultSet(resultSet);

            if (preparedStatement != null)
                Util.closePreparedStatement(preparedStatement);
        }
    }

    private void executeInsertOrUpdate(Class cls) throws Exception
    {
        Statement statement = null;
        Object obj = null;
        try
        {
            java.lang.reflect.Constructor constructor = cls.getConstructor();
            obj = constructor.newInstance();

            if (!Util.extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
            }

            Field fieldMQ = cls.getField("mountedQuery");
            fieldMQ.set(obj, query);

            preparedStatement.execute();
            if (this.getCommit_mode() == COMMIT_MODE.AUTO)
                iSession.commit();
            if (this.isCloseSessionAfterExecute())
                iSession.close();

            Field fieldSv = cls.getField("saved");
            fieldSv.set(obj, true);
            System.out.println("Persistor: \n " + query);

        }
        catch (Exception ex)
        {
            System.err.println("Persistor: execute query error at: \n");
            throw new Exception(ex.getMessage());
        }
        finally
        {
            if (preparedStatement != null)
            {
                Util.closePreparedStatement(preparedStatement);
            }

            if (statement != null)
            {
                Util.closeStatement(statement);
            }
        }
    }
}
