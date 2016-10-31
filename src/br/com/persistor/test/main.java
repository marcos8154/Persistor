package br.com.persistor.test;

import br.com.persistor.connectionManager.Configuration;
import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.Query;
import br.com.persistor.sessionManager.SessionFactory;
import javax.swing.JOptionPane;

public class main
{

    public static void main(String[] args)
    {
        Fields fs = new Fields();
        Session session = getSession();
        Query q = session.createQuery(fs, "desc pessoa");
        q.setResult_type(RESULT_TYPE.MULTIPLE);
        q.execute();
    }

    private static Session getSession()
    {
        try
        {
            DBConfig config = new DBConfig();
            config.setPersistenceLogger(LogTest.class.getName());
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
