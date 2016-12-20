package br.com.persistor.interfaces;

import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.generalClasses.Expressions;
import java.util.List;

public interface ICriteria<T>
{
    ICriteria add(Expressions expression);

    ICriteria add(JOIN_TYPE join_type, Object entity, String joinCondition);
    
    ICriteria execute();
    
    T loadEntity(T entity);
    
    List<T> loadList(Object entity);
    
    ICriteria beginPrecedence();
    
    ICriteria endPrecedence();
}
