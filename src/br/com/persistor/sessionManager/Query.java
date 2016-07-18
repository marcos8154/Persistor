/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import br.com.persistor.annotations.NamedQuery;
import br.com.persistor.annotations.OneToMany;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.COMMIT_MODE;
import br.com.persistor.enums.LOAD;
import br.com.persistor.enums.PARAMETER_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.JoinableObject;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author marcosvinicius
 */
public class Query
{
    private PreparedStatement preparedStatement;
    private SessionFactory sessionFactory;
    String query;

    private RESULT_TYPE result_type;
    private COMMIT_MODE commit_mode = COMMIT_MODE.AUTO;
    
    public COMMIT_MODE getCommit_mode()
    {
        return commit_mode;
    }

    public void setCommit_mode(COMMIT_MODE commit_mode)
    {
        this.commit_mode = commit_mode;
    }
    
    public RESULT_TYPE getResult_type()
    {
        return result_type;
    }

    public void setResult_type(RESULT_TYPE result_type)
    {
        this.result_type = result_type;
    }

    private Class cls;
    Object obj;
    
    public void createQuery(SessionFactory session, Object obj, String query)
    {
        //if "query" starts with "@", is an NamedQuery.
        //Find in Class "cls" the NamedQuery
        this.cls = obj.getClass();
        this.obj = obj;
        this.sessionFactory = session;
        
        try
        {
            if (cls.isAnnotationPresent(NamedQuery.class))
            {
                for (Annotation annotation : cls.getAnnotations())
                {
                    NamedQuery namedQuery = (NamedQuery) annotation;
                    if (namedQuery.queryName().equals(query.replace("@", "")))
                    {
                        this.query = namedQuery.queryValue();
                        break;
                    }
                }
        
                this.query = query.toLowerCase();
            }

            this.preparedStatement = session.connection.prepareStatement(this.query);

        } catch (Exception ex)
        {
            System.err.println("Persistor: Create Query error at:");
            ex.printStackTrace();
        }
    }

    public void setParameter(int parameter_index, Object value)
    {
        try
        {
            if (value instanceof String)
            {
                preparedStatement.setString(parameter_index, String.valueOf(value));
            }
            if (value instanceof Integer)
            {
                preparedStatement.setInt(parameter_index, (int) value);
            }
            if (value instanceof Double)
            {
                preparedStatement.setDouble(parameter_index, (double) value);
            }
            if (value instanceof Float)
            {
                preparedStatement.setFloat(parameter_index, (float) value);
            }
            if (value instanceof BigDecimal)
            {
                preparedStatement.setBigDecimal(parameter_index, (BigDecimal) value);
            }
            if (value instanceof Boolean)
            {
                preparedStatement.setBoolean(parameter_index, (boolean) value);
            }
            if (value instanceof FileInputStream)
            {
                preparedStatement.setBinaryStream(parameter_index, (FileInputStream) value);
            }
            if (value instanceof Short)
            {
                preparedStatement.setShort(parameter_index, (short) value);
            }
            if (value instanceof Character)
            {
                preparedStatement.setString(parameter_index, String.valueOf(value));
            }
            if (value instanceof Long)
            {
                preparedStatement.setLong(parameter_index, (long) value);
            }

        } catch (Exception ex)
        {
            System.err.println("Persistor: Set Parameter error at: ");
            ex.printStackTrace();
        }
    }

    /**
     * If this method will go be invoked for execute INSERT / UPDATE or DELETE
     * more of once, is recommended use "setCommit_mode(COMMIT_MODE.MANUAL)"
     */
    public void execute()
    {
        try
        {
            if (this.query.contains("select"))
            {
                if (this.getResult_type() == null)
                {
                    throw new Exception("Persistor: error on pre-execute query at: RESULT_TYPE cannot be null !");
                }
                executeSelect(cls, this.getResult_type());
            }

            if (this.query.contains("update") || this.query.contains("insert"))
            {
                executeInsertOrUpdate(cls);
            }

        } catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    private void executeSelect(Class clss, RESULT_TYPE resultType)
    {
        ResultSet resultSet = null;
        try
        {
            this.query = query.toLowerCase();

            Field fieldMQ = clss.getField("mountedQuery");
            fieldMQ.set(obj, query);

            List<Object> rList = new ArrayList<>();
            Object ob = obj;
            Class cls = ob.getClass();

            if (!br.com.persistor.generalClasses.Util.extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            resultSet = preparedStatement.executeQuery();
            
            if (resultType == RESULT_TYPE.UNIQUE)
            {
                resultSet.next();

                for (Method method : cls.getMethods())
                {
                    if (method.getName().startsWith("is") || method.getName().startsWith("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                    {
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
                            invokeMethod.invoke(ob, resultSet.getBoolean(name));
                            continue;
                        }

                        if (method.getReturnType() == int.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, int.class);
                            invokeMethod.invoke(ob, resultSet.getInt(name));
                            continue;
                        }

                        if (method.getReturnType() == double.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, double.class);
                            invokeMethod.invoke(ob, resultSet.getDouble(name));
                            continue;
                        }

                        if (method.getReturnType() == float.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, float.class);
                            invokeMethod.invoke(ob, resultSet.getFloat(name));
                            continue;
                        }

                        if (method.getReturnType() == short.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, short.class);
                            invokeMethod.invoke(ob, resultSet.getShort(name));
                            continue;
                        }

                        if (method.getReturnType() == long.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, long.class);
                            invokeMethod.invoke(ob, resultSet.getLong(name));
                            continue;
                        }

                        if (method.getReturnType() == String.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, String.class);
                            invokeMethod.invoke(ob, resultSet.getString(name));
                            continue;
                        }

                        if (method.getReturnType() == java.util.Date.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, java.util.Date.class);
                            invokeMethod.invoke(ob, resultSet.getDate(name));
                            continue;
                        }

