package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.EntitySet;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.Join;
import br.com.persistor.sessionManager.SessionFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
