package test;

import enums.DB_TYPE;
import generalClasses.DBConfig;
import sessionManager.SessionFactory;

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