                        if (method.getReturnType() == byte.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, byte.class);
                            invokeMethod.invoke(ob, resultSet.getByte(name));
                            continue;
                        }

                        if (method.getReturnType() == BigDecimal.class)
                        {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, BigDecimal.class);
                            invokeMethod.invoke(ob, resultSet.getBigDecimal(name));
                            continue;
                        }
                    }
                }
            } else
            {
                while (resultSet.next())
                {
                    Constructor ctor = cls.getConstructor();
                    ob = ctor.newInstance();

                    for (Method method : cls.getMethods())
                    {
                        if (method.getName().contains("is") || method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class"))
                        {
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

                            if (method.getReturnType() == boolean.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, boolean.class);
                                invokeMethod.invoke(ob, resultSet.getBoolean(name));
                                continue;
                            }

                            if (method.getReturnType() == int.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, int.class);
                                invokeMethod.invoke(ob, resultSet.getInt(name));
                                continue;
                            }

                            if (method.getReturnType() == double.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, double.class);
                                invokeMethod.invoke(ob, resultSet.getDouble(name));
                                continue;
                            }

                            if (method.getReturnType() == float.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, float.class);
                                invokeMethod.invoke(ob, resultSet.getFloat(name));
                                continue;
                            }

                            if (method.getReturnType() == short.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, short.class);
                                invokeMethod.invoke(ob, resultSet.getShort(name));
                                continue;
                            }

                            if (method.getReturnType() == long.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, long.class);
                                invokeMethod.invoke(ob, resultSet.getLong(name));
                                continue;
                            }

                            if (method.getReturnType() == String.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, String.class);
                                invokeMethod.invoke(ob, resultSet.getString(name));
                                continue;
                            }

                            if (method.getReturnType() == java.util.Date.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, java.util.Date.class);
                                invokeMethod.invoke(ob, resultSet.getDate(name));
                                continue;
                            }

                            if (method.getReturnType() == byte.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, byte.class);
                                invokeMethod.invoke(ob, resultSet.getByte(name));
                                continue;
                            }

                            if (method.getReturnType() == BigDecimal.class)
                            {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, BigDecimal.class);
                                invokeMethod.invoke(ob, resultSet.getBigDecimal(name));
                                continue;
                            }
                        }
                    }
                    rList.add(ob);
                }
            }

            System.out.println("Persistor: \n " + query);

            Field f = clss.getField("ResultList");
            f.set(obj, rList);

        } catch (Exception ex)
        {
            System.err.println("Persistor: Execute query error at \n" + ex.getMessage());
        } finally
        {
            if(resultSet != null) sessionFactory.closeResultSet(resultSet);
            if(preparedStatement != null) sessionFactory.closePreparedStatement(preparedStatement);
        }
    }

    private void executeInsertOrUpdate(Class cls)
    {
        Statement statement = null;
        Object obj = null;
        try
        {
            java.lang.reflect.Constructor constructor = cls.getConstructor();
            obj = constructor.newInstance();

            if (!Util.extendsEntity(cls))
            {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
            }

            Field fieldMQ = cls.getField("mountedQuery");
            fieldMQ.set(obj, query);

            preparedStatement.execute();
            if(this.getCommit_mode() == COMMIT_MODE.AUTO) sessionFactory.commit();
            
            Field fieldSv = cls.getField("saved");
            fieldSv.set(obj, true);
            System.out.println("Persistor: \n " + query);
            

        } catch (Exception ex)
        {
            System.err.println("Persistor: execute query error at: \n" + ex.getMessage());
        } finally
        {
            if(preparedStatement != null)
            {
                sessionFactory.closePreparedStatement(preparedStatement);
            }
            
            if (statement != null)
            {
                sessionFactory.closeStatement(statement);
            }
        }
    }
}
