package br.com.persistor.test;

import br.com.persistor.sessionManager.SessionFactory;

public class main
{

    public static void main(String[] args)
    {
        try
        {
            SessionFactory session = new ConfigureSession().getMySQLSession();
            
            Cliente cliente = new Cliente();
            cliente.setLoja(1);
            cliente.setId(1);
            cliente.setNome("Pimeirop alt");
            
            session.update(cliente, "loja = 1");
            session.commit();
            session.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
