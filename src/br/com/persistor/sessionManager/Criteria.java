package br.com.persistor.sessionManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.persistor.annotations.OneToOne;
import br.com.persistor.enums.LIMIT_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.Expressions;
import br.com.persistor.generalClasses.Limit;
import br.com.persistor.interfaces.ICriteria;
import java.io.InputStream;
import br.com.persistor.interfaces.Session;

public class Criteria implements ICriteria
{
    RESULT_TYPE resultType;
    Object obj;
    String query = "";
    String tableName = "";
    Session iSession;

    private boolean hasFbLimit = false;

    public Criteria(Session iSession, Object obj, RESULT_TYPE result_type)
    {
        this.iSession = iSession;
        this.resultType = result_type;
        this.obj = obj;

        String name = (obj.getClass().getSimpleName().toLowerCase());
        this.tableName = name;
    }

    public ICriteria addLimit(Limit limit)
    {
        switch (iSession.getConfig().getDb_type())
        {
            case FirebirdSQL:

                if (limit.limit_type == LIMIT_TYPE.paginate)
                {
                    String q = query;
                    this.query = ("SELECT FIRST " + limit.getPageSize() + " SKIP " + limit.getPagePosition() + " * FROM " + this.tableName) + " " + q;
                }

                if (limit.limit_type == LIMIT_TYPE.simple)
                {
                    String q = query;
                    this.query = ("SELECT FIRST " + limit.getPageSize() + " * FROM " + this.tableName) + " " + q;
                }

                hasFbLimit = true;

                break;

            case PostgreSQL:

                if (limit.limit_type == LIMIT_TYPE.paginate)
                {
                    this.query += " LIMIT " + limit.getPageSize() + " OFFSET " + limit.getPagePosition();
                }

                if (limit.limit_type == LIMIT_TYPE.simple)
                {
                    this.query += " LIMIT " + limit.getPageSize();
                }

                break;

            case MySQL:

                if (limit.limit_type == LIMIT_TYPE.paginate)
                {
                    this.query += " LIMIT " + limit.getPagePosition() + ", " + limit.getPageSize();
                }

                if (limit.limit_type == LIMIT_TYPE.simple)
                {
                    this.query += " LIMIT " + limit.getPageSize();
                }

                break;
        }

        return this;
    }

    @Override
    public Criteria add(Expressions expression)
    {
        query += expression.getCurrentValue();
        return this;
    }

