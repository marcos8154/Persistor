package br.com.persistor.interfaces;

import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public interface ISession
{

    void save(Object obj);

    void update(Object obj);

    void update(Object obj, String andCondition);

    void delete(Object obj);

    void delete(Object obj, String andCondition);

    void commit();

    void rollback();

    void onID(Object obj, int id);

    Object onID(Class cls, int id);
    
    void close();

    Criteria createCriteria(Object obj, RESULT_TYPE result_type);
    
    Query createQuery(Object obj, String queryCommand);
    
    Connection getActiveConnection();
    
    void closeResultSet(ResultSet resultSet);
    
    void closeStatement(Statement statement);

    void closePreparedStatement(PreparedStatement preparedStatement);
    
    DBConfig getConfig();
}
