package br.com.persistor.interfaces;

import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.generalClasses.Expressions;
import java.util.List;

public interface ICriteria
{
    ICriteria add(Expressions expression);

    ICriteria add(JOIN_TYPE join_type, Object entity, String joinCondition);
    
    ICriteria execute();
    
    ICriteria loadEntity(Object entity);
    
    ICriteria loadList(Object entity);
}
