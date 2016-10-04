package br.com.persistor.interfaces;

import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public interface Session
{

    void save(Object entity) throws Exception;

    void update(Object entity) throws Exception;

    void update(Object entity, String andCondition) throws Exception;

    void delete(Object entity) throws Exception;

    void delete(Object entity, String andCondition) throws Exception;

    void commit();

    void rollback();

    void onID(Object entity, int id) throws Exception;

    <T> T onID(Class entityCls, int id) throws Exception;

    <T> T Last(Class entity, String whereCondition) throws Exception;

    <T> T First(Class entity, String whereCondition) throws Exception;
    
    <T> List<T> getList(T t) throws Exception;
    
    void close();

    Criteria createCriteria(Object entity, RESULT_TYPE result_type);

    Query createQuery(Object entity, String queryCommand) throws Exception;

    Connection getActiveConnection();

    void closeResultSet(ResultSet resultSet);

    void closeStatement(Statement statement);

    void closePreparedStatement(PreparedStatement preparedStatement);

    DBConfig getConfig();

    void loadWithJoin(Object sourceEntity, Object targetEntity) throws Exception;
}
