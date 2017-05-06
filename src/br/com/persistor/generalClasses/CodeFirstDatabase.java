/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.annotations.Column;
import br.com.persistor.annotations.OneToMany;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SQLHelper;
import br.com.persistor.sessionManager.SessionFactory;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Marcos Vinícius
 */
public class CodeFirstDatabase
{

    private DBConfig config = null;
    private List<CodeFirstTableDomain> tables;
    private boolean enabledDatabaseVerification = false;
    private Session session = null;
    private SessionFactory sf = null;
    private String sqlToRunAfterCreation = null;
    
    public void setSqlToRun(String sql)
    {
        this.sqlToRunAfterCreation = sql;
    }
    
    private void buildConnection() throws Exception
    {
        if (sf == null)
        {
            sf = new SessionFactory();
            session = sf.getSession(config);
            return;
        }

        sf.reset();
        session = sf.getSession(config);
    }

    public CodeFirstDatabase(boolean enableDatabaseVerification)
    {
        this.enabledDatabaseVerification = enableDatabaseVerification;
        this.tables = new ArrayList<>();
    }

    public CodeFirstDatabase addTableDomain(CodeFirstTableDomain tableDomain)
    {
        tables.add(tableDomain);
        return this;
    }

    private void checkDatabase()
    {
        String originalDatabase = this.config.getDatabase();
        try
        {
            switch (config.getDb_type())
            {
                case MySQL:
                    config.setDatabase("");
                    buildConnection();
                    runSql("create database if not exists " + originalDatabase);
                    session.commit();
                    session.close();
                    session = null;
                    break;
            }
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }

        config.setDatabase(originalDatabase);
    }

    public void createTables(DBConfig config) throws Exception
    {
        this.config = config;
        String currentTable = null;
        String currentScript = null;
        try
        {
            if (this.enabledDatabaseVerification)
                checkDatabase();
            buildConnection();
            for (CodeFirstTableDomain domain : tables)
            {
                Class cls = domain.getDomainClass();
                String tableName = cls.getSimpleName().toLowerCase();

                String sqlScript = "create table if not exists " + tableName + " \n"
                        + "(\n";

                for (Method method : cls.getMethods())
                {
                    if (!isGetMethod(method))
                        continue;

                    String columnName = convertMethodToColumnName(method);
                    ColumnProperties properties = domain.getPropertiesForColumn(columnName);

                    sqlScript += "\n" + columnName + getColumnProperties(config, domain.getDomainClass(), properties, method.getReturnType()) + ",";
                }

                SQLHelper helper = new SQLHelper();
                ColumnKey key = helper.getKey(cls);
                List<Relashionship> foreigKeys = helper.ListForeignKeys(cls);

                if (key != null)
                    sqlScript += "primary key(" + key.getColumnName() + "),";

                for (Relashionship relashionship : foreigKeys)
                    if (relashionship.getOneToOne().join_type() != JOIN_TYPE.LEFT)
                        sqlScript += "\n foreign key(" + relashionship.getOneToOne().source() + ") references " + relashionship.getTargetTable() + "(" + relashionship.getOneToOne().target() + "),";

                if (sqlScript.endsWith(","))
                    sqlScript = sqlScript.substring(0, sqlScript.length() - 1);

                sqlScript += "\n);";

                currentTable = tableName;
                currentScript = sqlScript;
                runSql(sqlScript);
            }

            if(sqlToRunAfterCreation != null)
                runSql(sqlToRunAfterCreation);
            
            session.commit();
            session.close();
        }
        catch (Exception ex)
        {
            String message = "Exception on create table '" + currentTable + "'\n\n"
                    + "Generation script: " + currentScript + "\n\n"
                    + "Exception details: " + ex.getMessage();
            throw new Exception(message);
        }
    }

    private void runSql(String sql) throws Exception
    {
        PreparedStatement ps = session.getActiveConnection().prepareStatement(sql);
        ps.execute();
    }

