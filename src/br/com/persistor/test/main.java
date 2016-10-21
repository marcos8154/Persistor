package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{

    public static void main(String[] args)
    {
        Pessoa ve = new Pessoa();

        Session session = getSession();
      //  session.save(ve);
        session.createCriteria(ve, RESULT_TYPE.UNIQUE).add(Restrictions.eq(FILTER_TYPE.WHERE, "is", "4"))
                .execute();
        
        session.close();
    }

    private static void Ha(Pessoa p)
    {
        Pessoa t = p;
        t = new Pessoa();
        t.setNome("Ta");
        p = t;
    }

    private static Session getSession()
    {
        try
        {
            DBConfig config = new DBConfig();

            config.setPersistenceLogger(LogTest.class.getName());
           // config.setPersistenceContext("br.com.persistor.test.Context");
            config.setDb_type(DB_TYPE.MySQL);
            config.setHost("localhost");
            config.setPort(3306);
            config.setUser("root");
            config.setPassword("81547686");
            config.setDatabase("cadastro");

            SessionFactory sf = new SessionFactory();
            return sf.getSession(config);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        return null;
    }
}
