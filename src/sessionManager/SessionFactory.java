package sessionManager;

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

import annotations.OneToOne;
import annotations.PrimaryKey;
import annotations.Version;
import connection.DataSource;
import enums.LOAD;
import enums.ResultType;
import generalClasses.DBConfig;
import interfaces.ISession;

public class SessionFactory implements ISession
{
    public Connection connection;
    DBConfig config;

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

    private void clostSTMT(Statement statement)
    {
        try
        {
            statement.close();
        } catch (Exception ex)
        {
            System.err.println("Persistor: error at: \n" + ex.getMessage());
        }
    }

    private void closePS(PreparedStatement ps)
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
            Method method = obj.getClass().getMethod(methodName.replace("set", "get"));
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

                    session.save(object);
                    //  session.commit();

                    SQLHelper helper = new SQLHelper();
                    Method pkObject = object.getClass().getMethod(helper.getPrimaryKeyMethodName(object));

                    Method mtd = obj.getClass().getMethod(field, int.class);
                    mtd.invoke(obj, pkObject.invoke(object));
                }
            }

            SQLHelper sql_Helper = new SQLHelper();
            sql_Helper.prepareInsert(obj);
            String sqlBase = sql_Helper.getSqlBase();

            preparedStatement = connection.prepareStatement(sqlBase);

            int parameterIndex = 1;

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    continue;
                }
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    continue;
                }

                if (method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
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
                    //if (method.getReturnType() == byte.class){ preparedStatement.setByte(parameterIndex, (byte)method.invoke(obj)); parameterIndex ++; continue;}

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

            System.out.println("Persistor: \n " + sqlBase);
            preparedStatement.execute();

            Field fieldSaved = cls.getField("saved");
            fieldSaved.set(obj, true);

            lastID(obj);

        } catch (Exception ex)
        {
            System.err.println("Persistor: save error at: \n");
            ex.printStackTrace();
            rollback();
        } finally
        {
            closePS(preparedStatement);
        }
    }

    //UPDATE WITH AND CONDITIONS
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
                if (method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                {
                    if (method.isAnnotationPresent(PrimaryKey.class))
                    {
                        continue;
                    }

                    if (method.isAnnotationPresent(OneToOne.class))
                    {
                        continue;
                    }

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
            closePS(preparedStatement);
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
                    //  session.commit();

                    SQLHelper helper = new SQLHelper();
                    Method pkObject = object.getClass().getMethod(helper.getPrimaryKeyMethodName(object));

                    Method mtd = obj.getClass().getMethod(field, int.class);
                    mtd.invoke(obj, pkObject.invoke(object));
                }
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

            int parameterIndex = 1;

            for (Method method : cls.getMethods())
            {
                if (method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                {
                    if (method.isAnnotationPresent(PrimaryKey.class))
                    {
                        continue;
                    }
                    if (method.isAnnotationPresent(OneToOne.class))
                    {
                        continue;
                    }

                    if (method.isAnnotationPresent(Version.class))
                    {
                        int version = Integer.parseInt(method.invoke(obj).toString());
                        preparedStatement.setInt(parameterIndex, (version + 1));
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
            System.err.println("Persistor: update error at: \n" + ex.getMessage());
            rollback();
        } finally
        {
            closePS(preparedStatement);
        }
    }

    //DELETE WITH AND CONDITIONS
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
            closePS(preparedStatement);
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
            closePS(preparedStatement);
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

            String primaryKeyName = "";

            Join join = new Join(obj);

            List<Object> objectsToJoin = new ArrayList<>();

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    Object object = method.invoke(obj);

                    OneToOne oneToOne = (OneToOne) method.getAnnotation(OneToOne.class);

                    if (oneToOne.load() == LOAD.MANUAL)
                    {
                        continue;
                    }

                    String sourceName = cls.getSimpleName() + "." + oneToOne.source();
                    String targetName = object.getClass().getSimpleName() + "." + oneToOne.target();

                    join.addJoin(oneToOne.join_type(), object, sourceName + " = " + targetName);

                    objectsToJoin.add(object);
                }
            }

            if (join.joinCount > 0)
            {
                SQLHelper helper = new SQLHelper();
                String pkName = helper.getPrimaryKeyFieldName(obj);
                join.addFinalCondition("WHERE " + cls.getSimpleName() + "." + pkName + " = " + id);

                join.Execute(this);

                join.getResultObj(obj);

                for (Object object : objectsToJoin)
                {
                    join.getResultObj(object);

                    Method method = obj.getClass().getMethod("set" + object.getClass().getSimpleName(), object.getClass());
                    method.invoke(obj, object);
                }

                System.out.println("Persistor: \n" + join.mountedQuery);

                return;
            }

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    if (isNumber(method) && method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        primaryKeyName = method.getName().replace("get", "");
                        break;
                    }
                }
            }

            if (!extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            String sqlBase = ("select * from " + cls.getName().replace(cls.getPackage().getName() + ".", "") + " where " + primaryKeyName + " = " + id).toLowerCase();
            Field field = cls.getField("mountedQuery");
            field.set(obj, sqlBase);

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlBase);

            while (resultSet.next())
            {
                for (Method method : cls.getMethods())
                {
                    if (method.getName().contains("set") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        Method oneToOneMtd = cls.getMethod(method.getName().replace("set", "get"));

                        if (oneToOneMtd.isAnnotationPresent(OneToOne.class))
                        {
                            continue;
                        }

                        method.invoke(obj, resultSet.getObject(method.getName().replace("set", "")));
                    }
                }
            }

            System.out.println("Persistor: \n " + sqlBase);

        } catch (Exception ex)
        {
            System.err.println("Persistor: load on id error at: \n" + ex.getMessage());
        } finally
        {
            if (statement != null)
            {
                clostSTMT(statement);
            }
        }
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

    private void lastID(Object obj)
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
            className = className.replace(".", "");

            String sqlBase = "select max(" + primaryKeyName + ") " + primaryKeyName + " from " + className;

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlBase);

            while (resultSet.next())
            {
                String field = ("set" + primaryKeyName);
                Method method = obj.getClass().getMethod(field, int.class);
                method.invoke(obj, resultSet.getObject(primaryKeyName));
            }

        } catch (Exception ex)
        {
            System.err.println("Persistor: error at: \n" + ex.getMessage());
        } finally
        {
            this.clostSTMT(statement);
        }
    }

    @Override
    public Criteria createCriteria(Object obj, ResultType result_type)
    {
        Criteria criteria = null;

        try
        {
            criteria = new Criteria(connection, obj, result_type);

        } catch (Exception ex)
        {
            System.err.println("Persistor: create criteria error at \n");
            ex.printStackTrace();
        }

        return criteria;
    }
}
