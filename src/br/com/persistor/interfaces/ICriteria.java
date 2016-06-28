package br.com.persistor.interfaces;

import br.com.persistor.generalClasses.Expressions;
import br.com.persistor.sessionManager.SessionFactory;

public interface ICriteria
{
    void add(Expressions expression);

    void execute(SessionFactory sessionFactory);
}
