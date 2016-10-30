package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.MATCH_MODE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.Join;
import br.com.persistor.sessionManager.SessionFactory;
import java.util.ArrayList;
import java.util.List;

public class main
{

    public static void main(String[] args)
    {
        Pessoa p = new Pessoa();
        Veiculo v = new Veiculo();
        Session session = getSession();

        session.createCriteria(p, RESULT_TYPE.MULTIPLE)
                .add(JOIN_TYPE.INNER, v, "pessoa.veiculo_id = veiculo.id")
                .add(Restrictions.ge(FILTER_TYPE.WHERE, "pessoa.id", 5))
                .add(Restrictions.like(FILTER_TYPE.AND, "pessoa.nome", "Eva", MATCH_MODE.START))
                .execute()
                .loadList(p)
                .loadList(v);

        //System.out.println(p.getNome());
       
     
        for (Pessoa pes : session.getList(p))
        {
            System.out.println(pes.getNome());
        }

        /*  Join join = new Join(p);
        join.addJoin(JOIN_TYPE.INNER, v, "pessoa.veiculo_id = veiculo.id");
        join.execute(session);

        p = join.getEntity(Pessoa.class);
        p.ResultList = join.getList(p);
        System.out.println(p.getNome());

        for (Pessoa pes : session.getList(p))
        {
            System.out.println(pes.getNome());
        } */
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
