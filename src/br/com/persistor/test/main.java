package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;
import br.com.persistor.sessionManager.Util;
import java.util.List;
import javax.swing.JOptionPane;

public class main
{

    public static void main(String[] args)
    {
       Util.runPresentation();
    }

    private static Session getSession()
    {
        try
        {
            DBConfig config = new DBConfig();
            config.setPersistenceLogger(LogTest.class);
            config.setPersistenceContext(Contexto.class);
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
