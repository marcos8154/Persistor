package br.com.persistor.interfaces;

import br.com.persistor.enums.ResultType;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.Query;

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

    Criteria createCriteria(Object obj, ResultType result_type);
    
    Query createQuery(Class cls, ResultType resultType, String queryCommand);
}