    private void closeResultSet(ResultSet resultSet)
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
            iSession.getPersistenceLogger().newNofication(this.getClass().getName(), " closeResultSet(ResultSet resultSet) (internal Persistor)", Util.getDateTime(), Util.getFullStackTrace(ex), "");
        }
    }

    private void closeStatement(Statement statement)
    {
        try
        {
            if (statement != null)
            {
                if (!statement.isClosed())
                {
                    statement.close();
                }
            }
        }
        catch (Exception ex)
        {
            iSession.getPersistenceLogger().newNofication(this.getClass().getName(), "void closeStatement(Statement statement) (internal Persistor)", Util.getDateTime(), Util.getFullStackTrace(ex), "");
        }
    }

    @Override
    public void execute()
    {
        Statement statement = null;
        ResultSet resultSet = null;

        if (!hasFbLimit)
        {
            query = "select * from " + tableName + " " + query;
        }

        try
        {
            Class clss = obj.getClass();

            Field fieldMQ = clss.getField("mountedQuery");
            fieldMQ.set(obj, query);

            List<Object> rList = new ArrayList<>();
            Object ob = obj;
            
            Class cls = ob.getClass();

            if (!Util.extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            if(this.iSession.getPersistenceContext().getFromContext(obj) != null)
            {
                obj = this.iSession.getPersistenceContext().getFromContext(obj);
                return;
            }
            
            statement = iSession.getActiveConnection().createStatement();
            resultSet = statement.executeQuery(query);

            if (resultType == RESULT_TYPE.UNIQUE)
            {
                if (resultSet.next())
                    for (Method method : cls.getMethods())
                    {
                        if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                        {
                            String name;
                            String fieldName;

                            if (method.getName().startsWith("is"))
                            {
                                name = (method.getName().substring(2, method.getName().length())).toLowerCase();
                                fieldName = "set" + method.getName().substring(2, method.getName().length());
                            }
                            else
                            {
                                name = (method.getName().substring(3, method.getName().length())).toLowerCase();
                                fieldName = "set" + method.getName().substring(3, method.getName().length());
                            }

                            if (method.isAnnotationPresent(OneToOne.class));

                            if (method.getReturnType() == char.class)
                            {
                                String str = resultSet.getString(name);

                                if (str.length() > 0)
                                {
                                    Method invokeMethod = obj.getClass().getMethod(fieldName, char.class);
                                    invokeMethod.invoke(ob, str.charAt(0));
                                    continue;
                                }
                            }

                            if (method.getReturnType() == boolean.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, boolean.class);
                                invokeMethod.invoke(ob, resultSet.getBoolean(name));
                                continue;
                            }

                            if (method.getReturnType() == int.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, int.class);
                                invokeMethod.invoke(ob, resultSet.getInt(name));
                                continue;
                            }

                            if (method.getReturnType() == double.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, double.class);
                                invokeMethod.invoke(ob, resultSet.getDouble(name));
                                continue;
                            }

                            if (method.getReturnType() == float.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, float.class);
                                invokeMethod.invoke(ob, resultSet.getFloat(name));
                                continue;
                            }

                            if (method.getReturnType() == short.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, short.class);
                                invokeMethod.invoke(ob, resultSet.getShort(name));
                                continue;
                            }

                            if (method.getReturnType() == long.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, long.class);
                                invokeMethod.invoke(ob, resultSet.getLong(name));
                                continue;
                            }

                            if (method.getReturnType() == String.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, String.class);
                                invokeMethod.invoke(ob, resultSet.getString(name));
                                continue;
                            }

                            if (method.getReturnType() == java.util.Date.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, java.util.Date.class);
                                invokeMethod.invoke(ob, resultSet.getDate(name));
                                continue;
                            }

                            if (method.getReturnType() == byte.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, byte.class);
                                invokeMethod.invoke(ob, resultSet.getByte(name));
                                continue;
                            }

                            if (method.getReturnType() == BigDecimal.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, BigDecimal.class);
                                invokeMethod.invoke(ob, resultSet.getBigDecimal(name));
                                continue;
                            }

                            if (method.getReturnType() == InputStream.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, InputStream.class);
                                invokeMethod.invoke(ob, (InputStream) resultSet.getBinaryStream(name));
                                continue;
                            }
                        }
                    }
            }
            else
            {
                while (resultSet.next())
                {
                    Constructor ctor = cls.getConstructor();
                    ob = ctor.newInstance();
                    ob.getClass().getField("mountedQuery").set(ob, query);
                    for (Method method : cls.getMethods())
                    {
                        if (method.getName().contains("is") || method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                        {
                            String name;
                            String fieldName;

                            if (method.getName().startsWith("is"))
                            {
                                name = (method.getName().substring(2, method.getName().length())).toLowerCase();
                                fieldName = "set" + method.getName().substring(2, method.getName().length());
                            }
                            else
                            {
                                name = (method.getName().substring(3, method.getName().length())).toLowerCase();
                                fieldName = "set" + method.getName().substring(3, method.getName().length());
                            }

                            if (method.getReturnType() == char.class)
                            {
                                String str = resultSet.getString(name);

                                if (str.length() > 0)
                                {
                                    Method invokeMethod = obj.getClass().getMethod(fieldName, char.class);
                                    invokeMethod.invoke(ob, str.charAt(0));
                                    continue;
                                }
                            }

                            if (method.getReturnType() == boolean.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, boolean.class);
                                invokeMethod.invoke(ob, resultSet.getBoolean(name));
                                continue;
                            }

                            if (method.getReturnType() == int.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, int.class);
                                invokeMethod.invoke(ob, resultSet.getInt(name));
                                continue;
                            }

                            if (method.getReturnType() == double.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, double.class);
                                invokeMethod.invoke(ob, resultSet.getDouble(name));
                                continue;
                            }

                            if (method.getReturnType() == float.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, float.class);
                                invokeMethod.invoke(ob, resultSet.getFloat(name));
                                continue;
                            }

                            if (method.getReturnType() == short.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, short.class);
                                invokeMethod.invoke(ob, resultSet.getShort(name));
                                continue;
                            }

                            if (method.getReturnType() == long.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, long.class);
                                invokeMethod.invoke(ob, resultSet.getLong(name));
                                continue;
                            }

                            if (method.getReturnType() == String.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, String.class);
                                invokeMethod.invoke(ob, resultSet.getString(name));
                                continue;
                            }

                            if (method.getReturnType() == java.util.Date.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, java.util.Date.class);
                                invokeMethod.invoke(ob, resultSet.getDate(name));
                                continue;
                            }

                            if (method.getReturnType() == byte.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, byte.class);
                                invokeMethod.invoke(ob, resultSet.getByte(name));
                                continue;
                            }

                            if (method.getReturnType() == BigDecimal.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, BigDecimal.class);
                                invokeMethod.invoke(ob, resultSet.getBigDecimal(name));
                                continue;
                            }

                            if (method.getReturnType() == InputStream.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, InputStream.class);
                                invokeMethod.invoke(ob, (InputStream) resultSet.getBinaryStream(name));

                                continue;
                            }
                        }
                    }
                    rList.add(ob);
                }
            }

            System.out.println("Persistor: \n " + query);

            Field f = clss.getField("ResultList");
            f.set(obj, rList);
            this.iSession.getPersistenceContext().addToContext(ob);
        }
        catch (Exception ex)
        {
            iSession.getPersistenceLogger().newNofication(this.getClass().getName(), "void execute()", Util.getDateTime(), Util.getFullStackTrace(ex), query);
        }
        finally
        {
            closeResultSet(resultSet);
            closeStatement(statement);
        }
    }
}
