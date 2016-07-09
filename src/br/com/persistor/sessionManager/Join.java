package br.com.persistor.sessionManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.generalClasses.FieldIndex;
import br.com.persistor.interfaces.IJoin;

public class Join implements IJoin
{
    public String mountedQuery = "";

    Object primaryObj;

    List<Object> objects = new ArrayList<>();
    List<Object> resultList = new ArrayList<>();

    public int joinCount = 0;

    public Join(Object baseObject)
    {
        objects.add(baseObject);
        primaryObj = baseObject;
    }

    @Override
    public void addJoin(JOIN_TYPE join_type, Object obj, String condition)
    {
        Class cls = obj.getClass();

        String table = cls.getSimpleName().toLowerCase();

        String join = decodeJoin(join_type);
        mountedQuery += (("\n" + join + table + " ON " + condition).trim()).toLowerCase() + "\n";
        objects.add(obj);

        joinCount++;
    }

    @Override
    public void addFinalCondition(String final_and_or_where_condition)
    {
        mountedQuery += final_and_or_where_condition;
    }

    List<FieldIndex> fields_index = new ArrayList<>();

    public void Execute(SessionFactory session)
    {

        String fieldsSelect = "";
        int index = 1;

        FieldIndex field_index;

        for (Object obj : objects)
        {
            Class cls = obj.getClass();

            SQLHelper helper = new SQLHelper();
            String[] fields = helper.getFields(obj).split(",");

            String primaryKeyName = cls.getSimpleName().toLowerCase() + "." + helper.getPrimaryKeyFieldName(obj) + " " + helper.getPrimaryKeyFieldName(obj) + "_" + cls.getSimpleName().toLowerCase();

            fieldsSelect += primaryKeyName + ", ";

            field_index = new FieldIndex();
            field_index.field = primaryKeyName;
            field_index.index = index;
            fields_index.add(field_index);

            index++;

            for (int i = 0; i < fields.length; i++)
            {
                String fieldName = cls.getSimpleName().toLowerCase() + "." + fields[i] + ", ";
                fieldsSelect += fieldName;

                field_index = new FieldIndex();
                field_index.field = fieldName;
                field_index.index = index;
                fields_index.add(field_index);

                index++;
            }
        }

        String baseQ = "SELECT \n " + fieldsSelect;
        baseQ = baseQ.substring(0, baseQ.length() - 2);
        baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName() + "\n" + mountedQuery.trim();

        mountedQuery = baseQ + "\n";

        Connection connection;
        Statement statement;
        ResultSet resultSet;

        try
        {
            connection = session.connection;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(mountedQuery);

            while (resultSet.next())
            {
                for (Object obj : objects)
                {
                    Object otherObj = obj;
                    Constructor ctor = obj.getClass().getConstructor();
                    otherObj = ctor.newInstance();
                    Class cls = otherObj.getClass();
                    SQLHelper helper = new SQLHelper();
                    String tableName = cls.getSimpleName().toLowerCase();

                    for (Method method : cls.getMethods())
                    {
                        if (method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                        {
                            if (method.isAnnotationPresent(PrimaryKey.class))
                            {
                                String pkName = helper.getPrimaryKeyFieldName(otherObj);
                                String mtdName = method.getName().replace("get", "");
                                int inx = getFieldIndexByNamer(tableName + "." + mtdName.toLowerCase() + " " + mtdName.toLowerCase() + "_" + tableName);

                                if (method.getReturnType() == int.class)
                                {
                                    Method invokeMethod = otherObj.getClass().getMethod(("set" + mtdName), int.class);
                                    invokeMethod.invoke(otherObj, resultSet.getInt(inx));
                                    continue;
                                }

                                if (method.getReturnType() == long.class)
                                {
                                    Method invokeMethod = otherObj.getClass().getMethod(("set" + mtdName), long.class);
                                    invokeMethod.invoke(otherObj, resultSet.getInt(inx));
                                    continue;
                                }

                                if (method.getReturnType() == short.class)
                                {
                                    Method invokeMethod = otherObj.getClass().getMethod(("set" + mtdName), short.class);
                                    invokeMethod.invoke(otherObj, resultSet.getInt(inx));
                                    continue;
                                }
                            }

                            String name = cls.getSimpleName() + "." + method.getName().replace("get", "");
                            int indexParam = getFieldIndexByNamer(name.toLowerCase());

                            String methodSetName = method.getName().replace("get", "set");

                            if (method.getReturnType() == boolean.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, boolean.class);
                                invokeMethod.invoke(otherObj, resultSet.getBoolean(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == int.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, int.class);
                                invokeMethod.invoke(otherObj, resultSet.getInt(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == double.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, double.class);
                                invokeMethod.invoke(otherObj, resultSet.getDouble(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == float.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, float.class);
                                invokeMethod.invoke(otherObj, resultSet.getFloat(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == short.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, short.class);
                                invokeMethod.invoke(otherObj, resultSet.getShort(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == long.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, long.class);
                                invokeMethod.invoke(otherObj, resultSet.getLong(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == String.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, String.class);
                                invokeMethod.invoke(otherObj, resultSet.getString(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == java.util.Date.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, java.util.Date.class);
                                invokeMethod.invoke(otherObj, resultSet.getDate(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == byte.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, byte.class);
                                invokeMethod.invoke(otherObj, resultSet.getByte(indexParam));
                                continue;
                            }

                            if (method.getReturnType() == BigDecimal.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(methodSetName, BigDecimal.class);
                                invokeMethod.invoke(otherObj, resultSet.getBigDecimal(indexParam));
                                continue;
                            }
                        }
                    }
                    resultList.add(otherObj);
                }
            }

        } catch (Exception ex)
        {
            System.err.println("Persistor: execute join error at: \n" + ex.getMessage());
        }
    }

    public void getResultObj(Object object)
    {
        Object objToRemove = null;
        try
        {
            for (Object obj : resultList)
            {
                if (obj.getClass() == object.getClass())
                {
                    for (Method method : obj.getClass().getMethods())
                    {
                        if (method.isAnnotationPresent(OneToOne.class))
                        {
                            continue;
                        }

                        if (method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                        {
                            String name = (method.getName().replace("get", "set"));
                            Method setInvokeMethod = obj.getClass().getMethod(name, method.getReturnType());
                            setInvokeMethod.invoke(object, method.invoke(obj));
                        }
                    }

                    objToRemove = obj;
                    break;
                }
            }

            int index = 0;
            boolean hasIndex = false;

            for (Object obj : resultList)
            {
                if (obj == objToRemove)
                {
                    hasIndex = true;
                    break;
                }
                index++;
            }

            if (hasIndex)
            {
                resultList.remove(index);
            }

        } catch (Exception ex)
        {
            System.err.println("Persistor: internal error join.getResultObj: \n" + ex.getMessage());
        }
    }

    public List<Object> getList(Object object)
    {
        List<Object> returnList = new ArrayList<>();

        for (Object obj : resultList)
        {
            if (obj.getClass() == object.getClass())
            {
                returnList.add(obj);
            }
        }

        return returnList;
    }

    private void closeRS(ResultSet resultSet)
    {
        try
        {
            resultSet.close();

        } catch (Exception ex)
        {
            System.err.println("Persistor: Join closeRS error at: \n" + ex.getMessage());
        }
    }

    private void closeSt(Statement statement)
    {
        try
        {
            statement.close();

        } catch (Exception ex)
        {
            System.err.println("Persistor: Join closeSt error at: \n" + ex.getMessage());
        }
    }

    private int getFieldIndexByNamer(String fieldName)
    {
        for (FieldIndex field_index : fields_index)
        {
            String value = field_index.field.replace(", ", "");
            if (value.equals(fieldName))
            {
                return field_index.index;
            }
        }

        return 0;
    }

    @Override
    public String decodeJoin(JOIN_TYPE join_type)
    {

        switch (join_type)
        {
            case INNER:
                return " INNER JOIN ";
            case LEFT:
                return " LEFT JOIN ";
            case RIGHT:
                return " RIGHT JOIN ";
            case LEFT_OUTER:
                return " LEFT OUTER JOIN ";
        }
        return null;
    }

    @Override
    public List<Object> getResultList(Object object)
    {
        List<Object> returnList = new ArrayList<>();
        
        for(Object obj : resultList)
        {
            if(obj.getClass() == object.getClass()) returnList.add(obj);
        }
        
        return resultList;
    }
}
