package br.com.persistor.test;

import br.com.persistor.enums.ResultType;
import br.com.persistor.sessionManager.SessionFactory;

public class main {

    public static void main(String[] args) {

        
        SessionFactory session = new ConfigureSession().getFbSession();

        Pessoa pessoa = new Pessoa();
        
        pessoa.setId(1);
        pessoa.setNome("Alterando...");
        
        session.save(pessoa);
        session.commit();
        
        session.close(); 
    }
}
