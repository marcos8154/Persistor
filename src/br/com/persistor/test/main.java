package br.com.persistor.test;

import br.com.persistor.interfaces.Session;

public class main
{

    public static void main(String[] args)
    {
        try
        {
            Session sessionM = ConfiguraSession.getSession();
            Profissao p = (Profissao) sessionM.onID(Profissao.class, 1);
            sessionM.close();
        }
        catch (Exception ex)
        {

        }
    }
}
