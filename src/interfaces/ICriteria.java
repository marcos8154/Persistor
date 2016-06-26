package interfaces;

import generalClasses.Expressions;
import sessionManager.SessionFactory;

public interface ICriteria
{
    void add(Expressions expression);

    void execute(SessionFactory sessionFactory);
}
