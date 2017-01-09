package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Limit;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;
import br.com.persistor.generalClasses.Util;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class main
{

    public static void main(String[] args)
    {
        Util.runPresentation();

        try
        {
            //Session session = getSession();
            
          //  Cotacoes cotacao = session.onID(Cotacoes.class, 2);
            System.out.println("");
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static Session getSession()
    {
        try
        {
            DBConfig config = new DBConfig();
            config.setPersistenceLogger(LogTest.class);
            // config.setPersistenceContext(Contexto.class);
            config.setDb_type(DB_TYPE.PostgreSQL);
            config.setHost("localhost");
            config.setPort(5432);
            config.setUser("postgres");
            config.setPassword("81547686");
            config.setDatabase("mmn_teste");
            config.setMaxPoolSize(1);

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
