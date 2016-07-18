package br.com.persistor.test;

import br.com.persistor.enums.COMMIT_MODE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.sessionManager.Query;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{
    public static void main(String[] args)
    {
        SessionFactory sessionFactory = new ConfigureSession().getMySQLSession();
        
        Pessoa pessoa = new Pessoa();
        
        Query query = sessionFactory.createQuery(pessoa, "select*from pessoa");
        query.setResult_type(RESULT_TYPE.MULTIPLE);
        
        query.execute();
        query.execute();
        
        sessionFactory.close();
    }
}
