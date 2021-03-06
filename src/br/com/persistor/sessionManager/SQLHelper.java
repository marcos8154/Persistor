package br.com.persistor.sessionManager;

import br.com.persistor.annotations.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.annotations.Version;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.PRIMARYKEY_TYPE;
import br.com.persistor.generalClasses.ColumnKey;
import br.com.persistor.generalClasses.EntityKey;
import br.com.persistor.generalClasses.Relashionship;
import java.util.ArrayList;
import java.util.List;

public class SQLHelper
{

    private String sqlBase = "";
    private String columns = "";
    private String values = "";
    private String primaryKeyName = "";
    private String primaryKeyValue = "";

    public String getSqlBase()
    {
        return sqlBase;
    }

    public String getPrimaryKeyName()
    {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName)
    {
        this.primaryKeyName = primaryKeyName;
    }

    public String getPrimaryKeyValue()
    {
        return primaryKeyValue;
    }

    public void setPrimaryKeyValue(String primaryKeyValue)
    {
        this.primaryKeyValue = primaryKeyValue;
    }

    public void setSqlBase(String sqlBase)
    {
        this.sqlBase = sqlBase;
    }

    public String getColumns()
    {
        return columns;
    }

    public void setColumns(String columns)
    {
        this.columns = columns;
    }

    public String getValues()
    {
        return values;
    }

    public void setValues(String values)
    {
        this.values = values;
    }

    public String getFields(Object obj)
    {
        Class cls = obj.getClass();

        String fields = "";

        for (Method method : cls.getMethods())
        {
            if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test"))
            {
                if (method.getReturnType().getName().equals("java.lang.Class"))
                    continue;

                if (method.isAnnotationPresent(OneToOne.class))
                    continue;
                if (method.isAnnotationPresent(PrimaryKey.class))
                    continue;
                if (method.isAnnotationPresent(OneToMany.class))
                    continue;

                String name = "";

                if (method.getName().startsWith("is"))
                {
                    name = method.getName().substring(2, method.getName().length());
                }
                else
                {
                    name = method.getName().substring(3, method.getName().length());
                }

                fields += name.toLowerCase() + ",";
            }
        }

        return fields;
    }

