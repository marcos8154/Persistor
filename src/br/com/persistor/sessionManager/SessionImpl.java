package br.com.persistor.sessionManager;

import br.com.persistor.annotations.Column;
import br.com.persistor.annotations.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.annotations.Version;
import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.ISOLATION_LEVEL;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;
import br.com.persistor.enums.PRIMARYKEY_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.ColumnKey;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.EntityKey;
import br.com.persistor.generalClasses.JoinableObject;
import br.com.persistor.generalClasses.PersistenceLog;
import br.com.persistor.generalClasses.Util;
import br.com.persistor.interfaces.IPersistenceLogger;
import java.io.InputStream;
import java.math.BigDecimal;
import br.com.persistor.interfaces.Session;
import static java.sql.Connection.*;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SessionImpl implements Session
{

    private Connection connection = null;
    private DBConfig config = null;
    private PersistenceContext context = null;
    private PersistenceContext slContext = null;
    private IPersistenceLogger logger = null;

    private boolean enabledContext = true;
    private boolean isRollbacked = false;

    private boolean isVersionViolation = false;
    private String whereConditionGetLastID = "";

    @Override
    public boolean isVersionViolation()
    {
        return isVersionViolation;
    }

    public SessionImpl(Connection connection, DBConfig config)
    {
        this.connection = connection;
        this.context = new PersistenceContext();
        //this.setIsolationLevel(ISOLATION_LEVEL.TRANSACTION_READ_COMMITTED);
        this.setConfig(config);
    }

    @Override
    public void disableSLContext()
    {
        slContext = null;
    }

    @Override
    public void evict(boolean includeSLCache)
    {
        if (context.initialized)
            context.clear();
        if (includeSLCache)
            if (isEnabledSLContext())
                slContext.clear();
    }

    @Override
    public void setIsolationLevel(ISOLATION_LEVEL isolation_level)
    {
        try
        {
            switch (isolation_level)
            {
                case TRANSACTION_NONE:
                    this.connection.setTransactionIsolation(TRANSACTION_NONE);
                    break;

                case TRANSACTION_READ_COMMITTED:
                    this.connection.setTransactionIsolation(TRANSACTION_READ_COMMITTED);
                    break;

                case TRANSACTION_READ_UNCOMMITTED:
                    this.connection.setTransactionIsolation(TRANSACTION_READ_UNCOMMITTED);
                    break;

                case TRANSACTION_REPEATABLE_READ:
                    this.connection.setTransactionIsolation(TRANSACTION_REPEATABLE_READ);
                    break;

                case TRANSACTION_SERIALIZABLE:
                    this.connection.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
                    break;
            }
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog("SessionImpl", "void setIsolationLevel(ISOLATION_LEVEL isolation_level)", null, ex, "Error on set IsolationLevel for Active Database Connection in current Session"));
        }
    }

    @Override
    public PersistenceContext getPersistenceContext()
    {
        return this.context;
    }

    @Override
    public DBConfig getConfig()
    {
        return this.config;
    }

    @Override
    public IPersistenceLogger getPersistenceLogger()
    {
        return logger;
    }

    @Override
    public Connection getActiveConnection()
    {
        return this.connection;
    }

    @Override
    public Query createQuery(Object entity, String queryCommand)
    {
        try
        {
            Query query = new Query();
            query.createQuery(this, entity, queryCommand);
            return query;
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "Query createQuery(Object entity, String queryCommand)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex), ""));
        }
        return null;
    }

    @Override
    public void save(Object entity)
    {
        PreparedStatement preparedStatement = null;
        SQLHelper sql_helper = new SQLHelper();
        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
                Util.throwNotEntityException(this, cls, getClass(), logger);

            sql_helper.prepareInsert(entity);

            String sqlBase = sql_helper.getSqlBase();
            preparedStatement = connection.prepareStatement(sqlBase);
            saveOrUpdateForeignObjects(entity, false);
            loadPreparedStatement(preparedStatement, entity, false);
            preparedStatement.execute();
            System.out.println("Persistor: \n " + sqlBase);
            lastID(entity, whereConditionGetLastID);
            Field fieldSaved = cls.getField("saved");
            fieldSaved.set(entity, true);
        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "void save(Object entity)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sql_helper.getSqlBase()));
        }
        finally
        {
            Util.closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void update(Object entity, String and_or_condition)
    {
        PreparedStatement preparedStatement = null;
        SQLHelper sql_helper = new SQLHelper();
        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
                Util.throwNotEntityException(this, cls, getClass(), logger);

            sql_helper.prepareUpdate(entity, connection);
            String sqlBase = sql_helper.getSqlBase();

            sqlBase += and_or_condition;
            preparedStatement = connection.prepareStatement(sqlBase);
            saveOrUpdateForeignObjects(entity, true);
            loadPreparedStatement(preparedStatement, entity, true);
            preparedStatement.execute();
            Field fieldSaved = cls.getField("updated");
            fieldSaved.set(entity, true);

            System.out.println("Persistor: \n " + sqlBase);

            if (context.initialized)
                this.context.addToContext(entity);
            if (isEnabledSLContext())
                this.slContext.addToContext(entity);
        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "void update(Object entity, String andCondition)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sql_helper.getSqlBase()));
        }
        finally
        {
            Util.closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void update(Object entity)
    {
        PreparedStatement preparedStatement = null;
        SQLHelper sql_helper = new SQLHelper();
        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
                Util.throwNotEntityException(this, cls, getClass(), logger);

            sql_helper.prepareUpdate(entity, connection);
            String sqlBase = sql_helper.getSqlBase();

            preparedStatement = connection.prepareStatement(sqlBase);
            saveOrUpdateForeignObjects(entity, true);
            loadPreparedStatement(preparedStatement, entity, true);
            preparedStatement.execute();
            Field fieldSaved = cls.getField("updated");
            fieldSaved.set(entity, true);

            System.out.println("Persistor: \n " + sqlBase);

            if (context.initialized)
                this.context.addToContext(entity);
            if (isEnabledSLContext())
                this.slContext.addToContext(entity);

        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "void update(Object entity)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sql_helper.getSqlBase()));
        }
        finally
        {
            Util.closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void delete(Object entity, String and_or_where_condition)
    {
        PreparedStatement preparedStatement = null;
        SQLHelper sql_helper = new SQLHelper();
        String sqlBase = "";
        try
        {
            Class cls = entity.getClass();
            String tableName = cls.getSimpleName().toLowerCase();

            if (!extendsEntity(cls))
                Util.throwNotEntityException(this, cls, getClass(), logger);

            sql_helper.prepareDelete(entity);

            if (sql_helper.getPrimaryKeyName().isEmpty())
                sqlBase = "delete from " + tableName + " where " + and_or_where_condition;
            else
            {
                sqlBase = sql_helper.getSqlBase();
                sqlBase += " " + and_or_where_condition;
            }

            preparedStatement = connection.prepareStatement(sqlBase);
            preparedStatement.execute();
            Field fieldDel = cls.getField("deleted");
            fieldDel.set(entity, true);

            if (context.initialized)
                this.context.removeAllFromClass(entity.getClass());
            if (isEnabledSLContext())
                this.slContext.removeAllFromClass(entity.getClass());

            System.out.println("Persistor: \n " + sqlBase);
        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "void delete(Object entity, String andCondition)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sqlBase));
        }
        finally
        {
            Util.closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void delete(Object entity)
    {
        PreparedStatement preparedStatement = null;
        SQLHelper sql_helper = new SQLHelper();
        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
                Util.throwNotEntityException(this, cls, getClass(), logger);

            sql_helper.prepareDelete(entity);
            String sqlBase = sql_helper.getSqlBase();

            System.out.println("Persistor: \n " + sqlBase);
            preparedStatement = connection.prepareStatement(sqlBase);
            preparedStatement.execute();
            Field fieldDel = cls.getField("deleted");
            fieldDel.set(entity, true);

            this.context.removeFromContext(entity);

            if (isEnabledSLContext())
                this.slContext.removeFromContext(entity);
        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "void delete(Object entity)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sql_helper.getSqlBase()));
        }
        finally
        {
            Util.closePreparedStatement(preparedStatement);
        }
    }

    private <T> T joinExecutionResult(Class entityCls, Object entity, int id) throws Exception
    {
        entity = executeJoin(entity, id);
        if (entity == null)
            return (T) entityCls.newInstance();
        else
            return (T) entity;
    }

    @Override
    public <T> T onID(Class entityCls, int id)
    {
        SQLHelper sqlHelper = new SQLHelper();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        Object entity = null;
        try
        {
            if (!extendsEntity(entityCls))
                Util.throwNotEntityException(this, entityCls, getClass(), logger);

            entity = entityCls.newInstance();
            sqlHelper.prepareBasicSelect(entity, id);

            if (context.findByID(entity, id) != null)
            {
                Object cachedEntity = context.findByID(entity, id);
                if (cachedEntity != null)
                    return (T) cachedEntity;
            }

            if (isEnabledSLContext())
            {
                Object cachedEntity = slContext.findByID(entity, id);
                if (cachedEntity != null)
                    return (T) cachedEntity;
            }

            if (hasJoinableObjects(entity))
                return joinExecutionResult(entityCls, entity, id);

            String sqlBase = sqlHelper.getSqlBase();
            Field field = entityCls.getField("mountedQuery");
            field.set(entity, sqlBase);

            ps = connection.prepareStatement(sqlBase);
            resultSet = ps.executeQuery();
            if (loadEntity(entity, resultSet))
            {
                System.out.println("Persistor: \n " + sqlBase);

                context.addToContext(entity);
                if (isEnabledSLContext())
                    this.slContext.addToContext(entity);
            }
            enabledContext = true;
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "<T> T onID(Class entityCls, int id)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sqlHelper.getSqlBase()));
        }
        finally
        {
            if (resultSet != null)
                Util.closeResultSet(resultSet);
            if (ps != null)
                Util.closeStatement(ps);
        }

        return (T) entity;
    }

    @Override
    public <T> T onID(Class entityCls, EntityKey... keys)
    {
        SQLHelper sqlHelper = new SQLHelper();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        Object entity = null;
        try
        {
            if (!extendsEntity(entityCls))
                Util.throwNotEntityException(this, entityCls, getClass(), logger);

            entity = entityCls.newInstance();
            ColumnKey cKey = sqlHelper.getKey(entityCls);
            int id = 0;
            for (EntityKey key : keys)
            {
                Field keyField = null;
                try
                {
                    keyField = entity.getClass().getDeclaredField(key.getKeyField());
                }
                catch (Exception ex)
                {
                    throw new Exception("Persistor: field '" + key.getKeyField() + "' not exists in entity '" + entityCls.getName() + "'");
                }

                keyField.setAccessible(true);
                keyField.set(entity, key.getKeyValue());

                if (keyField.getName().equals(cKey.getColumnName()))
                    id = key.getKeyValue();
            }

            sqlHelper.prepareMultiKeySelect(entity, id);

            if (hasJoinableObjects(entity))
                return joinExecutionResult(entityCls, entity, id);

            String sqlBase = sqlHelper.getSqlBase();
            Field field = entityCls.getField("mountedQuery");
            field.set(entity, sqlBase);

            ps = connection.prepareStatement(sqlBase);
            resultSet = ps.executeQuery();
            if (loadEntity(entity, resultSet))
            {
                System.out.println("Persistor: \n " + sqlBase);

                context.addToContext(entity);
                if (isEnabledSLContext())
                    this.slContext.addToContext(entity);
            }
            enabledContext = true;
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "<T> T onID(Class entityCls, int id)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sqlHelper.getSqlBase()));
        }
        finally
        {
            if (resultSet != null)
                Util.closeResultSet(resultSet);
            if (ps != null)
                Util.closeStatement(ps);
        }

        return (T) entity;
    }

    @Override
    public void commit()
    {
        try
        {
            if (isRollbacked)
                return;
            connection.commit();
        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "void commit",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
        }
    }

    @Override
    public void close()
    {
        try
        {
            connection.close();

            if (context != null)
                if (context.initialized)
                    context.clear();
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "void close()",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
        }
    }

    @Override
    public void rollback()
    {
        try
        {
            System.out.println("Persistor: Rollbacking...");
            connection.rollback();
            isRollbacked = true;
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "void rollback",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
        }
    }

    @Override
    public <T> T first(Class cls, String... whereCondition)
    {
        Statement statement = null;
        ResultSet resultSet = null;
        String sqlBase = "";
        String whereClause = (whereCondition.length > 0
                ? whereCondition[0]
                : "");
        try
        {
            if (!extendsEntity(cls))
                Util.throwNotEntityException(this, cls, getClass(), logger);

            java.lang.reflect.Constructor constructor = cls.getConstructor();
            Object entity = constructor.newInstance();

            SQLHelper sql_helper = new SQLHelper();
            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);
            String auxiliarKeyName = sql_helper.getAuxiliarPK_name(cls);
            if (auxiliarKeyName != null)
                throw new Exception("Persistor: The method first(Class cls, String ... whereCondition) can not be executed on entities that have composite primary keys.");

            String className = cls.getSimpleName().toLowerCase();
            sqlBase = "SELECT MIN(" + primaryKeyName + ") FROM " + className;

            if (!whereClause.isEmpty())
                sqlBase += " where " + whereClause;

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBase);
            if (resultSet.next())
            {
                enabledContext = false;
                int obtainedId = resultSet.getInt(1);

                Util.closeResultSet(resultSet);
                Util.closeStatement(statement);

                return onID(cls, obtainedId);
            }
            context.addToContext(entity);
            if (isEnabledSLContext())
                this.slContext.addToContext(entity);
            return (T) entity;
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            " <T> T First(Class cls, String whereCondition)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sqlBase));
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }
        return null;
    }

    @Override
    public <T> T last(Class cls, String... whereCondition)
    {
        String sqlBase = "";
        Statement statement = null;
        ResultSet resultSet = null;
        String whereClause = (whereCondition.length > 0
                ? whereCondition[0]
                : "");
        try
        {
            if (!extendsEntity(cls))
                Util.throwNotEntityException(this, cls, getClass(), logger);

            java.lang.reflect.Constructor constructor = cls.getConstructor();
            Object entity = constructor.newInstance();

            SQLHelper sql_helper = new SQLHelper();
            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);
            String auxiliarKeyName = sql_helper.getAuxiliarPK_name(cls);
            if (auxiliarKeyName != null)
                throw new Exception("Persistor: The method last(Class cls, String ... whereCondition) can not be executed on entities that have composite primary keys.");

            String className = cls.getSimpleName().toLowerCase();
            sqlBase = "SELECT MAX(" + primaryKeyName + ") FROM " + className;

            if (!whereClause.isEmpty())
                sqlBase += " WHERE " + whereClause;

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBase);
            if (resultSet.next())
            {
                enabledContext = false;
                int obtainedId = resultSet.getInt(1);

                Util.closeResultSet(resultSet);
                Util.closeStatement(statement);

                return onID(cls, obtainedId);
            }

            context.addToContext(entity);
            if (isEnabledSLContext())
                this.slContext.addToContext(entity);

            return (T) entity;
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "<T> T Last(Class cls, String whereCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sqlBase));
        }
        finally
        {
            Util.closeStatement(statement);
            Util.closeResultSet(resultSet);
        }
        return null;
    }

    @Override
    public Criteria createCriteria(Object entity, RESULT_TYPE result_type)
    {
        Criteria criteria = null;
        try
        {
            criteria = new Criteria(this, entity, result_type);
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "Criteria createCriteria(Object entity, RESULT_TYPE result_type)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
        return criteria;
    }

    @Override
    public int count(Class entityClass, String... whereCondition)
    {
        String sql = "";
        int result = 0;

        Statement statement = null;
        ResultSet resultSet = null;

        String whereClause = (whereCondition.length > 0
                ? whereCondition[0]
                : "");
        try
        {
            if (!extendsEntity(entityClass))
            {
                Exception ex = new Exception("Persistor ERROR: THE CLASS '" + entityClass.getName() + "' NOT EXTENDS Entity. OPERATION HAS STOPED.");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
                rollback();
                return -1;
            }

            java.lang.reflect.Constructor constructor = entityClass.getConstructor();
            Object entity = constructor.newInstance();

            String tableName = (entity.getClass().getSimpleName().toLowerCase());
            sql = "SELECT COUNT(*) FROM " + tableName;

            if (!whereClause.isEmpty())
                sql += " where " + whereClause;

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next())
                result = resultSet.getInt(1);

            System.out.println("Persistor: \n" + sql);
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "<T> T Last(Class cls, String whereCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sql));
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }

        return result;
    }

    @Override
    public double sum(Class entityClass, String columnName, String... whereCondition)
    {
        String sql = "";
        double result = 0;

        Statement statement = null;
        ResultSet resultSet = null;

        String whereClause = (whereCondition.length > 0
                ? whereCondition[0]
                : "");
        try
        {
            if (!extendsEntity(entityClass))
            {
                Exception ex = new Exception("Persistor ERROR: THE CLASS '" + entityClass.getName() + "' NOT EXTENDS Entity. OPERATION HAS STOPED.");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
                rollback();
                return 0;
            }

            java.lang.reflect.Constructor constructor = entityClass.getConstructor();
            Object entity = constructor.newInstance();

            String tableName = (entity.getClass().getSimpleName().toLowerCase());
            sql = "SELECT SUM(" + columnName + ") " + columnName + " FROM " + tableName;

            if (!whereClause.isEmpty())
                sql += " where " + whereClause;

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next())
                result = resultSet.getDouble(1);

            System.out.println("Persistor: \n" + sql);
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "<T> T Last(Class cls, String whereCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sql));
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }

        return result;
    }

    @Override
    public PersistenceContext getSLPersistenceContext()
    {
        return this.slContext;
    }

    @Override
    public boolean isEnabledSLContext()
    {
        return (this.slContext != null);
    }

    private void setConfig(DBConfig config)
    {
        this.config = config;
        this.context.Initialize(config.getPersistenceContext());
        if (config.getPersistenceLogger() == null || config.getPersistenceLogger().isEmpty())
        {
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS NOT FOUND. CREATE OR INQUIRE THE PERSISTENCE LOGGER CLASS TO AVOID PROBLEMS ****");
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS NOT FOUND. CREATE OR INQUIRE THE PERSISTENCE LOGGER CLASS TO AVOID PROBLEMS ****");
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS NOT FOUND. CREATE OR INQUIRE THE PERSISTENCE LOGGER CLASS TO AVOID PROBLEMS ****");
            System.exit(0);
        }

        try
        {
            this.logger = (IPersistenceLogger) Class.forName(config.getPersistenceLogger()).newInstance();
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS INITIALIZATION ERROR AT: \n" + Util.getFullStackTrace(ex));
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS NOT FOUND. CREATE OR INQUIRE THE PERSISTENCE LOGGER CLASS TO AVOID PROBLEMS ****");
            System.exit(0);
        }
    }

    public void setSLContext(PersistenceContext pc)
    {
        this.slContext = pc;
    }

    private boolean extendsEntity(Class entityCls)
    {
        try
        {
            for (Field field : entityCls.getFields())
                if (field.getName() == "saved")
                    return true;
        }
        catch (Exception e)
        {
        }
        return false;
    }

    private boolean methodHasValue(Object entity, String methodName) throws Exception
    {
        try
        {
            Method method;
            if (methodName.startsWith("is"))
            {
                methodName = "is" + methodName.substring(2, methodName.length());
                method = entity.getClass().getMethod(methodName);
            }
            else
            {
                methodName = "get" + methodName.substring(3, methodName.length());
                method = entity.getClass().getMethod(methodName);
            }

            Object value = method.invoke(entity);

            if (value != null)
            {
                if (value instanceof String)
                    return (Integer.parseInt(value.toString()) > 0);

                if (value instanceof Number)
                    return (Integer.parseInt(value.toString()) > 0);
            }
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "boolean methodHasValue(Object entity, String methodName)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
            throw new Exception(ex.getMessage());
        }

        return false;
    }

    private void loadPreparedStatement(PreparedStatement preparedStatement, Object entity, boolean ignorePrimaryKey) throws Exception
    {
        try
        {
            Class cls = entity.getClass();
            int parameterIndex = 1;
            for (Method method : cls.getMethods())
            {
                SQLHelper helper = new SQLHelper();
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    if (ignorePrimaryKey)
                        continue;

                    PrimaryKey primaryKey = (PrimaryKey) method.getAnnotation(PrimaryKey.class);

                    int nextID;

                    if (primaryKey.increment() == INCREMENT.MANUAL)
                    {

                        if (primaryKey.primarykey_type() == PRIMARYKEY_TYPE.AUXILIAR)
                        {
                            preparedStatement.setInt(parameterIndex, (int) method.invoke(entity));
                            parameterIndex++;
                            continue;
                        }

                        if (helper.getAuxiliarPK_name(cls) != null)
                        {
                            String auxPK_name = helper.getAuxiliarPK_name(cls);
                            String columnAuxPK_name = auxPK_name.replace("get", "").toLowerCase();

                            whereConditionGetLastID = columnAuxPK_name + " = " + helper.getAuxiliarPK_value(entity, cls, auxPK_name);

                            nextID = (this.maxId(entity, whereConditionGetLastID) + 1);
                            preparedStatement.setInt(parameterIndex, nextID);
                            parameterIndex++;
                            continue;
                        }

                        if (helper.getAuxiliarPK_name(cls) == null)
                        {
                            nextID = (this.maxId(entity, "") + 1);
                            preparedStatement.setInt(parameterIndex, nextID);
                            parameterIndex++;
                            continue;
                        }
                    }
                    else
                    {
                        continue;
                    }
                }

                if (method.isAnnotationPresent(OneToOne.class) || method.isAnnotationPresent(OneToMany.class))
                    continue;

                if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test"))
                {
                    if (method.getReturnType().getName().equals("java.lang.Class"))
                        continue;

                    if (method.isAnnotationPresent(Version.class))
                    {
                        int nextVersion = (int) method.invoke(entity);
                        preparedStatement.setInt(parameterIndex, (nextVersion + 1));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == boolean.class)
                    {
                        preparedStatement.setBoolean(parameterIndex, (boolean) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == int.class)
                    {
                        preparedStatement.setInt(parameterIndex, (int) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == double.class)
                    {
                        preparedStatement.setDouble(parameterIndex, (double) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == BigDecimal.class)
                    {
                        preparedStatement.setBigDecimal(parameterIndex, (BigDecimal) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == String.class)
                    {
                        preparedStatement.setString(parameterIndex, (String) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == short.class)
                    {
                        preparedStatement.setShort(parameterIndex, (short) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == long.class)
                    {
                        preparedStatement.setLong(parameterIndex, (long) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == float.class)
                    {
                        preparedStatement.setFloat(parameterIndex, (float) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == byte.class)
                    {
                        preparedStatement.setByte(parameterIndex, (byte) method.invoke(entity));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == InputStream.class)
                    {
                        if (method.invoke(entity) == null)
                            preparedStatement.setNull(parameterIndex, Types.BINARY);
                        else if (config.getDb_type() == DB_TYPE.PostgreSQL)
                            preparedStatement.setBinaryStream(parameterIndex, (InputStream) method.invoke(entity), ((InputStream) method.invoke(entity)).available());
                        else
                            preparedStatement.setBinaryStream(parameterIndex, (InputStream) method.invoke(entity));

                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == Date.class)
                    {
                        Date date = (java.util.Date) method.invoke(entity);
                        if (date == null)
                        {
                            preparedStatement.setDate(parameterIndex, null);
                            parameterIndex++;
                            continue;
                        }

                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        preparedStatement.setDate(parameterIndex, java.sql.Date.valueOf(format.format(date)));
                        parameterIndex++;
                    }

                    if (method.getReturnType() == Calendar.class)
                    {
                        Calendar c = (Calendar) method.invoke(entity);
                        if (c == null)
                        {
                            preparedStatement.setDate(parameterIndex, null);
                            parameterIndex++;
                            continue;
                        }

                        preparedStatement.setDate(parameterIndex, new java.sql.Date(c.getTimeInMillis()));
                        parameterIndex++;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "void loadPreparedStatement(PreparedStatement preparedStatement, Object entity, boolean ignorePrimaryKey)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
            throw new Exception(ex.getMessage());
        }
    }

    private void saveOrUpdateForeignObjects(Object entity, boolean isUpdateMode) throws Exception
    {
        Class cls = entity.getClass();
        for (Method method : cls.getMethods())
        {
            if (method.isAnnotationPresent(OneToOne.class))
            {
                Object object = method.invoke(entity);

                if (object == null)
                {
                    if (isUpdateMode)
                        continue;

                    Class clss = Class.forName(method.getReturnType().getName());
                    java.lang.reflect.Constructor ctor = clss.getConstructor();
                    object = ctor.newInstance();
                }

                OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);
                if (oneToOne.join_type() == JOIN_TYPE.LEFT)
                    continue;

                String field = "set" + oneToOne.source().substring(0, 1).toUpperCase() + oneToOne.source().substring(1);

                if (methodHasValue(entity, field))
                    continue;

                SessionImpl session = new SessionImpl(this.connection, this.config);

                if (isUpdateMode)
                    session.update(object);
                else
                    session.save(object);

                SQLHelper helper = new SQLHelper();
                Method pkObject = object.getClass().getMethod(helper.getPrimaryKeyMethodName(object));

                Method mtd = entity.getClass().getMethod(field, int.class);
                mtd.invoke(entity, pkObject.invoke(object));
            }
        }
    }

    private boolean loadEntity(Object entity, ResultSet resultSet) throws Exception
    {
        boolean result = false;
        try
        {
            EntityFiller entityFiller = new EntityFiller();
            Class cls = entity.getClass();
            while (resultSet.next())
            {
                result = true;
                for (Method method : cls.getMethods())
                {
                    if (method.getName().startsWith("get") || method.getName().startsWith("is") && !method.getName().contains("class Test"))
                    {
                        if (method.getReturnType().getName().equals("java.lang.Class"))
                            continue;

                        if (method.isAnnotationPresent(OneToOne.class))
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

                        /*
                        if (method.isAnnotationPresent(OneToOne.class) ||
                                method.isAnnotationPresent(OneToMany.class))
                            continue;
*/
                            
                        entityFiller.fillEntity(method, resultSet, fieldName, columnName, entity, entity, this);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "boolean loadEntity(Object entity, ResultSet resultSet)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
            throw new Exception(ex.getMessage());
        }
        return result;
    }

    private boolean hasJoinableObjects(Object entity) throws Exception
    {
        try
        {
            Class cls = entity.getClass();
            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);
                    return (!(oneToOne.load() == LOAD.MANUAL));
                }

                if (method.isAnnotationPresent(OneToMany.class))
                {
                    OneToMany oneToMany = (OneToMany) method.getAnnotation(OneToMany.class);
                    return (!(oneToMany.load() == LOAD.MANUAL));
                }
            }
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "boolean hasJoinableObjects(Object entity)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
            throw new Exception(ex.getMessage());
        }

        return false;
    }

    private void resolveJoinOneToOneCase(Method method,
            List<JoinableObject> objectsToJoin, Join join, Class cls) throws Exception
    {
        Class clss = Class.forName(method.getReturnType().getName());
        java.lang.reflect.Constructor ctor = clss.getConstructor();
        Object entityObj = ctor.newInstance();
        OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);

        if (oneToOne.load() == LOAD.MANUAL)
            return;

        if (oneToOne.ignore_onID().length > 0)
            for (String fieldToIgnore : oneToOne.ignore_onID())
                join.addIgnorableField(fieldToIgnore);

        String sourceName = cls.getSimpleName() + "." + oneToOne.source();
        String targetName = entityObj.getClass().getSimpleName() + "." + oneToOne.target();
        join.addJoin(oneToOne.join_type(), entityObj, sourceName + " = " + targetName);
        JoinableObject objToJoin = new JoinableObject();
        objToJoin.result_type = RESULT_TYPE.UNIQUE;
        objToJoin.objectToJoin = entityObj;
        objToJoin.joinGetMethod = method.getName().substring(3, method.getName().length());
        objectsToJoin.add(objToJoin);
    }

    private void resolveJoinOneToManyCase(Method method,
            List<JoinableObject> objectsToJoin, Join join, Class cls) throws Exception
    {
        Class clss = Class.forName(method.getReturnType().getName());
        java.lang.reflect.Constructor ctor = clss.getConstructor();
        Object entityObj = ctor.newInstance();
        OneToMany oneToMany = (OneToMany) method.getAnnotation(OneToMany.class);

        if (oneToMany.load() == LOAD.MANUAL)
            return;

        String sourceName = cls.getSimpleName() + "." + oneToMany.source();
        String targetName = entityObj.getClass().getSimpleName() + "." + oneToMany.target();
        join.addJoin(oneToMany.join_type(), entityObj, sourceName + " = " + targetName);
        JoinableObject objToJoin = new JoinableObject();
        objToJoin.result_type = RESULT_TYPE.MULTIPLE;
        objToJoin.objectToJoin = entityObj;
        objToJoin.joinGetMethod = method.getName().substring(3, method.getName().length());
        objectsToJoin.add(objToJoin);
    }

    private <T> T executeJoin(T entity, int id) throws Exception
    {
        try
        {
            Class cls = entity.getClass();
            Join join = new Join(entity, false);
            join.setRestartEntityInstance(true);
            List<JoinableObject> objectsToJoin = new ArrayList<>();

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                    resolveJoinOneToOneCase(method, objectsToJoin, join, cls);

                if (method.isAnnotationPresent(OneToMany.class))
                    resolveJoinOneToManyCase(method, objectsToJoin, join, cls);
            }

            if (join.joinCount > 0)
            {
                String table = cls.getSimpleName().toLowerCase();
                SQLHelper helper = new SQLHelper();
                helper.prepareBasicSelect(entity, id);
                List<EntityKey> keys = helper.getEntityKeys(entity);
                String where = table + "." + helper.getPrimaryKeyName() + " = " + id + " and ";

                for (EntityKey key : keys)
                    if (!key.getKeyField().toLowerCase().equals(helper.getPrimaryKeyName()))
                        where += table + "." + key.getKeyField() + " = " + key.getKeyValue() + " and ";

                where = "where " + where.substring(0, where.lastIndexOf("and "));

                join.addFinalCondition(where);
                join.execute(this);

                entity = join.getEntity(entity.getClass());

                if (entity == null)
                    return entity;

                addJoinResultToContexts(join, entity);
                for (JoinableObject joinableObject : objectsToJoin)
                {
                    if (joinableObject.result_type == RESULT_TYPE.UNIQUE)
                        publishJoinResultUnique(joinableObject, entity, join);

                    if (joinableObject.result_type == RESULT_TYPE.MULTIPLE)
                        publishJoinResultMultiple(joinableObject, entity, join);
                }
            }
        }
        catch (Exception ex)
        {
            entity = null;
            logger.newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "<T> T executeJoin(T entity, int id)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
            throw new Exception(ex.getMessage());
        }

        return entity;
    }

    private void addJoinResultToContexts(Join join, Object entity) throws Exception
    {
        context.addToContext(entity);

        if (isEnabledSLContext())
            this.slContext.addToContext(entity);
    }

    private void publishJoinResultMultiple(JoinableObject joinableObject,
            Object entity, Join join) throws Exception
    {
        Class clss = joinableObject.objectToJoin.getClass();
        Field f = clss.getField("ResultList");
        f.set(joinableObject.objectToJoin, join.getList(joinableObject.objectToJoin));
        Method method = entity.getClass().getMethod("set" + joinableObject.joinGetMethod, joinableObject.objectToJoin.getClass());
        method.invoke(entity, joinableObject.objectToJoin);
    }

    private void publishJoinResultUnique(JoinableObject joinableObject,
            Object entity, Join join) throws Exception
    {
        joinableObject.objectToJoin = join.getEntity(joinableObject.objectToJoin.getClass());

        Method method = entity.getClass().getMethod("set" + joinableObject.joinGetMethod, joinableObject.objectToJoin.getClass());
        method.invoke(entity, joinableObject.objectToJoin);
    }

    private int maxId(Object entity, String where) throws Exception
    {
        Statement statement = null;
        ResultSet resultSet = null;
        int result = 0;
        String sqlBase = "";
        try
        {
            Class cls = entity.getClass();
            SQLHelper sql_helper = new SQLHelper();
            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);

            String className = cls.getSimpleName().toLowerCase();
            sqlBase = "SELECT MAX(" + primaryKeyName + ") " + primaryKeyName + " FROM " + className;

            if (!where.isEmpty())
                sqlBase += " WHERE " + where;

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBase);

            if (resultSet.next())
                result = resultSet.getInt(1);
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "int maxId(Object entity, String where)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sqlBase));
            throw new Exception(ex.getMessage());
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }

        return result;
    }

    private void lastID(Object entity, String whereCondition) throws Exception
    {
        enabledContext = false;
        Statement statement = null;
        ResultSet resultSet = null;
        String sqlBase = "";
        try
        {
            Class cls = entity.getClass();
            SQLHelper sql_helper = new SQLHelper();
            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);

            if (primaryKeyName.isEmpty())
                return;

            String className = cls.getSimpleName().toLowerCase();
            sqlBase = "SELECT MAX(" + primaryKeyName + ") FROM " + className;

            if (!whereCondition.isEmpty())
                sqlBase += " WHERE " + whereCondition;

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBase);
            if (resultSet.next())
            {
                enabledContext = false;
                int obtainedId = resultSet.getInt(1);
                Field f = entity.getClass().getDeclaredField(primaryKeyName);
                f.setAccessible(true);
                f.set(entity, obtainedId);
            }
        }
        catch (Exception ex)
        {
            logger.newNofication(
                    new PersistenceLog(
                            this.getClass().getName(),
                            "void lastID(Object entity, String whereCondition)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            sqlBase));
            throw new Exception(ex.getMessage());
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }
    }

}
