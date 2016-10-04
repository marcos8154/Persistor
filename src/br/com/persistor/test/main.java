package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.Join;
import br.com.persistor.sessionManager.SessionFactory;
import br.com.persistor.sessionManager.SessionImpl;
import java.util.ArrayList;
import java.util.List;

public class main
{
    public static void main(String[] args)
    {
        try
        {
            SessionFactory sf = new SessionFactory();
            DBConfig config = new DBConfig();
            config.setHost("MARCOS\\SQLSERVER");
            config.setDatabase("sig");
            config.setUser("sa");
            config.setPassword("81547686");
            config.setDb_type(DB_TYPE.SQLServer);
            
            Session session = sf.getSession(config);
        
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }
    
    public static <T> List<T> onId()
    {
        return  new ArrayList<>();
    }
}
