package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{
    
    public static void main(String[] args)
    {
        Armazens armazen = new Armazens();
        Session session = getSession();
        
        armazen.setDescricao("Depósito 1");
        armazen.setTipo(2);
        session.onID(armazen, 3);
        session.commit();
    }
    
    private static Session getSession()
    {
        try
        {
            DBConfig config = new DBConfig();
            config.setPersistenceLogger(LogTest.class.getName());
            //  config.setPersistenceContext(Contexto.class.getName());
            config.setDb_type(DB_TYPE.SQLServer);
            config.setHost("localhost");
            config.setPort(1433);
            config.setUser("sa");
            config.setPassword("81547686");
            config.setDatabase("sig");
            
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