    public String getPrimaryKeyMethodName(Object obj) throws Exception
    {
        Class cls = obj.getClass();
        String primaryKeyName = "";

        try
        {
            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    PrimaryKey primaryKey = (PrimaryKey) method.getAnnotation(PrimaryKey.class);
                    if (primaryKey.primarykey_type() == PRIMARYKEY_TYPE.AUXILIAR)
                    {
                        continue;
                    }

                    if (method.getName().startsWith("get") && !method.getName().contains("class Test"))
                    {
                        if (method.getReturnType().getName().equals("java.lang.Class"))
                            continue;

                        primaryKeyName = method.getName();
                        this.setPrimaryKeyValue(method.invoke(obj).toString());
                    }

                    break;
                }
            }
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: internal error at: \n");
            throw new Exception(ex.getMessage());
        }

        return primaryKeyName;
    }

    public String getPrimaryKeyFieldName(Object obj) throws Exception
    {
        Class cls = obj.getClass();
        String primaryKeyName = "";

        try
        {
            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    PrimaryKey primaryKey = (PrimaryKey) method.getAnnotation(PrimaryKey.class);
                    if (primaryKey.primarykey_type() == PRIMARYKEY_TYPE.AUXILIAR)
                    {
                        continue;
                    }

                    if (method.getName().startsWith("get") && !method.getName().contains("class Test"))
                    {
                        if (method.getReturnType().getName().equals("java.lang.Class"))
                            continue;

                        primaryKeyName = (method.getName().substring(3, method.getName().length())).toLowerCase();
                        this.setPrimaryKeyValue(method.invoke(obj) == null ? "" : method.invoke(obj).toString());
                    }

                    break;
                }
            }
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: internal error at: \n");
            throw new Exception(ex.getMessage());
        }

        return primaryKeyName;
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

    public void prepareSelect(Object obj, String whereCondition, int LIMIT) throws Exception
    {
        try
        {
            Class cls = obj.getClass();

            String sqlBase = "SELECT * FROM " + cls.getName().replace(cls.getPackage().getName() + ".", "") + " LIMIT " + LIMIT;

            sqlBase = sqlBase.toLowerCase();

            if (whereCondition != null && whereCondition != "")
            {
                sqlBase += " where " + whereCondition;
            }
            this.setSqlBase(sqlBase.toLowerCase());
            Field field = cls.getField("mountedQuery");
            field.set(obj, sqlBase);

        }
        catch (Exception ex)
        {
            System.err.println("Persistor: SQL_Helper_error: \n");
            throw new Exception(ex.getMessage());
        }
    }

    public void prepareDelete(Object entity) throws Exception
    {
        try
        {
            Class cls = entity.getClass();
            String table = cls.getSimpleName().toLowerCase();

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    if (isNumber(method) && method.getName().contains("get") && !method.getName().contains("class Test"))
                    {
                        if (method.getReturnType().getName().equals("java.lang.Class"))
                            continue;

                        primaryKeyName = method.getName().substring(3, method.getName().length());
                        primaryKeyValue = (method.invoke(entity)).toString();
                    }

                    continue;
                }
            }

            sqlBase = "delete from " + cls.getSimpleName();

            List<EntityKey> keys = getEntityKeys(entity);
            String where = "";

            for (EntityKey key : keys)
                where += table + "." + key.getKeyField() + " = " + key.getKeyValue() + " and ";
            sqlBase += " where " + where.substring(0, where.lastIndexOf("and "));

            Field field = cls.getField("mountedQuery");
            field.set(entity, sqlBase);
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: SQL_Helper_error: \n");
            throw new Exception(ex.getMessage());
        }
    }

    public void prepareBasicSelect(Object entity, int id) throws Exception
    {
        try
        {
            Class cls = entity.getClass();
            String table = cls.getSimpleName().toLowerCase();
            primaryKeyName = this.getPrimaryKeyFieldName(entity);

            sqlBase = ("SELECT * FROM " + table + " WHERE " + primaryKeyName + " = " + id).toLowerCase();
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: internal error at:");
            throw new Exception(ex.getMessage());
        }
    }

    public void prepareMultiKeySelect(Object entity, int id) throws Exception
    {
        try
        {
            Class cls = entity.getClass();
            String table = cls.getSimpleName().toLowerCase();
            primaryKeyName = this.getPrimaryKeyFieldName(entity);

            sqlBase = "SELECT * FROM " + table;// " WHERE " + primaryKeyName + " = " + id).toLowerCase();

            List<EntityKey> keys = getEntityKeys(entity);
            String where = "";

            for (EntityKey key : keys)
                where += table + "." + key.getKeyField() + " = " + key.getKeyValue() + " and ";
            sqlBase += " where " + where.substring(0, where.lastIndexOf("and "));
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: internal error at:");
            throw new Exception(ex.getMessage());
        }
    }

    public int updateStatus = 1;

    public void prepareUpdate(Object entity, Connection connection) throws Exception
    {
        try
        {
            Class cls = entity.getClass();

            String sql = "update " + cls.getName().replace(cls.getPackage().getName() + ".", "") + " set ";
            String parameters = "";

            String table = cls.getName().replace(cls.getPackage().getName() + ".", "");

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                    continue;
                if (method.isAnnotationPresent(OneToMany.class))
                    continue;

                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    if (method.getName().startsWith("get") && !method.getName().contains("class Test"))
                    {
                        if (method.getReturnType().getName().equals("java.lang.Class"))
                            continue;

                        primaryKeyName = method.getName().substring(3, method.getName().length());
                        continue;
                    }
                }

                if (method.isAnnotationPresent(Version.class))
                {
                    if (isNumber(method) && method.getName().startsWith("get") && !method.getName().contains("class Test"))
                    {
                        if (method.getReturnType().getName().equals("java.lang.Class"))
                            continue;

                        int versionObj = Integer.parseInt(method.invoke(entity).toString());

                        String field = ("get" + primaryKeyName);
                        Method mt = cls.getMethod(field);
                        String pkValue = mt.invoke(entity).toString();

                        int currentVersion = currentVersion(table, primaryKeyName, pkValue, method.getName().replace("get", ""), connection);

                        if (versionObj < currentVersion)
                            throw new Exception("Persistor: unable to update entity '" + cls.getName() + "'. @Version violation error.");

                        String fieldName = method.getName().substring(3, method.getName().length());
                        sql += fieldName + " = ?, ";

                        continue;
                    }
                }

                if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test"))
                {
                    if (method.getReturnType().getName().equals("java.lang.Class"))
                        continue;

                    String fieldName;

                    if (method.getName().startsWith("is"))
                        fieldName = method.getName().substring(2, method.getName().length());
                    else
                        fieldName = method.getName().substring(3, method.getName().length());

                    sql += fieldName + " = ?, ";
                }
            }

            if (sql.endsWith(", "))
                sql = sql.substring(0, sql.length() - 2);

            List<EntityKey> keys = getEntityKeys(entity);
            String where = "";

            for (EntityKey key : keys)
                where += table + "." + key.getKeyField() + " = " + key.getKeyValue() + " and ";
            sql += " where " + where.substring(0, where.lastIndexOf("and "));

            this.setSqlBase(sql.toLowerCase());

            Field fieldMQ = cls.getField("mountedQuery");
            fieldMQ.set(entity, sqlBase);

        }
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }
    }

    public Object getAuxiliarPK_value(Object obj, Class cls, String name) throws Exception
    {
        try
        {
            return cls.getMethod(name).invoke(obj);
        }
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }
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

    private int currentVersion(String table, String primaryKeyFieldName, String primaryKeyValue, String versionFieldName, Connection connection) throws Exception
    {
        try
        {
            String sql = "SELECT MAX(" + versionFieldName + ") FROM " + table + " WHERE " + primaryKeyFieldName.toLowerCase() + " = " + primaryKeyValue;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();

            return resultSet.getInt(1);

        }
        catch (Exception ex)
        {
            System.err.println("Persistor: internal error on get currentVersion \n");
            throw new Exception(ex.getMessage());
        }
    }

    public String parameterNames = "";

    public void prepareInsert(Object obj) throws Exception
    {
        try
        {
            Class cls = obj.getClass();

            String sql = "insert into " + cls.getName().replace(cls.getPackage().getName() + ".", "") + " (";
            String parameters = "";
            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    PrimaryKey primaryKey = (PrimaryKey) method.getAnnotation(PrimaryKey.class);

                    if (primaryKey.increment() == INCREMENT.MANUAL)
                    {
                        sql += method.getName().substring(3, method.getName().length()) + ", ";
                        parameters += "?, ";
                        continue;
                    }
                    else
                        continue;
                }

                if (method.isAnnotationPresent(OneToOne.class))
                    continue;
                if (method.isAnnotationPresent(OneToMany.class))
                    continue;

                if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test"))
                {
                    if (method.getReturnType().getName().equals("java.lang.Class"))
                        continue;

                    if (method.getName().startsWith("is"))
                        sql += method.getName().substring(2, method.getName().length()) + ", ";
                    else
                        sql += method.getName().substring(3, method.getName().length()) + ", ";

                    parameters += "?, ";
                }
            }

            if (sql.endsWith(", "))
            {
                sql = sql.substring(0, sql.length() - 2);
            }
            sql += ") ";

            if (parameters.endsWith(", "))
            {
                parameters = parameters.substring(0, parameters.length() - 2);
            }
            sql += "values (" + parameters + ")";

            this.setSqlBase(sql.toLowerCase());

            Field fieldMQ = cls.getField("mountedQuery");
            fieldMQ.set(obj, sqlBase);

        }
        catch (Exception ex)
        {
            System.err.println("Persistor: SQL_Helper_exception at :\n ");
            throw new Exception(ex.getMessage());
        }
    }

    public List<Relashionship> ListForeignKeys(Class clazz)
    {
        List<Relashionship> result = new ArrayList<>();
        try
        {
            for (Method method : clazz.getMethods())
                if (method.isAnnotationPresent(OneToOne.class))
                    result.add(new Relashionship(method.getName().replace("get", "").toLowerCase(), (OneToOne) method.getAnnotation(OneToOne.class)));
        }
        catch (Exception ex)
        {
        }
        return result;
    }

    public List<ColumnKey> getKeys(Class clazz)
    {
        List<ColumnKey> result = new ArrayList<>();
        try
        {
            for (Method method : clazz.getMethods())
                if (method.isAnnotationPresent(PrimaryKey.class))
                    result.add(new ColumnKey(method.getName().replace("get", "").toLowerCase(), (PrimaryKey) method.getAnnotation(PrimaryKey.class)));
        }
        catch (Exception ex)
        {
        }
        return result;
    }

    public ColumnKey getKey(Class clazz)
    {
        List<ColumnKey> result = new ArrayList<>();
        try
        {
            for (Method method : clazz.getMethods())
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    PrimaryKey pk = (PrimaryKey) method.getAnnotation(PrimaryKey.class);
                    if (pk.primarykey_type() == PRIMARYKEY_TYPE.HEAD)
                        return new ColumnKey(method.getName().replace("get", "").toLowerCase(), (PrimaryKey) method.getAnnotation(PrimaryKey.class));
                }
        }
        catch (Exception ex)
        {
        }
        return null;
    }

    List<EntityKey> getEntityKeys(Object entity) throws Exception
    {
        List<EntityKey> result = new ArrayList<>();
        for (Method method : entity.getClass().getMethods())
        {
            if (method.isAnnotationPresent(PrimaryKey.class))
            {
                String name = method.getName().substring(3, method.getName().length());
                int value = Integer.parseInt(method.invoke(entity).toString());

                result.add(EntityKey.set(name, value));
            }

        }
        return result;
    }

}
