package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Util;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{

    public static void main(String[] args)
    {
       // Banco banco = new Banco();
       // banco.Criar(getConfig());
        Util.runPresentation();
    }

    private static SessionFactory sf = null;

    private static Session getSession()
    {
        try
        {
            if (sf == null)
            {
                sf = new SessionFactory();
                return sf.getSession(getConfig());
            }
            return sf.getSession();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static DBConfig getConfig()
    {
        DBConfig config = new DBConfig();
        config.setPersistenceLogger(LogTest.class);
        //  config.setPersistenceContext(Context.class);
        config.setSlPersistenceContext(Context.class);
        config.setDb_type(DB_TYPE.MySQL);
        config.setHost("localhost");
        config.setPort(3306);
        config.setUser("root");
        config.setPassword("81547686");
        config.setDatabase("teste_cache");

        return config;
    }
}
