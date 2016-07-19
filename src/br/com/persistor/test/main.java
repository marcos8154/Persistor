package br.com.persistor.test;

import br.com.persistor.sessionManager.SessionFactory;

public class main
{

    public static void main(String[] args)
    {
        try
        {
            SessionFactory session = new ConfigureSession().getMySQLSession();

            
            session.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
