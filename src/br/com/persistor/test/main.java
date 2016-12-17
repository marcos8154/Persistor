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
import java.util.Date;

public class main
{

    public static void main(String[] args)
    {
        //  Util.runPresentation();

        try
        {
            Cidades c = new Cidades();
            Pessoa p = new Pessoa();
            
            Session session = getSession();
            session.createCriteria(p, RESULT_TYPE.MULTIPLE)
                    .addLimit(Limit.paginate(1, 5, "id"))
                    .execute();

            System.out.println("");
            System.out.println("*-----------------|RESULTADOS|-------------------*");
            for (Pessoa pes : session.getList(p))
            {
                System.out.println(pes.getId() + " " + pes.getNome());
            }
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
