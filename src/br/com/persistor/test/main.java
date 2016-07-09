package br.com.persistor.test;

import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.sessionManager.SessionFactory;

public class main {

    public static void main(String[] args) {
        Object obj = new Pessoa();

        Class<?> cls = obj.getClass();
        cls.getTypeName();

        SessionFactory s = new SessionFactory(new DBConfig());
            
        Pessoa p = (Pessoa)s.onID(Pessoa.class, 01);
    }
}
