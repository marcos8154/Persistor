package br.com.persistor.test;

import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.LIMIT;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.Query;
import br.com.persistor.sessionManager.SessionFactory;
import java.util.List;

public class main
{

    public static void main(String[] args)
    {
        SessionFactory session = new ConfigureSession().getPgSession();

        
     /*   for (int i = 0; i < 3000; i++)
        {

            Pessoa pessoa = new Pessoa();
            pessoa.setNome("Marcos 3");
            pessoa.setEmail("marcos8154@gmail.com");
            pessoa.setTelefone("999486444");
            pessoa.setEndereco("Rua vogue, 166");
            pessoa.setHabilitado(true);
            pessoa.setVeiculo_id(1);

            session.save(pessoa);
           
        }
        
        session.commit(); */
        
        Pessoa pessoa = new Pessoa();
        Criteria criteria = session.createCriteria(pessoa, RESULT_TYPE.MULTIPLE);
        
        criteria.add(Restrictions.eq(FILTER_TYPE.WHERE, "nome", "Mar"));
        criteria.addLimit(LIMIT.paginate(30, 10));
        
        criteria.execute(session);

   
        session.close();
    }
}
