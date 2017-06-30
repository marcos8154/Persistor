package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{

    public static void main(String[] args)
    {
        Session session = getSession();
        Produtos produto = session.onID(Produtos.class, 1);
        produto = session.onID(Produtos.class, 1);
        session.close();
        session = getSession();
        produto = session.onID(Produtos.class, 1);
        produto = session.onID(Produtos.class, 1);
        System.out.println(produto.getDescricao());
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
       // config.setPersistenceContext(Context.class);
        // config.setSlPersistenceContext(Context.class);
        config.setDb_type(DB_TYPE.MySQL);
        config.setHost("localhost");
        config.setPort(3306);
        config.setUser("root");
        config.setPassword("81547686");
        config.setDatabase("teste_cache");

        return config;
    }
}
