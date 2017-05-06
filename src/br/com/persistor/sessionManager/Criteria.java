package br.com.persistor.sessionManager;

import br.com.persistor.annotations.Column;
import br.com.persistor.annotations.OneToMany;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.persistor.annotations.OneToOne;
import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LIMIT_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.Expressions;
import br.com.persistor.generalClasses.Limit;
import br.com.persistor.generalClasses.PersistenceLog;
import br.com.persistor.generalClasses.Util;
import br.com.persistor.interfaces.ICriteria;
import java.io.InputStream;
import br.com.persistor.interfaces.Session;

public class Criteria<T> implements ICriteria<T>
{

    Join join = null;
    RESULT_TYPE resultType;
    Object baseEntity;
    String query = "";
    String originalQuery = "";
    String tableName = "";
    Session iSession;
    String[] specificFields = null;

    private boolean hasFbLimit = false;
    private boolean autoCloseSession = false;

    public Criteria(Session iSession, Object entity, RESULT_TYPE result_type)
    {
        this.iSession = iSession;
        this.resultType = result_type;
        this.baseEntity = entity;

        String name = (entity.getClass().getSimpleName().toLowerCase());
        this.tableName = name;
    }

    @Override
    public ICriteria enableCloseSessionAfterExecute()
    {
        this.autoCloseSession = true;
        return this;
    }

    @Override
    public ICriteria setSpecificFields(String... fields)
    {
        specificFields = fields;
        return this;
    }

