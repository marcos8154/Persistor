package test;

import enums.ResultType;
import sessionManager.Criteria;
import sessionManager.SessionFactory;

public class main {

    public static void main(String[] args) {
        SessionFactory session = new ConfigureSession().getMySQLSession();

        Pessoa pessoa = new Pessoa();
        
        pessoa.setNome("Primeira pessoa");
        
        session.update(pessoa, "");
        
        session.save(pessoa);
        session.commit();
    }
}
