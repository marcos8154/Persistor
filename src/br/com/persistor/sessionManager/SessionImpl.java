package br.com.persistor.sessionManager;

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
import br.com.persistor.connectionManager.DataSource;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;
import br.com.persistor.enums.PRIMARYKEY_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.JoinableObject;
import java.io.InputStream;
import java.math.BigDecimal;
import br.com.persistor.interfaces.Session;

public class SessionImpl implements Session
{

    private Connection connection = null;
    private DBConfig config = null;

    public SessionImpl(Connection connection)
    {
        this.connection = connection;
    }

    @Override
    public DBConfig getConfig()
    {
        return this.config;
    }

    public void setConfig(DBConfig config)
    {
        this.config = config;
    }

    @Override
    public Connection getActiveConnection()
    {
        return this.connection;
    }

    @Override
    public void closeStatement(Statement statement)
    {
        try
        {
            statement.close();
        } catch (Exception ex)
        {
            System.err.println("Persistor: error at: \n" + ex.getMessage());
        }
    }

    @Override
    public void closeResultSet(ResultSet resultSet)
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

        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error at: \n" + ex.getMessage());
        }
    }

    @Override
    public void closePreparedStatement(PreparedStatement preparedStatement)
    {
        try
        {
            preparedStatement.close();
        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
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

    private boolean methodHasValue(Object entity, String methodName)
    {
        try
        {
            Method method;
            if (methodName.startsWith("is"))
            {
                methodName = "is" + methodName.substring(2, methodName.length());
                method = entity.getClass().getMethod(methodName);
            } else
            {
                methodName = "get" + methodName.substring(3, methodName.length());
                method = entity.getClass().getMethod(methodName);
            }

            Object value = method.invoke(entity);

            if (value != null && (int) value != 0)
            {
                return true;
            }

        } catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }

        return false;
    }

    @Override
    public Query createQuery(Object entity, String queryCommand)
    {
        Query query = new Query();
        query.createQuery(this, entity, queryCommand);

        return query;
    }

    public Object getAuxiliarPK_value(Object entity, Class cls, String name)
    {

        try
        {
            return cls.getMethod(name).invoke(entity);

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    public String getAuxiliarPK_name(Class entityCls)
    {
        for (Method method : entityCls.getMethods())
        {
            if (method.isAnnotationPresent(PrimaryKey.class))
            {
                PrimaryKey primaryKey = (PrimaryKey) method.getAnnotation(PrimaryKey.class);

                if (primaryKey.primarykey_type() == PRIMARYKEY_TYPE.AUXILIAR)
                {
                    return method.getName();
                }
            }
        }

        return null;
    }

    private String whereConditionGetLastID = "";

    private void loadPreparedStatement(PreparedStatement preparedStatement, Object entity, boolean ignorePrimaryKey)
    {
        try
        {
            Class cls = entity.getClass();

            int parameterIndex = 1;

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    if (ignorePrimaryKey)
                    {
                        continue;
                    }

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

                        if (getAuxiliarPK_name(cls) != null)
                        {
                            String auxPK_name = getAuxiliarPK_name(cls);
                            String columnAuxPK_name = auxPK_name.replace("get", "").toLowerCase();

                            whereConditionGetLastID = columnAuxPK_name + " = " + getAuxiliarPK_value(entity, cls, auxPK_name);

                            nextID = (this.maxId(entity, whereConditionGetLastID) + 1);
                            preparedStatement.setInt(parameterIndex, nextID);
                            parameterIndex++;

                            continue;
                        }

                        if (getAuxiliarPK_name(cls) == null)
                        {
                            nextID = (this.maxId(entity, "") + 1);
                            preparedStatement.setInt(parameterIndex, nextID);
                            parameterIndex++;

                            continue;
                        }
                    } else
                    {
                        continue;
                    }
                }

                if (method.isAnnotationPresent(OneToOne.class))
                {
                    continue;
                }
                if (method.isAnnotationPresent(OneToMany.class))
                {
                    continue;
                }

                if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                {
                    if (method.isAnnotationPresent(Version.class))
                    {
                        preparedStatement.setInt(parameterIndex, 1);
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
                        java.sql.Date dt = new java.sql.Date(date.getYear(), date.getMonth(), date.getDay());

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(date.getYear(), date.getMonth(), date.getDay(), date.getHours(), date.getMinutes(), date.getSeconds());

                        preparedStatement.setDate(parameterIndex, dt, calendar);
                        parameterIndex++;
                    }
                }
            }

        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error at:");
            ex.printStackTrace();
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
                    {
                        continue;
                    }

                    Class clss = Class.forName(method.getReturnType().getName());
                    java.lang.reflect.Constructor ctor = clss.getConstructor();
                    object = ctor.newInstance();
                }

                OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);

                String field = "set" + oneToOne.source().substring(0, 1).toUpperCase() + oneToOne.source().substring(1);

                if (methodHasValue(entity, field))
                {
                    continue;
                }

                SessionImpl session = new SessionImpl(this.connection);

                if (isUpdateMode)
                {
                    session.update(object);
                } else
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

        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            SQLHelper sql_Helper = new SQLHelper();
            sql_Helper.prepareInsert(entity);
            String sqlBase = sql_Helper.getSqlBase();

            preparedStatement = connection.prepareStatement(sqlBase);

            SaveOrUpdateForeignObjects(entity, false);
            loadPreparedStatement(preparedStatement, entity, false);

            preparedStatement.execute();
            System.out.println("Persistor: \n " + sqlBase);

            Field fieldSaved = cls.getField("saved");
            fieldSaved.set(entity, true);

            lastID(entity, whereConditionGetLastID);

        } catch (Exception ex)
        {
            System.err.println("Persistor: save error at: \n");
            ex.printStackTrace();
            rollback();
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void update(Object entity, String andCondition)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            SQLHelper sql_Helper = new SQLHelper();
            sql_Helper.prepareUpdate(entity, connection);

            if (sql_Helper.updateStatus == 0)
            {
                isVersionViolation = true;
                return;
            }

            Class cls = entity.getClass();
            String sqlBase = sql_Helper.getSqlBase();

            SaveOrUpdateForeignObjects(entity, true);

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            sqlBase += " AND " + andCondition;

            preparedStatement = connection.prepareStatement(sqlBase);

            SaveOrUpdateForeignObjects(entity, true);
            loadPreparedStatement(preparedStatement, entity, true);

            preparedStatement.execute();

            Field fieldSaved = cls.getField("updated");
            fieldSaved.set(entity, true);

            System.out.println("Persistor: \n " + sqlBase);

        } catch (Exception ex)
        {
            System.err.println("Persistor: update error at: \n");
            ex.printStackTrace();
            rollback();
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    public boolean isVersionViolation = false;

    @Override
    public void update(Object entity)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            SQLHelper sql_Helper = new SQLHelper();
            sql_Helper.prepareUpdate(entity, connection);

            if (sql_Helper.updateStatus == 0)
            {
                isVersionViolation = true;
                return;
            }

            String sqlBase = sql_Helper.getSqlBase();

            preparedStatement = connection.prepareStatement(sqlBase);

            SaveOrUpdateForeignObjects(entity, true);
            loadPreparedStatement(preparedStatement, entity, true);

            preparedStatement.execute();

            Field fieldSaved = cls.getField("updated");
            fieldSaved.set(entity, true);

            System.out.println("Persistor: \n " + sqlBase);

        } catch (Exception ex)
        {
            System.err.println("Persistor: update error at: \n");
            ex.printStackTrace();
            rollback();
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void delete(Object entity, String andCondition)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            Class cls = entity.getClass();

            SQLHelper sql_helper = new SQLHelper();
            sql_helper.prepareDelete(entity);
            String sqlBase = sql_helper.getSqlBase();

            sqlBase += " AND " + andCondition;

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            System.out.println("Persistor: \n " + sqlBase);

            preparedStatement = connection.prepareStatement(sqlBase);
            preparedStatement.execute();

            Field fieldDel = cls.getField("deleted");
            fieldDel.set(entity, true);

        } catch (Exception ex)
        {
            System.err.println("Persistor: delete error at: \n");
            ex.printStackTrace();
            rollback();
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void delete(Object entity)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            Class cls = entity.getClass();

            SQLHelper sql_helper = new SQLHelper();
            sql_helper.prepareDelete(entity);

            String sqlBase = sql_helper.getSqlBase();

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            System.out.println("Persistor: \n " + sqlBase);

            preparedStatement = connection.prepareStatement(sqlBase);
            preparedStatement.execute();

            Field fieldDel = cls.getField("deleted");
            fieldDel.set(entity, true);

        } catch (Exception ex)
        {
            System.err.println("Persistor: delete error at: \n");
            ex.printStackTrace();
            rollback();
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    private void loadEntity(Object entity, ResultSet resultSet)
    {
        try
        {
            Class cls = entity.getClass();

            while (resultSet.next())
            {
                for (Method method : cls.getMethods())
                {
                    if (method.getName().startsWith("get") || method.getName().startsWith("is") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        //   Method oneToOneMtd = cls.getMethod(method.getName().replace("set", "get"));

                        if (method.isAnnotationPresent(OneToOne.class))
                        {
                            continue;
                        }

                        String name;
                        String fieldName;

                        if (method.getName().startsWith("is"))
                        {
                            name = (method.getName().substring(2, method.getName().length())).toLowerCase();
                            fieldName = "set" + method.getName().substring(2, method.getName().length());
                        } else
                        {
                            name = (method.getName().substring(3, method.getName().length())).toLowerCase();
                            fieldName = "set" + method.getName().substring(3, method.getName().length());
                        }

                        if (method.isAnnotationPresent(OneToOne.class));

                        if (method.getReturnType() == boolean.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, boolean.class);
                            invokeMethod.invoke(entity, resultSet.getBoolean(name));
                            continue;
                        }

                        if (method.getReturnType() == int.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, int.class);
                            invokeMethod.invoke(entity, resultSet.getInt(name));
                            continue;
                        }

                        if (method.getReturnType() == double.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, double.class);
                            invokeMethod.invoke(entity, resultSet.getDouble(name));
                            continue;
                        }

                        if (method.getReturnType() == float.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, float.class);
                            invokeMethod.invoke(entity, resultSet.getFloat(name));
                            continue;
                        }

                        if (method.getReturnType() == short.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, short.class);
                            invokeMethod.invoke(entity, resultSet.getShort(name));
                            continue;
                        }

                        if (method.getReturnType() == long.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, long.class);
                            invokeMethod.invoke(entity, resultSet.getLong(name));
                            continue;
                        }

                        if (method.getReturnType() == String.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, String.class);
                            invokeMethod.invoke(entity, resultSet.getString(name));
                            continue;
                        }

                        if (method.getReturnType() == java.util.Date.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, java.util.Date.class);
                            invokeMethod.invoke(entity, resultSet.getDate(name));
                            continue;
                        }

                        if (method.getReturnType() == byte.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, byte.class);
                            invokeMethod.invoke(entity, resultSet.getByte(name));
                            continue;
                        }

                        if (method.getReturnType() == BigDecimal.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, BigDecimal.class);
                            invokeMethod.invoke(entity, resultSet.getBigDecimal(name));
                            continue;
                        }

                        if (method.getReturnType() == InputStream.class)
                        {
                            Method invokeMethod = entity.getClass().getMethod(fieldName, InputStream.class);
                            invokeMethod.invoke(entity, (InputStream) resultSet.getBinaryStream(name));
                            continue;
                        }
                    }
                }
            }
        } catch (Exception ex)
        {

        }
    }

    private boolean hasJoinableObjects(Object entity)
    {
        try
        {
            Class cls = entity.getClass();

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    return true;
                }
            }
        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error at");
            ex.printStackTrace();
        }

        return false;
    }

    private void executeJoin(Object entity, int id)
    {
        try
        {
            Class cls = entity.getClass();
            Join join = new Join(entity);
            List<JoinableObject> objectsToJoin = new ArrayList<>();

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

                join.addFinalCondition("WHERE " + cls.getSimpleName().toLowerCase() + "." + pkName + " = " + id);
                join.execute(this);
                join.getResultObj(entity);

                for (JoinableObject object : objectsToJoin)
                {
                    if (object.result_type == RESULT_TYPE.UNIQUE)
                    {
                        join.getResultObj(object.objectToJoin);

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

                System.out.println("Persistor: \n" + join.mountedQuery);
            }
        } catch (Exception ex)
        {
            System.err.println("Persistor: join error at:");
            ex.printStackTrace();
        }
    }

    public void loadWithJoin(Object sourceEntity, Object targetEntity)
    {
        try
        {
            int mode = 0;
            Class sourceEntityClass = sourceEntity.getClass();
            Join join = new Join(sourceEntity);
            String finalCondition = "";
            
            for (Method method : sourceEntityClass.getMethods())
            {
                if (method.getReturnType() == targetEntity.getClass())
                {
                    if (method.isAnnotationPresent(OneToOne.class))
                    {
                        OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);
                        if (oneToOne.load() == LOAD.AUTO)
                        {
                            throw new Exception("Persistor: not allowed join between " + sourceEntityClass.getSimpleName() + " and " + method.getReturnType().getSimpleName() + ". \nLOAD mode in " + sourceEntityClass.getSimpleName() + "." + method.getName() + " is AUTO!");
                        }
                        join.addJoin(oneToOne.join_type(), targetEntity, null);
                        finalCondition = oneToOne.source() + " = " + oneToOne.target();
                        mode = 1;
                        break;
                    }

                    if (method.isAnnotationPresent(OneToMany.class))
                    {
                        if (method.isAnnotationPresent(OneToMany.class))
                        {
                            OneToMany oneToMany = (OneToMany) method.getAnnotation(OneToMany.class);
                            if (oneToMany.load() == LOAD.AUTO)
                            {
                                throw new Exception("Persistor: not allowed join between " + sourceEntityClass.getSimpleName() + " and " + method.getReturnType().getSimpleName() + ". \nLOAD mode in " + sourceEntityClass.getSimpleName() + "." + method.getName() + " is AUTO!");
                            }
                            join.addJoin(oneToMany.join_type(), targetEntity, null);
                            finalCondition = oneToMany.source() + " = " + oneToMany.target();
                            mode = 2;
                            break;
                        }
                    }
                }
            }
            
            join.addFinalCondition(finalCondition);
            join.execute(this);
           
            if(mode == 1) //OneToOne
            {
                join.getResultObj(sourceEntity);
                join.getResultObj(targetEntity);
            }
            
            if(mode == 2) // OneToMany
            {
                join.getResultList(sourceEntity);
                join.getResultList(targetEntity);
            }
        } 
        catch (Exception ex)
        {
            System.out.println("Persistor: loadWithJoin error at: \n");
            ex.getMessage();
        }
    }

    @Override
    public void onID(Object entity, int id)
    {
        Statement statement = null;

        try
        {
            Class cls = entity.getClass();

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            if (hasJoinableObjects(entity))
            {
                executeJoin(entity, id);
                return;
            }

            SQLHelper helper = new SQLHelper();
            helper.prepareBasicSelect(entity, id);

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            String sqlBase = helper.getSqlBase();

            Field field = cls.getField("mountedQuery");
            field.set(entity, sqlBase);

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlBase);

            loadEntity(entity, resultSet);

            System.out.println("Persistor: \n " + sqlBase);

        } catch (Exception ex)
        {
            System.err.println("Persistor: load on id error at: \n" + ex.getMessage());
        } finally
        {
            if (statement != null)
            {
                closeStatement(statement);
            }
        }
    }

    @Override
    public Object onID(Class entityCls, int id)
    {
        Statement statement = null;
        Object obj = null;
        try
        {
            java.lang.reflect.Constructor constructor = entityCls.getConstructor();
            obj = constructor.newInstance();

            if (!extendsEntity(entityCls))
            {
                System.err.println("Persistor warning: the class '" + entityCls.getName() + "' not extends Entity. Operation is stoped.");
                return null;
            }

            String primaryKeyName = "";

            if (hasJoinableObjects(obj))
            {
                executeJoin(obj, id);
                return obj;
            }

            SQLHelper helper = new SQLHelper();
            primaryKeyName = helper.getPrimaryKeyFieldName(obj);
            helper.prepareBasicSelect(obj, id);

            String sqlBase = helper.getSqlBase();
            Field field = entityCls.getField("mountedQuery");
            field.set(obj, sqlBase);

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlBase);
            loadEntity(obj, resultSet);

            System.out.println("Persistor: \n " + sqlBase);

        } catch (Exception ex)
        {
            System.err.println("Persistor: load on id error at: \n" + ex.getMessage());
        } finally
        {
            if (statement != null)
            {
                closeStatement(statement);
            }
        }

        return obj;
    }

    @Override
    public void commit()
    {
        try
        {
            connection.commit();
        } catch (Exception ex)
        {
            System.err.println("Persistor: commit error at: \n" + ex.getMessage());
            System.err.println("Executing rollback...");

            rollback();
        }
    }

    @Override
    public void close()
    {
        try
        {
            connection.close();
        } catch (Exception ex)
        {
            System.err.println("Persistor: close session error at: \n" + ex.getMessage());
        }
    }

    @Override
    public void rollback()
    {
        try
        {
            System.out.println("Rollbacking...");
            connection.rollback();
        } catch (Exception ex)
        {
            System.err.println("Persistor rollback error at: \n" + ex.getMessage());
        }
    }

    public int maxId(Object entity, String where)
    {
        Statement statement = null;
        int result = 0;
        try
        {
            Class cls = entity.getClass();
            SQLHelper sql_helper = new SQLHelper();

            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return 0;
            }

            String className = cls.getSimpleName().toLowerCase();
            String sqlBase = "select max(" + primaryKeyName + ") " + primaryKeyName + " from " + className;

            if (!where.isEmpty())
            {
                sqlBase += " where " + where;
            }

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlBase);

            String pkMethodName = sql_helper.getPrimaryKeyMethodName(entity).replace("get", "set");

            while (resultSet.next())
            {
                result = resultSet.getInt(primaryKeyName);
            }

        } catch (Exception ex)
        {
            System.err.println("Persistor: error at: \n" + ex.getMessage());
        } finally
        {
            this.closeStatement(statement);
        }

        return result;
    }

    private void lastID(Object entity, String whereCondition)
    {
        Statement statement = null;
        try
        {
            Class cls = entity.getClass();
            SQLHelper sql_helper = new SQLHelper();

            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(entity);

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            String className = cls.getName().replace(cls.getPackage().getName(), "");
            className = className.replace(".", "").toLowerCase();

            String sqlBase = "select max(" + primaryKeyName + ") " + primaryKeyName + " from " + className;
            if (!whereCondition.isEmpty())
            {
                sqlBase += " where " + whereCondition;
            }

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlBase);

            String pkMethodName = sql_helper.getPrimaryKeyMethodName(entity).replace("get", "set");

            while (resultSet.next())
            {
                String field = (pkMethodName);
                Method method = entity.getClass().getMethod(field, int.class);
                method.invoke(entity, resultSet.getObject(primaryKeyName));
            }

        } catch (Exception ex)
        {
            System.err.println("Persistor: error at: \n" + ex.getMessage());
        } finally
        {
            this.closeStatement(statement);
        }
    }

    @Override
    public Criteria createCriteria(Object entity, RESULT_TYPE result_type)
    {
        Criteria criteria = null;

        try
        {
            criteria = new Criteria(this, entity, result_type);

        } catch (Exception ex)
        {
            System.err.println("Persistor: create criteria error at \n");
            ex.printStackTrace();
        }

        return criteria;
    }
}