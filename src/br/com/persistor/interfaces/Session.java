package br.com.persistor.interfaces;

import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.PersistenceContext;
import br.com.persistor.sessionManager.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public interface Session
{

    PersistenceContext getPersistenceContext();

    IPersistenceLogger getPersistenceLogger();

    void save(Object entity);

    void update(Object entity);

    void update(Object entity, String andCondition);

    void delete(Object entity);

    void delete(Object entity, String andCondition);

    void commit();

    void rollback();

    void onID(Object entity, int id);

    <T> T onID(Class entityCls, int id);

    <T> T Last(Class entity, String whereCondition);

    <T> T First(Class entity, String whereCondition);

    <T> List<T> getList(T t);

    void close();

    Criteria createCriteria(Object entity, RESULT_TYPE result_type);

    Query createQuery(Object entity, String queryCommand);

    Connection getActiveConnection();

    void closeResultSet(ResultSet resultSet);

    void closeStatement(Statement statement);

    void closePreparedStatement(PreparedStatement preparedStatement);

    DBConfig getConfig();

    void loadWithJoin(Object sourceEntity, Object targetEntity);
}
