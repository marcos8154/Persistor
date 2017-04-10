package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.MATCH_MODE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;
import java.util.List;

public class main
{

    public static void main(String[] args)
    {
        try
        {
            Session session = getSession();

            Produtos produto = new Produtos();
            produto.setDescricao("Teste");
            session.save(produto);
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static SessionFactory sf = null;

    private static Session getSession()
    {
        try
        {
            if (sf != null)
                return sf.getSession();
            else
                sf = new SessionFactory();

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

            return sf.getSession(config);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        return null;
    }
}
