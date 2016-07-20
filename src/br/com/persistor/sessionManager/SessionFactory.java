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
import br.com.persistor.connection.DataSource;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.LOAD;
import br.com.persistor.enums.PRIMARYKEY_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.JoinableObject;
import br.com.persistor.interfaces.ISession;
import java.io.InputStream;
import java.math.BigDecimal;

public class SessionFactory implements ISession
{

    public Connection connection;
    DBConfig config;

    DataSource dataSource;

    public SessionFactory(Connection connectiion)
    {
        this.connection = connectiion;
    }

    public SessionFactory(DBConfig config)
    {
        try
        {
            connection = DataSource.getInstance(config).getConnection();
            this.config = config;
            connection.setAutoCommit(false);

        } catch (Exception ex)
        {
            System.err.println("Persistor: error at: \n" + ex.getMessage());
        }
    }

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

    public void closePreparedStatement(PreparedStatement ps)
    {
        try
        {
            ps.close();
        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    private boolean isNumber(Method method)
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

    private boolean extendsEntity(Class cls)
    {
        for (Field field : cls.getFields())
        {
            if (field.getName() == "saved")
            {
                return true;
            }
        }

        return false;
    }

    private boolean methodHasValue(Object obj, String methodName)
    {
        try
        {
            Method method;
            if (methodName.startsWith("is"))
            {
                methodName = "is" + methodName.substring(2, methodName.length());
                method = obj.getClass().getMethod(methodName);
            } else
            {
                methodName = "get" + methodName.substring(3, methodName.length());
                method = obj.getClass().getMethod(methodName);
            }

            Object value = method.invoke(obj);

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
    public Query createQuery(Object obj, String queryCommand)
    {
        Query query = new Query();
        query.createQuery(this, obj, queryCommand);

        return query;
    }

    public Object getAuxiliarPK_value(Object obj, Class cls, String name)
    {

        try
        {
            return cls.getMethod(name).invoke(obj);

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    public String getAuxiliarPK_name(Class cls)
    {
        for (Method method : cls.getMethods())
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

    private void loadPreparedStatement(PreparedStatement preparedStatement, Object obj, boolean ignorePrimaryKey)
    {
        try
        {
            Class cls = obj.getClass();

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
                            preparedStatement.setInt(parameterIndex, (int) method.invoke(obj));
                            parameterIndex++;
                            continue;
                        }

                        if (getAuxiliarPK_name(cls) != null)
                        {
                            String auxPK_name = getAuxiliarPK_name(cls);
                            String columnAuxPK_name = auxPK_name.replace("get", "").toLowerCase();

                            whereConditionGetLastID = columnAuxPK_name + " = " + getAuxiliarPK_value(obj, cls, auxPK_name);

                            nextID = (this.maxId(obj, whereConditionGetLastID) + 1);
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
                        preparedStatement.setBoolean(parameterIndex, (boolean) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == int.class)
                    {
                        preparedStatement.setInt(parameterIndex, (int) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == double.class)
                    {
                        preparedStatement.setDouble(parameterIndex, (double) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == String.class)
                    {
                        preparedStatement.setString(parameterIndex, (String) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == short.class)
                    {
                        preparedStatement.setShort(parameterIndex, (short) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == long.class)
                    {
                        preparedStatement.setLong(parameterIndex, (long) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == float.class)
                    {
                        preparedStatement.setFloat(parameterIndex, (float) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }
                    if (method.getReturnType() == byte.class)
                    {
                        preparedStatement.setByte(parameterIndex, (byte) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == InputStream.class)
                    {
                        preparedStatement.setBinaryStream(parameterIndex, (InputStream) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == Date.class)
                    {
                        Date date = (java.util.Date) method.invoke(obj);
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

    private void SaveOrUpdateForeignObjects(Object obj, boolean isUpdateMode)
    {
        try
        {
            Class cls = obj.getClass();

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    Object object = method.invoke(obj);

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

                    if (methodHasValue(obj, field))
                    {
                        continue;
                    }

                    SessionFactory session = new SessionFactory(this.connection);

                    if (isUpdateMode)
                    {
                        session.update(object);
                    } else
                    {
                        session.save(object);
                    }

                    SQLHelper helper = new SQLHelper();
                    Method pkObject = object.getClass().getMethod(helper.getPrimaryKeyMethodName(object));

                    Method mtd = obj.getClass().getMethod(field, int.class);
                    mtd.invoke(obj, pkObject.invoke(object));
                }
            }
        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error at:");
            ex.printStackTrace();
        }
    }

    @Override
    public void save(Object obj)
    {
        PreparedStatement preparedStatement = null;

        try
        {
            Class cls = obj.getClass();

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            SQLHelper sql_Helper = new SQLHelper();
            sql_Helper.prepareInsert(obj);
            String sqlBase = sql_Helper.getSqlBase();

            preparedStatement = connection.prepareStatement(sqlBase);

            SaveOrUpdateForeignObjects(obj, false);
            loadPreparedStatement(preparedStatement, obj, false);

            preparedStatement.execute();
            System.out.println("Persistor: \n " + sqlBase);

            Field fieldSaved = cls.getField("saved");
            fieldSaved.set(obj, true);

            lastID(obj, whereConditionGetLastID);

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

    /**
     * UPDATE WITH AND CONDITIONS
     */
    @Override
    public void update(Object obj, String andCondition)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            SQLHelper sql_Helper = new SQLHelper();
            sql_Helper.prepareUpdate(obj, connection);

            if (sql_Helper.updateStatus == 0)
            {
                isVersionViolation = true;
                return;
            }

            Class cls = obj.getClass();
            String sqlBase = sql_Helper.getSqlBase();

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    Object object = method.invoke(obj);

                    if (object == null)
                    {
                        continue;
                    }

                    OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);

                    String field = "set" + oneToOne.source().substring(0, 1).toUpperCase() + oneToOne.source().substring(1);

                    if (methodHasValue(obj, field))
                    {
                        continue;
                    }

                    SessionFactory session = new SessionFactory(this.connection);

                    session.update(object);
                    //   session.commit();

                    SQLHelper helper = new SQLHelper();
                    Method pkObject = object.getClass().getMethod(helper.getPrimaryKeyMethodName(object));

                    Method mtd = obj.getClass().getMethod(field, int.class);
                    mtd.invoke(obj, pkObject.invoke(object));
                }
            }

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            sqlBase += " AND " + andCondition;

            preparedStatement = connection.prepareStatement(sqlBase);

            int parameterIndex = 1;

            for (Method method : cls.getMethods())
            {
                if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                {
                    if (method.isAnnotationPresent(PrimaryKey.class))
                    {
                        continue;
                    }
                    if (method.isAnnotationPresent(OneToOne.class))
                    {
                        continue;
                    }
                    if (method.isAnnotationPresent(OneToMany.class));

                    if (method.isAnnotationPresent(Version.class))
                    {
                        System.out.println(method.getName());
                        int version = Integer.parseInt(method.invoke(obj).toString());
                        preparedStatement.setInt(parameterIndex, (version + 1));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == boolean.class)
                    {
                        System.out.println(method.getName());
                        preparedStatement.setBoolean(parameterIndex, (boolean) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == int.class)
                    {
                        System.out.println(method.getName());
                        preparedStatement.setInt(parameterIndex, (int) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == double.class)
                    {
                        System.out.println(method.getName());
                        preparedStatement.setDouble(parameterIndex, (double) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == String.class)
                    {
                        System.out.println(method.getName());
                        preparedStatement.setString(parameterIndex, (String) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == short.class)
                    {
                        System.out.println(method.getName());
                        preparedStatement.setShort(parameterIndex, (short) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == long.class)
                    {
                        System.out.println(method.getName());
                        preparedStatement.setLong(parameterIndex, (long) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == float.class)
                    {
                        System.out.println(method.getName());
                        preparedStatement.setFloat(parameterIndex, (float) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == byte.class)
                    {
                        System.out.println(method.getName());
                        preparedStatement.setByte(parameterIndex, (byte) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }

                    if (method.getReturnType() == InputStream.class)
                    {
                        preparedStatement.setBinaryStream(parameterIndex, (InputStream) method.invoke(obj));
                        parameterIndex++;
                        continue;
                    }
                    //if (method.getReturnType() == byte.class){ preparedStatement.setByte(parameterIndex, (byte)method.invoke(obj)); parameterIndex ++; continue;}

                    if (method.getReturnType() == Date.class)
                    {
                        Date date = (java.util.Date) method.invoke(obj);
                        java.sql.Date dt = new java.sql.Date(date.getYear(), date.getMonth(), date.getDay());

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(date.getYear(), date.getMonth(), date.getDay(), date.getHours(), date.getMinutes(), date.getSeconds());

                        preparedStatement.setDate(parameterIndex, dt, calendar);
                        parameterIndex++;
                        continue;
                    }
                }
            }

            preparedStatement.execute();

            Field fieldSaved = cls.getField("updated");
            fieldSaved.set(obj, true);

            System.out.println("Persistor: \n " + sqlBase);

        } catch (Exception ex)
        {
            rollback();
            System.err.println("Persistor: update error at: \n" + ex.getMessage());
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    public boolean isVersionViolation = false;

    @Override
    public void update(Object obj)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            Class cls = obj.getClass();

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            SQLHelper sql_Helper = new SQLHelper();
            sql_Helper.prepareUpdate(obj, connection);

            if (sql_Helper.updateStatus == 0)
            {
                isVersionViolation = true;
                return;
            }

            String sqlBase = sql_Helper.getSqlBase();

            preparedStatement = connection.prepareStatement(sqlBase);

            SaveOrUpdateForeignObjects(obj, true);
            loadPreparedStatement(preparedStatement, obj, true);

            preparedStatement.execute();

            Field fieldSaved = cls.getField("updated");
            fieldSaved.set(obj, true);

            System.out.println("Persistor: \n " + sqlBase);

        } catch (Exception ex)
        {
            System.err.println("Persistor: update error at: \n" + ex.getMessage());
            rollback();
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    /**
     * DELETE WITH AND CONDITIONS
     */
    @Override
    public void delete(Object obj, String andCondition)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            Class cls = obj.getClass();

            SQLHelper sql_helper = new SQLHelper();
            sql_helper.prepareDelete(obj);
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
            fieldDel.set(obj, true);

        } catch (Exception ex)
        {
            rollback();
            System.err.println("Persistor: delete error at: \n" + ex.getMessage());
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public void delete(Object obj)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            Class cls = obj.getClass();

            SQLHelper sql_helper = new SQLHelper();
            sql_helper.prepareDelete(obj);

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
            fieldDel.set(obj, true);

        } catch (Exception ex)
        {
            rollback();
            System.err.println("Persistor: delete error at: \n" + ex.getMessage());
        } finally
        {
            closePreparedStatement(preparedStatement);
        }
    }

    private void loadEntity(Object obj, ResultSet resultSet)
    {
        try
        {
            Class cls = obj.getClass();

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
                            Method invokeMethod = obj.getClass().getMethod(fieldName, boolean.class);
                            invokeMethod.invoke(obj, resultSet.getBoolean(name));
                            continue;
                        }

                        if (method.getReturnType() == int.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, int.class);
                            invokeMethod.invoke(obj, resultSet.getInt(name));
                            continue;
                        }

                        if (method.getReturnType() == double.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, double.class);
                            invokeMethod.invoke(obj, resultSet.getDouble(name));
                            continue;
                        }

                        if (method.getReturnType() == float.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, float.class);
                            invokeMethod.invoke(obj, resultSet.getFloat(name));
                            continue;
                        }

                        if (method.getReturnType() == short.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, short.class);
                            invokeMethod.invoke(obj, resultSet.getShort(name));
                            continue;
                        }

                        if (method.getReturnType() == long.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, long.class);
                            invokeMethod.invoke(obj, resultSet.getLong(name));
                            continue;
                        }

                        if (method.getReturnType() == String.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, String.class);
                            invokeMethod.invoke(obj, resultSet.getString(name));
                            continue;
                        }

                        if (method.getReturnType() == java.util.Date.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, java.util.Date.class);
                            invokeMethod.invoke(obj, resultSet.getDate(name));
                            continue;
                        }

                        if (method.getReturnType() == byte.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, byte.class);
                            invokeMethod.invoke(obj, resultSet.getByte(name));
                            continue;
                        }

                        if (method.getReturnType() == BigDecimal.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, BigDecimal.class);
                            invokeMethod.invoke(obj, resultSet.getBigDecimal(name));
                            continue;
                        }

                        if (method.getReturnType() == InputStream.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, InputStream.class);
                            invokeMethod.invoke(obj, (InputStream) resultSet.getBinaryStream(name));
                            continue;
                        }
                    }
                }
            }
        } catch (Exception ex)
        {

        }
    }

    private boolean hasJoinableObjects(Object obj)
    {
        try
        {
            Class cls = obj.getClass();

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

    private void executeJoin(Object obj, int id)
    {
        try
        {
            Class cls = obj.getClass();
            Join join = new Join(obj);
            List<JoinableObject> objectsToJoin = new ArrayList<>();

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    Class clss = Class.forName(method.getReturnType().getName());

                    java.lang.reflect.Constructor ctor = clss.getConstructor();

                    Object object = ctor.newInstance();

                    // object.getClass().newInstance();
                    OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);

                    if (oneToOne.load() == LOAD.MANUAL)
                    {
                        continue;
                    }

                    String sourceName = cls.getSimpleName() + "." + oneToOne.source();
                    String targetName = object.getClass().getSimpleName() + "." + oneToOne.target();

                    join.addJoin(oneToOne.join_type(), object, sourceName + " = " + targetName);

                    JoinableObject objToJoin = new JoinableObject();

                    objToJoin.result_type = RESULT_TYPE.UNIQUE;
                    objToJoin.objectToJoin = object;

                    objectsToJoin.add(objToJoin);
                }

                if (method.isAnnotationPresent(OneToMany.class))
                {
                    Class clss = Class.forName(method.getReturnType().getName());

                    java.lang.reflect.Constructor ctor = clss.getConstructor();

                    Object object = ctor.newInstance();

                    // object.getClass().newInstance();
                    OneToMany oneToMany = (OneToMany) method.getAnnotation(OneToMany.class);

                    if (oneToMany.load() == LOAD.MANUAL)
                    {
                        continue;
                    }

                    String sourceName = cls.getSimpleName() + "." + oneToMany.source();
                    String targetName = object.getClass().getSimpleName() + "." + oneToMany.target();

                    join.addJoin(oneToMany.join_type(), object, sourceName + " = " + targetName);

                    JoinableObject objToJoin = new JoinableObject();

                    objToJoin.result_type = RESULT_TYPE.MULTIPLE;
                    objToJoin.objectToJoin = object;

                    objectsToJoin.add(objToJoin);
                }
            }

            if (join.joinCount > 0)
            {
                SQLHelper helper = new SQLHelper();
                String pkName = helper.getPrimaryKeyFieldName(obj);
                join.addFinalCondition("WHERE " + cls.getSimpleName().toLowerCase() + "." + pkName + " = " + id);

                join.Execute(this);

                join.getResultObj(obj);
                for (JoinableObject object : objectsToJoin)
                {
                    if (object.result_type == RESULT_TYPE.UNIQUE)
                    {
                        join.getResultObj(object.objectToJoin);

                        Method method = obj.getClass().getMethod("set" + object.objectToJoin.getClass().getSimpleName(), object.objectToJoin.getClass());
                        method.invoke(obj, object.objectToJoin);
                    }

                    if (object.result_type == RESULT_TYPE.MULTIPLE)
                    {
                        Class clss = object.objectToJoin.getClass();

                        Field f = clss.getField("ResultList");
                        f.set(object.objectToJoin, join.getList(object.objectToJoin));

                        Method method = obj.getClass().getMethod("set" + object.objectToJoin.getClass().getSimpleName(), object.objectToJoin.getClass());
                        method.invoke(obj, object.objectToJoin);
                    }

                }

                System.out.println("Persistor: \n" + join.mountedQuery);
            }
        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error at:");
            ex.printStackTrace();
        }
    }

    @Override
    public void onID(Object obj, int id)
    {
        Statement statement = null;

        try
        {
            Class cls = obj.getClass();

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            if(hasJoinableObjects(obj))
            {
                executeJoin(obj, id);
                return;
            }
            
            SQLHelper helper = new SQLHelper();
            helper.prepareBasicSelect(obj, id);

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            String sqlBase = helper.getSqlBase();

            Field field = cls.getField("mountedQuery");
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
    }

    @Override
    public Object onID(Class cls, int id)
    {
        Statement statement = null;
        Object obj = null;
        try
        {
            java.lang.reflect.Constructor constructor = cls.getConstructor();
            obj = constructor.newInstance();

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return null;
            }

            String primaryKeyName = "";

            if(hasJoinableObjects(obj))
            {
                executeJoin(obj, id);
                return obj;
            }

            SQLHelper helper = new SQLHelper();
            primaryKeyName = helper.getPrimaryKeyFieldName(obj);
            helper.prepareBasicSelect(obj, id);
            
            String sqlBase = helper.getSqlBase();
            Field field = cls.getField("mountedQuery");
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

    public int maxId(Object obj, String where)
    {
        Statement statement = null;
        int result = 0;
        try
        {
            Class cls = obj.getClass();
            SQLHelper sql_helper = new SQLHelper();

            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(obj);

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

            String pkMethodName = sql_helper.getPrimaryKeyMethodName(obj).replace("get", "set");

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

    private void lastID(Object obj, String whereCondition)
    {
        Statement statement = null;
        try
        {
            Class cls = obj.getClass();
            SQLHelper sql_helper = new SQLHelper();

            String primaryKeyName = sql_helper.getPrimaryKeyFieldName(obj);

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

            String pkMethodName = sql_helper.getPrimaryKeyMethodName(obj).replace("get", "set");

            while (resultSet.next())
            {
                String field = (pkMethodName);
                Method method = obj.getClass().getMethod(field, int.class);
                method.invoke(obj, resultSet.getObject(primaryKeyName));
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
    public Criteria createCriteria(Object obj, RESULT_TYPE result_type)
    {
        Criteria criteria = null;

        try
        {
            criteria = new Criteria(this, obj, result_type);

        } catch (Exception ex)
        {
            System.err.println("Persistor: create criteria error at \n");
            ex.printStackTrace();
        }

        return criteria;
    }
}
