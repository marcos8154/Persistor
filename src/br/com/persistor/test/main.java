package br.com.persistor.test;

import br.com.persistor.enums.PARAMETER_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.sessionManager.Query;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{
    
    public static void main(String[] args)
    {
        SessionFactory factory = new ConfigureSession().getMySQLSession();
        Pessoa pessoa = new Pessoa();
        
        Query q = factory.createQuery(pessoa, "select*from pessoa where id = ?");

        q.setParameter(1, 9);
        
        //  q.setParameter(2, "Teste Named Query");
        q.setResult_type(RESULT_TYPE.UNIQUE);
        
        q.execute();
        
        factory.close();
    }
}
