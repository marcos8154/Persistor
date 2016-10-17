package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{
    
    public static void main(String[] args)
    {
        
    }
    
    private static Session getSession()
    {
        try
        {
            DBConfig config = new DBConfig();
            
            config.setPersistenceContext("br.com.persistor.test.Contexto");
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
