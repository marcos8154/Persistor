package br.com.persistor.test;

import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.Session;

public class main
{

    public static void main(String[] args)
    {
        Session sessionFactory = new ConfigureSession().getMySQLSession();
        
        Pessoa pessoa = new Pessoa();
        
        Criteria cri = sessionFactory.createCriteria(pessoa, RESULT_TYPE.UNIQUE);
        cri.add(Restrictions.eq(FILTER_TYPE.WHERE, "nome", "b"));
        cri.add(Restrictions.eq(FILTER_TYPE.AND, "nome", "b"));
        cri.execute();;
        
        sessionFactory.commit();
        sessionFactory.close();
    }
}
