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
            if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    continue;
                }
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    continue;
                }
                if (method.isAnnotationPresent(OneToMany.class))
                {
                    continue;
                }

                String name = "";

                if (method.getName().startsWith("is"))
                {
                    name = method.getName().substring(2, method.getName().length());
                } else
                {
                    name = method.getName().substring(3, method.getName().length());
                }

                fields += name.toLowerCase() + ",";
            }
        }

        return fields;
    }

    public String getPrimaryKeyMethodName(Object obj)
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

                    if (method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        primaryKeyName = method.getName();
                        this.setPrimaryKeyValue(method.invoke(obj).toString());
                    }

                    break;
                }
            }
        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error at: \n" + ex.getMessage());
        }

        return primaryKeyName;
    }

    public String getPrimaryKeyFieldName(Object obj)
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

                    if (method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        primaryKeyName = (method.getName().substring(3, method.getName().length())).toLowerCase();
                        this.setPrimaryKeyValue(method.invoke(obj).toString());
                    }

                    break;
                }
            }
        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error at: \n" + ex.getMessage());
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

    public void prepareSelect(Object obj, String whereCondition, int LIMIT)
    {
        try
        {
            Class cls = obj.getClass();

            String sqlBase = "select * from " + cls.getName().replace(cls.getPackage().getName() + ".", "") + " LIMIT " + LIMIT;

            sqlBase = sqlBase.toLowerCase();

            if (whereCondition != null && whereCondition != "")
            {
                sqlBase += " where " + whereCondition;
            }
            this.setSqlBase(sqlBase.toLowerCase());
            Field field = cls.getField("mountedQuery");
            field.set(obj, sqlBase);

        } catch (Exception ex)
        {
            System.err.println("Persistor: SQL_Helper_error: \n" + ex.getMessage());
        }
    }

    public void prepareDelete(Object obj)
    {
        try
        {
            Class cls = obj.getClass();

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    if (isNumber(method) && method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        primaryKeyName = method.getName().substring(3, method.getName().length());
                        primaryKeyValue = (method.invoke(obj)).toString();
                    }

                    continue;
                }
            }

            sqlBase = ("delete from " + cls.getSimpleName() + " where " + primaryKeyName + "=" + primaryKeyValue).toLowerCase();

            if (getAuxiliarPK_name(cls) != null)
            {
                String auxPK_name = getAuxiliarPK_name(cls);
                String columnAuxPK_name = auxPK_name.replace("get", "").toLowerCase();

                sqlBase += " AND " + columnAuxPK_name + " = " + getAuxiliarPK_value(obj, cls, auxPK_name);
            }

            Field field = cls.getField("mountedQuery");
            field.set(obj, sqlBase);

        } catch (Exception ex)
        {
            System.err.println("Persistor: SQL_Helper_error: \n" + ex.getMessage());
        }
    }

    public void prepareBasicSelect(Object obj, int id)
    {
        try
        {
            Class cls = obj.getClass();
            primaryKeyName = this.getPrimaryKeyFieldName(obj);

            sqlBase = ("select * from " + cls.getSimpleName() + " where " + primaryKeyName + " = " + id).toLowerCase();

            if (getAuxiliarPK_name(cls) != null)
            {
                String auxPK_name = getAuxiliarPK_name(cls);
                String columnAuxPK_name = auxPK_name.replace("get", "").toLowerCase();

                sqlBase += " AND " + columnAuxPK_name + " = " + getAuxiliarPK_value(obj, cls, auxPK_name);
            }

        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error at:");
            ex.printStackTrace();
        }
    }

    public int updateStatus = 1;

    public void prepareUpdate(Object obj, Connection connection)
    {
        try
        {
            Class cls = obj.getClass();

            String sql = "update " + cls.getName().replace(cls.getPackage().getName() + ".", "") + " set ";
            String parameters = "";

            String tableName = cls.getName().replace(cls.getPackage().getName() + ".", "");

            for (Method method : cls.getMethods())
            {
                if (method.isAnnotationPresent(OneToOne.class))
                {
                    continue;
                }
                if (method.isAnnotationPresent(OneToMany.class))
                {
                    continue;
                }

                if (method.isAnnotationPresent(PrimaryKey.class))
                {
                    if (method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        primaryKeyName = method.getName().substring(3, method.getName().length());
                        continue;
                    }
                }

                if (method.isAnnotationPresent(Version.class))
                {
                    if (isNumber(method) && method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
                        int versionObj = Integer.parseInt(method.invoke(obj).toString());

                        String field = ("get" + primaryKeyName);
                        Method mt = cls.getMethod(field);
                        String pkValue = mt.invoke(obj).toString();

                        int currentVersion = currentVersion(tableName, primaryKeyName, pkValue, method.getName().replace("get", ""), connection);

                        if (versionObj < currentVersion)
                        {
                            System.err.println("Persistor: error on update " + cls.getSimpleName() + ". @Version violation error.");
                            updateStatus = 0;
                            return;
                        }

                        String fieldName = method.getName().substring(3, method.getName().length());
                        sql += fieldName + " = ?, ";

                        continue;
                    }
                }

                if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                {
                    String fieldName;

                    if (method.getName().startsWith("is"))
                    {
                        fieldName = method.getName().substring(2, method.getName().length());
                    } else
                    {
                        fieldName = method.getName().substring(3, method.getName().length());
                    }

                    sql += fieldName + " = ?, ";
                }
            }

            if (sql.endsWith(", "))
            {
                sql = sql.substring(0, sql.length() - 2);
            }

            String pkFieldName = this.getPrimaryKeyFieldName(obj);

            sql += " where " + pkFieldName + " = " + this.getPrimaryKeyValue();

            if (getAuxiliarPK_name(cls) != null)
            {
                String auxPK_name = getAuxiliarPK_name(cls);
                String columnAuxPK_name = auxPK_name.replace("get", "").toLowerCase();

                sql += " AND " + columnAuxPK_name + " = " + getAuxiliarPK_value(obj, cls, auxPK_name);
            }

            this.setSqlBase(sql.toLowerCase());

            Field fieldMQ = cls.getField("mountedQuery");
            fieldMQ.set(obj, sqlBase);

        } catch (Exception ex)
        {
            System.out.println("Persistor: SQL_Helper_error: \n" + ex.getMessage());
        }
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

    private int currentVersion(String table, String primaryKeyFieldName, String primaryKeyValue, String versionFieldName, Connection connection)
    {
        try
        {
            String sql = "SELECT MAX(" + versionFieldName + ") FROM " + table + " WHERE " + primaryKeyFieldName.toLowerCase() + " = " + primaryKeyValue;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();

            return resultSet.getInt(1);

        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error on get currentVersion \n" + ex.getMessage());
        }

        return 0;
    }

    public String parameterNames = "";

    public void prepareInsert(Object obj)
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
                    if (method.getName().startsWith("is"))
                    {
                        sql += method.getName().substring(2, method.getName().length()) + ", ";
                    } else
                    {
                        sql += method.getName().substring(3, method.getName().length()) + ", ";
                    }

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

        } catch (Exception ex)
        {
            System.err.println("Persistor: SQL_Helper_exception: " + ex.getMessage());
        }
    }
}
