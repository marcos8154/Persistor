package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;
import br.com.persistor.sessionManager.SessionImpl;

public class ConfiguraSession {

    private static SessionFactory sfMySQL = null;
    private static SessionFactory sfPg = null;

    public static Session getSession()
    {
        try 
        {
            if (sfMySQL == null) {
                sfMySQL = new SessionFactory();
            }

            DBConfig config = new DBConfig();

            config.setDb_type(DB_TYPE.MySQL);
            config.setHost("localhost");
            config.setDatabase("controleagora");
            config.setPort(3306);
            config.setUser("root");
            config.setPassword("81547686");

            return sfMySQL.getSession(config);
        } 
        catch (Exception ex) {

        }
        
        return null;
    }

    /*  public Session getFbSession()
    {
        DBConfig config = new DBConfig();

        config.setDb_type(DB_TYPE.FirebirdSQL);
        config.setHost("localhost");
        config.setDatabase("Users/marcosvinicius/NetBeansProjects/Persistor/banco.fdb");
        config.setPort(3050);
        config.setUser("SYSDBA");
        config.setPassword("masterkey");

        Session sessionFactory = SessionFactory.getSession(config);
        return sessionFactory;
    } */

 /*    public static Session getPgSession()
    {
        if(sfPg == null) sfPg = new SessionFactory();
        
        DBConfig config = new DBConfig();

        config.setDb_type(DB_TYPE.PostgreSQL);
        config.setHost("localhost");
        config.setDatabase("cadastro");
        config.setPort(5432);
        config.setUser("postgres");
        config.setPassword("81547686");

        Session session = sfPg.getSession(config);

        return session;
    }  */
}
