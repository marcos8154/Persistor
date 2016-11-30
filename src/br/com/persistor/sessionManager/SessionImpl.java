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
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;
import br.com.persistor.enums.PRIMARYKEY_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.JoinableObject;
import br.com.persistor.generalClasses.PersistenceLog;
import br.com.persistor.interfaces.IPersistenceLogger;
import java.io.InputStream;
import java.math.BigDecimal;
import br.com.persistor.interfaces.Session;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SessionImpl implements Session
{

    private Connection connection = null;
    private DBConfig config = null;
    private PersistenceContext context = null;
    private IPersistenceLogger logger = null;

    private boolean enabledContext = true;
    private boolean showSql = true;
    private boolean isRollbacked = false;

    public SessionImpl(Connection connection)
    {
        this.connection = connection;
        this.context = new PersistenceContext();
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

    public void setConfig(DBConfig config)
    {
        this.config = config;
        this.context.Initialize(config.getPersistenceContext());
        if (config.getPersistenceLogger() == null || config.getPersistenceLogger().isEmpty())
        {
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS NOT FOUND. CREATE OR INQUIRE THE PERSISTENCE LOGGER CLASS TO AVOID PROBLEMS ****");
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS NOT FOUND. CREATE OR INQUIRE THE PERSISTENCE LOGGER CLASS TO AVOID PROBLEMS ****");
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS NOT FOUND. CREATE OR INQUIRE THE PERSISTENCE LOGGER CLASS TO AVOID PROBLEMS ****");
        }

        try
        {
            this.logger = (IPersistenceLogger) Class.forName(config.getPersistenceLogger()).newInstance();
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS INITIALIZATION ERROR AT: \n" + Util.getFullStackTrace(ex));
            System.err.println("Persistor: *** PERSISTENCE LOGGER CLASS NOT FOUND. CREATE OR INQUIRE THE PERSISTENCE LOGGER CLASS TO AVOID PROBLEMS ****");
        }
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

    private boolean extendsEntity(Class entityCls)
    {
        for (Field field : entityCls.getFields())
        {
            if (field.getName() == "saved")
            {
                return true;
            }
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
                if(value instanceof String)
                    return (Integer.parseInt(value.toString()) > 0);
                
                if (value instanceof Number)
                    return (Integer.parseInt(value.toString()) > 0);
            }
        }
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }

        return false;
    }

    @Override
    public <T> List<T> getList(T t)
    {
        try
        {
            Field f = t.getClass().getField("ResultList");
            List<Object> list = (List<Object>) f.get(t);
            List<T> resultList = new ArrayList<>();
            for (Object obj : list)
            {
                resultList.add((T) obj);
            }
            return resultList;
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "<T> List<T> getList(T t)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
        return new ArrayList<>();
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
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "Query createQuery(Object entity, String queryCommand)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
        return null;
    }

    private String whereConditionGetLastID = "";

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
                        java.sql.Date dt = java.sql.Date.valueOf(format.format(date));
                        Calendar calendar = Calendar.getInstance();

                        preparedStatement.setDate(parameterIndex, dt);
                        parameterIndex++;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }
    }

    private void SaveOrUpdateForeignObjects(Object entity, boolean isUpdateMode) throws Exception
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
                if(oneToOne.join_type() == JOIN_TYPE.LEFT)
                    continue;
                
                String field = "set" + oneToOne.source().substring(0, 1).toUpperCase() + oneToOne.source().substring(1);

                if (methodHasValue(entity, field))
                {
                    continue;
                }

                SessionImpl session = new SessionImpl(this.connection);

                if (isUpdateMode)
                {
                    session.update(object);
                }
                else
                {
                    session.save(object);
                }

                SQLHelper helper = new SQLHelper();
                Method pkObject = object.getClass().getMethod(helper.getPrimaryKeyMethodName(object));

                Method mtd = entity.getClass().getMethod(field, int.class);
                mtd.invoke(entity, pkObject.invoke(object));
            }
        }
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
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                rollback();
                return;
            }

            sql_helper.prepareInsert(entity);

            if (context.initialized)
            {
                if (!context.isEntitySet(entity))
                {
                    Exception ex = new Exception("Attach entity type '" + cls.getName() + "' failed bacause it was not found an EntitySet<> representation in Context");
                    logger.newNofication(new PersistenceLog(this.getClass().getName(), "void save(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                    rollback();
                    return;
                }
            }

            String sqlBase = sql_helper.getSqlBase();
            preparedStatement = connection.prepareStatement(sqlBase);
            SaveOrUpdateForeignObjects(entity, false);
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
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void save(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
        }
        finally
        {
            Util.closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void update(Object entity, String andCondition)
    {
        PreparedStatement preparedStatement = null;
        SQLHelper sql_helper = new SQLHelper();
        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void update(Object entity, String andCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                rollback();
                return;
            }

            sql_helper.prepareUpdate(entity, connection);
            String sqlBase = sql_helper.getSqlBase();
            SaveOrUpdateForeignObjects(entity, true);

            if (this.context.initialized)
            {
                if (context.getFromContext(entity) == null)
                {
                    Exception ex = new Exception("The entity type '" + entity.getClass().getName() + "' is not part of the model for the current context");
                    logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                    rollback();
                    return;
                }
            }

            sqlBase += " AND " + andCondition;
            preparedStatement = connection.prepareStatement(sqlBase);
            SaveOrUpdateForeignObjects(entity, true);
            loadPreparedStatement(preparedStatement, entity, true);
            preparedStatement.execute();
            Field fieldSaved = cls.getField("updated");
            fieldSaved.set(entity, true);

            System.out.println("Persistor: \n " + sqlBase);
            this.context.mergeEntity(entity);
        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void update(Object entity, String andCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
        }
        finally
        {
            Util.closePreparedStatement(preparedStatement);
        }
    }

    public boolean isVersionViolation = false;

    @Override
    public void update(Object entity)
    {
        PreparedStatement preparedStatement = null;
        SQLHelper sql_helper = new SQLHelper();
        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void update(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                rollback();
                return;
            }

            sql_helper.prepareUpdate(entity, connection);
            String sqlBase = sql_helper.getSqlBase();

            if (this.context.initialized)
            {
                if (context.getFromContext(entity) == null)
                {
                    Exception ex = new Exception("The entity type '" + entity.getClass().getName() + "' is not part of the model for the current context");
                    logger.newNofication(new PersistenceLog(this.getClass().getName(), "void update(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                    rollback();
                    return;
                }
            }

            preparedStatement = connection.prepareStatement(sqlBase);
            SaveOrUpdateForeignObjects(entity, true);
            loadPreparedStatement(preparedStatement, entity, true);
            preparedStatement.execute();
            Field fieldSaved = cls.getField("updated");
            fieldSaved.set(entity, true);

            System.out.println("Persistor: \n " + sqlBase);
            this.context.mergeEntity(entity);

        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void update(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
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
        try
        {
            Class cls = entity.getClass();
            String tableName = cls.getSimpleName().toLowerCase();

            if (!extendsEntity(cls))
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
                rollback();
                return;
            }

            String sqlBase = "";

            sql_helper.prepareDelete(entity);
            if (sql_helper.getPrimaryKeyName().isEmpty())
                sqlBase = "delete from " + tableName + " where " + and_or_where_condition;
            else
            {
                sqlBase = sql_helper.getSqlBase();
                sqlBase += and_or_where_condition;
            }

            if (this.context.initialized)
            {
                if (context.getFromContext(entity) == null)
                {
                    Exception ex = new Exception("The entity type '" + entity.getClass().getName() + "' is not part of the model for the current context");
                    logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                    rollback();
                    return;
                }
            }

            preparedStatement = connection.prepareStatement(sqlBase);
            preparedStatement.execute();
            Field fieldDel = cls.getField("deleted");
            fieldDel.set(entity, true);
            this.context.removeFromContext(entity);
            System.out.println("Persistor: \n " + sqlBase);
        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity, String andCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
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
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
                rollback();
                return;
            }

            sql_helper.prepareDelete(entity);
            String sqlBase = sql_helper.getSqlBase();

            if (this.context.initialized)
            {
                if (context.getFromContext(entity) == null)
                {
                    Exception ex = new Exception("The entity type '" + entity.getClass().getName() + "' is not part of the model for the current context");
                    logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                    rollback();
                    return;
                }
            }

            System.out.println("Persistor: \n " + sqlBase);
            preparedStatement = connection.prepareStatement(sqlBase);
            preparedStatement.execute();
            Field fieldDel = cls.getField("deleted");
            fieldDel.set(entity, true);
            this.context.removeFromContext(entity);
        }
        catch (Exception ex)
        {
            rollback();
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
        }
        finally
        {
            Util.closePreparedStatement(preparedStatement);
        }
    }

    private boolean loadEntity(Object entity, ResultSet resultSet) throws Exception
    {
        boolean result = false;
        try
        {
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

                        if (method.isAnnotationPresent(OneToOne.class));

                        if (method.getReturnType() == boolean.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, boolean.class);
                            invokeMethod.invoke(entity, resultSet.getBoolean(columnName));
                            continue;
                        }

                        if (method.getReturnType() == int.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, int.class);
                            invokeMethod.invoke(entity, resultSet.getInt(columnName));
                            continue;
                        }

                        if (method.getReturnType() == double.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, double.class);
                            invokeMethod.invoke(entity, resultSet.getDouble(columnName));
                            continue;
                        }

                        if (method.getReturnType() == float.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, float.class);
                            invokeMethod.invoke(entity, resultSet.getFloat(columnName));
                            continue;
                        }

                        if (method.getReturnType() == short.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, short.class);
                            invokeMethod.invoke(entity, resultSet.getShort(columnName));
                            continue;
                        }

                        if (method.getReturnType() == long.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, long.class);
                            invokeMethod.invoke(entity, resultSet.getLong(columnName));
                            continue;
                        }

                        if (method.getReturnType() == String.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, String.class);
                            invokeMethod.invoke(entity, resultSet.getString(columnName));
                            continue;
                        }

                        if (method.getReturnType() == java.util.Date.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, java.util.Date.class);
                            invokeMethod.invoke(entity, resultSet.getDate(columnName));
                            continue;
                        }

                        if (method.getReturnType() == byte.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, byte.class);
                            invokeMethod.invoke(entity, resultSet.getByte(columnName));
                            continue;
                        }

                        if (method.getReturnType() == BigDecimal.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, BigDecimal.class);
                            invokeMethod.invoke(entity, resultSet.getBigDecimal(columnName));
                            continue;
                        }

                        if (method.getReturnType() == InputStream.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, InputStream.class);
                            invokeMethod.invoke(entity, (InputStream) resultSet.getBinaryStream(columnName));
                            continue;
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
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

                    if (oneToOne.load() == LOAD.MANUAL)
                        return false;

                    return true;
                }
            }
        }
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }

        return false;
    }

    private <T> T executeJoin(T entity, int id) throws Exception
    {
        try
        {
            Class cls = entity.getClass();
            Join join = new Join(entity);
            join.setRestartEntityInstance(false);
            List<JoinableObject> objectsToJoin = new ArrayList<>();

            if (context.initialized)
            {
                if (!context.isEntitySet(entity))
                {
                    Exception ex = new Exception("Attach entity type '" + cls.getName() + "' failed bacause it was not found an EntitySet<> representation in Context");
                    logger.newNofication(new PersistenceLog(this.getClass().getName(), "void save(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
                    rollback();
                    return null;
                }
            }

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    Class clss = Class.forName(method.getReturnType().getName());
                    java.lang.reflect.Constructor ctor = clss.getConstructor();
                    Object entityObj = ctor.newInstance();
                    OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);

                    if (oneToOne.load() == LOAD.MANUAL)
                    {
                        continue;
                    }

                    String sourceName = cls.getSimpleName() + "." + oneToOne.source();
                    String targetName = entityObj.getClass().getSimpleName() + "." + oneToOne.target();
                    join.addJoin(oneToOne.join_type(), entityObj, sourceName + " = " + targetName);
                    JoinableObject objToJoin = new JoinableObject();
                    objToJoin.result_type = RESULT_TYPE.UNIQUE;
                    objToJoin.objectToJoin = entityObj;
                    objectsToJoin.add(objToJoin);
                }

                if (method.isAnnotationPresent(OneToMany.class))
                {
                    Class clss = Class.forName(method.getReturnType().getName());
                    java.lang.reflect.Constructor ctor = clss.getConstructor();
                    Object entityObj = ctor.newInstance();
                    OneToMany oneToMany = (OneToMany) method.getAnnotation(OneToMany.class);

                    if (oneToMany.load() == LOAD.MANUAL)
                    {
                        continue;
                    }

                    String sourceName = cls.getSimpleName() + "." + oneToMany.source();
                    String targetName = entityObj.getClass().getSimpleName() + "." + oneToMany.target();
                    join.addJoin(oneToMany.join_type(), entityObj, sourceName + " = " + targetName);
                    JoinableObject objToJoin = new JoinableObject();
                    objToJoin.result_type = RESULT_TYPE.MULTIPLE;
                    objToJoin.objectToJoin = entityObj;
                    objectsToJoin.add(objToJoin);
                }
            }

            if (join.joinCount > 0)
            {
                SQLHelper helper = new SQLHelper();
                String pkName = helper.getPrimaryKeyFieldName(entity);
                join.addFinalCondition("where " + cls.getSimpleName().toLowerCase() + "." + pkName + " = " + id);
                join.execute(this);

                entity = join.getEntity(entity.getClass());
                if (join.hasAllLoaded)
                {
                    if (entity != null)
                        context.addToContext(entity);
                    return entity;
                }
                if (entity == null)
                    return entity;

                for (JoinableObject object : objectsToJoin)
                {
                    if (object.result_type == RESULT_TYPE.UNIQUE)
                    {
                        object.objectToJoin = join.getEntity(object.objectToJoin.getClass());

                        Method method = entity.getClass().getMethod("set" + object.objectToJoin.getClass().getSimpleName(), object.objectToJoin.getClass());
                        method.invoke(entity, object.objectToJoin);
                    }

                    if (object.result_type == RESULT_TYPE.MULTIPLE)
                    {
                        Class clss = object.objectToJoin.getClass();
                        Field f = clss.getField("ResultList");
                        f.set(object.objectToJoin, join.getList(object.objectToJoin));
                        Method method = entity.getClass().getMethod("set" + object.objectToJoin.getClass().getSimpleName(), object.objectToJoin.getClass());
                        method.invoke(entity, object.objectToJoin);
                    }
                }
            }
            this.context.addToContext(entity);
        }
        catch (Exception ex)
        {
            entity = null;
            throw new Exception(ex.getMessage());
        }

        return entity;
    }

    @Override
    public void onID(Object entity, int id)
    {
        SQLHelper sql_helper = new SQLHelper();
        Statement statement = null;
        try
        {
            Class cls = entity.getClass();
            sql_helper.prepareBasicSelect(entity, id);

            if (!extendsEntity(cls))
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                rollback();
                return;
            }

            if (hasJoinableObjects(entity))
            {
                entity = executeJoin(entity, id);
                return;
            }

            String sqlBase = sql_helper.getSqlBase();
            Field field = cls.getField("mountedQuery");
            field.set(entity, sqlBase);

            if (enabledContext)
            {
                if (context.findByID(entity, id) != null)
                {
                    entity = context.findByID(entity, id);
                    return;
                }
            }

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlBase);
            if (loadEntity(entity, resultSet))
            {
                if (showSql)
                    System.out.println("Persistor: \n " + sqlBase);
                if (enabledContext)
                    this.context.addToContext(entity);
            }
            this.enabledContext = true;
            this.showSql = true;
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void onID(Object entity, int id)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
        }
        finally
        {
            if (statement != null)
            {
                Util.closeStatement(statement);
            }
        }
    }

    @Override
    public <T> T onID(Class entityCls, int id)
    {
        SQLHelper sql_helper = new SQLHelper();
        Statement statement = null;
        Object entity = null;
        try
        {
            entity = entityCls.newInstance();

            sql_helper.prepareBasicSelect(entity, id);

            if (!extendsEntity(entityCls))
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + entityCls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "void delete(Object entity)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                rollback();
                return null;
            }

            if (hasJoinableObjects(entity))
            {
                entity = executeJoin(entity, id);
                if (entity == null)
                    return (T) entityCls.newInstance();
                else
                    return (T) entity;
            }

            String sqlBase = sql_helper.getSqlBase();
            Field field = entityCls.getField("mountedQuery");
            field.set(entity, sqlBase);

            if (context.findByID(entity, id) != null)
                return (T) context.findByID(entity, id);

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlBase);
            if (loadEntity(entity, resultSet))
            {
                System.out.println("Persistor: \n " + sqlBase);
                context.addToContext(entity);
            }
            enabledContext = true;
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "<T> T onID(Class entityCls, int id)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
        }
        finally
        {
            if (statement != null)
            {
                Util.closeStatement(statement);
            }
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
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void commit", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
    }

    @Override
    public void close()
    {
        try
        {
            connection.close();
            context.clear();
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void close()", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
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
            logger.newNofication(new PersistenceLog(this.getClass().getName(), "void rollback", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
    }

    private int maxId(Object entity, String where) throws Exception
    {
        Statement statement = null;
        ResultSet resultSet = null;
        int result = 0;
        try
        {
            Class cls = entity.getClass();
            SQLHelper sql_helper = new SQLHelper();
            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);

            String className = cls.getSimpleName().toLowerCase();
            String sqlBase = "select max(" + primaryKeyName + ") " + primaryKeyName + " from " + className;

            if (!where.isEmpty())
            {
                sqlBase += " where " + where;
            }

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBase);

            if (resultSet.next())
                result = resultSet.getInt(1);
        }
        catch (Exception ex)
        {
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
        showSql = false;
        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            Class cls = entity.getClass();
            SQLHelper sql_helper = new SQLHelper();
            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);

            if (primaryKeyName.isEmpty())
                return;

            String className = cls.getSimpleName().toLowerCase();
            String sqlBase = "select max(" + primaryKeyName + ") from " + className;
            if (!whereCondition.isEmpty())
            {
                sqlBase += " where " + whereCondition;
            }
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBase);
            if (resultSet.next())
            {
                enabledContext = false;
                int obtainedId = resultSet.getInt(1);
                onID(entity, obtainedId);
            }
        }
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }
    }

    @Override
    public <T> T first(Class cls, String whereCondition)
    {
        Statement statement = null;
        ResultSet resultSet = null;
        String sqlBase = "";
        try
        {
            java.lang.reflect.Constructor constructor = cls.getConstructor();
            Object entity = constructor.newInstance();
            if (context.getFromContext(entity) != null)
                return (T) context.getFromContext(entity);
            SQLHelper sql_helper = new SQLHelper();
            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);

            if (!extendsEntity(cls))
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "<T> T First(Class cls, String whereCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                rollback();
                return null;
            }

            String className = cls.getSimpleName().toLowerCase();
            sqlBase = "select min(" + primaryKeyName + ") from " + className;

            if (!whereCondition.isEmpty())
            {
                sqlBase += " where " + whereCondition;
            }

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
            return (T) entity;
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog(this.getClass().getName(), " <T> T First(Class cls, String whereCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sqlBase));
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);
        }
        return null;
    }

    @Override
    public <T> T last(Class cls, String whereCondition)
    {
        String sqlBase = "";
        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            java.lang.reflect.Constructor constructor = cls.getConstructor();
            Object entity = constructor.newInstance();
            if (context.getFromContext(entity) != null)
                return (T) context.getFromContext(entity);
            SQLHelper sql_helper = new SQLHelper();
            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);

            if (!extendsEntity(cls))
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "<T> T Last(Class cls, String whereCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sql_helper.getSqlBase()));
                rollback();
                return null;
            }

            String className = cls.getSimpleName().toLowerCase();
            sqlBase = "select max(" + primaryKeyName + ") from " + className;

            if (!whereCondition.isEmpty())
            {
                sqlBase += " where " + whereCondition;
            }

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
    public int count(Class entityClass, String whereCondition)
    {
        String sql = "";
        int result = 0;

        if (whereCondition == null)
            whereCondition = "";

        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            java.lang.reflect.Constructor constructor = entityClass.getConstructor();
            Object entity = constructor.newInstance();

            String tableName = (entity.getClass().getSimpleName().toLowerCase());
            sql = "select count(*) from " + tableName;
            if (!whereCondition.isEmpty())
                sql += " where " + whereCondition;

            if (!extendsEntity(entityClass))
            {
                Exception ex = new Exception("\nPersistor warning: the class '" + entityClass.getName() + "' not extends Entity. Operation is stoped.\"");
                logger.newNofication(new PersistenceLog(this.getClass().getName(), "<T> T Last(Class cls, String whereCondition)", Util.getDateTime(), Util.getFullStackTrace(ex), sql));
                rollback();
                return 0;
            }

            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next())
                result = resultSet.getInt(1);

            System.out.println(sql);
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
}
