package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.ORDER_MODE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.interfaces.ICriteria;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;
import java.util.List;

public class main
{

    public static void main(String[] args)
    {
        Session session = getSession();
        Produtos produto = new Produtos();
        session.createQuery(produto, "select * from produtos").setResult_type(RESULT_TYPE.MULTIPLE).execute();
        session.createQuery(produto, "select * from produtos").setResult_type(RESULT_TYPE.MULTIPLE).execute();
        session.createQuery(produto, "select * from produtos").setResult_type(RESULT_TYPE.MULTIPLE).execute();
        session.createQuery(produto, "select * from produtos").setResult_type(RESULT_TYPE.MULTIPLE).execute();
        session.createQuery(produto, "select * from produtos").setResult_type(RESULT_TYPE.MULTIPLE).execute();
        session.createQuery(produto, "select * from produtos").setResult_type(RESULT_TYPE.MULTIPLE).execute();

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

    static String toCamelCase(String string)
    {
        StringBuffer sb = new StringBuffer(string);
        sb.replace(0, 1, string.substring(0, 1).toUpperCase());
        return sb.toString();

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
