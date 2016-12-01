package br.com.persistor.sessionManager;

import br.com.persistor.annotations.Column;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.persistor.annotations.OneToOne;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LIMIT_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.Expressions;
import br.com.persistor.generalClasses.Limit;
import br.com.persistor.generalClasses.PersistenceLog;
import br.com.persistor.interfaces.ICriteria;
import java.io.InputStream;
import br.com.persistor.interfaces.Session;

public class Criteria<T> implements ICriteria<T>
{

    Join join = null;
    RESULT_TYPE resultType;
    Object baseEntity;
    String query = "";
    String tableName = "";
    Session iSession;

    private boolean hasFbLimit = false;

    public Criteria(Session iSession, Object entity, RESULT_TYPE result_type)
    {
        this.iSession = iSession;
        this.resultType = result_type;
        this.baseEntity = entity;

        String name = (entity.getClass().getSimpleName().toLowerCase());
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
    public Criteria add(JOIN_TYPE join_type, Object entity, String joinCondition)
    {
        if (join == null)
        {
            join = new Join(baseEntity);
            join.setRestartEntityInstance(resultType == RESULT_TYPE.MULTIPLE);
        }
        join.addJoin(join_type, entity, joinCondition);
        return this;
    }

    @Override
    public Criteria add(Expressions expression)
    {
        query += expression.getCurrentValue();
        return this;
    }

    @Override
    public T loadEntity(T entity)
    {
        entity = (T) join.loadEntity(entity);
        return entity;
    }

    @Override
    public List<T> loadList(Object entity)
    {
        try
        {
            List<T> list = join.getList(entity);
            entity.getClass().getField("ResultList").set(entity, list);
            return list;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public ICriteria execute()
    {
        Statement statement = null;
        ResultSet resultSet = null;

        if (join != null)
        {
            join.addFinalCondition(query);
            join.execute(iSession);
            return this;
        }

        if (!hasFbLimit)
        {
            query = "select * from " + tableName + " " + query;
        }

        try
        {
            Class clss = baseEntity.getClass();

            Field fieldMQ = clss.getField("mountedQuery");
            fieldMQ.set(baseEntity, query);

            List<Object> rList = new ArrayList<>();
            Object ob = baseEntity;

            Class cls = ob.getClass();

            if (!Util.extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return null;
            }

            if (this.iSession.getPersistenceContext().getFromContext(baseEntity) != null)
            {
                baseEntity = this.iSession.getPersistenceContext().getFromContext(baseEntity);
                return this;
            }

            statement = iSession.getActiveConnection().createStatement();
            resultSet = statement.executeQuery(query);

            while (resultSet.next())
            {
                if (resultType == RESULT_TYPE.MULTIPLE)
                {
                    Constructor ctor = cls.getConstructor();
                    ob = ctor.newInstance();
                }

                for (Method method : cls.getMethods())
                {
                    if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test"))
                    {
                        if (method.getReturnType().getName().equals("java.lang.Class"))
                            continue;
                        
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
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, boolean.class);
                            invokeMethod.invoke(ob, resultSet.getBoolean(columnName));
                            continue;
                        }

                        if (method.getReturnType() == int.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, int.class);
                            invokeMethod.invoke(ob, resultSet.getInt(columnName));
                            continue;
                        }

                        if (method.getReturnType() == double.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, double.class);
                            invokeMethod.invoke(ob, resultSet.getDouble(columnName));
                            continue;
                        }

                        if (method.getReturnType() == float.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, float.class);
                            invokeMethod.invoke(ob, resultSet.getFloat(columnName));
                            continue;
                        }

                        if (method.getReturnType() == short.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, short.class);
                            invokeMethod.invoke(ob, resultSet.getShort(columnName));
                            continue;
                        }

                        if (method.getReturnType() == long.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, long.class);
                            invokeMethod.invoke(ob, resultSet.getLong(columnName));
                            continue;
                        }

                        if (method.getReturnType() == String.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, String.class);
                            invokeMethod.invoke(ob, resultSet.getString(columnName));
                            continue;
                        }

                        if (method.getReturnType() == java.util.Date.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, java.util.Date.class);
                            invokeMethod.invoke(ob, resultSet.getDate(columnName));
                            continue;
                        }

                        if (method.getReturnType() == byte.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, byte.class);
                            invokeMethod.invoke(ob, resultSet.getByte(columnName));
                            continue;
                        }

                        if (method.getReturnType() == BigDecimal.class)
                        {
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, BigDecimal.class);
                            invokeMethod.invoke(ob, resultSet.getBigDecimal(columnName));
                            continue;
                        }

                        if (method.getReturnType() == InputStream.class)
                        {
                            InputStream is = resultSet.getBlob(columnName).getBinaryStream();
                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, InputStream.class);
                            invokeMethod.invoke(baseEntity, is);
                            continue;
                        }
                    }
                }
                if (resultType == RESULT_TYPE.MULTIPLE)
                    rList.add(ob);
                else
                    break;
            }

            System.out.println("Persistor: \n " + query);

            Field f = clss.getField("ResultList");
            f.set(baseEntity, rList);
            this.iSession.getPersistenceContext().addToContext(ob);
        }
        catch (Exception ex)
        {
            iSession.getPersistenceLogger().newNofication(new PersistenceLog(this.getClass().getName(), "void execute()", Util.getDateTime(), Util.getFullStackTrace(ex), query));
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }
        return this;
    }
}
