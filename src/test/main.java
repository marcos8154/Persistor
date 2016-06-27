package test;

import sessionManager.SessionFactory;

public class main
{
    
    public static void main(String[] args)
    {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("Teste OneToMany");
        
        SessionFactory session = new ConfigureSession().getMySQLSession();
       
        session.onID(pessoa, 1);
        
        session.close();
    }
}
