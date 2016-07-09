package br.com.persistor.test;

import br.com.persistor.enums.ResultType;
import br.com.persistor.sessionManager.SessionFactory;

public class main {

    public static void main(String[] args) {

        SessionFactory session = new ConfigureSession().getMySQLSession();

        Pessoa p = (Pessoa)session.onID(Pessoa.class, 1);
        
        session.close();
    }
}
