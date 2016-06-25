package sessionManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.log.Log;

import annotations.OneToOne;
import enums.ResultType;
import generalClasses.Expressions;
import generalClasses.Util;
import interfaces.ICriteria;

public class Criteria implements ICriteria {

    ResultType resultType;
    Connection connection;
    Object obj;

    String query = "";

    public Criteria(Connection conn, Object obj, ResultType result_type) {
        this.connection = conn;
        this.resultType = result_type;
        this.obj = obj;

        String name = (obj.getClass().getName().toLowerCase()).replace(obj.getClass().getPackage().getName() + ".", "");

        query = "SELECT * FROM " + name;
    }

    @Override
    public void add(Expressions expression) {
        query += expression.getCurrentValue();
    }

    private void CloseRS(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                if (!resultSet.isClosed()) {
                    resultSet.close();
                }
            }

        } catch (Exception ex) {
            System.err.println("Persistor: internal error at: \n" + ex.getMessage());
        }
    }

    private void CloseStatement(Statement statement) {
        try {
            if (statement != null) {
                if (!statement.isClosed()) {
                    statement.close();
                }
            }
        } catch (Exception ex) {
            System.err.println("Persistor: internal error at: \n" + ex.getMessage());
        }
    }

    public void Ha() {
        System.out.println("Haou");
    }

    @Override
    public void execute(SessionFactory sessionFactory) {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            Class clss = obj.getClass();

            Field fieldMQ = clss.getField("mountedQuery");
            fieldMQ.set(obj, query);

            List<Object> rList = new ArrayList<Object>();
            Object ob = obj;
            Class cls = ob.getClass();

            if (!Util.extendsEntity(cls)) {
                System.err.println("Persistor warning: the class '" + cls.getName() + "' not extends Entity. Operation is stoped.");
                return;
            }

            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            if (resultType == ResultType.UNIQUE) {
                resultSet.next();

                for (Method method : cls.getMethods()) {
                    if (method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class")) {
                        String name = (method.getName().replace("get", "")).toLowerCase();
                        String fieldName = method.getName().replace("get", "set");

                        if (method.isAnnotationPresent(OneToOne.class)) {
                            continue;
                        }

                        if (method.getReturnType() == boolean.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, boolean.class);
                            invokeMethod.invoke(ob, resultSet.getBoolean(name));
                            continue;
                        }

                        if (method.getReturnType() == int.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, int.class);
                            invokeMethod.invoke(ob, resultSet.getInt(name));
                            continue;
                        }

                        if (method.getReturnType() == double.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, double.class);
                            invokeMethod.invoke(ob, resultSet.getDouble(name));
                            continue;
                        }

                        if (method.getReturnType() == float.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, float.class);
                            invokeMethod.invoke(ob, resultSet.getFloat(name));
                            continue;
                        }

                        if (method.getReturnType() == short.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, short.class);
                            invokeMethod.invoke(ob, resultSet.getShort(name));
                            continue;
                        }

                        if (method.getReturnType() == long.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, long.class);
                            invokeMethod.invoke(ob, resultSet.getLong(name));
                            continue;
                        }

                        if (method.getReturnType() == String.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, String.class);
                            invokeMethod.invoke(ob, resultSet.getString(name));
                            continue;
                        }

                        if (method.getReturnType() == java.util.Date.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, java.util.Date.class);
                            invokeMethod.invoke(ob, resultSet.getDate(name));
                            continue;
                        }

                        if (method.getReturnType() == byte.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, byte.class);
                            invokeMethod.invoke(ob, resultSet.getByte(name));
                            continue;
                        }

                        if (method.getReturnType() == BigDecimal.class) {
                            Method invokeMethod = obj.getClass().getMethod(fieldName, BigDecimal.class);
                            invokeMethod.invoke(ob, resultSet.getBigDecimal(name));
                            continue;
                        }
                    }
                }
            } else {
                while (resultSet.next()) {
                    Constructor ctor = cls.getConstructor();
                    ob = ctor.newInstance();

                    for (Method method : cls.getMethods()) {
                        if (method.getName().contains("get") && !method.getName().contains("class Test") && !method.getName().contains("Class")) {
                            String name = (method.getName().replace("get", "")).toLowerCase();
                            String fieldName = method.getName().replace("get", "set");

                            if (method.getReturnType() == boolean.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, boolean.class);
                                invokeMethod.invoke(ob, resultSet.getBoolean(name));
                                continue;
                            }

                            if (method.getReturnType() == int.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, int.class);
                                invokeMethod.invoke(ob, resultSet.getInt(name));
                                continue;
                            }

                            if (method.getReturnType() == double.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, double.class);
                                invokeMethod.invoke(ob, resultSet.getDouble(name));
                                continue;
                            }

                            if (method.getReturnType() == float.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, float.class);
                                invokeMethod.invoke(ob, resultSet.getFloat(name));
                                continue;
                            }

                            if (method.getReturnType() == short.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, short.class);
                                invokeMethod.invoke(ob, resultSet.getShort(name));
                                continue;
                            }

                            if (method.getReturnType() == long.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, long.class);
                                invokeMethod.invoke(ob, resultSet.getLong(name));
                                continue;
                            }

                            if (method.getReturnType() == String.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, String.class);
                                invokeMethod.invoke(ob, resultSet.getString(name));
                                continue;
                            }

                            if (method.getReturnType() == java.util.Date.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, java.util.Date.class);
                                invokeMethod.invoke(ob, resultSet.getDate(name));
                                continue;
                            }

                            if (method.getReturnType() == byte.class) {
                                Method invokeMethod = obj.getClass().getMethod(fieldName, byte.class);
                                invokeMethod.invoke(ob, resultSet.getByte(name));
                                continue;
                            }

                            if (method.getReturnType() == BigDecimal.class) {
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

        } catch (Exception ex) {
            System.err.println("Persistor: criteria error at \n" + ex.getMessage());
        } finally {
            CloseRS(resultSet);
            CloseStatement(statement);
        }
    }
}
