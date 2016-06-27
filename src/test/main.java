package test;

import sessionManager.SessionFactory;

public class main
{
    
    public static void main(String[] args)
    {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("outrs iclusao");
        
        pessoa.setVeiculo_id(1);
        
        SessionFactory session = new ConfigureSession().getMySQLSession();
       
       session.save(pessoa);
session.commit();
        session.close();
    }
}
