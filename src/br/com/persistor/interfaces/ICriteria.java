package br.com.persistor.interfaces;

import br.com.persistor.generalClasses.Expressions;

public interface ICriteria
{
    ICriteria add(Expressions expression);

    void execute();
}
