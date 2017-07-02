package br.com.persistor.sessionManager;

import br.com.persistor.annotations.OneToMany;
import br.com.persistor.annotations.OneToOne;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LIMIT_TYPE;
import br.com.persistor.generalClasses.EntityKey;
import br.com.persistor.generalClasses.FieldIndex;
import br.com.persistor.generalClasses.Limit;
import br.com.persistor.generalClasses.PersistenceLog;
import br.com.persistor.generalClasses.Util;
import br.com.persistor.interfaces.IJoin;
import br.com.persistor.interfaces.Session;
import java.lang.reflect.Field;

public class Join implements IJoin
{

    public String mountedQuery = "";
    public int joinCount = 0;
    public boolean hasAllLoaded = false;

    private boolean autoCloseAfterExecute = false;
    private boolean isCriteriaOwnerInstance = false;
    private boolean restartEntityInstance;
    private Object primaryObj;
    private List<Object> objects = new ArrayList<>();
    private List<Object> resultList = new ArrayList<>();
    private List<String> ignorableFields = new ArrayList<>();
    private Session mainSession = null;
    private Limit limit = null;

    public void setAutoCloseAfterExecute(boolean value)
    {
        autoCloseAfterExecute = value;
    }

    public boolean isRestartEntityInstance()
    {
        return restartEntityInstance;
    }

    public void setRestartEntityInstance(boolean restartEntityInstance)
    {
        this.restartEntityInstance = restartEntityInstance;
    }

    public Join(Object baseObject, boolean isCriteriaOwner)
    {
        objects.add(baseObject);
        primaryObj = baseObject;
        restartEntityInstance = true;
        isCriteriaOwnerInstance = isCriteriaOwner;
    }

    public void addIgnorableField(String fieldName)
    {
        ignorableFields.add(fieldName);
    }

    @Override
    public void addJoin(JOIN_TYPE join_type, Object obj, String condition)
    {
        Class cls = obj.getClass();

        String table = cls.getSimpleName().toLowerCase();

        String join = detectJoin(join_type);
        mountedQuery += ("\n" + " " + join + " " + table) + "\n";
        if (condition != null)
            mountedQuery += " on " + condition;
        objects.add(obj);

        joinCount++;
    }

    String final_condition = "";

    @Override
    public void addFinalCondition(String final_and_or_where_condition)
    {
        final_condition = " " + final_and_or_where_condition;
    }

    @Override
    public void addLimit(Limit limit)
    {
        this.limit = limit;
    }

    private boolean isIgnorableField(String fieldOrMethodName)
    {
        for (String field : ignorableFields)
        {
            if (fieldOrMethodName.contains(field))
                return true;
        }

        return false;
    }

