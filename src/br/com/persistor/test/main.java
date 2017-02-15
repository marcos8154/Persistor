package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Table;
import br.com.persistor.generalClasses.Util;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{

    public static void main(String[] args)
    {
          Util.runPresentation();

        try
        {
          
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static Session getSession()
    {
        try
        {
            DBConfig config = new DBConfig();
            config.setPersistenceLogger(LogTest.class);
            config.setPersistenceContext(Context.class);
            config.setDb_type(DB_TYPE.PostgreSQL);
            config.setHost("localhost");
            config.setPort(5432);
            config.setUser("postgres");
            config.setPassword("81547686");
            config.setDatabase("contratos");
            config.setMaxPoolSize(1);

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
