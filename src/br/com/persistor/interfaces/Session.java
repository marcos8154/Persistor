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

    void update(Object entity, String and_or_condition);

    /**
     * Delete entity from database and context(if initialized)
     * @param entity 
     */
    void delete(Object entity);

    /**
     * Delete entity from database and context(if initialized)
     * Example: session.delete(people, "and id > 10");
     * @param entity instance of a valid entity to delete
     * @param and_or_condition where / or aditional condition
     */
    void delete(Object entity, String and_or_condition);

    void commit();

    void rollback();

    /**
     * Return loaded loaded entity by informed id and entity class
     * If PersistenceContext is enabled, is right recommended use
     * this method to load entity by id
     * 
     * Example:
     *      People people = session.onID(People.class, 7);
     * 
     * @param <T> type returned (not necessary cast of type entity)
     * @param entityCls class of entity to load
     * @param id primary key id value to get db reccord
     * @return 
     */
    <T> T onID(Class entityCls, int id);

    /**
     * Return last reccord from database and load entity
     * 
     * Example:
     *    
     *     People p = session.
     * 
     * @param <T> type returned(not necessary cast of type entity)
     * @param entityClass class of entity to load
     * @param whereCondition where conditions
     * @return 
     */
    <T> T last(Class entityClass, String... whereCondition);

    <T> T first(Class entityClass, String... whereCondition);

    <T> List<T> getList(T t);

    void close();

    Criteria createCriteria(Object entity, RESULT_TYPE result_type);

    Query createQuery(Object entity, String queryCommand);

    Connection getActiveConnection();

    DBConfig getConfig();
    
    int count(Class entityClass, String... whereCondition);

    double sum(Class entityClass, String columnName, String... whereCondition);
}