    private String getColumnProperties(DBConfig config, Class entityClass, ColumnProperties properties, Class classType)
    {
        SQLHelper helper = new SQLHelper();
        ColumnKey key = helper.getKey(entityClass);

        String result = null;

        switch (config.getDb_type())
        {
            case MySQL:
                if (classType == int.class)
                {
                    result = " int ";
                    if (properties != null)
                    {
                        if(!properties.isNullable())
                            result += " not null";
                        if(properties.getDefaultValue() != null)
                            result += " default " + properties.getDefaultValue();
                        
                        if (key != null)
                            if (key.getColumnName().equals(properties.getColumnName()))
                                result += ((key.getKey().increment() == INCREMENT.AUTO ? " auto_increment" : ""));
                    }
                }

                if (classType == double.class)
                {
                    result = " double";
                    if (properties != null)
                        result += "(" + properties.getLength() + ", " + properties.getDecimalDigits() + ")" + (properties.isNullable() ? "" : " not null ") + (properties.getDefaultValue() == null ? "" : "default " + properties.getDefaultValue());
                }

                if (classType == String.class)
                {
                    result = " varchar";
                    if (properties != null)
                        result += "(" + properties.getLength() + ")" + (properties.isNullable() ? "" : " not null " + (properties.getDefaultValue() == null ? "" : " default " + properties.getDefaultValue()));
                    else
                        result += "(250)";
                }

                if (classType == short.class)
                {
                    result = " smallint ";
                    if (properties != null)
                        result += (properties.isNullable() ? "" : " not null ") + (properties.getDefaultValue() == null ? "" : " default " + properties.getDefaultValue());
                }
                if (classType == InputStream.class)
                {
                    result = " mediumblob";
                }
                if (classType == BigDecimal.class)
                {
                    result = " decimal";
                    if (properties != null)
                        result += "(" + properties.getLength() + ", " + properties.getDecimalDigits() + ")" + (properties.isNullable() ? "" : " not null ") + (properties.getDefaultValue() == null ? "" : " default " + properties.getDefaultValue());
                }
                if (classType == boolean.class)
                {
                    result = " boolean";
                    if (properties != null)
                        result += (properties.isNullable() ? "" : " not null " + properties.getDefaultValue() == null ? "" : " default " + properties.getDefaultValue());
                }
                if (classType == char.class)
                {
                    result = " char";
                    if (properties != null)
                        result += (properties.isNullable() ? "" : " not null " + properties.getDefaultValue() == null ? "" : " default " + properties.getDefaultValue());
                }
                if (classType == Date.class || classType == Calendar.class)
                {
                    result = " date";
                    if (properties != null)
                        result += (properties.isNullable() ? "" : " not null ");
                }
                if (classType == long.class)
                {
                    result = " bigint";
                    if (properties != null)
                        result += (properties.isNullable() ? "" : " not null ");
                }
                break;
        }

        return result;
    }

    private boolean isGetMethod(Method method)
    {
        if (method.isAnnotationPresent(OneToMany.class) || method.isAnnotationPresent(OneToOne.class))
            return false;

        if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test"))
        {
            if (!method.getReturnType().getName().equals("java.lang.Class"))
            {
                return true;
            }
        }

        return false;
    }

    private String convertMethodToColumnName(Method method)
    {
        String columnName = null;
        if (method.getName().startsWith("is"))
        {
            columnName = method.isAnnotationPresent(Column.class)
                    ? ((Column) method.getAnnotation(Column.class)).name()
                    : (method.getName().substring(2, method.getName().length())).toLowerCase();
        }
        else
        {
            columnName = method.isAnnotationPresent(Column.class)
                    ? ((Column) method.getAnnotation(Column.class)).name()
                    : (method.getName().substring(3, method.getName().length())).toLowerCase();
        }

        return columnName;
    }
}