    @Override
    public ICriteria addLimit(Limit limit)
    {
        if (join != null)
            join.addLimit(limit);
        else
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

                case SQLServer:

                    if (limit.limit_type == LIMIT_TYPE.paginate)
                    {
                        this.query += " ORDER BY " + limit.getFieldToOrder() + " OFFSET " + limit.getPagePosition()
                                + " ROWS FETCH NEXT " + limit.getPageSize() + " ROWS ONLY";
                    }

                    if (limit.limit_type == LIMIT_TYPE.simple)
                    {
                        String q = query;
                        this.query = ("SELECT TOP " + limit.getPageSize() + " * FROM " + this.tableName + " " + q);
                        hasFbLimit = true;
                    }

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

    private boolean isPrecedencePending = false;

    @Override
    public Criteria beginPrecedence()
    {
        isPrecedencePending = true;
        return this;
    }

    @Override
    public Criteria endPrecedence()
    {
        query += ") ";
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

    private String getNotInForExistingKeysInCachedQuery(CachedQuery cachedQuery)
    {
        try
        {
            SQLHelper helper = new SQLHelper();
            helper.prepareDelete(baseEntity);

            List<Object> list = iSession.getSLPersistenceContext().listByClassType(baseEntity.getClass());
            if (list.isEmpty())
                return "";
            String result = "where (" + baseEntity.getClass().getSimpleName().toLowerCase() + "."
                    + helper.getPrimaryKeyName().toLowerCase() + " not in(";

            for (int key : cachedQuery.getResultKeys())
                result += key + ", ";

            result = result.substring(0, result.length() - 2) + ")) and";
            return result;
        }
        catch (Exception ex)
        {
        }

        return "";
    }

    @Override
    public Criteria add(Expressions expression)
    {
        String value = expression.getCurrentValue();

        if (isPrecedencePending)
        {
            value = expression.getCurrentValue();

            if (value.toLowerCase().startsWith(" where"))
                value = value.replace("where", "where (");

            if (value.toLowerCase().startsWith(" or"))
                value = value.replace("OR", "OR (");

            if (value.toLowerCase().startsWith(" and"))
                value = value.replace("AND", "AND (");

            isPrecedencePending = false;
            expression.setCurrentValue(value);
        }

        String expr = expression.getCurrentValue();

        switch (iSession.getConfig().getDb_type())
        {
            case SQLServer:

                if (expr.contains("true"))
                    expr = expr.replace("true", "1");
                if (expr.contains("false"))
                    expr = expr.replace("false", "0");
                break;
        }

        expression.setCurrentValue(expr);
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
    public void loadList(Object entity)
    {
        try
        {
            List<T> list = join.getList(entity);
            entity.getClass().getField("ResultList").set(entity, list);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    @Override
    public ICriteria addJoinIgnoreField(String fieldName)
    {
        if (join != null)
            join.addIgnorableField(fieldName);

        return this;
    }

    @Override
    public ICriteria execute()
    {
        Statement statement = null;
        ResultSet resultSet = null;

        if (join != null)
        {
            join.setAutoCloseAfterExecute(autoCloseSession);
            join.addFinalCondition(query);
            join.execute(iSession);

            return this;
        }

        if (!hasFbLimit)
        {
            if (specificFields != null)
            {
                String fields = "";
                for (int i = 0; i < specificFields.length; i++)
                    fields += specificFields[i] + ", ";

                fields = fields.substring(0, fields.length() - 2);
                query = "select " + fields + " from " + tableName + " " + query;
            }
            else
                query = "select * from " + tableName + " " + query;
        }

        this.originalQuery = query;

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

            if (iSession.isEnabledSLContext())
            {
                if (iSession.getSLPersistenceContext().isEntitySet(baseEntity))
                {
                    CachedQuery cq = iSession.getSLPersistenceContext().findCachedQuery(this.query);
                    if (cq != null)
                    {
                        for (int pkey : cq.getResultKeys())
                        {
                            Object cachedEntity = iSession.getSLPersistenceContext().findByID(baseEntity, pkey);
                            if (cachedEntity != null)
                                rList.add(cachedEntity);
                        }
                    }
                }
            }

            if (iSession.isEnabledSLContext())
            {
                if (iSession.getSLPersistenceContext().isEntitySet(baseEntity))
                {
                    CachedQuery cq = iSession.getSLPersistenceContext().findCachedQuery(this.query);
                    if (cq != null)
                    {
                        if (cq.getResultKeys().length > 0)
                        {
                            if (this.query.toLowerCase().contains("where"))
                            {
                                String beforeWhere = this.query.substring(0, this.query.toLowerCase().indexOf("where"));
                                String afterWhere = this.query.substring(this.query.toLowerCase().indexOf("where") + 5, this.query.length());

                                String inClause = getNotInForExistingKeysInCachedQuery(cq);
                                if (!inClause.isEmpty())
                                    this.query = beforeWhere + inClause + afterWhere;
                            }
                            else
                                this.query += getNotInForExistingKeysInCachedQuery(cq).replace("and", "");
                        }
                    }
                }
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
                        if (method.isAnnotationPresent(OneToMany.class) || method.isAnnotationPresent(OneToOne.class))
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

                        try
                        {
                            //checking if column exists in resultset
                            resultSet.findColumn(columnName);
                        }
                        catch (Exception ex)
                        {
                            System.err.println("Persistor WARNING: The column " + columnName + " does not exists. Verify entity mapping.");
                            continue;
                        }

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
                            InputStream is;
                            if (iSession.getConfig().getDb_type() == DB_TYPE.SQLServer)
                                is = resultSet.getBlob(columnName).getBinaryStream();
                            else
                                is = resultSet.getBinaryStream(columnName);

                            Method invokeMethod = baseEntity.getClass().getMethod(fieldName, InputStream.class);
                            invokeMethod.invoke(ob, is);
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

            if (iSession.isEnabledSLContext())
            {
                if (iSession.getSLPersistenceContext().isEntitySet(baseEntity))
                {
                    int[] resultKeys = new int[rList.size()];

                    for (int i = 0; i < rList.size(); i++)
                    {
                        Object objResult = rList.get(i);

                        SQLHelper helper = new SQLHelper();
                        helper.prepareDelete(objResult);

                        resultKeys[i] = Integer.parseInt(helper.getPrimaryKeyValue());
                        iSession.getSLPersistenceContext().addToContext(objResult);
                    }

                    iSession.getSLPersistenceContext().addCachedQuery(this.originalQuery, resultKeys);
                }
            }
        }
        catch (Exception ex)
        {
            iSession.getPersistenceLogger().newNofication(new PersistenceLog(this.getClass().getName(), "void execute()", Util.getDateTime(), Util.getFullStackTrace(ex), query));
        }
        finally
        {
            if (this.autoCloseSession)
                this.iSession.close();
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }
        return this;
    }
}