    private String resolveLimit(String fieldsSelect)
    {
        String baseQ = "";
        switch (mainSession.getConfig().getDb_type())
        {
            case FirebirdSQL:

                if (limit.limit_type == LIMIT_TYPE.paginate)
                {
                    baseQ = "SELECT FIRST " + limit.getPageSize() + " SKIP " + limit.getPagePosition() + " " + fieldsSelect;
                    baseQ = baseQ.substring(0, baseQ.length() - 2);
                    baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();
                }

                if (limit.limit_type == LIMIT_TYPE.simple)
                {
                    baseQ = "SELECT FIRST \n " + fieldsSelect;
                    baseQ = baseQ.substring(0, baseQ.length() - 2);
                    baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();
                }

                mountedQuery = (baseQ + "\n").toLowerCase();
                mountedQuery += final_condition;

                break;

            case SQLServer:

                if (limit.limit_type == LIMIT_TYPE.paginate)
                {
                    baseQ = "SELECT \n " + fieldsSelect;
                    baseQ = baseQ.substring(0, baseQ.length() - 2);
                    baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();

                    mountedQuery = (baseQ + "\n").toLowerCase();
                    mountedQuery += final_condition;

                    mountedQuery += " ORDER BY " + limit.getFieldToOrder() + " OFFSET " + limit.getPagePosition()
                            + " ROWS FETCH NEXT " + limit.getPageSize() + " ROWS ONLY";
                }

                if (limit.limit_type == LIMIT_TYPE.simple)
                {
                    baseQ = "SELECT TOP \n " + fieldsSelect;
                    baseQ = baseQ.substring(0, baseQ.length() - 2);
                    baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();

                    mountedQuery = (baseQ + "\n").toLowerCase();
                    mountedQuery += final_condition;
                }

                break;

            case PostgreSQL:

                if (limit.limit_type == LIMIT_TYPE.paginate)
                {
                    baseQ = "SELECT \n " + fieldsSelect;
                    baseQ = baseQ.substring(0, baseQ.length() - 2);
                    baseQ += "\n FROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();

                    mountedQuery = (baseQ + "\n").toLowerCase();
                    mountedQuery += final_condition;
                    mountedQuery += " LIMIT " + limit.getPageSize() + " OFFSET " + limit.getPagePosition();
                }

                if (limit.limit_type == LIMIT_TYPE.simple)
                {
                    baseQ = "SELECT \n " + fieldsSelect;
                    baseQ = baseQ.substring(0, baseQ.length() - 2);
                    baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();

                    mountedQuery = (baseQ + "\n").toLowerCase();
                    mountedQuery += final_condition;
                    mountedQuery += " LIMIT " + limit.getPageSize();
                }

                break;

            case MySQL:

                if (limit.limit_type == LIMIT_TYPE.paginate)
                {
                    baseQ = "SELECT \n " + fieldsSelect;
                    baseQ = baseQ.substring(0, baseQ.length() - 2);
                    baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();

                    mountedQuery = (baseQ + "\n").toLowerCase();
                    mountedQuery += final_condition;
                    mountedQuery += " LIMIT " + limit.getPagePosition() + ", " + limit.getPageSize();
                }

                if (limit.limit_type == LIMIT_TYPE.simple)
                {
                    baseQ = "SELECT \n " + fieldsSelect;
                    baseQ = baseQ.substring(0, baseQ.length() - 2);
                    baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();

                    mountedQuery = (baseQ + "\n").toLowerCase();
                    mountedQuery += final_condition;
                    mountedQuery += " LIMIT " + limit.getPageSize();
                }

                break;
        }

        return baseQ;
    }

    private String finalizeQuery(String fieldsSelect)
    {
        String baseQ = "";
        baseQ = "SELECT \n " + fieldsSelect;
        baseQ = baseQ.substring(0, baseQ.length() - 2);
        baseQ += "\nFROM \n " + primaryObj.getClass().getSimpleName().toLowerCase() + "\n" + mountedQuery.trim();

        mountedQuery = (baseQ + "\n").toLowerCase();
        mountedQuery += final_condition;

        return baseQ;
    }

    private String resolveFieldsQuery() throws Exception
    {
        String fieldsSelect = "";
        int index = 1;
        FieldIndex field_index;

        for (Object obj : objects)
        {
            Class cls = obj.getClass();
            String tableName = cls.getSimpleName().toLowerCase();

            SQLHelper helper = new SQLHelper();
            String[] fields = helper.getFields(obj).split(",");

            List<EntityKey> keys = helper.getEntityKeys(obj);

            String primaryKeyName = "";
            for (EntityKey key : keys)
                primaryKeyName += tableName + "." + key.getKeyField().toLowerCase() + " " + key.getKeyField().toLowerCase() + "_" + tableName + ", ";

            primaryKeyName = primaryKeyName.substring(0, primaryKeyName.lastIndexOf(", "));
            fieldsSelect += primaryKeyName + ", \n";

            field_index = new FieldIndex();
            field_index.field = primaryKeyName;
            field_index.index = index;
            fields_index.add(field_index);

            index++;

            for (int i = 0; i < fields.length; i++)
            {
                //tableName.field field_tableName -->  field_tableName = query alias
                String fieldName = tableName + "." + fields[i] + " " + fields[i] + "_" + tableName + ", ";
                if (i < (fields.length - 1))
                    fieldName += "\n";
                if (isIgnorableField(fieldName))
                    continue;

                fieldsSelect += fieldName;

                field_index = new FieldIndex();
                field_index.field = fieldName;
                field_index.index = index;
                fields_index.add(field_index);

                index++;
            }
        }

        return fieldsSelect;
    }

