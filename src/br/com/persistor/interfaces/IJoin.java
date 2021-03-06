package br.com.persistor.interfaces;

import java.util.List;

import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.generalClasses.Limit;

public interface IJoin
{

    void addJoin(JOIN_TYPE join_type, Object obj, String condition);

    // void addJoin(JOIN_TYPE join_type, Object obj);
    void addFinalCondition(String final_and_or_where_condition);

    String detectJoin(JOIN_TYPE join_type);

    <T> List<T>  getResultList(Object obj);

    void execute(Session iSession) throws Exception;
   
    void addLimit(Limit limit);
}
