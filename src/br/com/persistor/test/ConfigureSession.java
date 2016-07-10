package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.sessionManager.SessionFactory;

public class ConfigureSession
{

    public SessionFactory getMySQLSession()
    {
        DBConfig config = new DBConfig();

        config.setDb_type(DB_TYPE.MySQL);
        config.setHost("localhost");
        config.setDatabase("cadastro");
        config.setPort(3306);
        config.setUser("root");
        config.setPassword("81547686");

        SessionFactory sessionFactory = new SessionFactory(config);

        return sessionFactory;
    }

    public SessionFactory getFbSession()
    {
        DBConfig config = new DBConfig();
        
        config.setDb_type(DB_TYPE.FirebirdSQL);
        config.setHost("localhost");
        config.setDatabase("c:/viva/banco.fdb");
        config.setPort(3050);
        config.setUser("SYSDBA");
        config.setPassword("masterkey");
        
        SessionFactory sessionFactory = new SessionFactory(config);
        return sessionFactory;
    }
    
    public SessionFactory getPgSession()
    {
        DBConfig config = new DBConfig();

        config.setDb_type(DB_TYPE.PostgreSQL);
        config.setHost("localhost");
        config.setDatabase("cadastro");
        config.setPort(5432);
        config.setUser("postgres");
        config.setPassword("81547686");

        SessionFactory sessionFactory = new SessionFactory(config);

        return sessionFactory;
    }
}