    private void resolveResultPrimaryKeyCase(Method method, Object otherObj,
            ResultSet resultSet, String tableName) throws Exception
    {
        String mtdName = method.getName().substring(3, method.getName().length());
        String pkColumnName = (mtdName.toLowerCase() + "_" + tableName);

        if (method.getReturnType() == int.class)
        {
            Method invokeMethod = otherObj.getClass().getMethod(("set" + mtdName), int.class);
            invokeMethod.invoke(otherObj, resultSet.getInt(pkColumnName));
        }

        if (method.getReturnType() == long.class)
        {
            Method invokeMethod = otherObj.getClass().getMethod(("set" + mtdName), long.class);
            invokeMethod.invoke(otherObj, resultSet.getLong(pkColumnName));
        }

        if (method.getReturnType() == short.class)
        {
            Method invokeMethod = otherObj.getClass().getMethod(("set" + mtdName), short.class);
            invokeMethod.invoke(otherObj, resultSet.getShort(pkColumnName));
        }

        if (method.getReturnType() == String.class)
        {
            Method invokeMethod = otherObj.getClass().getMethod(("set" + mtdName), String.class);
            invokeMethod.invoke(otherObj, resultSet.getString(pkColumnName));
        }
    }

    private String[] getColumnAndMethodName(Method method, String tableName) throws Exception
    {
        String[] result = new String[2];
        if (method.getName().startsWith("is"))
        {
            result[0] = (method.getName().substring(2, method.getName().length())).toLowerCase() + "_" + tableName;
            result[1] = "set" + method.getName().substring(2, method.getName().length());
        }
        else
        {
            result[0] = (method.getName().substring(3, method.getName().length())).toLowerCase() + "_" + tableName;
            result[1] = "set" + method.getName().substring(3, method.getName().length());
        }

        return result;
    }

    List<FieldIndex> fields_index = new ArrayList<>();

    @Override
    public void execute(Session iSession)
    {
        Connection connection;
        Statement statement = null;
        ResultSet resultSet = null;

        boolean loaded = false;
        try
        {
            this.mainSession = iSession;
            String fieldsSelect = resolveFieldsQuery();

            String baseQ = "";
            if (this.limit != null)
                baseQ = resolveLimit(fieldsSelect);
            else
                baseQ = finalizeQuery(fieldsSelect);

            connection = iSession.getActiveConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(mountedQuery);
            EntityFiller entityFiller = new EntityFiller();

            while (resultSet.next())
            {
                loaded = true;
                for (Object baseEntity : objects)
                {
                    Object targetEntity = baseEntity;
                    Class cls = targetEntity.getClass();

                    if (isRestartEntityInstance())
                        targetEntity = cls.newInstance();

                    String tableName = cls.getSimpleName().toLowerCase();
                    Object ignoreObj = null;

                    for (Method method : cls.getMethods())
                    {
                        if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test"))
                        {
                            if (method.getReturnType().getName().equals("java.lang.Class"))
                                continue;
                            if (method.isAnnotationPresent(OneToMany.class) || method.isAnnotationPresent(OneToOne.class))
                                continue;

                            if (method.isAnnotationPresent(PrimaryKey.class))
                                resolveResultPrimaryKeyCase(method, targetEntity, resultSet, tableName);

                            String[] columnMethodName = getColumnAndMethodName(method, tableName);
                            String columnName = columnMethodName[0];
                            String methodSetName = columnMethodName[1];

                            try
                            {
                                //checking if column exists in resultset
                                resultSet.findColumn(columnName);
                            }
                            catch (Exception ex)
                            {
                                System.err.println("Persistor: field '" + columnName + "' not exists in Result Set. Verify entity mapping.");
                                continue;
                            }

                            if (isIgnorableField(columnName))
                                continue;

                            entityFiller.fillEntity(method, resultSet, methodSetName, columnName, baseEntity, targetEntity, iSession);
                        }
                    }
                    if (ignoreObj == null)
                        resultList.add(targetEntity);
                }
            }
            if (loaded)
            {
                System.out.println("Persistor: \n" + mountedQuery);
                if (isCriteriaOwnerInstance)
                    loadAllResults();
            }
        }
        catch (Exception ex)
        {
            iSession.getPersistenceLogger().newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "void execute(Session iSession)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            this.mountedQuery));
        }
        finally
        {
            Util.closeResultSet(resultSet);
            Util.closeStatement(statement);

            if (autoCloseAfterExecute)
                iSession.close();
        }
    }

