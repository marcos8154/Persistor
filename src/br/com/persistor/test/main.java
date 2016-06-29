package br.com.persistor.test;

import br.com.persistor.enums.ResultType;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{

    public static void main(String[] args)
    {
        SessionFactory session = new ConfigureSession().getMySQLSession();
        
//        Profissao profissao = new Profissao();
//        
//        profissao.setDescricao("Técnico de redes");
//        
//        profissao.setPessoa_id(2);
//        
//        session.save(profissao);
//        session.commit();
        
        Pessoa pessoa = new Pessoa();
        
        session.createCriteria(pessoa, ResultType.MULTIPLE).execute(session);
        
        for(Object obj : pessoa.ResultList)
        {
            Pessoa p = (Pessoa)obj;
            System.out.println(p.getNome());
        }
        
        session.close();
    }
}
