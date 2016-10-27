package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.MATCH_MODE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.Query;
import br.com.persistor.sessionManager.SessionFactory;
import java.util.List;

public class main
{

    public static void main(String[] args)
    {
        Pessoa p = new Pessoa();
        Veiculo v = new Veiculo();
        Session session = getSession();
        
        p.setId(16);
        session.loadWithJoin(p, v);
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