    private void loadAllResults() throws Exception
    {
        List<Object> listBaseEntity = getList(primaryObj);
        for (Object objBaseEntity : listBaseEntity)
        {
            for (Object joinEntity : objects)
            {
                if (joinEntity.getClass().getName().equals(primaryObj.getClass().getName()))
                    continue;

                for (Method method : objBaseEntity.getClass().getDeclaredMethods())
                {
                    if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test"))
                    {
                        if (method.getReturnType().equals(joinEntity.getClass()))
                        {
                            Method setJoinObjMethod = null;
                            if (method.isAnnotationPresent(OneToOne.class))
                            {
                                String setMethodName = "set" + method.getName().substring(3, method.getName().length());
                                setJoinObjMethod = objBaseEntity.getClass().getMethod(setMethodName, joinEntity.getClass());
                                setJoinObjMethod.invoke(objBaseEntity, getEntity(joinEntity.getClass()));
                            }

                            if (method.isAnnotationPresent(OneToMany.class))
                            {
                                String setMethodName = "set" + method.getName().substring(3, method.getName().length());
                                setJoinObjMethod = objBaseEntity.getClass().getMethod(setMethodName, joinEntity.getClass());

                                Class clazz = method.getReturnType();
                                Object objClazz = clazz.newInstance();
                                Field field = objClazz.getClass().getField("ResultList");
                                field.set(objClazz, getList(joinEntity));
                                setJoinObjMethod.invoke(objBaseEntity, objClazz);
                            }
                        }
                    }
                }
            }
        }
    }

    public <T> T getEntity(Class entityClass)
    {
        Object objToRemove = null;
        T entity = null;
        try
        {
            entity = (T) entityClass.newInstance();
            for (Object obj : resultList)
            {
                if (obj.getClass() == entity.getClass())
                {
                    entity = (T) obj;
                    objToRemove = obj;
                    entity.getClass().getField("mountedQuery").set(entity, this.mountedQuery);
                    break;
                }
            }

            if (objToRemove != null)
            {
                resultList.remove(objToRemove);
                return entity;
            }
        }
        catch (Exception ex)
        {
            mainSession.getPersistenceLogger().newNofication(new PersistenceLog(this.getClass().getName(), "<T> T getEntity(Class entityClass)", Util.getDateTime(), Util.getFullStackTrace(ex), ""));
        }
        return null;
    }

    public Object loadEntity(Object entity)
    {
        Object objToRemove = null;
        try
        {
            for (Object obj : resultList)
            {
                if (obj.getClass().equals(entity.getClass()))
                {
                    entity = obj;
                    objToRemove = obj;
                    entity.getClass().getField("mountedQuery").set(entity, this.mountedQuery);
                    break;
                }
            }

            if (objToRemove != null)
            {
                resultList.remove(objToRemove);
            }
        }
        catch (Exception ex)
        {
            mainSession.getPersistenceLogger().newNofication(
                    new PersistenceLog(this.getClass().getName(),
                            "<T> T getEntity(Class entityClass)",
                            Util.getDateTime(),
                            Util.getFullStackTrace(ex),
                            ""));
        }
        return entity;
    }

    public <T> List<T> getList(Object object)
    {
        List<T> returnList = new ArrayList<>();

        for (Object obj : resultList)
        {
            if (obj.getClass() == object.getClass())
                returnList.add((T) obj);
        }

        return returnList;
    }

    private int getFieldIndexByNamer(String fieldName)
    {
        for (FieldIndex field_index : fields_index)
        {
            String value = field_index.field.replace(", ", "");
            if (value.startsWith(fieldName))
            {
                fields_index.remove(field_index);
                return field_index.index;
            }
        }

        return 0;
    }

    @Override
    public String detectJoin(JOIN_TYPE join_type)
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
    public <T> List<T> getResultList(Object object)
    {
        List<T> returnList = new ArrayList<>();

        for (Object obj : resultList)
        {
            if (obj.getClass() == object.getClass())
            {
                returnList.add((T) obj);
            }
        }

        return returnList;
    }
}
