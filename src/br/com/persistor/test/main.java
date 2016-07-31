package br.com.persistor.test;

import br.com.persistor.interfaces.Session;

public class main
{
    public static void main(String[] args)
    {
        Session sessionM = ConfiguraSession.getSession();
        Session sessionFb = ConfiguraSession.getPgSession();
        Pessoa p = new Pessoa();
   //     Session sessionFB = new SessionMySQL.getSession();
        
       // sessionM.close();
      //  sessionFB.close();
        
    }
}
