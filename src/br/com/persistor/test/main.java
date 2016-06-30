package br.com.persistor.test;

import br.com.persistor.enums.ResultType;
import br.com.persistor.sessionManager.SessionFactory;

public class main
{

    public static void main(String[] args)
    {
        SessionFactory session = new ConfigureSession().getMySQLSession();
            
        Pessoa pessoa  = new Pessoa();
        
        pessoa.setId(2);
        pessoa.setVersion(4);
        pessoa.setNome("Alterando...");
        
        pessoa.setVeiculo_id(1);
        
        session.update(pessoa);
        session.commit();
        
        session.close();
    }
}
